from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from django.db.models import Q
from django.db.models.functions import Cast
from django.db.models import CharField

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

    def filter_queryset(self, queryset):
        queryset = super().filter_queryset(queryset)
        search = self.request.query_params.get("search", "").strip()
        if search.isdigit():
            barcode_qs = (
                Producto.objects.select_related("marca")
                .annotate(id_str=Cast("id", output_field=CharField()))
                .filter(id_str__startswith=search)
            )
            queryset = (queryset | barcode_qs).distinct()
        return queryset

    def get_serializer_class(self):
        if self.action == "retrieve":
            return ProductoDetalleSerializer
        return ProductoSerializer
