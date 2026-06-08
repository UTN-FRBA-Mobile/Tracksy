from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SugerenciaViewSet, EstadoViewSet

router = DefaultRouter()
router.register(r"estados", EstadoViewSet, basename="estado-sugerencia")
router.register(r"", SugerenciaViewSet, basename="sugerencia")

urlpatterns = [
    path("", include(router.urls)),
]
