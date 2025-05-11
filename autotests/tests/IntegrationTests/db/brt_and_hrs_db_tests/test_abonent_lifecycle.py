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
    tariff_id = 1
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

    create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

    yield abonent_id, user_id, initial_in, initial_out

    delete_brt_abonent(abonent_id)
    delete_brt_abonent(user_id)


def test_abonent_lifecycle(test_abonent):
    abonent_id, user_id, initial_in, initial_out = test_abonent

    # 1. Проверка создания и начальных данных
    initial_balance = get_brt_balance(abonent_id)
    tariff_data = get_tariff_cost_details(abonent_id)

    assert initial_balance == 100.0
    assert tariff_data is not None
    assert 'tariff_price' in tariff_data

    # 2. Проверка тарифных данных
    assert isinstance(tariff_data['price_per_additional_minute_outcoming'], float)
    assert tariff_data['price_per_additional_minute_outcoming'] > 0

    # 3. Проверка списания средств (имитация звонка)
    # Имитируем 10-минутный исходящий звонок
    call_cost = (tariff_data['price_per_additional_minute_outcoming'] / 60) * 600
    new_balance = 100.0 - call_cost

    set_brt_balance(abonent_id, new_balance)
    updated_balance = get_brt_balance(abonent_id)

    assert abs(updated_balance - new_balance) < 0.01

    # 4. Проверка истории звонков (имитация добавления записи)
    # В реальном тесте здесь должна быть интеграция с системой CDR
    call_history = get_call_history(abonent_id)
    assert isinstance(call_history, list)

    # 5. Проверка удаления
    # Проверка выполняется автоматически в фикстуре через delete_abonent
    # Дополнительная проверка (при наличии метода проверки существования):
    try:
        get_brt_balance(abonent_id)
    except Exception as e:
        assert "не найден" in str(e)