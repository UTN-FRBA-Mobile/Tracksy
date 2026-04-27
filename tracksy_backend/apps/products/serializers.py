from rest_framework import serializers
from .models import Product, ProductImage, Category, SubCategory, Brand, MeasurementUnit


class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ("id", "name", "slug", "icon")


class SubCategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = SubCategory
        fields = ("id", "name", "slug", "category")


class BrandSerializer(serializers.ModelSerializer):
    class Meta:
        model = Brand
        fields = ("id", "name", "slug", "logo")


class MeasurementUnitSerializer(serializers.ModelSerializer):
    class Meta:
        model = MeasurementUnit
        fields = ("id", "name", "abbreviation")


class ProductImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProductImage
        fields = ("id", "image", "alt_text", "is_primary", "order")


class ProductListSerializer(serializers.ModelSerializer):
    """Lightweight serializer for list endpoints and nested use."""

    brand_name = serializers.CharField(source="brand.name", read_only=True)
    category_name = serializers.CharField(source="category.name", read_only=True)
    thumbnail_url = serializers.ImageField(source="thumbnail", read_only=True)

    class Meta:
        model = Product
        fields = (
            "id",
            "barcode",
            "name",
            "brand_name",
            "category_name",
            "presentation",
            "net_content",
            "thumbnail_url",
        )


class ProductDetailSerializer(serializers.ModelSerializer):
    """Full detail serializer for /products/{id}/."""

    brand = BrandSerializer(read_only=True)
    category = CategorySerializer(read_only=True)
    subcategory = SubCategorySerializer(read_only=True)
    measurement_unit = MeasurementUnitSerializer(read_only=True)
    images = ProductImageSerializer(many=True, read_only=True)

    class Meta:
        model = Product
        fields = (
            "id",
            "barcode",
            "name",
            "description",
            "brand",
            "category",
            "subcategory",
            "measurement_unit",
            "presentation",
            "net_content",
            "thumbnail",
            "images",
            "is_active",
            "created_at",
            "updated_at",
        )


class ProductWriteSerializer(serializers.ModelSerializer):
    """Used by backoffice to create/update products."""

    class Meta:
        model = Product
        fields = (
            "barcode",
            "name",
            "description",
            "brand",
            "category",
            "subcategory",
            "measurement_unit",
            "presentation",
            "net_content",
            "thumbnail",
            "is_active",
        )
