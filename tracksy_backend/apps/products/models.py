from django.db import models


class Marca(models.Model):
    nombre = models.CharField(max_length=150, unique=True)

    class Meta:
        db_table = "marca"
        verbose_name = "Marca"
        verbose_name_plural = "Marcas"
        ordering = ["nombre"]

    def __str__(self):
        return self.nombre


class Producto(models.Model):
    # Primary key IS the EAN-13 barcode — no auto-increment, always provided on import.
    id = models.BigIntegerField(
        primary_key=True,
        verbose_name="Código de barras EAN-13",
    )
    nombre = models.CharField(max_length=255, db_index=True)
    marca = models.ForeignKey(
        Marca,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="productos",
    )

    class Meta:
        db_table = "producto"
        verbose_name = "Producto"
        verbose_name_plural = "Productos"
        ordering = ["nombre"]

    def __str__(self):
        return self.nombre
