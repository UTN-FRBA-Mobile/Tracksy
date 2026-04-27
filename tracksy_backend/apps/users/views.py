from django.contrib.auth import get_user_model
from rest_framework import generics, status, viewsets
from rest_framework.decorators import action
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from drf_spectacular.utils import extend_schema, extend_schema_view

from .models import FavoriteProduct, FavoriteSupermarket
from .serializers import (
    RegisterSerializer,
    MeSerializer,
    FavoriteProductSerializer,
    FavoriteSupermarketSerializer,
)

User = get_user_model()


@extend_schema(tags=["auth"])
class RegisterView(generics.CreateAPIView):
    """Registrar un nuevo consumidor."""

    serializer_class = RegisterSerializer
    permission_classes = [AllowAny]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        return Response(
            {"message": "Registro exitoso.", "user_id": str(user.id)},
            status=status.HTTP_201_CREATED,
        )


@extend_schema(tags=["auth"])
class LoginView(TokenObtainPairView):
    """Login con email y contraseña. Retorna access + refresh JWT."""
    pass


@extend_schema(tags=["auth"])
class RefreshTokenView(TokenRefreshView):
    """Renovar el access token usando el refresh token."""
    pass


@extend_schema(tags=["users"])
class MeView(generics.RetrieveUpdateAPIView):
    """Obtener y actualizar el perfil del usuario autenticado."""

    serializer_class = MeSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user


@extend_schema(tags=["favorites"])
@extend_schema_view(
    list=extend_schema(summary="Listar productos favoritos"),
    create=extend_schema(summary="Agregar producto a favoritos"),
    destroy=extend_schema(summary="Eliminar producto de favoritos"),
)
class FavoriteProductViewSet(viewsets.ModelViewSet):
    """Gestión de productos favoritos del usuario autenticado."""

    serializer_class = FavoriteProductSerializer
    permission_classes = [IsAuthenticated]
    http_method_names = ["get", "post", "delete", "head", "options"]

    def get_queryset(self):
        return FavoriteProduct.objects.filter(user=self.request.user).select_related(
            "product", "product__brand", "product__category"
        )

    def perform_create(self, serializer):
        serializer.save()


@extend_schema(tags=["favorites"])
@extend_schema_view(
    list=extend_schema(summary="Listar supermercados favoritos"),
    create=extend_schema(summary="Agregar supermercado a favoritos"),
    destroy=extend_schema(summary="Eliminar supermercado de favoritos"),
)
class FavoriteSupermarketViewSet(viewsets.ModelViewSet):
    """Gestión de supermercados favoritos del usuario autenticado."""

    serializer_class = FavoriteSupermarketSerializer
    permission_classes = [IsAuthenticated]
    http_method_names = ["get", "post", "delete", "head", "options"]

    def get_queryset(self):
        return FavoriteSupermarket.objects.filter(user=self.request.user).select_related(
            "supermarket"
        )

    def perform_create(self, serializer):
        serializer.save()
