from flask import Blueprint, request, jsonify
from services.solicitud_service import SolicitudService

solicitud_bp = Blueprint("solicitud_bp", __name__, url_prefix="/solicitudes")
service = SolicitudService()

# Listar todas las solicitudes
@solicitud_bp.route("/", methods=["GET"])
def listar():
    return jsonify(service.listar()), 200

# Buscar solicitud por ID
@solicitud_bp.route("/<int:id>", methods=["GET"])
def obtener_por_id(id):
    solicitud = service.buscar_por_id(id)
    if solicitud:
        return jsonify(solicitud), 200
    return jsonify({"error": "Solicitud no encontrada"}), 404

# Crear una nueva solicitud
@solicitud_bp.route("/", methods=["POST"])
def crear():
    data = request.get_json()
    if not data or "nombre" not in data or "categoria" not in data or "ubicacion" not in data:
        return jsonify({"error": "Datos inválidos"}), 400
    return jsonify(service.crear(data)), 201
    
# Aprobar una solicitud
def aprobar(id):
    resultado = service.aprobar(id)
    if "error" in resultado:
        return jsonify(resultado), 404
    return jsonify(resultado), 200

# Rechazar una solicitud
@solicitud_bp.route("/<int:id>/rechazar", methods=["PUT"])
def rechazar(id):
    resultado = service.rechazar(id)
    if "error" in resultado:
        return jsonify(resultado), 404
    return jsonify(resultado), 200

# Listar solicitudes pendientes
@solicitud_bp.route("/pendientes", methods=["GET"])
def listar_pendientes():
    return jsonify(service.pendientes()), 200

# Listar solicitudes aceptadas
@solicitud_bp.route("/aceptadas", methods=["GET"])
def listar_aceptadas():
    return jsonify(service.aceptadas()), 200

# Listar solicitudes rechazadas
@solicitud_bp.route("/rechazadas", methods=["GET"])
def listar_rechazadas():
    return jsonify(service.rechazadas()), 200

# Eliminar una solicitud
@solicitud_bp.route("/<int:id>", methods=["DELETE"])
def eliminar(id):
    resultado = service.eliminar(id)
    if "error" in resultado:
        return jsonify(resultado), 404
    return jsonify(resultado), 200
