import pika
import psycopg2
import pytest
import time
from datetime import timedelta
from decimal import Decimal
from config import *
import json

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


def send_call_message(abonent_id, duration, call_type="01"):
    """Sends a call message to RabbitMQ in JSON format with properly formatted duration."""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.queue_declare(queue='call.queue', durable=True)

        # Convert duration to HH:MM:SS string
        if isinstance(duration, timedelta):
            total_seconds = int(duration.total_seconds())
            hours = total_seconds // 3600
            minutes = (total_seconds % 3600) // 60
            seconds = total_seconds % 60
            duration_str = f"{hours:02}:{minutes:02}:{seconds:02}"
        elif isinstance(duration, (int, float)):
            # Assuming duration is in seconds if it's a number
            total_seconds = int(duration)
            hours = total_seconds // 3600
            minutes = (total_seconds % 3600) // 60
            seconds = total_seconds % 60
            duration_str = f"{hours:02}:{minutes:02}:{seconds:02}"
        else:
            # Try to parse as HH:MM:SS string
            duration_str = str(duration)
            # Validate the format (simple check)
            if len(duration_str.split(':')) != 3:
                duration_str = f"00:00:{duration_str}"

        message = {
            "abonentId": abonent_id,
            "callType": call_type,
            "callDuration": duration_str
        }

        channel.basic_publish(
            exchange='',
            routing_key='call.queue',
            body=json.dumps(message).encode(),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )
        print(f" [x] Sent {message}")
    except pika.exceptions.UnroutableError:
        print("Message could not be delivered")
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


def send_json_message(json_data):
    """Отправляет готовый JSON в RabbitMQ"""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.queue_declare(queue='call.queue', durable=True)

        channel.basic_publish(
            exchange='',
            routing_key='call.queue',
            body=json.dumps(json_data).encode(),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )
        print(f" [x] Sent {json_data}")
    except pika.exceptions.UnroutableError:
        print("Message could not be delivered")
    finally:
        conn.close()

custom_message = {
    "abonentId": 10,
    "callType": "01",
    "callDuration": "00:25:00"
}

if __name__ == '__main__':
    send_call_message(2, 10)
    send_json_message(custom_message)