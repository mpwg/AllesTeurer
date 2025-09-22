fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

### dev_all

```sh
[bundle exec] fastlane dev_all
```

Build both iOS and Android for development

### beta_all

```sh
[bundle exec] fastlane beta_all
```

Build both iOS and Android for beta testing

### release_all

```sh
[bundle exec] fastlane release_all
```

Build both iOS and Android for release

### build_all

```sh
[bundle exec] fastlane build_all
```

Build both platforms for CI validation

### show_help

```sh
[bundle exec] fastlane show_help
```

Show available lanes

----


## iOS

### ios dev

```sh
[bundle exec] fastlane ios dev
```

Build iOS development version for testing

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Build iOS and upload to TestFlight

### ios release

```sh
[bundle exec] fastlane ios release
```

Build iOS and upload to App Store

### ios build

```sh
[bundle exec] fastlane ios build
```

Build iOS app without distribution (for CI validation)

### ios certificates

```sh
[bundle exec] fastlane ios certificates
```

Sync iOS certificates and provisioning profiles

----


## Android

### android dev

```sh
[bundle exec] fastlane android dev
```

Build Android debug APK

### android beta

```sh
[bundle exec] fastlane android beta
```

Build Android release APK and upload to Internal App Sharing

### android release

```sh
[bundle exec] fastlane android release
```

Build Android App Bundle and upload to Play Console

### android build

```sh
[bundle exec] fastlane android build
```

Build Android app without distribution (for CI validation)

### android setup_keystore

```sh
[bundle exec] fastlane android setup_keystore
```

Setup Android keystore for signing (creates keystore if needed)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
