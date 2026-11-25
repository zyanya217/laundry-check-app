# laundry-check-app

晾衣判斷的原生 Android 版。網頁版：<https://github.com/zyanya217/laundry-check-web>

每晚判斷「今晚晾在陽台，明早收衣前會不會乾、會不會下雨」，資料來自中央氣象署。

## 架構

單 module，依 Google 官方架構建議分層（參考 [android/nowinandroid](https://github.com/android/nowinandroid)）：

- `model/` 資料類別
- `logic/` 純 Kotlin 的判斷邏輯（由網頁版 `src/lib/*.ts` 與 `App.vue` 的組合邏輯移植，不依賴 Android）
- `data/` Repository：預報（Retrofit + kotlinx.serialization）、設定（DataStore）、行政區（assets）、定位（`LocationManager`）
- `di/` Hilt 模組
- `ui/` Compose：`forecast/`（主畫面）、`settings/`（設定頁、App 內語言切換）、`theme/`（dynamic color + 固定語意色 + 內嵌字型）

語言切換用系統的 per-app language（`LocaleManager`），語言由系統保存；主題（跟隨系統/淺色/深色）存在 DataStore。

預報資料打網頁版已部署的 `/api/forecast`，**CWA API key 只存在 server 端，不會進 APK**。

## 測試

`logic/` 的單元測試是網頁版 vitest 案例的移植：

```
./gradlew :app:testDebugUnitTest
```

字型（Huninn、Noto Sans TC、DM Mono）皆為 SIL OFL 1.1，授權檔在 `licenses/fonts/`。

## 狀態

- [x] 專案骨架、`logic/` 移植與測試
- [x] data 層、UI、設定頁、App 內語言/主題切換（尚未在 Android Studio 實機驗證）
- [ ] 每晚約 20:30 通知（WorkManager）
- [ ] 桌面 widget（Glance）
