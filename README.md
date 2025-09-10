# Alles Teurer 📱💰

> *"Everything More Expensive"* - An iOS app that helps you track price changes by scanning receipts

**Alles Teurer** is an intelligent iOS application that uses Apple Intelligence and Vision framework to automatically extract product information from receipt photos, creating a comprehensive price history database to track inflation and spending patterns.

## 🎯 Overview

With rising costs affecting everyone, this app empowers users to:
- **Track Price Changes**: Monitor how product prices evolve over time
- **Smart Receipt Processing**: Automatically extract merchant names, dates, and itemized purchases
- **Visual Analytics**: See price trends through interactive charts and graphs
- **Spending Insights**: Get detailed statistics about your purchasing patterns

## ✨ Features

### Core Functionality
- 📸 **Receipt Scanning**: Camera integration for capturing receipt photos
- 🧠 **Apple Intelligence**: Automatic data extraction using Vision and Natural Language frameworks
- 💾 **Smart Storage**: Core Data integration for reliable local data persistence
- 📊 **Price History**: Track price changes for products across different merchants and dates

### Analytics & Insights
- 📈 **Interactive Charts**: Visualize price trends using Swift Charts
- 🔍 **Product Matching**: Intelligent algorithms to link similar products across different receipts
- 📋 **Statistical Analysis**: Average prices, volatility measures, and inflation indicators
- 🏪 **Merchant Comparison**: Compare prices across different retailers

### User Experience
- 🎨 **Modern SwiftUI Interface**: Clean, intuitive design following iOS design guidelines
- 🌙 **Dark Mode Support**: Full support for light and dark appearances
- ♿ **Accessibility**: VoiceOver support and Dynamic Type compatibility
- 🔔 **Price Alerts**: Notifications when significant price changes are detected

## 🏗️ Project Structure

```
AllesTeurer/
├── Models/              # Core Data models and entities
├── Views/               # SwiftUI views and UI components
├── Services/            # Business logic and data processing
├── Utils/               # Helper functions and extensions
└── Resources/           # Assets, localization, and configuration
```

## 🚀 Getting Started

### Prerequisites
- **Xcode 15.0+**
- **iOS 17.0+** (target deployment)
- **Apple Developer Account** (for device testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/mpwg/AllesTeurer.git
   cd AllesTeurer
   ```

2. **Open in Xcode**
   ```bash
   open AllesTeurer.xcodeproj
   ```

3. **Configure Bundle Identifier**
   - Update the bundle identifier to your own: `com.yourdomain.allesteurer`
   - Select your development team in project settings

4. **Build and Run**
   - Select your target device or simulator
   - Press `Cmd+R` to build and run

### Required Permissions
The app requires the following permissions:
- **Camera Access**: For scanning receipt photos
- **Photo Library**: For importing existing receipt images (optional)

## 🛠️ Development Roadmap

Our development follows a structured milestone approach:

### 📋 [Milestone 1: Core Infrastructure](https://github.com/mpwg/AllesTeurer/milestone/1)
- iOS project setup with SwiftUI
- Core Data model definitions
- Dependency management configuration

### 📱 [Milestone 2: Receipt Processing](https://github.com/mpwg/AllesTeurer/milestone/2)
- Camera integration and photo capture
- Vision framework for text recognition
- Apple Intelligence data extraction
- Photo import functionality

### 💾 [Milestone 3: Data Management](https://github.com/mpwg/AllesTeurer/milestone/3)
- Core Data operations implementation
- Product matching algorithms
- Data validation and cleanup

### 📊 [Milestone 4: Analytics Engine](https://github.com/mpwg/AllesTeurer/milestone/4)
- Price history tracking
- Chart visualization with Swift Charts
- Statistical analysis calculations
- Analytics dashboard

### 🎨 [Milestone 5: User Interface](https://github.com/mpwg/AllesTeurer/milestone/5)
- Main navigation implementation
- Receipt scanning interface
- History and product management views
- Settings and preferences

### 🧪 [Milestone 6: Testing & Polish](https://github.com/mpwg/AllesTeurer/milestone/6)
- Comprehensive test suite
- Error handling and user feedback
- Performance optimization
- App Store preparation

## 🏛️ Architecture

### Core Technologies
- **SwiftUI**: Modern declarative UI framework
- **Core Data**: Local data persistence and management
- **Vision Framework**: Optical Character Recognition (OCR)
- **Natural Language**: Text processing and entity extraction
- **Swift Charts**: Data visualization and interactive graphs
- **AVFoundation**: Camera integration and media capture

### Design Patterns
- **MVVM**: Model-View-ViewModel architecture
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic separation
- **Dependency Injection**: Testable and modular code

## 📖 Usage

### Scanning Your First Receipt
1. **Open the app** and tap the "Scan" tab
2. **Point your camera** at a receipt and tap the capture button
3. **Review extracted data** - the app will automatically identify:
   - Merchant name
   - Purchase date
   - Individual items with prices
4. **Save the receipt** - data is stored locally on your device

### Viewing Price History
1. **Navigate to "Products"** tab
2. **Select any product** to view its price history
3. **Explore interactive charts** showing price trends over time
4. **Compare prices** across different merchants

### Analytics Dashboard
1. **Visit "Analytics"** tab for insights
2. **View spending patterns** by category and merchant
3. **Track inflation indicators** for your purchases
4. **Set up price alerts** for significant changes

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Check our [Issues](https://github.com/mpwg/AllesTeurer/issues)** for tasks that need attention
2. **Fork the repository** and create a feature branch
3. **Follow our user stories** format for new features
4. **Write tests** for your contributions
5. **Submit a pull request** with a clear description

### Development Setup
```bash
# Install dependencies (if using Swift Package Manager)
# Dependencies are resolved automatically by Xcode

# Run tests
xcodebuild test -scheme AllesTeurer -destination 'platform=iOS Simulator,name=iPhone 15'

# Code formatting (if using SwiftFormat)
swiftformat .
```

## 📄 Privacy & Data

- **Local Storage Only**: All data remains on your device
- **No Cloud Sync**: We don't store your receipts or purchase data
- **Minimal Permissions**: Only camera access for scanning
- **Transparent Processing**: Open source implementation

## 📱 System Requirements

- **iOS 17.0** or later
- **iPhone** (optimized for all sizes)
- **Camera** required for receipt scanning
- **50MB** storage space

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/mpwg/AllesTeurer/issues)
- **Discussions**: [GitHub Discussions](https://github.com/mpwg/AllesTeurer/discussions)
- **Documentation**: Check our [Wiki](https://github.com/mpwg/AllesTeurer/wiki)

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Apple** for the Vision and Natural Language frameworks
- **Swift Community** for excellent open-source libraries
- **Contributors** who help make this project better

---

**Made with ❤️ to help track the rising cost of everything**

*Star ⭐ this repo if you find it helpful!*