"""Separate router for categories and brands (mounted at API root level)."""
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CategoryViewSet, BrandViewSet

router = DefaultRouter()
router.register(r"categories", CategoryViewSet, basename="category")
router.register(r"brands", BrandViewSet, basename="brand")

urlpatterns = [
    path("", include(router.urls)),
]
