# StockSense

> **Smarter Stock. Better Business.**

StockSense is an inventory intelligence and point-of-sale platform engineered for retail grocery stores and neighbourhood supermarkets.  
It unifies real-time stock tracking, barcode billing, automated spreadsheet ingestion, intelligent purchase reordering, and basket analytics into an intuitive mobile interface.  
Powered by the bilingual Dukaan Mitra AI assistant, StockSense prevents stockouts, cuts dead inventory, and optimizes store working capital.

---

## Features

- **Authentication & Multi-Tenant Isolation**: Secure store owner signup and login, BCrypt password hashing, 64-character hexadecimal Bearer token session management, rate-limited login defense, and session revocation.
- **Home Dashboard**: Real-time overview of total inventory valuation, active low-stock alerts, category stock distribution, daily revenue metrics, and quick operational shortcuts.
- **Inventory Management**: Searchable and filterable product catalog supporting instant queries, category filtering, multi-field sorting (name, price, stock units, recency), barcode/SKU tracking, and unit increment/decrement controls.
- **Product Details & CRUD**: Comprehensive stock inspection displaying unit counts, cost price, selling price, calculated gross margin, supplier details, restock thresholds, and product catalog icon assignments.
- **CSV & XLSX Bulk Import**: Intelligent spreadsheet ingestion featuring automatic column header detection, data mapping preview, dry-run validation with auto-fixing suggestions, zero-stock product creation, and atomic idempotent commit.
- **Point of Sale (POS) Billing**: Fast multi-item checkout, real-time stock availability verification, and immediate atomic inventory decrement upon bill completion.
- **Sales Analytics & History**: Transaction logs, period revenue tracking, top-selling product ranking, and hourly store foot-traffic trends across customizable timeframes.
- **Reorder & Purchase Orders**: Automated restock recommendations factoring in current inventory and incoming orders, automatic purchase order generation (`PO-YYYY-NNN`), order status management, one-tap stock intake, and safe delete/cancel enforcement.
- **Retail Insights & Market Basket Analysis**: Data-driven recommendations including slow-moving stock clearance alerts with margin protection, market basket paired-item association rules (lift > 1.0), and decision audit history.
- **Proactive Notifications & Weekly Digest**: Proactive alerts for low-stock thresholds and purchase order updates delivered via Firebase Cloud Messaging (FCM HTTP v1), an in-app notification center, and automated weekly email digests.
- **Dukaan Mitra AI Assistant with Voice**: Built-in retail business co-pilot supporting bilingual conversational queries (English, Hindi, Tamil, Hinglish, Tanglish), voice input via `SpeechRecognizer`, speech synthesis playback via `TextToSpeech`, grounded shop metric tool calling (including stock valuation `stock_value`), retail business strategy advice, and resilient deterministic fallback.
- **Settings & Neobrutalist Design System**: High-contrast neobrutalist UI with light, dark (#070B20 background, #1A2560 cards), and system theme support, push notification toggles, low stock warning thresholds, and store profile customization.

---

## Tech Stack

### Android Mobile App
| Component | Technology | Version / Detail |
|---|---|---|
| **Language** | Kotlin | `2.2.10` |
| **UI Framework** | Jetpack Compose | Compose BOM `2026.02.01`, Material 3 |
| **Architecture** | MVVM | Lifecycle `2.6.1`, ViewModel Compose, Navigation Compose `2.8.0` |
| **Networking** | Retrofit & OkHttp | Retrofit `2.9.0`, Gson Converter `2.9.0`, OkHttp `4.12.0` |
| **Preferences** | Jetpack DataStore | Preferences DataStore `1.1.3` |
| **Push Notifications** | Firebase Cloud Messaging | Firebase BOM `33.9.0`, FCM `24.1.0`, Google Services `4.5.0` |
| **Speech & Audio** | Android Speech API | `android.speech.SpeechRecognizer` & `android.speech.tts.TextToSpeech` |
| **Build System** | Gradle | Android Gradle Plugin `9.4.1`, Gradle `9.6.0`, Java 11 Target, `targetSdk 37` |

### Backend Services
| Component | Technology | Version / Detail |
|---|---|---|
| **Server Environment** | Apache (XAMPP) | PHP 8.1+ |
| **Database** | MySQL / MariaDB | MySQL 5.7+ / MariaDB 10.4+, PDO with prepared statements |
| **Push Delivery** | Firebase Admin SDK | FCM HTTP v1 via OAuth2 JWT (Service Account) |
| **Email Delivery** | PHPMailer | SMTP Weekly Digest cron job |
| **AI Co-Pilot** | Ollama & Google Gemini | Hybrid local inference (`qwen2.5:7b` / `llama3.2`) + Cloud failover (`gemini-3.5-flash`) |

---

## How to Run

### 1. Backend Setup (XAMPP, MySQL, PHP, Ollama)

1. **Install Prerequisites**:
   - Download and install [XAMPP](https://www.apachefriends.org/) with PHP 8.1+ and MySQL.
   - Verify PHP extensions in `php.ini`: `pdo_mysql`, `curl`, `openssl`, `mbstring`, `json`.
2. **Clone & Place Backend**:
   - Place the backend project inside your XAMPP web root:
     ```bash
     C:\xampp\htdocs\StockSense_backend
     ```
3. **Database Configuration**:
   - Start **Apache** and **MySQL** from the XAMPP Control Panel.
   - Open phpMyAdmin (`http://localhost/phpmyadmin`) or MySQL CLI and create the database:
     ```sql
     CREATE DATABASE stocksense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```
   - Import the database schema from `docs/schema.sql`:
     ```bash
     mysql -u root -p stocksense < C:\xampp\htdocs\StockSense_backend\docs\schema.sql
     ```
   - Run incremental migrations from the backend directory:
     ```bash
     cd C:\xampp\htdocs\StockSense_backend
     php migrations/01_create_import_mappings_table.php
     php migrations/02_add_idempotency_key_to_import_history.php
     php migrations/03_create_device_tokens_table.php
     php migrations/04_create_ai_tables.php
     php migrations/05_add_icon_columns_to_products.php
     php migrations/06_create_intent_misses_table.php
     ```
4. **Create Private Directory & Configuration Files**:
   - For security, private keys reside outside `htdocs`. Create:
     ```
     C:\xampp\StockSense_private\
     ```
   - Configure local credentials as described in the **Security & Configuration Note** section below.
5. **Set Up Ollama (Optional Local LLM)**:
   - Download and install [Ollama](https://ollama.com/).
   - Pull the recommended model:
     ```bash
     ollama pull qwen2.5:7b
     ```
   - Ollama serves local inference at `http://localhost:11434`.

---

### 2. Android App Setup (Android Studio)

1. **Prerequisites**:
   - Install **Android Studio Ladybug (2024.2+)** or newer with JDK 11+.
   - An Android device running Android 7.0 (API 24) or higher, or an Android Studio Virtual Device (AVD).
2. **Open and Sync**:
   - Open Android Studio, select **Open**, and choose the `StockSense` project directory.
   - Allow Android Studio to download dependencies and sync Gradle.
3. **Add Firebase Configuration**:
   - Place your `google-services.json` in the `app/` directory (see template below).
4. **Configure Backend URL**:
   - In `app/build.gradle.kts`, set `BASE_URL` in `buildTypes.debug`:
     - **Android Emulator**: `"http://10.0.2.2/StockSense_backend/"`
     - **Physical Device**: `"http://<YOUR_LOCAL_IP>/StockSense_backend/"` (e.g. `http://192.168.1.100/StockSense_backend/`, phone and PC on same Wi-Fi)
     - **Tunneling**: `"https://<YOUR_TUNNEL>.ngrok-free.app/StockSense_backend/"`
5. **Build and Run**:
   - Select your target device or emulator in Android Studio and press **Run 'app'** (`Shift + F10`).

---

## How to Run the Tests

### Backend Automated Test Suite
The backend contains 23 comprehensive test suites validating authentication, rate limiting, multi-tenant isolation, inventory imports, sales calculations, reorder logic, purchase order delete rules, and AI intents.

To run the full suite from PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File C:\xampp\htdocs\StockSense_backend\tests\run_all_tests.ps1
```

To run individual test suites via PHP CLI:
```bash
php C:\xampp\htdocs\StockSense_backend\tests\test_token_auth.php
php C:\xampp\htdocs\StockSense_backend\tests\test_inventory_backend.php
php C:\xampp\htdocs\StockSense_backend\tests\test_reorder_backend.php
php C:\xampp\htdocs\StockSense_backend\tests\test_purchase_order_delete_rule.php
php C:\xampp\htdocs\StockSense_backend\tests\test_ai.php
```

### Android Unit Tests
The Android project includes unit tests for ViewModels, repository layers, and UI card formatting (including purchase order cards and import column detection).

To run unit tests via Gradle CLI:
```bash
# Windows PowerShell / CMD
.\gradlew testDebugUnitTest

# Run with fresh task execution
.\gradlew testDebugUnitTest --rerun-tasks
```

To run tests in Android Studio:
- Right-click `app/src/test/java` in the Project Explorer and select **Run 'Tests in 'test''**.

---

## Folder Structure

```
StockSense/
├── app/
│   ├── build.gradle.kts                   # App-level build configurations & Base URLs
│   ├── google-services.json.example       # Template for Firebase client configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml        # App permissions, activities, and services
│       │   ├── java/com/simats/stocksense/
│       │   │   ├── core/                  # Design system, theme, FCM service, network client
│       │   │   │   ├── components/        # Reusable Compose buttons, dialogs, avatars, chips
│       │   │   │   ├── fcm/               # Firebase token registration & push receiver
│       │   │   │   ├── network/           # Retrofit interface, auth interceptor, ApiConstants
│       │   │   │   ├── theme/             # Light and dark neobrutalist color palettes
│       │   │   │   └── util/              # Formatters, currency helpers, and extensions
│       │   │   ├── data/                  # Data layer models and repository implementations
│       │   │   │   ├── model/             # Serialization DTOs and business domain entities
│       │   │   │   └── repository/        # Inventory, sales, reorder, auth, and AI repos
│       │   │   └── feature/               # UI features and Jetpack Compose screens
│       │   │       ├── ai/                # Dukaan Mitra chat screen, voice sheet & ViewModel
│       │   │       ├── analytics/         # Point-of-sale billing & sales reporting screens
│       │   │       ├── auth/              # Login, registration, and password recovery
│       │   │       ├── dashboard/         # Supermarket home overview, notifications & profile
│       │   │       ├── inventory/         # Product catalog, CRUD, and spreadsheet import wizard
│       │   │       └── reorder/           # Reorder recommendations, PO details & management
│       │   └── res/                       # App icons, drawables, strings, and navigation XML
│       └── test/                          # Local JVM unit test suites
├── gradle/                                # Gradle wrapper files
├── build.gradle.kts                       # Root project build script
├── settings.gradle.kts                    # Module declarations and plugin repositories
└── README.md
```

---

## Security & Configuration Note

> [!IMPORTANT]
> **API keys, service account credentials, and database passwords are never committed to version control.**  
> Create the following configuration files on your local machine using the placeholder templates below:

### 1. `app/google-services.json` (Android Repo)
Download this file from your [Firebase Console](https://console.firebase.google.com/) (`Project Settings > General > Your Apps`) and place it in the `app/` folder.  
A committed placeholder template is available at `app/google-services.json.example`.

### 2. `config/db.php` (Backend Web Root)
Create at `C:\xampp\htdocs\StockSense_backend\config\db.php`:
```php
<?php
$host = "localhost";
$dbname = "stocksense";
$username = "root";
$password = ""; // Your local database password if configured
```

### 3. `firebase-service-account.json` (Private Storage)
Create at `C:\xampp\StockSense_private\firebase-service-account.json`:
```json
{
  "type": "service_account",
  "project_id": "your-firebase-project-id",
  "private_key_id": "YOUR_PRIVATE_KEY_ID",
  "private_key": "-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_HERE\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@your-firebase-project-id.iam.gserviceaccount.com",
  "client_id": "000000000000000000000",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

### 4. `gemini.json` (Private Storage - Optional Cloud AI)
Create at `C:\xampp\StockSense_private\gemini.json`:
```json
{
  "api_key": "YOUR_GEMINI_API_KEY"
}
```

*All secrets, passwords, and tokens shown in this document and throughout the repository are placeholders only.*
