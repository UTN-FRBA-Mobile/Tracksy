from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated

from .models import Supermercado, ProductoListado
from .serializers import SupermercadoSerializer, ProductoListadoSerializer


class SupermercadoViewSet(viewsets.ModelViewSet):
    queryset = Supermercado.objects.all()
    serializer_class = SupermercadoSerializer
    permission_classes = [IsAuthenticated]
    search_fields = ["nombre", "direccion"]
    ordering = ["nombre"]


class ProductoListadoViewSet(viewsets.ModelViewSet):
    serializer_class = ProductoListadoSerializer
    permission_classes = [IsAuthenticated]
    filterset_fields = ["supermercado", "producto", "disponible"]
    ordering = ["producto__nombre"]

    def get_queryset(self):
        return ProductoListado.objects.select_related(
            "producto", "supermercado"
        ).all()
