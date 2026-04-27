from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from drf_spectacular.utils import extend_schema, OpenApiParameter

from .services import PriceComparisonService, PromotionApplicationService, SupermarketRecommendationService


@extend_schema(tags=["comparisons"])
class ProductPriceComparisonView(APIView):
    """Compare a product's price across all available branches."""

    permission_classes = [IsAuthenticated]

    @extend_schema(
        summary="Comparar precios de un producto entre supermercados",
        parameters=[OpenApiParameter("product_id", str, required=True)],
    )
    def get(self, request):
        product_id = request.query_params.get("product_id")
        if not product_id:
            return Response({"detail": "product_id es requerido."}, status=400)

        from apps.products.models import Product
        product = Product.objects.filter(id=product_id, is_active=True).first()
        if not product:
            return Response({"detail": "Producto no encontrado."}, status=404)

        data = PriceComparisonService.compare_product_prices(product)
        return Response(data)


@extend_schema(tags=["comparisons"])
class SupermarketRecommendationView(APIView):
    """Recommend the cheapest branch for a set of products."""

    permission_classes = [IsAuthenticated]

    @extend_schema(
        summary="Recomendación de supermercado para una lista de productos",
        parameters=[
            OpenApiParameter(
                "product_ids",
                str,
                description="Lista de UUIDs separados por coma",
                required=True,
            )
        ],
    )
    def get(self, request):
        raw = request.query_params.get("product_ids", "")
        product_ids = [pid.strip() for pid in raw.split(",") if pid.strip()]
        if not product_ids:
            return Response({"detail": "product_ids es requerido."}, status=400)

        data = SupermarketRecommendationService.recommend(product_ids)
        return Response(data)


@extend_schema(tags=["comparisons"])
class ApplicablePromotionsView(APIView):
    """Return promotions that apply to a set of products at a given chain."""

    permission_classes = [IsAuthenticated]

    @extend_schema(
        summary="Promociones aplicables para un conjunto de productos en un supermercado",
        parameters=[
            OpenApiParameter("product_ids", str, required=True),
            OpenApiParameter("chain_id", str, required=True),
        ],
    )
    def get(self, request):
        raw = request.query_params.get("product_ids", "")
        chain_id = request.query_params.get("chain_id")
        product_ids = [pid.strip() for pid in raw.split(",") if pid.strip()]

        if not product_ids or not chain_id:
            return Response({"detail": "product_ids y chain_id son requeridos."}, status=400)

        data = PromotionApplicationService.get_applicable_promotions(product_ids, chain_id)
        return Response({"promotions": data, "count": len(data)})
