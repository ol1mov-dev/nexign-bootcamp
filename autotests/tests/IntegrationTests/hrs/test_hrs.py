# test_hrs_minutes_deduction.py
import pytest
from datetime import timedelta
import time
from utils.hrs_interaction import (
    create_hrs_abonent,
    delete_hrs_abonent,
    get_hrs_outgoing_minutes,
    set_hrs_outgoing_minutes,
    get_tariff_cost_details, get_hrs_incoming_minutes
)
from utils.rabbit_sender import send_call_message


@pytest.fixture
def hrs_test_abonent():
    """Фикстура для тестирования списания минут в HRS"""
    abonent_id = 88888
    user_id = 5001
    tariff_id = 12

    delete_hrs_abonent(abonent_id)

    # Создание абонента с начальными минутами
    create_hrs_abonent(
        abonent_id=abonent_id,
        user_id=user_id,
        tariff_id=tariff_id,
        initial_in_minutes=100,  # 100 входящих минут
        initial_out_minutes=100  # 100 исходящих минут
    )

    yield {
        'id': abonent_id,
        'initial_out': 100,
        'tariff_id': tariff_id
    }

    # Удаление после теста
    delete_hrs_abonent(abonent_id)

# test_hrs.py
def test_outgoing_call_minutes_deduction(hrs_test_abonent):
    """Проверка списания исходящих минут в HRS"""
    abonent_id = hrs_test_abonent['id']

    # Получаем актуальные начальные минуты
    outgoing_minutes = get_hrs_outgoing_minutes(abonent_id)
    incoming_minutes = get_hrs_incoming_minutes(abonent_id)

    # Отправляем исходящий звонок
    send_call_message(
        abonent_id=abonent_id,
        duration=timedelta(minutes=15),
        call_type="01"
    )

    time.sleep(1)
    # Проверяем обновленные минуты
    assert get_hrs_incoming_minutes(abonent_id) == incoming_minutes
    updated_minutes = get_hrs_outgoing_minutes(abonent_id)
    assert updated_minutes == outgoing_minutes - 15, (
        f"Ожидалось: {outgoing_minutes - 15}, Фактически: {updated_minutes}"
    )


def test_incoming_call_no_deduction(hrs_test_abonent):
    """Входящие звонки не должны списывать минуты"""
    abonent_id = hrs_test_abonent['id']
    outgoing_minutes = get_hrs_outgoing_minutes(abonent_id)
    incoming_minutes = get_hrs_incoming_minutes(abonent_id)

    # Отправляем входящий звонок
    send_call_message(
        abonent_id=abonent_id,
        duration=timedelta(minutes=15),
        call_type="02"
    )

    time.sleep(1)
    # Минуты остаются неизменными
    assert get_hrs_outgoing_minutes(abonent_id) == outgoing_minutes
    updated_minutes = get_hrs_incoming_minutes(abonent_id)
    assert updated_minutes == incoming_minutes - 15, (
        f"Ожидалось: {incoming_minutes - 15}, Фактически: {updated_minutes}"
    )


def test_tariff_limit_exceeded(hrs_test_abonent):
    """Проверка списания при превышении лимита тарифа"""
    abonent_id = hrs_test_abonent['id']

    # Устанавливаем минуты на границе лимита
    set_hrs_outgoing_minutes(abonent_id, 10)

    send_call_message(
        abonent_id=abonent_id,
        duration=timedelta(minutes=20),
        call_type="01"
    )
    time.sleep(1)
    updated_minutes = get_hrs_outgoing_minutes(abonent_id)
    assert updated_minutes == 0, f"Ожидалось 0, получено {updated_minutes}"


