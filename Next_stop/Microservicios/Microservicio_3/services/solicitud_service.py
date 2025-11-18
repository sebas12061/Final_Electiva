import requests
from repositories.solicitud_repository import SolicitudRepository
import os
import requests

LUGARES_BASE_URL = "http://lugares:8082/api/places"


class SolicitudService:
    def __init__(self):
        self.repo = SolicitudRepository()
    
    # Listar todas las solicitudes
    def listar(self):
        return self.repo.obtener_todas()

    # Buscar por ID
    def buscar_por_id(self, id):
        return self.repo.obtener_por_id(id)

    # Crear una nueva solicitud
    def crear(self, data):
        return self.repo.crear(data)

    # Aprobar una solicitud
    def aprobar(self, id):
        solicitud = self.repo.obtener_por_id(id)
        
        if not solicitud:
            return {"error": "Solicitud no encontrada"}
        
        if solicitud.get("estado") == "aceptada":
            return {"mensaje": "Solicitud ya aceptada"}
        
        # Preparar datos para crear lugar en microservicio de lugares
        payload = {
            "name": solicitud.get("nombre"),
            "description": f"Categoría: {solicitud.get('categoria')} - Ubicación: {solicitud.get('ubicacion')}",
            "status": "pendiente"  # Cambiado a español
        }
    
        try:
            # Paso 1: Crear el lugar con estado pendiente
            resp = requests.post(LUGARES_BASE_URL, json=payload, timeout=10)
            
            if resp.status_code not in (200, 201):
                return {"error": "Error creando lugar en microservicio de lugares", "detalle": resp.text}
        
            lugar_creado = resp.json()
            lugar_id = lugar_creado.get("id")
        
            # Paso 2: Aceptar automáticamente el lugar (cambiar a aceptada)
            accept_resp = requests.post(f"{LUGARES_BASE_URL}/{lugar_id}/accept", timeout=10)  # Este endpoint cambia a aceptada
            
            if accept_resp.status_code != 200:
                requests.delete(f"{LUGARES_BASE_URL}/{lugar_id}", timeout=5)  # Rollback
                return {"error": "Error aceptando el lugar", "detalle": accept_resp.text}
        
            # Paso 3: Actualizar estado de la solicitud
            self.repo.actualizar_estado(id, "aceptada")
            return {"mensaje": "Solicitud aceptada y lugar disponible para usuarios", "lugar_id": lugar_id}
        except Exception as e:
            return {"error": "Fallo de conexión con microservicio de lugares", "detalle": str(e)}

    # Rechazar una solicitud
    def rechazar(self, id):
        return self.repo.actualizar_estado(id, "rechazada")
    
    # Listar solicitudes pendientes
    def pendientes(self):
        return self.repo.obtener_por_estado("pendiente")
    
    # Listar solicitudes aprobadas
    def aceptadas(self):
        return self.repo.obtener_por_estado("aceptada")

    # Listar solicitudes rechazadas
    def rechazadas(self):
        return self.repo.obtener_por_estado("rechazada")

    # Eliminar una solicitud
    def eliminar(self, id):
        return self.repo.eliminar(id)
