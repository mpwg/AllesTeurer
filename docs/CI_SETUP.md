# GitHub Actions Secrets Setup

This document outlines all the GitHub repository secrets that need to be configured for the CI/CD pipeline to work properly.

## Setting Up Repository Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret** for each secret below

## Required Secrets

### iOS Code Signing & App Store Connect

| Secret Name                           | Description                        | Source                   |
| ------------------------------------- | ---------------------------------- | ------------------------ |
| `APPLE_ID`                            | Your Apple Developer account email | Find in your `.env` file |
| `DEVELOPMENT_TEAM`                    | Apple Developer Team ID            | Find in your `.env` file |
| `APPSTORE_TEAM_ID`                    | App Store Connect Team ID          | Find in your `.env` file |
| `APP_STORE_CONNECT_API_KEY_KEY_ID`    | App Store Connect API Key ID       | Find in your `.env` file |
| `APP_STORE_CONNECT_API_KEY_ISSUER_ID` | App Store Connect API Issuer ID    | Find in your `.env` file |
| `APP_STORE_CONNECT_API_KEY_CONTENT`   | App Store Connect API Private Key  | Find in your `.env` file |

### Fastlane Match (Code Signing)

| Secret Name                     | Description                               | Source                   |
| ------------------------------- | ----------------------------------------- | ------------------------ |
| `MATCH_PASSWORD`                | Password for Match certificate encryption | Find in your `.env` file |
| `MATCH_GIT_URL`                 | Git repository URL for Match certificates | Find in your `.env` file |
| `MATCH_GIT_BASIC_AUTHORIZATION` | Base64 encoded Git credentials for Match  | Find in your `.env` file |
| `KEYCHAIN_PASSWORD`             | Password for CI keychain setup            | Find in your `.env` file |

### Android Code Signing & Google Play

| Secret Name                 | Description                              | Source                   |
| --------------------------- | ---------------------------------------- | ------------------------ |
| `ANDROID_KEYSTORE_PASSWORD` | Android keystore password                | Find in your `.env` file |
| `ANDROID_KEY_ALIAS`         | Android key alias                        | Find in your `.env` file |
| `ANDROID_KEY_PASSWORD`      | Android key password                     | Find in your `.env` file |
| `GOOGLE_PLAY_JSON_KEY_PATH` | Path to Google Play Service Account JSON | Find in your `.env` file |

## Important Security Notes

### ⚠️ Do NOT commit the .env file to your repository

The `.env` file contains sensitive credentials and should be added to your `.gitignore` file.

### 🔐 Google Play Service Account JSON

For the `GOOGLE_PLAY_JSON_KEY_PATH` secret, you'll need to:

1. Take the content of your Google Play Service Account JSON file (find the path in your `.env` file)
2. Copy the entire JSON content
3. Create a GitHub secret with the JSON content as the value

Alternatively, you can upload the JSON file as a base64-encoded secret:

```bash
# Encode the JSON file to base64 (replace with your actual path)
base64 /path/to/your/google-play-service-account.json | pbcopy
```

Then create a secret called `GOOGLE_PLAY_JSON_KEY_BASE64` with the base64 content.

### 📱 iOS Certificates

The `APP_STORE_CONNECT_API_KEY_CONTENT` should contain the full private key including:

- `-----BEGIN PRIVATE KEY-----`
- The key content (multiple lines)
- `-----END PRIVATE KEY-----`

Copy the entire content from your `.env` file exactly as it appears.

## Verification

After setting up all secrets:

1. Go to your repository's **Actions** tab
2. Trigger a workflow run (either by pushing to `main`/`develop` or using "Run workflow")
3. Check that the workflow runs without authentication errors

## Troubleshooting

### Common Issues

1. **Invalid Apple credentials**: Double-check your Apple ID and team IDs
2. **Match authentication fails**: Verify the Git authorization token is correct
3. **Android signing fails**: Ensure keystore passwords match your local setup
4. **Google Play upload fails**: Verify the service account JSON is valid

### Debug Tips

1. Enable workflow debug logging by setting repository secret `ACTIONS_STEP_DEBUG` to `true`
2. Check Fastlane logs in the Actions tab for detailed error messages
3. Verify secrets are properly masked in logs (they should appear as `***`)

## Workflow Features

### Triggers

- **Push to main/develop**: Builds and deploys
- **Pull Requests**: Validation builds only
- **Manual trigger**: Choose build type (all/iOS-only/Android-only/validation-only)

### Build Matrix

- **iOS**: Built on `macos-14` with Xcode 16.1
- **Android**: Built on `ubuntu-latest` with Java 17
- **Validation**: Lightweight build for PRs

### Artifacts

- **Detekt reports**: Static analysis results
- **Test reports**: Unit test results
- **Build artifacts**: iOS IPA and Android APK/AAB files

### Deployments

- **Develop branch** → TestFlight (iOS) + Internal App Sharing (Android)
- **Main branch** → App Store (iOS) + Google Play (Android)

## Next Steps

1. Set up all repository secrets using the values from your `.env` file
2. Add your `.env` file to `.gitignore` if not already there
3. Test the workflow by pushing to a feature branch
4. Monitor the Actions tab for successful builds
