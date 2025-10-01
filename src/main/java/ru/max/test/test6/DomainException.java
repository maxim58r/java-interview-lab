package ru.max.test.test6;

import java.util.Map;

public abstract class DomainException extends RuntimeException {
    private final String code;
    private final int status;
    private final transient Map<String, Object> props;

    protected DomainException(String code, int status, String msg, Throwable cause, Map<String, Object> p) {
        super(msg, cause);
        this.code = code;
        this.status = status;
        this.props = p == null ? Map.of() : p;
    }

    public String code() {
        return code;
    }

    public int status() {
        return status;
    }

    public Map<String, Object> props() {
        return props;
    }

    // ==== типовые доменные исключения ====
    public static final class NotFound extends DomainException {
        public NotFound(String entity, String id) {
            super("NOT_FOUND", 404, entity + " " + id + " not found", null, Map.of("id", id));
        }
    }

    public static final class Conflict extends DomainException {
        public Conflict(String code, String msg, Map<String, Object> p) {
            super(code == null ? "CONFLICT" : code, 409, msg, null, p);
        }
    }

    public static final class Validation extends DomainException {
        public Validation(String msg, Map<String, Object> violations) {
            super("VALIDATION_FAILED", 422, msg, null, Map.of("violations", violations));
        }
    }

    public static final class Unauthorized extends DomainException {
        public Unauthorized() {
            super("UNAUTHORIZED", 401, "Authentication required or invalid", null, Map.of());
        }
    }

    public static final class Forbidden extends DomainException {
        public Forbidden(String msg) {
            super("FORBIDDEN", 403, msg, null, Map.of());
        }
    }

    public static final class RateLimit extends DomainException {
        public RateLimit(long retryAfterSec) {
            super("RATE_LIMIT", 429, "Too many requests", null, Map.of("retryAfter", retryAfterSec));
        }
    }
}
