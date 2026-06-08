from django.db import models


class Supermercado(models.Model):
    nombre = models.CharField(max_length=255)
    direccion = models.CharField(max_length=500)
    latitud = models.DecimalField(max_digits=10, decimal_places=7)
    longitud = models.DecimalField(max_digits=10, decimal_places=7)

    class Meta:
        db_table = "supermercado"
        verbose_name = "Supermercado"
        verbose_name_plural = "Supermercados"
        ordering = ["nombre"]

    def __str__(self):
        return self.nombre


class ProductoListado(models.Model):
    producto = models.ForeignKey(
        "products.Producto", on_delete=models.CASCADE, related_name="listados"
    )
    supermercado = models.ForeignKey(
        Supermercado, on_delete=models.CASCADE, related_name="listados"
    )
    precio = models.DecimalField(max_digits=10, decimal_places=2)
    disponible = models.BooleanField(default=True)
    fecha_actualizacion = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "producto_listado"
        unique_together = ("producto", "supermercado")
        verbose_name = "Producto Listado"
        verbose_name_plural = "Productos Listados"

    def __str__(self):
        return f"{self.producto.nombre} @ {self.supermercado.nombre} — ${self.precio}"
