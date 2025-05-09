import pytest
from decimal import Decimal
from unittest.mock import MagicMock, call, patch
from utils import brt_interaction
from utils import hrs_interaction
import re
import psycopg2


# Фикстуры для моков соединений
@pytest.fixture
def mock_brt_conn():
    # Мокируем соединение и контекстный менеджер курсора
    with patch("utils.brt_interaction.get_brt_db_connection") as mock_conn:
        mock_conn.return_value.__enter__.return_value = MagicMock()
        mock_cursor = MagicMock()
        mock_conn.return_value.cursor.return_value.__enter__.return_value = mock_cursor
        yield mock_conn, mock_cursor


@pytest.fixture
def mock_hrs_conn():
    with patch("utils.hrs_interaction.get_hrs_db_connection") as mock_conn:
        mock_conn.return_value.__enter__.return_value = MagicMock()
        mock_cursor = MagicMock()
        mock_conn.return_value.cursor.return_value.__enter__.return_value = mock_cursor
        yield mock_conn, mock_cursor


# 1. Получение данных абонента
@pytest.mark.parametrize(
    "mock_return, expected_result",
    [
        ((Decimal("150.75"),), 150.75),  # Нормальный случай
        (None, None),                    # Абонент не найден
        ((Decimal("0"),), 0.0),          # Нулевой баланс
        ((Decimal("-10.50"),), -10.50),  # Отрицательный баланс
    ]
)

def test_get_abonent_data(mock_brt_conn, mock_return, expected_result):
    _, mock_cursor = mock_brt_conn
    mock_cursor.fetchone.return_value = mock_return

    balance = brt_interaction.get_brt_balance(123)

    mock_cursor.execute.assert_called_once_with(
        "SELECT balance FROM abonents WHERE id = %s",
        (123,)
    )
    assert balance == expected_result


def test_get_abonent_data_invalid_id(mock_brt_conn):
    _, mock_cursor = mock_brt_conn
    with pytest.raises(ValueError):
        brt_interaction.get_brt_balance("invalid_id")


def test_get_abonent_data_db_error(mock_brt_conn):
    _, mock_cursor = mock_brt_conn
    mock_cursor.execute.side_effect = psycopg2.Error("Database error")
    with pytest.raises(psycopg2.Error):
        brt_interaction.get_brt_balance(123)


# 2. Получение данных о звонке абонента
@pytest.mark.parametrize(
    "mock_return, expected_result_len",
    [
        (  # Нормальный случай
            [
                (1, '+79277117677', '02', '2024-05-09 18:06:23', '2024-05-09 18:28:38', '00:22:15'),
                (4, '+79265432109', '01', '2024-05-10 00:06:23', '2024-05-10 00:35:29', '00:29:06')
            ],
            2
        ),
        ([], 0),  # Пустая история звонков
    ]
)
def test_get_call_history(mock_brt_conn, mock_return, expected_result_len):
    _, mock_cursor = mock_brt_conn
    mock_cursor.description = [
        ('id',), ('stranger_msisdn',), ('call_type',), ('start_time',), ('end_time',), ('duration',)
    ]
    mock_cursor.fetchall.return_value = mock_return

    calls = brt_interaction.get_call_history(abonent_id=3)

    def normalize_sql(sql):
        return re.sub(r'\s+', ' ', sql.strip())

    expected_sql = """
        SELECT id, stranger_msisdn, call_type, start_time, end_time, duration
        FROM calls
        WHERE abonent_id = %s
    """
    actual_call = mock_cursor.execute.call_args
    actual_sql = actual_call[0][0] if actual_call else ""

    assert normalize_sql(actual_sql) == normalize_sql(expected_sql)
    assert actual_call[0][1] == (3,)
    assert len(calls) == expected_result_len

    if mock_return:
        assert calls[0] == {
            'id': mock_return[0][0],
            'stranger_msisdn': mock_return[0][1],
            'call_type': mock_return[0][2],
            'start_time': mock_return[0][3],
            'end_time': mock_return[0][4],
            'duration': mock_return[0][5]
        }


def test_get_call_history_invalid_id(mock_brt_conn):
    _, mock_cursor = mock_brt_conn
    with pytest.raises(ValueError):
        brt_interaction.get_call_history(abonent_id="invalid_id")


# 3. Проверка списания средств
@pytest.mark.parametrize(
    "new_balance, expected_decimal",
    [
        (85.0, Decimal("85.0")),
        (0.0, Decimal("0.0")),
        (-10.5, Decimal("-10.5")),
    ]
)
def test_balance_deduction(mock_brt_conn, new_balance, expected_decimal):
    mock_conn, mock_cursor = mock_brt_conn
    test_id = 456

    brt_interaction.set_brt_balance(test_id, new_balance)

    mock_cursor.execute.assert_called_with(
        "UPDATE abonents SET balance = %s WHERE id = %s",
        (expected_decimal, test_id)
    )
    mock_conn.return_value.commit.assert_called_once()


def test_balance_deduction_invalid_balance(mock_brt_conn):
    mock_conn, mock_cursor = mock_brt_conn
    with pytest.raises(ValueError):
        brt_interaction.set_brt_balance(456, "invalid_balance")


def test_balance_deduction_db_error(mock_brt_conn):
    mock_conn, mock_cursor = mock_brt_conn
    mock_cursor.execute.side_effect = psycopg2.Error("Database error")
    with pytest.raises(psycopg2.Error):
        brt_interaction.set_brt_balance(456, 85.0)
    mock_conn.return_value.rollback.assert_called_once()


# 4. Получение данных о стоимости звонков для тарифа абонента
@pytest.mark.parametrize(
    "mock_return, expected_result",
    [
        (  # Нормальный случай
            (100.00, 1.50, 2.50),
            {'tariff_price': 100.00, 'price_per_additional_minute_outcoming': 1.50, 'price_per_additional_minute_incoming': 2.50}
        ),
        (None, None),  # Тариф не найден
    ]
)

def test_get_tariff_cost_details(mock_hrs_conn, mock_return, expected_result):
    _, mock_cursor = mock_hrs_conn
    mock_cursor.description = [
        ('price',), ('price_per_additional_minute_outcoming',), ('price_per_additional_minute_incoming',)
    ]
    mock_cursor.fetchone.return_value = mock_return

    cost_details = hrs_interaction.get_tariff_cost_details(abonent_id=4)

    assert cost_details == expected_result


def test_get_tariff_cost_details_invalid_id(mock_hrs_conn):
    _, mock_cursor = mock_hrs_conn
    with pytest.raises(ValueError):
        hrs_interaction.get_tariff_cost_details(abonent_id="invalid_id")