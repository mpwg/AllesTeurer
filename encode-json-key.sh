#!/bin/bash

# Helper script to encode Google Play Service Account JSON key to base64
# Usage: ./encode-json-key.sh /path/to/your-service-account.json

if [ $# -eq 0 ]; then
    echo "Usage: $0 /path/to/your-service-account.json"
    echo ""
    echo "This script will:"
    echo "1. Encode your Google Play Service Account JSON key to base64"
    echo "2. Display the base64-encoded key for copying to your .env file"
    echo "3. Validate that the encoded key can be decoded correctly"
    exit 1
fi

JSON_FILE="$1"

if [ ! -f "$JSON_FILE" ]; then
    echo "❌ Error: File '$JSON_FILE' not found"
    exit 1
fi

echo "🔄 Encoding Google Play Service Account JSON key to base64..."
echo ""

# Encode to base64 (without line breaks)
BASE64_ENCODED=$(base64 -w 0 "$JSON_FILE" 2>/dev/null || base64 -i "$JSON_FILE")

if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to encode file to base64"
    exit 1
fi

echo "✅ Base64-encoded JSON key:"
echo "=================================================="
echo "$BASE64_ENCODED"
echo "=================================================="
echo ""

# Validate by decoding and checking if it's valid JSON
echo "🔍 Validating encoded key..."
DECODED=$(echo "$BASE64_ENCODED" | base64 -d 2>/dev/null || echo "$BASE64_ENCODED" | base64 -D)

if echo "$DECODED" | jq empty 2>/dev/null; then
    echo "✅ Validation successful: Encoded key decodes to valid JSON"
    echo ""
    echo "📋 Instructions:"
    echo "1. Copy the base64-encoded key above"
    echo "2. Paste it into your .env file as: GOOGLE_PLAY_JSON_KEY_BASE64=<encoded_key>"
    echo "3. Run: gh secret set -f .env (to update GitHub repository secrets)"
    echo ""
    echo "🔒 The encoded key is ready for use with Fastlane!"
else
    echo "❌ Validation failed: Decoded content is not valid JSON"
    echo "Please check your input file and try again."
    exit 1
fi