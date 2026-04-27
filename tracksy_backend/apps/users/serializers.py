from django.contrib.auth import get_user_model
from django.contrib.auth.password_validation import validate_password
from rest_framework import serializers
from .models import UserProfile, UserPreferences, FavoriteProduct, FavoriteSupermarket

User = get_user_model()


class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, validators=[validate_password])
    password_confirm = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ("email", "username", "password", "password_confirm")

    def validate(self, attrs):
        if attrs["password"] != attrs["password_confirm"]:
            raise serializers.ValidationError({"password": "Las contraseñas no coinciden."})
        return attrs

    def create(self, validated_data):
        validated_data.pop("password_confirm")
        user = User.objects.create_user(
            email=validated_data["email"],
            username=validated_data["username"],
            password=validated_data["password"],
            role=User.ROLE_CONSUMER,
        )
        UserProfile.objects.create(user=user)
        UserPreferences.objects.create(user=user)
        return user


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ("first_name", "last_name", "phone", "avatar", "city", "province")


class UserPreferencesSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserPreferences
        fields = (
            "notify_promotions",
            "notify_price_drops",
            "default_search_radius_km",
            "currency",
        )


class MeSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer()
    preferences = UserPreferencesSerializer()

    class Meta:
        model = User
        fields = (
            "id",
            "email",
            "username",
            "role",
            "is_email_verified",
            "profile",
            "preferences",
            "created_at",
        )
        read_only_fields = ("id", "email", "role", "is_email_verified", "created_at")

    def update(self, instance, validated_data):
        profile_data = validated_data.pop("profile", {})
        preferences_data = validated_data.pop("preferences", {})

        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()

        if profile_data:
            for attr, value in profile_data.items():
                setattr(instance.profile, attr, value)
            instance.profile.save()

        if preferences_data:
            for attr, value in preferences_data.items():
                setattr(instance.preferences, attr, value)
            instance.preferences.save()

        return instance


class FavoriteProductSerializer(serializers.ModelSerializer):
    product_id = serializers.UUIDField(write_only=True)
    product = serializers.SerializerMethodField()

    class Meta:
        model = FavoriteProduct
        fields = ("id", "product_id", "product", "created_at")
        read_only_fields = ("id", "created_at")

    def get_product(self, obj):
        from apps.products.serializers import ProductListSerializer
        return ProductListSerializer(obj.product).data

    def validate_product_id(self, value):
        from apps.products.models import Product
        if not Product.objects.filter(id=value, is_active=True).exists():
            raise serializers.ValidationError("Producto no encontrado o inactivo.")
        return value

    def create(self, validated_data):
        from apps.products.models import Product
        product = Product.objects.get(id=validated_data["product_id"])
        user = self.context["request"].user
        favorite, created = FavoriteProduct.objects.get_or_create(user=user, product=product)
        if not created:
            raise serializers.ValidationError("El producto ya está en favoritos.")
        return favorite


class FavoriteSupermarketSerializer(serializers.ModelSerializer):
    supermarket_id = serializers.UUIDField(write_only=True)
    supermarket = serializers.SerializerMethodField()

    class Meta:
        model = FavoriteSupermarket
        fields = ("id", "supermarket_id", "supermarket", "created_at")
        read_only_fields = ("id", "created_at")

    def get_supermarket(self, obj):
        from apps.supermarkets.serializers import SupermarketChainSerializer
        return SupermarketChainSerializer(obj.supermarket).data

    def validate_supermarket_id(self, value):
        from apps.supermarkets.models import SupermarketChain
        if not SupermarketChain.objects.filter(id=value, is_active=True).exists():
            raise serializers.ValidationError("Supermercado no encontrado o inactivo.")
        return value

    def create(self, validated_data):
        from apps.supermarkets.models import SupermarketChain
        supermarket = SupermarketChain.objects.get(id=validated_data["supermarket_id"])
        user = self.context["request"].user
        favorite, created = FavoriteSupermarket.objects.get_or_create(
            user=user, supermarket=supermarket
        )
        if not created:
            raise serializers.ValidationError("El supermercado ya está en favoritos.")
        return favorite
