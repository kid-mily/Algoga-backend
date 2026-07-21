package com.kidmily.algoga_server.chatbot.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.chatbot.application.command.*;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminCommandUseCase;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotAdminQueryUseCase;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.presentation.api.request.*;
import com.kidmily.algoga_server.chatbot.presentation.api.response.AdminChatLogResponse;
import com.kidmily.algoga_server.chatbot.presentation.api.response.RagSourceStatResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot Admin", description = "관리자 전용 챗봇 지식/버튼 관리 API")
@PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ChatbotAdminController {

    private final ChatbotAdminCommandUseCase commandUseCase;
    private final ChatbotAdminQueryUseCase queryUseCase;

    @PostMapping("/suggested-questions")
    @Operation(summary = "[버튼 지식] 예상 질문 등록")
    public ResponseEntity<ApiResponse<Void>> registerSuggestedQuestion(@Valid @RequestBody RegisterSuggestedQuestionRequest request) {
        commandUseCase.registerSuggestedQuestion(new RegisterSuggestedQuestionCommand(request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_CREATED", "예상 질문 버튼이 등록되었습니다.", null));
    }

    @GetMapping("/suggested-questions")
    @Operation(summary = "[버튼 지식] 예상 질문 전체 조회 (어드민용)")
    public ResponseEntity<ApiResponse<List<SuggestedQuestionAdminResponse>>> getAllSuggestedQuestions() {
        List<SuggestedQuestionAdminResponse> responses = queryUseCase.getAllSuggestedQuestions().stream()
                .map(dto -> new SuggestedQuestionAdminResponse(dto.suggestedQuestionId(), dto.question(), dto.answer())).toList();
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTIONS_LOADED", "목록 조회 성공", responses));
    }

    @GetMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 단건 조회")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<SuggestedQuestionAdminResponse>> getSuggestedQuestion(@PathVariable("id") Long id) {
        var dto = queryUseCase.getSuggestedQuestion(id);
        var response = new SuggestedQuestionAdminResponse(dto.suggestedQuestionId(), dto.question(), dto.answer());
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_LOADED", "상세 조회 성공", response));
    }

    @PutMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 수정")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> updateSuggestedQuestion(@PathVariable("id") Long id, @Valid @RequestBody UpdateSuggestedQuestionRequest request) {
        commandUseCase.updateSuggestedQuestion(new UpdateSuggestedQuestionCommand(id, request.question(), request.answer()));
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_UPDATED", "예상 질문이 수정되었습니다.", null));
    }

    @DeleteMapping("/suggested-questions/{id}")
    @Operation(summary = "[버튼 지식] 예상 질문 삭제")
    @ApiErrorCodeExample(domain = ChatbotErrorCode.class, value = {"SUGGESTED_QUESTION_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> deleteSuggestedQuestion(@PathVariable("id") Long id) {
        commandUseCase.deleteSuggestedQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("SUGGESTED_QUESTION_DELETED", "예상 질문이 삭제되었습니다.", null));
    }

    @GetMapping("/chat-logs")
    @Operation(summary = "[대화 로그] 챗봇 대화 내역 조회",
            description = "모든 유저의 챗봇 질문/답변 로그를 최신순으로 페이징 조회합니다(한 페이지 20건). "
                    + "filtered(차단 여부), 기간(from/to), keyword(질문·답변 본문 검색)로 필터링할 수 있습니다. "
                    + "filtered=true 인 로그는 도메인 외/차단된 질문이며 answer 에 거절 메시지가 담깁니다.")
    public ResponseEntity<ApiResponse<PageResponse<AdminChatLogResponse>>> getChatLogs(
            @Parameter(description = "차단 여부 필터. true=차단된 질문만, false=정상 질문만, 미지정=전체")
            @RequestParam(required = false) Boolean filtered,

            @Parameter(description = "조회 시작일(포함, yyyy-MM-dd). 예: 2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "조회 종료일(포함, yyyy-MM-dd). 예: 2026-07-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "질문/답변 본문 키워드 검색어")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        ZoneId zone = ZoneId.systemDefault();
        // 종료일은 '그날 23:59:59까지' 포함되도록 다음날 0시를 미만(exclusive) 상한으로 사용한다.
        Instant fromInstant = (from != null) ? from.atStartOfDay(zone).toInstant() : null;
        Instant toExclusive = (to != null) ? to.plusDays(1).atStartOfDay(zone).toInstant() : null;
        String keywordOrNull = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        var command = new SearchChatLogCommand(filtered, fromInstant, toExclusive, keywordOrNull, page);
        var pageData = queryUseCase.getChatLogs(command);

        var responseList = pageData.content().stream()
                .map(dto -> new AdminChatLogResponse(
                        dto.chatLogId(), dto.userId(), dto.userName(), dto.userNickname(),
                        dto.question(), dto.answer(), dto.filtered(), dto.createdAt()
                )).toList();

        PageResponse<AdminChatLogResponse> finalResponse = new PageResponse<>(
                responseList, pageData.page(), pageData.size(), pageData.totalElements(),
                pageData.totalPages(), pageData.first(), pageData.last()
        );

        return ResponseEntity.ok(ApiResponse.success("ADMIN_CHAT_LOGS_LOADED", "챗봇 대화 로그 조회 성공", finalResponse));
    }

    @GetMapping("/rag-source-stats")
    @Operation(summary = "[대화 로그] 규정 문서/페이지별 RAG 채택 빈도 (페이징)",
            description = "챗봇이 답변 근거로 채택(검색되어 실제 사용)한 규정 문서·페이지의 빈도를 count 내림차순으로 페이징 집계합니다(한 페이지 20건). "
                    + "어떤 규정이 가장 자주 근거로 쓰이는지 파악하는 용도입니다. 기간(from/to)으로 범위를 좁힐 수 있습니다.")
    public ResponseEntity<ApiResponse<PageResponse<RagSourceStatResponse>>> getRagSourceStats(
            @Parameter(description = "집계 시작일(포함, yyyy-MM-dd). 미지정 시 전체 기간")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "집계 종료일(포함, yyyy-MM-dd). 미지정 시 전체 기간")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant fromInstant = (from != null) ? from.atStartOfDay(zone).toInstant() : null;
        Instant toExclusive = (to != null) ? to.plusDays(1).atStartOfDay(zone).toInstant() : null;

        var pageData = queryUseCase.getRagSourceStats(fromInstant, toExclusive, page);

        var responseList = pageData.content().stream()
                .map(dto -> new RagSourceStatResponse(dto.source(), dto.page(), dto.count()))
                .toList();

        PageResponse<RagSourceStatResponse> finalResponse = new PageResponse<>(
                responseList, pageData.page(), pageData.size(), pageData.totalElements(),
                pageData.totalPages(), pageData.first(), pageData.last()
        );

        return ResponseEntity.ok(ApiResponse.success("ADMIN_RAG_SOURCE_STATS_LOADED", "RAG 채택 빈도 조회 성공", finalResponse));
    }

    @GetMapping("/chat-logs/export")
    @Operation(summary = "[대화 로그] 챗봇 대화 내역 CSV 다운로드",
            description = "조회와 동일한 필터(filtered/from/to/keyword)에 해당하는 대화 로그 전체를 CSV 파일로 내려받습니다(페이징 없음).")
    public ResponseEntity<byte[]> exportChatLogs(
            @RequestParam(required = false) Boolean filtered,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant fromInstant = (from != null) ? from.atStartOfDay(zone).toInstant() : null;
        Instant toExclusive = (to != null) ? to.plusDays(1).atStartOfDay(zone).toInstant() : null;
        String keywordOrNull = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        var rows = queryUseCase.getAllChatLogs(filtered, fromInstant, toExclusive, keywordOrNull);

        StringBuilder sb = new StringBuilder();
        sb.append("chatLogId,userId,userName,userNickname,question,answer,filtered,createdAt\n");
        for (var r : rows) {
            sb.append(csv(r.chatLogId())).append(',')
              .append(csv(r.userId())).append(',')
              .append(csv(r.userName())).append(',')
              .append(csv(r.userNickname())).append(',')
              .append(csv(r.question())).append(',')
              .append(csv(r.answer())).append(',')
              .append(csv(r.filtered())).append(',')
              .append(csv(r.createdAt())).append('\n');
        }

        return csvResponse("chatbot-chat-logs.csv", sb.toString());
    }

    @GetMapping("/rag-source-stats/export")
    @Operation(summary = "[대화 로그] RAG 채택 빈도 CSV 다운로드",
            description = "조회와 동일한 기간 필터(from/to)에 해당하는 문서/페이지별 채택 빈도 전체를 CSV 파일로 내려받습니다(페이징 없음).")
    public ResponseEntity<byte[]> exportRagSourceStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant fromInstant = (from != null) ? from.atStartOfDay(zone).toInstant() : null;
        Instant toExclusive = (to != null) ? to.plusDays(1).atStartOfDay(zone).toInstant() : null;

        var rows = queryUseCase.getAllRagSourceStats(fromInstant, toExclusive);

        StringBuilder sb = new StringBuilder();
        sb.append("source,page,count\n");
        for (var r : rows) {
            sb.append(csv(r.source())).append(',')
              .append(csv(r.page())).append(',')
              .append(csv(r.count())).append('\n');
        }

        return csvResponse("chatbot-rag-source-stats.csv", sb.toString());
    }

    // ── CSV 헬퍼 ──

    /** CSV 한 필드 이스케이프: 항상 큰따옴표로 감싸고 내부 큰따옴표는 두 번(")으로 이스케이프. null 은 빈 값. */
    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String s = value.toString().replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    /** UTF-8 BOM 을 붙여(엑셀에서 한글 안 깨지게) CSV 다운로드 응답을 만든다. */
    private ResponseEntity<byte[]> csvResponse(String filename, String csvBody) {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = csvBody.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(out);
    }
}