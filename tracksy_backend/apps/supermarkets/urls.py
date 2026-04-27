from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    SupermarketChainViewSet,
    SupermarketBranchViewSet,
    AdminSupermarketChainViewSet,
    AdminSupermarketBranchViewSet,
)

router = DefaultRouter()
router.register(r"branches", SupermarketBranchViewSet, basename="branch")
router.register(r"", SupermarketChainViewSet, basename="supermarket")

admin_router = DefaultRouter()
admin_router.register(r"chains", AdminSupermarketChainViewSet, basename="admin-chain")
admin_router.register(r"branches", AdminSupermarketBranchViewSet, basename="admin-branch")

urlpatterns = [
    path("", include(router.urls)),
]
