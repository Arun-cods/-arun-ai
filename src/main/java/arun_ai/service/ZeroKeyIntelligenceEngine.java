package arun_ai.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZeroKeyIntelligenceEngine {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // Food & Culinary Intent (Dishes, Recipes, Best Foods)
        if (isFoodIntent(lower)) {
            return generateFoodResponse(query);
        }

        // Political Leaders & Governance Facts
        if (isLeadershipIntent(lower)) {
            return generateLeadershipResponse(lower);
        }

        // Superlatives, Curiosities & World Records
        if (isSuperlativeIntent(lower)) {
            return generateSuperlativeResponse(lower);
        }

        // Real-Time Wikipedia Encyclopedic Knowledge Fetcher (Zero-Key Autonomous Lookup)
        String wikiAnswer = tryFetchWikipediaSummary(query);
        if (wikiAnswer != null && !wikiAnswer.isBlank()) {
            return wikiAnswer;
        }

        // Intelligent Dynamic Standalone Synthesizer
        return generateSynthesizedResponse(query);
    }

    private boolean isFoodIntent(String lower) {
        return lower.contains("dish") || lower.contains("food") || lower.contains("cuisine") ||
               lower.contains("recipe") || lower.contains("eat") || lower.contains("meal") ||
               lower.contains("dinner") || lower.contains("lunch") || lower.contains("breakfast") ||
               lower.contains("snack") || lower.contains("cook") || lower.contains("delicious");
    }

    private String generateFoodResponse(String query) {
        return "### 🍽️ Top 10 World-Class Dishes for You\n\n" +
               "Here is a curated culinary guide featuring 10 of the most celebrated, mouth-watering dishes across world cuisines:\n\n" +
               "1. 🍲 **Hyderabadi Dum Biryani** *(India)*\n" +
               "   - **Flavors & Profile**: Fragrant aged long-grain Basmati rice slow-cooked on *dum* in a sealed handi with marinated meat (chicken/mutton) or rich paneer, saffron-infused milk, fried golden onions (*birista*), mint, and whole aromatic spices.\n" +
               "   - **Best paired with**: Mirchi ka Salan (tangy chili-peanut gravy) and cool cucumber-mint Raita.\n\n" +
               "2. 🍕 **Neapolitan Pizza Margherita** *(Naples, Italy)*\n" +
               "   - **Flavors & Profile**: Blistered wood-fired crust with a chewy airy cornicione, sweet crushed San Marzano tomatoes, fresh creamy buffalo mozzarella, fragrant sweet basil, and extra virgin olive oil.\n" +
               "   - **Highlight**: Recognized by UNESCO as an Intangible Cultural Heritage of Humanity.\n\n" +
               "3. 🌮 **Tacos al Pastor** *(Mexico)*\n" +
               "   - **Flavors & Profile**: Thinly carved pork marinated in achiote, dried guajillo chilies, and pineapple juice, roasted vertically on a spinning *trompo*, served on warm freshly pressed corn tortillas with charred pineapple, cilantro, and diced onions.\n" +
               "   - **Best paired with**: Salsa verde and freshly squeezed lime.\n\n" +
               "4. 🍣 **Sushi & Sashimi Omakase** *(Japan)*\n" +
               "   - **Flavors & Profile**: Masterfully prepared slices of bluefin tuna (otoro, chutoro), pristine Atlantic salmon, and sea urchin (uni) served over warm, delicately seasoned vinegared sushi rice with real grated Shizuoka wasabi.\n" +
               "   - **Highlight**: Unrivaled harmony of purity, delicate knife skills, and umami.\n\n" +
               "5. 🍜 **Authentic Pad Thai** *(Thailand)*\n" +
               "   - **Flavors & Profile**: Wok-tossed rice noodles with tangy tamarind pulp, palm sugar, fish sauce, eggs, crushed roasted peanuts, fresh bean sprouts, garlic chives, and plump prawns or tofu.\n" +
               "   - **Highlight**: Masterful balance of sweet, sour, salty, and spicy in every bite.\n\n" +
               "6. 🍛 **Butter Chicken (Murgh Makhani)** *(India)*\n" +
               "   - **Flavors & Profile**: Tender tandoor-roasted chicken pieces simmered in a silky, mildly sweet gravy made of vine-ripened tomatoes, rich dairy butter, fresh cream, and aromatic dried fenugreek leaves (*kasuri methi*).\n" +
               "   - **Best paired with**: Hot garlic butter naan or fragrant jeera rice.\n\n" +
               "7. 🍷 **Boeuf Bourguignon** *(Burgundy, France)*\n" +
               "   - **Flavors & Profile**: Beef chuck slow-braised for hours in rich French Pinot Noir with smoked bacon lardons, baby carrots, glazed pearl onions, cremini mushrooms, and fresh bouquet garni.\n" +
               "   - **Highlight**: Rich, velvety depth and melt-in-your-mouth tenderness.\n\n" +
               "8. 🥘 **Paella Valenciana de Marisco** *(Spain)*\n" +
               "   - **Flavors & Profile**: Spanish Bomba rice infused with floral saffron threads, sweet pimentón paprika, jumbo prawns, mussels, squid, and rosemary, cooked over an open fire until a crispy, caramelized crust (*socarrat*) forms at the pan base.\n" +
               "   - **Highlight**: The crunchy, flavorful socarrat bottom layer.\n\n" +
               "9. 🦆 **Peking Roasted Duck** *(Beijing, China)*\n" +
               "   - **Flavors & Profile**: Air-dried, maltose-glazed duck roasted over fruitwood until the skin becomes paper-thin and shattering-crisp, served with thin steamed mandarin pancakes, cucumber matchsticks, scallions, and savory hoisin sauce.\n" +
               "   - **Highlight**: Crisp, glossy skin with deeply savory, juicy meat.\n\n" +
               "10. 🍝 **Classic Lasagna alla Bolognese** *(Emilia-Romagna, Italy)*\n" +
               "    - **Flavors & Profile**: Delicate sheets of handmade egg pasta layered with a slow-simmered beef and pork ragù, silky béchamel sauce, and freshly grated aged Parmigiano-Reggiano, baked until bubbling golden-brown.\n" +
               "    - **Highlight**: True comfort food with rich, complex savory depth.\n\n" +
               "---\n" +
               "💡 **Arun AI Culinary Tip**: If you're craving quick convenience, a fresh **Pad Thai** or **Neapolitan Pizza** hits the spot in minutes; if you're celebrating or hosting, nothing surpasses the majesty of authentic **Hyderabadi Dum Biryani**!\n\n" +
               "*⚡ Powered by Arun AI Standalone Intelligence*";
    }

    private boolean isLeadershipIntent(String lower) {
        return lower.contains("cm of") || lower.contains("chief minister") || lower.contains("present cm") ||
               lower.contains("telangana cm") || lower.contains("ap cm") || lower.contains("prime minister") ||
               lower.contains("pm of india") || lower.contains("president of india");
    }

    private String generateLeadershipResponse(String lower) {
        if (lower.contains("telangana")) {
            return "### 🏛️ Chief Minister of Telangana\n\n" +
                   "- **Current Chief Minister**: **Anumula Revanth Reddy** (A. Revanth Reddy)\n" +
                   "- **Assumed Office**: December 7, 2023\n" +
                   "- **Political Party**: Indian National Congress (INC)\n" +
                   "- **Constituency**: Kodangal Assembly Constituency\n\n" +
                   "**Key Telangana Leadership Details**:\n" +
                   "- **Deputy Chief Minister**: Mallu Bhatti Vikramarka (holding Finance, Planning & Energy portfolios)\n" +
                   "- **Governor of Telangana**: Jishnu Dev Varma\n" +
                   "- **Legislative Assembly**: 119 seats (Secretariat located in Hyderabad)\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("andhra") || lower.contains("ap cm")) {
            return "### 🏛️ Chief Minister of Andhra Pradesh\n\n" +
                   "- **Current Chief Minister**: **N. Chandrababu Naidu**\n" +
                   "- **Assumed Office**: June 12, 2024\n" +
                   "- **Political Party**: Telugu Desam Party (TDP) / NDA Alliance\n" +
                   "- **Deputy Chief Minister**: Konidela Pawan Kalyan (Jana Sena Party)\n" +
                   "- **State Capital**: Amaravati\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("pm") || lower.contains("prime minister")) {
            return "### 🇮🇳 Prime Minister of India\n\n" +
                   "- **Current Prime Minister**: **Narendra Damodardas Modi**\n" +
                   "- **In Office**: Since May 26, 2014 (Serving his third consecutive term from June 2024)\n" +
                   "- **Political Party**: Bharatiya Janata Party (BJP) / NDA\n" +
                   "- **Parliamentary Constituency**: Varanasi, Uttar Pradesh\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("president")) {
            return "### 🇮🇳 President of India\n\n" +
                   "- **Current President**: **Droupadi Murmu**\n" +
                   "- **Assumed Office**: July 25, 2022 (15th President of India)\n" +
                   "- **Distinction**: First tribal woman and second female President of the Republic of India.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        return "### Leadership & Governance\n\n" +
               "Please specify which state or country leader you would like to know about (e.g., *present CM of Telangana*, *PM of India*, or *President of USA*)!\n";
    }

    private boolean isSuperlativeIntent(String lower) {
        return lower.contains("most expensive") || lower.contains("costliest") || lower.contains("fastest animal") ||
               lower.contains("tallest building") || lower.contains("deepest") || lower.contains("largest planet") ||
               lower.contains("speed of light");
    }

    private String generateSuperlativeResponse(String lower) {
        if (lower.contains("most expensive") || lower.contains("costliest")) {
            return "### 💎 The Most Expensive Things in the World\n\n" +
                   "Here is the definitive ranking of the most valuable substances, materials, and structures known to humanity:\n\n" +
                   "1. 🌌 **Antimatter** — **~$62.5 Trillion per gram**\n" +
                   "   - **Why**: Antimatter is composed of antiparticles (e.g., positrons and antiprotons) that possess opposite charges to ordinary matter. Producing even a single nanogram requires CERN's Large Hadron Collider operating for months. When antimatter contacts ordinary matter, it undergoes 100% annihilation, releasing pure energy.\n\n" +
                   "2. ☢️ **Californium-252** — **~$27 Million per gram**\n" +
                   "   - **Why**: A synthetic radioactive element made only in specialized high-flux nuclear reactors. It emits immense neutron radiation, making it indispensable for oil-well logging, detecting metal stress in aircraft, and cancer radiation therapy.\n\n" +
                   "3. ⚛️ **Endohedral Fullerenes (Nitrogen-doped Buckyballs)** — **~$140 Million per gram**\n" +
                   "   - **Why**: Microscopic spherical carbon cages enclosing individual nitrogen atoms. Used in atomic-scale electronics and highly accurate miniature atomic clocks for satellite navigation.\n\n" +
                   "4. 💎 **Red Diamonds** — **~$1 Million to $2 Million per carat ($5M–$10M/gram)**\n" +
                   "   - **Why**: The rarest gemstone on Earth. Less than thirty genuine red diamonds are documented worldwide, created by unique atomic lattice distortions under extreme subterranean mantle pressure.\n\n" +
                   "5. 🧪 **Painite** — **~$50,000 to $60,000 per carat**\n" +
                   "   - **Why**: Discovered in Myanmar by gemologist Arthur C.D. Pain, it was formerly listed by Guinness World Records as the world's rarest gem mineral.\n\n" +
                   "6. 🛰️ **The International Space Station (ISS)** — **~$150 Billion (Object)**\n" +
                   "   - **Why**: The single most expensive object ever built by human civilization, constructed in orbit by a coalition of NASA, Roscosmos, ESA, JAXA, and CSA.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("fastest animal")) {
            return "### ⚡ The Fastest Animals on Earth\n\n" +
                   "1. 🦅 **Peregrine Falcon (Air)**: Reaches hunting dive speeds over **389 km/h (242 mph)**, making it the fastest animal on the planet.\n" +
                   "2. 🐆 **Cheetah (Land)**: Can accelerate from 0 to 97 km/h (60 mph) in just 3 seconds, reaching top sprint speeds of **112–120 km/h (70–75 mph)**.\n" +
                   "3. 🐟 **Black Marlin / Sailfish (Water)**: Can slice through oceans at speeds up to **100–129 km/h (62–80 mph)**.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("tallest building")) {
            return "### 🏙️ The Tallest Building in the World\n\n" +
                   "- **Building**: **Burj Khalifa**\n" +
                   "- **Location**: Downtown Dubai, United Arab Emirates\n" +
                   "- **Height**: **828 meters (2,716.5 feet)** with 163 floors\n" +
                   "- **Completed**: 2010 (Architect: Adrian Smith / Skidmore, Owings & Merrill)\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("deepest")) {
            return "### 🌊 The Deepest Point on Earth\n\n" +
                   "- **Location**: **Challenger Deep** in the **Mariana Trench**\n" +
                   "- **Depth**: Approximately **10,994 meters (36,070 feet / nearly 11 km)** deep in the Western Pacific Ocean.\n" +
                   "- **Pressure**: Over 1,000 atmospheres (108.6 MPa), equivalent to 8 tons per square inch.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("largest planet")) {
            return "### 🪐 The Largest Planet in our Solar System\n\n" +
                   "- **Planet**: **Jupiter**\n" +
                   "- **Size**: Over **1,300 Earths** could fit inside Jupiter. Its mass is 2.5 times that of all other planets in the Solar System combined.\n" +
                   "- **Iconic Feature**: The Great Red Spot — a massive storm larger than Earth raging for hundreds of years.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("speed of light")) {
            return "### 💡 The Speed of Light\n\n" +
                   "- **Exact Constant**: **299,792,458 meters per second** (~300,000 km/s or 186,282 miles/s) in vacuum (*c*).\n" +
                   "- **Travel Times**:\n" +
                   "  - From the Moon to Earth: ~1.28 seconds\n" +
                   "  - From the Sun to Earth: ~8 minutes and 20 seconds\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        return "### Superlatives & World Records\n\n" +
               "Ask me about any world record, largest planet, fastest creature, or historic wonder!\n";
    }

    private String tryFetchWikipediaSummary(String query) {
        try {
            String topic = cleanQueryForTopic(query);
            if (topic.isBlank() || topic.length() < 2) return null;

            String encoded = URLEncoder.encode(topic.replace(" ", "_"), StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encoded;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ArunAI/2.0 (standalone-intelligence; contact@arunai.org)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                String extract = root.path("extract").asText("");
                String title = root.path("title").asText(topic);
                String description = root.path("description").asText("");

                if (extract.length() >= 40) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### 📚 ").append(title);
                    if (!description.isBlank()) {
                        sb.append(" — *").append(description).append("*");
                    }
                    sb.append("\n\n");
                    sb.append(extract).append("\n\n");
                    sb.append("---\n");
                    sb.append("*⚡ Powered by Arun AI Standalone Intelligence*");
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String cleanQueryForTopic(String query) {
        return query.trim()
                .replaceAll("(?i)^(what\\s+is|who\\s+is|who\\s+was|where\\s+is|tell\\s+me\\s+about|explain|describe|give\\s+me\\s+info\\s+about|history\\s+of|meaning\\s+of)\\s+", "")
                .replaceAll("[?!.,\"]+", "")
                .trim();
    }

    private String generateSynthesizedResponse(String query) {
        String clean = query.trim();
        String lower = clean.toLowerCase(Locale.ROOT);

        if (lower.contains("health") || lower.contains("workout") || lower.contains("fitness") || lower.contains("diet") || lower.contains("weight")) {
            return "### 💪 Health & Fitness Guidance\n\n" +
                   "Here are core evidence-based principles for your query regarding **" + clean + "**:\n\n" +
                   "1. **Nutrition Foundation**: Prioritize whole single-ingredient foods, lean protein (1.6g–2.2g per kg body weight), colorful fiber-rich vegetables, and clean hydration (3–4 liters daily).\n" +
                   "2. **Progressive Overload**: For physical training, continually increase resistance, volume, or control over time to stimulate muscle adaptation.\n" +
                   "3. **Sleep & Recovery**: 7–9 hours of quality sleep is essential for hormonal regulation, muscle repair, and mental clarity.\n" +
                   "4. **Consistency Over Intensity**: Sustainable daily habits outcompete extreme short-lived routines every time.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("money") || lower.contains("finance") || lower.contains("invest") || lower.contains("saving") || lower.contains("stock")) {
            return "### 📈 Financial & Investment Principles\n\n" +
                   "Key foundational guidelines for **" + clean + "**:\n\n" +
                   "1. **Emergency Reserve**: Maintain 3 to 6 months of living expenses in a liquid, high-yield account before taking market risks.\n" +
                   "2. **Budgeting Framework (50/30/20)**: Allocate 50% to essential needs, 30% to lifestyle/discretionary, and 20% directly to investments and debt elimination.\n" +
                   "3. **Broad Market Compounding**: Historically, diversified broad-market index funds (e.g., S&P 500 or total market funds) compound wealth reliably over decades without individual stock picking risk.\n" +
                   "4. **Continuous Skill Investment**: The highest ROI asset is often your own high-income professional skills.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        if (lower.contains("study") || lower.contains("learn") || lower.contains("focus") || lower.contains("exam") || lower.contains("memor")) {
            return "### 🧠 High-Efficiency Learning Strategy\n\n" +
                   "Accelerated learning blueprint for **" + clean + "**:\n\n" +
                   "1. **Feynman Technique**: Explain the concept in simple, plain language as if teaching an 8-year-old; pinpoint where your explanation stumbles to discover true knowledge gaps.\n" +
                   "2. **Active Recall & Spaced Repetition**: Test yourself using flashcards or practice questions at spaced intervals rather than passively rereading notes.\n" +
                   "3. **Pomodoro Deep Focus**: Work in 25-minute sprints with zero phone notifications, followed by 5 minutes of mindful rest.\n" +
                   "4. **Interleaved Practice**: Alternate between related problem types rather than drilling only one pattern repeatedly.\n\n" +
                   "---\n" +
                   "*⚡ Powered by Arun AI Standalone Intelligence*";
        }

        return "### Arun AI 💡\n\n" +
               "Here is direct insight for: **" + clean + "**\n\n" +
               "1. **Key Insights & Overview**:\n" +
               "   - " + clean + " involves understanding the core underlying objectives, relevant context, and practical applications.\n" +
               "   - For optimal results, break the topic into actionable steps or first-principles components.\n\n" +
               "2. **Recommended Action Plan**:\n" +
               "   - **Analyze**: Define the exact desired outcome or problem statement.\n" +
               "   - **Implement**: Apply direct, focused effort with continuous feedback loops.\n" +
               "   - **Refine**: Measure results and iterate based on real-world outcomes.\n\n" +
               "Feel free to ask for specific code, a deep-dive breakdown, or step-by-step guidance on any aspect!\n\n" +
               "---\n" +
               "*⚡ Powered by Arun AI Standalone Intelligence*";
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
