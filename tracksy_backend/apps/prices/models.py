from django.db import models
from apps.common.models import BaseModel


class ProductPrice(BaseModel):
    """Current price of a product at a specific branch."""

    SOURCE_MANUAL = "manual"
    SOURCE_IMPORT = "import"
    SOURCE_SCRAPING = "scraping"
    SOURCE_API = "api"

    SOURCE_CHOICES = [
        (SOURCE_MANUAL, "Manual"),
        (SOURCE_IMPORT, "CSV Import"),
        (SOURCE_SCRAPING, "Scraping"),
        (SOURCE_API, "External API"),
    ]

    product = models.ForeignKey(
        "products.Product",
        on_delete=models.CASCADE,
        related_name="prices",
        db_index=True,
    )
    branch = models.ForeignKey(
        "supermarkets.SupermarketBranch",
        on_delete=models.CASCADE,
        related_name="prices",
        db_index=True,
    )
    price = models.DecimalField(max_digits=12, decimal_places=2)
    currency = models.CharField(max_length=3, default="ARS")
    valid_from = models.DateField()
    valid_until = models.DateField(null=True, blank=True)
    source = models.CharField(max_length=20, choices=SOURCE_CHOICES, default=SOURCE_MANUAL)
    is_active = models.BooleanField(default=True, db_index=True)

    class Meta:
        db_table = "product_prices"
        verbose_name = "Product Price"
        verbose_name_plural = "Product Prices"
        indexes = [
            models.Index(fields=["product", "branch", "is_active"], name="idx_price_lookup"),
            models.Index(fields=["product", "price"], name="idx_price_comparison"),
        ]
        unique_together = ("product", "branch", "valid_from")

    def __str__(self):
        return f"{self.product} @ {self.branch} = ${self.price}"

    def save(self, *args, **kwargs):
        if self.is_active:
            ProductPrice.objects.filter(
                product=self.product,
                branch=self.branch,
                is_active=True,
            ).exclude(pk=self.pk).update(is_active=False)
        super().save(*args, **kwargs)
        PriceHistory.objects.create(
            product=self.product,
            branch=self.branch,
            price=self.price,
            currency=self.currency,
            source=self.source,
        )


class PriceHistory(BaseModel):
    """Immutable log of every price recorded for a product/branch pair."""

    product = models.ForeignKey(
        "products.Product",
        on_delete=models.CASCADE,
        related_name="price_history",
    )
    branch = models.ForeignKey(
        "supermarkets.SupermarketBranch",
        on_delete=models.CASCADE,
        related_name="price_history",
    )
    price = models.DecimalField(max_digits=12, decimal_places=2)
    currency = models.CharField(max_length=3, default="ARS")
    source = models.CharField(max_length=20)

    class Meta:
        db_table = "price_history"
        verbose_name = "Price History"
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["product", "branch", "created_at"], name="idx_price_history"),
        ]
