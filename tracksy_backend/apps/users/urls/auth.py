from django.urls import path
<<<<<<< HEAD
from apps.users.views import RegistroView, LoginView, RefreshTokenView
=======
from rest_framework_simplejwt.views import TokenBlacklistView
from apps.users.views import FirebaseSyncView, RegisterView, LoginView, RefreshTokenView
>>>>>>> 13673e03 (Add Firebase authentication backend)

urlpatterns = [
    path("registro/", RegistroView.as_view(), name="auth-registro"),
    path("login/", LoginView.as_view(), name="auth-login"),
    path("refresh/", RefreshTokenView.as_view(), name="auth-refresh"),
<<<<<<< HEAD
=======
    path("logout/", TokenBlacklistView.as_view(), name="auth-logout"),
    path("firebase/sync/", FirebaseSyncView.as_view(), name="auth-firebase-sync"),
>>>>>>> 13673e03 (Add Firebase authentication backend)
]
