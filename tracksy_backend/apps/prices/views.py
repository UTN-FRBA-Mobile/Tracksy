from rest_framework import viewsets, generics
from rest_framework.permissions import IsAuthenticated
from drf_spectacular.utils import extend_schema, extend_schema_view, OpenApiParameter

from apps.common.permissions import IsAdminOrBackoffice
from .models import ProductPrice, PriceHistory
from .serializers import ProductPriceSerializer, PriceHistorySerializer, ProductPriceWriteSerializer


@extend_schema(tags=["prices"])
@extend_schema_view(
    list=extend_schema(
        summary="Búsqueda de precios por producto y/o supermercado",
        parameters=[
            OpenApiParameter("product", str, description="UUID del producto"),
            OpenApiParameter("branch", str, description="UUID de la sucursal"),
            OpenApiParameter("chain", str, description="UUID de la cadena"),
            OpenApiParameter("city", str, description="Ciudad de la sucursal"),
        ],
    )
)
class PriceSearchViewSet(viewsets.ReadOnlyModelViewSet):
    """Búsqueda de precios activos con filtros."""

    serializer_class = ProductPriceSerializer
    permission_classes = [IsAuthenticated]
    filterset_fields = ["product", "branch", "branch__chain", "branch__city", "currency"]
    ordering_fields = ["price", "updated_at"]
    ordering = ["price"]

    def get_queryset(self):
        return (
            ProductPrice.objects.filter(is_active=True)
            .select_related("product", "branch__chain")
        )


@extend_schema(tags=["prices"])
class PriceHistoryView(generics.ListAPIView):
    """Historial de precios de un producto en una sucursal."""

    serializer_class = PriceHistorySerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        product_id = self.kwargs.get("product_id")
        branch_id = self.kwargs.get("branch_id")
        qs = PriceHistory.objects.filter(product_id=product_id)
        if branch_id:
            qs = qs.filter(branch_id=branch_id)
        return qs


# ── Backoffice ───────────────────────────────────────────────────────────────

@extend_schema(tags=["admin"])
class AdminPriceViewSet(viewsets.ModelViewSet):
    """CRUD de precios — solo administradores/backoffice."""

    permission_classes = [IsAdminOrBackoffice]
    serializer_class = ProductPriceWriteSerializer
    filterset_fields = ["product", "branch", "is_active"]
    ordering = ["-updated_at"]

    def get_queryset(self):
        return ProductPrice.objects.select_related("product", "branch__chain").all()
