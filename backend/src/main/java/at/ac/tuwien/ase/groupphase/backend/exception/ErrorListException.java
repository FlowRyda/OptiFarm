package at.ac.tuwien.ase.groupphase.backend.exception;

import java.util.Collections;
import java.util.List;

public abstract class ErrorListException extends Exception {

    private final List<String> errors;

    private final String messageSummary;

    public ErrorListException(String messageSummary, List<String> errors) {
        super(messageSummary);
        this.messageSummary = messageSummary;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return "%s: %s"
            .formatted(messageSummary, String.join(", ", errors));
    }

    public String summary() {
        return messageSummary;
    }

    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }
}
