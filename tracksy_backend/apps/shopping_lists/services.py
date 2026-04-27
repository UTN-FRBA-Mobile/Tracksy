from decimal import Decimal
from apps.prices.models import ProductPrice
from apps.supermarkets.models import SupermarketBranch
from .models import ShoppingList, ShoppingListEstimation


class ShoppingListEstimatorService:
    """
    Estimates the total cost of a shopping list at one or more branches.
    Uses only active prices.
    """

    @staticmethod
    def estimate_for_branch(shopping_list: ShoppingList, branch: SupermarketBranch) -> dict:
        items = shopping_list.items.select_related("product").all()
        total = Decimal("0")
        items_with_price = 0
        items_without_price = 0
        breakdown = []

        product_ids = [item.product_id for item in items]
        prices = {
            pp.product_id: pp.price
            for pp in ProductPrice.objects.filter(
                product_id__in=product_ids,
                branch=branch,
                is_active=True,
            )
        }

        for item in items:
            price = prices.get(item.product_id)
            if price is not None:
                line_total = price * item.quantity
                total += line_total
                items_with_price += 1
                breakdown.append(
                    {
                        "product_id": str(item.product_id),
                        "product_name": item.product.name,
                        "quantity": item.quantity,
                        "unit_price": float(price),
                        "line_total": float(line_total),
                    }
                )
            else:
                items_without_price += 1
                breakdown.append(
                    {
                        "product_id": str(item.product_id),
                        "product_name": item.product.name,
                        "quantity": item.quantity,
                        "unit_price": None,
                        "line_total": None,
                    }
                )

        return {
            "branch_id": str(branch.id),
            "branch_name": branch.name,
            "chain_name": branch.chain.name,
            "total_estimated": float(total),
            "items_with_price": items_with_price,
            "items_without_price": items_without_price,
            "currency": "ARS",
            "breakdown": breakdown,
        }

    @classmethod
    def compare_all_branches(cls, shopping_list: ShoppingList) -> list:
        """Return estimates for every branch that has at least one price for the list."""
        items = shopping_list.items.all()
        product_ids = [item.product_id for item in items]

        branch_ids = (
            ProductPrice.objects.filter(product_id__in=product_ids, is_active=True)
            .values_list("branch_id", flat=True)
            .distinct()
        )

        branches = (
            SupermarketBranch.objects.filter(id__in=branch_ids, is_active=True)
            .select_related("chain")
        )

        results = [cls.estimate_for_branch(shopping_list, branch) for branch in branches]
        results.sort(key=lambda r: r["total_estimated"])
        return results

    @classmethod
    def save_estimation(cls, shopping_list: ShoppingList, branch: SupermarketBranch):
        data = cls.estimate_for_branch(shopping_list, branch)
        ShoppingListEstimation.objects.update_or_create(
            shopping_list=shopping_list,
            branch=branch,
            defaults={
                "total_estimated": data["total_estimated"],
                "items_with_price": data["items_with_price"],
                "items_without_price": data["items_without_price"],
            },
        )
        return data
