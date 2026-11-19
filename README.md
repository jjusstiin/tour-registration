# 2027年臺澳青年推動惠民啟蘊交流活動 — 簡章與線上報名

| 網址 | 用途 |
|---|---|
| https://jjusstiin.github.io/tour-registration/ | 活動簡章＋線上報名（上方分頁切換；網址加 `#register` 直接開報名表） |
| https://jjusstiin.github.io/tour-registration/admin.html | 主辦方頁：名單＋統計（需要密碼） |
| https://tour-registration.onrender.com/api/health | 後端健康檢查，看到 `{"status":"ok"}` 代表正常 |

---

## 架構

```
             GitHub repo（jjusstiin/tour-registration）── 存放「全部」程式碼
                  │ frontend/                     │ backend/
                  ▼                               ▼
 瀏覽器 ──► GitHub Pages（前端）──API──►  Render（後端，Java Spring Boot）
           靜態網頁，不存資料                 檢查資料、驗證密碼、算統計
                                                  │ Google Sheets API（服務帳戶）
                                                  ▼
                                    Google 試算表「2027臺澳交流報名」分頁
                                    ← 唯一的資料來源（沒有另外的資料庫）
```

- **程式碼都在 GitHub**，但在兩個地方執行：前端是靜態檔案，由 GitHub Pages 提供；後端是 Java 程式，要有主機一直執行，由 Render 負責。
- **機密不在 GitHub**（repo 是公開的）：Google 金鑰、主辦方密碼都設在 Render 的 Environment。
- **後端不保存資料**，每次都透過 Google Sheets API 讀寫試算表。Render 重開、休眠都不影響資料。

### 帳號與服務

| 服務 | 用途 | 平常會去做什麼 |
|---|---|---|
| GitHub | 存程式碼、部署前端 | Actions 分頁看前端部署進度 |
| Render | 執行後端 | **改主辦方密碼**、看 log、看部署狀態 |
| Google Cloud | 服務帳戶 `sheet-writer@…`（後端用它登入試算表） | 只有換金鑰時才需要 |
| Google 試算表 | 存所有報名 | **看名單、刪資料、匯出、備份** |

### Render 環境變數

| Key | 說明 |
|---|---|
| `SHEET_ID` | 試算表網址 `/d/` 後面那串 |
| `GOOGLE_CREDENTIALS_JSON` | 服務帳戶金鑰 JSON 的整段內容 |
| `ADMIN_TOKEN` | 主辦方頁的密碼 |
| `ALLOWED_ORIGINS` | `https://jjusstiin.github.io`（允許呼叫後端的網站） |
| `SHEET_TAB` | （選填）分頁名稱，預設「2027臺澳交流報名」 |

---

## 資料夾

```
frontend/               網頁（GitHub Pages 發佈這個資料夾）
  index.html            簡章（中、英兩份內文）＋ 報名表
  admin.html            主辦方頁
  app.js / admin.js     兩個頁面的程式
  i18n.js               所有介面中英文字（含截止日、管理人資訊）
  options.js            報名表選項（地區、身分、運動、市集…）
  config.js             後端網址（本機自動連 localhost:8080）
  style.css             樣式；最上面是主色（深藍）／副色（晨曦橘）
  hero.jpg, hero-sm.jpg 頁首背景圖（桌機／手機）
backend/                後端（Render 用 backend/Dockerfile 打包）
  src/main/java/…/model/       報名資料欄位與驗證規則、選項代碼
  src/main/java/…/store/       存取資料：Google 試算表／本機記憶體＋假資料
  src/main/java/…/web/         API、CORS、錯誤處理
  src/main/resources/mock/     本機假資料（12 筆）
  secrets/                     Google 金鑰與 SHEET_ID（本機用，不會上傳）
tools/devserver.py      本機前端預覽伺服器（不快取）
dev.ps1                 本機一鍵啟動
.github/workflows/pages.yml    push 後自動發佈前端
```

---

## 常見工作

### 改網站文字、截止日
- 介面文字、截止日（`deadline_date`）、管理人資訊：`frontend/i18n.js`
- 頁首的「報名截止 2026/12/30」標籤：`frontend/i18n.js` 的 `fact_deadline`
- 簡章內文：`frontend/index.html`（中文、英文各一段，兩邊都要改）

### 改主辦方密碼
Render → tour-registration → **Environment** → 改 `ADMIN_TOKEN` → **Save, rebuild, and deploy**。約 3–5 分鐘後生效，舊密碼立刻失效。報名資料不受影響。

