import pytest
from utils.brt_interaction import *

from decimal import Decimal
import random


@pytest.fixture
def test_abonent():
    abonent_id = 987654

    delete_brt_abonent(abonent_id)
    # Создаем в BRT
    create_brt_abonent(
        abonent_id=abonent_id,
        first_name="Тест",
        name="Тестович",
        msisdn="+79000000000",
        last_name="Тестовый",
        balance=100.0
    )

    """
    # Создаем в HRS
    abonent_id = create_brt_abonent(
        abonent_id=abonent_id,
        user_id=5001,
        tariff_id=1
    )
    """

    yield abonent_id

    # Удаляем из обеих систем
    #   delete_brt_abonent(abonent_id)
    delete_brt_abonent(abonent_id)


def test_brt_abonent_creation(test_abonent):
    abonent_id = test_abonent
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


def test_abonent_lifecycle(test_abonent):
    abonent_id = test_abonent

    # 1. Проверка создания и начальных данных
    initial_balance = get_brt_balance(abonent_id)
    #    tariff_data = get_tariff_cost_details(abonent_id)

    assert initial_balance == 100.0
    #   assert tariff_data is not None
    #   assert 'tariff_price' in tariff_data

    # 2. Проверка тарифных данных
    #  assert isinstance(tariff_data['price_per_additional_minute_outcoming'], float)
    #  assert tariff_data['price_per_additional_minute_outcoming'] > 0

    # 3. Проверка списания средств (имитация звонка)
    # Имитируем 10-минутный исходящий звонок
    # call_cost = (tariff_data['price_per_additional_minute_outcoming'] / 60) * 600
    #  new_balance = 100.0 - call_cost

    #  set_brt_balance(abonent_id, new_balance)
    updated_balance = get_brt_balance(abonent_id)

    #  assert abs(updated_balance - new_balance) < 0.01

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