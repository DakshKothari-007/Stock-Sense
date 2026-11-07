# StockSense

> **Smarter Stock. Better Business.**

StockSense is an inventory intelligence app engineered for medium-sized supermarkets and grocery store owners.  
It unifies stock tracking, fast point-of-sale billing, intelligent reordering, and predictive analytics into an intuitive mobile interface.  
Powered by the Dukaan Mitra AI assistant and automated spreadsheet ingestion, StockSense prevents stockouts and optimizes working capital.

---

## Features

- **Authentication & Security**: Owner registration, secure password hashing, 64-character hexadecimal Bearer token session management, password recovery, and profile configuration.
- **Home Dashboard**: Real-time overview of total inventory valuation, active low-stock alerts, category distribution, daily revenue metrics, and quick operational shortcuts.
- **Inventory Management**: Searchable and filterable product catalog supporting instant queries, category filtering, and multi-field sorting (name, price, stock units, recency).
- **Product Detail**: Detailed stock inspection displaying unit counts, cost price, selling price, calculated gross margin, supplier details, and restock thresholds.
- **Product CRUD**: Add, update, and remove products with client- and server-side validation, barcode/SKU tracking, and unit increment/decrement logic.
- **CSV & XLSX Bulk Import**: Intelligent spreadsheet ingestion featuring automatic column header detection, data mapping preview, dry-run validation with auto-fixing suggestions, and atomic idempotent commit.
- **Record Sale (POS Billing)**: Fast point-of-sale billing allowing multi-item checkout, real-time stock availability verification, and immediate atomic inventory decrement.
- **Sales Analytics**: Revenue tracking, transaction history, top-selling product ranking, and hourly store foot-traffic trends across customizable timeframes.
- **Reorder & Purchase Orders**: Automated restock recommendations factoring in current inventory and incoming orders, automatic purchase order generation (`PO-YYYY-NNN`), order status management, and one-tap stock intake.
- **Retail Insights**: Data-driven recommendations including slow-moving stock clearance alerts with margin protection, market basket paired-item association rules (lift > 1.0), and decision audit history.
- **Push Notifications**: Proactive alerts for low-stock thresholds and purchase order updates delivered via Firebase Cloud Messaging (FCM HTTP v1) and an in-app notification center.
- **Settings & Preferences**: Customizable store options including Dark/Light/System theme mode, push notification toggles, low stock warning thresholds, and currency formatting.
- **Store Profile**: Store owner details, supermarket brand, business type, physical store location, and account management.
- **Dukaan Mitra AI Assistant with Voice**: Built-in retail business co-pilot supporting bilingual conversational queries, voice input via `SpeechRecognizer`, speech synthesis playback via `TextToSpeech`, grounded shop metric tool calling, retail guidance, and resilient fallback execution.

---

## Tech Stack

The application is built using modern Android engineering standards:

| Component | Technology | Version / Detail |
|---|---|---|
| **Language** | Kotlin | `2.2.10` |
| **UI Framework** | Jetpack Compose | Compose BOM `2026.02.01`, Material 3 |
| **Architecture** | MVVM | Lifecycle `2.6.1`, ViewModel Compose, Navigation Compose `2.8.0` |
| **Networking** | Retrofit & OkHttp | Retrofit `2.9.0`, Gson Converter `2.9.0`, OkHttp `4.12.0` |
| **Push Notifications** | Firebase Cloud Messaging | Firebase BOM `33.9.0`, FCM `24.1.0`, Google Services `4.5.0` |
| **Speech & Audio** | Android Speech API | `android.speech.SpeechRecognizer` & `android.speech.tts.TextToSpeech` |
| **Build System** | Gradle | Android Gradle Plugin `9.4.1`, Gradle `9.6.0`, Java 11 Target, `targetSdk 37` |

---

## Setup & Getting Started

### 1. Prerequisites
- **Android Studio**: Install Android Studio Ladybug (2024.2+) or newer with JDK 11+.
- **Device**: An Android phone running Android 7.0 (API 24) or higher, or an Android Studio Virtual Device (AVD).
- **Backend**: The StockSense backend running on a local Apache/MySQL server (XAMPP) or remote server.

### 2. Clone the Repository
```bash
git clone https://github.com/DakshKothari-007/Stock-Sense.git
cd Stock-Sense
```

