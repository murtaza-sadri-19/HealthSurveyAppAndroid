# HealthSurveyAppAndroid

**HealthSurveyAppAndroid** is an Android application for collecting health survey data.  
- **User authentication** is managed by Firebase Authentication.  
- **Survey responses** are stored in Google Sheets via the Google Sheets API.  
- **Role-based navigation**: Admins see the dashboard, users see the survey form.

---

## Features

- **Email/Password Authentication** (Firebase)
- **Role-Based Navigation**
  - Users are redirected to different screens based on their email or assigned role after login
- **Survey Data Collection**
  - Users fill out health surveys
  - Data is sent to a Google Sheet
- **Admin Dashboard**
  - Admins can view all survey submissions

---

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Firebase Authentication**
- **Google Sheets API**
- **MVVM Architecture**

---

## Getting Started

### Prerequisites

- Android Studio Flamingo or newer
- Android device or emulator
- Firebase Project ([Get started](https://console.firebase.google.com/))
- Google Cloud Project with Sheets API enabled

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/murtaza-sadri-19/HealthSurveyAppAndroid.git
   cd HealthSurveyAppAndroid
   ```

2. **Firebase Setup**
   - Create a Firebase project
   - Register your Android app in Firebase
   - Download `google-services.json` and place it in the `app/` directory
   - Enable **Email/Password** authentication in Firebase Console

3. **Google Sheets API Setup**
   - Create a Google Cloud Project
   - Enable the Google Sheets API
   - Create a service account and download `credentials.json`
   - Place `credentials.json` in the `app/src/main/assets/` folder
   - Share your Google Sheet with the service account email

4. **Configure App**
   - In `SheetsService.kt`, set your spreadsheet ID:
     ```kotlin
     private val spreadsheetId = "YOUR_SPREADSHEET_ID"
     ```

5. **Dependencies**
   - All dependencies are managed via Gradle. Sync the project in Android Studio.

---

## Usage

- **Register or login** with your email and password.
- **Admins** (e.g., `user@test.com`) are redirected to the dashboard.
- **Regular users** (e.g., `abc@user.com`) are redirected to the survey form.
- **Submit survey data** - it will appear in your connected Google Sheet.

---

## Role-Based Redirection

- The app checks the user's email or role after login:
  - `user@test.com` (admin) → Admin Dashboard
  - `abc@user.com` (user) → Survey Screen

---

## Project Structure

```
com\example\healthsurveyappandroid
 data/
     Survey.kt
     User.kt
 MainActivity.kt
 network/
     SheetService.kt
 repository/
     SurveyRepository.kt
 ui/
     components/
         SurveyCard.kt
     screens/
         admin/
             AdminHomeScreen.kt
         auth/
             LoginScreen.kt
             RegisterScreen.kt
         navigation/
             Navigation.kt
         user/
             SurveyFormScreen.kt
     theme/
         Color.kt
         Theme.kt
         Type.kt
 utils/
     Constants.kt
     Validators.kt
 viewmodel/
     AuthViewModel.kt
     LoginState.kt
     SurveyViewModel.kt
```

---

## Security

- Only authenticated users can access the app.
- Firestore security rules restrict access to user data.
- Google Sheets access is restricted to the service account.

---

## Acknowledgments

- [Firebase](https://firebase.google.com/)
- [Google Sheets API](https://developers.google.com/sheets/api)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
