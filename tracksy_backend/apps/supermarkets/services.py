from geopy.distance import geodesic
from .models import SupermarketBranch


class NearbyBranchService:
    """Calculate branches sorted by distance from a geographic point."""

    @staticmethod
    def get_nearby_branches(lat: float, lon: float, radius_km: float = 10):
        user_point = (lat, lon)
        branches = SupermarketBranch.objects.filter(
            is_active=True,
            latitude__isnull=False,
            longitude__isnull=False,
        ).select_related("chain")

        results = []
        for branch in branches:
            branch_point = (float(branch.latitude), float(branch.longitude))
            distance = geodesic(user_point, branch_point).km
            if distance <= radius_km:
                branch.distance_km = round(distance, 2)
                results.append(branch)

        results.sort(key=lambda b: b.distance_km)
        return results
