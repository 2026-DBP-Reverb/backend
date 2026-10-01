package dbp.backend.common.exception;

import dbp.backend.common.exception.code.CommonErrorCode;

public class DatabaseException extends ApiException {
    public DatabaseException() {
        super(CommonErrorCode.DATABASE_ERROR);
    }

    public DatabaseException(Throwable cause) {
        super(CommonErrorCode.DATABASE_ERROR, cause);
    }
}
