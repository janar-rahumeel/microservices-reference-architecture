export interface FieldError {
    field: string;
    code: string;
    parameters?: Record<string, unknown>;
}

export interface ApiError {
    id?: string;
    errorCode?: string;
    message?: string;
    fieldErrors?: FieldError[];
}
