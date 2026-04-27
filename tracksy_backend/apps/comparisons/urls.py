from django.urls import path
from .views import (
    ProductPriceComparisonView,
    SupermarketRecommendationView,
    ApplicablePromotionsView,
)

urlpatterns = [
    path("prices/", ProductPriceComparisonView.as_view(), name="compare-prices"),
    path("recommend/", SupermarketRecommendationView.as_view(), name="recommend-supermarket"),
    path("promotions/", ApplicablePromotionsView.as_view(), name="applicable-promotions"),
]
