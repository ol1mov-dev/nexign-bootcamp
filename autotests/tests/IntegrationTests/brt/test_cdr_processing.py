# test_cdr_processing.py
import pytest
import time
import json
from datetime import datetime
from utils.brt_interaction import (
    get_call_history,
    create_brt_abonent,
    delete_brt_abonent
)
from utils.hrs_interaction import create_hrs_abonent, delete_hrs_abonent
from utils.rabbit_sender import *
import os

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

cdr_path_1 = os.path.join(PROJECT_ROOT, "cdrs_files", "cdr3.json")
cdr_path_2 = os.path.join(PROJECT_ROOT, "cdrs_files", "cdr4.json")

#cdr_path_1 = os.path.join("..", "..", "..", "cdrs_files", "cdr3.json")
cdr_path_1_for_func = "..//cdrs_files//cdr3.json"
#cdr_path_2 = os.path.join("..", "..",  "..", "cdrs_files", "cdr4.json")
cdr_path_2_for_func = "..//cdrs_files//cdr4.json"

def load_cdr_data(file_path: str) -> list:
    """Загружает данные из JSON-файла"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Файл {file_path} не найден")

    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


@pytest.fixture(scope="module")
def test_data():
    return load_cdr_data(cdr_path_1)

def generate_abonent_id(msisdn: str) -> int:
    """Генерирует ID абонента из номера (первые 8 цифр без +)"""
    return int(msisdn.lstrip("+")[:8])


@pytest.fixture(autouse=True)
def cleanup(request, test_data):
    """Фикстура для очистки данных после тестов"""
    yield
    all_msisdns = set()
    for call in test_data:
        all_msisdns.update([call["firstMsisdn"], call["secondMsisdn"]])

    for msisdn in all_msisdns:
        user_id = generate_abonent_id(msisdn)
        delete_brt_abonent(user_id)
        delete_hrs_abonent(user_id)


def test_cdr_processing_1(json_file: str = cdr_path_1):
    cdr_data = load_cdr_data(json_file)

    # Собираем уникальные номера
    all_msisdns = set()
    for call in cdr_data:
        all_msisdns.update([call["firstMsisdn"], call["secondMsisdn"]])

    # Создаем абонентов
    abonents = {}
    for msisdn in all_msisdns:
        try:
            abonent_id = generate_abonent_id(msisdn)
            now = datetime.now()
            user_id = abonent_id
            tariff_id = 1
            initial_in = 4
            initial_out = 4

            delete_brt_abonent(abonent_id)
            delete_hrs_abonent(user_id)

            # Создаем в BRT
            create_brt_abonent(
                abonent_id=user_id,
                first_name="Тест",
                name="Тестович",
                msisdn=msisdn,
                last_name="Тестовый",
                balance=100.0
            )

            create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

            abonents[msisdn] = abonent_id
            print(f"Создан абонент: {msisdn} -> ID {abonent_id}")
        except Exception as e:
            print(f"Ошибка создания {msisdn}: {str(e)}")

    send_cdr_from_file(cdr_path_1_for_func)

    for call in cdr_data:
        caller_id = generate_abonent_id(call["firstMsisdn"])


        # Получаем историю звонков
        calls = get_call_history(caller_id)
        assert len(calls) > 0, f"Для абонента {caller_id} звонки не найдены"

        # Проверяем последний звонок
        last_call = calls[0]
        # Преобразуем время из БД в datetime

        db_start_time = last_call["start_time"]
        if isinstance(db_start_time, str):
            db_start_time = datetime.fromisoformat(db_start_time.replace("Z", ""))

        # Преобразуем ожидаемое время
        expected_start = datetime.fromisoformat(call["startTime"].replace("Z", ""))

        # Сравниваем с точностью до секунд
        assert db_start_time.replace(microsecond=0) == expected_start.replace(microsecond=0), "Неверное время начала"


def test_cdr_processing_2(json_file: str = cdr_path_2):
    cdr_data = load_cdr_data(json_file)

    # Собираем уникальные номера
    all_msisdns = set()
    for call in cdr_data:
        all_msisdns.update([call["firstMsisdn"], call["secondMsisdn"]])

    # Создаем абонентов
    abonents = {}
    for msisdn in all_msisdns:
        try:
            abonent_id = generate_abonent_id(msisdn)
            now = datetime.now()
            user_id = abonent_id
            tariff_id = 1
            initial_in = 4
            initial_out = 4

            delete_brt_abonent(abonent_id)
            delete_hrs_abonent(user_id)

            # Создаем в BRT
            create_brt_abonent(
                abonent_id=user_id,
                first_name="Тест",
                name="Тестович",
                msisdn=msisdn,
                last_name="Тестовый",
                balance=100.0
            )

            create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

            abonents[msisdn] = abonent_id
            print(f"Создан абонент: {msisdn} -> ID {abonent_id}")
        except Exception as e:
            print(f"Ошибка создания {msisdn}: {str(e)}")

    send_cdr_from_file(cdr_path_2_for_func)

    for call in cdr_data:
        caller_id = generate_abonent_id(call["firstMsisdn"])


        # Получаем историю звонков
        calls = get_call_history(caller_id)
        assert len(calls) > 0, f"Для абонента {caller_id} звонки не найдены"

        # Проверяем последний звонок
        last_call = calls[0]
        # Преобразуем время из БД в datetime

        db_start_time = last_call["start_time"]
        if isinstance(db_start_time, str):
            db_start_time = datetime.fromisoformat(db_start_time.replace("Z", ""))

        # Преобразуем ожидаемое время
        expected_start = datetime.fromisoformat(call["startTime"].replace("Z", ""))

        # Сравниваем с точностью до секунд
        assert db_start_time.replace(microsecond=0) == expected_start.replace(microsecond=0), "Неверное время начала"