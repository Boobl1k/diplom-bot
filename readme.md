# Mansur's DIS chat-dot

## Переменные

* BOT_TOKEN - api токен телеграм бота
* KFU_URL - url до КФУ. Например https://portal-dis.kpfu.ru/ (тестовая среда)
* KFU_SUB_PATH - uri до апи чат-ботов в КФУ. Например pls/tech_center (в тестовой среде, предположительно такой же будет в релизе)
* KFU_ACCESS_KEY - параметр p_access_key в запросах в КФУ используемый для авторизации

## Запуск программы

Необходимо установить [переменные](#переменные) среды

Для запуска требуется установленный docker

При запуске приложение поднимает базу данных в докере, дополнительных настроек не требуется

## Развертывание программы

1. Установить docker 
2. Скопировать репозиторий или только файлы `docker-compose.yml` и `env` в хост
3. Заполнить переменные в файле `env`
4. `docker compose --env-file ./env up -d`

## Обновление программы

1. Установить нужный логин docker hub в файле `docker-compose.yml` (вместо mansoooor)
2. `docker compose build`
3. `docker compose push`
4. На сервере обновить файл `docker-compose.yml`
5. На сервере `docker compose pull`
6. На сервере `docker compose --env-file ./env up -d`
