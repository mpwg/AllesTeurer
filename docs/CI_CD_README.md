# AllesTeurer CI/CD Pipeline

[![Build and Test](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/mpwg/AllesTeurer/actions/workflows/build-and-test.yml)

This repository includes a comprehensive CI/CD pipeline using GitHub Actions and Fastlane for building and deploying the AllesTeurer Kotlin Multiplatform Mobile (KMP) app.

## 🚀 Features

- **Multi-platform builds**: Supports both iOS and Android
- **Automated testing**: Runs unit tests and static analysis (Detekt)
- **Code signing**: Automatic iOS certificate management with Fastlane Match
- **Deployment**: Automated deployment to TestFlight and Google Play
- **Build artifacts**: Saves build outputs for debugging and distribution
- **Smart triggers**: Different workflows for PRs, feature branches, and releases

## 🏗️ Workflow Overview

### Build Matrix

| Platform   | Runner          | Tools                          | Output                     |
| ---------- | --------------- | ------------------------------ | -------------------------- |
| iOS        | `macos-14`      | Xcode 16.1, Ruby 3.2, Fastlane | IPA files for simulator    |
| Android    | `ubuntu-latest` | Java 17, Android SDK, Fastlane | APK/AAB files              |
| Validation | `ubuntu-latest` | Java 17, Gradle                | Lint reports, test results |

### Triggers

| Event             | iOS Build | Android Build | Deploy             |
| ----------------- | --------- | ------------- | ------------------ |
| Push to `main`    | ✅        | ✅            | 🚀 Production      |
| Push to `develop` | ✅        | ✅            | 🧪 Staging         |
| Pull Request      | ❌        | ❌            | ✅ Validation only |
| Manual dispatch   | 🎛️        | 🎛️            | ⚙️ Configurable    |

## 📋 Prerequisites

### 1. GitHub Secrets Setup

You'll need to configure repository secrets with your signing credentials and API keys. See detailed instructions in [docs/CI_SETUP.md](docs/CI_SETUP.md).

#### Quick Setup with Script

If you have the GitHub CLI installed:

```bash
# Run the automated setup script
./scripts/setup-github-secrets.sh
```

#### Manual Setup

Required secrets:

- **iOS**: `APPLE_ID`, `DEVELOPMENT_TEAM`, `APPSTORE_TEAM_ID`, `APP_STORE_CONNECT_API_KEY_*`
- **Code Signing**: `MATCH_PASSWORD`, `MATCH_GIT_URL`, `MATCH_GIT_BASIC_AUTHORIZATION`
- **Android**: `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`
- **Google Play**: `GOOGLE_PLAY_JSON_KEY_PATH`

### 2. Fastlane Match Setup

Ensure your iOS code signing certificates are managed by Fastlane Match:

```bash
# Initialize Match (one-time setup)
cd fastlane
bundle exec fastlane match init

# Generate certificates
bundle exec fastlane match development
bundle exec fastlane match appstore
```

## 🎯 Usage

### Manual Workflow Dispatch

You can manually trigger builds with different options:

1. Go to **Actions** → **Build and Test** → **Run workflow**
2. Choose your build type:
   - `all`: Build both iOS and Android
   - `ios-only`: Build only iOS
   - `android-only`: Build only Android
   - `validation-only`: Run tests and linting only

### Branch-Based Deployment

- **Feature branches** (`feature/*`, `fix/*`): Validation builds only
- **Development branch** (`develop`): Deploy to staging (TestFlight + Internal App Sharing)
- **Main branch** (`main`): Deploy to production (App Store + Google Play)

## 📊 Monitoring

### Build Artifacts

Each successful build produces artifacts:

- **iOS**: `ios-build-artifacts` (IPA files, build logs)
- **Android**: `android-build-artifacts` (APK/AAB files, build logs)
- **Reports**: `detekt-reports`, `test-reports` (static analysis, test results)

### Status Monitoring

- **GitHub Actions tab**: Real-time build status and logs
- **Pull Request checks**: Automatic validation on PRs
- **Status badge**: Shows current build status in README

## 🛠️ Development Workflow

### For Pull Requests

1. Create feature branch: `git checkout -b feature/my-feature`
2. Make changes and commit
3. Push branch: `git push origin feature/my-feature`
4. Create PR → Validation builds run automatically
5. Merge after approval → No deployment (validation only)

### For Releases

1. Merge to `develop` → Deploy to staging environments
2. Test in staging
3. Merge `develop` to `main` → Deploy to production

## 🔧 Customization

### Fastlane Lanes

The workflow uses these Fastlane lanes:

- `fastlane ios build`: Build iOS for simulator (CI validation)
- `fastlane ios beta`: Build and upload to TestFlight
- `fastlane ios release`: Build and upload to App Store
- `fastlane android build`: Build Android debug APK
- `fastlane android beta`: Build and upload to Internal App Sharing
- `fastlane android release`: Build and upload to Google Play

### Workflow Modifications

Edit `.github/workflows/build-and-test.yml` to:

- Add new build steps
- Change deployment targets
- Modify trigger conditions
- Add additional platforms

## 🆘 Troubleshooting

### Common Issues

1. **iOS build fails**: Check Apple Developer account credentials and certificates
2. **Android signing fails**: Verify keystore passwords in secrets
3. **Upload failures**: Check API keys and permissions for App Store/Google Play
4. **Dependency issues**: Clear caches or update Gradle/Ruby dependencies

### Debugging

1. Enable debug logging: Set repository secret `ACTIONS_STEP_DEBUG` to `true`
2. Check Fastlane logs in the Actions tab for detailed errors
3. Download build artifacts to examine outputs locally
4. Use manual workflow dispatch to test specific components

### Support

- 📖 [Detailed Setup Guide](docs/CI_SETUP.md)
- 🔧 [GitHub Actions Documentation](https://docs.github.com/en/actions)
- 🚀 [Fastlane Documentation](https://docs.fastlane.tools)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
