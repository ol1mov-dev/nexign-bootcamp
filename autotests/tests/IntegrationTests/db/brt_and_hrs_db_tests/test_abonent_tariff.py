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
        msisdn="+79000000001",
        middle_name="Тестовый",
        balance=100.0
    )

    create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

    yield abonent_id, user_id, initial_in, initial_out

    delete_brt_abonent(abonent_id)
    delete_hrs_abonent(user_id)


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

    try:
        get_brt_balance(abonent_id)
    except Exception as e:
        assert "не найден" in str(e)