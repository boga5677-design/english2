# TOEIC 3000 v5 Stable

此版本已修正 GitHub Actions 的 Material 3 Experimental API 編譯錯誤。

## 修正內容

- `ToeicApp.kt` 已加入 `ExperimentalMaterial3Api` opt-in
- Java 17
- Gradle 8.10.2
- GitHub Actions 自動產生 Debug APK
- APK Artifact：`TOEIC3000-v5-Stable-APK`
- 內含 3001 筆 TSV 資料列

## 上傳

1. 解壓縮本 ZIP。
2. 把全部內容上傳到 GitHub repository 根目錄並覆蓋舊檔。
3. 刪除 `.github/workflows/` 內其他舊的 `.yml` 或 `.yaml`。
4. 到 `Actions → Build TOEIC 3000 v5 APK → Run workflow`。
5. 成功後下載 `TOEIC3000-v5-Stable-APK`。
