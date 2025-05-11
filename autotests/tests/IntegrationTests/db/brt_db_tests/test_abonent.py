import pytest
from utils.brt_interaction import *

from decimal import Decimal
import random


@pytest.fixture
def test_abonent():
    abonent_id = 987654
    now = datetime.now()

    delete_brt_abonent(abonent_id)

    create_brt_abonent(
        abonent_id=abonent_id,
        first_name="Тест",
        name="Тестович",
        msisdn="+79000000001",
        middle_name="Тестовый",
        balance=100.0
    )

    yield abonent_id
    delete_brt_abonent(abonent_id)


def test_brt_abonent_creation(test_abonent):
    abonent_id = test_abonent
    balance = get_brt_balance(abonent_id)

    assert balance == 100.0
#   assert get_call_history(abonent_id) == []

    # Проверка данных абонента
    conn = get_brt_db_connection()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM abonents WHERE id = %s", (abonent_id,))
            result = cur.fetchone()
            assert result[1] == "Тест"  # first_name
            assert result[4] == "+79000000001"  # msisdn
    finally:
        conn.close()
