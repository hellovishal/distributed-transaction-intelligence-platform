package com.vishal.dtx.analytics.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.dtx.analytics.model.VectorDocument;
import com.vishal.dtx.common.model.TransactionEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Slf4j
@Repository
public class QdrantVectorRepository implements VectorRepository {

    @Value("${qdrant.base-url}")
    private String baseUrl;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Value("${qdrant.vector-dimension}")
    private int vectorDimension;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public QdrantVectorRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initCollection() {
        try {
            String checkUrl = baseUrl + "/collections/" + collectionName;
            ResponseEntity<String> response = restTemplate.getForEntity(checkUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Qdrant collection '{}' already exists", collectionName);
                return;
            }
        } catch (Exception e) {
            log.info("Collection '{}' does not exist, creating...", collectionName);
        }

        try {
            String createUrl = baseUrl + "/collections/" + collectionName;
            
            Map<String, Object> vectors = Map.of(
                    "size", vectorDimension,
                    "distance", "Cosine"
            );
            Map<String, Object> body = Map.of("vectors", vectors);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.put(createUrl, request);
            log.info("Created Qdrant collection '{}' with dimension {}", collectionName, vectorDimension);
        } catch (Exception e) {
            log.error("Failed to create Qdrant collection: {}", e.getMessage());
        }
    }

    @Override
    public void store(TransactionEvent event, String content, float[] embedding) {
        try {
            String url = baseUrl + "/collections/" + collectionName + "/points";

            String pointId = UUID.randomUUID().toString();
            
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("transactionId", event.getTransactionId());
            payload.put("correlationId", event.getCorrelationId());
            payload.put("userId", event.getUserId());
            payload.put("amount", event.getAmount());
            payload.put("status", event.getStatus().name());
            payload.put("content", content);
            payload.put("timestamp", Instant.now().toString());

            List<Double> vector = new ArrayList<>();
            for (float f : embedding) {
                vector.add((double) f);
            }

            Map<String, Object> point = Map.of(
                    "id", pointId,
                    "vector", vector,
                    "payload", payload
            );

            Map<String, Object> body = Map.of("points", List.of(point));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.put(url, request);
            
            log.info("Stored vector for txId={} status={} pointId={}",
                    event.getTransactionId(), event.getStatus(), pointId);

        } catch (Exception e) {
            log.error("Failed to store vector in Qdrant: {}", e.getMessage(), e);
            throw new RuntimeException("Vector storage failed", e);
        }
    }

    @Override
    public List<VectorDocument> search(float[] queryEmbedding, int topK) {
        try {
            String url = baseUrl + "/collections/" + collectionName + "/points/search";

            List<Double> vector = new ArrayList<>();
            for (float f : queryEmbedding) {
                vector.add((double) f);
            }

            Map<String, Object> body = Map.of(
                    "vector", vector,
                    "limit", topK,
                    "with_payload", true
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode results = root.get("result");

            List<VectorDocument> documents = new ArrayList<>();
            
            if (results != null && results.isArray()) {
                for (JsonNode result : results) {
                    String id = result.get("id").asText();
                    JsonNode payload = result.get("payload");
                    
                    String content = payload.has("content") ? payload.get("content").asText() : "";
                    
                    Map<String, Object> metadata = new LinkedHashMap<>();
                    metadata.put("transactionId", payload.has("transactionId") ? payload.get("transactionId").asText() : "");
                    metadata.put("userId", payload.has("userId") ? payload.get("userId").asText() : "");
                    metadata.put("status", payload.has("status") ? payload.get("status").asText() : "");
                    metadata.put("amount", payload.has("amount") ? payload.get("amount").asDouble() : 0.0);
                    metadata.put("score", result.has("score") ? result.get("score").asDouble() : 0.0);

                    documents.add(new VectorDocument(id, null, content, metadata));
                }
            }

            log.info("Search returned {} results", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Failed to search Qdrant: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
