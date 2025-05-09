import pytest
from utils.brt_interaction import *
from utils.hrs_interaction import *
from utils.rabbit_sender import *

send_cdr_from_file("..//IntegrationTests//brt//cdrs//cdr3.json")