from rest_framework import serializers
from .models import Estado, Sugerencia, FeedbackSugerencia


class EstadoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Estado
        fields = ["id", "nombre"]


class FeedbackSugerenciaSerializer(serializers.ModelSerializer):
    class Meta:
        model = FeedbackSugerencia
        fields = ["id", "sugerencia", "fue_util", "fecha"]
        read_only_fields = ["id", "fecha"]


class SugerenciaSerializer(serializers.ModelSerializer):
    estado_nombre = serializers.CharField(source="estado.nombre", read_only=True)
    producto_nombre = serializers.CharField(source="producto.nombre", read_only=True)
    feedbacks = FeedbackSugerenciaSerializer(many=True, read_only=True)

    class Meta:
        model = Sugerencia
        fields = [
            "id",
            "usuario",
            "producto",
            "producto_nombre",
            "fecha",
            "estado",
            "estado_nombre",
            "motivo",
            "feedbacks",
        ]
        read_only_fields = ["id", "usuario", "fecha"]

    def create(self, validated_data):
        validated_data["usuario"] = self.context["request"].user
        return super().create(validated_data)
