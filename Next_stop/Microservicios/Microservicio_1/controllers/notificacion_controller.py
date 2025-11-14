from flask import Blueprint, request, jsonify
from services.notificacion_service import NotificacionService

notificacion_bp = Blueprint("notificaciones", __name__)
service = NotificacionService()

# Crear notificación
@notificacion_bp.route("/notificaciones", methods=["POST"])
def crear_notificacion():
    data = request.get_json()
    nueva = service.enviar_notificacion(data)
    if nueva:
        return jsonify(nueva), 201
    return jsonify({"error": "El mensaje no puede estar vacío"}), 400

# Listar notificaciones
@notificacion_bp.route("/notificaciones", methods=["GET"])
def listar_notificaciones():
    return jsonify(service.obtener_notificaciones()), 200

# Actualizar notificaciones como leídas
@notificacion_bp.route("/notificaciones/<int:notificacion_id>/leer", methods=["PUT"])
def notificacion_leida(notificacion_id):
    actualizada = service.notificacion_leida(notificacion_id)
    if actualizada:
        return jsonify(actualizada), 200
    return jsonify({"error": "Notificación no encontrada"}), 404

# Eliminar notificaciones
@notificacion_bp.route("/notificaciones/<int:notificacion_id>", methods=["DELETE"])
def eliminar_notificacion(notificacion_id):
    eliminada = service.eliminar_notificacion(notificacion_id)
    if eliminada:
        return jsonify({"mensaje": "Notificación eliminada"}), 200
    return jsonify({"error": "Notificación no encontrada"}), 404

# Filtrar notificaciones por estado
@notificacion_bp.route("/notificaciones/estado/<string:estado>", methods=["GET"])
def obtener_notificaciones_por_estado(estado):
    notificaciones = service.obtener_notificaciones_por_estado(estado)
    return jsonify(notificaciones), 200

# Obtener notificación por ID
@notificacion_bp.route("/notificaciones/<int:notificacion_id>", methods=["GET"])
def obtener_notificacion_por_id(notificacion_id):
    notificacion = service.obtener_notificaciones_por_id(notificacion_id)
    if notificacion:
        return jsonify(notificacion), 200
    return jsonify({"error": "Notificación no encontrada"}), 404
from models.notificacion import Notificacion