from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import EstadoProducto, ListaCompra, ItemProducto
from .serializers import (
    EstadoProductoSerializer,
    ListaCompraSerializer,
    ItemProductoSerializer,
)


class EstadoProductoViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = EstadoProducto.objects.all()
    serializer_class = EstadoProductoSerializer
    permission_classes = [IsAuthenticated]


class ListaCompraViewSet(viewsets.ModelViewSet):
    serializer_class = ListaCompraSerializer
    permission_classes = [IsAuthenticated]
    ordering = ["-fecha_creacion"]

    def get_queryset(self):
        return ListaCompra.objects.filter(
            usuario=self.request.user
        ).select_related("supermercado").prefetch_related(
            "items__producto", "items__estado"
        )

    def perform_create(self, serializer):
        serializer.save(usuario=self.request.user)

    @action(detail=True, methods=["post"], url_path="items")
    def agregar_item(self, request, pk=None):
        lista = self.get_object()
        data = {**request.data, "lista": lista.pk}
        serializer = ItemProductoSerializer(data=data)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)

    @action(detail=True, methods=["patch"], url_path=r"items/(?P<item_id>\d+)")
    def actualizar_item(self, request, pk=None, item_id=None):
        lista = self.get_object()
        item = ItemProducto.objects.get(pk=item_id, lista=lista)
        serializer = ItemProductoSerializer(item, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data)

    @action(detail=True, methods=["delete"], url_path=r"items/(?P<item_id>\d+)/delete")
    def eliminar_item(self, request, pk=None, item_id=None):
        lista = self.get_object()
        ItemProducto.objects.filter(pk=item_id, lista=lista).delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class ItemProductoViewSet(viewsets.ModelViewSet):
    serializer_class = ItemProductoSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return ItemProducto.objects.filter(
            lista__usuario=self.request.user
        ).select_related("producto", "estado")
