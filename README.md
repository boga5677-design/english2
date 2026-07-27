# TOEIC Pet 3000 v6

已加入：

- 寵物主題首頁
- 使用使用者提供的貓咪與狗狗照片（共 9 張）
- Android Text-to-Speech 美式英文發音
- 單字搜尋
- 單字測驗
- 收藏與錯題本
- 學習進度
- GitHub Actions 自動產生 APK

## 產生 APK

1. 解壓縮 ZIP。
2. 將解壓後的內容上傳到 GitHub Repository 根目錄。
3. 確認第一層可看到 `app`、`build.gradle.kts`、`settings.gradle.kts`。
4. 到 Actions 執行 `Build TOEIC Pet v6 APK`。
5. 在成功的工作流程頁面下載 `TOEIC-Pet-v6-APK`。

## 單字庫

目前沿用原專案的 TSV 單字庫，實際載入筆數會顯示在 App 內。
要擴充時，替換：

`app/src/main/assets/toeic_words.tsv`

每列格式：

`編號<TAB>英文<TAB>中文`
