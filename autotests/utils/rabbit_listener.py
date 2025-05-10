import pika
import json
from config import *


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

# Параметры подключения (заменить на ваши)
conn = get_rabbitmq_connection()
channel = conn.channel()
queue_name = 'bill.queue'  # Указать имя очереди

# Callback для сохранения сообщений
def callback(ch, method, properties, body):
    try:
        json_data = json.loads(body)
        print(json_data)
        with open('../trash/messages.json', 'a') as f:
            f.write(json.dumps(json_data) + '\n')
    except json.JSONDecodeError:
        print("Не JSON:", body)

channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=True)
print('Ожидание сообщений...')
channel.start_consuming()