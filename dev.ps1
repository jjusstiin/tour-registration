# 本機預覽：一鍵啟動後端 + 前端，並打開瀏覽器
#
# 用法（在 VS Code 終端機，專案根目錄）：
#   .\dev.ps1           → 資料存記憶體（安全，關掉就清空，不會寫進 Google 試算表）
#   .\dev.ps1 -Sheet    → 連真的 Google 試算表（需要 backend\secrets\ 裡有金鑰）
#
# 要停止：關掉跳出來的兩個視窗即可。
param([switch]$Sheet)

$root = $PSScriptRoot
$backendEnv = "`$env:ADMIN_TOKEN='local-admin';"

if ($Sheet) {
    $key = Get-ChildItem "$root\backend\secrets\*.json" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $key) { Write-Host "找不到金鑰：請把 Google 服務帳戶 JSON 放進 backend\secrets\" -ForegroundColor Red; exit 1 }
    $sheetId = Read-Host "請輸入 SHEET_ID（試算表網址 /d/ 後面那串）"
    $backendEnv += "`$env:SHEET_ID='$sheetId'; `$env:GOOGLE_CREDENTIALS_FILE='$($key.FullName)';"
    Write-Host "⚠ 連到真的試算表：測試資料記得刪掉" -ForegroundColor Yellow
} else {
    Write-Host "資料存在記憶體，不會寫進試算表" -ForegroundColor Green
}

# 後端（port 8080）
Start-Process powershell -ArgumentList '-NoExit', '-Command',
    "`$host.UI.RawUI.WindowTitle='後端 :8080'; Set-Location '$root\backend'; $backendEnv mvn spring-boot:run"

# 前端（port 5500）
Start-Process powershell -ArgumentList '-NoExit', '-Command',
    "`$host.UI.RawUI.WindowTitle='前端 :5500'; jwebserver -d '$root\frontend' -p 5500"

Write-Host "後端啟動約需 20 秒…"
Start-Sleep -Seconds 4
Start-Process "http://localhost:5500"
Write-Host ""
Write-Host "報名頁    http://localhost:5500"
Write-Host "主辦方頁  http://localhost:5500/admin.html   （密碼 local-admin）"
