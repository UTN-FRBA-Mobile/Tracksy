from rest_framework import serializers
from .models import Supermercado, ProductoListado


class SupermercadoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Supermercado
        fields = ["id", "nombre", "direccion", "latitud", "longitud"]


class ProductoListadoSerializer(serializers.ModelSerializer):
    producto_nombre = serializers.CharField(source="producto.nombre", read_only=True)
    supermercado_nombre = serializers.CharField(
        source="supermercado.nombre", read_only=True
    )

    class Meta:
        model = ProductoListado
        fields = [
            "id",
            "producto",
            "producto_nombre",
            "supermercado",
            "supermercado_nombre",
            "precio",
            "disponible",
            "fecha_actualizacion",
        ]
        read_only_fields = ["id", "fecha_actualizacion"]
