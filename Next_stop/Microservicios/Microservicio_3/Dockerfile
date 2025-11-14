# ========== Microservicio: Solicitudes ==========
FROM python:3.10-slim

# Directorio de trabajo
WORKDIR /app

# Copiar dependencias
COPY requirements.txt .

# Instalar dependencias
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código fuente
COPY . .

# Exponer puerto
EXPOSE 5007

# Comando de ejecución
CMD ["python", "app.py"]
