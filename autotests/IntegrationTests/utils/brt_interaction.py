import psycopg2
from decimal import Decimal
from config import *

def get_brt_db_connection():
    """Establishes a connection to the BRT database."""
    try:
        return psycopg2.connect(
            host=BRT_DB_HOST,
            port=BRT_DB_PORT,
            dbname=BRT_DB_NAME,
            user=BRT_DB_USER,
            password=BRT_DB_PASS
        )
    except psycopg2.OperationalError as op_err:
        print(f"Operational error connecting to BRT database: {op_err}")
        raise



def set_brt_balance(abonent_id, balance):
    """Устанавливает баланс с явным преобразованием в Decimal"""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE abonents SET balance = %s WHERE id = %s",
                (Decimal(str(balance)), abonent_id)  # Явное преобразование
            )
            conn.commit()
    finally:
        conn.close()


def get_brt_balance(abonent_id):
    """Возвращает баланс как float для сравнения"""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT balance FROM abonents WHERE id = %s", (abonent_id,))
            return float(cur.fetchone()[0])  # Конвертация в float
    finally:
        conn.close()
