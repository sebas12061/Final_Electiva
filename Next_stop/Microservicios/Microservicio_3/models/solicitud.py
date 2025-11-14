class Solicitud:
    def __init__(self, id, nombre, categoria, ubicacion, estado="pendiente"):
        self.id = id
        self.nombre = nombre
        self.categoria = categoria
        self.ubicacion = ubicacion
        self.estado = estado

    def to_dict(self):
        return {
            "id": self.id,
            "nombre": self.nombre,
            "categoria": self.categoria,
            "ubicacion": self.ubicacion,
            "estado": self.estado
        }
