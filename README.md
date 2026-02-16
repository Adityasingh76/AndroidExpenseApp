# ExpensePilot (Android Expense Management App)

ExpensePilot is an Android app focused on UPI-first expense tracking.

## Core UPI flow
1. Scan merchant UPI QR.
2. Enter payment amount.
3. Pick or type expense category.
4. Choose installed UPI app (GPay, Paytm, PhonePe, etc.).
5. Open selected UPI app with amount + note prefilled.
6. Save expense metadata for reporting and export.

## Built-in features
- Predefined categories + add custom categories on the fly.
- Manual expense entry (for cash/card/non-UPI spending).
- Expense timeline/history list.
- Monthly total and category-wise breakdown summary.
- CSV export and share.
- Local persistence using Room database.

## APK
After build, install debug APK from:
`app/build/outputs/apk/debug/app-debug.apk`

## Build
```bash
./gradlew assembleDebug -Pandroid.builder.sdkDownload=true
```

If SDK licenses are missing in a fresh environment, accept or provide Android SDK licenses before build.

## Repository policy
- Binary files are not tracked in git in this repository.
- The Gradle wrapper JAR is intentionally excluded; use a locally installed `gradle` to regenerate wrapper files if needed.

## Generate APK artifact
Run:
```bash
./scripts/build_apk.sh
```
The generated APK is created at:
`app/build/outputs/apk/debug/app-debug.apk`

> Note: APK files are binary artifacts and are intentionally **not committed** to git in this repo.
