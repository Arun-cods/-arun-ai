# Arun AI Mobile App (React Native & Expo)

This is the standalone React Native mobile app for **Arun AI**, featuring:
- Full Gemini & ChatGPT interface (Obsidian Dark & Clean Light modes)
- Direct connection to Arun AI Java Spring Boot backend
- Suggestion cards (Brainstorm, Code, Draft, Explain)
- Quick chat clear & model switching
- Configured for 1-click **Android APK Build**

---

### How to Build the Android APK (100% Free):
Run this command from inside the `arun-mobile` folder:
```bash
npx eas-cli build -p android --profile preview
```
Expo will compile a signed `.apk` in the cloud and give you a direct download link to install on any Android phone!

---

### How to Run on Web / Mobile Browser:
```bash
npx expo start --web
```