#!/bin/bash

# GitHub Actions Secrets Setup Script for AllesTeurer
# This script helps you set up GitHub repository secrets from your .env file

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 AllesTeurer CI/CD Setup Script${NC}"
echo "=================================================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Error: .env file not found in current directory${NC}"
    echo "Please run this script from the root of your repository where .env is located."
    exit 1
fi

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ Error: GitHub CLI (gh) is not installed${NC}"
    echo "Please install GitHub CLI: https://cli.github.com/"
    echo "Or set up secrets manually using the documentation in docs/CI_SETUP.md"
    exit 1
fi

# Check if user is logged in to GitHub CLI
if ! gh auth status &> /dev/null; then
    echo -e "${YELLOW}⚠️  You need to authenticate with GitHub CLI${NC}"
    echo "Run: gh auth login"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"
echo

# Function to set a secret
set_secret() {
    local secret_name="$1"
    local env_var="$2"
    
    # Get value from .env file
    local value=$(grep "^${env_var}=" .env | cut -d'=' -f2- | sed 's/^"//' | sed 's/"$//')
    
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⚠️  ${secret_name}: No value found for ${env_var} in .env file${NC}"
        return
    fi
    
    # Set the secret
    echo -n "Setting ${secret_name}... "
    if echo "$value" | gh secret set "$secret_name"; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
}

echo -e "${BLUE}📱 Setting up iOS secrets...${NC}"
set_secret "APPLE_ID" "APPLE_ID"
set_secret "DEVELOPMENT_TEAM" "DEVELOPMENT_TEAM"
set_secret "APPSTORE_TEAM_ID" "APPSTORE_TEAM_ID"
set_secret "APP_STORE_CONNECT_API_KEY_KEY_ID" "APP_STORE_CONNECT_API_KEY_KEY_ID"
set_secret "APP_STORE_CONNECT_API_KEY_ISSUER_ID" "APP_STORE_CONNECT_API_KEY_ISSUER_ID"
set_secret "APP_STORE_CONNECT_API_KEY_CONTENT" "APP_STORE_CONNECT_API_KEY_CONTENT"

echo
echo -e "${BLUE}🔐 Setting up code signing secrets...${NC}"
set_secret "MATCH_PASSWORD" "MATCH_PASSWORD"
set_secret "MATCH_GIT_URL" "MATCH_GIT_URL"
set_secret "MATCH_GIT_BASIC_AUTHORIZATION" "MATCH_GIT_BASIC_AUTHORIZATION"
set_secret "KEYCHAIN_PASSWORD" "KEYCHAIN_PASSWORD"

echo
echo -e "${BLUE}🤖 Setting up Android secrets...${NC}"
set_secret "ANDROID_KEYSTORE_PASSWORD" "ANDROID_KEYSTORE_PASSWORD"
set_secret "ANDROID_KEY_ALIAS" "ANDROID_KEY_ALIAS"
set_secret "ANDROID_KEY_PASSWORD" "ANDROID_KEY_PASSWORD"

# Special handling for Google Play JSON key
echo -n "Setting GOOGLE_PLAY_JSON_KEY_PATH... "
GOOGLE_PLAY_JSON_PATH=$(grep "^GOOGLE_PLAY_JSON_KEY_PATH=" .env | cut -d'=' -f2)
if [ -n "$GOOGLE_PLAY_JSON_PATH" ]; then
    # Expand the tilde to home directory
    EXPANDED_PATH="${GOOGLE_PLAY_JSON_PATH/#\~/$HOME}"
    
    if [ -f "$EXPANDED_PATH" ]; then
        if cat "$EXPANDED_PATH" | gh secret set "GOOGLE_PLAY_JSON_KEY_PATH"; then
            echo -e "${GREEN}✅${NC}"
        else
            echo -e "${RED}❌ Failed to set JSON content${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  JSON file not found at: $EXPANDED_PATH${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  No GOOGLE_PLAY_JSON_KEY_PATH found in .env${NC}"
fi

echo
echo -e "${GREEN}🎉 Setup completed!${NC}"
echo
echo -e "${BLUE}Next steps:${NC}"
echo "1. 📝 Review the setup documentation: docs/CI_SETUP.md"
echo "2. 🧪 Test your CI/CD pipeline by pushing to a feature branch"
echo "3. 👀 Monitor workflow runs in the GitHub Actions tab"
echo "4. 🔒 Make sure to add .env to your .gitignore file"
echo
echo -e "${YELLOW}⚠️  Security reminder:${NC}"
echo "Never commit your .env file to the repository!"
echo "All sensitive data should only exist in GitHub Secrets."