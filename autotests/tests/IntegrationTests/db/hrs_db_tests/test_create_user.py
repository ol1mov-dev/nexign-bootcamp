import pytest
from utils.hrs_interaction import *


@pytest.fixture
def create_test_abonent():
    abonent_id = 999998
    user_id = 123
    tariff_id = 11
    initial_in = 10
    initial_out = 20

    # Очистка возможных остаточных данных
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("DELETE FROM abonents WHERE id = %s", (abonent_id,))
            cur.execute("DELETE FROM balances WHERE id IN (SELECT balance_id FROM abonents WHERE id = %s)", (abonent_id,))
            conn.commit()
    finally:
        conn.close()

    # Создание абонента
    created_id = create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)
    assert created_id == abonent_id, "Ошибка создания абонента"

    yield abonent_id, initial_in, initial_out

    # Удаление абонента после теста
    delete_hrs_abonent(abonent_id)

    # Проверка, что удаление действительно произошло
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM abonents WHERE id = %s", (abonent_id,))
            abonent_exists = cur.fetchone() is not None
            assert not abonent_exists, "Абонент не удален из таблицы abonents"

            cur.execute("SELECT * FROM balances WHERE id = (SELECT balance_id FROM abonents WHERE id = %s)", (abonent_id,))
            balance_exists = cur.fetchone() is not None
            assert not balance_exists, "Баланс не удален из таблицы balances"
    finally:
        conn.close()


def test_create_and_delete_abonent(create_test_abonent):
    abonent_id, initial_in, initial_out = create_test_abonent

    # Проверка, что абонент и его баланс созданы
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT balance_id FROM abonents WHERE id = %s", (abonent_id,))
            abonent_data = cur.fetchone()
            assert abonent_data is not None, "Абонент не найден в таблице abonents"
            balance_id = abonent_data[0]

            cur.execute("SELECT amount_of_minutes_for_incoming_call, amount_of_minutes_for_outcoming_call FROM balances WHERE id = %s", (balance_id,))
            balance_data = cur.fetchone()
            assert balance_data is not None, "Баланс не найден в таблице balances"
            assert balance_data[0] == initial_in, "Входящие минуты не совпадают"
            assert balance_data[1] == initial_out, "Исходящие минуты не совпадают"
    finally:
        conn.close()