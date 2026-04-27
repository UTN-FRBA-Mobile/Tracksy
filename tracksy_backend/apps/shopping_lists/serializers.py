from rest_framework import serializers
from .models import ShoppingList, ShoppingListItem


class ShoppingListItemSerializer(serializers.ModelSerializer):
    product_id = serializers.UUIDField()
    product_name = serializers.CharField(source="product.name", read_only=True)
    product_thumbnail = serializers.ImageField(source="product.thumbnail", read_only=True)
    product_brand = serializers.CharField(source="product.brand.name", read_only=True)

    class Meta:
        model = ShoppingListItem
        fields = (
            "id",
            "product_id",
            "product_name",
            "product_thumbnail",
            "product_brand",
            "quantity",
            "is_purchased",
            "notes",
        )

    def validate_product_id(self, value):
        from apps.products.models import Product
        if not Product.objects.filter(id=value, is_active=True).exists():
            raise serializers.ValidationError("Producto no encontrado o inactivo.")
        return value

    def validate_quantity(self, value):
        if value < 1:
            raise serializers.ValidationError("La cantidad debe ser al menos 1.")
        return value


class ShoppingListItemUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = ShoppingListItem
        fields = ("quantity", "is_purchased", "notes")


class ShoppingListSerializer(serializers.ModelSerializer):
    items = ShoppingListItemSerializer(many=True, read_only=True)
    total_items = serializers.IntegerField(read_only=True)
    purchased_items = serializers.IntegerField(read_only=True)
    preferred_branch_name = serializers.SerializerMethodField()

    class Meta:
        model = ShoppingList
        fields = (
            "id",
            "name",
            "preferred_branch",
            "preferred_branch_name",
            "status",
            "notes",
            "total_items",
            "purchased_items",
            "items",
            "created_at",
            "updated_at",
        )
        read_only_fields = ("id", "created_at", "updated_at")

    def get_preferred_branch_name(self, obj):
        if obj.preferred_branch:
            return f"{obj.preferred_branch.chain.name} — {obj.preferred_branch.name}"
        return None


class ShoppingListCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = ShoppingList
        fields = ("name", "preferred_branch", "notes")

    def create(self, validated_data):
        validated_data["user"] = self.context["request"].user
        return super().create(validated_data)


class ShoppingListUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = ShoppingList
        fields = ("name", "preferred_branch", "status", "notes")
