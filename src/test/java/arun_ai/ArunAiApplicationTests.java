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

	@Test
	void contextLoads() {
		assertNotNull(chatController);
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

