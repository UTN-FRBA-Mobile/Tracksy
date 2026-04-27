from django.contrib import admin
from .models import SupermarketChain, SupermarketBranch, BranchOpeningHours


class BranchOpeningHoursInline(admin.TabularInline):
    model = BranchOpeningHours
    extra = 7


class SupermarketBranchInline(admin.TabularInline):
    model = SupermarketBranch
    extra = 0
    fields = ("name", "city", "province", "is_active")
    show_change_link = True


@admin.register(SupermarketChain)
class SupermarketChainAdmin(admin.ModelAdmin):
    inlines = [SupermarketBranchInline]
    list_display = ("name", "is_active", "created_at")
    search_fields = ("name",)
    prepopulated_fields = {"slug": ("name",)}


@admin.register(SupermarketBranch)
class SupermarketBranchAdmin(admin.ModelAdmin):
    inlines = [BranchOpeningHoursInline]
    list_display = ("name", "chain", "city", "province", "is_active")
    list_filter = ("chain", "city", "province", "is_active")
    search_fields = ("name", "address", "city")