### 改選項（例如加一個地區）
三個地方要一起改：`frontend/options.js` 的 `OPTS`、`backend/.../model/Options.java` 的對照表，以及同一個檔案裡的 `*_RE`。

### 看資料、刪資料、備份
- 直接開 Google 試算表。可以排序、加註記、標顏色；**刪整列＝刪除那筆報名**。
- **不要**改第 1 列標題、不要刪「報名編號」與最後一欄「完整資料(系統用,請勿修改)」。系統以最後一欄為準，所以改中間欄位的文字不會反映到網站。
- 刪錯了：檔案 → 版本記錄 → 查看版本記錄 → 還原。
- 建議定期：檔案 → 建立副本。

### 報名者想修改資料
同一台裝置、同一個瀏覽器打開網站，會自動載入他之前送出的資料。換裝置或清除瀏覽器資料就改不了，由主辦方直接改試算表。

---

## 本機開發

```powershell
powershell -ExecutionPolicy Bypass -File .\dev.ps1          # 記憶體模式＋12 筆假資料（安全）
powershell -ExecutionPolicy Bypass -File .\dev.ps1 -Sheet   # 連真的 Google 試算表（⚠ 測試資料記得刪）
```

會開兩個視窗（後端 :8080、前端 :5500）並打開瀏覽器：

| 網址 | 說明 |
|---|---|
| http://localhost:5500 | 簡章＋報名表 |
| http://localhost:5500/admin.html | 主辦方頁，密碼 `local-admin` |

- 改前端檔案 → 存檔 → 瀏覽器按 F5（本機伺服器不快取，一定是最新版）
- 改後端 Java → 關掉「後端」視窗，重新執行 `dev.ps1`
- 測完把兩個視窗都關掉，避免下次 port 被佔用
- `-Sheet` 第一次會問 SHEET_ID，之後記在 `backend/secrets/sheet-id.txt`（不會上傳）

後端測試：`cd backend && mvn test`

### 上線

```bash
git add .
git commit -m "說明這次改了什麼"
git push
```

| 改到的地方 | 自動發生 | 時間 |
|---|---|---|
| `frontend/` | GitHub Actions 發佈到 Pages，CSS/JS 網址自動換成新版本號 | 約 1 分鐘 |
| `backend/` | Render 重新 build、部署 | 約 3–5 分鐘 |
| 其他（README、dev.ps1…） | 不會重新部署 | — |

進度：GitHub 的 Actions 分頁（前端）、Render 的 Deploys 頁（後端）。改壞了可以在 Render Deploys 按 Rollback，或用 `git revert`。

---

## API

| 方法 | 路徑 | 說明 |
|---|---|---|
| GET | `/api/health` | 健康檢查／叫醒休眠中的 Render |
| POST | `/api/registrations` | 新增報名，回傳 `{id, editToken}` |
| GET | `/api/registrations/{id}` | 帶 `X-Edit-Token` 讀回自己的報名 |
| PUT | `/api/registrations/{id}` | 帶 `X-Edit-Token` 修改自己的報名 |
| GET | `/api/admin/registrations` | 帶 `X-Admin-Token` 讀全部報名＋統計 |

---

## 注意事項與限制

- **Render 免費方案會休眠**：閒置 15 分鐘後睡著，下一位訪客要等 30–60 秒。網頁一打開就會先叫醒它並顯示提示。
- **Google Sheets API 每分鐘約 60 次讀寫**：一般活動規模（數百人）沒問題。
- **金鑰外洩**：Google Cloud → 服務帳戶 → 金鑰 → 刪除舊的、建立新的 → 更新 Render 的 `GOOGLE_CREDENTIALS_JSON`。
- **原始檔不上傳**：簡章 `.docx`（含聯絡人）、背景原圖 `.png`、`backend/secrets/` 都在 `.gitignore`。

---

## 從零重建（交接或換帳號時參考）

1. **Google 試算表**：新建一份，記下網址中的 SHEET_ID。
2. **Google Cloud**：建立專案 → 啟用 Google Sheets API → IAM 與管理 → 服務帳戶 → 建立 → 金鑰 → 新增 JSON 金鑰，放進 `backend/secrets/`。
3. **共用試算表**：把金鑰裡的 `client_email` 加為試算表的「編輯者」。
4. **GitHub**：建立 Public repo 並 push；Settings → Pages → Source 選「GitHub Actions」。
5. **Render**：New → Web Service → 選 repo；Language 選 Docker、Root Directory 填 `backend`、Instance Type 選 Free、Health Check Path 填 `/api/health`；填入上面的環境變數。
6. **前端接後端**：`frontend/config.js` 的 `API_BASE` 改成 Render 網址，push。
