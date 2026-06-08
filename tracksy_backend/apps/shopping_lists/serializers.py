from rest_framework import serializers
from .models import EstadoProducto, ListaCompra, ItemProducto


class EstadoProductoSerializer(serializers.ModelSerializer):
    class Meta:
        model = EstadoProducto
        fields = ["id", "nombre"]


class ItemProductoSerializer(serializers.ModelSerializer):
    producto_nombre = serializers.CharField(source="producto.nombre", read_only=True)
    estado_nombre = serializers.CharField(source="estado.nombre", read_only=True)

    class Meta:
        model = ItemProducto
        fields = [
            "id",
            "lista",
            "producto",
            "producto_nombre",
            "cantidad",
            "estado",
            "estado_nombre",
            "precio_unitario",
        ]
        read_only_fields = ["id"]


class ListaCompraSerializer(serializers.ModelSerializer):
    items = ItemProductoSerializer(many=True, read_only=True)
    usuario_email = serializers.EmailField(source="usuario.email", read_only=True)

    class Meta:
        model = ListaCompra
        fields = [
            "id",
            "usuario",
            "usuario_email",
            "supermercado",
            "nombre",
            "fecha_creacion",
            "total_estimado",
            "items",
        ]
        read_only_fields = ["id", "usuario", "fecha_creacion"]

    def create(self, validated_data):
        validated_data["usuario"] = self.context["request"].user
        return super().create(validated_data)
