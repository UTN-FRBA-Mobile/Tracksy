from rest_framework import serializers
from .models import ProductPrice, PriceHistory


class ProductPriceSerializer(serializers.ModelSerializer):
    branch_id = serializers.UUIDField(source="branch.id", read_only=True)
    branch_name = serializers.CharField(source="branch.name", read_only=True)
    chain_id = serializers.UUIDField(source="branch.chain.id", read_only=True)
    chain_name = serializers.CharField(source="branch.chain.name", read_only=True)
    chain_logo = serializers.ImageField(source="branch.chain.logo", read_only=True)
    city = serializers.CharField(source="branch.city", read_only=True)
    province = serializers.CharField(source="branch.province", read_only=True)

    class Meta:
        model = ProductPrice
        fields = (
            "id",
            "branch_id",
            "branch_name",
            "chain_id",
            "chain_name",
            "chain_logo",
            "city",
            "province",
            "price",
            "currency",
            "valid_from",
            "valid_until",
            "source",
            "updated_at",
        )


class PriceHistorySerializer(serializers.ModelSerializer):
    class Meta:
        model = PriceHistory
        fields = ("id", "price", "currency", "source", "created_at")


class ProductPriceWriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductPrice
        fields = (
            "product",
            "branch",
            "price",
            "currency",
            "valid_from",
            "valid_until",
            "source",
            "is_active",
        )

    def validate_price(self, value):
        if value <= 0:
            raise serializers.ValidationError("El precio debe ser mayor a 0.")
        return value
