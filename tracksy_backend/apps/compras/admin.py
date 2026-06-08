from django.contrib import admin
from .models import Compra, ProductoComprado


class ProductoCompradoInline(admin.TabularInline):
    model = ProductoComprado
    extra = 0
    fields = ("producto", "cantidad", "precio_unitario")


@admin.register(Compra)
class CompraAdmin(admin.ModelAdmin):
    inlines = [ProductoCompradoInline]
    list_display = ("id", "usuario", "supermercado", "fecha", "total")
    list_filter = ("supermercado",)
    search_fields = ("usuario__email",)
    ordering = ("-fecha",)


@admin.register(ProductoComprado)
class ProductoCompradoAdmin(admin.ModelAdmin):
    list_display = ("compra", "producto", "cantidad", "precio_unitario")
