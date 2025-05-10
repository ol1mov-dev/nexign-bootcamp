from brt_interaction import create_brt_abonent, create_call, delete_brt_abonent
from datetime import datetime
from hrs_interaction import *
import json
import os

# Данные для добавления
cdr_data = "..//cdrs_files//cdr3.json"


def load_cdr_data(file_path: str) -> list:
    """Загружает данные из JSON-файла"""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Файл {file_path} не найден")

    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def generate_abonent_id(msisdn: str) -> int:
    """Генерирует ID абонента из номера (первые 8 цифр без +)"""
    return int(msisdn.lstrip("+")[:8])


def add_test_data(json_file: str = cdr_data):
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
            initial_in = 10
            initial_out = 20

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


if __name__ == "__main__":
    add_test_data()
