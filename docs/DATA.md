# 資料來源與範圍

## 資料來源

本專案所有農民曆內容皆由程式自行計算或內建，不依賴任何外部網站或網路來源：

- 農曆日期：由 Android ICU `ChineseCalendar` 計算。
- 二十四節氣：由內建天文演算法（太陽視黃經）即時計算。
- 節日：內建對照表（`FestivalTable`）。
- 台灣神明生日：內建對照表（`DeityBirthdays`）。

APP 執行期間不連線網路，亦不包含任何第三方農民曆資料。

## 收錄範圍

- 無固定日期範圍：農曆日期與節氣皆即時計算，涵蓋 ICU 支援的年份。
- 節日與神明生日依農曆月日建檔，農曆閏月不顯示。

## 節氣計算

二十四節氣以太陽視黃經每 15 度為一期，依序為立春、雨水、驚蟄、春分、清明、穀雨、立夏、小滿、芒種、夏至、小暑、大暑、立秋、處暑、白露、秋分、寒露、霜降、立冬、小雪、大雪、冬至、小寒、大寒。

- 演算法：Meeus 低精度太陽視黃經公式搭配牛頓迭代求過中時刻，並換算為 `Asia/Taipei` 當地日期。
- 誤差：太陽視黃經約 0.01 度（換算時間約十餘分鐘），日期判定與官方曆法幾乎一致；極少數跨午夜交界可能相差一日。

## 台灣神明生日

各神明生日依農曆月日建檔，資料維護於：

```text
app/src/main/java/com/simplelunar/calendar/data/DeityBirthdays.kt
```

神明生日與節日資料分開，農曆閏月不顯示神明生日，以免與當月重複。不同廟宇與流派記載可能略有差異，僅供傳統民俗參考。

## APP 行為

- APP 執行期間不會連線任何網站。
- 農曆與節氣皆於查詢時即時計算，無需預先載入資料檔。
- 節氣欄位顯示所選日期當下最近的節氣。
- 神明生日依農曆月日查表，於農曆閏月不顯示。

## 資料維護

- 節日對照：`app/src/main/java/com/simplelunar/calendar/data/FestivalTable.kt`
- 神明生日對照：`app/src/main/java/com/simplelunar/calendar/data/DeityBirthdays.kt`
- 節氣演算法：`app/src/main/java/com/simplelunar/calendar/data/SolarTerms.kt`

## 權利與免責

本專案程式碼、內建資料與文件均採 [MIT License](../LICENSE)。

內容屬傳統民俗資訊，可能因流派差異而略有不同，僅供日常參考，不構成醫療、法律、投資或其他專業建議。
