from database import get_connection
from models.notificacion import Notificacion

class NotificacionRepository:

    def guardar_notificacion(self, mensaje, tipo="general"):
        conn = get_connection()
        cursor = conn.cursor()
        query = "INSERT INTO notificaciones (mensaje, tipo, estado) VALUES (%s, %s, %s)"
        cursor.execute(query, (mensaje, tipo, "pendiente"))
        conn.commit()
        new_id = cursor.lastrowid
        conn.close()
        return Notificacion(new_id, mensaje, tipo, "pendiente")

    def listar_notificaciones(self):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM notificaciones")
        rows = cursor.fetchall()
        conn.close()
        return [Notificacion(**row).to_dict() for row in rows]

    def listar_por_estado(self, estado):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM notificaciones WHERE estado = %s", (estado,))
        rows = cursor.fetchall()
        conn.close()
        return [Notificacion(**row).to_dict() for row in rows]

    def obtener_notificacion_por_id(self, notificacion_id):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM notificaciones WHERE id = %s", (notificacion_id,))
        row = cursor.fetchone()
        conn.close()
        return Notificacion(**row) if row else None

    def actualizar_estado(self, notificacion_id, nuevo_estado):
        conn = get_connection()
        cursor = conn.cursor()
        query = "UPDATE notificaciones SET estado = %s WHERE id = %s"
        cursor.execute(query, (nuevo_estado, notificacion_id))
        conn.commit()
        conn.close()
        return {"mensaje": f"Notificación {nuevo_estado}"}

    def eliminar_notificacion(self, notificacion_id):
        conn = get_connection()
        cursor = conn.cursor()
        query = "DELETE FROM notificaciones WHERE id = %s"
        cursor.execute(query, (notificacion_id,))
        conn.commit()
        conn.close()
        return True
