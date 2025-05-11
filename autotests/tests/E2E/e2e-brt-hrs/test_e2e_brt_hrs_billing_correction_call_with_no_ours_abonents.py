# test_e2e.py
import pytest
import time
import json
import os
from datetime import datetime, timedelta
from utils.brt_interaction import (
    create_brt_abonent,
    delete_brt_abonent,
    get_brt_balance,
    get_call_history
)
from utils.hrs_interaction import (
    create_hrs_abonent,
    delete_hrs_abonent,
    get_hrs_outgoing_minutes,
    get_hrs_incoming_minutes,
    set_hrs_outgoing_minutes
)
from utils.rabbit_sender import send_cdr_from_file, send_call_message, send_bill_message

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
cdr_path_outgoing = os.path.join(PROJECT_ROOT, "cdrs_files", "cdr_e2e_outgoing.json")
cdr_path_incoming = os.path.join(PROJECT_ROOT, "cdrs_files", "cdr_e2e_incoming.json")

TEST_ABONENT_ID = 88888
TARIFF_ID = 11
MSISDN = "+79991111111"
MSISDN2 = "+79991111110"

@pytest.fixture(scope="module")
def e2e_setup():
    # Cleanup before test
    delete_brt_abonent(TEST_ABONENT_ID)
    delete_hrs_abonent(TEST_ABONENT_ID)

    # Create BRT abonent
    create_brt_abonent(
        abonent_id=TEST_ABONENT_ID,
        first_name="E2E",
        name="Test",
        msisdn=MSISDN,
        middle_name="User",
        balance=300.0
    )

    # Create HRS abonent
    create_hrs_abonent(
        abonent_id=TEST_ABONENT_ID,
        user_id=TEST_ABONENT_ID,
        tariff_id=TARIFF_ID,
        initial_in_minutes=50,
        initial_out_minutes=50
    )


    yield

    # Cleanup after test
    delete_brt_abonent(TEST_ABONENT_ID)
    delete_hrs_abonent(TEST_ABONENT_ID)


def test_e2e_outgoingcall_correction_test(e2e_setup):
    """Полный E2E тест: CDR, минуты, биллинг"""

    # Проверка существования файла CDR
    if not os.path.exists(cdr_path_outgoing):
        pytest.fail(f"Файл CDR не найден: {cdr_path_outgoing}")

    old_balance = get_brt_balance(TEST_ABONENT_ID)

    send_cdr_from_file(cdr_path_outgoing)
    time.sleep(10)

    new_balance = get_brt_balance(TEST_ABONENT_ID)
    assert new_balance == old_balance - 25, "Баланс обновлен странно"

def test_e2e_incomingcall_correct_test(e2e_setup):
    """Полный E2E тест: CDR, минуты, биллинг"""

    # Проверка существования файла CDR
    if not os.path.exists(cdr_path_incoming):
        pytest.fail(f"Файл CDR не найден: {cdr_path_incoming}")

    old_balance = get_brt_balance(TEST_ABONENT_ID)

    send_cdr_from_file(cdr_path_incoming)
    time.sleep(10)

    new_balance = get_brt_balance(TEST_ABONENT_ID)
    assert new_balance == old_balance - 25, "Баланс обновлен странно"
