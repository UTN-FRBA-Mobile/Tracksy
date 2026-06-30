from rest_framework import serializers
from .models import Compra, ProductoComprado


class ProductoCompradoSerializer(serializers.ModelSerializer):
    producto_nombre = serializers.CharField(source="producto.nombre", read_only=True)

    class Meta:
        model = ProductoComprado
        fields = ["id", "compra", "producto", "producto_nombre", "cantidad", "precio_unitario"]
        read_only_fields = ["id"]


class CompraSerializer(serializers.ModelSerializer):
    productos = ProductoCompradoSerializer(many=True, read_only=True)
    supermercado_nombre = serializers.CharField(
        source="supermercado.nombre", read_only=True
    )

    class Meta:
        model = Compra
        fields = [
            "id",
            "usuario",
            "supermercado",
            "supermercado_nombre",
            "nombre_lista",
            "fecha",
            "total",
            "productos",
        ]
        read_only_fields = ["id", "usuario", "fecha"]

    def create(self, validated_data):
        validated_data["usuario"] = self.context["request"].user
        return super().create(validated_data)


class ProductoCompradoCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductoComprado
        fields = ["producto", "cantidad", "precio_unitario"]


class CompraCreateSerializer(serializers.ModelSerializer):
    productos = ProductoCompradoCreateSerializer(many=True)

    class Meta:
        model = Compra
        fields = ["supermercado", "nombre_lista", "total", "productos"]

    def create(self, validated_data):
        productos_data = validated_data.pop("productos")
        validated_data["usuario"] = self.context["request"].user
        compra = Compra.objects.create(**validated_data)
        for item in productos_data:
            ProductoComprado.objects.create(compra=compra, **item)
        return compra
