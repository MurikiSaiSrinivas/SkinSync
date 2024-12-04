# Skin Sync

**Skin Sync** is an Android application that helps users decide what to wear for photoshoots based on their skin, lip, and eye colors. By analyzing these features and the colors of a specific location, the app provides outfit recommendations to ensure the user looks their best in every photo. It also scrapes clothing suggestions online, including prices and purchase links, making the experience seamless and efficient.

## Features
- **Skin Color Analysis**: Analyze skin, lips, and eye colors using Google’s Mediapipe and TensorFlow.
- **Location-Based Recommendations**: Use Google GenAI to suggest the best colors to wear based on the user’s features and location.
- **Clothing Suggestions**: Retrieve outfit ideas with prices and links using SerpApi.
- **Profile Management**: Save and update user details such as name, age, gender, and extracted colors.

## Prerequisites
1. **API Keys**:
    - **Google Gemini API**: Obtain from [Google AI Studio](https://aistudio.google.com/).
    - **SerpApi**: Obtain from [SerpApi](https://serpapi.com/).

2. **Android Studio**:
    - Download and install [Android Studio](https://developer.android.com/studio) for development.

3. **Dependencies**:
    - Kotlin
    - TensorFlow
    - Mediapipe
    - Retrofit for API calls

## Getting Started

### Clone the Repository
```bash
git clone https://github.com/your-repo/skin-sync.git
cd skin-sync
```

### Add Your API Keys
1. Open the `MainActivity.kt` file.
2. Replace the placeholders with your actual API keys:
   ```kotlin
   val GEMINI_API_KEY = "your_gemini_api_key_here"
   val SERP_API_KEY = "your_serp_api_key_here"
   ```

### Build and Run
1. Open the project in Android Studio.
2. Sync the Gradle files.
3. Build and run the application on an Android device or emulator.

## High-Level Architecture
- **Frontend**: Android application built in Kotlin.
- **Backend**: Processes images using Mediapipe and TensorFlow to extract dominant colors.
- **External APIs**:
    - **Google GenAI**: Provides location-based outfit color recommendations.
    - **SerpApi**: Scrapes the internet for clothing suggestions.

![Architecture Diagram](path/to/architecture-diagram.png)

## Usage
1. Upload a picture of your face in the **Profile Activity**.
2. Enter your name, age, and gender.
3. Input the location name in **Main Activity**.
4. Receive outfit color recommendations along with explanations.
5. Browse clothing suggestions, prices, and purchase links.

## Technologies Used
- **Languages**: Kotlin
- **Frameworks**: Android Studio, TensorFlow, Mediapipe
- **APIs**: Google Gemini API, SerpApi

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contributing
Feel free to submit issues and enhancement requests, or fork this repository to contribute.

---

Let me know if you want to include additional details or make edits!