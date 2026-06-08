from rest_framework import serializers
from .models import Marca, Producto


class MarcaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Marca
        fields = ["id", "nombre"]


class ProductoSerializer(serializers.ModelSerializer):
    marca_nombre = serializers.CharField(source="marca.nombre", read_only=True)

    class Meta:
        model = Producto
        fields = ["id", "nombre", "marca", "marca_nombre"]


class ProductoDetalleSerializer(serializers.ModelSerializer):
    marca = MarcaSerializer(read_only=True)
    marca_id = serializers.PrimaryKeyRelatedField(
        queryset=Marca.objects.all(), source="marca", write_only=True, allow_null=True
    )

    class Meta:
        model = Producto
        fields = ["id", "nombre", "marca", "marca_id"]
