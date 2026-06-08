"""
Migration: barcode as PK for Producto.

- Removes the `codigo_barra` column added in 0002.
- Changes `id` from BigAutoField (auto-increment sequence) to
  BigIntegerField (manual — the EAN-13 barcode IS the primary key).

All tables that FK to producto are wiped first (dev data only):
  item_producto, producto_comprado, producto_listado, sugerencia,
  producto_usuario — all reached via CASCADE on TRUNCATE.
"""
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("products", "0002_add_codigo_barra_to_producto"),
    ]

    operations = [
        # 1. Remove the extra column we added in 0002.
        migrations.RemoveField(
            model_name="producto",
            name="codigo_barra",
        ),

        # 2. In the DB: wipe dependent data, drop the auto-increment sequence.
        #    In Django's migration state: declare id as BigIntegerField (no auto).
        migrations.SeparateDatabaseAndState(
            database_operations=[
                migrations.RunSQL(
                    sql="""
                        -- Wipe all rows that FK to producto (dev data — safe to clear).
                        TRUNCATE TABLE producto CASCADE;

                        -- Django 4+ uses IDENTITY columns (not sequences) for BigAutoField.
                        -- Drop the identity property so id must always be provided explicitly.
                        ALTER TABLE producto ALTER COLUMN id DROP IDENTITY;
                    """,
                    reverse_sql=migrations.RunSQL.noop,
                ),
            ],
            state_operations=[
                migrations.AlterField(
                    model_name="producto",
                    name="id",
                    field=models.BigIntegerField(
                        primary_key=True,
                        serialize=False,
                        verbose_name="Código de barras EAN-13",
                    ),
                ),
            ],
        ),
    ]
