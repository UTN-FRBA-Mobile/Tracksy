import logging
from .models import AuditLog

logger = logging.getLogger(__name__)

AUDIT_METHODS = {"POST", "PUT", "PATCH", "DELETE"}
AUDIT_PATH_PREFIXES = ("/api/v1/admin-api/", "/api/v1/auth/")


class AuditMiddleware:
    """
    Logs write operations on admin and auth endpoints to AuditLog.
    Read endpoints (GET/HEAD) are ignored to avoid noise.
    """

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        response = self.get_response(request)

        if (
            request.method in AUDIT_METHODS
            and any(request.path.startswith(p) for p in AUDIT_PATH_PREFIXES)
            and response.status_code < 500
        ):
            try:
                user = request.user if request.user.is_authenticated else None
                AuditLog.objects.create(
                    user=user,
                    action=self._infer_action(request.method),
                    entity_type=request.path,
                    ip_address=self._get_client_ip(request),
                    user_agent=request.META.get("HTTP_USER_AGENT", "")[:500],
                    description=f"{request.method} {request.path} → {response.status_code}",
                )
            except Exception:
                logger.exception("AuditMiddleware failed to write log")

        return response

    @staticmethod
    def _infer_action(method: str) -> str:
        return {
            "POST": AuditLog.ACTION_CREATE,
            "PUT": AuditLog.ACTION_UPDATE,
            "PATCH": AuditLog.ACTION_UPDATE,
            "DELETE": AuditLog.ACTION_DELETE,
        }.get(method, AuditLog.ACTION_CREATE)

    @staticmethod
    def _get_client_ip(request) -> str:
        x_forwarded = request.META.get("HTTP_X_FORWARDED_FOR")
        if x_forwarded:
            return x_forwarded.split(",")[0].strip()
        return request.META.get("REMOTE_ADDR")
