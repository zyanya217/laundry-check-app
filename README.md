# laundry-check-app

晾衣判斷的原生 Android 版。網頁版：<https://github.com/zyanya217/laundry-check-web>

每晚判斷「今晚晾在陽台，明早收衣前會不會乾、會不會下雨」，資料來自中央氣象署。

## 架構

單 module，依 Google 官方架構建議分層（參考 [android/nowinandroid](https://github.com/android/nowinandroid)）：

- `model/` 資料類別
- `logic/` 純 Kotlin 的判斷邏輯（由網頁版 `src/lib/*.ts` 移植，不依賴 Android）
- `data/` Repository（預報、設定、行政區、定位）— 尚未實作
- `ui/` Compose + ViewModel — 尚未實作

預報資料打網頁版已部署的 `/api/forecast`，**CWA API key 只存在 server 端，不會進 APK**。

## 測試

`logic/` 的單元測試是網頁版 vitest 案例的移植：

```
./gradlew :app:testDebugUnitTest
```

## 狀態

- [x] 專案骨架、`logic/` 移植與測試
- [ ] data 層、UI、設定頁、App 內語言/主題切換
- [ ] 每晚約 20:30 通知（WorkManager）
- [ ] 桌面 widget（Glance）
