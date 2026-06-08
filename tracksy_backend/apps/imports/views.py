from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from apps.common.permissions import IsAdminUser
from .models import ImportBatch
from .serializers import ImportBatchSerializer
from .services import ProductImportService, SupermercadoImportService, ListadoImportService, MarcaImportService


SERVICE_MAP = {
    ImportBatch.TYPE_PRODUCTS: ProductImportService,
    ImportBatch.TYPE_SUPERMARKETS: SupermercadoImportService,
    ImportBatch.TYPE_LISTADOS: ListadoImportService,
    ImportBatch.TYPE_MARCAS: MarcaImportService,
}


class ImportBatchViewSet(viewsets.ModelViewSet):
    """Subir archivos CSV para importar datos en lote. Solo administradores."""

    permission_classes = [IsAdminUser]
    serializer_class = ImportBatchSerializer
    filterset_fields = ["import_type", "status"]
    ordering = ["-created_at"]
    http_method_names = ["get", "post", "head", "options"]

    def get_queryset(self):
        return ImportBatch.objects.prefetch_related("errors").all()

    def perform_create(self, serializer):
        batch = serializer.save(uploaded_by=self.request.user)
        service_cls = SERVICE_MAP.get(batch.import_type)
        if service_cls:
            service_cls(batch).process()

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
