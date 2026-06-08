"""
Servicios de importación CSV para cada entidad.
Interfaz común: service.process() procesa el batch completo.
"""
import csv
import io
import logging
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
            with open(self.batch.file.path, "r", encoding="utf-8-sig") as f:
                content = f.read()
            # Strip comment lines (starting with #) before parsing
            clean_lines = [ln for ln in content.splitlines() if not ln.lstrip().startswith("#")]
            clean_content = "\n".join(clean_lines)
            reader = csv.DictReader(io.StringIO(clean_content))
            rows = list(reader)
            self.batch.total_rows = len(rows)
            self.batch.save(update_fields=["total_rows"])

            for i, row in enumerate(rows, start=2):
                missing = [f for f in self.REQUIRED_FIELDS if not row.get(f, "").strip()]
                if missing:
                    self._add_error(i, ", ".join(missing), f"Campos requeridos faltantes: {missing}", row)
                    continue
                self._process_row(i, row)

        except Exception as exc:
            logger.exception("Import batch %s falló", self.batch.id)
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
        # Count error rows as processed so success_rows = processed_rows - error_rows >= 0
        self._inc_processed()

    def _inc_processed(self):
        self.batch.processed_rows += 1
        ImportBatch.objects.filter(pk=self.batch.pk).update(
            processed_rows=self.batch.processed_rows
        )


class ProductImportService(BaseImportService):
    """
    CSV esperado: codigo_barra (EAN-13 = PK), nombre, marca_nombre
    El campo `codigo_barra` es la clave primaria del Producto.
    """
    REQUIRED_FIELDS = ["codigo_barra", "nombre"]

    def _process_row(self, row_num: int, row: dict):
        from apps.products.models import Marca, Producto

        barcode_str = row["codigo_barra"].strip()
        try:
            barcode_id = int(barcode_str)
        except ValueError:
            self._add_error(row_num, "codigo_barra", f"El código de barras '{barcode_str}' no es un número válido.", row)
            return

        nombre = row["nombre"].strip()
        marca = None
        if row.get("marca_nombre", "").strip():
            marca, _ = Marca.objects.get_or_create(nombre=row["marca_nombre"].strip())

        Producto.objects.update_or_create(
            id=barcode_id,
            defaults={"nombre": nombre, "marca": marca},
        )
        self._inc_processed()


class SupermercadoImportService(BaseImportService):
    """
    CSV esperado: nombre, direccion, latitud, longitud
    Crea o actualiza Supermercado.
    """
    REQUIRED_FIELDS = ["nombre", "direccion", "latitud", "longitud"]

    def _process_row(self, row_num: int, row: dict):
        from apps.supermarkets.models import Supermercado

        try:
            lat = Decimal(row["latitud"].strip())
            lon = Decimal(row["longitud"].strip())
        except (InvalidOperation, ValueError):
            self._add_error(row_num, "latitud/longitud", "Valores decimales inválidos.", row)
            return

        Supermercado.objects.update_or_create(
            nombre=row["nombre"].strip(),
            defaults={
                "direccion": row["direccion"].strip(),
                "latitud": lat,
                "longitud": lon,
            },
        )
        self._inc_processed()


class ListadoImportService(BaseImportService):
    """
    CSV esperado: producto_id, supermercado_id, precio, disponible
    Crea o actualiza ProductoListado.
    """
    REQUIRED_FIELDS = ["producto_id", "supermercado_id", "precio"]

    def _process_row(self, row_num: int, row: dict):
        from apps.products.models import Producto
        from apps.supermarkets.models import Supermercado, ProductoListado

        try:
            producto = Producto.objects.get(pk=int(row["producto_id"].strip()))
        except (Producto.DoesNotExist, ValueError):
            self._add_error(row_num, "producto_id", "Producto no encontrado.", row)
            return

        try:
            supermercado = Supermercado.objects.get(pk=int(row["supermercado_id"].strip()))
        except (Supermercado.DoesNotExist, ValueError):
            self._add_error(row_num, "supermercado_id", "Supermercado no encontrado.", row)
            return

        try:
            precio = Decimal(row["precio"].strip())
            if precio <= 0:
                raise ValueError
        except (InvalidOperation, ValueError):
            self._add_error(row_num, "precio", "Precio inválido.", row)
            return

        disponible = row.get("disponible", "true").strip().lower() != "false"

        ProductoListado.objects.update_or_create(
            producto=producto,
            supermercado=supermercado,
            defaults={"precio": precio, "disponible": disponible},
        )
        self._inc_processed()


class MarcaImportService(BaseImportService):
    """
    CSV esperado: nombre
    Crea o actualiza Marca.
    """
    REQUIRED_FIELDS = ["nombre"]

    def _process_row(self, row_num: int, row: dict):
        from apps.products.models import Marca
        nombre = row["nombre"].strip()
        Marca.objects.get_or_create(nombre=nombre)
        self._inc_processed()
