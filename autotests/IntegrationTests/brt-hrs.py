import pika
import psycopg2
import pytest
import time
from datetime import timedelta
from decimal import Decimal
from config import *
from utils.rabbit_sender import *
from utils.hrs_interaction import *
from utils.brt_interaction import *


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
    send_call_message(abonent_id, timedelta(minutes=10))

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
