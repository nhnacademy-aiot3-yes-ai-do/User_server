package site.yesaido.user_server.domain.inquiry.dto.response;

public record CultivationSummaryResponse(
        Long cultivationId,
        String name,
        String status,
        String mode
) {
}
