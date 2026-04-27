from django import forms
from django.contrib import admin, messages
from django.shortcuts import redirect, render
from django.urls import path
from django.utils.html import format_html

from .models import ImportBatch, ImportError
from .services import SepaZipImportService


class SepaZipUploadForm(forms.Form):
    zip_file = forms.FileField(
        label="ZIP file",
        help_text=(
            "Upload a <strong>.zip</strong> containing "
            "<code>comercio.csv</code>, <code>sucursales.csv</code> and "
            "<code>productos.csv</code> in SEPA format (pipe <code>|</code> delimited)."
        ),
        widget=forms.ClearableFileInput(attrs={"accept": ".zip"}),
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
        ImportBatch.STATUS_COMPLETED: "#28a745",
        ImportBatch.STATUS_FAILED: "#dc3545",
        ImportBatch.STATUS_PARTIAL: "#fd7e14",
        ImportBatch.STATUS_PROCESSING: "#007bff",
        ImportBatch.STATUS_PENDING: "#6c757d",
    }
    labels = {
        ImportBatch.STATUS_COMPLETED: "Completed",
        ImportBatch.STATUS_FAILED: "Failed",
        ImportBatch.STATUS_PARTIAL: "Partial",
        ImportBatch.STATUS_PROCESSING: "Processing",
        ImportBatch.STATUS_PENDING: "Pending",
    }
    color = colors.get(status, "#6c757d")
    label = labels.get(status, status)
    return format_html(
        '<span style="background:{};color:#fff;padding:2px 8px;border-radius:4px;font-size:0.85em">{}</span>',
        color,
        label,
    )


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
                "sepa-zip-upload/",
                self.admin_site.admin_view(self._sepa_zip_upload_view),
                name="imports_sepa_zip_upload",
            ),
        ]
        return custom + urls

    def _sepa_zip_upload_view(self, request):
        if request.method == "POST":
            form = SepaZipUploadForm(request.POST, request.FILES)
            if form.is_valid():
                batch = ImportBatch.objects.create(
                    uploaded_by=request.user,
                    import_type=ImportBatch.TYPE_SEPA_ZIP,
                    file=form.cleaned_data["zip_file"],
                )
                SepaZipImportService(batch).process()
                batch.refresh_from_db()
                level = (
                    messages.SUCCESS
                    if batch.status == ImportBatch.STATUS_COMPLETED
                    else messages.WARNING
                )
                messages.add_message(
                    request,
                    level,
                    f"SEPA import finished: {batch.success_rows} successful, "
                    f"{batch.error_rows} errors out of {batch.total_rows} total rows.",
                )
                return redirect(f"../{batch.pk}/change/")
        else:
            form = SepaZipUploadForm()

        context = {
            **self.admin_site.each_context(request),
            "form": form,
            "title": "Import SEPA ZIP",
            "opts": self.model._meta,
        }
        return render(request, "admin/imports/sepa_zip_upload.html", context)

    @admin.display(description="Status")
    def colored_status(self, obj):
        return _status_badge(obj.status)

    @admin.display(description="Duration")
    def duration(self, obj):
        if obj.started_at and obj.finished_at:
            secs = int((obj.finished_at - obj.started_at).total_seconds())
            return f"{secs}s"
        return "—"
