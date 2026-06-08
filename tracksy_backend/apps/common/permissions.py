from rest_framework.permissions import BasePermission


class IsAdminUser(BasePermission):
    """Solo administradores Django (is_staff)."""

    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_staff)


class IsOwner(BasePermission):
    """Object-level: solo el propietario del recurso."""

    def has_object_permission(self, request, view, obj):
        owner = getattr(obj, "usuario", None) or getattr(obj, "user", None)
        return owner == request.user
