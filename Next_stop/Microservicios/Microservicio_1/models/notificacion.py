class Notificacion:
    def __init__(self, id, mensaje, tipo="general", estado="pendiente"):
        self.id = id
        self.mensaje = mensaje
        self.tipo = tipo
        self.estado = estado

    def to_dict(self):
        return {
            "id": self.id,
            "mensaje": self.mensaje,
            "tipo": self.tipo,
            "estado": self.estado
        }
