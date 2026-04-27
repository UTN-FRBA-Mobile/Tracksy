from django.contrib.auth.models import AbstractUser
from django.db import models
from apps.common.models import BaseModel


class User(AbstractUser):
    """
    Custom user model.
    username is kept for Django admin compatibility; email is the login field.
    """

    ROLE_CONSUMER = "consumer"
    ROLE_ADMIN = "admin"
    ROLE_BACKOFFICE = "backoffice"

    ROLE_CHOICES = [
        (ROLE_CONSUMER, "Consumidor"),
        (ROLE_ADMIN, "Administrador"),
        (ROLE_BACKOFFICE, "Operador Backoffice"),
    ]

    email = models.EmailField(unique=True)
    role = models.CharField(max_length=20, choices=ROLE_CHOICES, default=ROLE_CONSUMER)
    is_email_verified = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["username"]

    class Meta:
        db_table = "users"
        verbose_name = "Usuario"
        verbose_name_plural = "Usuarios"

    def __str__(self):
        return self.email


class UserProfile(BaseModel):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name="profile")
    first_name = models.CharField(max_length=100, blank=True)
    last_name = models.CharField(max_length=100, blank=True)
    phone = models.CharField(max_length=20, blank=True)
    avatar = models.ImageField(upload_to="avatars/", null=True, blank=True)
    city = models.CharField(max_length=100, blank=True)
    province = models.CharField(max_length=100, blank=True)

    class Meta:
        db_table = "user_profiles"
        verbose_name = "Perfil de Usuario"

    def __str__(self):
        return f"Perfil de {self.user.email}"


class UserPreferences(BaseModel):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name="preferences")
    preferred_supermarkets = models.ManyToManyField(
        "supermarkets.SupermarketChain",
        blank=True,
        related_name="preferred_by_users",
    )
    notify_promotions = models.BooleanField(default=True)
    notify_price_drops = models.BooleanField(default=True)
    default_search_radius_km = models.PositiveSmallIntegerField(default=10)
    currency = models.CharField(max_length=3, default="ARS")

    class Meta:
        db_table = "user_preferences"
        verbose_name = "Preferencias de Usuario"


class FavoriteProduct(BaseModel):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="favorite_products")
    product = models.ForeignKey(
        "products.Product", on_delete=models.CASCADE, related_name="favorited_by"
    )

    class Meta:
        db_table = "favorite_products"
        unique_together = ("user", "product")
        verbose_name = "Producto Favorito"

    def __str__(self):
        return f"{self.user.email} — {self.product}"


class FavoriteSupermarket(BaseModel):
    user = models.ForeignKey(
        User, on_delete=models.CASCADE, related_name="favorite_supermarkets"
    )
    supermarket = models.ForeignKey(
        "supermarkets.SupermarketChain",
        on_delete=models.CASCADE,
        related_name="favorited_by",
    )

    class Meta:
        db_table = "favorite_supermarkets"
        unique_together = ("user", "supermarket")
        verbose_name = "Supermercado Favorito"
