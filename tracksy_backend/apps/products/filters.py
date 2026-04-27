import django_filters
from .models import Product


class ProductFilter(django_filters.FilterSet):
    name = django_filters.CharFilter(lookup_expr="icontains")
    brand = django_filters.UUIDFilter(field_name="brand__id")
    brand_name = django_filters.CharFilter(field_name="brand__name", lookup_expr="icontains")
    category = django_filters.UUIDFilter(field_name="category__id")
    category_slug = django_filters.CharFilter(field_name="category__slug")
    subcategory = django_filters.UUIDFilter(field_name="subcategory__id")
    is_active = django_filters.BooleanFilter()

    class Meta:
        model = Product
        fields = ["name", "brand", "brand_name", "category", "category_slug", "subcategory", "is_active"]
