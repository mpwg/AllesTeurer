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

### ios ci

```sh
[bundle exec] fastlane ios ci
```

CI build and test (no deployment)

### ios test

```sh
[bundle exec] fastlane ios test
```

Run all tests

### ios alpha

```sh
[bundle exec] fastlane ios alpha
```

Alpha release to GitHub (v*.*.*-alpha.*)

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Beta release to TestFlight (v*.*.*-beta.*)

### ios release

```sh
[bundle exec] fastlane ios release
```

Production release to App Store (v*.*.*)

### ios version

```sh
[bundle exec] fastlane ios version
```

Show current version

### ios setup_match

```sh
[bundle exec] fastlane ios setup_match
```

Setup certificates for development

### ios update_certificates

```sh
[bundle exec] fastlane ios update_certificates
```

Update certificates and profiles

### ios nuke_certificates

```sh
[bundle exec] fastlane ios nuke_certificates
```

Nuke all certificates and start fresh (DANGEROUS)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
