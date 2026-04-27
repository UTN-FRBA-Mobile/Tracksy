from rest_framework.permissions import BasePermission


class IsConsumer(BasePermission):
    """Allows access only to users with role 'consumer'."""

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.role == "consumer"
        )


class IsAdminOrBackoffice(BasePermission):
    """Allows access to administrators and backoffice operators."""

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.role in ("admin", "backoffice")
        )


class IsAdmin(BasePermission):
    """Allows access only to administrators."""

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.role == "admin"
        )


class IsOwnerOrAdmin(BasePermission):
    """Object-level: owner of the resource or admin."""

    def has_object_permission(self, request, view, obj):
        if request.user.role == "admin":
            return True
        return getattr(obj, "user", None) == request.user
