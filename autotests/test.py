import pytest
from utils.brt_interaction import *
from utils.hrs_interaction import *

abonent_id = 999999
user_id = 123
tariff_id = 1
initial_in = 10
initial_out = 20
create_hrs_abonent(abonent_id, user_id, tariff_id, initial_in, initial_out)

print(get_call_history(3))
# print(get_tariff_cost_details(3))