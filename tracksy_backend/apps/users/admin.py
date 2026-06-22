from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import Usuario, ProductoUsuario


@admin.register(Usuario)
class UsuarioAdmin(BaseUserAdmin):
    list_display = ("email", "nombre", "firebase_uid", "is_active", "is_staff")
    list_filter = ("is_active", "is_staff")
    search_fields = ("email", "nombre", "firebase_uid")
    ordering = ("email",)
    fieldsets = BaseUserAdmin.fieldsets + (
        ("Datos adicionales", {"fields": ("nombre", "firebase_uid")}),
    )
    add_fieldsets = (
        (
            None,
            {
                "classes": ("wide",),
                "fields": ("email", "nombre", "username", "password1", "password2"),
            },
        ),
    )


@admin.register(ProductoUsuario)
class ProductoUsuarioAdmin(admin.ModelAdmin):
    list_display = ("usuario", "producto", "favorito")
    list_filter = ("favorito",)
    search_fields = ("usuario__email", "producto__nombre")
