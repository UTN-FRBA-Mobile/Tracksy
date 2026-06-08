from django.contrib import admin
from .models import Estado, Sugerencia, FeedbackSugerencia


class FeedbackInline(admin.TabularInline):
    model = FeedbackSugerencia
    extra = 0
    fields = ("fue_util", "fecha")
    readonly_fields = ("fecha",)


@admin.register(Estado)
class EstadoAdmin(admin.ModelAdmin):
    list_display = ("id", "nombre")


@admin.register(Sugerencia)
class SugerenciaAdmin(admin.ModelAdmin):
    inlines = [FeedbackInline]
    list_display = ("id", "usuario", "producto", "estado", "fecha")
    list_filter = ("estado",)
    search_fields = ("usuario__email", "producto__nombre", "motivo")
    ordering = ("-fecha",)


@admin.register(FeedbackSugerencia)
class FeedbackSugerenciaAdmin(admin.ModelAdmin):
    list_display = ("sugerencia", "fue_util", "fecha")
    list_filter = ("fue_util",)
