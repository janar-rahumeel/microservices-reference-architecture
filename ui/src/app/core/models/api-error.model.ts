export interface EntityFieldValidationError {
    fieldName: string;
    validationErrorMessage: string;
}

export interface ApiError {
    uuid?: string;
    timestamp: Date;
    message?: string;
    entityFieldValidationErrors: EntityFieldValidationError[];
}
