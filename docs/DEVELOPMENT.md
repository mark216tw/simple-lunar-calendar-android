# 開發與建置文件

## 技術架構

- 語言：Kotlin
- UI：Jetpack Compose、Material 3
- 狀態管理：Android ViewModel、StateFlow
- 設定儲存：Jetpack DataStore Preferences
- 桌面小工具：Jetpack Glance AppWidget
- 日期：`java.time` 與 Android ICU `ChineseCalendar`
- 最低版本：Android 8.0（API 26）
- 目標版本：Android 16（API 36）
- Java：JDK 17
- Gradle：Gradle Wrapper 8.11.1
- Android Gradle Plugin：8.10.1

## 開發環境需求

建議使用支援 Android API 36 的最新版 Android Studio，並安裝：

- JDK 17
- Android SDK Platform 36
- Android SDK Build-Tools 35 或更新版本
- Android SDK Platform-Tools

## 取得專案

```powershell
git clone https://github.com/mark216tw/simple-lunar-calendar-android.git
Set-Location simple-lunar-calendar-android
```

Android Studio 通常會自動建立 `local.properties`。如需手動設定，內容如下：

```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
```

`local.properties` 含本機路徑，不應提交至版本控制。

## 建置 APK

### Debug 版本

Windows：

```powershell
.\gradlew.bat assembleDebug
```

輸出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Pre-release 版本（含 R8 壓縮、Debug 金鑰簽署）

`prerelease` Build Type 由 `release` 初始化，啟用 R8 程式碼壓縮與資源縮減，並使用 Debug 簽署設定，適合提供測試用的預先發行版本。

```powershell
.\gradlew.bat assemblePrerelease
```

輸出位置：

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

版本名稱由 `app/build.gradle.kts` 的 `versionName` 加上 Build Type 後綴組成，目前為 `1.0.0-prerelease`。

## 執行測試與靜態檢查

Windows：

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

執行完整驗證：

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Lint HTML 報告位於：

```text
app/build/reports/lint-results-debug.html
```

## 專案結構

```text
app/src/main/java/com/simplelunar/calendar/
  MainActivity.kt                 Compose 畫面與操作
  MainViewModel.kt                主題設定狀態
  data/AlmanacRepository.kt       每日資訊查詢與農曆計算
  data/SolarTerms.kt              二十四節氣天文演算法
  data/FestivalTable.kt           農曆歲時節日對照表
  data/DeityBirthdays.kt          台灣神明生日（農曆月日對照）
  data/ThemePreferences.kt        DataStore 設定
  ui/theme/AppTheme.kt            主題與系統列配色
  widget/LunarWidget.kt           Glance 桌面小工具

docs/
  ARCHITECTURE.md                 系統架構與技術文件
  DESIGN.md                       系統設計文件
  DATA.md                         資料來源與範圍
  DEVELOPMENT.md                  本文件
  USER_GUIDE.md                   使用指南
```

## 維護內建資料

- 農曆節日：編輯 `data/FestivalTable.kt` 的農曆月日對照表（閏月不顯示）。
- 台灣神明生日：編輯 `data/DeityBirthdays.kt` 的農曆月日對照表（閏月不顯示）。
- 節氣：由 `data/SolarTerms.kt` 的天文演算法即時計算，無需維護資料表。

以上資料皆內建於程式碼中，不含外部或網路資料來源。維護後應執行完整建置驗證，並抽樣核對節氣日期與跨年節日（如除夕、春節）。

## 版本與發行流程

目前以 `prerelease` Build Type 產生測試用的預先發行版本：

- 版本名稱：`1.0.0-prerelease`（`versionName` 加 Build Type 後綴）。
- 啟用 R8 壓縮與資源縮減。
- 使用 Android 開發用金鑰（Debug 金鑰）簽署，並非正式簽署版本。
- GitHub Release 必須標示為 **Pre-release**，並於說明明確標示為測試用途與非正式上線版本。
- 發行檔名為 `app-prerelease.apk`。
- 不應將測試 APK 上架正式應用程式商店。

發行步驟：

1. 更新 `app/build.gradle.kts` 的 `versionName`（必要時調整 `versionCode`）。
2. 更新 [`CHANGELOG.md`](../CHANGELOG.md)。
3. 執行完整驗證：

   ```powershell
   .\gradlew.bat assemblePrerelease testDebugUnitTest lintDebug
   ```

4. 建立 GitHub Release，上傳 `app/build/outputs/apk/prerelease/app-prerelease.apk`，並勾選 Pre-release。

## 專案文件

- [`README.md`](../README.md)：專案說明
- [`docs/USER_GUIDE.md`](USER_GUIDE.md)：使用指南
- [`docs/ARCHITECTURE.md`](ARCHITECTURE.md)：系統架構與技術文件
- [`docs/DESIGN.md`](DESIGN.md)：系統設計文件
- [`docs/DATA.md`](DATA.md)：資料來源與範圍
- [`PRIVACY.md`](../PRIVACY.md)、[`CONTRIBUTING.md`](../CONTRIBUTING.md)、[`CHANGELOG.md`](../CHANGELOG.md)
