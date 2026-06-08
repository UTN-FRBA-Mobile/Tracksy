from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated

from .models import Compra, ProductoComprado
from .serializers import CompraSerializer, CompraCreateSerializer, ProductoCompradoSerializer


class CompraViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    ordering = ["-fecha"]

    def get_queryset(self):
        return Compra.objects.filter(
            usuario=self.request.user
        ).select_related("supermercado").prefetch_related("productos__producto")

    def get_serializer_class(self):
        if self.action == "create":
            return CompraCreateSerializer
        return CompraSerializer

    def perform_create(self, serializer):
        serializer.save(usuario=self.request.user)


class ProductoCompradoViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = ProductoCompradoSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return ProductoComprado.objects.filter(
            compra__usuario=self.request.user
        ).select_related("producto", "compra")
