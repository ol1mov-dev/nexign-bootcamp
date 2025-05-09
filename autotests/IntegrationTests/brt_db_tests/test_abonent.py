import pytest
from utils.brt_interaction import *

from decimal import Decimal
import random


@pytest.fixture
def test_abonent():
    abonent_id = 987654
    now = datetime.now()

    delete_brt_abonent(abonent_id)

    create_brt_abonent(
        abonent_id=abonent_id,
        first_name="Тест",
        name="Тестович",
        msisdn="+79000000000",
        last_name="Тестовый",
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

    yield abonent_id
    delete_brt_abonent(abonent_id)


def test_brt_abonent_creation(test_abonent):
    abonent_id = test_abonent
    balance = get_brt_balance(abonent_id)

    assert balance == 100.0
#   assert get_call_history(abonent_id) == []

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


def test_call_history(test_abonent):
    abonent_id = test_abonent
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

    # Проверка каскадного удаления
    delete_brt_abonent(abonent_id)
    assert len(get_call_history(abonent_id)) == 0
