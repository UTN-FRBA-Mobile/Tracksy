from django.urls import path, include
from rest_framework.routers import DefaultRouter
from apps.users.views import MeView, FavoriteProductViewSet, FavoriteSupermarketViewSet

router = DefaultRouter()
router.register(r"favorites/products", FavoriteProductViewSet, basename="favorite-products")
router.register(r"favorites/supermarkets", FavoriteSupermarketViewSet, basename="favorite-supermarkets")

urlpatterns = [
    path("me/", MeView.as_view(), name="user-me"),
    path("", include(router.urls)),
]
