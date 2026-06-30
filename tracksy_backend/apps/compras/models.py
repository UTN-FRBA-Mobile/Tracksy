from django.db import models
from django.conf import settings


class Compra(models.Model):
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="compras"
    )
    supermercado = models.ForeignKey(
        "supermarkets.Supermercado", on_delete=models.PROTECT, related_name="compras",
        null=True, blank=True
    )
    nombre_lista = models.CharField(max_length=255, blank=True, default="")
    fecha = models.DateTimeField(auto_now_add=True)
    total = models.DecimalField(max_digits=10, decimal_places=2)

    class Meta:
        db_table = "compra"
        verbose_name = "Compra"
        verbose_name_plural = "Compras"
        ordering = ["-fecha"]

    def __str__(self):
        return f"Compra #{self.pk} — {self.usuario.email}"


class ProductoComprado(models.Model):
    compra = models.ForeignKey(
        Compra, on_delete=models.CASCADE, related_name="productos"
    )
    producto = models.ForeignKey(
        "products.Producto", on_delete=models.PROTECT, related_name="comprados"
    )
    cantidad = models.PositiveIntegerField()
    precio_unitario = models.DecimalField(max_digits=10, decimal_places=2)

    class Meta:
        db_table = "producto_comprado"
        unique_together = ("compra", "producto")
        verbose_name = "Producto Comprado"
        verbose_name_plural = "Productos Comprados"

    def __str__(self):
        return f"{self.producto.nombre} x{self.cantidad}"
