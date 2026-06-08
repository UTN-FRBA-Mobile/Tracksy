from django.contrib.auth import get_user_model
from rest_framework import serializers
from .models import ProductoUsuario

User = get_user_model()


class RegistroSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ["id", "nombre", "email", "password"]

    def create(self, validated_data):
        password = validated_data.pop("password")
        validated_data["username"] = validated_data["email"]
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user


class UsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ["id", "nombre", "email"]
        read_only_fields = ["id", "email"]


class CambiarPasswordSerializer(serializers.Serializer):
    password_actual = serializers.CharField(write_only=True)
    password_nuevo = serializers.CharField(write_only=True, min_length=8)

    def validate_password_actual(self, value):
        user = self.context["request"].user
        if not user.check_password(value):
            raise serializers.ValidationError("Contraseña actual incorrecta.")
        return value

    def save(self, **kwargs):
        user = self.context["request"].user
        user.set_password(self.validated_data["password_nuevo"])
        user.save()
        return user


class ProductoUsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductoUsuario
        fields = ["id", "usuario", "producto", "favorito"]
        read_only_fields = ["id", "usuario"]

    def create(self, validated_data):
        validated_data["usuario"] = self.context["request"].user
        obj, _ = ProductoUsuario.objects.update_or_create(
            usuario=validated_data["usuario"],
            producto=validated_data["producto"],
            defaults={"favorito": validated_data.get("favorito", False)},
        )
        return obj
