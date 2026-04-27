from rest_framework import viewsets, generics
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from drf_spectacular.utils import extend_schema, extend_schema_view

from apps.common.permissions import IsAdminOrBackoffice
from .models import Product, Category, Brand
from .serializers import (
    ProductListSerializer,
    ProductDetailSerializer,
    ProductWriteSerializer,
    CategorySerializer,
    BrandSerializer,
)
from .filters import ProductFilter


@extend_schema(tags=["products"])
@extend_schema_view(
    list=extend_schema(summary="Listar productos con filtros y búsqueda"),
    retrieve=extend_schema(summary="Detalle de un producto"),
)
class ProductViewSet(viewsets.ReadOnlyModelViewSet):
    """
    Endpoints de solo lectura para consumidores.
    Soporta búsqueda por nombre, marca, categoría y código de barras.
    """

    permission_classes = [IsAuthenticated]
    filterset_class = ProductFilter
    search_fields = ["name", "barcode", "brand__name", "description"]
    ordering_fields = ["name", "created_at"]
    ordering = ["name"]

    def get_queryset(self):
        return (
            Product.objects.filter(is_active=True)
            .select_related("brand", "category", "subcategory", "measurement_unit")
            .prefetch_related("images")
        )

    def get_serializer_class(self):
        if self.action == "retrieve":
            return ProductDetailSerializer
        return ProductListSerializer

    @extend_schema(summary="Buscar producto por código de barras")
    @action(detail=False, methods=["get"], url_path="barcode/(?P<barcode>[^/.]+)")
    def by_barcode(self, request, barcode=None):
        product = generics.get_object_or_404(
            Product.objects.filter(is_active=True)
            .select_related("brand", "category", "subcategory", "measurement_unit")
            .prefetch_related("images"),
            barcode=barcode,
        )
        serializer = ProductDetailSerializer(product, context={"request": request})
        return Response(serializer.data)

    @extend_schema(
        tags=["prices"],
        summary="Precios del producto en distintas sucursales",
    )
    @action(detail=True, methods=["get"], url_path="prices")
    def prices(self, request, pk=None):
        from apps.prices.serializers import ProductPriceSerializer
        from apps.prices.models import ProductPrice

        product = self.get_object()
        prices = (
            ProductPrice.objects.filter(product=product, is_active=True)
            .select_related("branch__chain")
            .order_by("price")
        )
        serializer = ProductPriceSerializer(prices, many=True, context={"request": request})
        return Response(serializer.data)

    @extend_schema(
        tags=["prices"],
        summary="Comparar precios de un producto entre supermercados",
    )
    @action(detail=True, methods=["get"], url_path="price-comparison")
    def price_comparison(self, request, pk=None):
        from apps.comparisons.services import PriceComparisonService

        product = self.get_object()
        data = PriceComparisonService.compare_product_prices(product)
        return Response(data)

    @extend_schema(
        tags=["promotions"],
        summary="Promociones vigentes para un producto",
    )
    @action(detail=True, methods=["get"], url_path="promotions")
    def promotions(self, request, pk=None):
        from apps.promotions.serializers import PromotionListSerializer
        from apps.promotions.models import Promotion

        product = self.get_object()
        promotions = Promotion.objects.active().filter(
            models.Q(promotion_products__product=product)
            | models.Q(promotion_categories__category=product.category)
        ).distinct()
        serializer = PromotionListSerializer(promotions, many=True, context={"request": request})
        return Response(serializer.data)


@extend_schema(tags=["products"])
class CategoryViewSet(viewsets.ReadOnlyModelViewSet):
    """Listar categorías y subcategorías disponibles."""

    queryset = Category.objects.filter(is_active=True).prefetch_related("subcategories")
    serializer_class = CategorySerializer
    permission_classes = [IsAuthenticated]
    search_fields = ["name"]
    ordering = ["name"]


@extend_schema(tags=["products"])
class BrandViewSet(viewsets.ReadOnlyModelViewSet):
    """Listar marcas disponibles."""

    queryset = Brand.objects.filter(is_active=True)
    serializer_class = BrandSerializer
    permission_classes = [IsAuthenticated]
    search_fields = ["name"]
    ordering = ["name"]


# ── Backoffice ──────────────────────────────────────────────────────────────

@extend_schema(tags=["admin"])
class AdminProductViewSet(viewsets.ModelViewSet):
    """CRUD completo de productos — solo administradores/backoffice."""

    permission_classes = [IsAdminOrBackoffice]
    serializer_class = ProductWriteSerializer
    search_fields = ["name", "barcode"]
    ordering_fields = ["name", "created_at"]
    ordering = ["-created_at"]

    def get_queryset(self):
        return Product.objects.select_related(
            "brand", "category", "subcategory", "measurement_unit"
        ).all()
