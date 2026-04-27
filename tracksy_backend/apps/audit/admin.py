from django.contrib import admin
from .models import AuditLog


@admin.register(AuditLog)
class AuditLogAdmin(admin.ModelAdmin):
    list_display = ("action", "entity_type", "entity_id", "user", "ip_address", "created_at")
    list_filter = ("action", "entity_type")
    search_fields = ("user__email", "entity_id", "description")
    ordering = ("-created_at",)
    readonly_fields = (
        "user", "action", "entity_type", "entity_id",
        "ip_address", "user_agent", "previous_data", "new_data", "description", "created_at",
    )

    def has_add_permission(self, request):
        return False

    def has_change_permission(self, request, obj=None):
        return False
