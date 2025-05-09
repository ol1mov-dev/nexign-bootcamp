import psycopg2
from config import *

def get_hrs_db_connection():
    """Establishes a connection to the HRS database."""
    try:
        return psycopg2.connect(
            host=HRS_DB_HOST,
            port=HRS_DB_PORT,
            dbname=HRS_DB_NAME,
            user=HRS_DB_USER,
            password=HRS_DB_PASS
        )
    except psycopg2.OperationalError as op_err:
        print(f"Operational error connecting to HRS database: {op_err}")
        raise

def set_hrs_outgoing_minutes(abonent_id, minutes):
    """Обновляет исходящие минуты в HRS"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE balances SET amount_of_minutes_for_outcoming_call = %s WHERE id = %s",
                (minutes, abonent_id)
            )
            conn.commit()
    finally:
        conn.close()



def get_hrs_outgoing_minutes(abonent_id):
    """Возвращает минуты как целое число"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT amount_of_minutes_for_outcoming_call FROM balances WHERE id = %s",
                (abonent_id,)
            )
            return cur.fetchone()[0]
    finally:
        conn.close()
