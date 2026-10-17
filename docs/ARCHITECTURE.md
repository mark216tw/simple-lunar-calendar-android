# 系統架構與技術文件

本文件說明「簡單農民曆」Android APP 的技術架構、模組職責、資料流程、建置方式與品質保證。若需了解畫面與互動設計，請參閱 [系統設計文件](DESIGN.md)。

## 1. 技術總覽

| 項目 | 內容 |
| --- | --- |
| 語言 | Kotlin |
| 使用者介面 | Jetpack Compose、Material 3 |
| 桌面小工具 | Jetpack Glance（Glance AppWidget） |
| 狀態管理 | Android ViewModel、`StateFlow`、`collectAsStateWithLifecycle` |
| 設定儲存 | Jetpack DataStore Preferences |
| 日期與曆法 | `java.time`、Android ICU `ChineseCalendar` |
| 建置系統 | Gradle Wrapper 8.11.1、Android Gradle Plugin 8.10.1、Kotlin 2.1.20 |
| Java 版本 | JDK 17 |
| 最低版本 | Android 8.0（API 26） |
| 目標版本 | Android 16（API 36） |
| 應用程式 ID | `com.simplelunar.calendar` |
| 曆法資料 | 內建計算與對照表，無外部來源 |

設計原則：

- **離線優先**：執行期間不連線網路，資料皆為內建計算與對照表。
- **單一 Activity**：以 Compose 畫面狀態切換頁面，降低生命週期複雜度。
- **單一資料來源**：`AlmanacRepository` 為查詢每日資訊的唯一入口。
- **即時計算**：農曆以 Android ICU、節氣以內建天文演算法計算，無資料範圍限制。

## 2. 專案結構

```text
.
├─ app/
│  ├─ build.gradle.kts                 應用程式模組設定與 Build Type
│  └─ src/
│     ├─ main/
│     │  ├─ AndroidManifest.xml         小工具與開機廣播註冊
│     │  ├─ java/com/simplelunar/calendar/
│     │  │  ├─ MainActivity.kt          單一 Activity、Compose 畫面
│     │  │  ├─ MainViewModel.kt         設定狀態與小工具更新
│     │  │  ├─ data/
│     │  │  │  ├─ AlmanacRepository.kt  每日資訊查詢、農曆計算與月曆查詢
│     │  │  │  ├─ SolarTerms.kt         二十四節氣天文演算法
│     │  │  │  ├─ FestivalTable.kt      農曆歲時節日對照表
│     │  │  │  ├─ DeityBirthdays.kt     台灣神明生日對照表
│     │  │  │  ├─ LunarNames.kt         農曆月名與日名轉換
│     │  │  │  └─ ThemePreferences.kt   主題設定模型與 DataStore
│     │  │  ├─ ui/theme/AppTheme.kt     主題配色與系統列樣式
│     │  │  └─ widget/LunarWidget.kt    Glance 桌面小工具
│     │  └─ res/                        字串、主題、小工具中繼資料、圖示
│     └─ test/java/com/simplelunar/calendar/
│        ├─ ui/theme/AppThemeTest.kt
│        ├─ data/SolarTermsTest.kt
│        ├─ data/SolarTermsTermOnTest.kt
│        ├─ data/FestivalTableTest.kt
│        ├─ data/AlmanacRepositoryMonthTest.kt
│        └─ data/DeityBirthdaysTest.kt
├─ docs/                               專案文件
├─ build.gradle.kts / settings.gradle.kts
└─ gradlew / gradlew.bat
```

## 3. 分層與模組職責

### 3.1 呈現層（Presentation）

