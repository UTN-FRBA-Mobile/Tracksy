from django.contrib import admin
from .models import ProductPrice, PriceHistory


@admin.register(ProductPrice)
class ProductPriceAdmin(admin.ModelAdmin):
    list_display = ("product", "branch", "price", "currency", "valid_from", "is_active", "source")
    list_filter = ("is_active", "source", "currency")
    search_fields = ("product__name", "product__barcode", "branch__name", "branch__chain__name")
    ordering = ("-updated_at",)


@admin.register(PriceHistory)
class PriceHistoryAdmin(admin.ModelAdmin):
    list_display = ("product", "branch", "price", "currency", "created_at")
    list_filter = ("currency", "source")
    search_fields = ("product__name", "branch__name")
    ordering = ("-created_at",)
    readonly_fields = ("product", "branch", "price", "currency", "source", "created_at")
