<h1>CDR сервис </h1>

Порт сервиса: 8081<br>
Доступ к бд h2: http://localhost:8081/h2-console/<br>
Логин: admin<br>
Пароль: (отсутствует)<br>
jdbc url: jdbc:h2:mem:testdb

<b>Важно: Перед запуском сервиса, не забудьте запустить Eurika Server! </b><br>
Порядок запуска:<br>
1. Eurika Server<br>
2. Amqp<br>
3. CdrService<br>


В CdrController есть только один метод: /cdr/generate
http://localhost:8081/cdr/generate -
Генерирует cdr и отправляет в очередь.