- `MainActivity`：唯一的 `ComponentActivity`，`setContent` 啟動 `SimpleLunarTheme` 與 `SimpleLunarApp`。
- `Screen`（`TODAY`、`SETTINGS`、`CALENDAR`、`DEITIES`）：以 `rememberSaveable` 保存的畫面狀態；非首頁時以 `BackHandler` 攔截系統返回鍵回到首頁。
- 首頁：`TodayScreen` → `DateHero`（日期、農曆、日期切換）、`OccasionCard`（節氣與節日）、`DeityBirthdayCard`（神明生日，含「查看全部」）、日期選擇對話框。
- 月曆頁：`CalendarScreen` → `MonthHeader`（上／下個月、年月選擇）、`WeekdayHeader`、`CalendarDayCell` 月曆格線、`MonthYearPickerDialog`（年份加減與十二個月選擇）。
- 神明生日清單頁：`DeityListScreen`，依農曆月份分組的 `LazyColumn`。
- 設定頁：`SettingsScreen` → 顯示模式、主題色彩、自訂色彩面板。

### 3.2 狀態層（State）

- `MainViewModel`：持有 `ThemePreferences`，將設定以 `StateFlow<ThemeSettings>` 曝光。
  - `settings` 收集自 DataStore。
  - `selectColor`／`setCustomHue`／`selectDisplayMode` 以樂觀更新寫回 `_settings`，再非同步儲存。
  - `setCustomHue` 具 150ms 去抖動，避免拖曳滑桿時過度寫入。
  - 設定變更後呼叫 `LunarWidget().updateAll()` 同步刷新桌面小工具。

### 3.3 資料層（Data）

- `AlmanacRepository`（`object`）：
  - `day(date)`：查詢指定日期的 `AlmanacDay`。
  - `month(yearMonth)`：查詢整月 `CalendarMonth`（6 週 × 7 天）；`monthDates(yearMonth)` 為不依賴 ICU 的純月曆格線（供測試與節氣標記）。
  - `lunarParts(date)`：以 ICU `ChineseCalendar` 計算農曆月、日、閏月與顯示名稱。
  - `festivals(date, lunar)`：國曆與農曆節日查表，並判斷農曆除夕。
- `SolarTerms`（`object`）：以太陽視黃經天文演算法計算二十四節氣；`currentTerm(date)` 取所選日期當下最近的節氣，`termOn(date)` 僅於交節氣當日回傳名稱，`termsInYear(year)` 提供該年全部節氣日期（含年份快取）。
- `FestivalTable`（`object`）：農曆月→日→節日名稱清單的靜態對照，閏月回傳空清單。
- `DeityBirthdays`（`object`）：農曆月→日→名稱清單的靜態對照；`forLunarDay(month, day, isLeap)` 於閏月回傳空清單，`all()` 依農曆月、日排序回傳全部 `DeityBirthday`。
- `LunarNames`（`object`）：農曆月名（正月…臘月）與日名（初一…三十）轉換。
- `ThemePreferences`：`preferencesDataStore` 擴充、`ThemeChoice`／`DisplayMode`／`ThemeSettings` 模型與存取。

### 3.4 主題層（Theme）

- `AppTheme`：
  - `resolvePrimary(settings)`：解析六種暖色預設與自訂色相的主色。
  - `selectedContentColor(background)`：依亮度選擇深／淺文字色。
  - `SimpleLunarTheme`：組裝淺色／深色 `ColorScheme`，並同步狀態列與導覽列圖示明暗。

### 3.5 小工具層（Widget）

- `LunarWidget`（`GlanceAppWidget`）：讀取主題設定與當日 `AlmanacDay`，以 Glance 組裝 `WidgetContent`。
- `LunarWidgetReceiver`（`GlanceAppWidgetReceiver`）：系統小工具生命週期入口。
- `DateChangeReceiver`（`BroadcastReceiver`）：接收 `DATE_CHANGED`、`TIME_CHANGED`、`TIMEZONE_CHANGED`、`BOOT_COMPLETED` 後呼叫 `updateAll`。
- 定期更新由 `res/xml/lunar_widget_info.xml` 的 `updatePeriodMillis` 控制（30 分鐘）。

## 4. 資料流程

