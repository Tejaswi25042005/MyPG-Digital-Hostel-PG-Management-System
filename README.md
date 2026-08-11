
# MyPG – Digital Hostel & PG Management System

MyPG is an Android-based Hostel and PG Management System designed to simplify and digitize day-to-day hostel and paying guest management.

The application provides separate functionality for hostel/PG owners and tenants, helping manage rooms, tenants, rent, payments, expenses, requests, notices, and other operational activities from a single mobile application.

---

## 🚀 Features

### 🏠 Hostel & Room Management
- Add and manage hostels
- Manage floors and rooms
- Track room availability
- Manage room sharing and pricing
- View room details and occupancy

### 👤 Tenant Management
- Add and manage tenants
- View tenant profiles
- Assign tenants to rooms
- Manage tenant details
- View tenant payment information
- Track tenant history

### 💰 Rent & Payment Management
- Record rent payments
- Track payment history
- Manage tenant revenue
- Generate revenue information
- Support subscription-based features

### 📊 Revenue & Expense Management
- Track hostel revenue
- Record expenses
- View monthly financial information
- Generate revenue-related reports
- Manage financial records

### 📝 Requests & Services
- Tenant requests
- Hostel service requests
- Request status tracking
- Vacate requests
- Settlement management

### 📢 Notices
- Create hostel notices
- Display notices to users
- Manage important announcements

### ⚡ Quick Actions
- Recent actions
- Quick access to frequently used features
- Centralized hostel management dashboard

### 👤 Profile & Settings
- User profile management
- Application settings
- Hostel-specific settings

### 🗺️ Location Support
- Google Maps integration
- Location-based hostel functionality

### 💳 Subscription & Payments
- Subscription plans
- Razorpay payment integration
- Subscription status management

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| Java | Android application development |
| Android SDK | Mobile application framework |
| XML | UI layouts and resources |
| Gradle | Build system |
| Google Maps | Location and map functionality |
| Razorpay | Payment integration |
| Git & GitHub | Version control |

---

## 📱 Application Structure

The application includes modules for:

- Authentication
- Dashboard
- Hostel Management
- Room Management
- Tenant Management
- Revenue Management
- Expense Management
- Notices
- Service Requests
- Vacate Requests
- Settlements
- Subscriptions
- Profile & Settings

---

## 🔐 Security

Sensitive configuration values are not included in the repository.

The project uses local configuration for API keys and credentials.

Sensitive files such as:

```text
local.properties
google-services.json
````

are excluded from version control.

API keys should be configured locally before building the application.

---

## ⚙️ Setup & Installation

### 1. Clone the repository

```bash
git clone https://github.com/SAITEJA-THANNEERU/MyPG-Digital-Hostel-PG-Management-System-.git
```

### 2. Open the project

Open the cloned project in **Android Studio**.

### 3. Configure local properties

Create/configure the required local configuration values in:

```text
local.properties
```

Do not commit this file to GitHub.

### 4. Configure Firebase

Place your Firebase configuration file locally:

```text
app/google-services.json
```

This file is intentionally excluded from the repository.

### 5. Build the project

Allow Android Studio to sync Gradle dependencies and then build/run the application on an Android device or emulator.

---

## 📂 Project Structure

```text
MyPG/
│
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/srikanta/mypg/
│   │   │   └── res/
│   │   └── test/
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
│   └── wrapper/
│
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── .gitignore
```

---

## 🎯 Purpose

MyPG aims to provide a simple digital solution for managing hostel and PG operations while reducing manual paperwork and making everyday management faster and more organized.

---

## 🔮 Future Improvements

Potential future improvements include:

* Cloud-based synchronization
* Push notifications
* Advanced analytics and dashboards
* Automated payment reminders
* Digital receipts
* Improved financial reports
* Multi-hostel management
* Tenant communication
* Automated backups

---

## 👨‍💻 Developer

**SAI TEJA THANNEERU**

GitHub:
[https://github.com/SAITEJA-THANNEERU](https://github.com/SAITEJA-THANNEERU)

---

## 📄 License

This project is currently available for educational and portfolio purposes.

Please contact the repository owner before using, modifying, or distributing the project commercially.

````

### Quick recommendation

For your GitHub repository, I'd use this README as-is. It is **professional enough for recruiters/portfolio reviewers** without making claims about features that aren't represented in your current project.

One small thing: after adding `README.md`, run:

```powershell
git add README.md
git commit -m "Add project documentation"
git push
````

