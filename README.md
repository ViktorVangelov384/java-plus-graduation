## Explore With Me — Микросервисная архитектура
Платформа для поиска и посещения мероприятий.

## Содержание
Архитектура
Микросервисы
Инфраструктура
Конфигурация
Запуск проекта
Внутренний API
Внешний API
Технологический стек
Структура проекта

## Архитектура ##
                    ┌─────────────────┐
                        │   API Gateway   │ 
                        │   (Spring Cloud │
                        │    Gateway)     │
                        └────────┬────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
        ┌───────▼───────┐ ┌─────▼──────┐ ┌──────▼───────┐
        │ event-service │ │user-service│ │request-svc   │
        │               │ │            │ │              │
        │  Мероприятия  │ │Пользователи│ │   Заявки     │
        │  Категории    │ │            │ │              │
        │  Подборки     │ └────────────┘ └──────────────┘
        └───────┬───────┘                        ▲
                │                                │
        ┌───────▼───────┐                 ┌──────┴────────┐
        │ stats-server  │ ◄───────────────┤  Feign-клиенты│
        │               │   internal      │  (OpenFeign)  │
        │  Статистика   │   HTTP/REST     └───────────────┘
        └───────────────┘
                │
        ┌───────▼───────┐      ┌──────────────────┐
        │               │      │ Discovery Server │
        │   PostgreSQL  │      │    (Eureka)      │
        │   (maindb)    │      │   Порт: 8761     │
        └───────────────┘      └──────────────────┘
                                      ▲
                                      │
                              ┌───────┴────────┐
                              │ Config Server  │
                              │  Порт: 8888    │
                              └────────────────┘


## Ключевые принципы
API Gateway — единая точка входа для всех клиентов

Service Discovery (Eureka) — автоматическая регистрация и обнаружение сервисов

Config Server — централизованное управление конфигурацией

Изолированные схемы — каждый сервис работает со своей схемой в общей БД

OpenFeign — декларативные HTTP-клиенты для межсервисного взаимодействия

## Микросервисы
--event-service
* Назначение - Управление мероприятиями, категориями и подборками
* Схема БД- core_events
* Таблицы - categories, events, compilations, compilations_events
* Порт	- динамический (регистрация в Eureka)
-Функциональность:
CRUD мероприятий (создание, редактирование)
Публикация/отклонение мероприятий (админ)
Поиск и фильтрация мероприятий
Управление категориями и подборками
Подсчёт подтверждённых заявок (через request-service)
Подсчёт просмотров (через stats-server)

--user-service
* Назначение - Управление пользователями
* Схема БД - core_users
* Таблицы - users
* Порт	- динамический 
-Функциональность:
Регистрация пользователей (админ)
Просмотр списка пользователей
Удаление пользователей
* 
--request-service
* Назначение - Управление заявками на участие
* Схема БД - core_requests
* Таблицы - requests
* Порт - динамический
-Функциональность:
Создание и отмена заявок
Подтверждение/отклонение заявок (инициатор события)
Просмотр заявок участника и по событию
Подсчёт подтверждённых заявок (внутренний API)

--stats-server
* Назначение - Сбор статистики посещений
* БД - отдельная (statdb)
* Таблицы - stats
* Порт - динамический
-Функциональность:
Фиксация обращений к эндпоинтам
Получение статистики по URI
Фильтрация по уникальным IP

## Инфраструктура
gateway-server (порт 8080)
API Gateway на базе Spring Cloud Gateway.

