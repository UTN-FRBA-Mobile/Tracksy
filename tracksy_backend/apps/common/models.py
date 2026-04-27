import uuid
from django.db import models


class TimeStampedModel(models.Model):
    """Abstract base that adds created_at / updated_at to every model."""

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        abstract = True


class UUIDModel(models.Model):
    """Abstract base that replaces the integer PK with a UUID."""

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)

    class Meta:
        abstract = True


class BaseModel(UUIDModel, TimeStampedModel):
    """Canonical abstract base: UUID PK + timestamps."""

    class Meta:
        abstract = True
