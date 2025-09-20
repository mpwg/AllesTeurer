Monorepo Setup for Alles Teuer

Overview

Configure a complete monorepo setup using Turborepo for managing iOS, Android, and Web applications with shared backend services.

Directory Structure

Code
alles-teuer/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml
│   │   ├── deploy-production.yml
│   │   └── deploy-staging.yml
├── apps/
│   ├── ios/
│   │   ├── AllesTeurer.xcodeproj
│   │   ├── AllesTeurer/
│   │   └── Podfile
│   ├── android/
│   │   ├── app/
│   │   ├── build.gradle
│   │   └── settings.gradle
│   ├── web/
│   │   ├── src/
│   │   ├── package.json
│   │   └── next.config.js
│   └── backend/
│       ├── src/
│       ├── package.json
│       └── nest-cli.json
├── packages/
│   ├── shared/
│   ├── ui-components/
│   ├── api-client/
│   └── database/
├── turbo.json
├── package.json
└── pnpm-workspace.yaml
Configuration Files

Root package.json

JSON
{
  "name": "alles-teuer",
  "private": true,
  "workspaces": [
    "apps/*",
    "packages/*"
  ],
  "scripts": {
    "dev": "turbo run dev",
    "build": "turbo run build",
    "test": "turbo run test",
    "lint": "turbo run lint",
    "format": "prettier --write \"**/*.{ts,tsx,js,jsx,md}\"",
    "db:generate": "turbo run db:generate",
    "db:migrate": "turbo run db:migrate",
    "db:seed": "turbo run db:seed"
  },
  "devDependencies": {
    "turbo": "latest",
    "prettier": "^3.0.0",
    "eslint": "^8.0.0",
    "@typescript-eslint/parser": "^6.0.0",
    "@typescript-eslint/eslint-plugin": "^6.0.0"
  }
}
Turbo Configuration (turbo.json)

JSON
{
  "$schema": "https://turbo.build/schema.json",
  "globalDependencies": ["**/.env.*local"],
  "pipeline": {
    "build": {
      "dependsOn": ["^build"],
      "outputs": [".next/**", "!.next/cache/**", "dist/**"]
    },
    "dev": {
      "cache": false,
      "persistent": true
    },
    "test": {
      "dependsOn": ["build"],
      "outputs": ["coverage/**"]
    },
    "lint": {},
    "db:generate": {
      "cache": false
    },
    "db:migrate": {
      "cache": false
    }
  }
}
Docker Compose for Local Development

YAML
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: alles_teuer
      POSTGRES_PASSWORD: local_dev_password
      POSTGRES_DB: alles_teuer_dev
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

  backend:
    build:
      context: .
      dockerfile: apps/backend/Dockerfile.dev
    ports:
      - "4000:4000"
    environment:
      NODE_ENV: development
      DATABASE_URL: postgresql://alles_teuer:local_dev_password@postgres:5432/alles_teuer_dev
      REDIS_URL: redis://redis:6379
      S3_ENDPOINT: http://minio:9000
    depends_on:
      - postgres
      - redis
      - minio
    volumes:
      - ./apps/backend:/app
      - /app/node_modules

volumes:
  postgres_data:
  redis_data:
  minio_data:
Development Setup Scripts

setup.sh

bash
#!/bin/bash

echo "🚀 Setting up Alles Teuer development environment..."

# Check prerequisites
command -v node >/dev/null 2>&1 || { echo "Node.js is required but not installed. Aborting." >&2; exit 1; }
command -v pnpm >/dev/null 2>&1 || { echo "Installing pnpm..."; npm install -g pnpm; }
command -v docker >/dev/null 2>&1 || { echo "Docker is required but not installed. Aborting." >&2; exit 1; }

# Install dependencies
echo "📦 Installing dependencies..."
pnpm install

# Setup environment files
echo "🔧 Setting up environment files..."
cp .env.example .env.local
cp apps/backend/.env.example apps/backend/.env.local
cp apps/web/.env.example apps/web/.env.local

# Start infrastructure
echo "🐳 Starting Docker services..."
docker-compose up -d

# Wait for services
echo "⏳ Waiting for services to be ready..."
sleep 10

# Run database migrations
echo "🗄️ Running database migrations..."
pnpm run db:migrate

# Seed database
echo "🌱 Seeding database..."
pnpm run db:seed

echo "✅ Setup complete! Run 'pnpm dev' to start development servers."
GitHub Actions CI/CD

CI Workflow (.github/workflows/ci.yml)

YAML
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      - run: pnpm install --frozen-lockfile
      - run: pnpm run lint

  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      - run: pnpm install --frozen-lockfile
      - run: pnpm run test
        env:
          DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test

  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        app: [backend, web]
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
      - run: pnpm install --frozen-lockfile
      - run: pnpm run build --filter=${{ matrix.app }}

  build-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: cd apps/ios && xcodebuild -scheme AllesTeurer -sdk iphonesimulator

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - run: cd apps/android && ./gradlew build
Implementation Tasks

 Initialize monorepo with Turborepo
 Setup pnpm workspaces
 Configure TypeScript paths and aliases
 Setup ESLint and Prettier
 Configure Husky and lint-staged
 Create Docker development environment
 Setup CI/CD pipelines
 Configure environment variables management
 Setup database migrations system
 Create development setup scripts
 Document development workflow
