#!/usr/bin/env python3
"""
Script de Verificación de Bienestar y Check-ins en PostgreSQL (Railway / Local)
Uso:
  python3 scripts/check_user_status.py [--email EMAIL] [--db-url DB_URL]
"""

import sys
import os
import argparse
from datetime import datetime, timezone
import psycopg2
from psycopg2.extras import RealDictCursor

def get_connection(db_url=None):
    if not db_url:
        db_url = os.environ.get("DATABASE_URL")
    
    if not db_url:
        # Fallback a PostgreSQL de Docker local
        db_url = "postgresql://estoyok_user:estoyok_password@localhost:5433/estoyok_db"
        print(f"ℹ️ No se especificó DATABASE_URL ni --db-url. Usando Postgres local: {db_url}")
    else:
        # Ocultar password en el log por seguridad
        sanitized = db_url.split("@")[-1] if "@" in db_url else db_url
        print(f"ℹ️ Conectando a Base de Datos: ...@{sanitized}")
    
    return psycopg2.connect(db_url)

def check_user(email_query, db_url=None):
    conn = None
    try:
        conn = get_connection(db_url)
        cur = conn.cursor(cursor_factory=RealDictCursor)

        # 1. Buscar Usuario
        cur.execute("""
            SELECT id, name, email, last_check_in_at, checkin_interval_hours, 
                   wifi_checkin_enabled, safe_wifi_ssid, sensor_checkin_enabled,
                   is_premium, created_at, updated_at
            FROM users 
            WHERE email ILIKE %s OR email ILIKE %s
            ORDER BY id ASC
        """, (email_query, f"%{email_query}%"))
        
        users = cur.fetchall()

        if not users:
            print(f"❌ No se encontró ningún usuario con el email: '{email_query}'")
            return

        for user in users:
            print("=" * 70)
            print(f"👤 USUARIO: {user['name']} (ID: {user['id']}) - Email: {user['email']}")
            print("=" * 70)
            print(f" • Último Check-in Registrado (last_check_in_at): {user['last_check_in_at']}")
            print(f" • Intervalo Configurado: {user['checkin_interval_hours']} horas")
            print(f" • Wi-Fi Auto Check-in Habilitado: {user['wifi_checkin_enabled']}")
            print(f" • SSID de Wi-Fi Seguro: '{user['safe_wifi_ssid']}'")
            print(f" • Sensor de Movimiento Habilitado: {user['sensor_checkin_enabled']}")
            print(f" • Última Actualización del Usuario: {user['updated_at']}")

            # Cálculo de Vigencia
            if user['last_check_in_at']:
                last_time = user['last_check_in_at']
                if last_time.tzinfo is None:
                    last_time = last_time.replace(tzinfo=timezone.utc)
                
                now = datetime.now(timezone.utc)
                interval_hours = user['checkin_interval_hours'] or 24
                expires_at = last_time.timestamp() + (interval_hours * 3600)
                diff_seconds = expires_at - now.timestamp()

                print("-" * 70)
                print("⏱️ ESTADO DEL CONTADOR DE BIENESTAR:")
                if diff_seconds > 0:
                    hours_left = int(diff_seconds // 3600)
                    mins_left = int((diff_seconds % 3600) // 60)
                    secs_left = int(diff_seconds % 60)
                    print(f" ✅ EN VIGOR / SEGURO. Próximo reporte vence en: {hours_left}h {mins_left}m {secs_left}s")
                    print(f" 📅 Vencimiento exacto (UTC): {datetime.fromtimestamp(expires_at, timezone.utc).isoformat()}")
                else:
                    overdue_hours = int(abs(diff_seconds) // 3600)
                    overdue_mins = int((abs(diff_seconds) % 3600) // 60)
                    print(f" ⚠️ REPORTE VENCIDO hace {overdue_hours}h {overdue_mins}m")
            else:
                print(" ⚠️ SIN REPORTES PREVIOS (last_check_in_at es NULL)")

            # 2. Historial de Check-ins Recientes
            cur.execute("""
                SELECT id, source, created_at
                FROM check_ins
                WHERE user_id = %s
                ORDER BY created_at DESC
                LIMIT 10
            """, (user['id'],))
            
            checkins = cur.fetchall()
            print("-" * 70)
            print(f"📋 HISTORIAL DE CHECK-INS (Últimos {len(checkins)} registros):")
            if checkins:
                for c in checkins:
                    print(f" • ID: {c['id']:<5} | Origen: {c['source']:<10} | Fecha: {c['created_at']}")
            else:
                print(" • No hay check-ins registrados en la tabla 'check_ins'.")

            # 3. Alertas de Emergencia
            cur.execute("""
                SELECT id, type, status, created_at, audio_path
                FROM emergency_alerts
                WHERE user_id = %s
                ORDER BY created_at DESC
                LIMIT 5
            """, (user['id'],))
            alerts = cur.fetchall()
            print("-" * 70)
            print(f"🚨 ALERTAS DE EMERGENCIA (Últimas {len(alerts)}):")
            if alerts:
                for a in alerts:
                    print(f" • ID: {a['id']} | Tipo: {a['type']} | Estado: {a['status']} | Audio: {bool(a['audio_path'])} | Fecha: {a['created_at']}")
            else:
                print(" • Sin alertas registradas.")
            print("=" * 70 + "\n")

    except Exception as e:
        print(f"❌ Error al consultar la base de datos: {e}")
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Verificar estado de bienestar y check-ins en PostgreSQL")
    parser.add_argument("--email", default="nicolas", help="Email o búsqueda parcial del usuario (default: nicolas)")
    parser.add_argument("--db-url", default=None, help="String de conexión PostgreSQL (ej: postgresql://user:pass@host:port/db)")
    args = parser.parse_args()

    check_user(args.email, args.db_url)
