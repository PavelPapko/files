# Находясь в корне проекта, выполните:

# Сборка образов
docker-compose build

# Запуск всех сервисов в фоне
docker-compose up -d

# Просмотр логов в реальном времени
docker-compose logs -f

      POSTGRES_DB: files_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres