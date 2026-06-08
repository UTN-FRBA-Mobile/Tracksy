from django.db import models
from django.contrib.auth import get_user_model
from apps.common.models import BaseModel

User = get_user_model()


class ImportBatch(BaseModel):
    TYPE_PRODUCTS = "products"
    TYPE_SUPERMARKETS = "supermarkets"
    TYPE_LISTADOS = "listados"
    TYPE_MARCAS = "marcas"

    TYPE_CHOICES = [
        (TYPE_PRODUCTS, "Productos"),
        (TYPE_SUPERMARKETS, "Supermercados"),
        (TYPE_LISTADOS, "Listados de precios"),
        (TYPE_MARCAS, "Marcas"),
    ]

    STATUS_PENDING = "pending"
    STATUS_PROCESSING = "processing"
    STATUS_COMPLETED = "completed"
    STATUS_FAILED = "failed"
    STATUS_PARTIAL = "partial"

    STATUS_CHOICES = [
        (STATUS_PENDING, "Pending"),
        (STATUS_PROCESSING, "Processing"),
        (STATUS_COMPLETED, "Completed"),
        (STATUS_FAILED, "Failed"),
        (STATUS_PARTIAL, "Partial"),
    ]

    uploaded_by = models.ForeignKey(
        User, on_delete=models.SET_NULL, null=True, related_name="import_batches"
    )
    import_type = models.CharField(max_length=20, choices=TYPE_CHOICES)
    file = models.FileField(upload_to="imports/")
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default=STATUS_PENDING)
    total_rows = models.PositiveIntegerField(default=0)
    processed_rows = models.PositiveIntegerField(default=0)
    success_rows = models.PositiveIntegerField(default=0)
    error_rows = models.PositiveIntegerField(default=0)
    started_at = models.DateTimeField(null=True, blank=True)
    finished_at = models.DateTimeField(null=True, blank=True)
    notes = models.TextField(blank=True)

    class Meta:
        db_table = "import_batches"
        verbose_name = "Import Batch"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.get_import_type_display()} — {self.status} ({self.created_at:%Y-%m-%d})"


class ImportError(BaseModel):
    batch = models.ForeignKey(ImportBatch, on_delete=models.CASCADE, related_name="errors")
    row_number = models.PositiveIntegerField()
    field = models.CharField(max_length=100, blank=True)
    error_message = models.TextField()
    raw_data = models.JSONField(default=dict)

    class Meta:
        db_table = "import_errors"
        verbose_name = "Import Error"
        ordering = ["row_number"]
