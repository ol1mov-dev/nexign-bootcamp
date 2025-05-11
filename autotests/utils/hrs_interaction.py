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


def delete_hrs_abonent(abonent_id):
    """Удаляет абонента и связанный баланс (если существуют)"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Получаем balance_id для удаления
            cur.execute("SELECT balance_id FROM abonents WHERE id = %s", (abonent_id,))
            balance_data = cur.fetchone()

            # Удаляем абонента
            cur.execute("DELETE FROM abonents WHERE id = %s", (abonent_id,))

            # Удаляем связанный баланс
            if balance_data:
                cur.execute("DELETE FROM balances WHERE id = %s", (balance_data[0],))

            conn.commit()
    except Exception as e:
        conn.rollback()
        raise RuntimeError(f"Ошибка удаления: {str(e)}")
    finally:
        conn.close()


def create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in_minutes=0, initial_out_minutes=0):
    """Создает нового абонента с балансом и возвращает ID абонента"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Создаем баланс
            cur.execute(
                """INSERT INTO balances (
                    amount_of_minutes_for_incoming_call,
                    amount_of_minutes_for_outcoming_call
                ) VALUES (%s, %s) RETURNING id""",
                (initial_in_minutes, initial_out_minutes)
            )
            new_balance_id = cur.fetchone()[0]

            # Создаем абонента и возвращаем его ID
            cur.execute(
                """INSERT INTO abonents (
                    id, user_id, tariff_id, balance_id, created_at, is_deleted
                ) VALUES (%s, %s, %s, %s, NOW(), FALSE)""",
                (abonent_id, user_id, tariff_id, new_balance_id)
            )

            conn.commit()
            return abonent_id  # Возвращаем ID абонента, который был явно задан

    except psycopg2.IntegrityError as e:
        conn.rollback()
        raise e  # Пробрасываем оригинальное исключение
    finally:
        conn.close()


def set_hrs_outgoing_minutes(abonent_id, minutes):
    """Обновляет исходящие минуты в HRS"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Получаем balance_id из таблицы abonents
            cur.execute(
                "SELECT balance_id FROM abonents WHERE id = %s",
                (abonent_id,)
            )
            balance_id = cur.fetchone()[0]  # Получаем настоящий ID баланса

            # Обновляем минуты в таблице balances
            cur.execute(
                "UPDATE balances SET amount_of_minutes_for_outcoming_call = %s WHERE id = %s",
                (minutes, balance_id)  # Используем balance_id вместо abonent_id
            )
            conn.commit()
    except Exception as e:
        conn.rollback()
        raise RuntimeError(f"Ошибка обновления минут: {str(e)}")
    finally:
        conn.close()


def get_hrs_outgoing_minutes(abonent_id):
    """Возвращает исходящие минуты из баланса абонента"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Получаем ID баланса из таблицы абонентов
            cur.execute(
                "SELECT balance_id FROM abonents WHERE id = %s",
                (abonent_id,)
            )
            balance_id_row = cur.fetchone()

            if not balance_id_row:
                raise ValueError(f"Абонент {abonent_id} не найден")

            balance_id = balance_id_row[0]

            # Получаем минуты из таблицы балансов
            cur.execute(
                "SELECT amount_of_minutes_for_outcoming_call FROM balances WHERE id = %s",
                (balance_id,)
            )
            result = cur.fetchone()

            if result:
                return result[0]
            else:
                raise ValueError(f"Баланс для абонента {abonent_id} не найден")
    finally:
        conn.close()


def get_hrs_incoming_minutes(abonent_id):
    """Возвращает исходящие минуты из баланса абонента"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Получаем ID баланса из таблицы абонентов
            cur.execute(
                "SELECT balance_id FROM abonents WHERE id = %s",
                (abonent_id,)
            )
            balance_id_row = cur.fetchone()

            if not balance_id_row:
                raise ValueError(f"Абонент {abonent_id} не найден")

            balance_id = balance_id_row[0]

            # Получаем минуты из таблицы балансов
            cur.execute(
                "SELECT amount_of_minutes_for_incoming_call FROM balances WHERE id = %s",
                (balance_id,)
            )
            result = cur.fetchone()

            if result:
                return result[0]
            else:
                raise ValueError(f"Баланс для абонента {abonent_id} не найден")
    finally:
        conn.close()


def get_tariff_cost_details(abonent_id):
    """Returns tariff details for the given abonent"""
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT 
                    tp.price as tariff_price,
                    l.price_per_additional_minute_outcoming,
                    l.price_per_additional_minute_incoming,
                    l.minutes_for_outcoming,
                    l.minutes_for_incoming,
                    tp.payment_period_in_days
                FROM abonents a
                JOIN tariffs t ON a.tariff_id = t.id
                JOIN tariff_parameters tp ON t.tariff_parameters_id = tp.id
                LEFT JOIN limits l ON tp.limit_id = l.id
                WHERE a.id = %s
            """, (abonent_id,))
            result = cur.fetchone()

            if not result:
                return None

            return {
                'tariff_price': float(result[0]),
                'price_per_additional_minute_outcoming': float(result[1]),
                'price_per_additional_minute_incoming': float(result[2]),
                'minutes_for_outcoming': result[3],
                'minutes_for_incoming': result[4],
                'payment_period_in_days': result[5]
            }
    finally:
        conn.close()