package arun_ai;

import arun_ai.controller.ChatController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArunAiApplicationTests {

	@Autowired
	private ChatController chatController;

	@Autowired
	private arun_ai.service.ZeroKeyIntelligenceEngine zeroKeyIntelligenceEngine;

	@Test
	void contextLoads() {
		assertNotNull(chatController);
		assertNotNull(zeroKeyIntelligenceEngine);
	}

	@Test
	void testFoodDishesIntent() {
		String answer = zeroKeyIntelligenceEngine.generateAnswer("give me 10th best dishes for me");
		assertNotNull(answer);
		assertTrue(answer.contains("Biryani"), "Should include Biryani");
		assertTrue(answer.contains("Pizza"), "Should include Pizza");
		assertTrue(answer.contains("Tacos"), "Should include Tacos");
		assertTrue(answer.contains("Lasagna"), "Should include Lasagna");
		assertFalse(answer.contains("modular, decoupled"), "Must NOT contain generic architecture boilerplate");
	}

	@Test
	void testMostExpensiveThingIntent() {
		String answer = zeroKeyIntelligenceEngine.generateAnswer("what is the most expensive thing in this world");
		assertNotNull(answer);
		assertTrue(answer.contains("Antimatter"), "Should include Antimatter");
		assertTrue(answer.contains("Californium"), "Should include Californium");
		assertFalse(answer.contains("modular, decoupled"), "Must NOT contain generic architecture boilerplate");
	}

	@Test
	void testTelanganaCmIntent() {
		String answer = zeroKeyIntelligenceEngine.generateAnswer("present cm of telangana'");
		assertNotNull(answer);
		assertTrue(answer.contains("Revanth Reddy"), "Should name Revanth Reddy");
		assertFalse(answer.contains("modular, decoupled"), "Must NOT contain generic architecture boilerplate");
	}

	@Test
	void testEmptyMessageValidation() {
		ChatController.ChatRequest emptyRequest = new ChatController.ChatRequest("", null, null, null);
		ResponseEntity<ChatController.ChatResponse> response = chatController.chat(emptyRequest);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Please enter a message.", response.getBody().message());
	}
}

