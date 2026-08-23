export interface PageResponse<E> {
    elements: E[];
    number: number;
    size: number;
    totalPages: number;
    totalElements: number;
}

export enum Direction {
    Asc = 'ASC',
    Desc = 'DESC',
}
