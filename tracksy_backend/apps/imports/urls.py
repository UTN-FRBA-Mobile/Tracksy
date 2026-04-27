from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ImportBatchViewSet

router = DefaultRouter()
router.register(r"imports", ImportBatchViewSet, basename="import-batch")

urlpatterns = [
    path("", include(router.urls)),
]
