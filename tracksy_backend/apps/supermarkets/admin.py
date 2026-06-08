from django.contrib import admin
from .models import Supermercado, ProductoListado


class ProductoListadoInline(admin.TabularInline):
    model = ProductoListado
    extra = 0
    fields = ("producto", "precio", "disponible")


@admin.register(Supermercado)
class SupermercadoAdmin(admin.ModelAdmin):
    inlines = [ProductoListadoInline]
    list_display = ("id", "nombre", "direccion")
    search_fields = ("nombre", "direccion")
    ordering = ("nombre",)


@admin.register(ProductoListado)
class ProductoListadoAdmin(admin.ModelAdmin):
    list_display = ("producto", "supermercado", "precio", "disponible", "fecha_actualizacion")
    list_filter = ("supermercado", "disponible")
    search_fields = ("producto__nombre", "supermercado__nombre")
