package com.anduckhmt146.chatbot.Chatbot.controllers;

import com.anduckhmt146.chatbot.Chatbot.config.DataLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private final EmbeddingModel embeddingModel;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DataLoader.class);

    public OllamaController(OllamaChatModel ollamaChatModel, VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.chatClient = ChatClient.create(ollamaChatModel);
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam("query") String query) {
        String jsonPrompt = """
        You are a helpful AI assistant. Based on the following user query,
        respond strictly in this JSON format:

        {
          "answer": "<your answer here>"
        }

        If you are unsure, respond with:
        {
          "answer": "I don't know."
        }

        Question: %s
        """.formatted(query);

        ChatResponse response = chatClient.prompt(jsonPrompt).call().chatResponse();
        String answer = response.getResult().getOutput().getText();

        // JSON format
        if (!answer.startsWith("{") || !answer.endsWith("}")) {
            return ResponseEntity.badRequest().body("Invalid response format. Expected JSON.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(answer);
    }

    @GetMapping("/embedding")
    public ResponseEntity<String> getEmbedding(@RequestParam("query") String text) {
        try {
            var embedding = embeddingModel.embed(text);

            String embeddingJson = new ObjectMapper().writeValueAsString(embedding);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"embedding\": " + embeddingJson + "}");

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return ResponseEntity
                    .status(500)
                    .body("{\"error\": \"Failed to generate embedding.\"}");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam("query") String query) {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(1)
                .build());

        if (docs.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"answer\": \"I don't know\"}");
        }

        // Combine document contents
        String context = String.join("\n\n", docs.stream()
                .map(Document::getFormattedContent)
                .toList());

        String jsonPrompt = """
            You are a product search assistant.
            
            You will receive a product-related user query and a list of retrieved product documents.
            
            Based ONLY on the provided product documents, return a **JSON object** with the following format:
            
            {
              "title": "<product title>",
              "description": "<short product description>",
              "price": "<price, e.g., $19.50>",
              "category": "<product category>",
              "features": "<comma-separated list of main features>"
            }
            
            If you cannot answer confidently based on the data, return:
            
            {
              "title": "",
              "description": "",
              "price": "",
              "category": "",
              "features": ""
            }
            
            Context:
            %s
            
            User query: %s
            """.formatted(context, query);


        String response = chatClient
                .prompt(jsonPrompt)
                .call()
                .content();

        if (!response.startsWith("{") || !response.endsWith("}")) {
            return ResponseEntity.badRequest().body("Invalid JSON response from model.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}
