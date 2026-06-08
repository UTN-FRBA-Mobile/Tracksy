from django.db import migrations


ESTADOS_DEFAULT = [
    "Pendiente",
    "Comprado",
    "No disponible",
]


def seed_estados(apps, schema_editor):
    EstadoProducto = apps.get_model("shopping_lists", "EstadoProducto")
    for nombre in ESTADOS_DEFAULT:
        EstadoProducto.objects.get_or_create(nombre=nombre)


def reverse_seed(apps, schema_editor):
    EstadoProducto = apps.get_model("shopping_lists", "EstadoProducto")
    EstadoProducto.objects.filter(nombre__in=ESTADOS_DEFAULT).delete()


class Migration(migrations.Migration):

    dependencies = [
        ("shopping_lists", "0001_initial"),
    ]

    operations = [
        migrations.RunPython(seed_estados, reverse_code=reverse_seed),
    ]
