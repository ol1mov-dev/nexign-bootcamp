import pytest
from utils.hrs_interaction import *
from psycopg2 import IntegrityError


@pytest.fixture
def setup_test_abonent():
    abonent_id = 999999
    user_id = 123
    tariff_id = 1
    initial_in = 10
    initial_out = 20

    # Очистка перед тестом
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("DELETE FROM abonents WHERE id = %s", (abonent_id,))
            conn.commit()
    finally:
        conn.close()

    # Создание первоначального абонента
    create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

    yield abonent_id, user_id

    # Удаление после теста
    delete_hrs_abonent(abonent_id)


def test_duplicate_abonent_creation(setup_test_abonent):
    abonent_id, user_id = setup_test_abonent
    tariff_id = 1
    initial_in = 15
    initial_out = 25

    # Попытка создания дубликата
    with pytest.raises(IntegrityError) as exc_info:
        create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

    # Проверка типа ошибки
    assert "duplicate key value violates unique constraint" in str(exc_info.value)

    # Проверка, что в базе остался оригинальный абонент
    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            # Проверка количества записей
            cur.execute("SELECT COUNT(*) FROM abonents WHERE id = %s", (abonent_id,))
            count = cur.fetchone()[0]
            assert count == 1, "В таблице abonents появился дубликат"

            # Проверка неизменности данных
            cur.execute(
                "SELECT balance_id FROM abonents WHERE id = %s",
                (abonent_id,)
            )
            balance_id = cur.fetchone()[0]

            cur.execute(
                "SELECT amount_of_minutes_for_incoming_call, amount_of_minutes_for_outcoming_call FROM balances WHERE id = %s",
                (balance_id,)
            )
            balance_data = cur.fetchone()
            assert balance_data[0] == 10, "Входящие минуты оригинала изменились"
            assert balance_data[1] == 20, "Исходящие минуты оригинала изменились"
    finally:
        conn.close()