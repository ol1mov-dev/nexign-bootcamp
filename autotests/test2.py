from utils.brt_interaction import create_brt_abonent, create_call, delete_brt_abonent
from datetime import datetime
from utils.hrs_interaction import *
import json
import os

create_brt_abonent(
                abonent_id="кот",
                first_name="Тест",
                name="Тестович",
                msisdn="кот",
                last_name="Тестовый",
                balance="кот"
            )