from rest_framework import serializers
from .models import (
    Promotion,
    PromotionType,
    PromotionProduct,
    PromotionCategory,
    PromotionSupermarket,
    PromotionPaymentMethod,
    PromotionDayOfWeek,
)


class PromotionTypeSerializer(serializers.ModelSerializer):
    class Meta:
        model = PromotionType
        fields = ("id", "code", "label", "description")


class PromotionPaymentMethodSerializer(serializers.ModelSerializer):
    class Meta:
        model = PromotionPaymentMethod
        fields = ("id", "method", "bank_name")


class PromotionDayOfWeekSerializer(serializers.ModelSerializer):
    weekday_label = serializers.SerializerMethodField()

    class Meta:
        model = PromotionDayOfWeek
        fields = ("weekday", "weekday_label")

    def get_weekday_label(self, obj):
        days = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"]
        return days[obj.weekday]


class PromotionListSerializer(serializers.ModelSerializer):
    promotion_type_label = serializers.CharField(
        source="promotion_type.label", read_only=True
    )
    is_currently_active = serializers.BooleanField(read_only=True)

    class Meta:
        model = Promotion
        fields = (
            "id",
            "name",
            "promotion_type_label",
            "discount_percentage",
            "discount_amount",
            "start_date",
            "end_date",
            "banner_image",
            "is_currently_active",
        )


class PromotionDetailSerializer(serializers.ModelSerializer):
    promotion_type = PromotionTypeSerializer(read_only=True)
    supermarkets = serializers.SerializerMethodField()
    products = serializers.SerializerMethodField()
    categories = serializers.SerializerMethodField()
    payment_methods = PromotionPaymentMethodSerializer(many=True, read_only=True)
    days_of_week = PromotionDayOfWeekSerializer(many=True, read_only=True)
    is_currently_active = serializers.BooleanField(read_only=True)

    class Meta:
        model = Promotion
        fields = (
            "id",
            "name",
            "description",
            "promotion_type",
            "discount_percentage",
            "discount_amount",
            "second_unit_discount_percentage",
            "max_refund_amount",
            "start_date",
            "end_date",
            "supermarkets",
            "products",
            "categories",
            "payment_methods",
            "days_of_week",
            "banner_image",
            "terms",
            "is_currently_active",
        )

    def get_supermarkets(self, obj):
        from apps.supermarkets.serializers import SupermarketChainSerializer
        return SupermarketChainSerializer(
            [ps.supermarket for ps in obj.promotion_supermarkets.select_related("supermarket")],
            many=True,
            context=self.context,
        ).data

    def get_products(self, obj):
        from apps.products.serializers import ProductListSerializer
        return ProductListSerializer(
            [pp.product for pp in obj.promotion_products.select_related("product")],
            many=True,
            context=self.context,
        ).data

    def get_categories(self, obj):
        from apps.products.serializers import CategorySerializer
        return CategorySerializer(
            [pc.category for pc in obj.promotion_categories.select_related("category")],
            many=True,
            context=self.context,
        ).data


class PromotionWriteSerializer(serializers.ModelSerializer):
    product_ids = serializers.ListField(
        child=serializers.UUIDField(), write_only=True, required=False
    )
    category_ids = serializers.ListField(
        child=serializers.UUIDField(), write_only=True, required=False
    )
    supermarket_ids = serializers.ListField(
        child=serializers.UUIDField(), write_only=True, required=False
    )

    class Meta:
        model = Promotion
        fields = (
            "promotion_type",
            "name",
            "description",
            "discount_percentage",
            "discount_amount",
            "second_unit_discount_percentage",
            "max_refund_amount",
            "start_date",
            "end_date",
            "is_active",
            "banner_image",
            "terms",
            "product_ids",
            "category_ids",
            "supermarket_ids",
        )

    def create(self, validated_data):
        product_ids = validated_data.pop("product_ids", [])
        category_ids = validated_data.pop("category_ids", [])
        supermarket_ids = validated_data.pop("supermarket_ids", [])

        promotion = Promotion.objects.create(**validated_data)

        from apps.products.models import Product, Category
        from apps.supermarkets.models import SupermarketChain

        for pid in product_ids:
            if Product.objects.filter(id=pid).exists():
                PromotionProduct.objects.create(promotion=promotion, product_id=pid)

        for cid in category_ids:
            if Category.objects.filter(id=cid).exists():
                PromotionCategory.objects.create(promotion=promotion, category_id=cid)

        for sid in supermarket_ids:
            if SupermarketChain.objects.filter(id=sid).exists():
                PromotionSupermarket.objects.create(promotion=promotion, supermarket_id=sid)

        return promotion
