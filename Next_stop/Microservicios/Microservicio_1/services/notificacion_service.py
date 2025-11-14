from repositories.notificacion_repository import NotificacionRepository

class NotificacionService:
    def __init__(self):
        self.repo = NotificacionRepository()

    def enviar_notificacion(self, data):
        mensaje = data.get("mensaje")
        tipo = data.get("tipo", "general")

        if not mensaje or mensaje.strip() == "":
            return None

        return self.repo.guardar_notificacion(mensaje, tipo).to_dict()

    # Listar todas las notificaciones
    def obtener_notificaciones(self):
        return self.repo.listar_notificaciones()

    # Marcar notificación como leída
    def notificacion_leida(self, notificacion_id):
        return self.repo.actualizar_estado(notificacion_id, "leida")

    # Eliminar notificación
    def eliminar_notificacion(self, notificacion_id):
        return self.repo.eliminar_notificacion(notificacion_id)

    # Filtrar notificaciones por estado
    def obtener_notificaciones_por_estado(self, estado):
        return self.repo.listar_por_estado(estado)

    # Obtener notificación por ID
    def obtener_notificaciones_por_id(self, notificacion_id):
        notificacion = self.repo.obtener_notificacion_por_id(notificacion_id)
        return notificacion.to_dict() if notificacion else None
