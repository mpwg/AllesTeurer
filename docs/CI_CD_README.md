# AllesTeurer CI/CD Pipeline

[![Build and Test](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml)

This repository includes a comprehensive CI/CD pipeline using GitHub Actions for building, testing, and deploying the AllesTeurer Kotlin Multiplatform Mobile (KMP) app.

---

## 🚀 Features

- **Multi-platform builds**: Supports both iOS and Android
- **Automated testing**: Runs unit tests and static analysis (Detekt)
- **Build artifacts**: Saves build outputs for debugging and distribution
- **Smart triggers**: Different workflows for PRs, feature branches, and releases
- **Manual deployment**: Staging and production deploys via workflow dispatch

---

## 🏗️ Workflow Overview

### Build Matrix

| Platform   | Runner          | Tools                | Output                     |
| ---------- | --------------- | -------------------- | -------------------------- |
| iOS        | `macos-26`      | Xcode 26.0, Java 21  | Simulator build logs       |
| Android    | `ubuntu-latest` | Java 21, Android SDK | APK files                  |
| Validation | `ubuntu-latest` | Java 21, Gradle      | Lint reports, test results |

### Triggers

| Event             | iOS Build | Android Build | Deploy             |
| ----------------- | --------- | ------------- | ------------------ |
| Push to `main`    | ✅        | ✅            | 🚀 Production      |
| Push to `develop` | ✅        | ✅            | 🧪 Staging         |
| Pull Request      | ❌        | ✅            | ✅ Validation only |
| Manual dispatch   | 🎛️        | 🎛️            | ⚙️ Configurable    |

---

## 📋 Prerequisites

### 1. GitHub Secrets Setup

Configure repository secrets for signing credentials and API keys. See [docs/CI_SETUP.md](docs/CI_SETUP.md).

### 2. Fastlane Match Setup (iOS only)

Manage iOS code signing certificates with Fastlane Match.

---

## 🎯 Usage

### Submitting Test Builds

- **Android**: On every push/PR to `main` or `develop`, the workflow builds the APK and uploads it as an artifact (`android-debug-apk`).
  - To download: Go to the relevant workflow run in GitHub Actions → Artifacts → Download `android-debug-apk`.
  - For PRs, a comment is posted with a link to the APK artifact.
- **iOS**: On push to `main`/`develop` or manual trigger, the workflow builds the iOS simulator app. Build logs are uploaded as artifacts on failure.

### Submitting Release Builds / Deployments

- **Manual Trigger Required**: Go to GitHub → Actions → Build and Test → Run workflow. Select `deploy_target`:
  - `staging`: Deploys Android APK to Firebase App Distribution (testers group). Requires secrets: `FIREBASE_APP_ID_ANDROID`, `FIREBASE_SERVICE_ACCOUNT`.
  - `production`: Manual steps required (see workflow output):
    1. Download APK artifact
    2. Sign APK locally
    3. Upload to Google Play Console
    4. Build iOS release on local Mac
    5. Submit to App Store Connect

---

## 📊 Monitoring & Artifacts

- **Artifacts**: APKs, test reports, and iOS build logs are uploaded for each run. Access via the workflow run page.
- **Status**: Build status and summary are posted in the workflow summary and PR comments.

---

## 🛠️ Development Workflow

- **Feature branches**: Validation builds only (no deploy)
- **Develop branch**: Deploys to staging (Firebase testers)
- **Main branch**: Deploys to production (manual steps)

---

## 🆘 Troubleshooting

- Check workflow logs and artifacts for errors.
- For deployment, ensure all required secrets are set.
- For manual production deploy, follow the steps in the workflow output.

---

## 📄 License

MIT License - see [LICENSE](LICENSE)

---

### Last updated: 2025-09-21

---

> This documentation was updated to reflect the current CI workflow. For questions, see [CI Setup](./CI_SETUP.md) or ask in the [repository discussions](https://github.com/mpwg/AllesTeurer/discussions)
