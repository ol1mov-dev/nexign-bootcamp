# test_hrs_processing.py
import pytest
import json
import os
from utils.brt_interaction import (
    create_brt_abonent,
    delete_brt_abonent,
    get_brt_balance
)
from utils.hrs_interaction import create_hrs_abonent, delete_hrs_abonent
from utils.rabbit_sender import send_bill_message
import time
from datetime import datetime

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

bill_path_1 = os.path.join(PROJECT_ROOT, "bills_jsons", "bill1.json")
bill_path_2 = os.path.join(PROJECT_ROOT, "bills_jsons", "bill3.json")


def load_bill_data(file_path: str) -> list:
    """Загружает данные из JSON-файла счетами"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Файл {file_path} не найден")
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def generate_abonent_id(abonent_id: int) -> int:
    """Для совместимости с фикстурой (ID уже указан в биллинге)"""
    return abonent_id


@pytest.fixture(scope="module")
def test_data():
    return load_bill_data(bill_path_1)


@pytest.fixture(autouse=True)
def cleanup(request, test_data):
    """Фикстура для очистки данных после тестов"""
    yield
    for bill in test_data:
        user_id = bill["abonentId"]
        delete_brt_abonent(user_id)


def test_hrs_processing():
    # Загрузка данных биллинга
    billing_data = load_bill_data(bill_path_1)

    # Собираем уникальные ID абонентов
    abonent_ids = {bill["abonentId"] for bill in billing_data}

    # Создаем абонентов и устанавливаем начальный баланс
    for abonent_id in abonent_ids:
        try:
            # Очистка перед созданием
            delete_brt_abonent(abonent_id)

            # Создание в BRT с балансом 200
            create_brt_abonent(
                abonent_id=abonent_id,
                first_name="Тест",
                name="Тестович",
                msisdn=f"+{abonent_id}",
                middle_name="Тестовый",
                balance=200.0
            )

            print(f"Создан абонент ID: {abonent_id}")
        except Exception as e:
            pytest.fail(f"Ошибка создания абонента {abonent_id}: {str(e)}")

    # Отправка данных биллинга
 #   send_bill_message(bill for bill in billing_data)


    # Проверяем списания
    for bill in billing_data:
        abonent_id = bill["abonentId"]
        start_balance = get_brt_balance(abonent_id)
        send_bill_message(bill)
        time.sleep(1)
        expected_balance = start_balance - bill["totalPrice"]

        # Получаем текущий баланс
        current_balance = get_brt_balance(abonent_id)

        assert round(current_balance, 2) == round(expected_balance, 2), (
            f"Неверный баланс для абонента {abonent_id}. "
            f"Ожидалось: {expected_balance}, Получено: {current_balance}"
        )

def test_negative_bills_hrs_processing():
    # Загрузка данных биллинга
    billing_data = load_bill_data(bill_path_2)

    # Собираем уникальные ID абонентов
    abonent_ids = {bill["abonentId"] for bill in billing_data}

    # Создаем абонентов и устанавливаем начальный баланс
    for abonent_id in abonent_ids:
        try:
            # Очистка перед созданием
            delete_brt_abonent(abonent_id)

            # Создание в BRT с балансом 200
            create_brt_abonent(
                abonent_id=abonent_id,
                first_name="Тест",
                name="Тестович",
                msisdn=f"+{abonent_id}",
                middle_name="Тестовый",
                balance=200.0
            )

            print(f"Создан абонент ID: {abonent_id}")
        except Exception as e:
            pytest.fail(f"Ошибка создания абонента {abonent_id}: {str(e)}")

    # Отправка данных биллинга
 #   send_bill_message(bill for bill in billing_data)


    # Проверяем списания
    for bill in billing_data:
        abonent_id = bill["abonentId"]
        start_balance = get_brt_balance(abonent_id)
        send_bill_message(bill)
        time.sleep(1)
        expected_balance = start_balance - bill["totalPrice"]

        # Получаем текущий баланс
        current_balance = get_brt_balance(abonent_id)

        assert round(current_balance, 2) < round(expected_balance, 2), (
            f"Неверный баланс для абонента {abonent_id}. "
            f"Ожидалось: {expected_balance}, Получено: {current_balance}"
        )