# 澳洲 9 日遊報名表

```
報名者瀏覽器 ──► GitHub Pages（frontend/，Vue 靜態網頁）
                     │  fetch JSON
                     ▼
               Render（backend/，Spring Boot）
                     │  Google Sheets API（服務帳戶）
                     ▼
               Google 試算表（一筆報名一列）
```

| 資料夾 | 內容 |
|---|---|
| `frontend/` | 報名頁 `index.html`、主辦方頁 `admin.html`。**部署設定只改 `config.js`**，行程日期改 `i18n.js` 最上面 |
| `backend/` | Spring Boot API。沒設 Google 金鑰時自動用記憶體暫存，方便本機先試 |
| `.github/workflows/pages.yml` | push 到 main 時自動把 `frontend/` 發佈到 GitHub Pages |

## API

| 方法 | 路徑 | 說明 |
|---|---|---|
| GET | `/api/health` | 健康檢查／叫醒休眠中的 Render |
| POST | `/api/registrations` | 新增報名，回傳 `{id, editToken}` |
| GET | `/api/registrations/{id}` | 帶 `X-Edit-Token` 讀回自己的報名 |
| PUT | `/api/registrations/{id}` | 帶 `X-Edit-Token` 修改自己的報名 |
| GET | `/api/admin/registrations` | 帶 `X-Admin-Token` 讀全部報名＋統計 |

---

## 步驟 1：本機試跑（不需要任何帳號）

```bash
# 終端機 1：後端
cd backend
mvn spring-boot:run          # 或：mvn package -DskipTests && java -jar target/app.jar
```
```bash
# 終端機 2：前端（Java 18+ 內建的小型網頁伺服器；或用 VS Code 的 Live Server 開 5500 port）
jwebserver -d "<完整路徑>/frontend" -p 5500
```
打開 http://localhost:5500 填寫送出。主辦方頁 http://localhost:5500/admin.html
（本機要看主辦方頁，啟動後端前先設環境變數 `ADMIN_TOKEN`，例如 PowerShell：`$env:ADMIN_TOKEN="local-admin"`）

## 步驟 2：建立 Google 試算表 + 服務帳戶

1. 到 Google 雲端硬碟新建一份試算表，例如「澳洲9日遊報名」。
   網址 `https://docs.google.com/spreadsheets/d/【這一段】/edit` 的【這一段】就是 **SHEET_ID**。
2. 到 https://console.cloud.google.com/ → 建立新專案（例如 `tour-registration`）。
3. 左上選單 → **API 和服務 → 程式庫** → 搜尋 **Google Sheets API** → 啟用。
4. **API 和服務 → 憑證 → 建立憑證 → 服務帳戶**，名稱隨意，角色可略過 → 完成。
5. 點進剛建立的服務帳戶 → **金鑰 → 新增金鑰 → JSON** → 會下載一個 `.json` 檔。
   把它放到 `backend/secrets/`（這個資料夾已被 `.gitignore` 排除，**絕對不要上傳到 GitHub**）。
6. 打開 JSON 檔找到 `client_email`（長得像 `xxx@xxx.iam.gserviceaccount.com`），
   回到試算表 → **共用** → 貼上這個 email → 權限選 **編輯者** → 傳送。

本機接上試算表測試（PowerShell）：
```powershell
cd backend
$env:SHEET_ID="你的SHEET_ID"
$env:GOOGLE_CREDENTIALS_FILE="secrets\你的金鑰檔.json"
$env:ADMIN_TOKEN="local-admin"
mvn spring-boot:run
```
送出一筆後，試算表會自動多出「報名名單」分頁、標題列和資料。

## 步驟 3：上傳到 GitHub

1. 在 GitHub 建立新的 repository（例如 `tour-registration`，Public 才能用免費 Pages）。
2. 在專案根目錄：
   ```bash
   git init -b main
   git add .
   git status            # ⚠ 確認清單裡沒有任何金鑰 .json
   git commit -m "Initial commit"
   git remote add origin https://github.com/<你的帳號>/tour-registration.git
   git push -u origin main
   ```
3. GitHub repo → **Settings → Pages → Build and deployment → Source** 選 **GitHub Actions**。
4. 到 **Actions** 分頁看部署完成，網址會是 `https://<你的帳號>.github.io/tour-registration/`。

## 步驟 4：部署後端到 Render

1. https://render.com/ 用 GitHub 登入 → **New → Web Service** → 選剛剛的 repo。
2. 設定：
   - **Root Directory**：`backend`
   - **Runtime / Language**：Docker
   - **Instance Type**：Free
3. **Environment Variables**：

   | Key | Value |
   |---|---|
   | `SHEET_ID` | 步驟 2 的 SHEET_ID |
   | `GOOGLE_CREDENTIALS_JSON` | 金鑰 JSON 檔的**整段內容**（打開檔案全選複製貼上） |
   | `ADMIN_TOKEN` | 自己設一組長密碼，主辦方頁登入用 |
   | `ALLOWED_ORIGINS` | `https://<你的帳號>.github.io`（只要網域，不要加路徑和結尾 `/`） |

4. 建立後等它 build 完（第一次約 5–10 分鐘），得到網址例如 `https://tour-registration.onrender.com`。
   打開 `https://…onrender.com/api/health` 看到 `{"status":"ok"}` 就成功了。

## 步驟 5：前端接上後端

1. 改 `frontend/config.js` 的 `API_BASE` 為你的 Render 網址；`SHEET_URL` 可填試算表網址。
2. `git add . && git commit -m "Set API URL" && git push`，等 Actions 跑完。
3. 打開 GitHub Pages 網址實際報名一筆，確認試算表有資料、`admin.html` 看得到。

---

## 注意事項

- **Render 免費方案會休眠**：閒置 15 分鐘後睡著，下一個人打開頁面要等 30–60 秒。前端已經會一打開就先叫醒它並顯示提示。
- **報名者修改資料**：送出後瀏覽器會記住「報名編號＋修改憑證」，同一台裝置、同一個瀏覽器再打開就能修改。換裝置或清除瀏覽器資料就改不了，請他聯絡主辦方，由主辦方直接改試算表。
- **主辦方直接改試算表**：可以改內容、排序，但**不要改第 1 列標題、不要刪「報名編號」與最後一欄「同行者資料(系統用)」**。要刪除某筆報名，直接刪掉那一列即可。
- **金鑰外洩怎麼辦**：到 Google Cloud 該服務帳戶 → 金鑰 → 刪除舊金鑰、建立新金鑰，更新 Render 的 `GOOGLE_CREDENTIALS_JSON`。
- **改選項**（例如加一種房型）：`frontend/i18n.js` 的 `OPTS`、`backend/.../model/Options.java` 的對照表與 `*_RE` 都要改。
