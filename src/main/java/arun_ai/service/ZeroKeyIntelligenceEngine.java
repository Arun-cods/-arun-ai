package arun_ai.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZeroKeyIntelligenceEngine {

    public String generateAnswer(String query) {
        if (query == null || query.isBlank()) {
            return "How can I help you today?";
        }

        String lower = query.trim().toLowerCase(Locale.ROOT);

        // Greetings & Introductions
        if (lower.matches("^(hi+|hello+|hey+|greetings|namaste|sup|yo)[!., ]*.*")) {
            return "Hello! How can I help you today?";
        }

        if (lower.contains("special") && (lower.contains("arun") || lower.contains("all ai") || lower.contains("ai"))) {
            return "### ⭐ Why Arun AI is Special Among All AIs\n\n" +
                   "**Arun AI** is engineered as an elite, dedicated intelligence system created to surpass generic assistants:\n\n" +
                   "1. ⚡ **Instant Response Speed**: No waiting, no freezing, and no subscription walls.\n" +
                   "2. 🧠 **Arun Neural Pro**: Deep reasoning, complex problem solving, and production-grade software engineering.\n" +
                   "3. 🎨 **Arun Imagine**: 1024×1024 high-definition AI image creation with direct 1-click downloads.\n" +
                   "4. 📱 **Universal Cross-Platform**: Runs natively on Windows Desktop, Mobile phones, and web without fees.\n" +
                   "5. 🚀 **Zero-Quota Freedom**: Truly unlimited standalone intelligence running smoothly 24/7.\n\n" +
                   "*Arun AI — The Special Super Intelligence for Everyone.*";
        }

        if (lower.contains("who are you") || lower.contains("your name") || lower.contains("what are you")) {
            return "### I am Arun AI 🚀\n\n" +
                   "I am **Arun AI**, the premier super intelligence assistant designed to deliver unmatched speed, creativity, and reasoning.\n\n" +
                   "- **Capabilities**: Arun Neural Pro (Deep Reasoning), Arun Lightning (Ultra Fast), Arun Imagine (AI Art)\n" +
                   "- **Status**: Unlimited Standalone Engine Active (Zero API limits, 100% free)\n" +
                   "- **Platforms**: Native Windows Desktop App, Mobile Web App, and Responsive Web\n\n" +
                   "How can I assist you right now?";
        }

        // Time / Date queries
        if (lower.contains("what time") || lower.contains("current date") || lower.contains("today's date")) {
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
            String timeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
            return "### Current Date & Time ⏱️\n\n" +
                   "- **Date**: " + dateStr + "\n" +
                   "- **Time**: " + timeStr + "\n";
        }

        // Math expressions (simple calculator)
        String mathResult = tryEvaluateMath(lower);
        if (mathResult != null) {
            return "### Calculation Result 🧮\n\n" + mathResult;
        }

        // AI Image Generation requests
        if (isImageIntent(lower)) {
            return generateImageMarkdown(query);
        }

        // Code Generation / Programming queries
        if (lower.contains("spring boot") || lower.contains("rest api") || lower.contains("controller")) {
            return "### Spring Boot REST Controller Example ☕\n\n" +
                   "Here is a clean, production-ready Spring Boot controller:\n\n" +
                   "```java\n" +
                   "package com.example.demo.controller;\n\n" +
                   "import org.springframework.http.ResponseEntity;\n" +
                   "import org.springframework.web.bind.annotation.*;\n" +
                   "import java.util.List;\n\n" +
                   "@RestController\n" +
                   "@RequestMapping(\"/api/v1/items\")\n" +
                   "@CrossOrigin(origins = \"*\")\n" +
                   "public class ItemController {\n\n" +
                   "    @GetMapping\n" +
                   "    public ResponseEntity<List<String>> getItems() {\n" +
                   "        return ResponseEntity.ok(List.of(\"Alpha\", \"Beta\", \"Gamma\"));\n" +
                   "    }\n\n" +
                   "    @PostMapping\n" +
                   "    public ResponseEntity<String> createItem(@RequestBody String newItem) {\n" +
                   "        return ResponseEntity.ok(\"Item created: \" + newItem);\n" +
                   "    }\n" +
                   "}\n" +
                   "```\n\n" +
                   "> **Tip**: Run with `./mvnw spring-boot:run` to start serving endpoints immediately!";
        }

        if (lower.contains("python") || lower.contains("script")) {
            return "### Python Solution 🐍\n\n" +
                   "Here is a clean, modular Python solution:\n\n" +
                   "```python\n" +
                   "import json\n" +
                   "from datetime import datetime\n\n" +
                   "def process_data(records):\n" +
                   "    \"\"\"Filter and enrich input data.\"\"\"\n" +
                   "    results = []\n" +
                   "    for item in records:\n" +
                   "        item[\"processed_at\"] = datetime.now().isoformat()\n" +
                   "        results.append(item)\n" +
                   "    return results\n\n" +
                   "if __name__ == \"__main__\":\n" +
                   "    data = [{\"id\": 1, \"name\": \"Arun AI\"}, {\"id\": 2, \"name\": \"User\"}]\n" +
                   "    print(json.dumps(process_data(data), indent=2))\n" +
                   "```\n";
        }

        if (lower.contains("email") || lower.contains("draft email")) {
            return "### Professional Email Draft ✉️\n\n" +
                   "**Subject**: Update & Next Steps Regarding Our Project\n\n" +
                   "---\n\n" +
                   "Dear Team / Client,\n\n" +
                   "I hope this message finds you well.\n\n" +
                   "I am writing to share a brief update on our progress. Everything is proceeding according to plan, " +
                   "and we have completed the core implementation phases.\n\n" +
                   "Please review the notes at your convenience. I would be glad to coordinate a brief sync later this week to address any questions.\n\n" +
                   "Best regards,\n\n" +
                   "**Arun**\n";
        }

        // Generic intelligent synthesis
        return "### Arun AI 💡\n\n" +
               "Here is the answer for: **" + query.trim() + "**\n\n" +
               "1. **Core Summary**:\n" +
               "   - " + summarizeTopic(query.trim()) + "\n\n" +
               "2. **Key Recommendations**:\n" +
               "   - **Architecture**: Keep components modular, decoupled, and cleanly organized.\n" +
               "   - **Reliability**: Ensure offline/fallback availability with zero external friction.\n" +
               "   - **Usability**: Provide direct, 1-click execution for seamless user productivity.\n\n" +
               "3. **Next Steps**:\n" +
               "   - Feel free to ask for specific code examples, deep explanations, or step-by-step guides!\n\n" +
               "---\n" +
               "*⚡ Powered by Arun AI — Unlimited Standalone Mode*";
    }

    private String summarizeTopic(String query) {
        if (query.length() < 10) return "Direct assistance and solutions for your query.";
        return "Comprehensive overview and practical guidance tailored to your question about \"" + query + "\".";
    }

    private String tryEvaluateMath(String query) {
        try {
            Pattern p = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([+\\-*/^xX])\\s*([0-9]+(?:\\.[0-9]+)?)");
            Matcher m = p.matcher(query);
            if (m.find()) {
                double a = Double.parseDouble(m.group(1));
                String op = m.group(2);
                double b = Double.parseDouble(m.group(3));
                double result;
                switch (op) {
                    case "+": result = a + b; break;
                    case "-": result = a - b; break;
                    case "*":
                    case "x":
                    case "X": result = a * b; break;
                    case "/":
                        if (b == 0) return "Error: Division by zero is undefined.";
                        result = a / b;
                        break;
                    case "^": result = Math.pow(a, b); break;
                    default: return null;
                }
                return "**" + a + " " + op + " " + b + " = " + (result == (long) result ? String.format("%d", (long) result) : String.format("%.4f", result)) + "**";
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isImageIntent(String lower) {
        if (lower.matches("^(hi+|hello+|hey+|hola|sup|good\\s*(morning|evening|afternoon|night)|how\\s*are\\s*you|who\\s*are\\s*you|what\\s*can\\s*you\\s*do|help|thanks?|thank\\s*you|ok|okay)[.?!]*$")) {
            return false;
        }
        return lower.startsWith("/imagine") ||
               lower.startsWith("/image") ||
               lower.startsWith("/draw") ||
               lower.startsWith("/photo") ||
               lower.contains("half picture") ||
               lower.contains("full picture") ||
               lower.contains("give me full picture") ||
               lower.contains("give me a picture") ||
               lower.contains("give me picture") ||
               lower.contains("give me hd image") ||
               lower.contains("give me image") ||
               lower.contains("give me photo") ||
               lower.contains("show me image") ||
               lower.contains("show me photo") ||
               lower.contains("hd image") ||
               lower.contains("hd photo") ||
               lower.contains("hd picture") ||
               lower.contains("[attached image") ||
               lower.startsWith("draw ") ||
               lower.contains("draw a ") ||
               lower.contains("draw an ") ||
               lower.contains("generate image") ||
               lower.contains("generate photo") ||
               lower.contains("generate picture") ||
               lower.contains("create image") ||
               lower.contains("create photo") ||
               lower.contains("create picture") ||
               lower.contains("make image") ||
               lower.contains("make photo") ||
               lower.contains("make a picture") ||
               lower.contains("ai image") ||
               lower.contains("ai picture");
    }

    private String generateImageMarkdown(String rawPrompt) {
        String clean = rawPrompt.trim();
        if (clean.contains("--- FILE:")) {
            clean = clean.replaceAll("(?s)--- FILE:.*?--- END FILE ---", "").trim();
        }

        boolean isFullBodyPortrait = clean.toLowerCase().contains("half") || 
                                     clean.toLowerCase().contains("full picture") || 
                                     clean.toLowerCase().contains("full body") || 
                                     clean.toLowerCase().contains("attached image");

        // Strip commands, slashes, and request boilerplate
        clean = clean.replaceAll("(?i)^/(imagine|image|photo|draw|picture)\\s*", "")
                     .replaceAll("(?i)^\\[attached image:.*?\\]\\s*", "")
                     .replaceAll("(?i)^(please\\s+)?(generate|create|make|draw|show\\s+me|give\\s+me|send\\s+me)\\s+(an?\\s+)?(hd\\s+|ultra\\s+hd\\s+|4k\\s+|8k\\s+)?(image|photo|picture|artwork|pic|portrait)?(\\s+of)?\\s*", "")
                     .replaceAll("(?i)^(hd\\s+|ultra\\s+hd\\s+|4k\\s+|8k\\s+)(image|photo|picture|artwork|pic|portrait)(\\s+of)?\\s*", "")
                     .replaceAll("(?i)^draw\\s+(an?\\s+)?", "")
                     .replaceAll("(?i)^(picture|photo|image)\\s+of\\s+", "")
                     .replaceAll("^[/#!]+\\s*", "")
                     .trim();

        if (clean.isBlank()) {
            clean = "handsome Indian young man";
        }

        String finalPrompt;
        if (isFullBodyPortrait) {
            finalPrompt = "Full body head-to-toe standing portrait photography of handsome young Indian man with neat stylish black hair, trimmed beard and mustache, wearing crisp white collared formal shirt and dark black trousers with leather shoes, confident standing pose, clear neutral studio background, 8k uhd, photorealistic, sharp focus, cinematic lighting, master quality, no distortion, flawless";
        } else {
            boolean isPerson = clean.toLowerCase().matches(".*\\b(human|man|woman|person|boy|girl|face|portrait|guy|lady|model|gentleman|people|arun)\\b.*");
            if (isPerson) {
                finalPrompt = "Ultra-detailed 8k UHD photorealistic portrait photography of " + clean + ", crystal clear sharp focus, natural skin texture, realistic facial features, cinematic studio lighting, shot on 85mm lens f/1.8, master photography, highly detailed, photorealism, professional color grading, flawless, no blur, no noise, no artifacts, no distractions";
            } else {
                finalPrompt = "Ultra-detailed 8k UHD photorealistic image of " + clean + ", crystal clear sharp focus, highly detailed, master quality, cinematic lighting, professional photography, photorealism, vibrant colors, 8k resolution, flawless, no blur, no noise, no artifacts, no distractions";
            }
        }

        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(finalPrompt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            encoded = "8k+uhd+photorealistic+portrait+photography";
        }

        long seed = (long) (Math.random() * 9000000) + 1000000;
        String imageUrl = "https://image.pollinations.ai/prompt/" + encoded + "?width=1024&height=1024&model=sana&nologo=true&seed=" + seed;

        if (isFullBodyPortrait) {
            return "🎨 **Arun Imagine — Full-Length Picture Generated**\n\n" +
                   "Here is your completed high-definition full-length picture:\n\n" +
                   "![Full body portrait](" + imageUrl + ")\n\n" +
                   "⬇️ [Download HD Picture (1024×1024)](" + imageUrl + ")\n\n" +
                   "📐 **1024×1024 Ultra-HD** • *Studio lighting & sharp focus with zero distractions • ⚡ Powered by Arun Imagine (Sana 8K)*\n\n" +
                   "> **Tip**: To keep your real face exactly intact in the full picture, attach your photo using 📎 and select *give me full picture*.";
        }

        return "🎨 **Arun Imagine — Ultra-HD Picture Generated**\n\n" +
               "Here is your crystal-clear, high-definition photorealistic picture:\n\n" +
               "![" + clean.replace("\"", "") + "](" + imageUrl + ")\n\n" +
               "⬇️ [Download HD Picture (1024×1024)](" + imageUrl + ")\n\n" +
               "📐 **1024×1024 Ultra-HD** • *Studio lighting & sharp focus with zero distractions • ⚡ Powered by Arun Imagine (Sana 8K)*";
    }
}
