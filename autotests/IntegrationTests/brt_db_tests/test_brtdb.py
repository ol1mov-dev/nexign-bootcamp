import pytest
from decimal import Decimal
from unittest.mock import MagicMock, patch
from utils.brt_interaction import set_brt_balance, get_brt_balance


@pytest.fixture
def mock_db():
    """Фикстура для мокирования соединения с БД"""
    with patch('utils.brt_interaction.get_brt_db_connection') as mock_conn:
        connection = MagicMock()
        cursor = MagicMock()

        # Мокируем контекстный менеджер для курсора
        connection.cursor.return_value.__enter__.return_value = cursor
        connection.cursor.return_value.__exit__.return_value = False

        mock_conn.return_value = connection
        yield connection, cursor


def test_set_balance_success(mock_db):
    """Тест успешной установки баланса"""
    _, cursor = mock_db
    set_brt_balance(1, 200.75)

    cursor.execute.assert_called_once_with(
        "UPDATE abonents SET balance = %s WHERE id = %s",
        (Decimal('200.75'), 1)
    )


def test_get_balance_success(mock_db):
    """Тест успешного получения баланса"""
    _, cursor = mock_db
    cursor.fetchone.return_value = (Decimal('150.00'),)

    result = get_brt_balance(1)
    assert result == 150.00
    cursor.execute.assert_called_once_with(
        "SELECT balance FROM abonents WHERE id = %s", (1,)
    )


def test_balance_not_found(mock_db):
    """Тест отсутствия записи о балансе"""
    _, cursor = mock_db
    cursor.fetchone.return_value = None

    with pytest.raises(TypeError):
        get_brt_balance(999)


def test_database_connection_error():
    """Тест ошибки подключения"""
    with patch('utils.brt_interaction.get_brt_db_connection', side_effect=Exception("Connection error")):
        with pytest.raises(Exception):
            set_brt_balance(1, 100)

        with pytest.raises(Exception):
            get_brt_balance(1)



def test_negative_balance(mock_db):
    """Тест обработки отрицательного баланса"""
    _, cursor = mock_db
    cursor.fetchone.return_value = (Decimal('-50.00'),)
    result = get_brt_balance(1)
    assert result == -50.00

