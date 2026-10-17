# 簡單農民曆

「簡單農民曆」是一款以簡單、易讀、好操作為目標的 Android 農民曆 APP。開啟後會直接顯示今日的西元日期、農曆日期、節日、節氣與台灣神明生日，並提供桌面小工具方便快速查看。

> 本專案內容屬傳統民俗資訊，僅供日常參考，不構成醫療、法律、投資或其他專業建議。

## 主要功能

- 開啟 APP 立即顯示今日農民曆。
- 顯示西元日期、星期、農曆日期、節日、節氣與台灣神明生日。
- 使用前一天、後一天或日期選擇器切換日期。
- 月曆檢視：以月為單位瀏覽西元與農曆日期、節氣與節日，可切換月份與年份，點選日期查看當日詳細。
- 神明生日清單：依農曆月份分組列出全部神明生日。
- 提供六種預設主題色與單一彩虹色相滑桿自訂色彩。
- 提供系統、淺色與深色三種顯示模式。
- 主題設定立即生效，並同步調整狀態列與系統導覽列圖示明暗。
- 已選項使用實心主色背景、高對比粗體文字及勾選圖示。
- 提供顯示今日農曆、節氣與神明生日的 Android 桌面小工具。
- 支援 Android 8.0 至 Android 16（API 26 至 API 36）。
- 農曆、節氣與節日皆內建計算，可離線使用，不會在執行期間連線任何網站。

## 資料範圍

- 農曆日期：由 Android ICU `ChineseCalendar` 即時計算。
- 二十四節氣：由內建天文演算法（太陽視黃經）即時計算。
- 節日與台灣神明生日：內建對照表，依農曆月日查表，閏月不顯示。
- 無固定日期範圍限制，涵蓋 ICU 支援的年份。

資料來源與授權說明請參閱 [`docs/DATA.md`](docs/DATA.md)。

## 下載與安裝

目前 GitHub Release 提供的是**測試用 Pre-release 版本** `app-prerelease.apk`，並非正式簽署版本。此版本以 R8 壓縮並使用 Android 開發用金鑰（Debug 金鑰）簽署，僅供測試，不應視為正式上線版本。

請前往本專案的 [Releases](../../releases) 頁面下載標示為 **Pre-release** 的 APK。安裝測試 APK 可能觸發 Android 的未知來源或 Play Protect 提示，請只從本專案 GitHub Release 下載。詳細操作請參閱 [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md)。

## 開發與建置

專案採用 Kotlin、Jetpack Compose、Material 3、DataStore 與 Jetpack Glance。快速建置指令：

```powershell
# 測試版本（含 R8 壓縮、Debug 金鑰簽署，版本 1.1.0-prerelease）
.\gradlew.bat assemblePrerelease

# 偵錯版本
.\gradlew.bat assembleDebug
```

`prerelease` APK 輸出於：

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

完整環境需求、測試方式與資料更新流程請參閱 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。

## 專案文件

- [使用指南](docs/USER_GUIDE.md)
- [系統架構與技術文件](docs/ARCHITECTURE.md)
- [系統設計文件](docs/DESIGN.md)
- [開發與建置](docs/DEVELOPMENT.md)
- [資料來源與範圍](docs/DATA.md)
- [隱私權說明](PRIVACY.md)
- [參與貢獻](CONTRIBUTING.md)
- [版本紀錄](CHANGELOG.md)

## 授權

本專案程式碼採用 [MIT License](LICENSE) 授權。

本專案程式碼與內建資料採用 [MIT License](LICENSE) 授權，不含任何第三方農民曆資料。
