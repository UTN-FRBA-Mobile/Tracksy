from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from drf_spectacular.utils import extend_schema, extend_schema_view

from apps.common.permissions import IsAdminOrBackoffice
from .models import Promotion
from .serializers import PromotionListSerializer, PromotionDetailSerializer, PromotionWriteSerializer


@extend_schema(tags=["promotions"])
@extend_schema_view(
    list=extend_schema(summary="Listar promociones vigentes"),
    retrieve=extend_schema(summary="Detalle completo de una promoción"),
)
class PromotionViewSet(viewsets.ReadOnlyModelViewSet):
    """Promociones disponibles para el consumidor (solo lectura)."""

    permission_classes = [IsAuthenticated]
    filterset_fields = ["promotion_type", "promotion_supermarkets__supermarket"]
    search_fields = ["name", "description"]
    ordering_fields = ["start_date", "end_date", "name"]
    ordering = ["-start_date"]

    def get_queryset(self):
        return (
            Promotion.objects.active()
            .select_related("promotion_type")
            .prefetch_related(
                "promotion_supermarkets__supermarket",
                "promotion_products__product",
                "promotion_categories__category",
                "payment_methods",
                "days_of_week",
            )
        )

    def get_serializer_class(self):
        if self.action == "retrieve":
            return PromotionDetailSerializer
        return PromotionListSerializer


# ── Backoffice ────────────────────────────────────────────────────────────────

@extend_schema(tags=["admin"])
class AdminPromotionViewSet(viewsets.ModelViewSet):
    """CRUD de promociones — solo administradores/backoffice."""

    permission_classes = [IsAdminOrBackoffice]
    filterset_fields = ["is_active", "promotion_type"]
    search_fields = ["name"]
    ordering = ["-start_date"]

    def get_queryset(self):
        return Promotion.objects.select_related("promotion_type").prefetch_related(
            "promotion_supermarkets__supermarket",
            "promotion_products__product",
            "promotion_categories__category",
        ).all()

    def get_serializer_class(self):
        if self.action in ("create", "update", "partial_update"):
            return PromotionWriteSerializer
        if self.action == "retrieve":
            return PromotionDetailSerializer
        return PromotionListSerializer
