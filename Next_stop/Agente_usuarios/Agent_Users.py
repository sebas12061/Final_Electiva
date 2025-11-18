import asyncio
import json
import requests
import re  # Para extracción de JSON
import os
import queue
import threading
import time
from mirascope.core.gemini import gemini_call
import google.generativeai as genai

# Configurar API KEY de Gemini
genai.configure(api_key=os.getenv("GEMINI_API_KEY", "AIzaSyBnkvmMzgrxBBU5MojGCvaKYEiEJDRuljA"))

# ================= BASE URLs =================
BASE_URL_NOTIFICACIONES = "http://notificaciones:5005/notificaciones"
BASE_URL_LUGARES = "http://lugares:8082/api/places"

# ================= PROMPT =================
PLANTILLA_PROMPT = """
Eres un asistente limitado para usuarios. Solo puedes:
- Ver lugares aceptados.
- Ver notificaciones.

FORMATO JSON OBLIGATORIO:

EJEMPLOS CONCRETOS (copia la estructura exacta):

1. LISTAR lugares aceptados:
{{"modulo": "lugares", "accion": "aceptada"}}

2. LISTAR notificaciones:
{{"modulo": "notificaciones", "accion": "listar"}}

3. OBTENER detalles de una notificación:
{{"modulo": "notificaciones", "accion": "obtener", "id": 2}}

REGLAS:
1. SIEMPRE devuelve JSON válido con estructura {{"modulo": "...", "accion": "...", ...}}
2. Solo módulos permitidos: lugares (aceptadas) y notificaciones.
3. No puedes crear, aprobar o rechazar nada.

# NOTIFICACIONES (mensaje*, tipo default=general, estado default=pendiente)
{{ "modulo": "notificaciones", "accion": "listar" }}
{{ "modulo": "notificaciones", "accion": "obtener", "id": 2 }}

# LUGARES (solo aceptados)
{{ "modulo": "lugares", "accion": "aceptada" }}

Instrucción: {instruccion}
"""

# Gemini
@gemini_call(model="models/gemini-2.5-flash")
def interpretar_con_gemini(texto: str) -> str:
    return PLANTILLA_PROMPT.format(instruccion=texto)

# ================= SSE / STATUS =================
_status_queue = queue.Queue()
_last_status = {}
_status_lock = threading.Lock()

def publish_status(payload: dict):
    payload.setdefault("timestamp", time.time())
    with _status_lock:
        global _last_status
        _last_status = payload
    try:
        _status_queue.put(payload, block=False)
    except Exception:
        pass
    print(f"STATUS PUBLISHED: {json.dumps(payload, ensure_ascii=False)}")

def _start_status_server(port=5011):  # Puerto diferente: 5011
    try:
        from flask import Flask, Response, stream_with_context, jsonify
    except Exception as e:
        publish_status({"step": "server_skipped", "message": f"Flask no disponible: {e}"})
        return

    app = Flask(__name__)

    def _sse_gen():
        try:
            with _status_lock:
                if _last_status:
                    yield f"data: {json.dumps(_last_status, ensure_ascii=False)}\n\n"
        except Exception:
            pass
        while True:
            msg = _status_queue.get()
            yield f"data: {json.dumps(msg, ensure_ascii=False)}\n\n"

    @app.route('/events')
    def sse_events():
        return Response(stream_with_context(_sse_gen()), mimetype='text/event-stream')

    @app.route('/status')
    def get_status():
        with _status_lock:
            return jsonify(_last_status or {"message": "no status yet"})

    def run():
        app.run(host='0.0.0.0', port=port, debug=False, use_reloader=False)

    t = threading.Thread(target=run, daemon=True)
    t.start()
    publish_status({"step": "server_started", "message": f"status server on port {port}"})

# ================= FUNCIÓN PARA EXTRAER JSON =================
def extraer_json(texto: str) -> str:
    match = re.search(r'```json\s*(.*?)\s*```', texto, re.DOTALL | re.IGNORECASE)
    if match:
        return match.group(1).strip()
    
    match = re.search(r'```\s*(.*?)\s*```', texto, re.DOTALL)
    if match:
        return match.group(1).strip()
    
    return texto.strip()

# ================= VALIDACIÓN / FALLBACK =================
def intentar_parsear_accion(crudo: str):
    crudo_strip = crudo.strip()
    simple_token = re.sub(r"[\s`'\"]+", "", crudo_strip.lower())

    if simple_token == "accion" or re.fullmatch(r'[\"\']?accion[\"\']?', crudo_strip, re.IGNORECASE):
        return {"error": "Respuesta incompleta del modelo: falta modulo y datos. Reformula tu instrucción con un verbo y el nombre del lugar."}

    try:
        return json.loads(crudo_strip)
    except Exception:
        pass

    modulo_match = re.search(r'modulo\s*[:=]\s*"?([a-zA-Z_]+)"?', crudo_strip)
    accion_match = re.search(r'accion\s*[:=]\s*"?([a-zA-Z_]+)"?', crudo_strip)
    id_match = re.search(r'id\s*[:=]\s*"?([0-9A-Za-z\-]+)"?', crudo_strip)

    if modulo_match and accion_match:
        accion_dict = {
            "modulo": modulo_match.group(1),
            "accion": accion_match.group(1)
        }
        if id_match:
            accion_dict["id"] = id_match.group(1)
        return accion_dict

    bloque = re.search(r'(\{.*\})', crudo_strip, re.DOTALL)
    if bloque:
        try:
            return json.loads(bloque.group(1))
        except Exception:
            return {"error": "Bloque JSON detectado pero inválido. Reformula tu instrucción de manera más clara."}

    return {"error": "No se pudo interpretar la respuesta del modelo. Por favor reformula: ej. 'muéstrame lugares aceptados'."}

