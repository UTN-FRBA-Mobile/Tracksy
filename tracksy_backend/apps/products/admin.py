from django.contrib import admin
from .models import Product, ProductImage, Category, SubCategory, Brand, MeasurementUnit


class ProductImageInline(admin.TabularInline):
    model = ProductImage
    extra = 1


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    inlines = [ProductImageInline]
    list_display = ("name", "barcode", "brand", "category", "is_active", "created_at")
    list_filter = ("is_active", "category", "brand")
    search_fields = ("name", "barcode")
    ordering = ("-created_at",)


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ("name", "slug", "is_active")
    prepopulated_fields = {"slug": ("name",)}


@admin.register(SubCategory)
class SubCategoryAdmin(admin.ModelAdmin):
    list_display = ("name", "category", "is_active")
    list_filter = ("category",)


@admin.register(Brand)
class BrandAdmin(admin.ModelAdmin):
    list_display = ("name", "slug", "is_active")
    prepopulated_fields = {"slug": ("name",)}


@admin.register(MeasurementUnit)
class MeasurementUnitAdmin(admin.ModelAdmin):
    list_display = ("name", "abbreviation")
