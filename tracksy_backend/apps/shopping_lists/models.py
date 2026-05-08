from django.db import models
from django.contrib.auth import get_user_model
from apps.common.models import BaseModel

User = get_user_model()


class ShoppingList(BaseModel):
    STATUS_ACTIVE = "active"
    STATUS_COMPLETED = "completed"
    STATUS_ARCHIVED = "archived"

    STATUS_CHOICES = [
        (STATUS_ACTIVE, "Active"),
        (STATUS_COMPLETED, "Completed"),
        (STATUS_ARCHIVED, "Archived"),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="shopping_lists")
    name = models.CharField(max_length=200)
    preferred_branch = models.ForeignKey(
        "supermarkets.SupermarketBranch",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="shopping_lists",
    )
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default=STATUS_ACTIVE)
    notes = models.TextField(blank=True)

    class Meta:
        db_table = "shopping_lists"
        verbose_name = "Shopping List"
        verbose_name_plural = "Shopping Lists"
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["user", "status"], name="idx_list_user_status"),
        ]

    def __str__(self):
        return f"{self.user.email} — {self.name}"

    @property
    def total_items(self):
        return self.items.count()

    @property
    def purchased_items(self):
        return self.items.filter(is_purchased=True).count()


class ShoppingListItem(BaseModel):
    shopping_list = models.ForeignKey(
        ShoppingList, on_delete=models.CASCADE, related_name="items"
    )
    product = models.ForeignKey(
        "products.Product",
        on_delete=models.CASCADE,
        related_name="shopping_list_items",
    )
    quantity = models.PositiveSmallIntegerField(default=1)
    is_purchased = models.BooleanField(default=False)
    notes = models.CharField(max_length=200, blank=True)

    class Meta:
        db_table = "shopping_list_items"
        unique_together = ("shopping_list", "product")
        verbose_name = "List Item"

    def __str__(self):
        return f"{self.product.name} x{self.quantity}"


class ShoppingListEstimation(BaseModel):
    """Snapshot of an estimated cost for a shopping list at a specific branch."""

    shopping_list = models.ForeignKey(
        ShoppingList, on_delete=models.CASCADE, related_name="estimations"
    )
    branch = models.ForeignKey(
        "supermarkets.SupermarketBranch",
        on_delete=models.CASCADE,
        related_name="list_estimations",
    )
    total_estimated = models.DecimalField(max_digits=14, decimal_places=2)
    items_with_price = models.PositiveSmallIntegerField(default=0)
    items_without_price = models.PositiveSmallIntegerField(default=0)
    currency = models.CharField(max_length=3, default="ARS")

    class Meta:
        db_table = "shopping_list_estimations"
        verbose_name = "List Estimation"
        ordering = ["total_estimated"]
