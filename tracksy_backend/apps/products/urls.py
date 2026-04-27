from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ProductViewSet, CategoryViewSet, BrandViewSet, AdminProductViewSet

router = DefaultRouter()
router.register(r"", ProductViewSet, basename="product")

category_router = DefaultRouter()
category_router.register(r"categories", CategoryViewSet, basename="category")

brand_router = DefaultRouter()
brand_router.register(r"brands", BrandViewSet, basename="brand")

admin_router = DefaultRouter()
admin_router.register(r"products", AdminProductViewSet, basename="admin-product")

urlpatterns = [
    path("", include(router.urls)),
]
