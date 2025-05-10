# test_hrs_rabbit_integration.py
import pytest
from datetime import timedelta
import time
from utils.hrs_interaction import (
    create_hrs_abonent,
    delete_hrs_abonent,
    get_tariff_cost_details,
    get_hrs_outgoing_minutes
)
from utils.brt_interaction import get_brt_balance, set_brt_balance
from utils.rabbit_sender import send_call_message

@pytest.fixture
def integration_test_abonent():
    """Фикстура для интеграционного теста с RabbitMQ"""
    abonent_id = 77777
    user_id = 5001
    tariff_id = 1

    # Очистка перед тестом
    try:
        delete_hrs_abonent(abonent_id)
    except Exception:
        pass

    # Создание абонента
    create_hrs_abonent(
        abonent_id=abonent_id,
        user_id=user_id,
        tariff_id=tariff_id,
        initial_in_minutes=0,
        initial_out_minutes=0
    )

    yield {
        'id': abonent_id,
        'initial_balance': initial_balance,
        'tariff_id': tariff_id
    }

    # Очистка после теста
    delete_hrs_abonent(abonent_id)

def test_rabbit_call_processing(integration_test_abonent):
    """Интеграционный тест: отправка в Rabbit -> проверка списаний"""
    abonent_id = integration_test_abonent['id']
    tariff = get_tariff_cost_details(abonent_id)
    initial_balance = integration_test_abonent['initial_balance']


    # Отправляем исходящий звонок длительностью 15 минут
    send_call_message(
        abonent_id=abonent_id,
        duration=timedelta(minutes=15),
        call_type="02"  # Исходящий
    )

    # Ждем обработки сообщения (может потребоваться настройка времени)
    time.sleep(3)

    # Расчет ожидаемого списания
    expected_cost = (15 * tariff['price_per_additional_minute_outcoming'])
    new_balance = initial_balance - expected_cost

    # Проверяем баланс в BRT
    assert get_brt_balance(abonent_id) == pytest.approx(new_balance, abs=0.01)

    # Проверяем минуты в HRS (для тарифов с лимитами)
    if tariff.get('included_outgoing_minutes', 0) > 0:
        assert get_hrs_outgoing_minutes(abonent_id) == 15
    else:
        assert get_hrs_outgoing_minutes(abonent_id) == 0

def test_incoming_call_processing(integration_test_abonent):
    """Проверка обработки входящих звонков"""
    abonent_id = integration_test_abonent['id']
    initial_balance = integration_test_abonent['initial_balance']

    # Отправляем входящий звонок 10 минут
    send_call_message(
        abonent_id=abonent_id,
        duration=600,  # 10 минут в секундах
        call_type="01"  # Входящий
    )

    time.sleep(3)

    # Входящие звонки не должны списывать средства
    assert get_brt_balance(abonent_id) == pytest.approx(initial_balance, abs=0.01)


def test_invalid_call_processing(integration_test_abonent):
    """Проверка обработки некорректных сообщений"""
    abonent_id = integration_test_abonent['id']
    initial_balance = integration_test_abonent['initial_balance']

    # Отправляем сообщение с неверным call_type
    send_call_message(
        abonent_id=abonent_id,
        duration=timedelta(minutes=5),
        call_type="99"  # Несуществующий тип
    )

    time.sleep(5)

    # Баланс не должен измениться
    assert get_brt_balance(abonent_id) == pytest.approx(initial_balance, abs=0.01)