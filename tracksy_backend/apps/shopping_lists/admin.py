from django.contrib import admin
from .models import EstadoProducto, ListaCompra, ItemProducto


class ItemProductoInline(admin.TabularInline):
    model = ItemProducto
    extra = 0
    fields = ("producto", "cantidad", "estado", "precio_unitario")


@admin.register(EstadoProducto)
class EstadoProductoAdmin(admin.ModelAdmin):
    list_display = ("id", "nombre")


@admin.register(ListaCompra)
class ListaCompraAdmin(admin.ModelAdmin):
    inlines = [ItemProductoInline]
    list_display = ("id", "nombre", "usuario", "supermercado", "fecha_creacion", "total_estimado")
    list_filter = ("supermercado",)
    search_fields = ("nombre", "usuario__email")
    ordering = ("-fecha_creacion",)


@admin.register(ItemProducto)
class ItemProductoAdmin(admin.ModelAdmin):
    list_display = ("lista", "producto", "cantidad", "estado", "precio_unitario")
    list_filter = ("estado",)
