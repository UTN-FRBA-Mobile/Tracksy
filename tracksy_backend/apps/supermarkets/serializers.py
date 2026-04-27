from rest_framework import serializers
from .models import SupermarketChain, SupermarketBranch, BranchOpeningHours


class BranchOpeningHoursSerializer(serializers.ModelSerializer):
    weekday_label = serializers.CharField(source="get_weekday_display", read_only=True)

    class Meta:
        model = BranchOpeningHours
        fields = ("weekday", "weekday_label", "open_time", "close_time", "is_closed")


class SupermarketBranchSerializer(serializers.ModelSerializer):
    chain_name = serializers.CharField(source="chain.name", read_only=True)
    chain_logo = serializers.ImageField(source="chain.logo", read_only=True)
    opening_hours = BranchOpeningHoursSerializer(many=True, read_only=True)
    distance_km = serializers.SerializerMethodField()

    class Meta:
        model = SupermarketBranch
        fields = (
            "id",
            "chain",
            "chain_name",
            "chain_logo",
            "name",
            "code",
            "address",
            "city",
            "province",
            "postal_code",
            "latitude",
            "longitude",
            "phone",
            "email",
            "is_active",
            "opening_hours",
            "distance_km",
        )

    def get_distance_km(self, obj):
        return getattr(obj, "distance_km", None)


class SupermarketBranchListSerializer(serializers.ModelSerializer):
    chain_name = serializers.CharField(source="chain.name", read_only=True)
    chain_logo = serializers.ImageField(source="chain.logo", read_only=True)
    distance_km = serializers.SerializerMethodField()

    class Meta:
        model = SupermarketBranch
        fields = (
            "id",
            "chain",
            "chain_name",
            "chain_logo",
            "name",
            "address",
            "city",
            "province",
            "latitude",
            "longitude",
            "distance_km",
        )

    def get_distance_km(self, obj):
        return getattr(obj, "distance_km", None)


class SupermarketChainSerializer(serializers.ModelSerializer):
    branches_count = serializers.SerializerMethodField()

    class Meta:
        model = SupermarketChain
        fields = ("id", "name", "slug", "logo", "website", "branches_count")

    def get_branches_count(self, obj):
        return obj.branches.filter(is_active=True).count()


class SupermarketChainDetailSerializer(serializers.ModelSerializer):
    branches = SupermarketBranchListSerializer(many=True, read_only=True)

    class Meta:
        model = SupermarketChain
        fields = ("id", "name", "slug", "logo", "website", "branches")


class SupermarketChainWriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = SupermarketChain
        fields = ("name", "slug", "logo", "website", "is_active")


class SupermarketBranchWriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = SupermarketBranch
        fields = (
            "chain", "name", "code", "address", "city", "province",
            "postal_code", "latitude", "longitude", "phone", "email", "is_active",
        )
