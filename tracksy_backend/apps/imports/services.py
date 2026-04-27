"""
CSV / ZIP import services for each entity type.
Each service follows a common interface:
  process(batch: ImportBatch) -> None
"""
import csv
import io
import logging
import re
import zipfile
from datetime import datetime, time
from decimal import Decimal, InvalidOperation

from django.utils import timezone

from .models import ImportBatch, ImportError

logger = logging.getLogger(__name__)


class BaseImportService:
    REQUIRED_FIELDS: list[str] = []

    def __init__(self, batch: ImportBatch):
        self.batch = batch
        self.errors: list[ImportError] = []

    def process(self):
        self.batch.status = ImportBatch.STATUS_PROCESSING
        self.batch.started_at = timezone.now()
        self.batch.save(update_fields=["status", "started_at"])

        try:
            content = self.batch.file.read().decode("utf-8-sig")
            reader = csv.DictReader(io.StringIO(content))
            rows = list(reader)
            self.batch.total_rows = len(rows)
            self.batch.save(update_fields=["total_rows"])

            for i, row in enumerate(rows, start=2):
                missing = [f for f in self.REQUIRED_FIELDS if not row.get(f, "").strip()]
                if missing:
                    self._add_error(i, ", ".join(missing), f"Required fields missing: {missing}", row)
                    continue
                self._process_row(i, row)

        except Exception as exc:
            logger.exception("Import batch %s failed", self.batch.id)
            self.batch.status = ImportBatch.STATUS_FAILED
            self.batch.notes = str(exc)
        else:
            self.batch.status = (
                ImportBatch.STATUS_PARTIAL if self.errors else ImportBatch.STATUS_COMPLETED
            )

        self.batch.finished_at = timezone.now()
        self.batch.error_rows = len(self.errors)
        self.batch.success_rows = self.batch.processed_rows - self.batch.error_rows
        self.batch.save()

        if self.errors:
            ImportError.objects.bulk_create(self.errors, batch_size=500)

    def _process_row(self, row_num: int, row: dict):
        raise NotImplementedError

    def _add_error(self, row_num: int, field: str, message: str, raw_data: dict):
        self.errors.append(
            ImportError(
                batch=self.batch,
                row_number=row_num,
                field=field,
                error_message=message,
                raw_data=raw_data,
            )
        )

    def _inc_processed(self):
        self.batch.processed_rows += 1
        ImportBatch.objects.filter(pk=self.batch.pk).update(
            processed_rows=self.batch.processed_rows
        )


class ProductImportService(BaseImportService):
    REQUIRED_FIELDS = ["barcode", "name"]

    def _process_row(self, row_num: int, row: dict):
        from apps.products.models import Brand, Category, Product

        barcode = row["barcode"].strip()
        if not barcode.isalnum():
            self._add_error(row_num, "barcode", "Invalid barcode (alphanumeric only).", row)
            return

        brand = None
        if row.get("brand_name"):
            brand, _ = Brand.objects.get_or_create(
                name=row["brand_name"].strip(),
                defaults={"slug": row["brand_name"].strip().lower().replace(" ", "-")},
            )

        category = None
        if row.get("category_name"):
            category, _ = Category.objects.get_or_create(
                name=row["category_name"].strip(),
                defaults={"slug": row["category_name"].strip().lower().replace(" ", "-")},
            )

        Product.objects.update_or_create(
            barcode=barcode,
            defaults={
                "name": row["name"].strip(),
                "description": row.get("description", "").strip(),
                "brand": brand,
                "category": category,
                "presentation": row.get("presentation", "").strip(),
                "net_content": row.get("net_content", "").strip(),
                "is_active": row.get("is_active", "true").strip().lower() != "false",
            },
        )
        self._inc_processed()


class PriceImportService(BaseImportService):
    REQUIRED_FIELDS = ["barcode", "branch_code", "price", "valid_from"]

    def _process_row(self, row_num: int, row: dict):
        from apps.prices.models import ProductPrice
        from apps.products.models import Product
        from apps.supermarkets.models import SupermarketBranch

        product = Product.objects.filter(barcode=row["barcode"].strip(), is_active=True).first()
        if not product:
            self._add_error(row_num, "barcode", f"Product with barcode {row['barcode']} not found.", row)
            return

        branch = SupermarketBranch.objects.filter(code=row["branch_code"].strip(), is_active=True).first()
        if not branch:
            self._add_error(row_num, "branch_code", f"Branch {row['branch_code']} not found.", row)
            return

        try:
            price = Decimal(row["price"].strip())
            if price <= 0:
                raise ValueError
        except (InvalidOperation, ValueError):
            self._add_error(row_num, "price", "Invalid price.", row)
            return

        try:
            valid_from = datetime.strptime(row["valid_from"].strip(), "%Y-%m-%d").date()
        except ValueError:
            self._add_error(row_num, "valid_from", "Invalid date format. Use YYYY-MM-DD.", row)
            return

        ProductPrice.objects.update_or_create(
            product=product,
            branch=branch,
            valid_from=valid_from,
            defaults={"price": price, "source": "import", "is_active": True},
        )
        self._inc_processed()


