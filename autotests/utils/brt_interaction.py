import psycopg2
from config import *
from decimal import Decimal
from datetime import datetime, timedelta, time


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


def create_brt_abonent(abonent_id, first_name, name, msisdn, middle_name=None, balance=0.0):
    """Создает нового абонента в BRT системе"""
    if not all(isinstance(arg, (int, str)) for arg in [abonent_id, first_name, name, msisdn]):
        raise ValueError("Некорректные типы данных аргументов")

    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """INSERT INTO abonents (
                    id, first_name, name, middle_name, msisdn, balance
                ) VALUES (%s, %s, %s, %s, %s, %s) RETURNING id""",
                (
                    abonent_id,
                    first_name.strip(),
                    name.strip(),
                    middle_name.strip() if middle_name else None,
                    msisdn.strip(),
                    Decimal(str(balance))
                )
            )
            new_id = cur.fetchone()[0]
            conn.commit()
            return new_id

    except psycopg2.IntegrityError as e:
        conn.rollback()
        raise RuntimeError(f"Ошибка создания абонента: {str(e)}")
    finally:
        conn.close()


def delete_brt_abonent(abonent_id):
    """Удаляет абонента из BRT системы со всеми связанными вызовами"""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            # Удаляем связанные вызовы
            cur.execute("DELETE FROM calls WHERE abonent_id = %s", (abonent_id,))
            # Удаляем абонента
            cur.execute("DELETE FROM abonents WHERE id = %s", (abonent_id,))
            conn.commit()
    except Exception as e:
        conn.rollback()
        raise RuntimeError(f"Ошибка удаления: {str(e)}")
    finally:
        conn.close()


def set_brt_balance(abonent_id, balance):
    """Устанавливает баланс с явным преобразованием в Decimal"""
    if not isinstance(abonent_id, int):
        raise ValueError("abonent_id must be an integer")
    try:
        balance = float(balance)  # Проверяем, что balance можно преобразовать в число
    except (TypeError, ValueError):
        raise ValueError("balance must be a valid number")
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE abonents SET balance = %s WHERE id = %s",
                (Decimal(str(balance)), abonent_id)
            )
        conn.commit()
    except psycopg2.Error as e:
        conn.rollback()
        raise


def get_brt_balance(abonent_id):
    """Возвращает баланс как float для сравнения"""
    if not isinstance(abonent_id, int):
        raise ValueError("abonent_id must be an integer")
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT balance FROM abonents WHERE id = %s", (abonent_id,))
            result = cur.fetchone()
            return float(result[0]) if result else None  # Возвращаем None, если абонент не найден
    except psycopg2.Error as e:
        raise


def get_call_history(abonent_id):
    """Возвращает историю звонков с преобразованием duration"""
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
                ORDER BY start_time DESC
                """,
                (abonent_id,))
            columns = [desc[0] for desc in cur.description]
            calls = []
            for row in cur.fetchall():
                call = dict(zip(columns, row))
                # Преобразуем duration в timedelta
                if isinstance(call['duration'], time):  # Используем импортированный time
                    delta = timedelta(
                        hours=call['duration'].hour,
                        minutes=call['duration'].minute,
                        seconds=call['duration'].second
                    )
                    call['duration'] = delta
                calls.append(call)
            return calls
    except psycopg2.Error as e:
        raise


def create_call(abonent_id: int, stranger_msisdn: str, call_type: str, start_time: datetime, end_time: datetime, duration: timedelta):
    """Создает запись о звонке в BRT системе"""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """INSERT INTO calls (
                    abonent_id, 
                    stranger_msisdn, 
                    call_type, 
                    start_time, 
                    end_time, 
                    duration
                ) VALUES (%s, %s, %s, %s, %s, %s)""",
                (
                    abonent_id,
                    stranger_msisdn,
                    call_type,
                    start_time,
                    end_time,
                    duration
                )
            )
            conn.commit()
    except psycopg2.Error as e:
        conn.rollback()
        raise RuntimeError(f"Ошибка создания звонка: {str(e)}")
    finally:
        conn.close()