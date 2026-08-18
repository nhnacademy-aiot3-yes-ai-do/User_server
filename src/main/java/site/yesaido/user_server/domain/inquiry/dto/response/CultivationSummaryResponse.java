package site.yesaido.user_server.domain.inquiry.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CultivationSummaryResponse(
        Long cultivationId,
        String name,
        String status,
        String mode
) {
}
