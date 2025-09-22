# Google Play JSON Key Configuration

This document explains how to configure the Google Play Console JSON key for automated Android app deployments.

## Overview

The AllesTeurer project supports two methods for configuring the Google Play JSON key:

1. **Base64-encoded key (Recommended)** - Secure for CI/CD environments
2. **File path (Legacy)** - For local development only

## Method 1: Base64-Encoded Key (Recommended)

### Step 1: Encode Your JSON Key

Use the provided helper script to encode your Google Play Service Account JSON key:

```bash
# Run the encoding script
./encode-json-key.sh /path/to/your-service-account.json
```

This script will:

- Encode your JSON key to base64
- Validate the encoding works correctly
- Show you the base64 string to copy

### Step 2: Configure Environment Variable

Add the base64-encoded key to your `.env` file:

```bash
# In your .env file
GOOGLE_PLAY_JSON_KEY_BASE64=LS0tLS1CRUdJTi...your_base64_encoded_json_here...
```

### Step 3: Update GitHub Secrets (for CI/CD)

Push the environment variables to GitHub repository secrets:

```bash
gh secret set -f .env
```

## Method 2: File Path (Legacy)

For local development, you can still use the file path approach:

```bash
# In your .env file
GOOGLE_PLAY_JSON_KEY_PATH=~/path/to/your-service-account.json
```

## How It Works

The Fastlane configuration automatically detects which method you're using:

1. **Base64 method**: If `GOOGLE_PLAY_JSON_KEY_BASE64` is set, it decodes the base64 content and creates a temporary JSON file
2. **File path method**: If `GOOGLE_PLAY_JSON_KEY_PATH` is set, it uses the file directly
3. **No configuration**: Skips Google Play uploads and only builds the APK/AAB

## Fastlane Lanes That Use This Configuration

- `android beta` - Uploads APK to Internal App Sharing
- `android release` - Uploads AAB to Play Console (Internal Track)

## Security Notes

- **Base64 encoding is NOT encryption** - it's just encoding for storage convenience
- Always treat the encoded key as sensitive data
- Store it only in secure environment variables or GitHub secrets
- The temporary file created from base64 is automatically cleaned up after use

## Troubleshooting

### Invalid Base64 Error

If you see "Failed to decode base64 Google Play JSON key":

1. Ensure your base64 string is complete (no missing characters)
2. Re-run the encoding script to generate a fresh base64 string
3. Make sure there are no extra spaces or line breaks in the environment variable

### File Not Found Error

If using the file path method and getting "not found" errors:

1. Check the file path is correct and accessible
2. Use absolute paths instead of relative paths
3. Ensure the JSON key file has proper read permissions

### Upload Failures

If uploads to Google Play fail:

1. Verify your service account has the correct permissions
2. Check that the JSON key is valid and not expired
3. Ensure your app is properly set up in Google Play Console

## Manual Encoding (Alternative)

If you prefer to encode manually without the script:

```bash
# Encode JSON key to base64 (Linux/macOS)
base64 -w 0 /path/to/your-service-account.json

# Copy the output and paste it into your .env file
```
