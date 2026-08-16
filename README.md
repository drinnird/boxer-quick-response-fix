# Boxer Quick Responses blank-line fix

This repository builds a Morphe `.mpp` patch for Workspace ONE Boxer (`com.boxer.email`).

The patch fixes Quick Responses losing blank lines when inserted into the HTML composer. It modifies only `assets/selection.js`: newline characters are converted to real `<br>` DOM nodes during insertion.

## Build on GitHub (no computer required)

1. Upload this repository to GitHub.
2. Open the repository's **Actions** tab.
3. Open **Build Boxer Quick Response Patch**.
4. Tap **Run workflow**.
5. When the run finishes, open it and download the artifact named **Boxer-QuickResponses-Morphe-Patch**.
6. Extract the downloaded ZIP. It contains the `.mpp` file.
7. On Android, tap the `.mpp` file and open it with Morphe Manager. Recent Morphe Manager releases support opening `.mpp` patch sources directly from a file manager.
8. In Morphe, select Boxer and apply **Fix Boxer Quick Response blank lines**.

Keep your original Boxer APK/app until you have verified the patched build works with your account and device management setup.
