from pathlib import Path
from datetime import timedelta
from decouple import config

BASE_DIR = Path(__file__).resolve().parent.parent.parent

SECRET_KEY = config("SECRET_KEY")
DEBUG = config("DEBUG", default=False, cast=bool)
ALLOWED_HOSTS = config("ALLOWED_HOSTS", default="localhost").split(",")

DJANGO_APPS = [
    "jazzmin",  # must be before django.contrib.admin
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
]

THIRD_PARTY_APPS = [
    "rest_framework",
    "rest_framework_simplejwt",
    "corsheaders",
    "django_filters",
    "drf_spectacular",
]

LOCAL_APPS = [
    "apps.common",
    "apps.users",
    "apps.products",
    "apps.supermarkets",
    "apps.shopping_lists",
    "apps.compras",
    "apps.sugerencias",
    "apps.imports",
]

INSTALLED_APPS = DJANGO_APPS + THIRD_PARTY_APPS + LOCAL_APPS

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "corsheaders.middleware.CorsMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
    "apps.common.middleware.RequestLoggingMiddleware",
]

ROOT_URLCONF = "config.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [BASE_DIR / "templates"],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "config.wsgi.application"

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": config("DB_NAME", default="tracksy_db"),
        "USER": config("DB_USER", default="tracksy_user"),
        "PASSWORD": config("DB_PASSWORD", default="tracksy_password"),
        "HOST": config("DB_HOST", default="localhost"),
        "PORT": config("DB_PORT", default="5432"),
        "OPTIONS": {
            "options": "-c search_path=public",
        },
    }
}

AUTH_USER_MODEL = "users.Usuario"

AUTH_PASSWORD_VALIDATORS = [
    {"NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator"},
    {"NAME": "django.contrib.auth.password_validation.MinimumLengthValidator"},
    {"NAME": "django.contrib.auth.password_validation.CommonPasswordValidator"},
    {"NAME": "django.contrib.auth.password_validation.NumericPasswordValidator"},
]

LANGUAGE_CODE = "en-us"
TIME_ZONE = "America/Argentina/Buenos_Aires"
USE_I18N = True
USE_TZ = True

STATIC_URL = "/static/"
STATIC_ROOT = BASE_DIR / "staticfiles"
STATICFILES_DIRS = [BASE_DIR / "static"]

MEDIA_URL = config("MEDIA_URL", default="/media/")
MEDIA_ROOT = BASE_DIR / config("MEDIA_ROOT", default="media")

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": [
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ],
    "DEFAULT_PERMISSION_CLASSES": [
        "rest_framework.permissions.IsAuthenticated",
    ],
    "DEFAULT_SCHEMA_CLASS": "drf_spectacular.openapi.AutoSchema",
    "DEFAULT_PAGINATION_CLASS": "apps.common.pagination.StandardResultsPagination",
    "PAGE_SIZE": 20,
    "DEFAULT_FILTER_BACKENDS": [
        "django_filters.rest_framework.DjangoFilterBackend",
        "rest_framework.filters.SearchFilter",
        "rest_framework.filters.OrderingFilter",
    ],
    "DEFAULT_RENDERER_CLASSES": [
        "rest_framework.renderers.JSONRenderer",
    ],
    "EXCEPTION_HANDLER": "apps.common.exceptions.custom_exception_handler",
    # Return decimals as JSON numbers, not strings — avoids Gson parse failures on mobile
    "COERCE_DECIMAL_TO_STRING": False,
}

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(
        minutes=config("JWT_ACCESS_TOKEN_LIFETIME_MINUTES", default=60, cast=int)
    ),
    "REFRESH_TOKEN_LIFETIME": timedelta(
        days=config("JWT_REFRESH_TOKEN_LIFETIME_DAYS", default=7, cast=int)
    ),
    "AUTH_HEADER_TYPES": ("Bearer",),
    "AUTH_HEADER_NAME": "HTTP_AUTHORIZATION",
    "USER_ID_FIELD": "id",
    "USER_ID_CLAIM": "user_id",
}

SPECTACULAR_SETTINGS = {
    "TITLE": "Tracksy API",
    "DESCRIPTION": (
        "REST API for Tracksy — a price comparison and shopping list management "
        "platform for consumers."
    ),
    "VERSION": "1.0.0",
    "SERVE_INCLUDE_SCHEMA": False,
    "COMPONENT_SPLIT_REQUEST": True,
    "TAGS": [
        {"name": "auth", "description": "Authentication and JWT tokens"},
        {"name": "users", "description": "Consumer profile and preferences"},
        {"name": "products", "description": "Product catalogue"},
        {"name": "supermarkets", "description": "Chains and branches"},
        {"name": "prices", "description": "Prices and history"},
        {"name": "promotions", "description": "Active promotions"},
        {"name": "shopping-lists", "description": "Shopping lists"},
        {"name": "favorites", "description": "User favourites"},
        {"name": "comparisons", "description": "Comparison and recommendations"},
        {"name": "admin", "description": "Backoffice — requires administrator role"},
        {"name": "imports", "description": "Bulk data import"},
    ],
}

CORS_ALLOWED_ORIGINS = config(
    "CORS_ALLOWED_ORIGINS",
    default="http://localhost:8000,http://10.0.2.2:8000",
).split(",")
CORS_ALLOW_ALL_ORIGINS = config("CORS_ALLOW_ALL_ORIGINS", default=True, cast=bool)
CORS_ALLOW_CREDENTIALS = True

