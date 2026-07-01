from django import forms
from django.contrib import admin, messages
from django.shortcuts import redirect, render
from django.urls import path
from django.utils.html import format_html

from .models import ImportBatch, ImportError
from .services import ListadoImportService, MarcaImportService, ProductImportService, SupermercadoImportService


class CsvUploadForm(forms.Form):
    csv_file = forms.FileField(
        label="Archivo CSV",
        widget=forms.ClearableFileInput(attrs={"accept": ".csv"}),
    )


class ImportErrorInline(admin.TabularInline):
    model = ImportError
    extra = 0
    readonly_fields = ("row_number", "field", "error_message", "raw_data")
    can_delete = False
    max_num = 0
    verbose_name = "Error"
    verbose_name_plural = "Errors"


def _status_badge(status):
    colors = {
        ImportBatch.STATUS_COMPLETED: "#7c5cbf",
        ImportBatch.STATUS_FAILED: "#dc3545",
        ImportBatch.STATUS_PARTIAL: "#c97b2e",
        ImportBatch.STATUS_PROCESSING: "#9b7fd4",
        ImportBatch.STATUS_PENDING: "#8e7aaa",
    }
    labels = {
        ImportBatch.STATUS_COMPLETED: "Completado",
        ImportBatch.STATUS_FAILED: "Fallido",
        ImportBatch.STATUS_PARTIAL: "Parcial",
        ImportBatch.STATUS_PROCESSING: "Procesando",
        ImportBatch.STATUS_PENDING: "Pendiente",
    }
    color = colors.get(status, "#6c757d")
    label = labels.get(status, status)
    return format_html(
        '<span style="background:{};color:#fff;padding:2px 8px;border-radius:4px;font-size:0.85em">{}</span>',
        color,
        label,
    )


_SERVICE_MAP = {
    ImportBatch.TYPE_PRODUCTS: ProductImportService,
    ImportBatch.TYPE_SUPERMARKETS: SupermercadoImportService,
    ImportBatch.TYPE_LISTADOS: ListadoImportService,
    ImportBatch.TYPE_MARCAS: MarcaImportService,
}

_UPLOAD_URLS = {
    ImportBatch.TYPE_PRODUCTS: "imports_productos_upload",
    ImportBatch.TYPE_SUPERMARKETS: "imports_supermercados_upload",
    ImportBatch.TYPE_LISTADOS: "imports_listados_upload",
    ImportBatch.TYPE_MARCAS: "imports_marcas_upload",
}

_CSV_HELP = {
    ImportBatch.TYPE_PRODUCTS: "Columnas requeridas: <code>codigo_barra</code>, <code>nombre</code> — Opcional: <code>marca_nombre</code>",
    ImportBatch.TYPE_SUPERMARKETS: "Columnas requeridas: <code>nombre</code>, <code>direccion</code>, <code>latitud</code>, <code>longitud</code>",
    ImportBatch.TYPE_LISTADOS: "Columnas requeridas: <code>producto_id</code>, <code>supermercado_id</code>, <code>precio</code>, <code>disponible</code>",
    ImportBatch.TYPE_MARCAS: "Columnas requeridas: <code>nombre</code>",
}


@admin.register(ImportBatch)
class ImportBatchAdmin(admin.ModelAdmin):
    inlines = [ImportErrorInline]
    list_display = (
        "import_type",
        "colored_status",
        "total_rows",
        "success_rows",
        "error_rows",
        "uploaded_by",
        "created_at",
        "duration",
    )
    list_filter = ("import_type", "status")
    readonly_fields = (
        "status",
        "total_rows",
        "processed_rows",
        "success_rows",
        "error_rows",
        "started_at",
        "finished_at",
    )
    ordering = ("-created_at",)

    def get_urls(self):
        urls = super().get_urls()
        custom = [
            path(
                "upload/productos/",
                self.admin_site.admin_view(
                    lambda req: self._csv_upload_view(req, ImportBatch.TYPE_PRODUCTS)
                ),
                name=_UPLOAD_URLS[ImportBatch.TYPE_PRODUCTS],
            ),
            path(
                "upload/marcas/",
                self.admin_site.admin_view(
                    lambda req: self._csv_upload_view(req, ImportBatch.TYPE_MARCAS)
                ),
                name=_UPLOAD_URLS[ImportBatch.TYPE_MARCAS],
            ),
            path(
                "upload/supermercados/",
                self.admin_site.admin_view(
                    lambda req: self._csv_upload_view(req, ImportBatch.TYPE_SUPERMARKETS)
                ),
                name=_UPLOAD_URLS[ImportBatch.TYPE_SUPERMARKETS],
            ),
            path(
                "upload/listados/",
                self.admin_site.admin_view(
                    lambda req: self._csv_upload_view(req, ImportBatch.TYPE_LISTADOS)
                ),
                name=_UPLOAD_URLS[ImportBatch.TYPE_LISTADOS],
            ),
        ]
        return custom + urls

    def save_model(self, request, obj, form, change):
        super().save_model(request, obj, form, change)
        if not change:
            service_cls = _SERVICE_MAP.get(obj.import_type)
            if service_cls:
                service_cls(obj).process()
                obj.refresh_from_db()

    def _csv_upload_view(self, request, import_type):
        if request.method == "POST":
            form = CsvUploadForm(request.POST, request.FILES)
            if form.is_valid():
                batch = ImportBatch.objects.create(
                    uploaded_by=request.user,
                    import_type=import_type,
                    file=form.cleaned_data["csv_file"],
                )
                service_class = _SERVICE_MAP[import_type]
                service_class(batch).process()
                batch.refresh_from_db()
                level = (
                    messages.SUCCESS
                    if batch.status == ImportBatch.STATUS_COMPLETED
                    else messages.WARNING
                )
                messages.add_message(
                    request,
                    level,
                    f"Importación finalizada: {batch.success_rows} correctas, "
                    f"{batch.error_rows} errores de {batch.total_rows} filas totales.",
                )
                return redirect(f"../../{batch.pk}/change/")
        else:
            form = CsvUploadForm()

        display_name = dict(ImportBatch.TYPE_CHOICES).get(import_type, import_type)
        form.fields["csv_file"].help_text = format_html(_CSV_HELP[import_type])

        context = {
            **self.admin_site.each_context(request),
            "form": form,
            "title": f"Importar CSV — {display_name}",
            "opts": self.model._meta,
        }
        return render(request, "admin/imports/csv_upload.html", context)

    @admin.display(description="Estado")
    def colored_status(self, obj):
        return _status_badge(obj.status)

    @admin.display(description="Duración")
    def duration(self, obj):
        if obj.started_at and obj.finished_at:
            secs = int((obj.finished_at - obj.started_at).total_seconds())
            return f"{secs}s"
        return "—"
