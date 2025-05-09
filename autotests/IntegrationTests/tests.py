import pytest
from decimal import Decimal
from unittest.mock import MagicMock, call
from utils import brt_interaction
from utils import hrs_interaction
import re


# Фикстуры для моков соединений
@pytest.fixture
def mock_brt_conn(mocker):
    # Мокируем соединение и контекстный менеджер курсора
    mock_conn = mocker.patch("utils.brt_interaction.get_brt_db_connection")
    mock_conn.return_value.__enter__.return_value = MagicMock()  # Для "with conn"

    # Мокируем курсор и его контекстный менеджер
    mock_cursor = MagicMock()
    mock_conn.return_value.cursor.return_value.__enter__.return_value = mock_cursor

    return mock_conn, mock_cursor


@pytest.fixture
def mock_hrs_conn(mocker):  # Аналогично для HRS
    mock_conn = mocker.patch("utils.hrs_interaction.get_hrs_db_connection")
    mock_conn.return_value.__enter__.return_value = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.return_value.cursor.return_value.__enter__.return_value = mock_cursor
    return mock_conn, mock_cursor


# 1. Получение данных абонента
def test_get_abonent_data(mock_brt_conn):
    _, mock_cursor = mock_brt_conn
    mock_cursor.fetchone.return_value = (Decimal("150.75"),)

    balance = brt_interaction.get_brt_balance(123)

    # Проверяем вызов execute с правильными аргументами
    mock_cursor.execute.assert_called_once_with(
        "SELECT balance FROM abonents WHERE id = %s",
        (123,)
    )
    assert balance == 150.75


# 4. Получение данных о звонке абонента
def test_get_call_history(mock_brt_conn):
    _, mock_cursor = mock_brt_conn

    # Мокируем данные из БД
    mock_cursor.description = [
        ('id',),
        ('stranger_msisdn',),
        ('call_type',),
        ('start_time',),
        ('end_time',),
        ('duration',)
    ]
    mock_cursor.fetchall.return_value = [
        (1, '+79277117677', '02', '2024-05-09 18:06:23', '2024-05-09 18:28:38', '00:22:15'),
        (4, '+79265432109', '01', '2024-05-10 00:06:23', '2024-05-10 00:35:29', '00:29:06')
    ]

    # Вызываем тестируемую функцию
    calls = brt_interaction.get_call_history(abonent_id=3)

    # Нормализуем SQL-запросы для сравнения
    def normalize_sql(sql):
        return re.sub(r'\s+', ' ', sql.strip())

    expected_sql = """
        SELECT
            id,
            stranger_msisdn,
            call_type,
            start_time,
            end_time,
            duration
        FROM calls
        WHERE abonent_id = %s
        """
    actual_call = mock_cursor.execute.call_args
    actual_sql = actual_call[0][0] if actual_call else ""

    assert normalize_sql(actual_sql) == normalize_sql(expected_sql)
    assert actual_call[0][1] == (3,)

    # Проверяем преобразование данных
    assert len(calls) == 2
    assert calls[0] == {
        'id': 1,
        'stranger_msisdn': '+79277117677',
        'call_type': '02',
        'start_time': '2024-05-09 18:06:23',
        'end_time': '2024-05-09 18:28:38',
        'duration': '00:22:15'
    }











"""
# 3. Проверка списания средств
def test_balance_deduction(mock_brt_conn):
    mock_conn, mock_cursor = mock_brt_conn
    test_id = 456
    new_balance = 85.0

    brt_interaction.set_brt_balance(test_id, new_balance)

    mock_cursor.execute.assert_called_with(
        "UPDATE abonents SET balance = %s WHERE id = %s",
        (Decimal("85.0"), test_id))
    mock_conn.return_value.commit.assert_called_once()

    # 4. Получение данных о звонке


def test_get_call_details(mock_hrs_conn):
    _, mock_cursor = mock_hrs_conn
    mock_cursor.fetchone.return_value = (300, 45.0)  # Длительность и стоимость

    # Предположим, что есть функция get_call_info
    duration, cost = 300, 45.0  # Заглушка

    assert duration == 300
    assert cost == 45.0


# 5. Проверка записи звонка
def test_call_record_correctness(mock_hrs_conn):
    _, mock_cursor = mock_hrs_conn
    test_data = ("2023-01-01 12:00", 300, 45.0, 789)

    # Предположим, что есть функция create_call_record
    mock_cursor.execute.return_value = None
    # hrs_interaction.create_call_record(*test_data)

    mock_cursor.execute.assert_called_with(
        "INSERT INTO calls (timestamp, duration, cost, abonent_id) VALUES (%s, %s, %s, %s)",
        test_data
    )

"""