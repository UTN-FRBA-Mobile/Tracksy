from rest_framework.views import exception_handler
from rest_framework.response import Response
from rest_framework import status


def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)

    if response is not None:
        error_data = {
            "success": False,
            "error": {
                "status_code": response.status_code,
                "message": _flatten_errors(response.data),
                "detail": response.data,
            },
        }
        return Response(error_data, status=response.status_code)

    return Response(
        {
            "success": False,
            "error": {
                "status_code": status.HTTP_500_INTERNAL_SERVER_ERROR,
                "message": "Error interno del servidor.",
                "detail": str(exc),
            },
        },
        status=status.HTTP_500_INTERNAL_SERVER_ERROR,
    )


def _flatten_errors(data) -> str:
    if isinstance(data, dict):
        messages = []
        for key, value in data.items():
            if isinstance(value, list):
                messages.append(f"{key}: {' '.join(str(v) for v in value)}")
            else:
                messages.append(str(value))
        return " | ".join(messages)
    if isinstance(data, list):
        return " | ".join(str(item) for item in data)
    return str(data)
