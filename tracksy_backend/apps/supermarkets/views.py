from rest_framework import viewsets
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from drf_spectacular.utils import extend_schema, extend_schema_view, OpenApiParameter

from apps.common.permissions import IsAdminOrBackoffice
from .models import SupermarketChain, SupermarketBranch
from .serializers import (
    SupermarketChainSerializer,
    SupermarketChainDetailSerializer,
    SupermarketChainWriteSerializer,
    SupermarketBranchSerializer,
    SupermarketBranchListSerializer,
    SupermarketBranchWriteSerializer,
)
from .services import NearbyBranchService


@extend_schema(tags=["supermarkets"])
@extend_schema_view(
    list=extend_schema(summary="Listar cadenas de supermercados"),
    retrieve=extend_schema(summary="Detalle de una cadena con sus sucursales"),
)
class SupermarketChainViewSet(viewsets.ReadOnlyModelViewSet):
    permission_classes = [IsAuthenticated]
    search_fields = ["name"]
    ordering = ["name"]

    def get_queryset(self):
        return SupermarketChain.objects.filter(is_active=True).prefetch_related("branches")

    def get_serializer_class(self):
        if self.action == "retrieve":
            return SupermarketChainDetailSerializer
        return SupermarketChainSerializer

    @extend_schema(
        tags=["promotions"],
        summary="Promociones vigentes de una cadena",
    )
    @action(detail=True, methods=["get"], url_path="promotions")
    def promotions(self, request, pk=None):
        from apps.promotions.serializers import PromotionListSerializer
        from apps.promotions.models import Promotion

        chain = self.get_object()
        promotions = Promotion.objects.active().filter(
            promotion_supermarkets__supermarket=chain
        ).distinct()
        serializer = PromotionListSerializer(promotions, many=True, context={"request": request})
        return Response(serializer.data)


@extend_schema(tags=["supermarkets"])
@extend_schema_view(
    list=extend_schema(summary="Listar todas las sucursales"),
    retrieve=extend_schema(summary="Detalle de una sucursal con horarios"),
)
class SupermarketBranchViewSet(viewsets.ReadOnlyModelViewSet):
    permission_classes = [IsAuthenticated]
    search_fields = ["name", "city", "province", "chain__name"]
    filterset_fields = ["city", "province", "chain"]
    ordering = ["chain__name", "name"]

    def get_queryset(self):
        return (
            SupermarketBranch.objects.filter(is_active=True)
            .select_related("chain")
            .prefetch_related("opening_hours")
        )

    def get_serializer_class(self):
        if self.action == "retrieve":
            return SupermarketBranchSerializer
        return SupermarketBranchListSerializer

    @extend_schema(
        summary="Sucursales cercanas a una coordenada geográfica",
        parameters=[
            OpenApiParameter("lat", float, description="Latitud del usuario", required=True),
            OpenApiParameter("lon", float, description="Longitud del usuario", required=True),
            OpenApiParameter("radius_km", float, description="Radio en km (default 10)", required=False),
        ],
    )
    @action(detail=False, methods=["get"], url_path="nearby")
    def nearby(self, request):
        lat = request.query_params.get("lat")
        lon = request.query_params.get("lon")
        radius_km = float(request.query_params.get("radius_km", 10))

        if not lat or not lon:
            return Response({"detail": "Los parámetros lat y lon son obligatorios."}, status=400)

        try:
            lat, lon = float(lat), float(lon)
        except ValueError:
            return Response({"detail": "lat y lon deben ser números válidos."}, status=400)

        branches = NearbyBranchService.get_nearby_branches(lat, lon, radius_km)
        serializer = SupermarketBranchListSerializer(
            branches, many=True, context={"request": request}
        )
        return Response(serializer.data)


# ── Backoffice ───────────────────────────────────────────────────────────────

@extend_schema(tags=["admin"])
class AdminSupermarketChainViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAdminOrBackoffice]
    serializer_class = SupermarketChainWriteSerializer
    queryset = SupermarketChain.objects.all()
    search_fields = ["name"]
    ordering = ["-created_at"]


@extend_schema(tags=["admin"])
class AdminSupermarketBranchViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAdminOrBackoffice]
    serializer_class = SupermarketBranchWriteSerializer
    queryset = SupermarketBranch.objects.select_related("chain").all()
    filterset_fields = ["chain", "city", "province", "is_active"]
    ordering = ["-created_at"]
