from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ListaCompraViewSet, EstadoProductoViewSet, ItemProductoViewSet

router = DefaultRouter()
router.register(r"estados-producto", EstadoProductoViewSet, basename="estado-producto")
router.register(r"items", ItemProductoViewSet, basename="item-producto")
router.register(r"", ListaCompraViewSet, basename="lista-compra")

urlpatterns = [
    path("", include(router.urls)),
]
