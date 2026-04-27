from decimal import Decimal
from django.db.models import Min
from apps.prices.models import ProductPrice
from apps.promotions.models import Promotion


class PriceComparisonService:
    """
    Compare a single product's price across all supermarket branches.
    Returns a ranked list with the cheapest option highlighted.
    """

    @staticmethod
    def compare_product_prices(product) -> dict:
        prices = (
            ProductPrice.objects.filter(product=product, is_active=True)
            .select_related("branch__chain")
            .order_by("price")
        )

        if not prices:
            return {"product_id": str(product.id), "prices": [], "cheapest": None}

        entries = []
        for pp in prices:
            entries.append(
                {
                    "branch_id": str(pp.branch.id),
                    "branch_name": pp.branch.name,
                    "chain_id": str(pp.branch.chain.id),
                    "chain_name": pp.branch.chain.name,
                    "city": pp.branch.city,
                    "price": float(pp.price),
                    "currency": pp.currency,
                    "updated_at": pp.updated_at.isoformat(),
                }
            )

        cheapest = entries[0]
        most_expensive = entries[-1]
        saving = round(most_expensive["price"] - cheapest["price"], 2)

        return {
            "product_id": str(product.id),
            "product_name": product.name,
            "prices": entries,
            "cheapest": cheapest,
            "most_expensive": most_expensive,
            "potential_saving": saving,
            "currency": "ARS",
        }


class PromotionApplicationService:
    """
    Determine which active promotions apply to a set of products
    at a specific supermarket chain.
    """

    @staticmethod
    def get_applicable_promotions(product_ids: list, chain_id: str) -> list:
        from django.db.models import Q

        promotions = (
            Promotion.objects.active()
            .filter(
                Q(promotion_supermarkets__supermarket_id=chain_id)
                | Q(promotion_supermarkets__isnull=True)
            )
            .filter(
                Q(promotion_products__product_id__in=product_ids)
                | Q(promotion_categories__category__products__id__in=product_ids)
                | Q(promotion_products__isnull=True, promotion_categories__isnull=True)
            )
            .distinct()
            .select_related("promotion_type")
        )

        result = []
        for promo in promotions:
            result.append(
                {
                    "id": str(promo.id),
                    "name": promo.name,
                    "type": promo.promotion_type.code,
                    "discount_percentage": (
                        float(promo.discount_percentage)
                        if promo.discount_percentage
                        else None
                    ),
                    "discount_amount": (
                        float(promo.discount_amount) if promo.discount_amount else None
                    ),
                    "end_date": promo.end_date.isoformat() if promo.end_date else None,
                }
            )
        return result


class SupermarketRecommendationService:
    """
    Given a list of products, recommend the single best branch
    based on lowest total price and available promotions.
    """

    @staticmethod
    def recommend(product_ids: list) -> dict:
        from apps.supermarkets.models import SupermarketBranch

        branch_totals: dict[str, dict] = {}

        prices = (
            ProductPrice.objects.filter(product_id__in=product_ids, is_active=True)
            .select_related("branch__chain")
        )

        for pp in prices:
            bid = str(pp.branch.id)
            if bid not in branch_totals:
                branch_totals[bid] = {
                    "branch_id": bid,
                    "branch_name": pp.branch.name,
                    "chain_id": str(pp.branch.chain.id),
                    "chain_name": pp.branch.chain.name,
                    "total": Decimal("0"),
                    "products_found": 0,
                }
            branch_totals[bid]["total"] += pp.price
            branch_totals[bid]["products_found"] += 1

        if not branch_totals:
            return {"recommendation": None, "alternatives": []}

        ranked = sorted(branch_totals.values(), key=lambda x: x["total"])
        best = ranked[0]
        best["total"] = float(best["total"])
        for alt in ranked[1:]:
            alt["total"] = float(alt["total"])

        return {
            "recommendation": best,
            "alternatives": ranked[1:5],
            "total_products_searched": len(product_ids),
        }
