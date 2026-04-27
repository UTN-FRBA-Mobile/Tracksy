from django.contrib import admin
from .models import (
    Promotion, PromotionType, PromotionProduct, PromotionCategory,
    PromotionSupermarket, PromotionPaymentMethod, PromotionDayOfWeek,
)


class PromotionProductInline(admin.TabularInline):
    model = PromotionProduct
    extra = 1


class PromotionCategoryInline(admin.TabularInline):
    model = PromotionCategory
    extra = 1


class PromotionSupermarketInline(admin.TabularInline):
    model = PromotionSupermarket
    extra = 1


class PromotionPaymentMethodInline(admin.TabularInline):
    model = PromotionPaymentMethod
    extra = 1


class PromotionDayOfWeekInline(admin.TabularInline):
    model = PromotionDayOfWeek
    extra = 1


@admin.register(Promotion)
class PromotionAdmin(admin.ModelAdmin):
    inlines = [
        PromotionProductInline,
        PromotionCategoryInline,
        PromotionSupermarketInline,
        PromotionPaymentMethodInline,
        PromotionDayOfWeekInline,
    ]
    list_display = ("name", "promotion_type", "start_date", "end_date", "is_active", "is_currently_active")
    list_filter = ("is_active", "promotion_type")
    search_fields = ("name",)
    ordering = ("-start_date",)


@admin.register(PromotionType)
class PromotionTypeAdmin(admin.ModelAdmin):
    list_display = ("code", "label")