### 3. Open and Sync
1. Open Android Studio and choose **Open**, then select the `Stock-Sense` project folder.
2. Allow Android Studio to download Gradle wrapper dependencies and perform a project sync.

### 4. Add Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create or open your StockSense project.
2. Register an Android app with the package name: `com.simats.stocksense`.
3. Download the `google-services.json` file.
4. Place `google-services.json` in the `app/` directory:
   ```
   Stock-Sense/
   └── app/
       └── google-services.json
   ```

### 5. Configure the Backend Base URL
Open [ApiConstants.kt](file:///c:/Users/DESKTOP/StudioProjects/Stock-Sense/app/src/main/java/com/simats/stocksense/core/network/ApiConstants.kt) located at:
`app/src/main/java/com/simats/stocksense/core/network/ApiConstants.kt`

Set `BASE_URL` to match your backend server address (the trailing slash `/` is mandatory):

```kotlin
package com.simats.stocksense.core.network

object ApiConstants {
    // For physical phone testing on the same Wi-Fi network:
    const val BASE_URL = "http://<YOUR_LOCAL_IP>/StockSense_backend/"

    // For Android Emulator running on the host machine:
    // const val BASE_URL = "http://10.0.2.2/StockSense_backend/"

    // For secure tunneling (e.g. ngrok / cloud domain):
    // const val BASE_URL = "https://<YOUR_TUNNEL_DOMAIN>/StockSense_backend/"
}
```

> [!TIP]
> To find your local IP address on Windows, open PowerShell or Command Prompt and run `ipconfig`. Look for the **IPv4 Address** (e.g., `192.168.x.x`). Ensure your phone and development PC are connected to the same Wi-Fi network.

### 6. Build and Run
1. Connect your Android phone via USB (with **USB Debugging** enabled) or start an emulator.
2. Click **Run 'app'** (`Shift + F10`) in Android Studio.

---

## Project Structure

The core codebase is organized under `app/src/main/java/com/simats/stocksense`:

- `core/components/`: Reusable Compose UI components including buttons, headers, metric chips, custom dialogs, and cards.
- `core/fcm/`: Firebase Cloud Messaging token registration handler and push message receiver service.
- `core/network/`: Retrofit service interface, OkHttp client interceptors, auth headers, and backend `ApiConstants.kt`.
- `core/theme/`: Jetpack Compose design system including color schemes, typography styles, and component shapes.
- `core/util/`: Application-wide utility classes, date/time formatters, and Kotlin extension helpers.
- `data/model/`: Strongly typed Kotlin data models and JSON request/response serialization DTOs.
- `data/repository/`: Data layer repositories managing API communication, session token propagation, and remote caching.
- `feature/ai/`: Dukaan Mitra conversational assistant UI (`AiChatScreen`), voice input/output integrations, and chat ViewModel.
- `feature/analytics/`: Sales billing screen (`RecordSaleScreen`), cart item picker, and visual revenue analytics dashboards.
- `feature/auth/`: User onboarding, store owner registration, login screen, password reset, and splash navigation.
- `feature/dashboard/`: Main supermarket home dashboard, store alert summaries, notification inbox, and profile screens.
- `feature/inventory/`: Real-time inventory list, product detail viewer, single product CRUD, and spreadsheet import wizard.
- `feature/reorder/`: Intelligent restock recommendations, purchase order tracking, and stock delivery intake workflows.

---

## Not in This Repository

For security and privacy, the following files and services are omitted from this repository and must be configured locally:

1. `app/google-services.json`: Firebase client configuration file containing project identifiers and client keys. Obtain this from your Firebase Console.
2. **Backend Server (`StockSense_backend`)**: The PHP 8.1 REST API backend and MySQL database are maintained in a separate repository. Refer to `README_BACKEND.md` for backend setup.
3. **Private Backend Keys & Credentials**:
   - `C:\xampp\StockSense_private\firebase-service-account.json`: Private Google Service Account key for Firebase Admin SDK (used by the backend to dispatch FCM pushes).
   - `C:\xampp\StockSense_private\gemini.json`: API credentials for the Dukaan Mitra AI engine.
   - `config/db.php`: Local MySQL database connection credentials.