```text
使用者操作（Compose）
   │
   ├─ 切換日期 → TodayScreen 重新計算
   │                 └─ AlmanacRepository.day()
   │                       ├─ lunarParts()        ICU ChineseCalendar
   │                       ├─ SolarTerms          節氣天文演算法
   │                       ├─ festivals()          節日查表與除夕判斷
   │                       └─ DeityBirthdays       農曆月日查表
   │
   ├─ 瀏覽月曆 → CalendarScreen
   │                 └─ AlmanacRepository.month()
   │                       ├─ monthDates()        純月曆格線（週日起算、6×7）
   │                       └─ SolarTerms.termsInYear() 節氣日期與快取
   │
   ├─ 查看神明生日 → DeityListScreen → DeityBirthdays.all()
   │
   └─ 更改設定 → MainViewModel
                    ├─ _settings 樂觀更新
                    ├─ ThemePreferences.save()  DataStore
                    └─ LunarWidget().updateAll() Glance 重新繪製
```

每日資訊皆於查詢時即時計算，無需讀取或快取資料檔。

## 5. 內建資料

農民曆內容皆為內建，無外部資料檔：

- 農曆日期：ICU `ChineseCalendar` 計算。
- 二十四節氣：`SolarTerms` 以太陽視黃經天文演算法計算，換算為 `Asia/Taipei` 當地日期。
- 農曆節日：`FestivalTable` 以農曆月日建檔。
- 台灣神明生日：`DeityBirthdays` 以農曆月日建檔，閏月不顯示。

詳見 [資料來源與範圍](DATA.md)。

## 6. 建置與 Build Type

`app/build.gradle.kts` 定義三種 Build Type：

| Build Type | 壓縮（R8） | 資源縮減 | 簽署 | 版本名稱後綴 | APK |
| --- | --- | --- | --- | --- | --- |
| `debug` | 否 | 否 | Debug 金鑰 | `-debug` | `app-debug.apk` |
| `prerelease` | 是 | 是 | Debug 金鑰 | `-prerelease` | `app-prerelease.apk` |
| `release` | 是 | 是 | 需另行設定 | 無 | `app-release.apk` |

`prerelease` 由 `release` 初始化，啟用 `isMinifyEnabled` 與 `isShrinkResources`，並使用 Debug 簽署設定，適合提供測試用的預先發行版本。

建置指令：

```powershell
# 測試版本（含 R8）
.\gradlew.bat assemblePrerelease

# 偵錯版本
.\gradlew.bat assembleDebug
```

輸出位置：

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

## 7. 測試與靜態檢查

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

- 單元測試：`AppThemeTest`（主題色解析與內容色對比）、`SolarTermsTest`（節氣日期）、`SolarTermsTermOnTest`（交節氣當日判定與年度節氣）、`FestivalTableTest`（農曆節日查表與閏月行為）、`AlmanacRepositoryMonthTest`（月曆格線與節氣標記）、`DeityBirthdaysTest`（神明生日查表、閏月行為與全部清單排序）。
- Lint：`lintDebug`，報告位於 `app/build/reports/lint-results-debug.html`。

## 8. 效能與相容性

- 農曆與節氣皆於查詢時即時計算，無需載入資料檔。
- 農曆計算使用系統 ICU，節氣以內建演算法計算，無需額外函式庫。
- 小工具採用 Glance，與 Compose 共用 Kotlin/Gradle 設定。
- 深色模式、狀態列與導覽列圖示明暗自動同步。

## 9. 疑難排解

- **增量編譯錯誤 `different roots`**：專案曾被移動路徑時，Kotlin 增量快取可能失效；Gradle 會自動退回非增量編譯，可忽略。必要時刪除 `.gradle`、`.kotlin`、`app/build` 後重建。
- **APK 無法安裝**：Debug 金鑰簽署的測試版本可能觸發未知來源或 Play Protect 提示，請僅從本專案 Release 下載。
- **版本號顯示**：`versionName` 加上 Build Type 後綴，`prerelease` 版本顯示為 `1.1.0-prerelease`。

## 10. 相關文件

- [系統設計文件](DESIGN.md)
- [資料來源與範圍](DATA.md)
- [使用指南](USER_GUIDE.md)
- [開發與建置](DEVELOPMENT.md)
