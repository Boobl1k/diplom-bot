# Mansur's DIS chat-dot

## Запуск программы

Необходимо установить переменную `BOT_TOKEN`, записать туда токен бота к Telegram API

Для запуска требуется установленный docker

При запуске приложение поднимает базу данных в докере, дополнительных настроек не требуется

## Развертывание программы

1. Установить docker 
2. Скопировать репозиторий или только файлы `docker-compose.yml` и 'env' в хост
3. Заполнить переменные в файле 'env'
4. `docker compose --env-file ./env up -d`

## Обновление программы

1. Установить нужный логин docker hub в файле `docker-compose.yml` (вместо mansoooor)
2. `docker compose build`
3. `docker compose push`
4. На сервере обновить файл `docker-compose.yml`
5. На сервере `docker compose pull`
6. На сервере `docker compose up`