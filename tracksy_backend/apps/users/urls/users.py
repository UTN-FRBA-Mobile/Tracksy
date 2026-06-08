from django.urls import path
from apps.users.views import PerfilView, CambiarPasswordView, FavoritosView, FavoritoDetalleView

urlpatterns = [
    path("perfil/", PerfilView.as_view(), name="usuario-perfil"),
    path("cambiar-password/", CambiarPasswordView.as_view(), name="usuario-cambiar-password"),
    path("favoritos/", FavoritosView.as_view(), name="usuario-favoritos"),
    path("favoritos/<int:pk>/", FavoritoDetalleView.as_view(), name="usuario-favorito-detalle"),
]
