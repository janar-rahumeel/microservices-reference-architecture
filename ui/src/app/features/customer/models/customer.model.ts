export enum CustomerType {
    Person = 'PERSON',
    LegalEntity = 'LEGAL_ENTITY',
}

export interface CustomerFilter {
    partialName?: string;
    partialCode?: string;
}

export interface CustomerSearchRequest {
    partialName?: string;
    partialCode?: string;
    page: number;
    size: number;
    sort?: string[];
}

export interface CustomerListElement {
    id: string;
    type: CustomerType;
    name: string;
    code: string;
}

export type NewCustomer = NewPersonCustomer | NewLegalEntityCustomer;

export interface NewPersonCustomer {
    type: CustomerType.Person;
    firstName: string;
    lastName: string;
    personalIdentificationCode: string;
}

export interface NewLegalEntityCustomer {
    type: CustomerType.LegalEntity;
    name: string;
    registrationCode: string;
}

export type Customer = PersonCustomer | LegalEntityCustomer;

export interface PersonCustomer {
    type: CustomerType.Person;
    id: string;
    firstName: string;
    lastName: string;
    personalIdentificationCode: string;
}

export interface LegalEntityCustomer {
    type: CustomerType.LegalEntity;
    id: string;
    name: string;
    registrationCode: string;
}
