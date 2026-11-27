# laundry-check-app

晾衣判斷的原生 Android 版。  
網頁版：<https://github.com/zyanya217/laundry-check-web>

每晚判斷「今晚晾在陽台，明早收衣前會不會乾、會不會下雨」，資料來自中央氣象署。

## 畫面

> 以下為設計稿（用於確認版面與配色），實際畫面以裝置為準。

| 主畫面 | 設定 |
| :---: | :---: |
| <img width="260" alt="home" src="https://github.com/user-attachments/assets/652a08d0-fb4c-4776-b101-f442bd7c4668" /> | <img width="260" alt="settings" src="https://github.com/user-attachments/assets/da632c06-7963-47ab-ad9f-15e58c42ddf9" /> |

主畫面第一屏就有結論、乾燥程度、開始晾／收衣時間、近期洗衣建議與逐時預報。設定以底部視窗開啟，可調整地點、預設時間、陽台是否遮雨、晚上提醒、語言與外觀。

### 桌面 widget

三種尺寸，點一下會開啟 App。背景色依結果變化（綠、黃、紅），沒有資料時為灰色，深色模式會自動切換。

| 2×2 | 4×2 | 4×3 |
| :---: | :---: | :---: |
| <img width="200" alt="widget-2x2" src="https://github.com/user-attachments/assets/44ca767a-85dc-4990-b1d6-de514de3b85c" /> | <img width="260" alt="widget-4x2" src="https://github.com/user-attachments/assets/5777bfb2-15ec-4279-a7a1-581a17a2f2f1" /> | <img width="260" alt="widget-4x3" src="https://github.com/user-attachments/assets/382bb8e9-9c27-4102-a49c-4c82129c7a49" /> |

Widget 每小時在背景更新一次（需有網路），並會保存最近一次預報，離線時仍可顯示；離開 App 時也會更新一次，讓設定變更立即反映。

### 晚上提醒

設定頁可開啟「晚上提醒」並選擇提醒時間（整點，00:00 到 23:00，預設 20:00）。**只有今晚判定適合晾衣服時才會通知**，不適合就不打擾。

- 沒有獨立排程，而是搭在 widget 每小時的背景更新上：更新完預報後，若今晚適合、已過設定的整點、且今天還沒通知過，就發出通知。每天最多一則。
- 因為是每小時檢查一次，實際通知時間可能比設定的整點晚，最晚約一小時，省電模式下可能更久。沒有設時間上限，過了設定整點後的第一次更新就會通知。
- 沒放桌面 widget 也能運作，只要通知開著。widget 和通知都沒開時，背景更新會直接略過，不使用網路。
- 抓預報失敗時不通知（使用過期預報可能誤判），等下次更新再試。
- 第一次開啟時會要求通知權限；拒絕的話開關維持關閉。

## 架構

單 module，依 Google 官方架構建議分層（參考 [android/nowinandroid](https://github.com/android/nowinandroid)）：

- `model/` 資料類別
- `logic/` 純 Kotlin 的判斷邏輯（由網頁版 `src/lib/*.ts` 與 `App.vue` 的組合邏輯移植，不依賴 Android）
- `data/` Repository：預報（Retrofit + kotlinx.serialization）、設定（DataStore）、行政區（assets）、定位（`LocationManager`）
- `di/` Hilt 模組
- `widget/` Glance 桌面 widget（2×2、4×2、4×3）、背景更新（WorkManager）與離線用的預報快取
- `notification/` 晚上提醒：判斷是否該通知（`shouldNotify`）與發送通知
- `ui/` Compose：`forecast/`（主畫面）、`settings/`（設定頁、App 內語言切換）、`theme/`（dynamic color + 固定語意色 + 內嵌字型）

語言切換用系統的 per-app language（`LocaleManager`），語言由系統保存；主題（跟隨系統/淺色/深色）存在 DataStore。

預報資料打網頁版已部署的 `/api/forecast`，**CWA API key 只存在 server 端，不會進 APK**。

晾衣時段有缺漏時，以現有時段的平均乾燥能力估算整段的乾燥分數；降雨判斷採現有資料的最高降雨機率。只要有一個時段即可參與近期建議排名；全部時段均無資料時無法計算。所有可比較的日期都不適合時，標示相對較佳的日期，仍建議室內晾衣。

## 測試

`logic/` 的單元測試是網頁版 vitest 案例的移植；另有 widget 資料與通知判斷（`shouldNotify`）的測試：

```
./gradlew :app:testDebugUnitTest
```

字型（Huninn、Noto Sans TC、DM Mono）皆為 SIL OFL 1.1，授權檔在 `licenses/fonts/`。

## 狀態

- [x] 專案骨架、`logic/` 移植與測試
- [x] data 層、UI、設定頁、App 內語言/主題切換
- [x] 晚上提醒：只在適合晾衣服時通知，時間可在設定調整（搭在 widget 的每小時背景更新上）
- [x] 桌面 widget（Glance）：2×2、4×2、4×3