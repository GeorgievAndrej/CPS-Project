# CPS — Classroom Presence System

Систем за евидентирање на присуство на студенти преку NFC технологија

---

# Содржина

- [Преглед](#преглед)
- [Архитектура](#архитектура)
- [Компоненти](#компоненти)
- [Барања](#барања)
- [Инсталација и подесување](#инсталација-и-подесување)
- [Користење](#користење)
- [API документација](#api-документација)
- [База на податоци](#база-на-податоци)
- [Тим](#тим)

---

## Преглед

CPS (Classroom Presence System) е систем кој им овозможува на наставниците брзо и безжично да ја евидентираат присутноста на студентите во текот на час. Студентот го приближува телефонот до телефонот на наставникот — системот автоматски го регистрира присуството, го зачувува локално и го синхронизира со серверот.

---

## Архитектура

| Слој | Технологија | Опис |
|------|-------------|------|
| StudentApp | Android (Kotlin + Jetpack Compose) | HCE емулација на NFC картичка |
| TeacherApp | Android (Java + MVVM) | NFC читач, локална база, sync |
| Backend | PHP + MySQL (XAMPP) | REST API, JWT автентикација |
| Dashboard | HTML + JavaScript + Chart.js | Веб интерфејс за статистики |

**NFC протокол:** ISO 7816-4 APDU преку Android Host-based Card Emulation (HCE)

---

## Компоненти

### 📱 StudentApp
Апликација за студенти која го емулира NFC паметна картичка. По логирање, студентот само го приближува телефонот до телефонот на наставникот.

**Клучни фајлови:**
```
StudentApp/app/src/main/java/com/example/studentapp/
├── MainActivity.kt           # Login UI + реален API повик
├── MyHostApduService.kt      # HCE сервис — испраќа JSON преку APDU
└── ui/theme/                 # Jetpack Compose тема
```

### 📱 TeacherApp
Апликација за наставници која ги чита NFC сигналите од студентите, ги зачувува локално (Room) и ги синхронизира со серверот.

**Клучни фајлови:**
```
TeacherApp/app/src/main/java/com/cps/teacherapp/
├── MainActivity.java                    # NFC читање (IsoDep/APDU)
├── LoginActivity.java                   # Автентикација
├── data/
│   ├── local/AppDatabase.java           # Room база
│   ├── local/AttendanceDao.java         # DAO операции
│   ├── model/AttendanceRecord.java      # Entity модел
│   └── sync/SyncCallback.java          # Sync callback интерфејс
├── network/
│   ├── ApiService.java                  # Retrofit интерфејс
│   ├── RetrofitClient.java              # HTTP клиент
│   └── SyncRequestBody.java            # Payload маппер
├── repository/AttendanceRepository.java # Data layer
├── viewmodel/AttendanceViewModel.java   # MVVM ViewModel
├── ui/adapter/AttendanceAdapter.java    # RecyclerView адаптер
└── storage/TokenManager.java           # JWT зачувување
```

### Backend (cps/)
REST API напишан во PHP со JWT автентикација и MySQL база.

```
cps/
├── api/
│   ├── login.php        # POST /api/login.php
│   ├── attendance.php   # GET/POST /api/attendance.php
│   └── statistics.php   # GET /api/statistics.php
├── config/
│   └── database.php     # PDO конекција
├── helpers/
│   └── jwt.php          # JWT encode/decode
├── models/
│   ├── user.php         # User модел
│   └── attendance.php   # Attendance модел
└── index.html           # Web Dashboard
```

---

## Барања

### За развој
- Android Studio Hedgehog или понов
- JDK 11+
- XAMPP (Apache + MySQL)
- PHP 7.4+
- MySQL 5.7+

### За телефони
| Уред | Барање |
|------|--------|
| TeacherApp | Android 8.0+, NFC |
| StudentApp | Android 8.0+, NFC со поддршка за HCE |

### Мрежа
Сите уреди (двата телефона + компјутер со XAMPP) мораат да бидат на **иста WiFi мрежа**.

---

## Инсталација и подесување

### 1. База на податоци

Отвори phpMyAdmin (`http://localhost/phpmyadmin`) и изврши:

```sql
CREATE DATABASE cps_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cps_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin', 'teacher', 'student') NOT NULL,
    student_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    teacher_id INT,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_external_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    course_id INT NOT NULL,
    tapped_at DATETIME NOT NULL,
    session_id VARCHAR(20),
    teacher_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Почетни податоци
INSERT INTO users (username, password, full_name, role, student_id) VALUES
('admin',      '$2y$10$...', 'Administrator',   'admin',   NULL),
('profesor',   '$2y$10$...', 'Проф. Јованова',  'teacher', NULL),
('student001', '$2y$10$...', 'Ана Петровска',   'student', 'S001'),
('student002', '$2y$10$...', 'Марко Николов',   'student', 'S002');

-- Лозинка за сите е: password (bcrypt hash)

INSERT INTO courses (name, code, teacher_id) VALUES
('Mathematics 101', 'MATH101', 2),
('Computer Science', 'CS101',  2);
```

### 2. Backend

```bash
# Клонирај го репото
git clone https://github.com/GeorgievAndrej/CPS-Project.git

# Копирај ја cps/ папката во XAMPP
cp -r CPS-Project/cps/ C:/xampp/htdocs/cps/
```

Отвори `cps/config/database.php` и постави ги credentials:
```php
private $host     = "localhost";
private $db_name  = "cps_db";
private $username = "root";
private $password = "";        // Празно за XAMPP default
```

Стартај XAMPP → Apache + MySQL.

Тест:
```
http://localhost/cps/api/login.php
# Очекуван одговор: {"error":"Username and password required"}
```

### 3. Android апликации

**Важно:** Пред build, постави го IP-то на компјутерот во двете апликации.

Најди го IP-то:
```bash
# Windows
ipconfig
# Барај IPv4 под Wi-Fi адаптерот
```

**TeacherApp** — `RetrofitClient.java`:
```java
private static final String BASE_URL = "http://YOUR_IP/cps/";
```

**StudentApp** — `MainActivity.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_IP/cps/"
```

**TeacherApp** — `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">YOUR_IP</domain>
    </domain-config>
</network-security-config>
```

Build и инсталирај:
```bash
# Поврзи го телефонот со USB (USB Debugging вклучено)
adb install app/build/outputs/apk/debug/app-debug.apk

# Ако апликацијата веќе постои
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. NFC поставки

**Телефон со TeacherApp:**
- Поставки → NFC → Основен метод → **Оперативен систем Android**

**Телефон со StudentApp:**
- Поставки → Безконтактни плаќања → **Others** → избери **Student App**

---

## Користење

### Наставник
1. Отвори TeacherApp → логирај се (`profesor` / `password`)
2. Екранот покажува " NFC слуша..." — сесијата е активна
3. Секој студент го приближува телефонот → се pojавува ime + вибрација
4. По завршување на час → "Синхронизирај со сервер"

### Студент
1. Отвори StudentApp → логирај се (`student001` / `password`)
2. Екранот покажува "Ready for Tap"
3. Приближи го задниот дел на телефонот до телефонот на наставникот

### Web Dashboard
Отвори во прелистувач:
```
http://YOUR_IP/cps/index.html
```
Логирај се со admin или teacher акаунт за да ги гледаш статистиките и записите.

---

## API документација

### POST `/api/login.php`
```json
// Request
{ "username": "profesor", "password": "password" }

// Response 200
{
  "access_token": "eyJ...",
  "user": {
    "id": 2,
    "full_name": "Проф. Јованова",
    "role": "teacher"
  }
}
```

### POST `/api/attendance.php`
```
Authorization: Bearer {token}
```
```json
// Request
{
  "records": [{
    "student_external_id": "S001",
    "student_name": "Ана Петровска",
    "course_id": 1,
    "tapped_at": "2024-01-15 10:30:00",
    "session_id": "ABC123"
  }]
}

// Response 201
{ "received": 1, "inserted": 1, "duplicates": 0 }
```

### GET `/api/statistics.php`
```
Authorization: Bearer {token}
```
```json
// Response 200
{
  "course_stats": [
    { "name": "Mathematics 101", "code": "MATH101", "total_records": 45, "unique_students": 12, "total_sessions": 5 }
  ],
  "daily_trend": [
    { "date": "2024-01-15", "count": 23 }
  ]
}
```

---

## База на податоци

```
users
├── id, username, password (bcrypt)
├── full_name, role (admin/teacher/student)
└── student_id (само за студенти: S001, S002...)

courses
├── id, name, code
└── teacher_id → users.id

attendance
├── id, student_external_id, student_name
├── course_id → courses.id
├── tapped_at, session_id
└── teacher_id → users.id
```

---

## Тим

| Член | Index |
|------|-----------|
| Radmila Lazarova | 102758 |
| Andrej Georgiev | 102737 |
| Kristijan Pelivanov | 102748 |

---

## Забелешки за развој

- IP адресата во BASE_URL **мора да се ажурира** при секоја промена на мрежа
- Android блокира HTTP по default — `network_security_config.xml` е задолжителен
- HCE работи само кога екранот е **вклучен и отклучен**
- NFC методот на TeacherApp телефонот мора да биде **Оперативен систем Android**
- StudentApp мора да биде поставена како default во **Others** под Безконтактни плаќања
