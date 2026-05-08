from django.db import models
from django.utils import timezone
from apps.common.models import BaseModel


class PromotionType(BaseModel):
    PERCENTAGE = "percentage"
    FIXED_AMOUNT = "fixed_amount"
    TWO_FOR_ONE = "2x1"
    THREE_FOR_TWO = "3x2"
    SECOND_UNIT = "second_unit"
    BANK = "bank"
    DAY_OF_WEEK = "day_of_week"
    CATEGORY = "category"
    PRODUCT = "product"

    TYPE_CHOICES = [
        (PERCENTAGE, "Percentage discount"),
        (FIXED_AMOUNT, "Fixed amount discount"),
        (TWO_FOR_ONE, "2x1"),
        (THREE_FOR_TWO, "3x2"),
        (SECOND_UNIT, "Second unit with discount"),
        (BANK, "Bank / payment method promotion"),
        (DAY_OF_WEEK, "Day-of-week promotion"),
        (CATEGORY, "Category promotion"),
        (PRODUCT, "Product promotion"),
    ]

    code = models.CharField(max_length=30, unique=True, choices=TYPE_CHOICES)
    label = models.CharField(max_length=100)
    description = models.TextField(blank=True)

    class Meta:
        db_table = "promotion_types"
        verbose_name = "Promotion Type"

    def __str__(self):
        return self.label


class PromotionQuerySet(models.QuerySet):
    def active(self):
        today = timezone.now().date()
        return self.filter(
            is_active=True,
            start_date__lte=today,
        ).filter(models.Q(end_date__isnull=True) | models.Q(end_date__gte=today))


class Promotion(BaseModel):
    objects = PromotionQuerySet.as_manager()

    promotion_type = models.ForeignKey(
        PromotionType, on_delete=models.PROTECT, related_name="promotions"
    )
    name = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    discount_percentage = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True
    )
    discount_amount = models.DecimalField(
        max_digits=10, decimal_places=2, null=True, blank=True
    )
    second_unit_discount_percentage = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True
    )
    max_refund_amount = models.DecimalField(
        max_digits=10, decimal_places=2, null=True, blank=True
    )
    start_date = models.DateField(db_index=True)
    end_date = models.DateField(null=True, blank=True, db_index=True)
    is_active = models.BooleanField(default=True, db_index=True)
    banner_image = models.ImageField(upload_to="promotions/", null=True, blank=True)
    terms = models.TextField(blank=True)

    class Meta:
        db_table = "promotions"
        verbose_name = "Promotion"
        verbose_name_plural = "Promotions"
        ordering = ["-start_date"]
        indexes = [
            models.Index(fields=["is_active", "start_date", "end_date"], name="idx_promo_active"),
        ]

    def __str__(self):
        return self.name

    @property
    def is_currently_active(self):
        today = timezone.now().date()
        if not self.is_active or self.start_date > today:
            return False
        return self.end_date is None or self.end_date >= today


class PromotionProduct(BaseModel):
    promotion = models.ForeignKey(
        Promotion, on_delete=models.CASCADE, related_name="promotion_products"
    )
    product = models.ForeignKey(
        "products.Product", on_delete=models.CASCADE, related_name="promotions"
    )

    class Meta:
        db_table = "promotion_products"
        unique_together = ("promotion", "product")


class PromotionCategory(BaseModel):
    promotion = models.ForeignKey(
        Promotion, on_delete=models.CASCADE, related_name="promotion_categories"
    )
    category = models.ForeignKey(
        "products.Category", on_delete=models.CASCADE, related_name="promotions"
    )

    class Meta:
        db_table = "promotion_categories"
        unique_together = ("promotion", "category")


class PromotionSupermarket(BaseModel):
    promotion = models.ForeignKey(
        Promotion, on_delete=models.CASCADE, related_name="promotion_supermarkets"
    )
    supermarket = models.ForeignKey(
        "supermarkets.SupermarketChain", on_delete=models.CASCADE, related_name="promotions"
    )

    class Meta:
        db_table = "promotion_supermarkets"
        unique_together = ("promotion", "supermarket")


class PromotionPaymentMethod(BaseModel):
    CARD_VISA = "visa"
    CARD_MASTERCARD = "mastercard"
    CARD_AMEX = "amex"
    DEBIT = "debit"
    MERCADO_PAGO = "mercado_pago"
    NARANJA = "naranja"

    METHOD_CHOICES = [
        (CARD_VISA, "Visa"),
        (CARD_MASTERCARD, "Mastercard"),
        (CARD_AMEX, "Amex"),
        (DEBIT, "Debit"),
        (MERCADO_PAGO, "Mercado Pago"),
        (NARANJA, "Naranja"),
    ]

    promotion = models.ForeignKey(
        Promotion, on_delete=models.CASCADE, related_name="payment_methods"
    )
    method = models.CharField(max_length=30, choices=METHOD_CHOICES)
    bank_name = models.CharField(max_length=100, blank=True)

    class Meta:
        db_table = "promotion_payment_methods"


class PromotionDayOfWeek(BaseModel):
    promotion = models.ForeignKey(
        Promotion, on_delete=models.CASCADE, related_name="days_of_week"
    )
    weekday = models.PositiveSmallIntegerField(
        choices=[(i, d) for i, d in enumerate(
            ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        )]
    )

    class Meta:
        db_table = "promotion_days_of_week"
        unique_together = ("promotion", "weekday")
