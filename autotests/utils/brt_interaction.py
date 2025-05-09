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


from decimal import Decimal
from datetime import timedelta


def get_brt_abonents_calls(abonent_id):
    """
    Возвращает информацию о вызовах абонента:
    - список всех вызовов
    - общее количество вызовов
    - общая продолжительность в секундах (как Decimal)
    """
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            # Получаем все вызовы абонента
            cur.execute(
                "SELECT id, stranger_msisdn, call_type, start_time, end_time, duration "
                "FROM calls WHERE abonent_id = %s ORDER BY start_time DESC",
                (abonent_id,)
            )
            calls = cur.fetchall()

            # Вычисляем общую статистику
            total_calls = len(calls)
            total_duration_seconds = Decimal('0')

            # Преобразуем продолжительность в секунды
            for call in calls:
                if isinstance(call[5], timedelta):  # Если duration как интервал
                    total_duration_seconds += Decimal(call[5].total_seconds())
                elif isinstance(call[5], str):  # Если duration как строка 'HH:MM:SS'
                    h, m, s = map(int, call[5].split(':'))
                    total_duration_seconds += Decimal(h * 3600 + m * 60 + s)

            return {
                'calls': calls,
                'total_calls': total_calls,
                'total_duration_seconds': total_duration_seconds
            }

    except Exception as e:
        print(f"Database error: {e}")
        return {
            'calls': [],
            'total_calls': 0,
            'total_duration_seconds': Decimal('0')
        }
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


def get_call_history(abonent_id):
    """Возвращает историю звонков абонента."""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT
                    id,
                    stranger_msisdn,
                    call_type,
                    start_time,
                    end_time,
                    duration
                FROM calls
                WHERE abonent_id = %s
                """,
                (abonent_id,)
            )
            columns = [desc[0] for desc in cur.description]
            rows = cur.fetchall()
            return [dict(zip(columns, row)) for row in rows]
    finally:
        conn.close()
