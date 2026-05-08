from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from drf_spectacular.views import (
    SpectacularAPIView,
    SpectacularSwaggerView,
    SpectacularRedocView,
)

admin.site.site_header = "Tracksy Administrator"
admin.site.site_title = "Tracksy Admin"
admin.site.index_title = "Administration Panel"

api_v1 = [
    # Auth
    path("auth/", include("apps.users.urls.auth")),
    # Users / profile
    path("users/", include("apps.users.urls.users")),
    # Products, categories, brands
    path("products/", include("apps.products.urls")),
    path("", include("apps.products.urls_meta")),
    # Supermarkets
    path("supermarkets/", include("apps.supermarkets.urls")),
    # Prices
    path("prices/", include("apps.prices.urls")),
    # Promotions
    path("promotions/", include("apps.promotions.urls")),
    # Shopping lists
    path("shopping-lists/", include("apps.shopping_lists.urls")),
    # Comparisons
    path("comparisons/", include("apps.comparisons.urls")),
    # Admin / backoffice
    path("admin-api/", include("apps.imports.urls")),
]

urlpatterns = [
    path("tracksy-admin/", admin.site.urls),
    path("api/v1/", include(api_v1)),
    # OpenAPI schema
    path("api/schema/", SpectacularAPIView.as_view(), name="schema"),
    path("api/docs/", SpectacularSwaggerView.as_view(url_name="schema"), name="swagger-ui"),
    path("api/redoc/", SpectacularRedocView.as_view(url_name="schema"), name="redoc"),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

if settings.DEBUG:
    import debug_toolbar
    urlpatterns = [path("__debug__/", include(debug_toolbar.urls))] + urlpatterns
