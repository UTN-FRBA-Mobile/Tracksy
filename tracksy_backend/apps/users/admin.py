from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import Usuario, ProductoUsuario


<<<<<<< HEAD
@admin.register(Usuario)
class UsuarioAdmin(BaseUserAdmin):
    list_display = ("email", "nombre", "is_active", "is_staff")
    list_filter = ("is_active", "is_staff")
    search_fields = ("email", "nombre")
    ordering = ("email",)
    fieldsets = BaseUserAdmin.fieldsets + (
        ("Datos adicionales", {"fields": ("nombre",)}),
    )
    add_fieldsets = (
        (None, {
            "classes": ("wide",),
            "fields": ("email", "nombre", "username", "password1", "password2"),
        }),
=======
class UserProfileInline(admin.StackedInline):
    model = UserProfile
    can_delete = False


class UserPreferencesInline(admin.StackedInline):
    model = UserPreferences
    can_delete = False


@admin.register(User)
class UserAdmin(BaseUserAdmin):
    inlines = [UserProfileInline, UserPreferencesInline]
    list_display = ("email", "username", "firebase_uid", "role", "is_active", "created_at")
    list_filter = ("role", "is_active")
    search_fields = ("email", "username", "firebase_uid")
    ordering = ("-created_at",)
    fieldsets = BaseUserAdmin.fieldsets + (
        ("Tracksy", {"fields": ("firebase_uid", "role", "is_email_verified")}),
>>>>>>> 13673e03 (Add Firebase authentication backend)
    )


@admin.register(ProductoUsuario)
class ProductoUsuarioAdmin(admin.ModelAdmin):
    list_display = ("usuario", "producto", "favorito")
    list_filter = ("favorito",)
    search_fields = ("usuario__email", "producto__nombre")
