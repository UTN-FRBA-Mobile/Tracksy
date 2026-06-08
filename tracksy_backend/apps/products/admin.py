from django.contrib import admin
from .models import Marca, Producto


@admin.register(Marca)
class MarcaAdmin(admin.ModelAdmin):
    list_display = ("id", "nombre")
    search_fields = ("nombre",)
    ordering = ("nombre",)


@admin.register(Producto)
class ProductoAdmin(admin.ModelAdmin):
    list_display = ("id", "nombre", "marca")
    list_filter = ("marca",)
    search_fields = ("nombre",)
    ordering = ("nombre",)
