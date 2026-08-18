package site.yesaido.user_server.global.exception;

public class SecurityFilterChainConfigurationException extends RuntimeException {
    public SecurityFilterChainConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
