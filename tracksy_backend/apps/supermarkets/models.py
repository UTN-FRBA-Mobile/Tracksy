from django.db import models
from apps.common.models import BaseModel


class SupermarketChain(BaseModel):
    name = models.CharField(max_length=150, unique=True)
    slug = models.SlugField(max_length=170, unique=True)
    logo = models.ImageField(upload_to="supermarkets/logos/", null=True, blank=True)
    website = models.URLField(blank=True)
    is_active = models.BooleanField(default=True, db_index=True)

    class Meta:
        db_table = "supermarket_chains"
        verbose_name = "Cadena de Supermercado"
        verbose_name_plural = "Cadenas de Supermercado"
        ordering = ["name"]

    def __str__(self):
        return self.name


class SupermarketBranch(BaseModel):
    chain = models.ForeignKey(
        SupermarketChain, on_delete=models.CASCADE, related_name="branches"
    )
    name = models.CharField(max_length=200)
    code = models.CharField(max_length=50, blank=True, db_index=True)
    address = models.CharField(max_length=300)
    city = models.CharField(max_length=100, db_index=True)
    province = models.CharField(max_length=100, db_index=True)
    postal_code = models.CharField(max_length=20, blank=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    longitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    phone = models.CharField(max_length=30, blank=True)
    email = models.EmailField(blank=True)
    is_active = models.BooleanField(default=True, db_index=True)

    class Meta:
        db_table = "supermarket_branches"
        verbose_name = "Sucursal"
        verbose_name_plural = "Sucursales"
        indexes = [
            models.Index(fields=["city", "province"], name="idx_branch_location"),
            models.Index(fields=["latitude", "longitude"], name="idx_branch_geo"),
        ]

    def __str__(self):
        return f"{self.chain.name} — {self.name} ({self.city})"


class BranchOpeningHours(BaseModel):
    WEEKDAYS = [
        (0, "Lunes"),
        (1, "Martes"),
        (2, "Miércoles"),
        (3, "Jueves"),
        (4, "Viernes"),
        (5, "Sábado"),
        (6, "Domingo"),
    ]

    branch = models.ForeignKey(
        SupermarketBranch, on_delete=models.CASCADE, related_name="opening_hours"
    )
    weekday = models.PositiveSmallIntegerField(choices=WEEKDAYS)
    open_time = models.TimeField()
    close_time = models.TimeField()
    is_closed = models.BooleanField(default=False)

    class Meta:
        db_table = "branch_opening_hours"
        unique_together = ("branch", "weekday")
        verbose_name = "Horario de Sucursal"

    def __str__(self):
        return f"{self.branch} — {self.get_weekday_display()}"
