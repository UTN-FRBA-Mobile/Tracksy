from django.db import models
from django.conf import settings


class Estado(models.Model):
    nombre = models.CharField(max_length=100, unique=True)

    class Meta:
        db_table = "estado"
        verbose_name = "Estado"
        verbose_name_plural = "Estados"

    def __str__(self):
        return self.nombre


class Sugerencia(models.Model):
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="sugerencias"
    )
    producto = models.ForeignKey(
        "products.Producto", on_delete=models.CASCADE, related_name="sugerencias"
    )
    fecha = models.DateTimeField(auto_now_add=True)
    estado = models.ForeignKey(
        Estado, on_delete=models.PROTECT, related_name="sugerencias"
    )
    motivo = models.TextField()

    class Meta:
        db_table = "sugerencia"
        verbose_name = "Sugerencia"
        verbose_name_plural = "Sugerencias"
        ordering = ["-fecha"]

    def __str__(self):
        return f"Sugerencia #{self.pk} — {self.usuario.email}"


class FeedbackSugerencia(models.Model):
    sugerencia = models.ForeignKey(
        Sugerencia, on_delete=models.CASCADE, related_name="feedbacks"
    )
    fue_util = models.BooleanField()
    fecha = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "feedback_sugerencia"
        verbose_name = "Feedback Sugerencia"
        verbose_name_plural = "Feedbacks Sugerencia"
        ordering = ["-fecha"]

    def __str__(self):
        return f"Feedback #{self.pk} — {'útil' if self.fue_util else 'no útil'}"
