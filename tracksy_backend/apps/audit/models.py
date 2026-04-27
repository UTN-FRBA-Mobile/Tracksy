from django.db import models
from apps.common.models import BaseModel


class AuditLog(BaseModel):
    ACTION_CREATE = "create"
    ACTION_UPDATE = "update"
    ACTION_DELETE = "delete"
    ACTION_LOGIN = "login"
    ACTION_LOGOUT = "logout"
    ACTION_IMPORT = "import"

    ACTION_CHOICES = [
        (ACTION_CREATE, "Creación"),
        (ACTION_UPDATE, "Actualización"),
        (ACTION_DELETE, "Eliminación"),
        (ACTION_LOGIN, "Login"),
        (ACTION_LOGOUT, "Logout"),
        (ACTION_IMPORT, "Importación"),
    ]

    user = models.ForeignKey(
        "users.User",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="audit_logs",
    )
    action = models.CharField(max_length=20, choices=ACTION_CHOICES)
    entity_type = models.CharField(max_length=100)
    entity_id = models.CharField(max_length=100, blank=True)
    ip_address = models.GenericIPAddressField(null=True, blank=True)
    user_agent = models.CharField(max_length=500, blank=True)
    previous_data = models.JSONField(null=True, blank=True)
    new_data = models.JSONField(null=True, blank=True)
    description = models.TextField(blank=True)

    class Meta:
        db_table = "audit_logs"
        verbose_name = "Log de Auditoría"
        verbose_name_plural = "Logs de Auditoría"
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["entity_type", "entity_id"], name="idx_audit_entity"),
            models.Index(fields=["user", "created_at"], name="idx_audit_user"),
            models.Index(fields=["action", "created_at"], name="idx_audit_action"),
        ]

    def __str__(self):
        return f"{self.user} | {self.action} | {self.entity_type} | {self.created_at}"
