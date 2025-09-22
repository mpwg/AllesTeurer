# AllesTeurer CI/CD Pipeline

[![Build and Test](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml)

This repository includes a comprehensive CI/CD pipeline using GitHub Actions for building, testing, and deploying the AllesTeurer native iOS app.

---

## 🚀 Features

- **iOS-focused builds**: Native iOS app with Xcode build system
- **Automated testing**: Runs Swift Testing and XCUITest suites
- **Build artifacts**: Saves .ipa files for TestFlight and App Store distribution
- **Smart triggers**: Different workflows for PRs, feature branches, and releases
- **Manual deployment**: Staging and production deploys to TestFlight/App Store

---

## 🏗️ Workflow Overview

### Build Matrix

| Platform   | Runner     | Tools              | Output                     |
| ---------- | ---------- | ------------------ | -------------------------- |
| iOS        | `macos-14` | Xcode 15.0+, Swift | .ipa files, test reports   |
| Validation | `macos-14` | SwiftLint, Tests   | Lint reports, test results |

### Triggers

| Event             | iOS Build | Tests | Deploy             |
| ----------------- | --------- | ----- | ------------------ |
| Push to `main`    | ✅        | ✅    | 🚀 App Store       |
| Push to `develop` | ✅        | ✅    | 🧪 TestFlight      |
| Pull Request      | ✅        | ✅    | ✅ Validation only |
| Manual dispatch   | 🎛️        | 🎛️    | ⚙️ Configurable    |

---

## 📋 Prerequisites

### 1. GitHub Secrets Setup

Configure repository secrets for iOS signing credentials and App Store Connect API. See [docs/CI_SETUP.md](docs/CI_SETUP.md).

### 2. App Store Connect API Key

Set up API key for automated TestFlight and App Store deployments.

---

---

## 🎯 Usage

### Submitting Test Builds

- **iOS TestFlight**: On every push/PR to `main` or `develop`, the workflow builds the iOS app and uploads it to TestFlight for testing.
  - Test builds are automatically distributed to internal testers
  - External testers require manual approval in App Store Connect
- **Local Testing**: For PRs, the workflow builds for iOS Simulator and uploads build logs as artifacts

### Submitting Release Builds / Deployments

- **Manual Trigger Required**: Go to GitHub → Actions → Build and Test → Run workflow. Select `deploy_target`:
  - `testflight`: Deploys iOS app to TestFlight for beta testing. Requires secrets: `APP_STORE_CONNECT_API_KEY`, `MATCH_PASSWORD`.
  - `appstore`: Submits to App Store for review and release. Manual steps required (see workflow output):
    1. Download .ipa artifact
    2. Upload to App Store Connect via Xcode or Transporter
    3. Submit for App Store review

---

## 📊 Monitoring & Artifacts

- **Artifacts**: .ipa files, test reports, and build logs are uploaded for each run. Access via the workflow run page.
- **Status**: Build status and summary are posted in the workflow summary and PR comments.
- **TestFlight**: Automatic upload to TestFlight for main/develop branches with build notifications

---

## 🛠️ Development Workflow

- **Feature branches**: Validation builds only (Simulator builds + tests)
- **Develop branch**: Deploys to TestFlight internal testing
- **Main branch**: Deploys to TestFlight external testing and App Store submission

---

## 🆘 Troubleshooting

- Check Xcode build logs and test reports in workflow artifacts
- For TestFlight deployment, ensure App Store Connect API key is configured
- For local development issues, verify Xcode version compatibility (15.0+)
- Check iOS deployment target matches project settings (iOS 17.0+)

---

## 📄 License

MIT License - see [LICENSE](LICENSE)

---

### Last updated: 2025-09-22

---

> This documentation was updated to reflect the iOS-first workflow. For questions, see [CI Setup](./CI_SETUP.md) or ask in the [repository discussions](https://github.com/mpwg/AllesTeurer/discussions)
