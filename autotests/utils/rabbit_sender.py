import pika
from datetime import timedelta
from config import *
import json
import os

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

        print(f" [x] Sent {message}")

        channel.basic_publish(
            exchange='',
            routing_key='call.queue',
            body=json.dumps(message).encode(),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )
    except pika.exceptions.UnroutableError:
        print("Message could not be delivered")
    finally:
        conn.close()

def send_call_json(json_data):
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

def send_cdr_message(cdr_data):
    """Отправляет CDR в очередь cdrs.queue"""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.queue_declare(queue='cdr.queue', durable=True)  # Используем целевую очередь
        channel.basic_publish(
            exchange='',
            routing_key='cdr.queue',
            body=json.dumps(cdr_data).encode(),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )
        print(f" [x] Sent CDR: {cdr_data}")
    except pika.exceptions.UnroutableError:
        print("CDR message could not be delivered")
    finally:
        conn.close()


def send_cdr_from_file(file_name):
    """Отправляет CDR из JSON-файла в очередь cdrs.queue"""
    file_path = os.path.join(os.path.dirname(__file__), file_name)

    if not os.path.exists(file_path):
        raise FileNotFoundError(f"CDR file {file_path} not found")

    with open(file_path, 'r', encoding='utf-8') as f:
        cdr_data = json.load(f)

    send_cdr_message(cdr_data)


def send_bill_message(bill_data):
    """Отправляет CDR в очередь cdrs.queue"""
    conn = get_rabbitmq_connection()
    try:
        channel = conn.channel()
        channel.queue_declare(queue='bill.queue', durable=True)  # Используем целевую очередь
        channel.basic_publish(
            exchange='',
            routing_key='bill.queue',
            body=json.dumps(bill_data).encode(),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )
        print(f" [x] Sent bill: {bill_data}")
    except pika.exceptions.UnroutableError:
        print("CDR message could not be delivered")
    finally:
        conn.close()

def send_bill_from_file(file_name):
    """Отправляет CDR из JSON-файла в очередь cdrs.queue"""
    file_path = os.path.join(os.path.dirname(__file__), file_name)

    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Bill file {file_path} not found")

    with open(file_path, 'r', encoding='utf-8') as f:
        bill_data = json.load(f)

    send_bill_message(bill_data)