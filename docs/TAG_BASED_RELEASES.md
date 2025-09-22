# Tag-Based Release System

This document describes the tag-based release system for AllesTeurer, providing manual control over when releases are deployed to TestFlight and the App Store.

## Overview

The tag-based system separates development workflows from release deployments. Regular branch pushes only build and test the app, while releases are triggered by creating specific Git tags.

## Tag Naming Conventions

### Production Releases (App Store)
- Format: `v1.0.0`, `v1.1.0`, `v2.0.0`
- Pattern: `v{MAJOR}.{MINOR}.{PATCH}`
- Triggers: App Store deployment
- Examples:
  - `v1.0.0` - Initial release
  - `v1.1.0` - Feature update
  - `v1.0.1` - Bug fix

### Beta Releases (TestFlight)
- Format: `v1.0.0-beta.1`, `v1.1.0-rc.1`
- Pattern: `v{MAJOR}.{MINOR}.{PATCH}-{PRE-RELEASE}`
- Pre-release identifiers: `beta`, `rc` (release candidate)
- Triggers: TestFlight deployment
- Examples:
  - `v1.0.0-beta.1` - First beta of v1.0.0
  - `v1.0.0-beta.2` - Second beta of v1.0.0
  - `v1.1.0-rc.1` - Release candidate for v1.1.0

### Alpha/Internal Releases
- Format: `v1.0.0-alpha.1`
- Pattern: `v{MAJOR}.{MINOR}.{PATCH}-alpha.{NUMBER}`
- Triggers: Build only (no deployment)
- Used for: Internal testing, build validation

## Release Process

### 1. Create a Beta Release (TestFlight)

```bash
# Ensure your branch is ready for release
git checkout develop
git pull origin develop

# Create and push a beta tag
git tag v1.0.0-beta.1
git push origin v1.0.0-beta.1
```

This will:
- ✅ Build the app with release configuration
- ✅ Set version to `1.0.0` and build number to timestamp
- ✅ Upload to TestFlight
- ✅ Make available to internal testers

### 2. Create a Production Release (App Store)

```bash
# Ensure your branch is ready for release  
git checkout main
git pull origin main

# Create and push a production tag
git tag v1.0.0
git push origin v1.0.0
```

This will:
- ✅ Build the app with release configuration
- ✅ Set version to `1.0.0` and incremental build number
- ✅ Upload to App Store Connect
- ✅ Ready for manual submission to review

### 3. Manual Emergency Release

If you need to trigger a release manually without creating a tag:

1. Go to GitHub Actions in your repository
2. Click "Run workflow" on the iOS CI/CD Pipeline
3. Select the target deployment:
   - `testflight` - Deploy to TestFlight
   - `appstore` - Deploy to App Store
   - `none` - Build only
4. Optionally specify a tag override for version numbering

## Version Management

### Automatic Version Setting
- **From tags**: Version is automatically extracted from the tag name
  - `v1.2.3-beta.1` → App version `1.2.3`, build number timestamp
  - `v1.2.3` → App version `1.2.3`, build number incremental
- **No tag**: Uses current version in Xcode project

### Build Numbers
- **Beta releases**: Uses timestamp format `YYYYMMDDHHMM` for uniqueness
- **Production releases**: Uses incremental build numbers from TestFlight
- **Alpha releases**: Uses timestamp format

## Branch Workflow Integration

### Regular Development
```bash
# These only build and test (no deployments):
git push origin develop    # ✅ Build & test
git push origin main       # ✅ Build & test
git push origin feature/*  # ✅ Build & test (via PR)
```

### Hotfix Releases
```bash
# Create hotfix branch
git checkout main
git checkout -b hotfix/urgent-fix

# Make your fix and commit
git add .
git commit -m "fix: urgent security patch"

# Push and create PR
git push origin hotfix/urgent-fix
# Create PR to main → this builds and tests

# After PR approval and merge:
git checkout main
git pull origin main

# Tag and release immediately
git tag v1.0.1
git push origin v1.0.1  # ✅ Deploys to App Store
```

## Rollback Strategy

If a release needs to be rolled back:

1. **TestFlight**: Create a new tag with the previous version
   ```bash
   git tag v1.0.0-beta.2  # New beta with fixes
   git push origin v1.0.0-beta.2
   ```

2. **App Store**: Create a new production release
   ```bash
   git tag v1.0.1  # New version with fixes
   git push origin v1.0.1
   ```

## Migration from Branch-Based System

### What Changed
- ❌ **Before**: Push to `develop` → automatic TestFlight deployment
- ✅ **After**: Push to `develop` → build and test only
- ❌ **Before**: Push to `main` → automatic App Store deployment  
- ✅ **After**: Push to `main` → build and test only
- ✅ **New**: Tag `v*.*.*-beta*` → TestFlight deployment
- ✅ **New**: Tag `v*.*.*` → App Store deployment

### Benefits
1. **Manual Control**: Deployments happen only when you create tags
2. **Clear Versioning**: Tags represent actual release versions
3. **Safer Development**: Regular pushes don't trigger deployments
4. **Flexible Timing**: Release from any branch when ready
5. **Better History**: Git tags provide immutable release markers

## Troubleshooting

### Tag Creation Failed
```bash
# Check if tag already exists
git tag -l "v1.0.0*"

# Delete local tag if needed
git tag -d v1.0.0

# Delete remote tag if needed
git push origin --delete v1.0.0
```

### Version Conflicts
If TestFlight shows version conflicts:
- Check that build numbers are incrementing properly
- For beta releases, timestamp-based build numbers avoid conflicts
- For production releases, use incremental build numbers

### Failed Deployment
1. Check GitHub Actions logs for detailed error messages
2. Verify App Store Connect API credentials are valid
3. Ensure certificates and provisioning profiles are up to date
4. Run `bundle exec fastlane match` to sync certificates

## Best Practices

1. **Test Before Tagging**: Always test your branch thoroughly before creating release tags
2. **Follow Semantic Versioning**: Use meaningful version numbers (MAJOR.MINOR.PATCH)
3. **Beta Test First**: Create beta tags before production releases
4. **Document Changes**: Use clear commit messages and release notes
5. **Coordinate Team**: Communicate release plans with your team
6. **Monitor Deployments**: Watch GitHub Actions and App Store Connect for successful deployments

## Commands Quick Reference

```bash
# Beta Release
git tag v1.0.0-beta.1 && git push origin v1.0.0-beta.1

# Production Release  
git tag v1.0.0 && git push origin v1.0.0

# List existing tags
git tag -l

# Delete tag locally and remotely
git tag -d v1.0.0 && git push origin --delete v1.0.0

# Check what would be deployed
git log --oneline HEAD...main  # Compare current branch with main
```