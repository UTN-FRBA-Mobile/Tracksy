from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from .models import Estado, Sugerencia, FeedbackSugerencia
from .serializers import EstadoSerializer, SugerenciaSerializer, FeedbackSugerenciaSerializer


class EstadoViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Estado.objects.all()
    serializer_class = EstadoSerializer
    permission_classes = [IsAuthenticated]


class SugerenciaViewSet(viewsets.ModelViewSet):
    serializer_class = SugerenciaSerializer
    permission_classes = [IsAuthenticated]
    ordering = ["-fecha"]

    def get_queryset(self):
        return Sugerencia.objects.filter(
            usuario=self.request.user
        ).select_related("producto", "estado").prefetch_related("feedbacks")

    def perform_create(self, serializer):
        serializer.save(usuario=self.request.user)

    @action(detail=True, methods=["post"], url_path="feedback")
    def agregar_feedback(self, request, pk=None):
        sugerencia = self.get_object()
        data = {**request.data, "sugerencia": sugerencia.pk}
        serializer = FeedbackSugerenciaSerializer(data=data)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)
