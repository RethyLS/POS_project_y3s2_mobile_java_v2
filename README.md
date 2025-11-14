# POS System - Stable Release

A Point of Sale (POS) system designed for retail businesses, featuring a native Android mobile app (Java) and Laravel API backend. This solution provides streamlined sales processing, inventory management, user authentication, and real-time data synchronization.

## 🎯 What is POS?

A Point of Sale (POS) system is business software that handles sales transactions, inventory management, user control, and reporting. It replaces traditional cash registers with digital solutions that:

- **Process Sales**: Handle product selection, cart management, payment processing, and receipt generation
- **Manage Inventory**: Track stock levels, update quantities automatically, and manage product catalogs
- **Control Access**: Role-based user authentication (Admin, Cashier) with secure login systems
- **Generate Reports**: Provide sales analytics, inventory reports, and business insights
- **Sync Data**: Maintain consistency between local storage and central database

Perfect for retail stores, cafes, small businesses, and any point-of-sale environment requiring reliable transaction processing.

## 🔄 App Process Overview

### Sales Flow:
1. **Login** → User authenticates with username/password
2. **Product Selection** → Browse and select items from inventory
3. **Cart Management** → Add/remove items, adjust quantities
4. **Payment Processing** → Enter payment details and process transaction
5. **Sale Completion** → Generate receipt, update inventory, sync with backend

### Data Flow:
1. **Local First** → All operations work offline using Room database
2. **Background Sync** → Data synchronizes with Laravel API when connected
3. **Conflict Resolution** → Server data takes precedence during sync
4. **Real-time Updates** → Inventory and sales data updated across all sessions

## 🚀 Tech Stack

### 📱 **Android App (Java)**
- **Language**: Java (Native Android Development)
- **Local Database**: Room (SQLite wrapper) for offline-first operations
- **API Communication**: Retrofit2 with Gson for REST API calls
- **UI Framework**: Material Design Components
- **Architecture**: Repository Pattern with DAO (Data Access Objects)
- **Authentication**: JWT token-based with secure storage

**Why Java for Android?**
- Robust, mature language with extensive Android support
- Excellent performance for complex business logic
- Strong offline capabilities with Room database
- Wide compatibility across Android devices

### ⚡ **Backend API (Laravel)**
- **Framework**: Laravel (PHP) - Full-featured web application framework
- **Database**: MySQL for reliable data persistence
- **Authentication**: Laravel Sanctum for secure JWT token management
- **API Architecture**: RESTful endpoints with resource controllers
- **Validation**: Laravel Form Requests for data integrity
- **Security**: Built-in CSRF protection, input sanitization, and middleware

**Why Laravel?**
- Rapid API development with built-in authentication and validation
- Eloquent ORM for clean database operations and relationships
- Robust security features and middleware system
- Excellent documentation and active community support

*Note: The Next.js frontend is included as a sample implementation and not part of the core stable system.*

## 🔗 Communication Architecture

### API Communication Flow:
```
Android App (Java) ←→ Laravel API (PHP) ←→ MySQL Database
     ↓                      ↓                    ↓
Room Database          RESTful APIs        Persistent Storage
(Local Cache)         (JSON/HTTP)          (Production Data)
```

### Data Synchronization:
- **Authentication**: JWT tokens managed by Laravel Sanctum
- **API Calls**: Retrofit2 handles HTTP requests/responses
- **Data Format**: JSON for all API communications
- **Endpoints**: RESTful URLs for products, sales, users, categories
- **Error Handling**: Comprehensive error responses and offline fallbacks

### Key Integration Points:
- **ApiService.java**: Defines all API endpoint interfaces
- **ApiClient.java**: Configures Retrofit client with base URL and interceptors
- **Repository Classes**: Handle data synchronization between local and remote sources
- **Laravel Controllers**: Process API requests and return formatted responses
## 📁 Project Structure (POS_Project)

### Main Directories:

