from django.contrib import admin
from .models import ShoppingList, ShoppingListItem, ShoppingListEstimation


class ShoppingListItemInline(admin.TabularInline):
    model = ShoppingListItem
    extra = 0
    readonly_fields = ("product",)


@admin.register(ShoppingList)
class ShoppingListAdmin(admin.ModelAdmin):
    inlines = [ShoppingListItemInline]
    list_display = ("name", "user", "status", "total_items", "created_at")
    list_filter = ("status",)
    search_fields = ("name", "user__email")
    ordering = ("-created_at",)