# ── SEPA ZIP import ───────────────────────────────────────────────────────────

_HORARIO_RE = re.compile(r"(\d{1,2}):(\d{2})\s+a\s+(\d{1,2}):(\d{2})")

_HORARIO_FIELDS = [
    ("sucursales_lunes_horario_atencion", 0),
    ("sucursales_martes_horario_atencion", 1),
    ("sucursales_miercoles_horario_atencion", 2),
    ("sucursales_jueves_horario_atencion", 3),
    ("sucursales_viernes_horario_atencion", 4),
    ("sucursales_sabado_horario_atencion", 5),
    ("sucursales_domingo_horario_atencion", 6),
]


def _parse_horario(value: str):
    """'08:00 a 21:00' → (time(8, 0), time(21, 0)) or (None, None)."""
    m = _HORARIO_RE.search(value)
    if m:
        h1, m1, h2, m2 = (int(x) for x in m.groups())
        return time(h1, m1), time(h2, m2)
    return None, None


def _parse_decimal(value: str):
    try:
        return Decimal(value.strip().replace(",", "."))
    except (InvalidOperation, AttributeError, ValueError):
        return None


def _v(row: dict, key: str) -> str:
    """Safe row value — DictReader fills missing columns with None, not ''."""
    return (row.get(key) or "").strip()


def _read_pipe_csv(content: str) -> list[dict]:
    reader = csv.DictReader(io.StringIO(content), delimiter="|")
    return list(reader)