LOGGING = {
    "version": 1,
    "disable_existing_loggers": False,
    "formatters": {
        "verbose": {
            "format": "{levelname} {asctime} {module} {process:d} {thread:d} {message}",
            "style": "{",
        },
        "request": {
            "format": "{asctime} {message}",
            "style": "{",
        },
    },
    "handlers": {
        "console": {
            "class": "logging.StreamHandler",
            "formatter": "verbose",
        },
        "request_console": {
            "class": "logging.StreamHandler",
            "formatter": "request",
        },
    },
    "root": {
        "handlers": ["console"],
        "level": "INFO",
    },
    "loggers": {
        "django": {"handlers": ["console"], "level": "INFO", "propagate": False},
        "apps": {"handlers": ["console"], "level": "DEBUG", "propagate": False},
        "tracksy.requests": {
            "handlers": ["request_console"],
            "level": "INFO",
            "propagate": False,
        },
    },
}

# ── Jazzmin admin UI ──────────────────────────────────────────────────────────
JAZZMIN_SETTINGS = {
    "site_title": "Tracksy Admin",
    "site_header": "Tracksy Administrator",
    "site_brand": "Tracksy Admin",
    "site_logo": "img/tracksy_logo.png",
    "login_logo": "img/tracksy_logo.png",
    "site_logo_classes": "img-fluid",
    "site_icon": None,
    "welcome_sign": "Welcome to the Tracksy Administration Panel",
    "copyright": "Tracksy — DAMM TP 2026",
    "search_model": [
        "users.Usuario",
        "products.Producto",
        "supermarkets.Supermercado",
    ],
    "user_avatar": None,
    "topmenu_links": [
        {"name": "Home", "url": "admin:index", "permissions": ["auth.view_user"]},
        {
            "name": "API Docs",
            "url": "/api/docs/",
            "new_window": True,
            "icon": "fas fa-book",
        },
    ],
    "usermenu_links": [],
    "show_sidebar": True,
    "navigation_expanded": True,
    "hide_apps": [],
    "hide_models": ["products.measurementunit"],
    "order_with_respect_to": [
        "imports",
        "supermarkets",
        "products",
        "shopping_lists",
        "compras",
        "sugerencias",
        "users",
        "auth",
    ],
    "custom_links": {
        "imports": [
            {
                "name": "Importar Productos",
                "url": "imports_productos_upload",
                "icon": "fas fa-box",
                "permissions": ["imports.add_importbatch"],
            },
            {
                "name": "Importar Marcas",
                "url": "imports_marcas_upload",
                "icon": "fas fa-trademark",
                "permissions": ["imports.add_importbatch"],
            },
            {
                "name": "Importar Supermercados",
                "url": "imports_supermercados_upload",
                "icon": "fas fa-store",
                "permissions": ["imports.add_importbatch"],
            },
            {
                "name": "Importar Listados",
                "url": "imports_listados_upload",
                "icon": "fas fa-dollar-sign",
                "permissions": ["imports.add_importbatch"],
            },
        ],
    },
    "icons": {
        "auth": "fas fa-users-cog",
        "auth.user": "fas fa-user",
        "auth.Group": "fas fa-users",
        "users.Usuario": "fas fa-user-circle",
        "users.ProductoUsuario": "fas fa-heart",
        "products.Producto": "fas fa-box",
        "products.Marca": "fas fa-trademark",
        "supermarkets.Supermercado": "fas fa-store",
        "supermarkets.ProductoListado": "fas fa-dollar-sign",
        "shopping_lists.ListaCompra": "fas fa-list",
        "shopping_lists.ItemProducto": "fas fa-shopping-basket",
        "shopping_lists.EstadoProducto": "fas fa-tags",
        "compras.Compra": "fas fa-receipt",
        "compras.ProductoComprado": "fas fa-check",
        "sugerencias.Sugerencia": "fas fa-lightbulb",
        "sugerencias.Estado": "fas fa-flag",
        "imports.ImportBatch": "fas fa-file-import",
    },
    "default_icon_parents": "fas fa-chevron-circle-right",
    "default_icon_children": "fas fa-circle",
    "related_modal_active": True,
    "custom_css": "admin/css/tracksy_admin.css",
    "custom_js": "admin/js/tracksy_admin.js",
    "use_google_fonts_cdn": True,
    "show_ui_builder": False,
    "changeform_format": "horizontal_tabs",
    "changeform_format_overrides": {
        "auth.user": "collapsible",
    },
    "language_chooser": False,
}

JAZZMIN_UI_TWEAKS = {
    "navbar_small_text": False,
    "footer_small_text": False,
    "body_small_text": True,
    "brand_small_text": False,
    "brand_colour": "navbar-purple",
    "accent": "accent-purple",
    "navbar": "navbar-purple navbar-dark",
    "no_navbar_border": True,
    "navbar_fixed": True,
    "layout_boxed": False,
    "footer_fixed": False,
    "sidebar_fixed": True,
    "sidebar": "sidebar-dark-purple",
    "sidebar_nav_small_text": False,
    "sidebar_disable_expand": False,
    "sidebar_nav_child_indent": True,
    "sidebar_nav_compact_style": True,
    "sidebar_nav_legacy_style": False,
    "sidebar_nav_flat_style": False,
    "theme": "pulse",
    "dark_mode_theme": None,
    "button_classes": {
        "primary": "btn-primary",
        "secondary": "btn-outline-secondary",
        "info": "btn-primary",
        "warning": "btn-warning",
        "danger": "btn-danger",
        "success": "btn-primary",
    },
}
