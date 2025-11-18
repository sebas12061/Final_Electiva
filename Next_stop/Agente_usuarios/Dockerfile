# ================================
# Dockerfile para el agente y bridge_api
# ================================
FROM python:3.10-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo de requisitos
COPY requirements.txt .

#instala dependencias
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install pillow

# Copia el código del agente
COPY . .

# Expone el puerto para el servidor de status
EXPOSE 9001

# Comando para ejecutar el agente
CMD ["uvicorn", "bridge_api_users:app", "--host", "0.0.0.0", "--port", "9001"]