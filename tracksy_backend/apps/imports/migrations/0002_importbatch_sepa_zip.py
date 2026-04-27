from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("imports", "0001_initial"),
    ]

    operations = [
        migrations.AlterField(
            model_name="importbatch",
            name="import_type",
            field=models.CharField(
                choices=[
                    ("products", "Productos"),
                    ("prices", "Precios"),
                    ("supermarkets", "Supermercados"),
                    ("branches", "Sucursales"),
                    ("promotions", "Promociones"),
                    ("sepa_zip", "SEPA ZIP (comercios + sucursales + productos)"),
                ],
                max_length=20,
            ),
        ),
    ]
