from fastapi import FastAPI
from pydantic import BaseModel
import json
import requests
import uvicorn

# Importar funciones del agente
from Agent_Users import interpretar_con_gemini, extraer_json, ejecutar_accion

app = FastAPI()

# ================= CORS =================
from fastapi.middleware.cors import CORSMiddleware

origins = [
    "http://localhost:63342",
    "http://127.0.0.1:63342",
    "http://localhost:5500",
    "http://127.0.0.1:5500"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ================= MODELOS =================
class UserMessage(BaseModel):
    message: str

# ================= ENDPOINT /chat =================
@app.post("/chat")
async def chat(msg: UserMessage):
    try:
        print(f"\n📩 MENSAJE RECIBIDO: {msg.message}")

        # Interpretar instrucción con Gemini
        respuesta = interpretar_con_gemini(msg.message)
        interpretado_crudo = respuesta.content
        interpretado_json = extraer_json(interpretado_crudo)
        accion = json.loads(interpretado_json)

        print(f"🧠 Interpretado: {accion}")

        # Ejecutar acción real
        resultado = ejecutar_accion(accion)
        print(f"✅ Resultado real: {resultado}")

        # Detectar si el resultado es lista (para render cards)
        if isinstance(resultado, list):
            return {
                "type": "cards",
                "modulo": accion.get("modulo", "general"),
                "data": resultado
            }

        # Si solo es texto o mensaje simple
        if isinstance(resultado, dict) and not "error" in resultado:
            return {
                "type": "cards",
                "modulo": accion.get("modulo", "general"),
                "data": resultado  # Para objetos individuales
            }

        # Si no es lista ni error, es texto o dict simple
        return {
            "type": "text",
            "data": json.dumps(resultado, ensure_ascii=False, indent=2)
        }

    except Exception as e:
        print(f"⚠️ Error en /chat: {e}")
        return {"type": "error", "data": str(e)}

# ================= ENDPOINT ESTADO =================
@app.get("/estado")
async def estado():
    return {"status": "online", "precision": 92, "lugares": 48, "conocimiento": 95}

# ================= RUN =================
if __name__ == "__main__":
    uvicorn.run("bridge_api_users:app", host="0.0.0.0", port=9001, reload=True)
