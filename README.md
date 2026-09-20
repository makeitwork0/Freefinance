# My Finance 💳💰
> **Offline-First Personal Wealth, Multi-Currency Ledger & Financial Dashboard for Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room-SQLite%20Offline-009688.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**My Finance** is a secure, 100% offline-first personal finance application built with modern **Kotlin** and **Jetpack Compose (Material 3)**. It is designed to give you complete sovereignty over your finances with zero third-party telemetry, instant multi-mode cash tracking, privacy masking, bank app quick-launchers, assets & valuables tracking, and interactive home screen widgets.

---

## ✨ Features at a Glance

### 🏦 Multi-Mode Account & Cash Management
- **Modes of Cash**: Track Cash on Hand, High-Yield Savings Accounts (HYSA), Digital Banks, E-Wallets (GCash, Maya, ShopeePay, GrabPay), and Traditional Bank accounts.
- **Direct Banking Launcher**: Instantly launch your installed banking or e-wallet apps (MariBank, GCash, Maya, SeaBank, BDO, BPI, UnionBank, GoTyme, etc.) directly with 1 tap.
- **Privacy Masking**: One-tap eye toggle hides all sensitive numbers with Asterisk Masks (`₱****` / `$****`) for secure browsing in public.

### 📊 Modular Dynamic Dashboard
- **Total Wealth Hero Card**: Unified net worth calculated dynamically from cleared ledger transactions with multi-currency conversion.
- **30-Day Cash Flow Forecast**: Real-time line projection chart with smooth canvas rendering and trend analytics.
- **Modular Dashboard Cards**: Reorder, show, or hide widgets such as Money Flow, Budget Progress, Debt Management, Liquid vs. Locked Funds, Long-Term Receivables, Weekly Forecasts, Upcoming Subscriptions, and Monthly Spending Trends.

### 💎 Assets & Valuables Tracker
- **Physical & Digital Assets**: Log real estate, vehicles, tech hardware, precious metals, and collectibles.
- **Live Appreciation & Gain/Loss**: Calculates capital gains/losses vs. initial purchase prices and provides asset category breakdowns.

### 🧾 Receipts Inbox & Review Pipeline
- **Receipt Capture**: Snap or import receipts on the fly for OCR and structured extraction.
- **Review Queue**: Verify and convert pending receipt drafts directly into ledger entries with custom category tags and notes.

### 📱 Interactive Home Screen App Widgets
- **Glanceable Balances**: Quick glance at total net worth and cash breakdown from your Android home screen.
- **Fast Action Bar**: 1-tap buttons to immediately open Quick Add or launch the Receipt Camera.

### 💾 Backup, Export & Universal JSON Sync
- **Universal JSON State Importer & Exporter**: Backup all accounts, transactions, debts, projects, and budgets in a portable JSON format.
- **Spreadsheet CSV Export**: Export clean, spreadsheet-ready CSV ledgers for accounting.
- **Google Drive Backup**: Optional Google Drive AppData sync.

---

## 🏗 Architecture & Tech Stack

The app follows **Modern Android Architecture (MVVM + Clean Architecture)** with an offline-first repository pattern:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material Design 3 (M3)](https://m3.material.io/)
- **Programming Language**: [Kotlin](https://kotlinlang.org/)
- **Database / Local Persistence**: [Room (SQLite)](https://developer.android.com/training/data-storage/room) with Kotlin Symbol Processing (KSP)
- **Asynchronous Flow**: Kotlin Coroutines, StateFlow, and SharedFlow
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/)
- **JSON Serialization**: [Kotlinx.Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **App Widgets**: Android AppWidgetProvider with RemoteViews

```
app/src/main/java/com/example/
├── data/
│   ├── local/              # Room Database, DAOs, Entities, and Type Converters
│   ├── model/              # Domain and Presentation Data Models
│   └── preferences/        # User Settings & UI Configuration
├── ui/
│   ├── components/         # Reusable Compose Components & Modular Cards
│   ├── screens/            # Main Destination Screens (Dashboard, Assets, Records, Settings, etc.)
│   └── theme/              # Material 3 Themes, Color Schemes, Typography
├── util/                   # Currency Formatting, Bank App Launchers, JSON Serializers
├── viewmodels/             # MVVM ViewModels with Reactive StateFlows
└── widget/                 # Home Screen AppWidget Provider & RemoteViews Factory
```

---

## 🚀 Getting Started & Building the APK

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer) OR standard command-line Gradle
- **JDK 17** or **JDK 21**
- **Android SDK API 34+** installed

### Clone the Repository
```bash
git clone https://github.com/your-username/my-finance.git
cd my-finance
```

### Build Debug APK
To build the debug APK directly from your terminal:
```bash
./gradlew assembleDebug
```
The compiled APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
To build an optimized, signed release APK:
```bash
./gradlew assembleRelease
```
The output APK will be located at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Install APK onto Connected Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 APK Releases & Distribution

You can download ready-to-install APK packages directly:
1. **In-App Release Center**: Navigate to **Settings → App Version & Releases** within the app to view the latest changelog, build metadata, and update notices.
2. **GitHub Releases**: Visit the [Releases](https://github.com/your-username/my-finance/releases) tab on GitHub to grab the latest standalone APK.
3. **AI Studio / Web Preview Export**: Export full ZIP source packages or compiled APKs directly via the **Project Settings** menu in Google AI Studio.

---

## 🔒 Privacy & Permissions

- **100% Offline-First**: No personal or financial records ever leave your device unless you explicitly trigger Google Drive backup.
- **Zero Ads & Zero Trackers**: No analytics or ad SDKs included.
- **Least-Privilege Permissions**:
  - `CAMERA`: Only used when you choose to snap physical receipts in the Receipts Inbox.
  - `INTERNET`: Only used for optional Google Drive cloud backups and currency rate updates.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
