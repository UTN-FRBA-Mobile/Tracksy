from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from drf_spectacular.utils import extend_schema, extend_schema_view, OpenApiParameter

from apps.common.permissions import IsOwnerOrAdmin
from .models import ShoppingList, ShoppingListItem
from .serializers import (
    ShoppingListSerializer,
    ShoppingListCreateSerializer,
    ShoppingListUpdateSerializer,
    ShoppingListItemSerializer,
    ShoppingListItemUpdateSerializer,
)
from .services import ShoppingListEstimatorService


@extend_schema(tags=["shopping-lists"])
@extend_schema_view(
    list=extend_schema(summary="Listar las listas de compra del usuario"),
    create=extend_schema(summary="Crear una lista de compra"),
    retrieve=extend_schema(summary="Detalle de una lista con todos sus ítems"),
    partial_update=extend_schema(summary="Actualizar nombre, sucursal o estado de la lista"),
    destroy=extend_schema(summary="Eliminar una lista de compra"),
)
class ShoppingListViewSet(viewsets.ModelViewSet):
    """CRUD de listas de compra — acceso solo al propietario."""

    permission_classes = [IsAuthenticated, IsOwnerOrAdmin]
    filterset_fields = ["status"]
    ordering_fields = ["created_at", "updated_at", "name"]
    ordering = ["-updated_at"]
    http_method_names = ["get", "post", "patch", "delete", "head", "options"]

    def get_queryset(self):
        return (
            ShoppingList.objects.filter(user=self.request.user)
            .select_related("preferred_branch__chain")
            .prefetch_related(
                "items__product__brand",
                "items__product__images",
            )
        )

    def get_serializer_class(self):
        if self.action == "create":
            return ShoppingListCreateSerializer
        if self.action == "partial_update":
            return ShoppingListUpdateSerializer
        return ShoppingListSerializer

    # ── Items nested under the list ─────────────────────────────────────────

    @extend_schema(summary="Agregar un producto a la lista")
    @action(detail=True, methods=["post"], url_path="items")
    def add_item(self, request, pk=None):
        shopping_list = self.get_object()
        serializer = ShoppingListItemSerializer(data=request.data, context={"request": request})
        serializer.is_valid(raise_exception=True)

        from apps.products.models import Product
        product = Product.objects.get(id=serializer.validated_data["product_id"])

        item, created = ShoppingListItem.objects.get_or_create(
            shopping_list=shopping_list,
            product=product,
            defaults={
                "quantity": serializer.validated_data.get("quantity", 1),
                "notes": serializer.validated_data.get("notes", ""),
            },
        )
        if not created:
            item.quantity += serializer.validated_data.get("quantity", 1)
            item.save()

        return Response(
            ShoppingListItemSerializer(item).data,
            status=status.HTTP_201_CREATED if created else status.HTTP_200_OK,
        )

    @extend_schema(summary="Actualizar cantidad o estado de compra de un ítem")
    @action(
        detail=True,
        methods=["patch"],
        url_path=r"items/(?P<item_id>[0-9a-f-]+)",
    )
    def update_item(self, request, pk=None, item_id=None):
        shopping_list = self.get_object()
        item = ShoppingListItem.objects.get(id=item_id, shopping_list=shopping_list)
        serializer = ShoppingListItemUpdateSerializer(item, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(ShoppingListItemSerializer(item).data)

    @extend_schema(summary="Eliminar un producto de la lista")
    @action(
        detail=True,
        methods=["delete"],
        url_path=r"items/(?P<item_id>[0-9a-f-]+)/delete",
    )
    def remove_item(self, request, pk=None, item_id=None):
        shopping_list = self.get_object()
        ShoppingListItem.objects.filter(id=item_id, shopping_list=shopping_list).delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

    # ── Estimation & comparison ──────────────────────────────────────────────

    @extend_schema(
        summary="Calcular costo estimado de la lista en la sucursal preferida",
        parameters=[
            OpenApiParameter(
                "branch_id",
                str,
                description="UUID de la sucursal (si no se especifica, usa la preferida de la lista)",
                required=False,
            )
        ],
    )
    @action(detail=True, methods=["get"], url_path="estimate")
    def estimate(self, request, pk=None):
        shopping_list = self.get_object()
        branch_id = request.query_params.get("branch_id")

        if branch_id:
            from apps.supermarkets.models import SupermarketBranch
            branch = SupermarketBranch.objects.filter(id=branch_id, is_active=True).first()
            if not branch:
                return Response({"detail": "Sucursal no encontrada."}, status=400)
        else:
            branch = shopping_list.preferred_branch

        if not branch:
            return Response(
                {"detail": "No hay sucursal asociada. Pasa branch_id como parámetro."},
                status=400,
            )

        data = ShoppingListEstimatorService.estimate_for_branch(shopping_list, branch)
        return Response(data)

    @extend_schema(
        summary="Comparar el costo total de la lista entre todos los supermercados disponibles"
    )
    @action(detail=True, methods=["get"], url_path="compare-supermarkets")
    def compare_supermarkets(self, request, pk=None):
        shopping_list = self.get_object()
        results = ShoppingListEstimatorService.compare_all_branches(shopping_list)
        return Response(
            {
                "list_id": str(shopping_list.id),
                "list_name": shopping_list.name,
                "comparisons": results,
                "best_option": results[0] if results else None,
            }
        )