```
POS_Project/
├── app/                          # Main Android application module
│   ├── src/main/java/com/example/pos_project/
│   │   ├── activity/             # Activity classes (App screens)
│   │   │   ├── LoginActivity.java           # User authentication screen
│   │   │   ├── SalesActivity.java           # Main POS sales interface
│   │   │   ├── CartActivity.java            # Shopping cart management
│   │   │   ├── UserProfileActivity.java     # User profile and logout
│   │   │   ├── SplashActivity.java          # App startup screen
│   │   │   └── SaleCompleteDialog.java      # Transaction completion dialog
│   │   ├── adapter/              # RecyclerView adapters for lists
│   │   │   ├── ProductAdapter.java          # Product list display
│   │   │   ├── CartAdapter.java             # Shopping cart items
│   │   │   └── CategoryAdapter.java         # Product categories
│   │   ├── api/                  # Backend communication layer
│   │   │   ├── ApiService.java              # REST API endpoint definitions
│   │   │   └── ApiClient.java               # Retrofit client configuration
│   │   ├── auth/                 # Authentication management
│   │   │   └── AuthManager.java             # JWT token and user session handling
│   │   ├── dao/                  # Data Access Objects (Room DB)
│   │   │   ├── UserDao.java                 # User data operations
│   │   │   ├── ProductDao.java              # Product CRUD operations
│   │   │   ├── SaleDao.java                 # Sales transaction data
│   │   │   └── CategoryDao.java             # Category management
│   │   ├── database/             # Database configuration
│   │   │   └── POSDatabase.java             # Room database setup and entities
│   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── LoginRequest.java            # API login request format
│   │   │   ├── LoginResponse.java           # API login response format
│   │   │   ├── SaleRequest.java             # Sales transaction request
│   │   │   └── ApiResponse.java             # Standard API response wrapper
│   │   ├── model/                # Entity classes (Database models)
│   │   │   ├── User.java                    # User entity with roles
│   │   │   ├── Product.java                 # Product information and stock
│   │   │   ├── Sale.java                    # Sales transaction record
│   │   │   ├── SaleItem.java                # Individual sale line items
│   │   │   ├── Category.java                # Product categories
│   │   │   └── Store.java                   # Store/location information
│   │   ├── repository/           # Data layer abstraction
│   │   │   ├── ProductRepository.java       # Product data management
│   │   │   ├── SaleRepository.java          # Sales data handling
│   │   │   └── UserRepository.java          # User data operations
│   │   └── utils/                # Utility classes
│   │       └── DatabaseInitializer.java     # Sample data setup
│   ├── src/main/res/             # Android resources
│   │   ├── layout/                          # XML layout files
│   │   ├── drawable/                        # Images and icons
│   │   ├── values/                          # Colors, strings, themes
│   │   └── menu/                            # Menu configurations
│   └── build.gradle              # App-level dependencies and config
├── gradle/                       # Gradle wrapper and configuration
└── build.gradle                  # Project-level Gradle configuration
```

### 🔑 Key Files for Backend Integration:

#### **ApiService.java** - RESTful API Interface
- Defines all API endpoints using Retrofit annotations
- Handles authentication, products, sales, categories, and stores
- Manages JWT token headers for secure requests
- Provides type-safe API method declarations

#### **ApiClient.java** - HTTP Client Configuration  
- Configures Retrofit instance with base URL
- Sets up JSON converters and request interceptors
- Manages connection timeouts and error handling
- Provides singleton API service instance

#### **AuthManager.java** - Authentication Controller
- Manages JWT token storage and retrieval
- Handles user login/logout operations
- Provides authentication state management
- Integrates with Laravel Sanctum authentication

#### **Repository Classes** - Data Synchronization Layer
- **ProductRepository.java**: Syncs product data between local Room DB and Laravel API
- **SaleRepository.java**: Handles sales transaction sync and conflict resolution
- **UserRepository.java**: Manages user data and authentication state

#### **DTO Classes** - API Data Contracts
- **LoginRequest/Response.java**: Authentication data structures
- **SaleRequest.java**: Sales transaction format for API submission
- **ApiResponse.java**: Standard wrapper for all API responses

### 📂 Backend Structure (pos-api-usea):

```
pos-api-usea/
├── app/Http/Controllers/Api/      # API Controllers
│   ├── AuthController.php                  # User authentication
│   ├── ProductController.php               # Product CRUD operations
│   ├── SaleController.php                  # Sales transaction handling
│   ├── CategoryController.php              # Category management
│   ├── UserController.php                  # User management
│   └── StoreController.php                 # Store/location management
├── app/Models/                   # Eloquent models
│   ├── User.php                            # User model with roles
│   ├── Product.php                         # Product model with relationships
│   ├── Sale.php                            # Sale transaction model
│   └── Category.php                        # Category model
├── routes/api.php                # API route definitions
├── database/migrations/          # Database schema
└── config/sanctum.php           # JWT authentication configuration
```

## 🔧 Key Integration Features

### Data Synchronization
- **Offline-First**: App works without internet, syncs when available
- **Background Sync**: Automatic data synchronization in background
- **Conflict Resolution**: Server data takes precedence during sync conflicts
- **Error Handling**: Graceful fallbacks for network failures

### Authentication Flow
1. User enters credentials in LoginActivity
2. AuthManager sends request via ApiService to Laravel
3. Laravel validates and returns JWT token
4. Token stored locally and used for subsequent API calls
5. Automatic token refresh and logout handling

### Sales Processing
1. Products loaded from local Room database (synced from API)
2. Cart management handled locally for performance
3. Sale completion triggers API call to Laravel
4. Inventory updated both locally and remotely
5. Transaction data synced across all devices

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Java 11 or higher
- PHP 8.1+ with Composer (for backend)
- MySQL database

### Setup Steps
1. **Backend Setup**:
   ```bash
   cd pos-api-usea
   composer install
   php artisan migrate
   php artisan serve
   ```

2. **Android Setup**:
   - Open `POS_Project` in Android Studio
   - Update API base URL in `ApiClient.java`
   - Sync Gradle files and build project

### Default Credentials
- **Admin**: username: `admin`, password: `admin123`
- **Cashier**: username: `cashier`, password: `cashier123`

---

*Built for reliable retail operations with offline-first architecture and seamless backend integration.*