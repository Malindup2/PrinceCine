#  PrinceCine
<img width="1920" height="1080" alt="Clean and Modern App Portfolio Mockup Presentation" src="https://github.com/user-attachments/assets/084cf2ae-5fe5-401f-9e2f-865291099667" />

A modern Android movie management application built with **Kotlin**, designed to provide a smooth and intuitive experience for managing and organizing personal movie collections.

---

##  Overview

**PrinceCine** is an Android application that allows users to store, organize, and manage movie information effortlessly. The app uses **Firebase** as its backend and integrates **QR code functionality** to quickly access or share movie details.

It is built following modern Android development practices, focusing on clean UI, performance, and scalability.

---

##  Features

*  **Modern Android UI** – Built using Material Design components
*  **User Authentication** – Secure login and registration with Firebase Authentication
*  **Cloud Database** – Store and sync movie details using Firebase Firestore
*  **Image Management** – Upload and manage movie posters via Firebase Storage
*  **QR Code Integration** – Generate and scan QR codes for quick movie access (ZXing)
*  **Fast Image Loading** – Efficient image loading and caching with Glide
*  **Asynchronous Processing** – Smooth performance using Kotlin Coroutines

---

## 🛠 Tech Stack

### Core

* **Language:** Kotlin
* **Minimum SDK:** 29 (Android 10)
* **Target SDK:** 34
* **Compile SDK:** 35

### AndroidX Libraries

* Core KTX
* AppCompat
* ConstraintLayout
* Activity

### Firebase Services

* Firebase Authentication
* Firebase Firestore
* Firebase Storage
* Firebase Analytics

### Third-Party Libraries

* **ZXing** – QR code generation and scanning
* **Glide** – Image loading and caching
* **Kotlin Coroutines** – Asynchronous programming

---

##  Getting Started

### Prerequisites

To run this project, ensure you have:

* Android Studio **Arctic Fox** or later
* **JDK 11** or higher
* Android SDK (API level 29+)
* A Firebase account with a configured project

---

##  Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/Malindup2/PrinceCine.git
   ```

2. **Open the project** in Android Studio

3. **Configure Firebase**

   * Add `google-services.json` to the `app/` directory
   * Enable **Authentication**, **Firestore**, and **Storage** in Firebase Console

4. **Sync Gradle files**

5. **Build and Run** the app on an emulator or physical Android device

---

##  Project Structure

```
PrinceCine/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/princecine/
│   │   │   └── res/
│   │   └── test/
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚙ Build Configuration

* **Application ID:** `com.example.princecine`
* **Version Code:** 1
* **Version Name:** 1.0
* **Java Version:** 11

---

##  Contributing

Contributions are welcome!

If you’d like to improve this project:

1. Fork the repository
2. Create a new branch
3. Make your changes
4. Submit a Pull Request

---

##  License

This project is open for learning, modification, and further development.

---

##  Contact

**Developer:** Malindup2
**GitHub Repository:** [https://github.com/Malindup2/PrinceCine](https://github.com/Malindup2/PrinceCine)

---

 If you like this project, don’t forget to star the repository!
