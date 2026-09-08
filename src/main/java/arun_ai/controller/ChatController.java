package arun_ai.controller;

import arun_ai.service.ZeroKeyIntelligenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are Arun AI, the premier super intelligence assistant designed to be faster, smarter, and more creative than other AIs. " +
            "Provide fast, direct, brilliant, and concise responses. Get straight to the answer without unnecessary introductory filler. " +
            "Use GitHub Flavored Markdown cleanly: bold key terms, use bullet points, tables where appropriate, " +
            "and standard fenced code blocks with proper language identifiers for code. " +
            "Maintain context from the conversation history provided.";

    private final ChatClient defaultChatClient;
    private final ZeroKeyIntelligenceEngine zeroKeyIntelligenceEngine;
    private final arun_ai.service.PhotoOutpaintingService photoOutpaintingService;
    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

    public ChatController(ChatClient.Builder builder,
                          ZeroKeyIntelligenceEngine zeroKeyIntelligenceEngine,
                          arun_ai.service.PhotoOutpaintingService photoOutpaintingService) {
        this.defaultChatClient = builder.build();
        this.zeroKeyIntelligenceEngine = zeroKeyIntelligenceEngine;
        this.photoOutpaintingService = photoOutpaintingService;
    }

    private ChatClient resolveClient(String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            return clientCache.computeIfAbsent(apiKey.trim(), key -> {
                try {
                    var genAiClient = com.google.genai.Client.builder().apiKey(key).build();
                    var chatModel = GoogleGenAiChatModel.builder()
                            .genAiClient(genAiClient)
                            .build();
                    return ChatClient.builder(chatModel).build();
                } catch (Exception e) {
                    log.error("Failed to construct ChatClient for provided API key", e);
                    return defaultChatClient;
                }
            });
        }
        return defaultChatClient;
    }

    // Circuit breaker: track if cloud quota is exhausted so we don't crash streams or lag requests
    private static volatile long cloudQuotaBlockedUntil = 0L;

    public ResponseEntity<ChatResponse> chat(ChatRequest request) {
        return chat(request, null);
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String headerApiKey) {

        if (request.message() == null || request.message().isBlank()) {
            if (request.images() == null || request.images().isEmpty()) {
                return ResponseEntity.badRequest().body(new ChatResponse("Please enter a message."));
            }
        }

        String requestedModel = (request.model() != null && !request.model().isBlank()) ? request.model() : "arun-neural-pro";

        // Fast path: Photo Editing & Outpainting with real face preservation
        if (request.images() != null && !request.images().isEmpty()) {
            if (photoOutpaintingService.isClothingChangeIntent(request.message(), true)) {
                String shirtChanged = photoOutpaintingService.generateClothingChangedPhoto(request.images().get(0), request.message());
                return ResponseEntity.ok(new ChatResponse(shirtChanged));
            }
            if (photoOutpaintingService.isOutpaintingIntent(request.message(), true)) {
                String outpainted = photoOutpaintingService.generateOutpaintedFullPicture(request.images().get(0), request.message());
                return ResponseEntity.ok(new ChatResponse(outpainted));
            }
            if (photoOutpaintingService.isBackgroundChangeIntent(request.message(), true)) {
                String bgChanged = photoOutpaintingService.generateBackgroundChangedPhoto(request.images().get(0), request.message());
                return ResponseEntity.ok(new ChatResponse(bgChanged));
            }
        }

        // Fast path: AI Image Generation
        if (isImageRequest(request.message(), requestedModel)) {
            return ResponseEntity.ok(new ChatResponse(generateImageResponse(request.message())));
        }

        String key = (request.apiKey() != null && !request.apiKey().isBlank()) ? request.apiKey() : headerApiKey;
        boolean hasCustomKey = (request.apiKey() != null && !request.apiKey().isBlank());

        // Call cloud if custom key provided or cooldown passed
        if (hasCustomKey || System.currentTimeMillis() >= cloudQuotaBlockedUntil) {
            try {
                ChatClient client = resolveClient(key);
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> tryCallGemini(client, request, requestedModel));
                String answer = future.get(25000, TimeUnit.MILLISECONDS);
                if (answer != null && !answer.isBlank()) {
                    return ResponseEntity.ok(new ChatResponse(answer));
                }
            } catch (Exception e) {
                if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("quota") || e.getMessage().contains("Quota"))) {
                    cloudQuotaBlockedUntil = System.currentTimeMillis() + 60_000L;
                }
                log.info("Cloud engine timed out or had quota issue ({}), switching to standalone engine", e.getMessage());
            }
        }

        // Instant Zero-Key fallback
        String zeroKeyAnswer = zeroKeyIntelligenceEngine.generateAnswer(request.message());
        return ResponseEntity.ok(new ChatResponse(zeroKeyAnswer));
    }

    private String tryCallGemini(ChatClient client, ChatRequest request, String model) {
        List<Message> messages = buildMessages(request);
        var promptSpec = client.prompt()
                .system(DEFAULT_SYSTEM_PROMPT)
                .messages(messages);
        configureOptions(promptSpec, request, model);
        return promptSpec.call().content();
    }

    public Flux<String> chatStream(ChatRequest request) {
        return chatStream(request, null);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String headerApiKey) {

        if (request.message() == null || request.message().isBlank()) {
            if (request.images() == null || request.images().isEmpty()) {
                return Flux.just("Please enter a message.");
            }
        }

        String requestedModel = (request.model() != null && !request.model().isBlank()) ? request.model() : "arun-neural-pro";

        // Fast path: Photo Editing & Outpainting with real face preservation
        if (request.images() != null && !request.images().isEmpty()) {
            if (photoOutpaintingService.isClothingChangeIntent(request.message(), true)) {
                String shirtChanged = photoOutpaintingService.generateClothingChangedPhoto(request.images().get(0), request.message());
                String[] chunks = shirtChanged.split("(?<=\\s)");
                return Flux.fromArray(chunks).delayElements(Duration.ofMillis(10));
            }
            if (photoOutpaintingService.isOutpaintingIntent(request.message(), true)) {
                String outpainted = photoOutpaintingService.generateOutpaintedFullPicture(request.images().get(0), request.message());
                String[] chunks = outpainted.split("(?<=\\s)");
                return Flux.fromArray(chunks).delayElements(Duration.ofMillis(10));
            }
            if (photoOutpaintingService.isBackgroundChangeIntent(request.message(), true)) {
                String bgChanged = photoOutpaintingService.generateBackgroundChangedPhoto(request.images().get(0), request.message());
                String[] chunks = bgChanged.split("(?<=\\s)");
                return Flux.fromArray(chunks).delayElements(Duration.ofMillis(10));
            }
        }

        // Fast path: AI Image Generation
        if (isImageRequest(request.message(), requestedModel)) {
            String imgResp = generateImageResponse(request.message());
            String[] chunks = imgResp.split("(?<=\\s)");
            return Flux.fromArray(chunks).delayElements(Duration.ofMillis(10));
        }

        String key = (request.apiKey() != null && !request.apiKey().isBlank()) ? request.apiKey() : headerApiKey;
        boolean hasCustomKey = (request.apiKey() != null && !request.apiKey().isBlank());

        // If cloud is blocked due to 429 quota, stream from standalone engine immediately
        if (!hasCustomKey && System.currentTimeMillis() < cloudQuotaBlockedUntil) {
            return streamFallback(request.message());
        }

        try {
            ChatClient client = resolveClient(key);

            List<Message> messages = buildMessages(request);
            var promptSpec = client.prompt()
                    .system(DEFAULT_SYSTEM_PROMPT)
                    .messages(messages);

            configureOptions(promptSpec, request, requestedModel);

            return promptSpec.stream()
                    .content()
                    .timeout(Duration.ofMillis(20000))
                    .onErrorResume(e -> {
                        if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("quota") || e.getMessage().contains("Quota"))) {
                            cloudQuotaBlockedUntil = System.currentTimeMillis() + 60_000L;
                        }
                        log.info("Stream error ({}), streaming standalone engine response", e.getMessage());
                        return streamFallback(request.message());
                    });
        } catch (Exception exception) {
            log.info("Stream setup error ({}), streaming standalone engine response", exception.getMessage());
            return streamFallback(request.message());
        }
    }

    private Flux<String> streamFallback(String message) {
        String answer = zeroKeyIntelligenceEngine.generateAnswer(message);
        String[] chunks = answer.split("(?<=\\s)");
        return Flux.fromArray(chunks).delayElements(Duration.ofMillis(20));
    }

    private List<Message> buildMessages(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        if (request.history() != null) {
            for (ChatMessageDto item : request.history()) {
                if (item.content() == null || item.content().isBlank()) {
                    continue;
                }
                if ("user".equalsIgnoreCase(item.role())) {
                    messages.add(new UserMessage(item.content()));
                } else if ("assistant".equalsIgnoreCase(item.role()) || "model".equalsIgnoreCase(item.role())) {
                    messages.add(new AssistantMessage(item.content()));
                }
            }
        }

        String promptText = (request.message() != null && !request.message().isBlank()) 
                ? request.message().trim() 
                : "Analyze and explain this image in detail.";

        // Multimodal Vision support: attach images as Media objects
        if (request.images() != null && !request.images().isEmpty()) {
            List<Media> mediaList = new ArrayList<>();
            for (String imgBase64 : request.images()) {
                try {
                    String clean = imgBase64;
                    String mimeType = "image/jpeg";
                    if (clean.contains(";base64,")) {
                        String prefix = clean.substring(0, clean.indexOf(";base64,"));
                        if (prefix.contains(":")) {
                            mimeType = prefix.substring(prefix.indexOf(":") + 1);
                        }
                        clean = clean.substring(clean.indexOf(";base64,") + 8);
                    }
                    byte[] bytes = java.util.Base64.getDecoder().decode(clean.trim());
                    mediaList.add(new Media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(bytes)));
                } catch (Exception e) {
                    log.warn("Failed to parse image for vision analysis: {}", e.getMessage());
                }
            }
            if (!mediaList.isEmpty()) {
                messages.add(UserMessage.builder().text(promptText).media(mediaList).build());
                return messages;
            }
        }

        messages.add(new UserMessage(promptText));
        return messages;
    }

    private void configureOptions(ChatClient.ChatClientRequestSpec promptSpec, ChatRequest request, String model) {
        String modelName = "gemini-3.5-flash";
        if ("arun-lightning".equalsIgnoreCase(model) || "gemini-3.5-flash-lite".equalsIgnoreCase(model)) {
            modelName = "gemini-3.5-flash-lite";
        } else if ("arun-neural-pro".equalsIgnoreCase(model) || "gemini-3.5-flash".equalsIgnoreCase(model)) {
            modelName = "gemini-3.5-flash";
        }
        try {
            var optionsBuilder = GoogleGenAiChatOptions.builder().model(modelName);
            if (Boolean.TRUE.equals(request.search())) {
                optionsBuilder.googleSearchRetrieval(true);
            }
            promptSpec.options(optionsBuilder);
        } catch (Exception e) {
            log.warn("Could not apply custom model options: {}", e.getMessage());
        }
    }

    public static boolean isImageRequest(String message, String model) {
        if (message == null || message.isBlank()) return false;
        String lower = message.trim().toLowerCase(java.util.Locale.ROOT);

        // Conversational greetings and chat queries must NEVER be converted to images
        if (lower.matches("^(hi+|hello+|hey+|hola|sup|good\\s*(morning|evening|afternoon|night)|how\\s*are\\s*you|who\\s*are\\s*you|what\\s*can\\s*you\\s*do|help|thanks?|thank\\s*you|ok|okay)[.?!]*$")) {
            return false;
        }

        // Image editing intents (clothing, background, outpainting) must NEVER be hijacked by text-to-image
        if (lower.contains("shirt") || lower.contains("dress") || lower.contains("clothes") || 
            lower.contains("clothing") || lower.contains("background") || lower.contains("backdrop") || 
            lower.contains("bg change") || lower.contains("outpaint") || lower.contains("tshirt") ||
            lower.contains("t-shirt") || lower.contains("suit") || lower.contains("attire") ||
            lower.contains("recolor") || lower.contains("change color") || lower.contains("full body") ||
            lower.contains("give me full picture") || lower.contains("full picture")) {
            return false;
        }

        // Explicit slash commands
        if (lower.startsWith("/imagine") || lower.startsWith("/image") || lower.startsWith("/draw") || lower.startsWith("/photo")) {
            return true;
        }

        // Explicit image creation keywords
        if (lower.contains("half picture") ||
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
            lower.contains("generate an image") ||
            lower.contains("generate a image") ||
            lower.contains("generate a picture") ||
            lower.contains("generate an artwork") ||
            lower.contains("generate photo") ||
            lower.contains("generate picture") ||
            lower.contains("create image") ||
            lower.contains("create an image") ||
            lower.contains("create a image") ||
            lower.contains("create a picture") ||
            lower.contains("create a photo") ||
            lower.contains("create photo") ||
            lower.contains("create picture") ||
            lower.contains("make image") ||
            lower.contains("make an image") ||
            lower.contains("make a picture") ||
            lower.contains("make photo") ||
            lower.contains("ai image") ||
            lower.contains("ai picture")) {
            return true;
        }

        // In Imagine mode, only treat descriptive prompts as images, not questions or chatting
        if ("arun-imagine".equalsIgnoreCase(model)) {
            boolean isQuestionOrChat = lower.startsWith("what ") || lower.startsWith("why ") ||
                                       lower.startsWith("how ") || lower.startsWith("who ") ||
                                       lower.startsWith("can you ") || lower.startsWith("tell me ");
            return !isQuestionOrChat && lower.length() >= 2;
        }

        return false;
    }

    public static String generateImageResponse(String rawPrompt) {
        String clean = rawPrompt.trim();
        // Remove raw file artifacts if any
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

        // Handle clothing edit requests gracefully if passed to imagine
        if (clean.toLowerCase().contains("shirt") || clean.toLowerCase().contains("suit") || clean.toLowerCase().contains("dress") || clean.toLowerCase().contains("clothes")) {
            String col = "black";
            if (clean.toLowerCase().contains("blue")) col = "royal navy blue";
            else if (clean.toLowerCase().contains("red")) col = "crimson red";
            else if (clean.toLowerCase().contains("white")) col = "crisp white";
            else if (clean.toLowerCase().contains("green")) col = "emerald green";
            clean = "handsome young South Asian man with neat stylish black hair, trimmed beard, wearing a tailored " + col + " formal shirt";
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

    public record ChatMessageDto(String role, String content) { }
    public record ChatRequest(String message, List<ChatMessageDto> history, String model, Boolean search, String apiKey, List<String> images) {
        public ChatRequest(String message) {
            this(message, null, null, null, null, null);
        }
        public ChatRequest(String message, List<ChatMessageDto> history, String model, Boolean search) {
            this(message, history, model, search, null, null);
        }
        public ChatRequest(String message, List<ChatMessageDto> history, String model, Boolean search, String apiKey) {
            this(message, history, model, search, apiKey, null);
        }
    }
    public record ChatResponse(String message) { }
}
