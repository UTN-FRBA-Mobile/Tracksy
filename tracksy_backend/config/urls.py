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
admin.site.index_title = "Panel de Administración"

api_v1 = [
    # Auth
    path("auth/", include("apps.users.urls.auth")),
    # Usuarios / perfil / favoritos
    path("usuarios/", include("apps.users.urls.users")),
    # Productos y marcas
    path("productos/", include("apps.products.urls")),
    # Supermercados y listados de precios
    path("supermercados/", include("apps.supermarkets.urls")),
    # Listas de compra
    path("listas/", include("apps.shopping_lists.urls")),
    # Compras (historial)
    path("compras/", include("apps.compras.urls")),
    # Sugerencias y feedback
    path("sugerencias/", include("apps.sugerencias.urls")),
    # Importaciones CSV (admin)
    path("imports/", include("apps.imports.urls")),
]

urlpatterns = [
    path("tracksy-admin/", admin.site.urls),
    path("api/v1/", include(api_v1)),
    path("api/schema/", SpectacularAPIView.as_view(), name="schema"),
    path("api/docs/", SpectacularSwaggerView.as_view(url_name="schema"), name="swagger-ui"),
    path("api/redoc/", SpectacularRedocView.as_view(url_name="schema"), name="redoc"),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

if settings.DEBUG:
    import debug_toolbar
    urlpatterns = [path("__debug__/", include(debug_toolbar.urls))] + urlpatterns
