import { Observable } from 'rxjs';
import { Direction, PageResponse } from '@core/models/page-response.model';

export interface SearchService<F, E> {
    search(filter: F, sortProperty: string, sortDirection: Direction, pageIndex: number, pageSize: number): Observable<PageResponse<E>>;
}
