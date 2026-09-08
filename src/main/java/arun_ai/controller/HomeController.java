package arun_ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return "index";
    }

    @GetMapping("/api/version")
    @ResponseBody
    public Map<String, Object> version() {
        return Map.of(
                "version", "3.2.0-speed",
                "name", "Arun AI",
                "status", "live",
                "speedEngine", "Antigravity Ultra-Fast",
                "timestamp", System.currentTimeMillis()
        );
    }
}