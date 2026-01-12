package org.t2404e.kanji_together_db.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.AiStoryRequest;
import org.t2404e.kanji_together_db.dto.AiStoryResponse;
import org.t2404e.kanji_together_db.entity.KanjiStoryAi;
import org.t2404e.kanji_together_db.repository.KanjiStoryAiRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiKanjiService {

    @Autowired
    private KanjiStoryAiRepository repository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Đổi sang gemini-1.5-flash
    // Sửa dòng này: Đổi sang model gemini-2.5-flash (đang có trong danh sách của bạn)
    // Đổi sang bản Lite để không bị giới hạn 20 request/ngày
    // Dùng alias "gemini-flash-latest" -> Tự chọn bản ổn định & miễn phí cao nhất
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KanjiStoryAi createStoryFromAi(AiStoryRequest request) {
        // 1. Tạo Prompt (Lấy custom_prompt từ request)
        String finalPrompt =
                "Bạn là chuyên gia ngôn ngữ học Kanji theo phương pháp Heisig (Chiết tự hình ảnh). " +
                        "Nhiệm vụ: Phân tích Kanji [" + request.getKanji() + "] thành các bộ thủ và tạo câu chuyện logic.\n" +

                        "QUY TẮC 1: PHÂN TÍCH BỘ THỦ (Cực kỳ chính xác)\n" +
                        "- Tách chữ thành các phần nhỏ nhất (Radicals).\n" +
                        "- Gán mỗi bộ một hình ảnh CỤ THỂ, ĐỜI THƯỜNG (Không dùng khái niệm trừu tượng).\n" +
                        "  Dùng [BỘ PHẬN 1](Nghĩa 1) kết hợp [BỘ PHẬN 2](Nghĩa 2)... để tạo thành [Bộ phận  ĐÍCH](Nghĩa đích)\n" +

                        "QUY TẮC 2: CÂU CHUYỆN (Ngắn - Sốc - Dễ nhớ)\n" +
                        "- Kết hợp các hình ảnh bộ thủ thành một hành động phi lý hoặc hài hước.\n" +
                        "- TUYỆT ĐỐI KHÔNG viết văn hoa (như 'hành trình cuộc đời', 'nắng mưa'...). Chỉ tập trung vào hình ảnh bộ thủ.\n" +

                        "QUY TẮC 3: JLPT LEVEL (Phải chuẩn)\n" +
                        "- Hãy đánh giá độ khó thực tế. (Ví dụ: 歳 nét nhiều là N3, đừng điền N5).\n" +

                        "OUTPUT JSON (Không Markdown, Chỉ 1 Object):\n" +
                        "{ " +
                        "\"kanji\": \"" + request.getKanji() + "\", " +
                        "\"han_viet\": \"Âm Hán Việt (Viết hoa)\", " +
                        "\"meaning\": \"Nghĩa tiếng Việt (Ngắn gọn)\", " +
                        "\"story\": \"[BỘ Thủ thành phần 1(chữ Hán)] kết hợp [BỘ thủ thành phần 2(Chữ Hán)]... để tạo thành [Chữ ĐÍCH(chữ Hán)].\", " +
                        "\"jlpt_level\": \"(N5/N4/N3/N2/N1)\" " +
                        "}";
        // 2. Gọi API Gemini
        String jsonResponse = callGeminiApi(finalPrompt);

        // 3. Làm sạch JSON
        String cleanJson = cleanJsonString(jsonResponse);

        try {
            // 4. Map JSON -> DTO
            AiStoryResponse aiResponse = objectMapper.readValue(cleanJson, AiStoryResponse.class);

            // 5. Lưu vào Entity (Bảng kanji_stories_ai)
            KanjiStoryAi entity = new KanjiStoryAi();
            entity.setKanji(aiResponse.getKanji());
            entity.setMeaning(aiResponse.getMeaning());

// XỬ LÝ LÀM SẠCH STORY:
// Lấy story gốc từ AI
            String rawStory = aiResponse.getStory();
// Dùng hàm replace để xóa bỏ hết dấu ** và dấu * đi
            String cleanStory = rawStory.replace("**", "").replace("*", "");
            entity.setStory(cleanStory);
            entity.setJlpt_level(aiResponse.getJlpt_level());
            return repository.save(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi xử lý dữ liệu AI: " + e.getMessage());
        }
    }


    private String callGeminiApi(String prompt) {
        try {
            String url = GEMINI_URL + geminiApiKey;
            Map<String, Object> content = Collections.singletonMap("parts", Collections.singletonList(Collections.singletonMap("text", prompt)));
            Map<String, Object> body = Collections.singletonMap("contents", Collections.singletonList(content));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            return extractText(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không kết nối được với AI");
        }
    }

    private String extractText(String rawJson) {
        try {
            return objectMapper.readTree(rawJson).path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) { return ""; }
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        Pattern pattern = Pattern.compile("```json(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) return matcher.group(1).trim();
        return raw.replace("```json", "").replace("```", "").trim();
    }
}