/* ====== 部署設定：只需要改這個檔案 ====== */
window.APP_CONFIG = {
  // 後端網址。本機開發時自動連 localhost:8080；上線後連 Render。
  // 👉 部署到 Render 後，把下面的網址換成你的 Render 服務網址（結尾不要加 /）
  API_BASE: (location.hostname === "localhost" || location.hostname === "127.0.0.1")
    ? "http://localhost:8080"
    : "https://tour-registration.onrender.com",

  // （選填）Google 試算表網址，主辦方頁面會顯示「開啟試算表」連結
  SHEET_URL: ""
};
