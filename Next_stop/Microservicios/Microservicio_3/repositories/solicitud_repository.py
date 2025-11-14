from database import get_connection
from models.solicitud import Solicitud

class SolicitudRepository:
    
    # Listar todas las solicitudes
    def obtener_todas(self):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM solicitudes")
        rows = cursor.fetchall()
        conn.close()
        return [Solicitud(**row).to_dict() for row in rows]

    # Obtener solicitud por ID
    def obtener_por_id(self, id):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM solicitudes WHERE id = %s", (id,))
        row = cursor.fetchone()
        conn.close()
        return Solicitud(**row).to_dict() if row else None

    # Crear una nueva solicitud
    def crear(self, data):
        conn = get_connection()
        cursor = conn.cursor()
        query = "INSERT INTO solicitudes (nombre, categoria, ubicacion, estado) VALUES (%s, %s, %s, %s)"
        cursor.execute(query, (data["nombre"], data["categoria"], data["ubicacion"], "pendiente"))
        conn.commit()
        conn.close()
        return {"mensaje": "Solicitud creada con éxito"}
    
    # Actualizar estado de una solicitud
    def actualizar_estado(self, id, nuevo_estado):
        conn = get_connection()
        cursor = conn.cursor()
        query = "UPDATE solicitudes SET estado = %s WHERE id = %s"
        cursor.execute(query, (nuevo_estado, id))
        conn.commit()
        conn.close()
        return {"mensaje": f"Solicitud {nuevo_estado}"}
    
    # Obtener solicitudes por estado
    def obtener_por_estado(self, estado):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM solicitudes WHERE estado = %s", (estado,))
        rows = cursor.fetchall()
        conn.close()
        return [Solicitud(**row).to_dict() for row in rows]
    
    # Eliminar una solicitud
    def eliminar(self, id):
        conn = get_connection()
        cursor = conn.cursor()
        query = "DELETE FROM solicitudes WHERE id = %s"
        cursor.execute(query, (id,))
        conn.commit()
        conn.close()
        return {"mensaje": "Solicitud eliminada"}
