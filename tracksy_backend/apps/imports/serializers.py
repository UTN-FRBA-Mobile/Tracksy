from rest_framework import serializers
from .models import ImportBatch, ImportError


class ImportErrorSerializer(serializers.ModelSerializer):
    class Meta:
        model = ImportError
        fields = ("id", "row_number", "field", "error_message", "raw_data")


class ImportBatchSerializer(serializers.ModelSerializer):
    errors = ImportErrorSerializer(many=True, read_only=True)
    uploaded_by_email = serializers.EmailField(source="uploaded_by.email", read_only=True)

    class Meta:
        model = ImportBatch
        fields = (
            "id",
            "import_type",
            "file",
            "status",
            "total_rows",
            "processed_rows",
            "success_rows",
            "error_rows",
            "started_at",
            "finished_at",
            "notes",
            "uploaded_by_email",
            "errors",
            "created_at",
        )
        read_only_fields = (
            "id", "status", "total_rows", "processed_rows",
            "success_rows", "error_rows", "started_at", "finished_at",
            "notes", "uploaded_by_email", "errors", "created_at",
        )

    def create(self, validated_data):
        validated_data["uploaded_by"] = self.context["request"].user
        return super().create(validated_data)
