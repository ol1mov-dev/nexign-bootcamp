import pika
import psycopg2
import pytest
import time
from datetime import timedelta
from decimal import Decimal

# RabbitMQ connection parameters
RABBITMQ_HOST = 'localhost'
RABBITMQ_PORT = 5672
RABBITMQ_USER = 'admin'
RABBITMQ_PASS = 'password'
RABBITMQ_VHOST = '/'

# BRT database connection parameters
BRT_DB_HOST = 'localhost'
BRT_DB_PORT = 5433
BRT_DB_NAME = 'brtdb'
BRT_DB_USER = 'postgres'
BRT_DB_PASS = 'postgres'

# HRS database connection parameters
HRS_DB_HOST = 'localhost'
HRS_DB_PORT = 5434
HRS_DB_NAME = 'hrsdb'
HRS_DB_USER = 'postgres'
HRS_DB_PASS = 'postgres'


def get_rabbitmq_connection():
    """Establishes a connection to RabbitMQ with explicit credentials."""
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
        parameters = pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            virtual_host=RABBITMQ_VHOST,
            credentials=credentials
        )
        return pika.BlockingConnection(parameters)
    except pika.exceptions.ProbableAuthenticationError as auth_err:
        print(f"Authentication error: {auth_err}")
        raise
    except pika.exceptions.AMQPConnectionError as conn_err:
        print(f"Connection error: {conn_err}")
        raise


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


def send_call_message(abonent_id, duration):
    """Sends a call message to RabbitMQ."""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.queue_declare(queue='call_queue', durable=True)

        # Convert timedelta to HH:MM:SS string
        if isinstance(duration, timedelta):
            hours, remainder = divmod(duration.seconds, 3600)
            minutes, seconds = divmod(remainder, 60)
            duration_str = f"{hours:02}:{minutes:02}:{seconds:02}"
        else:
            duration_str = str(duration)

        message = f"{abonent_id},{duration_str}"
        channel.basic_publish(
            exchange='',
            routing_key='call_queue',
            body=message.encode(),
            properties=pika.BasicProperties(delivery_mode=2)
        )
    finally:
        conn.close()


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


def get_brt_balance(abonent_id):
    """Возвращает баланс как float для сравнения"""
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT balance FROM abonents WHERE id = %s", (abonent_id,))
            return float(cur.fetchone()[0])  # Конвертация в float
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


def send_call_message(abonent_id, duration):
    """Добавлено подтверждение доставки"""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.confirm_delivery()  # Включить подтверждения
        channel.queue_declare(queue='call_queue', durable=True)

        if isinstance(duration, timedelta):
            duration_str = str(duration).split('.')[0]  # Убрать микросекунды
        else:
            duration_str = duration

        message = f"{abonent_id},{duration_str}"
        channel.basic_publish(
            exchange='',
            routing_key='call_queue',
            body=message.encode(),
            properties=pika.BasicProperties(delivery_mode=2),
            mandatory=True
        )
        print(f" [x] Sent {message}")
    except pika.exceptions.UnroutableError:
        print("Message could not be delivered")
    finally:
        conn.close()


@pytest.fixture(autouse=True)
def cleanup():
    """Очистка с явным указанием ID абонентов"""
    yield
    with get_brt_db_connection() as conn:
        conn.cursor().execute("UPDATE abonents SET balance = 0 WHERE id IN (1,2)")
        conn.commit()
    with get_hrs_db_connection() as conn:
        conn.cursor().execute("UPDATE balances SET amount_of_minutes_for_outcoming_call = 50 WHERE id = 2")
        conn.commit()


def test_classic_tariff():
    """Классический тариф с увеличенным таймаутом"""
    abonent_id = 1
    initial_balance = 500.0
    set_brt_balance(abonent_id, initial_balance)
    send_call_message(abonent_id, timedelta(minutes=1))

    time.sleep(3)  # Увеличенное время ожидания

    assert get_brt_balance(abonent_id) == pytest.approx(initial_balance - 1.5, abs=0.01)


def test_monthly_tariff_within_limit():
    """Проверка обновления минут с привязкой к конкретному балансу"""
    abonent_id = 2
    set_hrs_outgoing_minutes(abonent_id, 50)
    send_call_message(abonent_id, timedelta(minutes=1))

    time.sleep(3)

    assert get_hrs_outgoing_minutes(abonent_id) == 51, "Минуты должны увеличиться на 1"


def test_monthly_tariff_over_limit():
    """Проверка списания при превышении лимита"""
    abonent_id = 2
    initial_balance = 300.0
    set_brt_balance(abonent_id, initial_balance)
    send_call_message(abonent_id, timedelta(minutes=51))

    time.sleep(3)

    assert get_hrs_outgoing_minutes(abonent_id) == 50, "Лимит не должен превышаться"
    assert get_brt_balance(abonent_id) == pytest.approx(initial_balance - 1.5, abs=0.01)
