from .base import *  # noqa

DEBUG = True

INSTALLED_APPS += ["debug_toolbar"]  # noqa: F405

MIDDLEWARE.insert(1, "debug_toolbar.middleware.DebugToolbarMiddleware")  # noqa: F405

INTERNAL_IPS = ["127.0.0.1"]

REST_FRAMEWORK["DEFAULT_RENDERER_CLASSES"] += [  # noqa: F405
    "rest_framework.renderers.BrowsableAPIRenderer",
]
