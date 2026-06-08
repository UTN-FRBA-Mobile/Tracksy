from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CompraViewSet, ProductoCompradoViewSet

router = DefaultRouter()
router.register(r"productos-comprados", ProductoCompradoViewSet, basename="producto-comprado")
router.register(r"", CompraViewSet, basename="compra")

urlpatterns = [
    path("", include(router.urls)),
]
