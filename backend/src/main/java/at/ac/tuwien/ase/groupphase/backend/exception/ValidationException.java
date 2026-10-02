package at.ac.tuwien.ase.groupphase.backend.exception;

import java.util.List;

public class ValidationException extends ErrorListException {

    public ValidationException(String messageSummary, List<String> errors) {
        super(messageSummary, errors);
    }
}
