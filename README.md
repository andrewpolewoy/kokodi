# Кокоди - Карточная игра

## Описание
Микросервис для карточной игры «Кокоди», где игроки собирают очки, разыгрывая карты. Поддерживает от 2 до 4 игроков.

## Технологии
- Kotlin 1.9.22
- Spring Boot 3.2.3
- PostgreSQL
- JWT для авторизации
- Docker
- OpenAPI/Swagger для документации

## Требования
- JDK 21
- Docker и Docker Compose
- PostgreSQL (или Docker)

## Быстрый старт

1. Клонируйте репозиторий:
```bash
git clone https://github.com/andrewpolewoy/kokodi.git
cd kokodi
```

2. Запустите базу данных:
```bash
docker-compose up -d postgres
```

3. Запустите приложение:
```bash
./gradlew bootRun
```

Приложение будет доступно по адресу: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

## API Endpoints

### Аутентификация
- POST /api/auth/register - Регистрация нового пользователя
- POST /api/auth/login - Вход в систему (получение JWT токена)

### Игровые endpoints
- POST /api/games - Создать новую игру
- POST /api/games/{id}/join - Присоединиться к игре
- POST /api/games/{id}/start - Начать игру
- POST /api/games/{id}/turn - Сделать ход
- GET /api/games/{id} - Получить состояние игры
- GET /api/games - Получить список игр пользователя

## Правила игры

### Типы карт
1. Points Card
   - Добавляет игроку указанное количество очков

2. Action Card
   - Block: следующий игрок пропускает ход
   - Steal: кража N очков у выбранного соперника
   - DoubleDown: удвоение текущих очков игрока (максимум до 30)

### Условия победы
- Игрок побеждает при достижении 30 очков
- Игра заканчивается, когда заканчиваются карты в колоде

## Разработка

### Сборка проекта
```bash
./gradlew build
```

### Запуск тестов
```bash
./gradlew test
```

### Docker сборка
```bash
docker-compose up --build
```

## Примеры использования API

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{
    "login": "player1",
    "password": "password",
    "name": "Player One"
}'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{
    "login": "player1",
    "password": "password"
}'
```

### Создание игры
```bash
curl -X POST http://localhost:8080/api/games \
-H "Authorization: Bearer YOUR_TOKEN" \
-H "Content-Type: application/json"
```
## API Тестирование

### Swagger UI
Документация API доступна через Swagger UI:
- **URL**: `http://localhost:8080/swagger-ui.html`  
  Используйте Swagger для интерактивного тестирования эндпоинтов.

### Postman Коллекция
Для тестирования API предоставлена Postman коллекция. Найдите её в папке `docs/`:
- **Файл**: `docs/kokodi.postman_collection.json`

#### Как использовать Postman коллекцию:
1. Откройте Postman.
2. Нажмите **"Import"** → выберите файл `docs/kokodi.postman_collection.json`.
3. (Опционально) Импортируйте файл окружения `docs/KokodiEnvironment.postman_environment.json`, если он есть.
4. Настройте переменную `baseUrl` (например, `http://localhost:8080`) в окружении Postman.
5. Используйте запросы из коллекции для взаимодействия с API.