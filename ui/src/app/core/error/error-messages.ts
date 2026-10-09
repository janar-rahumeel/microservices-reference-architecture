const ERROR_MESSAGES: Readonly<Record<string, string>> = {
  'validation.not-null': 'This field is required',
  'validation.not-blank': 'This field is required',
  'validation.not-empty': 'At least one value is required',
  'validation.too-short': 'Must be at least {min} characters long',
  'validation.too-long': 'Must be at most {max} characters long',
  'validation.exact-digits': 'Must consist of exactly {length} digits',
  'general.technical.field-validation': 'Some fields are invalid',
  'general.technical.entity-not-found': 'The requested item was not found',
  'general.technical.service-unavailable': 'Service is temporarily unavailable',
  'general.technical.error': 'An unexpected error occurred',
  'general.business.duplicate-entity': 'This item already exists',
  'general.business.invariant-violation': 'The request violates a business rule',
  'general.business.rule-violation': 'The request violates a business rule',
  'customer.business.duplicate-entity': 'A customer with these details already exists',
  'customer.technical.entity-not-found': 'Customer not found',
  'user.technical.entity-not-found': 'User not found',
};

export function translateErrorCode(code: string, parameters?: Record<string, unknown>, fallback?: string): string {
  const template: string | undefined = ERROR_MESSAGES[code];
  if (template === undefined) {
    return fallback ?? code;
  }
  return template.replace(/\{(\w+)}/g, (placeholder: string, name: string): string => {
    const value: unknown = parameters?.[name];
    return value === undefined ? placeholder : String(value);
  });
}
