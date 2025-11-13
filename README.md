# POS System - Multi-Platform Solution

A comprehensive Point of Sale (POS) system designed for modern retail businesses, featuring Android mobile app, Laravel API backend, and Next.js web frontend. This solution provides seamless sales management, inventory tracking, and real-time synchronization across all platforms.

## 🎯 What is this app?

This POS system enables retail businesses to:
- **Process sales transactions** with multiple payment methods
- **Manage inventory** with real-time stock updates
- **Track customer data** and purchase history
- **Generate reports** for business insights
- **Synchronize data** across mobile and web platforms

Perfect for coffee shops, retail stores, restaurants, and small to medium enterprises that need reliable, offline-capable point of sale solution.

## 🚀 Tech Stack

### 📱 **Android Frontend (Java)**
- **Framework**: Native Android with Java
- **Database**: Room (SQLite) for offline storage
- **API Communication**: Retrofit + Gson
- **UI**: Material Design components
- **Architecture**: MVVM pattern with Repository

**Why Android Native?**
- Superior performance and user experience
- Offline-first capability for unstable internet
- Hardware integration (barcode scanners, receipt printers)
- Wide device compatibility

### 🌐 **Web Frontend (Next.js)**
- **Framework**: Next.js 14 with TypeScript
- **Styling**: Tailwind CSS
- **State Management**: React Context API
- **API Client**: Custom service layer
- **Authentication**: JWT-based sessions

**Why Next.js?**
- Server-side rendering for better SEO and performance
- TypeScript for type safety and developer experience
- Modern React ecosystem with excellent tooling
- Fast development and deployment cycles

### ⚡ **Backend API (Laravel)**
- **Framework**: Laravel 11 with PHP 8.2+
- **Database**: MySQL/PostgreSQL
- **Authentication**: Laravel Sanctum (JWT)
- **Validation**: Laravel Form Requests
- **Architecture**: RESTful API with resource controllers

**Why Laravel?**
- Rapid development with built-in features (auth, validation, ORM)
- Eloquent ORM for clean database interactions
- Robust security features and middleware
- Excellent documentation and large community

## ✨ Key Features

### 🛍️ **Sales Processing**
- Real-time product selection and cart management
- Multiple payment methods (Cash, Card, Mobile, Credit)
- Automatic tax and discount calculations
- Receipt generation and printing support
- Offline sales with server synchronization

### 📦 **Inventory Management**
- Product catalog with categories and SKUs
- Real-time stock level tracking
- Low stock alerts and notifications
- Barcode scanning for quick product lookup
- Bulk import/export capabilities

### 👥 **User & Customer Management**
- Role-based authentication (Admin, Manager, Cashier)
- Customer profiles and purchase history
- Employee performance tracking
- Shift management and reporting

### 📊 **Analytics & Reporting**
- Daily/weekly/monthly sales reports
- Top-selling products analysis
- Revenue trends and forecasting
- Inventory turnover reports
- Export data to CSV/PDF formats

### 🔄 **Cross-Platform Sync**
- Real-time data synchronization between mobile and web
- Offline-first mobile app with sync when online
- Centralized data management through Laravel API
- Conflict resolution for simultaneous updates

## 🏗️ System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Android App   │    │   Next.js Web    │    │   Laravel API   │
│     (Java)      │◄──►│   (TypeScript)   │◄──►│     (PHP)       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Room Database  │    │  Browser Storage │    │ MySQL Database │
│    (SQLite)     │    │   (Local Cache)  │    │   (Main Store)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (for mobile development)
- Node.js 18+ (for web frontend)
- PHP 8.2+ & Composer (for backend API)
- MySQL/PostgreSQL database

### Quick Setup
1. **Backend**: `cd pos-api-usea && composer install && php artisan serve`
2. **Web Frontend**: `cd pos-frontend-usea && npm install && npm run dev`
3. **Android App**: Open `POS_Project` in Android Studio and run

## 📱 Platform Compatibility

- **Android**: API level 24+ (Android 7.0+)
- **Web**: Modern browsers (Chrome, Firefox, Safari, Edge)
- **Backend**: Cross-platform (Windows, macOS, Linux)

---

*Built with ❤️ for modern retail businesses*

### Key Technologies
- Android SDK (API Level 24+)
- Room Database Library
- Material Design Components
- RecyclerView for lists
- CardView for modern UI
- AsyncTask alternatives with ExecutorService

## Installation & Setup

### Prerequisites
- Android Studio
- Android SDK (API 24 or higher)
- Java 11 or higher

### Setup Steps
1. Clone or download the project
2. Open in Android Studio
3. Sync Gradle files
4. Build and run the application

### Default Login Credentials
The app initializes with sample data including:
- **Admin**: username: `admin`, password: `admin123`
- **Cashier**: username: `cashier`, password: `cash123`

## Project Structure

```
app/src/main/java/com/example/pos_project/
├── activity/           # Activity classes
│   ├── MainActivity.java
│   ├── ProductActivity.java
│   ├── AddEditProductActivity.java
│   ├── SalesActivity.java
│   ├── UsersActivity.java
│   └── ReportsActivity.java
├── adapter/           # RecyclerView adapters
│   ├── ProductAdapter.java
│   ├── SaleProductAdapter.java
│   └── CartAdapter.java
├── dao/              # Data Access Objects
│   ├── UserDao.java
│   ├── ProductDao.java
│   ├── SaleDao.java
│   └── SaleItemDao.java
├── database/         # Database setup
│   └── POSDatabase.java
├── model/           # Entity classes
│   ├── User.java
│   ├── Product.java
│   ├── Sale.java
│   ├── SaleItem.java
│   └── CartItem.java
└── utils/           # Utility classes
    └── DatabaseInitializer.java
```

## Sample Data

The application comes pre-loaded with:
- **10 sample products** across different categories (Beverages, Bakery, Dairy, Fruits, etc.)
- **2 user accounts** (Admin and Cashier roles)
- **Product categories** for better organization

## Key Features Implementation

### Sales Process
1. Select products from the product list
2. Add items to cart with quantity adjustments
3. Enter customer information (optional)
4. Select payment method
5. Enter amount paid
6. Process transaction with automatic change calculation
7. Update inventory quantities

### Inventory Management
- Real-time stock tracking
- Low stock warnings (color-coded indicators)
- Automatic quantity reduction on sales
- Easy product addition and editing

### Data Persistence
- All data stored locally using Room Database
- Automatic database initialization
- Transaction safety with proper threading

## Future Enhancements

### Potential Improvements
- Barcode scanning functionality
- Receipt printing integration
- Cloud backup and sync
- Advanced reporting with charts
- Multi-store support
- Tax calculation
- Discount management
- Customer loyalty programs

### Technical Improvements
- ViewBinding implementation
- MVVM architecture with LiveData
- Dependency injection with Hilt
- API integration for cloud sync
- Unit and integration testing

## Usage Guidelines

### For Students
This project demonstrates:
- Android development best practices
- Database design and implementation
- UI/UX design principles
- Business logic implementation
- Error handling and user feedback

### For SME Businesses
- Suitable for small retail operations
- Easy to use interface
- Essential POS functionality
- Local data storage for reliability
- Customizable for specific business needs

## License

This project is created for educational purposes and SME use. Feel free to modify and adapt according to your requirements.

## Support

For questions or issues, please refer to the code documentation and comments throughout the project files.

---

**Note**: This is a basic implementation suitable for learning and small business use. For production environments, consider additional security measures, data backup strategies, and thorough testing.