# GitHub Actions macOS 26 (arm64) Runner: Installed Software Reference

This document summarizes the available software, tools, SDKs, and environment details for the official GitHub Actions runner image: **macOS 26 (Tahoe) arm64**.

---

## OS & System

- **OS Version:** macOS 26.0 (25A354)
- **Kernel Version:** Darwin 25.0.0
- **Image Version:** 20250916.110

---

## Language & Runtime

- **.NET Core SDK:** 8.0.x, 9.0.x
- **Bash:** 3.2.57
- **Clang/LLVM:** 17.0.0 (default), 20.1.8 (Homebrew)
- **GCC:** 13.4.0, 14.3.0, 15.1.0 (Homebrew, aliased)
- **GNU Fortran:** 13, 14, 15 (Homebrew, aliased)
- **Kotlin:** 2.2.20
- **Node.js:** 24.8.0 (default), 20.x, 22.x (cached)
- **Perl:** 5.40.2
- **Python3:** 3.13.7 (default), 3.11.9, 3.12.10 (cached)
- **Ruby:** 3.4.5 (default), 3.2.9, 3.3.9, 3.4.6 (cached)
- **Go:** 1.25.1 (default), 1.23.12, 1.24.7 (cached)
- **Rust:** 1.89.0 (Cargo, Rustdoc, Rustup)

---

## Package Management

- **Bundler:** 2.7.2
- **Carthage:** 0.40.0
- **CocoaPods:** 1.16.2
- **Homebrew:** 4.6.11
- **NPM:** 11.6.0
- **Pip3:** 25.2
- **Pipx:** 1.7.1
- **RubyGems:** 3.7.2
- **Vcpkg:** 2025 (commit 8a48867e54)
- **Yarn:** 1.22.22

---

## Project Management

- **Apache Ant:** 1.10.15
- **Apache Maven:** 3.9.11
- **Gradle:** 9.0.0

---

## Utilities

- **7-Zip:** 17.05
- **aria2:** 1.37.0
- **azcopy:** 10.30.1
- **bazel:** 8.4.1
- **bazelisk:** 1.27.0
- **bsdtar:** 3.5.3
- **curl:** 8.7.1
- **git:** 2.50.1
- **git-lfs:** 3.7.0
- **GitHub CLI:** 2.79.0
- **GNU tar:** 1.35
- **wget:** 1.25.0
- **gpg:** 2.4.8
- **jq:** 1.8.1
- **openssl:** 3.5.2
- **packer:** 1.14.2
- **pkgconf:** 2.5.1
- **unxip:** 3.2
- **yq:** 4.47.2
- **zstd:** 1.5.7
- **ninja:** 1.13.1

---

## Tools

- **AWS CLI:** 2.30.2
- **AWS SAM CLI:** 1.144.0
- **AWS Session Manager CLI:** 1.2.707.0
- **Azure CLI:** 2.77.0
- **Azure CLI (azure-devops):** 1.0.2
- **Bicep CLI:** 0.37.4
- **CMake:** 4.1.1
- **CodeQL Action Bundle:** 2.23.0
- **Fastlane:** 2.228.0
- **SwiftFormat:** 0.57.2
- **Xcbeautify:** 2.30.1
- **Xcode Command Line Tools:** 26.0.0.0.1.1757719676
- **Xcodes:** 1.6.2

---

## Browsers

- **Safari:** 26.0
- **SafariDriver:** 26.0
- **Google Chrome:** 140.0.7339.133
- **Chrome for Testing:** 140.0.7339.82
- **ChromeDriver:** 140.0.7339.82
- **Microsoft Edge:** 140.0.3485.66
- **Edge WebDriver:** 140.0.3485.66
- **Mozilla Firefox:** 142.0.1
- **geckodriver:** 0.36.0
- **Selenium server:** 4.35.0

### Browser Environment Variables

- `CHROMEWEBDRIVER`, `EDGEWEBDRIVER`, `GECKOWEBDRIVER` set for driver paths

---

## Java

- **JDKs:**
  - 11.0.28+6 (`JAVA_HOME_11_arm64`)
  - 17.0.16+8 (`JAVA_HOME_17_arm64`)
  - 21.0.8+9.0 (default, `JAVA_HOME_21_arm64`)

---

## Rust Tools

- **Cargo:** 1.89.0
- **Rustdoc:** 1.89.0
- **Rustup:** 1.28.2
- **Clippy:** 0.1.89
- **Rustfmt:** 1.8.0-stable

---

## PowerShell Tools

- **PowerShell:** 7.4.11
- **Modules:** Az 14.3.0, Pester 5.7.1, PSScriptAnalyzer 1.24.0

---

## Xcode

- **Versions:**
  - 26.0 (default, 17A324)
  - 16.4 (16F6)
- **Paths:** `/Applications/Xcode_26.app`, `/Applications/Xcode_26.0.0.app`, `/Applications/Xcode_26.0.app`, `/Applications/Xcode.app`, `/Applications/Xcode_16.4.app`, `/Applications/Xcode_16.4.0.app`
- **Command Line Tools:** 26.0.0.0.1.1757719676

### Installed SDKs

- **macOS:** 15.5, 26.0
- **iOS:** 18.5, 26.0
- **tvOS:** 18.5, 26.0
- **watchOS:** 11.5, 26.0
- **visionOS:** 2.5, 26.0
- **DriverKit:** 24.5, 25.0

### Installed Simulators

- **iOS:** 18.5, 18.6, 26.0 (iPhone/iPad models)
- **tvOS:** 18.5, 26.0 (Apple TV models)
- **watchOS:** 11.5, 26.0 (Apple Watch models)
- **visionOS:** 2.5, 26.0 (Apple Vision Pro)

---

## Android

- **Command Line Tools:** 16.0
- **Emulator:** 36.1.9
- **SDK Build-tools:** 36.0.0, 35.0.0, 35.0.1
- **SDK Platforms:** android-36-ext19, android-36-ext18, android-36, android-35-ext15, android-35-ext14, android-35, android-34-ext8, android-34-ext12, android-34-ext11, android-34-ext10, android-33-ext5, android-33-ext4
- **Platform-Tools:** 36.0.0
- **Support Repository:** 47.0.0
- **CMake:** 3.31.5
- **Google Play services:** 49
- **Google Repository:** 58
- **NDK:** 27.3.13750724 (default), 28.2.13676358

### Android Environment Variables

- `ANDROID_HOME`, `ANDROID_NDK`, `ANDROID_NDK_HOME`, `ANDROID_NDK_LATEST_HOME`, `ANDROID_NDK_ROOT`, `ANDROID_SDK_ROOT`

---

## Notes

- All versions are as of September 2025 and may change.
- For full details, see the [official runner image documentation](https://github.com/actions/runner-images/blob/main/images/macos/macos-26-arm64-Readme.md).

---

## References

- [GitHub Actions Runner Images](https://github.com/actions/runner-images)
- [macOS 26 arm64 Readme](https://github.com/actions/runner-images/blob/main/images/macos/macos-26-arm64-Readme.md)
