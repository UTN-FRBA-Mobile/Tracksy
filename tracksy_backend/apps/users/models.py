from django.contrib.auth.models import AbstractUser
from django.db import models


class Usuario(AbstractUser):
    nombre = models.CharField(max_length=150)
    email = models.EmailField(unique=True)

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["username", "nombre"]

    class Meta:
        db_table = "usuario"
        verbose_name = "Usuario"
        verbose_name_plural = "Usuarios"

    def __str__(self):
        return self.email


class ProductoUsuario(models.Model):
    usuario = models.ForeignKey(
        Usuario, on_delete=models.CASCADE, related_name="productos_usuario"
    )
    producto = models.ForeignKey(
        "products.Producto", on_delete=models.CASCADE, related_name="usuarios"
    )
    favorito = models.BooleanField(default=False)

    class Meta:
        db_table = "producto_usuario"
        unique_together = ("usuario", "producto")
        verbose_name = "Producto Usuario"
        verbose_name_plural = "Productos Usuario"

    def __str__(self):
        return f"{self.usuario.email} — {self.producto.nombre}"
