from django.contrib.auth import get_user_model
from rest_framework import generics, status
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

<<<<<<< HEAD
from .models import ProductoUsuario
=======
from .firebase_auth import FirebaseAuthentication
from .models import FavoriteProduct, FavoriteSupermarket
>>>>>>> 13673e03 (Add Firebase authentication backend)
from .serializers import (
    RegistroSerializer,
    UsuarioSerializer,
    CambiarPasswordSerializer,
    ProductoUsuarioSerializer,
)

User = get_user_model()


class RegistroView(generics.CreateAPIView):
    serializer_class = RegistroSerializer
    permission_classes = [AllowAny]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        return Response(
            {"message": "Registro exitoso.", "id": user.id},
            status=status.HTTP_201_CREATED,
        )


class LoginView(TokenObtainPairView):
    pass


class RefreshTokenView(TokenRefreshView):
    pass


<<<<<<< HEAD
class PerfilView(generics.RetrieveUpdateAPIView):
    serializer_class = UsuarioSerializer
=======
@extend_schema(tags=["auth"])
class FirebaseSyncView(APIView):
    """Sincronizar el usuario Firebase con el usuario local Django."""

    authentication_classes = [FirebaseAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request, *args, **kwargs):
        user = request.user
        return Response(
            {
                "id": str(user.id),
                "firebase_uid": user.firebase_uid,
                "email": user.email,
                "is_email_verified": user.is_email_verified,
            },
            status=status.HTTP_200_OK,
        )


@extend_schema(tags=["users"])
class MeView(generics.RetrieveUpdateAPIView):
    """Obtener y actualizar el perfil del usuario autenticado."""

    serializer_class = MeSerializer
>>>>>>> 13673e03 (Add Firebase authentication backend)
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user


class CambiarPasswordView(generics.GenericAPIView):
    serializer_class = CambiarPasswordSerializer
    permission_classes = [IsAuthenticated]

    def post(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response({"message": "Contraseña actualizada."})


class FavoritosView(generics.ListCreateAPIView):
    serializer_class = ProductoUsuarioSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return ProductoUsuario.objects.filter(
            usuario=self.request.user, favorito=True
        ).select_related("producto")


class FavoritoDetalleView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = ProductoUsuarioSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return ProductoUsuario.objects.filter(usuario=self.request.user)
