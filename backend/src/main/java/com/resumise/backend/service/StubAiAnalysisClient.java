package com.resumise.backend.service;

import com.resumise.backend.model.Cv;
import com.resumise.backend.model.JobPosting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Component
public class StubAiAnalysisClient implements AiAnalysisClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String advicePath;

    public StubAiAnalysisClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${AI_BASE_URL:http://localhost:8000}") String aiBaseUrl,
            @Value("${ai.api.get-advice-path:/get-advice}") String advicePath
    ) {
        this.webClient = webClientBuilder.baseUrl(aiBaseUrl).build();
        this.objectMapper = objectMapper;
        this.advicePath = advicePath;
    }

    @Override
    public AiAnalysisPayload analyze(Cv cv, JobPosting jobPosting) {
        byte[] cvBytes = readCv(cv);
        String cvFileName = cv.getFileName() != null ? cv.getFileName() : "cv.pdf";
        String jobDescription = jobPosting.getNotes() != null ? jobPosting.getNotes() : jobPosting.getJobLink();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("cv_file", new ByteArrayResource(cvBytes) {
            @Override
            public String getFilename() {
                return cvFileName;
            }
        });
        body.add("job_description", jobDescription);

        String raw = webClient.post()
                .uri(advicePath)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(45))
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI API request failed", ex))
                .block();

        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI API returned empty response");
        }

        return toPayload(raw);
    }

    private byte[] readCv(Cv cv) {
        Path path = Path.of(cv.getFilePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file could not be read", ex);
        }
    }

    private AiAnalysisPayload toPayload(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode payload = root.path("data").isObject() ? root.path("data") : root;

            int matchScore = intOrDefault(payload, "match_score", 0);
            String strengths = stringify(payload, "strengths", "matched_skills", "pros");
            String gaps = stringify(payload, "skill_gap", "gaps", "missing_skills", "cons");

            String cvSuggestions = stringify(payload, "cv_suggestions", "suggestions", "recommendations");
            String interviewPrep = stringify(payload, "interview_prep", "interview_questions");
            String actionItems = mergeSections(
                    section("CV Suggestions", cvSuggestions),
                    section("Interview Prep", interviewPrep)
            );

            String summary = textOrFirstAvailable(payload, "summary", "advice", "message", "result");
            if ("AI advice generated".equals(summary) && !strengths.isBlank()) {
                summary = strengths;
            }

            return new AiAnalysisPayload(
                    matchScore,
                    summary,
                    strengths,
                    gaps,
                    actionItems,
                    raw
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI API returned invalid JSON", ex);
        }
    }

    private int intOrDefault(JsonNode root, String key, int defaultValue) {
        JsonNode node = root.path(key);
        return node.isInt() ? node.intValue() : defaultValue;
    }

    private String textOrFirstAvailable(JsonNode root, String... keys) {
        for (String key : keys) {
            JsonNode node = root.path(key);
            if (node.isTextual() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return "AI advice generated";
    }

    private String stringify(JsonNode root, String... keys) {
        for (String key : keys) {
            JsonNode node = root.path(key);
            if (node == null || node.isMissingNode() || node.isNull()) {
                continue;
            }
            if (node.isTextual()) {
                return node.asText();
            }
            if (node.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < node.size(); i++) {
                    JsonNode item = node.get(i);
                    String line = item.isTextual() ? item.asText() : item.toString();
                    if (!line.isBlank()) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append("- ").append(line);
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
            return node.toString();
        }
        return "";
    }

    private String section(String title, String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        return title + ":\n" + content;
    }

    private String mergeSections(String... sections) {
        StringBuilder sb = new StringBuilder();
        for (String section : sections) {
            if (section == null || section.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(section);
        }
        return sb.toString();
    }
}

