fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios dev

```sh
[bundle exec] fastlane ios dev
```

Build development version for testing

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Build and upload to TestFlight

### ios release

```sh
[bundle exec] fastlane ios release
```

Build and upload to App Store

### ios test

```sh
[bundle exec] fastlane ios test
```

Run unit tests and build for CI validation

### ios test_ios

```sh
[bundle exec] fastlane ios test_ios
```

Run tests on iOS Simulator (manual use for iOS-specific testing)

### ios build

```sh
[bundle exec] fastlane ios build
```

Build app without distribution (for CI validation)

### ios build_ios

```sh
[bundle exec] fastlane ios build_ios
```

Build app for iOS distribution

### ios clean_build

```sh
[bundle exec] fastlane ios clean_build
```

Clean build when incremental builds are causing issues

### ios certificates

```sh
[bundle exec] fastlane ios certificates
```

Sync development certificates and provisioning profiles

### ios update_certificates

```sh
[bundle exec] fastlane ios update_certificates
```

Update certificates and push to git

### ios version

```sh
[bundle exec] fastlane ios version
```

Show current version and build number

### ios show_help

```sh
[bundle exec] fastlane ios show_help
```

Show available lanes

### ios benchmark

```sh
[bundle exec] fastlane ios benchmark
```

Run build performance benchmark

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
