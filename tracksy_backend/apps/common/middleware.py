import logging
import time

logger = logging.getLogger("tracksy.requests")


class RequestLoggingMiddleware:
    """
    Logs every HTTP request and response: method, path, auth presence,
    status code, elapsed time, and a truncated JSON response body.
    """

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        start = time.time()
        has_auth = bool(request.headers.get("Authorization"))
        qs = request.META.get("QUERY_STRING", "")
        full_path = f"{request.path}?{qs}" if qs else request.path

        logger.info(
            "→ %s %s | auth=%s",
            request.method,
            full_path,
            "JWT" if has_auth else "none",
        )

        response = self.get_response(request)

        elapsed_ms = round((time.time() - start) * 1000)
        body_preview = ""
        if (
            hasattr(response, "content")
            and "application/json" in response.get("Content-Type", "")
        ):
            raw = response.content.decode("utf-8", errors="replace")
            body_preview = raw[:600] + ("…" if len(raw) > 600 else "")

        logger.info(
            "← %s %s | status=%d | %dms | %s",
            request.method,
            full_path,
            response.status_code,
            elapsed_ms,
            body_preview,
        )

        return response