# ================= EJECUTAR ACCIÓN =================
def ejecutar_accion(accion: dict):
    publish_status({"step": "ejecutar_accion_start", "accion": accion})
    print(f"DEBUG: Iniciando ejecución de acción: {accion}")
    
    try:
        modulo = accion.get("modulo")
        accion_tipo = accion.get("accion")
        print(f"DEBUG: Módulo: {modulo}, Acción: {accion_tipo}")
        publish_status({"step": "modulo_accion_detected", "modulo": modulo, "accion": accion_tipo})

        # ----- NOTIFICACIONES -----
        if modulo == "notificaciones":
            url_base = BASE_URL_NOTIFICACIONES

            if accion_tipo == "listar":
                url = url_base
                publish_status({"step": "http_get", "url": url})
                response = requests.get(url, timeout=5)
                response.raise_for_status()
                publish_status({"step": "http_ok", "url": url})
                return response.json()
            
            if accion_tipo == "obtener":
                id_val = accion.get('id')
                if not id_val:
                    return {"error": "ID requerido para 'obtener'"}
                url = f"{url_base}/{id_val}"
                publish_status({"step": "http_get", "url": url})
                response = requests.get(url, timeout=5)
                response.raise_for_status()
                publish_status({"step": "http_ok", "url": url})
                return response.json()

        # ----- LUGARES -----
        if modulo == "lugares":
            url_base = BASE_URL_LUGARES

            if accion_tipo == "aceptada":
                url = f"{url_base}/aceptada"
                publish_status({"step": "http_get", "url": url})
                response = requests.get(url, timeout=5)
                response.raise_for_status()
                publish_status({"step": "http_ok", "url": url})
                return response.json()

        return {"error": f"Acción no reconocida o no permitida: {accion_tipo} en {modulo}"}

    except requests.exceptions.RequestException as e:
        error_msg = f"Error en la petición HTTP: {str(e)} (verifica si el servidor está corriendo)"
        print(f"DEBUG: {error_msg}")
        publish_status({"step": "error", "message": error_msg})
        return {"error": error_msg}
    except json.JSONDecodeError as e:
        error_msg = f"Error al decodificar JSON de la respuesta: {str(e)}"
        print(f"DEBUG: {error_msg}")
        publish_status({"step": "error", "message": error_msg})
        return {"error": error_msg}
    except Exception as e:
        error_msg = f"Error inesperado: {str(e)}"
        print(f"DEBUG: {error_msg}")
        publish_status({"step": "error", "message": error_msg})
        return {"error": error_msg}

# ================= MAIN LOOP =================
async def main():
    _start_status_server(port=5011)

    publish_status({"step": "agent_started", "message": "Agente de usuarios listo. Solo puedes ver lugares aceptados y notificaciones."})
    print("Agente de usuarios listo. Solo puedes ver lugares aceptados y notificaciones.")
    print("Ejemplos:")
    print("- 'Muéstrame lugares aceptados'")
    print("- 'Dame la lista de notificaciones'")
    print("- 'Obtén notificación con ID 2'")
    print("\nIMPORTANTE: Funcionalidades limitadas para usuarios.\n")

    while True:
        instruccion = input("> ")
        if instruccion.lower() in ["salir", "exit", "quit"]:
            publish_status({"step": "agent_exit", "message": "Hasta luego"})
            print("Hasta luego")
            break

        try:
            publish_status({"step": "received_instruction", "instruccion": instruccion})
            print(f"DEBUG: Procesando instrucción: '{instruccion}'")
            respuesta = interpretar_con_gemini(instruccion)
            interpretado_crudo = getattr(respuesta, "content", respuesta)
            publish_status({"step": "gemini_called", "raw": str(interpretado_crudo)[:1000]})

            interpretado = extraer_json(str(interpretado_crudo))
            print("Salida cruda de Gemini:")
            print(interpretado_crudo)
            print("JSON extraído:")
            print(interpretado)
            publish_status({"step": "gemini_parsed", "parsed": interpretado})

            accion = intentar_parsear_accion(interpretado)
            if isinstance(accion, dict) and 'error' in accion and len(accion) == 1:
                print(json.dumps(accion, ensure_ascii=False))
                publish_status({"step": "parse_error", "error": accion["error"]})
                continue
            if not isinstance(accion, dict) or 'modulo' not in accion or 'accion' not in accion:
                err = {"error": "Estructura inválida. Debe incluir 'modulo' y 'accion'."}
                print(json.dumps(err, ensure_ascii=False))
                publish_status({"step": "invalid_structure", "error": err})
                continue
            resultado = ejecutar_accion(accion)
            publish_status({"step": "accion_completed", "accion": accion, "resultado": resultado})
            print("Resultado:")
            print(json.dumps(resultado, indent=2, ensure_ascii=False))
        except json.JSONDecodeError as e:
            msg = f"Error al interpretar la respuesta de Gemini como JSON: {str(e)}"
            print(msg)
            print("Respuesta cruda de Gemini:")
            print(interpretado_crudo)
            publish_status({"step": "error_json_decode", "message": msg, "raw": str(interpretado_crudo)[:1000]})
        except Exception as e:
            msg = f"Error inesperado: {str(e)}"
            print(msg)
            publish_status({"step": "error_unexpected", "message": msg})

if __name__ == "__main__":
    asyncio.run(main())