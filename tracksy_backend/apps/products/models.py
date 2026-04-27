from django.db import models
from apps.common.models import BaseModel


class Category(BaseModel):
    name = models.CharField(max_length=100, unique=True)
    slug = models.SlugField(max_length=120, unique=True)
    icon = models.CharField(max_length=100, blank=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = "categories"
        verbose_name = "Categoría"
        verbose_name_plural = "Categorías"
        ordering = ["name"]

    def __str__(self):
        return self.name


class SubCategory(BaseModel):
    category = models.ForeignKey(Category, on_delete=models.CASCADE, related_name="subcategories")
    name = models.CharField(max_length=100)
    slug = models.SlugField(max_length=120)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = "subcategories"
        unique_together = ("category", "slug")
        verbose_name = "Subcategoría"
        verbose_name_plural = "Subcategorías"
        ordering = ["name"]

    def __str__(self):
        return f"{self.category.name} > {self.name}"


class Brand(BaseModel):
    name = models.CharField(max_length=150, unique=True)
    slug = models.SlugField(max_length=170, unique=True)
    logo = models.ImageField(upload_to="brands/", null=True, blank=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = "brands"
        verbose_name = "Marca"
        verbose_name_plural = "Marcas"
        ordering = ["name"]

    def __str__(self):
        return self.name


class MeasurementUnit(BaseModel):
    name = models.CharField(max_length=50, unique=True)
    abbreviation = models.CharField(max_length=10, unique=True)

    class Meta:
        db_table = "measurement_units"
        verbose_name = "Unidad de Medida"

    def __str__(self):
        return self.abbreviation


class Product(BaseModel):
    barcode = models.CharField(max_length=50, unique=True, db_index=True)
    name = models.CharField(max_length=250, db_index=True)
    description = models.TextField(blank=True)
    brand = models.ForeignKey(
        Brand, on_delete=models.SET_NULL, null=True, blank=True, related_name="products"
    )
    category = models.ForeignKey(
        Category, on_delete=models.SET_NULL, null=True, blank=True, related_name="products"
    )
    subcategory = models.ForeignKey(
        SubCategory,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="products",
    )
    measurement_unit = models.ForeignKey(
        MeasurementUnit,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="products",
    )
    presentation = models.CharField(max_length=100, blank=True)
    net_content = models.CharField(max_length=50, blank=True)
    thumbnail = models.ImageField(upload_to="products/", null=True, blank=True)
    is_active = models.BooleanField(default=True, db_index=True)

    class Meta:
        db_table = "products"
        verbose_name = "Producto"
        verbose_name_plural = "Productos"
        indexes = [
            models.Index(fields=["name"], name="idx_product_name"),
            models.Index(fields=["barcode"], name="idx_product_barcode"),
            models.Index(fields=["is_active"], name="idx_product_active"),
        ]

    def __str__(self):
        return f"{self.name} ({self.barcode})"


class ProductImage(BaseModel):
    product = models.ForeignKey(Product, on_delete=models.CASCADE, related_name="images")
    image = models.ImageField(upload_to="products/images/")
    alt_text = models.CharField(max_length=200, blank=True)
    is_primary = models.BooleanField(default=False)
    order = models.PositiveSmallIntegerField(default=0)

    class Meta:
        db_table = "product_images"
        ordering = ["order"]
        verbose_name = "Imagen de Producto"
