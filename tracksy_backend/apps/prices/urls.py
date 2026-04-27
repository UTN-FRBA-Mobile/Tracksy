from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import PriceSearchViewSet, PriceHistoryView, AdminPriceViewSet

router = DefaultRouter()
router.register(r"search", PriceSearchViewSet, basename="price-search")

admin_router = DefaultRouter()
admin_router.register(r"prices", AdminPriceViewSet, basename="admin-price")

urlpatterns = [
    path("", include(router.urls)),
    path(
        "products/<uuid:product_id>/history/",
        PriceHistoryView.as_view(),
        name="price-history",
    ),
    path(
        "products/<uuid:product_id>/history/<uuid:branch_id>/",
        PriceHistoryView.as_view(),
        name="price-history-branch",
    ),
]
