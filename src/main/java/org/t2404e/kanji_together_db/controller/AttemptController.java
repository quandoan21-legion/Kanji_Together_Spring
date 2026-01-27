package org.t2404e.kanji_together_db.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.AttemptRequest;
import org.t2404e.kanji_together_db.dto.AttemptResponse;
import org.t2404e.kanji_together_db.dto.BatchAttemptRequest;
import org.t2404e.kanji_together_db.dto.ErrorResponse;
import org.t2404e.kanji_together_db.dto.KanjiMasteryView;
import org.t2404e.kanji_together_db.entity.UserKanjiMastery;
import org.t2404e.kanji_together_db.service.UserAttemptService;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Attempts", description = "APIs for submitting user attempts")
public class AttemptController {
    private final UserAttemptService userAttemptService;

    public AttemptController(UserAttemptService userAttemptService) {
        this.userAttemptService = userAttemptService;
    }

    @PostMapping("/attempts")
    @Operation(
            summary = "Submit a single attempt",
            description = "Records a single question attempt and automatically updates kanji mastery."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempt accepted and mastery updated",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or question not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AttemptResponse> submitAttempt(@RequestBody AttemptRequest request) {
        return ResponseEntity.ok(userAttemptService.submitOne(request));
    }

    @PostMapping("/attempts/batch")
    @Operation(
            summary = "Submit multiple attempts",
            description = "Records multiple question attempts in a single transaction (all-or-nothing)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Batch accepted and mastery updated",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttemptResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or question not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<AttemptResponse>> submitBatch(@RequestBody BatchAttemptRequest request) {
        List<AttemptRequest> attempts = request != null ? request.getAttempts() : null;
        return ResponseEntity.ok(userAttemptService.submitBatch(attempts));
    }

    @GetMapping("/users/{userId}/kanji/{kanjiId}/mastery")
    @Operation(
            summary = "Get kanji mastery",
            description = "Returns the mastery state for a specific user and kanji.",
            tags = {"Kanji Mastery"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mastery state returned",
                    content = @Content(schema = @Schema(implementation = KanjiMasteryView.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or kanji not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<KanjiMasteryView> getMastery(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID of the kanji", required = true)
            @PathVariable Long kanjiId
    ) {
        UserKanjiMastery mastery = userAttemptService.getMastery(userId, kanjiId);
        KanjiMasteryView view = new KanjiMasteryView();
        view.setKanjiId(mastery.getKanji().getId());
        view.setMasteryLevel(mastery.getMasteryLevel());
        view.setEaseFactor(mastery.getEaseFactor());
        view.setIntervalDays(mastery.getIntervalDays());
        view.setRepetitions(mastery.getRepetitions());
        view.setNextReviewAt(mastery.getNextReviewAt());
        return ResponseEntity.ok(view);
    }

    @GetMapping("/review/due")
    @Operation(
            summary = "Get due review kanji",
            description = "Returns kanji items due for review for a given user.",
            tags = {"Review"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Due review list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = KanjiMasteryView.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<KanjiMasteryView>> getDue(
            @Parameter(description = "ID of the user", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Maximum number of kanji to return")
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(userAttemptService.getDueMasteryAndSyncDailyExam(userId, limit));
    }
}
