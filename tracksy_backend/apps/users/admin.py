from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import User, UserProfile, UserPreferences, FavoriteProduct, FavoriteSupermarket


class UserProfileInline(admin.StackedInline):
    model = UserProfile
    can_delete = False


class UserPreferencesInline(admin.StackedInline):
    model = UserPreferences
    can_delete = False


@admin.register(User)
class UserAdmin(BaseUserAdmin):
    inlines = [UserProfileInline, UserPreferencesInline]
    list_display = ("email", "username", "role", "is_active", "created_at")
    list_filter = ("role", "is_active")
    search_fields = ("email", "username")
    ordering = ("-created_at",)
    fieldsets = BaseUserAdmin.fieldsets + (
        ("Tracksy", {"fields": ("role", "is_email_verified")}),
    )


@admin.register(FavoriteProduct)
class FavoriteProductAdmin(admin.ModelAdmin):
    list_display = ("user", "product", "created_at")
    list_filter = ("created_at",)


@admin.register(FavoriteSupermarket)
class FavoriteSupermarketAdmin(admin.ModelAdmin):
    list_display = ("user", "supermarket", "created_at")
