# Arun AI · Gemini & ChatGPT Experience

A modern Spring Boot chat application modeled after Google Gemini AI and ChatGPT, backed by Google Gemini via Spring AI.

## Key Features

- **Gemini & ChatGPT Aesthetics**: Sleek dark and light mode themes with smooth transitions and persistent theme preferences.
- **Collapsible Sidebar**: Organized conversation history grouped by *Today*, *Yesterday*, *Previous 7 Days*, and *Older*, with search filtering, inline renaming, and conversation deletion.
- **Multi-turn Memory**: Retains conversational context across chat turns for natural, context-aware dialogue.
- **Real-Time Streaming**: Token streaming with Server-Sent Events (SSE) and live stop generation control.
- **Rich Markdown & Code Highlighting**: Full GitHub Flavored Markdown support (tables, lists, blockquotes) and syntax-highlighted code blocks with language badges and one-click "Copy code" buttons.
- **Audio & Voice Capabilities**:
  - **Read Aloud (TTS)**: Listens to assistant responses using browser text-to-speech with play/stop toggle.
  - **Voice Dictation (STT)**: Microphone speech-to-text dictation right into the composer.
- **Prompt Suggestion Cards**: Quick-start interactive cards for brainstorming, code generation, summarization, and concept explanation.
- **File & Code Attachment**: Attach local files (`.txt`, `.java`, `.py`, `.js`, etc.) into prompt context.
- **Model Selector**: Switch between Gemini 2.5 Flash, Gemini 2.5 Pro, and Gemini 2.5 Flash Lite.

## Run Arun AI

1. Configure your Gemini API key in your terminal:
   ```powershell
   $env:GEMINI_API_KEY = "your-gemini-api-key"
   .\mvnw.cmd spring-boot:run
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

3. Enjoy your Gemini & ChatGPT powered personal thought partner!

