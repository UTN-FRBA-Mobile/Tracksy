from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SupermercadoViewSet, ProductoListadoViewSet

router = DefaultRouter()
router.register(r"listados", ProductoListadoViewSet, basename="producto-listado")
router.register(r"", SupermercadoViewSet, basename="supermercado")

urlpatterns = [
    path("", include(router.urls)),
]
