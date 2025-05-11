from typing import Any

import pytest

from utils.brt_interaction import *

from utils.hrs_interaction import *
from decimal import Decimal
import random


@pytest.fixture
def test_abonent():
    abonent_id = 999999
    now = datetime.now()
    user_id = abonent_id
    tariff_id = 11
    initial_in = 10
    initial_out = 20

    delete_brt_abonent(abonent_id)
    delete_hrs_abonent(user_id)

    # Создаем в BRT
    create_brt_abonent(
        abonent_id=user_id,
        first_name="Тест",
        name="Тестович",
        msisdn="+79000000000",
        middle_name="Тестовый",
        balance=100.0
    )

    create_call(
        abonent_id=abonent_id,
        stranger_msisdn="+79007654321",
        call_type="02",
        start_time=now - timedelta(hours=1),
        end_time=now - timedelta(minutes=50),
        duration=timedelta(minutes=10)
    )

    create_call(
        abonent_id=abonent_id,
        stranger_msisdn="+79001234567",
        call_type="01",
        start_time=now - timedelta(hours=2),
        end_time=now - timedelta(hours=1, minutes=55),
        duration=timedelta(minutes=5)
    )

    create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

    yield abonent_id, user_id, initial_in, initial_out

    delete_brt_abonent(abonent_id)
    delete_hrs_abonent(user_id)


def test_full_abonent_creation(test_abonent):
    abonent_id, user_id, initial_in, initial_out = test_abonent
    balance = get_brt_balance(abonent_id)

    assert balance == 100.0

    # Проверка данных абонента
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM abonents WHERE id = %s", (abonent_id,))
            result = cur.fetchone()
            assert result[1] == "Тест"  # first_name
            assert result[4] == "+79000000000"  # msisdn
    finally:
        conn.close()

    conn = get_hrs_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT balance_id FROM abonents WHERE id = %s", (abonent_id,))
            abonent_data = cur.fetchone()
            assert abonent_data is not None, "Абонент не найден в таблице abonents"
            balance_id = abonent_data[0]

            cur.execute(
                "SELECT amount_of_minutes_for_incoming_call, amount_of_minutes_for_outcoming_call FROM balances WHERE id = %s",
                (balance_id,))
            balance_data = cur.fetchone()
            assert balance_data is not None, "Баланс не найден в таблице balances"
            assert balance_data[0] == initial_in, "Входящие минуты не совпадают"
            assert balance_data[1] == initial_out, "Исходящие минуты не совпадают"
    finally:
        conn.close()

    history = get_call_history(abonent_id)

    # Проверяем общее количество звонков
    assert len(history) == 2

    # Проверяем структуру данных
    for call in history:
        assert 'stranger_msisdn' in call
        assert 'call_type' in call
        assert 'duration' in call

    # Проверяем конкретные значения
    first_call = history[0]
    assert first_call['call_type'] == "02"
    assert first_call['stranger_msisdn'] == "+79007654321"

    # Проверка формата duration
    assert isinstance(first_call['duration'], (timedelta, str))

