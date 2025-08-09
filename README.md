# 🚀 HostelLite — Hostel Resource Tracking & Analytics

<p align="center">
  <img src="https://img.shields.io/badge/Android-Java-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Backend-PHP-777BB4?style=for-the-badge&logo=php&logoColor=white"/>
  <img src="https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Design-XML-0081CB?style=for-the-badge&logo=material-design&logoColor=white"/>
</p>

---

## 🌟 Overview
HostelLite is a native Android application designed to track and analyze resource consumption in student housing. It monitors daily electricity and water usage while facilitating communication between students and administration. The app focuses on bringing transparency to resource management through simple tracking and data visualization.

---

## 🚀 How It Works
1.  **Authentication**: Secure login system for students and hostel admins.
2.  **Usage Logging**: Simple interface for recording daily water and electricity units.
3.  **Real-time Monitoring**: Tracks room occupancy and ambient temperature data.
4.  **Data Insights**: Visualizes consumption patterns through interactive charts.
5.  **Saving Goals**: Allows users to set targets and receive reminders to reduce waste.

---

## 🔥 Key Features

| Feature | Description |
| :--- | :--- |
| **📊 Usage Dashboard** | Central command center displaying real-time usage stats and room conditions. |
| **💧 Resource Tracking** | Granular logging modules for electricity (units) and water consumption. |
| **📋 Admin Module** | Management panel for user oversight, audit logs, and historic data review. |
| **🌡️ Environmental Stats** | Integrated monitoring for room occupancy and ambient temperature logging. |
| **🎯 Goal Manager** | Setting saving targets and tracking daily progress toward resource goals. |
| **🔔 Alert Engine** | Notification system for system alerts and resource-saving nudges. |
| **📈 Interactive Charts** | Simple visual representation of usage history using chart libraries. |

---

## 🛠️ Stack & Tools
*   **Android (Native Java)**: Application logic built with Material Design components.
*   **PHP (Backend)**: REST API architecture for data handling, hosted via XAMPP.
*   **MySQL**: Database for storing user profiles, consumption logs, and settings.
*   **Networking**: HTTP communication with backend APIs.
*   **UI/UX**: Clean XML layouts with a focus on responsive mobile experience.
*   **Persistence**: Shared Preferences and ViewModel-based state management.

---

## 📂 Project Architecture
```text
HostelLite/
 ├── app/src/main/
 │    ├── java/             # Logic (Activities, Adapters, Models)
 │    ├── res/              # UI (Layouts, Drawables, Values)
 │    └── AndroidManifest.xml 
 ├── gradle/                # Build and dependency management
 └── README.md              # Documentation
```

---

## ⚙️ Installation & Setup

### 1. Backend & Database
1.  Start **Apache** and **MySQL** in the XAMPP Control Panel.
2.  Import your database schema into **phpMyAdmin**.
3.  Place the project's PHP scripts in your XAMPP `htdocs` directory.

### 2. Android Project
1.  Open the project in **Android Studio**.
2.  Update the API base URL with your local system IP address.
3.  Build and run on an Android device or emulator (Min SDK 24).

---

## 🛡️ Stability & Security
*   **Auth Logic**: Secure user authentication and local session management.
*   **Validation**: Client and server-side validation to ensure data accuracy.
*   **Connectivity**: Integrated listeners to handle network state changes gracefully.
*   **Standardized UI**: Consistent use of Material tokens for a professional finish.

---

> "HostelLite was developed to solve the lack of transparency in communal resource management. It provides a practical, data-driven solution for more sustainable hostel environments."

---

<div align="center">

✨ **Built for Efficiency. Optimized for Hostels.**

</div>
