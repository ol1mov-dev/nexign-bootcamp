import pytest
from utils.brt_interaction import *
from utils.hrs_interaction import *


set_hrs_outgoing_minutes(88888, 10)
print(get_hrs_outgoing_minutes(88888))
