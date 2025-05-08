import psycopg2
from psycopg2 import OperationalError


def get_abonents():
    connection = None  # Initialize connection as None
    try:
        # Подключение к базе данных
        connection = psycopg2.connect(
            host="localhost",
            database="brtdb",
            user="postgres",
            password="postgres",
            port="5433"
        )

        # Создание курсора
        cursor = connection.cursor()

        # Выполнение SQL-запроса
        cursor.execute("SELECT first_name, balance FROM abonents")

        # Получение всех результатов
        abonents = cursor.fetchall()

        # Вывод результатов
        print("\nСписок абонентов и их балансов:")
        print("--------------------------------")
        for first_name, balance in abonents:
            print(f"{first_name}: {balance}")
        print("--------------------------------")
        print(f"Всего абонентов: {len(abonents)}")

    except OperationalError as e:
        print(f"Ошибка подключения к PostgreSQL: {e}")
    except Exception as e:
        print(f"Ошибка при выполнении запроса: {e}")
    finally:
        # Закрытие соединения
        if connection:
            cursor.close()
            connection.close()
            print("\nСоединение с PostgreSQL закрыто")


# Вызов функции
get_abonents()