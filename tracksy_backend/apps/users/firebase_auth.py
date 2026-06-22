from pathlib import Path

import firebase_admin
from django.conf import settings
from django.contrib.auth import get_user_model
from firebase_admin import auth, credentials
from rest_framework import authentication, exceptions
from rest_framework_simplejwt.authentication import JWTAuthentication


def _initialize_firebase_app():
    if firebase_admin._apps:
        return firebase_admin.get_app()

    credentials_path = settings.FIREBASE_CREDENTIALS_PATH
    project_id = settings.FIREBASE_PROJECT_ID

    if not credentials_path:
        raise exceptions.AuthenticationFailed("Firebase credentials are not configured.")

    path = Path(credentials_path)
    if not path.exists():
        raise exceptions.AuthenticationFailed("Firebase credentials file was not found.")

    options = {"projectId": project_id} if project_id else None
    return firebase_admin.initialize_app(credentials.Certificate(str(path)), options)


class FirebaseAuthentication(authentication.BaseAuthentication):
    """
    Authenticate DRF requests using Firebase ID tokens.

    Requests must send:
        Authorization: Bearer <firebase_id_token>
    """

    keyword = "Bearer"

    def authenticate(self, request):
        auth_header = authentication.get_authorization_header(request).split()

        if not auth_header:
            return None

        if auth_header[0].lower() != self.keyword.lower().encode():
            return None

        if len(auth_header) != 2:
            raise exceptions.AuthenticationFailed("Invalid Authorization header.")

        token = auth_header[1].decode("utf-8")

        try:
            _initialize_firebase_app()
            decoded_token = auth.verify_id_token(token)
        except exceptions.AuthenticationFailed:
            raise
        except Exception as exc:
            raise exceptions.AuthenticationFailed("Invalid Firebase ID token.") from exc

        user = self.get_or_create_user(decoded_token)
        return (user, decoded_token)

    def get_or_create_user(self, decoded_token):
        firebase_uid = decoded_token.get("uid")
        email = decoded_token.get("email")

        if not firebase_uid:
            raise exceptions.AuthenticationFailed("Firebase token does not include uid.")

        if not email:
            raise exceptions.AuthenticationFailed("Firebase token does not include email.")

        display_name = decoded_token.get("name") or email.split("@", 1)[0]

        User = get_user_model()
        user = User.objects.filter(firebase_uid=firebase_uid).first()

        if user is None:
            user = User.objects.filter(email=email).first()

        if user is None:
            username = self.build_username(email, firebase_uid)
            user = User.objects.create_user(
                email=email,
                username=username,
                nombre=display_name,
                password=None,
            )

        changed_fields = []
        if user.firebase_uid != firebase_uid:
            user.firebase_uid = firebase_uid
            changed_fields.append("firebase_uid")
        if user.email != email:
            user.email = email
            changed_fields.append("email")

        if changed_fields:
            user.save(update_fields=changed_fields)

        return user

    @staticmethod
    def build_username(email, firebase_uid):
        base_username = email.split("@", 1)[0][:120] or "firebase-user"
        return f"{base_username}-{firebase_uid[:24]}"


class TracksyAuthentication(authentication.BaseAuthentication):
    """
    Backwards-compatible auth during the Firebase migration.

    Existing Django JWTs and new Firebase ID tokens both use Authorization: Bearer,
    so this wrapper tries both schemes before rejecting the request.
    """

    def __init__(self):
        self.jwt_authentication = JWTAuthentication()
        self.firebase_authentication = FirebaseAuthentication()

    def authenticate(self, request):
        jwt_error = None

        try:
            jwt_result = self.jwt_authentication.authenticate(request)
            if jwt_result is not None:
                return jwt_result
        except Exception as exc:
            jwt_error = exc

        try:
            firebase_result = self.firebase_authentication.authenticate(request)
            if firebase_result is not None:
                return firebase_result
        except exceptions.AuthenticationFailed:
            if jwt_error is not None:
                raise
            raise

        if jwt_error is not None:
            raise jwt_error

        return None