class SepaZipImportService(BaseImportService):
    """
    Processes a ZIP with SEPA-format pipe-delimited CSVs:
      comercio.csv   → SupermarketChain
      sucursales.csv → SupermarketBranch + BranchOpeningHours
      productos.csv  → Product + Brand + ProductPrice (precio_lista)

    Processing order matters: chains first, then branches, then products.
    SEPA CSVs often have a trailing footer/comment row — those are skipped
    silently when the id_bandera column is absent/None.
    """

    ENCODING = "latin-1"

    def process(self):
        self.batch.status = ImportBatch.STATUS_PROCESSING
        self.batch.started_at = timezone.now()
        self.batch.save(update_fields=["status", "started_at"])

        try:
            raw_zip = self.batch.file.read()
            with zipfile.ZipFile(io.BytesIO(raw_zip)) as zf:
                names = set(zf.namelist())
                csv_data = {}
                total_rows = 0

                for fname in ["comercio.csv", "sucursales.csv", "productos.csv"]:
                    if fname in names:
                        content = zf.read(fname).decode(self.ENCODING, errors="replace")
                        csv_data[fname] = content
                        # don't count footer/comment rows (id_bandera is None)
                        rows = _read_pipe_csv(content)
                        total_rows += sum(1 for r in rows if r.get("id_bandera") is not None)

                self.batch.total_rows = total_rows
                self.batch.save(update_fields=["total_rows"])

                chains: dict = {}
                if "comercio.csv" in csv_data:
                    chains = self._process_comercios(csv_data["comercio.csv"])

                branches: dict = {}
                if "sucursales.csv" in csv_data:
                    branches = self._process_sucursales(csv_data["sucursales.csv"], chains)

                if "productos.csv" in csv_data:
                    self._process_productos(csv_data["productos.csv"], branches)

        except zipfile.BadZipFile:
            self.batch.status = ImportBatch.STATUS_FAILED
            self.batch.notes = "The uploaded file is not a valid ZIP archive."
        except Exception as exc:
            logger.exception("SEPA ZIP import batch %s failed", self.batch.id)
            self.batch.status = ImportBatch.STATUS_FAILED
            self.batch.notes = str(exc)
        else:
            self.batch.status = (
                ImportBatch.STATUS_PARTIAL if self.errors else ImportBatch.STATUS_COMPLETED
            )

        self.batch.finished_at = timezone.now()
        self.batch.error_rows = len(self.errors)
        self.batch.success_rows = self.batch.processed_rows - self.batch.error_rows
        self.batch.save()

        if self.errors:
            ImportError.objects.bulk_create(self.errors, batch_size=500)

    def _process_row(self, row_num, row):
        pass  # not used — process() is fully overridden

    def _process_comercios(self, content: str) -> dict:
        """Returns {id_bandera: SupermarketChain}."""
        from slugify import slugify

        from apps.supermarkets.models import SupermarketChain

        chains = {}
        rows = _read_pipe_csv(content)

        for i, row in enumerate(rows, start=2):
            # SEPA files end with a footer comment row — id_bandera is None there
            if row.get("id_bandera") is None:
                continue

            id_bandera = _v(row, "id_bandera")
            nombre = _v(row, "comercio_bandera_nombre")
            if not id_bandera or not nombre:
                self._add_error(i, "id_bandera/comercio_bandera_nombre", "Required fields empty.", row)
                continue

            chain, _ = SupermarketChain.objects.update_or_create(
                slug=slugify(nombre),
                defaults={
                    "name": nombre,
                    "website": _v(row, "comercio_bandera_url"),
                    "is_active": True,
                },
            )
            chains[id_bandera] = chain
            self._inc_processed()

        return chains

    def _process_sucursales(self, content: str, chains: dict) -> dict:
        """Returns {(id_bandera, id_sucursal): SupermarketBranch}."""
        from apps.supermarkets.models import BranchOpeningHours, SupermarketBranch

        branches = {}
        rows = _read_pipe_csv(content)

        for i, row in enumerate(rows, start=2):
            if row.get("id_bandera") is None:
                continue

            id_bandera = _v(row, "id_bandera")
            id_sucursal = _v(row, "id_sucursal")
            nombre = _v(row, "sucursales_nombre")

            chain = chains.get(id_bandera)
            if not chain:
                self._add_error(
                    i, "id_bandera",
                    f"Chain id_bandera={id_bandera} not found in comercio.csv.", row,
                )
                continue

            calle = _v(row, "sucursales_calle")
            numero = _v(row, "sucursales_numero")
            address = f"{calle} {numero}".strip()

            branch, _ = SupermarketBranch.objects.update_or_create(
                chain=chain,
                code=f"{id_bandera}-{id_sucursal}",
                defaults={
                    "name": nombre or f"{chain.name} #{id_sucursal}",
                    "address": address,
                    "city": _v(row, "sucursales_localidad"),
                    "province": _v(row, "sucursales_provincia"),
                    "postal_code": _v(row, "sucursales_codigo_postal"),
                    "latitude": _parse_decimal(_v(row, "sucursales_latitud")),
                    "longitude": _parse_decimal(_v(row, "sucursales_longitud")),
                    "is_active": True,
                },
            )

            for field_name, weekday in _HORARIO_FIELDS:
                horario = _v(row, field_name)
                if horario:
                    open_t, close_t = _parse_horario(horario)
                    if open_t and close_t:
                        BranchOpeningHours.objects.update_or_create(
                            branch=branch,
                            weekday=weekday,
                            defaults={
                                "open_time": open_t,
                                "close_time": close_t,
                                "is_closed": False,
                            },
                        )

            branches[(id_bandera, id_sucursal)] = branch
            self._inc_processed()

        return branches

    def _process_productos(self, content: str, branches: dict):
        from slugify import slugify

        from apps.prices.models import ProductPrice
        from apps.products.models import Brand, Product

        rows = _read_pipe_csv(content)
        today = timezone.now().date()

        for i, row in enumerate(rows, start=2):
            if row.get("id_bandera") is None:
                continue

            barcode = _v(row, "id_producto")
            descripcion = _v(row, "productos_descripcion")

            if not barcode or not descripcion:
                self._add_error(
                    i, "id_producto/productos_descripcion", "Required fields empty.", row
                )
                continue

            brand = None
            marca = _v(row, "productos_marca")
            if marca:
                brand, _ = Brand.objects.get_or_create(
                    name=marca,
                    defaults={"slug": slugify(marca)},
                )

            cantidad = _v(row, "productos_cantidad_presentacion")
            unidad = _v(row, "productos_unidad_medida_presentacion")
            presentation = f"{cantidad} {unidad}".strip() if cantidad else ""

            product, _ = Product.objects.update_or_create(
                barcode=barcode,
                defaults={
                    "name": descripcion,
                    "brand": brand,
                    "presentation": presentation,
                    "is_active": True,
                },
            )

            id_bandera = _v(row, "id_bandera")
            id_sucursal = _v(row, "id_sucursal")
            branch = branches.get((id_bandera, id_sucursal))
            precio_str = _v(row, "productos_precio_lista")

            if branch and precio_str:
                precio = _parse_decimal(precio_str)
                if precio and precio > 0:
                    ProductPrice.objects.update_or_create(
                        product=product,
                        branch=branch,
                        valid_from=today,
                        defaults={
                            "price": precio,
                            "source": ProductPrice.SOURCE_IMPORT,
                            "is_active": True,
                        },
                    )

            self._inc_processed()
