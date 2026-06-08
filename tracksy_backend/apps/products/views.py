from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated

from .models import Marca, Producto
from .serializers import MarcaSerializer, ProductoSerializer, ProductoDetalleSerializer


class MarcaViewSet(viewsets.ModelViewSet):
    queryset = Marca.objects.all()
    serializer_class = MarcaSerializer
    permission_classes = [IsAuthenticated]
    search_fields = ["nombre"]
    ordering = ["nombre"]


class ProductoViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    search_fields = ["nombre", "marca__nombre"]
    ordering_fields = ["nombre"]
    ordering = ["nombre"]

    def get_queryset(self):
        return Producto.objects.select_related("marca").all()

    def get_serializer_class(self):
        if self.action == "retrieve":
            return ProductoDetalleSerializer
        return ProductoSerializer
