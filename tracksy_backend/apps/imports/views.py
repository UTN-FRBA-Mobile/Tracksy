from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from drf_spectacular.utils import extend_schema, extend_schema_view

from apps.common.permissions import IsAdminOrBackoffice
from .models import ImportBatch
from .serializers import ImportBatchSerializer
from .services import PriceImportService, ProductImportService, SepaZipImportService


SERVICE_MAP = {
    ImportBatch.TYPE_PRODUCTS: ProductImportService,
    ImportBatch.TYPE_PRICES: PriceImportService,
    ImportBatch.TYPE_SEPA_ZIP: SepaZipImportService,
}


@extend_schema(tags=["imports"])
@extend_schema_view(
    list=extend_schema(summary="Listar lotes de importación"),
    create=extend_schema(summary="Subir un archivo CSV para importar datos"),
    retrieve=extend_schema(summary="Ver detalle y errores de un lote"),
)
class ImportBatchViewSet(viewsets.ModelViewSet):
    """
    Upload CSV files to import products, prices, supermarkets, etc.
    The processing runs synchronously; for large files connect a Celery worker.
    """

    permission_classes = [IsAdminOrBackoffice]
    serializer_class = ImportBatchSerializer
    filterset_fields = ["import_type", "status"]
    ordering = ["-created_at"]
    http_method_names = ["get", "post", "head", "options"]

    def get_queryset(self):
        return ImportBatch.objects.prefetch_related("errors").all()

    def perform_create(self, serializer):
        batch = serializer.save()
        service_cls = SERVICE_MAP.get(batch.import_type)
        if service_cls:
            service_cls(batch).process()

    @extend_schema(summary="Re-procesar un lote fallido")
    @action(detail=True, methods=["post"], url_path="reprocess")
    def reprocess(self, request, pk=None):
        batch = self.get_object()
        if batch.status == ImportBatch.STATUS_PROCESSING:
            return Response({"detail": "El lote ya está siendo procesado."}, status=400)

        batch.errors.all().delete()
        batch.processed_rows = 0
        batch.success_rows = 0
        batch.error_rows = 0
        batch.save()

        service_cls = SERVICE_MAP.get(batch.import_type)
        if not service_cls:
            return Response({"detail": "Tipo de importación no soportado."}, status=400)

        service_cls(batch).process()
        return Response(ImportBatchSerializer(batch).data)
