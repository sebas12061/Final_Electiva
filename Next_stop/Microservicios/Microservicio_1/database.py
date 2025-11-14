import mysql.connector
import os

def get_connection():
    conn = mysql.connector.connect(
        host=os.environ.get('DB_HOST', 'localhost'),
        port=int(os.environ.get('DB_PORT', 3306)),
        user=os.environ.get('DB_USER', 'root'),
        password=os.environ.get('DB_PASSWORD', '4787'),
        database=os.environ.get('DB_NAME', 'notificaciones_db')
    )
    return conn
