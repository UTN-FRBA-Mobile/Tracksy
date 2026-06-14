from django.apps import AppConfig


ESTADOS_DEFAULT = ["Pendiente", "Comprado", "No disponible"]


def create_estados_default(sender, **kwargs):
    from apps.shopping_lists.models import EstadoProducto
    for nombre in ESTADOS_DEFAULT:
        EstadoProducto.objects.get_or_create(nombre=nombre)


class ShoppingListsConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "apps.shopping_lists"
    verbose_name = "Shopping Lists"

    def ready(self):
        from django.db.models.signals import post_migrate
        post_migrate.connect(create_estados_default, sender=self)
