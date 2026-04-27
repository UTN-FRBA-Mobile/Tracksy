from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import PromotionViewSet, AdminPromotionViewSet

router = DefaultRouter()
router.register(r"", PromotionViewSet, basename="promotion")

admin_router = DefaultRouter()
admin_router.register(r"promotions", AdminPromotionViewSet, basename="admin-promotion")

urlpatterns = [
    path("", include(router.urls)),
]
