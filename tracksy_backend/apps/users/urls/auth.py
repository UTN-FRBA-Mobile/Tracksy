from django.urls import path
from apps.users.views import FirebaseSyncView, RegistroView, LoginView, RefreshTokenView

urlpatterns = [
    path("registro/", RegistroView.as_view(), name="auth-registro"),
    path("login/", LoginView.as_view(), name="auth-login"),
    path("refresh/", RefreshTokenView.as_view(), name="auth-refresh"),
    path("firebase/sync/", FirebaseSyncView.as_view(), name="auth-firebase-sync"),
]
