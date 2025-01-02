<h1 align="center">Smart Agent Bot</h1>
<p align="center">
  Telegram-бот для поиска арендного жилья (квартир, домов, помещений).
</p>

<br>

## Обзор

Этот проект представляет собой телеграм-бота, разработанного на Java с использованием Spring Boot, который помогает пользователям находить подходящее арендное жилье. Бот позволяет фильтровать предложения по району и цене, облегчая поиск квартир, домов и других помещений в аренду.

<br>

## Структура Проекта


*   `config`: Содержит файлы конфигурации бота.
*   `model`: Содержит классы, представляющие данные (сущности), такие как `Apartments` и `User`.
*   `repository`: Интерфейсы для доступа к данным (репозитории), такие как `ApartmentsRepository` и `UserRepository`.
*   `service`: Сервисные классы, содержащие бизнес-логику приложения, такие как `ApartmentsService` и `UserService`.
*   `SpringDemoBotApplication.java`: Основной класс приложения, содержащий точку входа и логику обработки сообщений.
*    `docker-compose.yml`: Файл для запуска базы данных PostgreSQL в Docker.

<br>

## Особенности

*   **Поиск арендного жилья:** Бот помогает пользователям находить квартиры, дома и другие помещения в аренду.
*   **Фильтрация:** Поддерживает фильтрацию предложений по району и цене.
*   **Telegram Bot API:** Для взаимодействия с Telegram.
*   **Spring Boot:** Для управления приложением и обработки сообщений.
*   **PostgreSQL:** Для хранения данных.

<br>

## Зависимости

*   **Spring Boot**: Для создания веб-приложения и управления жизненным циклом бота.
*   **Telegram Bot API**: Для взаимодействия с Telegram.
*   **Spring Data JPA**: Для работы с базой данных.
*  **PostgresSQL Driver**
*  **Lombok**
<br>

## Запуск Приложения

### Предварительные требования

1.  Java Development Kit (JDK) 17 или выше.
2.  Maven (или Gradle).
3.  Созданный Telegram Bot и полученный токен для доступа к API.
4.  Docker и Docker Compose.
5.  Установленный Telegram.

### Шаги для запуска

1.  **Клонирование репозитория:**

    ```bash
    git clone https://github.com/BogdanPryadko4853/smartAgentBot
    cd smartAgentBot
    ```

2.  **Сборка проекта (с помощью Maven):**

    ```bash
    mvn clean install
    ```
     *или используйте Gradle*

3.  **Настройка токена бота:**
    *   Вам потребуется поместить токен вашего бота в файл `application.properties` или `application.yml`.
    *   Создайте файл `application.properties` в папке `src/main/resources` и добавьте строку:
     ```properties
      telegram.bot.token=YOUR_BOT_TOKEN
     ```
   * *Замените `YOUR_BOT_TOKEN` на токен, который вы получили от BotFather.*

4.  **Запуск с использованием Docker Compose (включает базу данных):**

    ```bash
    docker-compose up --build
    ```
   
  *  Приложение будет доступно через Telegram.

<br>

### Использование

1.  Найдите своего бота в Telegram, используя имя, которое вы дали боту при его создании.
2.  Начните диалог с ботом, отправив `/start` или любое другое сообщение.
3.  Следуйте инструкциям бота для фильтрации и поиска арендного жилья.

<br>

### Классы

*   `SpringDemoBotApplication.java`: Основной класс приложения, который обрабатывает сообщения от Telegram и взаимодействует с сервисами.
*   `Apartments`: Класс, представляющий данные об арендном жилье.
*   `User`: Класс, представляющий пользователя бота.
*   `ApartmentsService`: Класс, содержащий бизнес-логику для работы с предложениями аренды.
*   `UserService`: Класс, содержащий бизнес-логику для работы с пользователями.
<br>
## База Данных
PostgreSQL будет доступна на порту 7070

<br>