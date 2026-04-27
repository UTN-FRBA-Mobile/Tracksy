from django.urls import path
from rest_framework_simplejwt.views import TokenBlacklistView
from apps.users.views import RegisterView, LoginView, RefreshTokenView

urlpatterns = [
    path("register/", RegisterView.as_view(), name="auth-register"),
    path("login/", LoginView.as_view(), name="auth-login"),
    path("refresh/", RefreshTokenView.as_view(), name="auth-refresh"),
    path("logout/", TokenBlacklistView.as_view(), name="auth-logout"),
]