## Префикс пути	                Микросервис	        Описание
/admin/events/**	            event-service	    Администрирование мероприятий
/users/*/events/**	            event-service	    Приватные мероприятия пользователя
/events/**	                    event-service	    Публичный поиск мероприятий
/admin/categories/**	        event-service	    Администрирование категорий
/categories/**	                event-service	    Публичные категории
/admin/compilations/**	        event-service	    Администрирование подборок
/compilations/**	            event-service	    Публичные подборки
/admin/users/**	                user-service	    Администрирование пользователей
/users/*/requests/**	        request-service	    Заявки пользователя
/users/*/events/*/requests/**	request-service	    Заявки на событие
/stats/**, /hit/**	            stats-server	    Статистика посещений


## discovery-server (порт 8761)
Eureka Server — сервис обнаружения.
URL панели: http://localhost:8761

## config-server (порт 8888)
Config Server — централизованная конфигурация.
Файлы конфигурации лежат в classpath:config/.

text
config-server/src/main/resources/config/
├── core/
│   ├── event-service.yaml
│   ├── user-service.yaml
│   └── request-service.yaml
├── stats/
│   └── stats-server.yaml
└── gateway/
└── gateway-server.yaml


## База данных
Контейнер	Порт	    БД	    Пользователь
ewm-db	    5432:5432	maindb	user/user
stats-db	5431:5432	statdb	user/user
* Схемы базы данных maindb
| core_events   |   event-service   |   categories, events, compilations, compilations_events
| core_users    |   user-service    |   users
core_requests	|   request-service	|   requests
Схема базы данных statdb
| public | stats-server	 | stats


 Структура таблиц
 ## core_users.users

Колонка	 |      Тип      | Описание
id	     |     BIGINT    | PK, автоинкремент
name	 | VARCHAR(255)	 | NOT NULL
email	 | VARCHAR(255)	 | UNIQUE, NOT NULL


 # core_events.categories
Колонка	   |    Тип	    | Описание
id	       |   BIGINT   | PK, автоинкремент
name	   | VARCHAR(50) |UNIQUE, NOT NULL


## core_events.events
Колонка	   Тип	Описание
id	                |     BIGINT    | PK, автоинкремент
annotation	        | VARCHAR(2000)	| NOT NULL
category_id	        |     BIGINT    | FK → categories(id)
confirmed_requests	|    INTEGER    | DEFAULT 0
created_on	        |   TIMESTAMP
description         |  VARCHAR(7000)
event_date	        |   TIMESTAMP   |NOT NULL
initiator_id	    |     BIGINT
lat	                |     DOUBLE
lon	                |     DOUBLE
paid	            |     BOOLEAN	 |DEFAULT FALSE
participant_limit	|     INTEGER    |DEFAULT 0
published_on	    |    TIMESTAMP
request_moderation	|    BOOLEAN     |DEFAULT TRUE
state	            |   VARCHAR(20)  |NOT NULL (PENDING/PUBLISHED/CANCELED)
title	            |   VARCHAR(120) | NOT NULL
views	            |    BIGINT      |DEFAULT 0



## core_events.compilations
Колонка	     |      Тип	      | Описание
id           |      BIGINT	  |PK, автоинкремент
title	     |   VARCHAR(50)  | UNIQUE, NOT NULL
pinned	     |    BOOLEAN     | DEFAULT FALSE


## core_events.compilations_events
Колонка	        |     Тип	    | Описание
compilation_id	|     BIGINT	|FK → compilations(id)
event_id	    |     BIGINT	| FK → events(id)
PK	            | (compilation_id, event_id) |составной ключ


## core_requests.requests
Колонка	        |     Тип	      | Описание
id	            |     BIGINT	  | PK, автоинкремент
created	        |    TIMESTAMP	  | NOT NULL
status	        |   VARCHAR(20)	  | NOT NULL (PENDING/CONFIRMED/REJECTED/CANCELED)
event_id	    |     BIGINT
requester_id	|     BIGINT


## public.stats (statdb)
Колонка	    |     Тип      | Описание
id	        |    BIGINT    | PK, автоинкремент
app	        | VARCHAR(100) | NOT NULL
uri	        | VARCHAR(300) | NOT NULL
ip	        | VARCHAR(45)  | NOT NULL
timestamp	|  TIMESTAMP   | NOT NULL


##  Запуск проекта
-Запуск баз данных
   docker-compose up -d
-Запуск инфраструктуры (по порядку)
# Discovery Server (Eureka)
java -jar infra/discovery-server/target/*.jar

# Config Server
java -jar infra/config-server/target/*.jar

# Core микросервисы
java -jar core/user-service/target/*.jar
java -jar core/event-service/target/*.jar
java -jar core/request-service/target/*.jar

# Stats Server
java -jar stats/stats-server/target/*.jar

# Gateway Server
java -jar infra/gateway-server/target/*.jar

Проверка
   Сервис	URL
   Eureka Dashboard	http://localhost:8761
   API Gateway	http://localhost:8080
   Внутренний API (межсервисное взаимодействие)
   Микросервисы взаимодействуют через Feign-клиенты. Контракты определены в модуле interaction-api.

## event-service → user-service (UserClient)
Метод    |          Путь	         |  Описание
GET	     | /internal/users/{id}	     |  Получить пользователя
POST	 | /internal/users/by-ids    |  Пакетное получение пользователей
GET	     | /internal/users/check/{id}| 	Проверить существование пользователя
## event-service → request-service (RequestClient)
Метод	|              Путь	                   | Описание
GET	    |  /internal/requests/count/{eventId}  |Количество подтверждённых заявок
POST	|  /internal/requests/count	           | Пакетный подсчёт заявок
## request-service → event-service (EventClient)
Метод	   |                 Путь	                   | Описание
GET	       | /internal/events/{eventId}	               | Получить событие для внутреннего использования
GET	       | /internal/events/check/{userId}/{eventId} |Проверить, что пользователь — инициатор
POST	   | /internal/events/confirmed-requests	   | Обновить счётчик подтверждённых заявок


## Общая ошибка
-- Feign-клиенты используют CustomErrorDecoder, преобразующий HTTP-ошибки в исключения:

  HTTP  | 	Исключение
  404   | NotFoundException
  400	| ValidationException
  409	 ConditionsNotMetException


## DTO для межсервисного взаимодействия
-- UserShortDto
Long id;
String name;
String email;

-- EventForRequestDto
Long id;
Long initiatorId;
Integer participantLimit;
EventState state;  // PENDING, PUBLISHED, CANCELED
Boolean requestModeration;


##  Внешний API
Метод	 |                     Эндпоинт	                  |Описание
GET	     |                  /categories	                  | Список категорий
GET	     |                   /events	                  | Поиск событий
GET	     |                 /events/{id}	                  | Событие по ID
GET	     |                 /compilations	              | Подборки событий
POST	 |           /users/{userId}/events	              | Создать событие
PATCH	 |      /users/{userId}/events/{eventId}          | Редактировать событие
POST	 |  /users/{userId}/requests?eventId={eventId}	  | Подать заявку
PATCH	 |  /users/{userId}/requests/{requestId}/cancel	  | Отменить заявку
PATCH	 |   /users/{userId}/events/{eventId}/requests	  |Подтвердить/отклонить заявки
GET	     |                /admin/users	                  |Список пользователей (админ)
POST	 |                /admin/users	                  |Создать пользователя (админ)
POST	 |              /admin/categories	              | Создать категорию (админ)
PATCH	 |             /admin/events/{eventId}	          |Опубликовать/отклонить событие (админ)


## Статистика
Метод	|  Эндпоинт	 |Описание
POST	|  /hit	     |Сохранить посещение
GET	    | /stats	 | Получить статистику
Базовый URL: http://localhost:8080

📚 Технологический стек
Категория	Технологии
Язык	Java 21
Фреймворк	Spring Boot 3.3.0, Spring Cloud 2023.0.x
Микросервисы	Spring Cloud Gateway, Eureka, Config Server, OpenFeign
БД	PostgreSQL
ORM	Spring Data JPA (Hibernate), QueryDSL
Кодогенерация	Lombok, MapStruct
Сборка	Maven
Контейнеризация	Docker Compose
📁 Структура проекта
text
explore-with-me/
├── pom.xml                             # Родительский POM
├── docker-compose.yml                  # Базы данных
│
├── infra/                              # Инфраструктура
│   ├── discovery-server/               # Eureka Server
│   ├── config-server/                  # Config Server
│   │   └── src/main/resources/config/  # Конфигурации
│   └── gateway-server/                 # API Gateway
│
├── core/                               # Основные микросервисы
│   ├── interaction-api/                # Feign-клиенты и DTO
│   ├── event-service/                  # Мероприятия
│   ├── user-service/                   # Пользователи
│   └── request-service/                # Заявки
│
└── stats/                              # Статистика
├── stat-client/                        # Клиент для stats-server
├── stat-dto/                           # DTO статистики
└── stat-server/                        # Сервер статистики=

