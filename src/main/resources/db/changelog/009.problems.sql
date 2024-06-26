ALTER TABLE problem ALTER COLUMN solution_text TYPE VARCHAR(1023);


INSERT INTO problem (dis_problem_id, condition, description, solution_text, solution_picture)
VALUES (121, 'Не сотруднику нужна учетка в AD', null,
        'Например, приглашенному лектору для доступа к мультимедийной трибуне. Принимаем заявку только при наличии письменной формы заявки от действующего сотрудника с обязательным указанием даты до которой открывается доступ и обязательным обоснованием открытия доступа.',
        null),
       (22, 'искажения текста в пдф файле РПД или он отсутствует', null, e'Нужно создать заявку. В заявке необходимо указывать:
- полное название программы,
- институт,
- специальность,
- направление,
- тип обучения,
- год,
- описание проблемы', null),
       (22, 'Отсутствуеет кнопка редактирования страницы портала', null, e'1) Убедитесь что вы вошли в свой личный кабинет
2) Убедитесь что вы на той странице, которую хотите редактировать
3) Если браузер старой версии, обновите
4) Выключите расширение для блокировке рекламы
5) Почистите кэш браузера', null),
       (22, 'Преподаватель не видит своей РПД/РПК', null,
        'РПД и РПК заводятся сотрудником соответствующей кафедры. Если преподаватель не видит своей программы, значит программу на него неправильно завели. Он должен узнать на кафедре кто там отвечает за оформление программ и сказать, чтобы ее завели правильно. Напр., иногда имя преподавателя указывают с опечаткой и т.д. ',
        null),
       (22, 'При оформлении командировки в списке отсутствует нужный город', null,
        'Заявителю нужно обратиться в управление кадров - отд. по работе с профессорско-преподавательским составом. Орлова Лиана Ринатовна. Тел. 2337110. Заявку заводить не нужно',
        null),
       (87, 'Не удается войти в ПП', null, e'Работа в ПП "Парус" невозможна, т.к. превышено максимальное количество подключений к приложению которое пользователь собирается использовать.
Если при попытке войти в ПП "Парус" в приложение выходит сообщение, что превышено максимальное количество подключений, то это означает, что к данному приложению подключено больше пользователя, что превышает количество лицензий. В таком случае необходимо кому-то из пользователей выйти из приложения, чтобы данный пользователь, у которого вышло сообщение, что превышено максимальное количество подключений, смог зайти в данное приложение. 
Количество лицензий на приложения:
Кадры и штатное расписание - 21
Расчет заработной платы - 19
Бухгалтерский учет - 52', null),
       (100, 'Подключение студентов к электронным образовательным площадкам', null,
        'Заявителю необходимо обратиться в Отдел дистанционного образования по адресу ул. Кремлевская 35 (Корпус №2), каб. 304, Ахмерова Эльвира Хатифовна, тел. 233-74-26. ',
        null),
       (30, 'Не включается компьютер', null,
        'Проверить включен ли сетевой фильтр. Проверить воткнут ли шнур питания в розетку или сетевой фильтр. Проверить кнопку включения на блоке питания. Если есть источник бесперебойного питания, уточнить, подключен ли он кабелем к сети и запитан',
        null),
       (30, 'Отсутствует изображение на мониторе', null, e'1. Нажать кнопку питания на мониторе.Оценить работу индикатора включения.
2. Проверить подключен ли кабель питания к монитору и источнику питания(розетке).
3. Проверить включен ли сетевой фильтр.', null),
       (30, 'Отсутствует видеосигнал на монитор', null, e'1. Проверить подключен ли видео-кабель от монитора к видеокарте(либо встроенной видеокарте на самой мат. плате).
2. Вынуть видео кабель из разъема и проверить кабель и разъем на наличие физических повреждений(согнутые контакты, сильные перегибы).
3. Воткнуть кабель обратно. ', null),
       (30, 'Не включается принтер, мфу', null,
        'Проверить тумблер питания на задней или боковой части устройства, проверить подключен ли шнур питания к устройству, поключен ли он к розетке(или сетевому фильтру), включен ли сетевой фильтр',
        null),
       (30, 'Принтер или мфу зажевал бумагу', null, e'1. Выключить принтер.
2. Открыть крышку корпуса принтера.
3. Открыть переднюю крышку корпуса, аккуратно вытащить (если возможно) лист бумаги, если к нему доступа нет, то открыть заднюю крышку и произвести те же действия.
4. Закрыть корпус устройства.
5. Включить устройство. ', null),
       (105, 'ПК перезагружается при работе/включении', null, e'1) При включении ПК нажать F8 и из меню выбрать пункт "Загрузка последней удачной конфигурации"
2) Если не помогло, из этого же меню вызвать Безопасный режим и проверить диски на ошибки, вирусы из безопасного режима 
3) Если не помогает, то завести заявку, при возможности записать код ошибки.
', null),
       (105, 'При загрузке Windows система вылетает в синий экран( BSoD)', null, e'1) При включении ПК нажать F8 и из меню выбрать пункт "Загрузка последней удачной конфигурации"
2) Если не помогло, из этого же меню вызвать Безопасный режим и проверить диски на ошибки, вирусы из безопасного режима
3) Если не помогает, то завести заявку, при возможности записать код ошибки.', null),
       (108, 'Открыть общий доступ к данным из личного профиля', null,
        'Необходимо создать папку непосредственно на жестком диске (C, D, и др.) и перенести туда информацию предназначенную для открытого доступа другим пользователям. ',
        null),
       (108, 'Удаление сертификата КФУ', null,
        'Для удаление сертификата КФУ требуется нажать сочетание клавиш "Windows+R" (или воспользоваться командой "Выполнить") и ввести certmgr.msc, нажать "Ок". В открывшемся окне проследовать по пути [Сертификаты - текущий пользователь\Личное\Сертификаты], выделить требуемый сертификат, нажать ПКМ и выбрать "Удалить".',
        null),
       (80, 'Internet Explorer не отображает веб-страницы, проблема с отображением страниц в IE', null, e'1) Необходимо в свойства обозревателя (Нажать Alt- Сервис-Св-ва обозревателя) и выбрать вкладку "Подключения"
2) Нажать на кнопку "Настройка сети"/"Настройка LAN"
3) В появившемся окне нажать "Дополнительно" и в поле исключений дописать через точку с запятой следующие сайты:
- *ksu.ru
- *kpfu.ru', null),
       (80, 'В Teams в Chrome нет кнопки демонстрации экрана', null,
        'Отключить приложение Adblock Plus (Проверено на версии Chrome 87.0.4280.88 -64 бит). Или использовать Opera, Edge',
        null),
       (80, 'В WhatsApp Web (https://web.whatsapp.com/) не отображается QR-код в Firefox ', null,
        'Если в "Параметрах соединения" выбрана "Ручная настройка прокси" и в поле "Узел SOCKS" прописан адрес прокси-сервера и порт, то необходимо сбросить настройки браузера Mozilla Firefox к заводским. Для этого необходимо нажать на "Открыть меню" - Справка - Перезапустить без дополнений - Перезапуститься и в открывшемся окне выбрать "Очистить Firefox".',
        null),
       (80, 'Не отображается кнопка "Вход" в ЭУ на сайте kpfu.ru', null, e'Необходимо в свойства обозревателя (Нажать Alt- Сервис-Св-ва обозревателя) и выбрать вкладку "Безопасность". Щелкнуть ЛКМ по кнопке "Надежные сайты", Нажать кнопку "Сайты". Убрать галочку внизу окна и добавить в зону след. узлы :
- *ksu.ru
- *kpfu.ru
И закрыть окна.', null),
       (80, 'При доступе на ex.kpfu.ru ошибка PR_CONNECT_RESET_ERROR', null,
        'В Firefox x86 нет кнопки "Включить TLS 1.0 и 1.1". В about:config найти "security.tls.version.enable-deprecated", установить "True".',
        null),
       (81, 'Установить программное обеспечение', null, e'1) Если ПО имеется у самого заказчика, проверить, не является ли пользователь локальным администратором. Если является, предложить пользователю установить ПО самому
2) Если необходимо установить стандартное ПО, то уточнить, какая ОС стоит на ПК, и завести заявку.', null),
       (82, 'В Internet Explorer некорректно загружается сайт kpfu.ru', null, e'Почистить кеш браузера.
Проверить версию браузера. Обновиться до IE 11, если текущая версия меньше.
Проверить не включен ли режим совместимости: Настройки -> параметры просмотра в режиме совместимости.
В списке сайтов не должно быть kpfu.ru. Если есть - удалить.
Также убрать галочку с "Отображать сайты интрасети в режиме совместимости".', null),
       (16, 'Отсутствует подключение к прокси', null, e'Если при попытке зайти на любой сайт пользователь получает сообщение о том, что прокси сервер не доступен и не получает DNS (проверяется через сведения о подключении), необходимо:
1. Нажать Windows + R или "Пуск" - "Выполнить", затем в окне "Выполнить" ввести "cmd", нажать Enter.
2. В окне "Командная строка" написать "ipconfig /renew", нажать Enter.
3. Проверить подключение к сети.', null),
       (16, 'Устранение неполадок подключения к Интернету', null, e'Запустите программу "Диагностика сети", щелкнув правой кнопкой мыши значок сети в области уведомлений и выбрав команду Диагностика и восстановление.
Убедитесь, что все кабели подключены (например, модем должен быть соединен с исправной телефонной розеткой или подключен с помощью кабеля - напрямую или через маршрутизатор).
Сбросьте параметры модема и маршрутизатора. Отсоедините шнур питания от модема и/или маршрутизатора, подождите не менее 10 секунд, а затем снова подключите модем и/или маршрутизатор.
Проверьте маршрутизатор.', null),
       (17, 'Google Scholar (Академия)', null,
        'Открыть новую вкладку браузера в режиме инкогнито, либо предварительно выйти из аккаунта Google. Перейти на сайт https://scholar.google.ru. В правом верхнем углу нажать на "Войти". На следующем этапе внизу центрального окошечка нажать на "Создать аккаунт". На следующем этапе нажать на "Использовать текущий адрес электронной почты", заполнить все поля и нажать кнопку "Далее". На следующем этапе необходимо ввести код, который должен быть отправлен на указанную корпоративную почту.',
        null),
       (17, 'При заходе на Scopus и т.п. сайты запрашивает пароль на прокси-сервер http://squid2.kpfu.ru:8080', null, e'Для доступа к подписным ресурсам библиотеки необходимо:
Настроить VPN подключение по инструкции https://shelly.kpfu.ru/e-ksu/docs/F943017969/VPN.pdf
или выполнить следующие настройки:
1) В "Панели управления" - "Свойства браузера" на вкладке "Подключения" нажать кнопку "Настройка сети" и поставить "галочку" на пункте "Использовать сценарий автоматической настройки" (остальные галочки снять).
В поле адрес ввести libres.kpfu.ru\\wpad.dat либо libress.kpfu.ru\\wpad.dat
2) Использовать браузер Mozilla Firefox. При запросе логина и пароля на прокси-сервер squid2.kpfu.ru:8080 следует вводить логин в формате "int\\логин" (или "stud\\логин") и пароль от корпоративной сети.',
        null),
       (17, 'У пользователя постоянно разрывается сессия при работе на сайте КФУ', null,
        'Провайдер выдает динамический IP. Рекомендовать настроить VPN. Или сменить провайдера.', null),
       (19, 'Бесконечная авторизация в браузере Google Chrome на proxy.int.kpfu.ru', null, e'Пуск\\Панель управления\\Учетные записи пользователей\\Диспетчер учетных данных\\Учетные данные Windows. Нажимаем ссылку "Добавить учетные данные Windows".
В поле "Адрес в Интернете или сети" вбиваем proxy.int.kpfu.ru.
В поле "Имя пользователя" вбиваем логин для входа в корпоративную сеть в формате login@int.kpfu.ru.
В поле "Пароль" вбиваем пароль для входа в корпоративную сеть (базовый пароль).', null),
       (19, 'Проблемы с подключением VPN, получением IP-адреса внутренней сети КФУ на Windows 10', null,
        'Необходимо использовать следующие настройки: Имя подключения: vpn.kpfu.ru, Имя или адрес сервера: vpn.kpfu.ru, Тип VPN: L2TP/IPsec с предварительным ключом, Общий ключ: vpn.kpfu.ru, Имя пользователя (необязательно): int\login, Пароль (необязательно): password. После создания VPN-подключения необходимо зайти в раздел Панель управления\Сеть и Интернет\Сетевые подключения\Изменения параметров адаптера. На иконке VPN-подключения нажать правой кнопкой мыши, из выпадающего списка выбрать "Свойства". В открывшемся окне перейти на вкладку "Сеть", выбрать "IP версии 5 (TCP/Ipv4)" и нажать ниже на кнопку "Свойства". В открывшемся окне нажать на кнопку "Дополнительно...". В открывшемся окне во вкладке "Параметры IP" поставить галочку напротив пункта "Использовать шлюз в удаленной сети", если она отсутствует.',
        null),
       (125, 'Браузер Google Chrome, не прикрепляются файлы в Outlook Web App', null,
        'Необходимо установить расширение для браузера Google Chrome. Настройки\Расширения внизу окна нажимаем ссылку "Ещё расширения". В строке "Поиск по магазину" вводим showModalDialog shim и устанавливаем его. После проделанного перезапустите браузер и всё должно работать.',
        null),
       (125, 'Восстановление писем эл. почты', null,
        'Ошибочно удалено письмо и очищена папка Удаленные. Для восстановления необходимо: 1. MS Outlook: на верхней панели перейти во вкладку Папка, выбрать Восстановить удаленный элемент, в открывшемся окне найти и выделить элемент требующий восстановления, нажать Восстановить выбранные элементы. 2. Outlook WebApp: нажать правой клавишей мыши по папке Удаленные, выбрать в выпадающем меню Восстановить удаленные элементы, в открывшемся окне выделить элементы требующие восстановления, нажать на иконку Восстановить выбранные элементы',
        null),
       (125, 'Почта переполнена сообщениями типа Mail Delivery... и подобными', null, e'Признак того, что почта была взломана и использована для спам-рассылки.
1) Поменять пароль.
2) Настроить правило обработки почты, чтобы эти сообщения попадали сразу в удаленные;
3) Отключить все другие неизвестные владельцу ящика правила обработки почты;
4) Выяснить не переходил ли заявитель по странным ссылкам, не вводил ли там свои логин-пароль;
5) Желательно проверить ПК антивирусом - согласовать с заявителем и завести соответствующую заявку.', null),
       (125, 'Рассылка почтовых сообщений от имени пользователя почтового домена @kpfu.ru', null, e'1. ДИС не предоставляет услуг по рассылке почтовых сообщений.
2. Рассылка почтовых сообщений от имени пользователя почтового домена @kpfu.ru может быть произведена пользователем самостоятельно. При массовой рассылке писем необходимо учитывать, что есть ограничение на количество исходящих писем - не более 50 в сутки. 
3. Использование нестандартного формата HTML для писем не запрещено, но настраивается пользователем самостоятельно. 
4. Для массовых рассылок настоятельно рекомендуется использование специализированных сервисов.', null),
       (126, 'Студент просит создать алиас', null,
        'Отказать. Студентам алиасы почты не создаем. При смене фамилии почта остается старой', null),
       (12, 'Бывший сотрудник просит разблокировать учетку в AD', null,
        'Увольнявшийся и вновь принятый сотрудник просит открыть доступ в его заблокированную учетку (до офиц. оформления на работу). Заводим заявку только от действующего сотрудника, только при наличии письменной формы заявки с обязательным указанием срока до которого активируется учетка и обязательным обоснованием для разблокировки.',
        null),
       (15, 'Просьба предоставить администраторские права на ПК', null,
        'Для предоставления прав администратора на ПК требуется письменная заявка с указанием имени компьютера и пользователя, которому открывают админ. права. Заявка должна быть за подписью руководителя первого уровня. Шаблон заявки можно скачать по ссылке http://kpfu.ru/portal/docs/F1635006149/Obrazec.pismennoj.zayavki.docx',
        null),
       (101, 'Переносы курсов на do.kpfu.ru и с do на edu.kpfu.ru', null,
        'Заявителю необходимо обратиться в Отдел дистанционного образования по адресу ул. Кремлевская 35 (Корпус №2), каб. 304, Ахмерова Эльвира Хатифовна, тел. 233-74-26',
        null),
       (119, 'Кремлевская 29. Адреса нет в каталоге', null, e'Новый сотрудник сомневается в точном адресе.
Имеется в виду Лобачевского 1/29 - Старый корпус института химии. Новый корпус - Кремлевская 27. В старом корпусе 4 этажа, в новом 6 + 1 подвальный.',
        null),
       (115, 'Открытие доступа на междугороднюю/международную связь', null, e'Для подключения телефонного номера к услуге международной, междугородной и внутризоновой телефонной связи необходимо подать заявку установленной формы:
Приложение к документу "Порядок предоставления услуг международной, междугородной и внутризоновой телефонной связи для сотрудников КФУ", ссылка на скачивание: 
http://kpfu.ru/portal/docs/F1033047049/ZAJAVKA_MG_MN.pdf
Если в заявке просят провести только IP-телефон, то можно принимать без письменной формы. ', null),
       (37, 'Куда обращаться по ремонту техники', null, e'По ремонту компьютерной техники и периферийных устройств обращаться в сервисный центр:
http://remik.ru/
225-30-80
89173910396
Дмитрий Дериглазов', null);
