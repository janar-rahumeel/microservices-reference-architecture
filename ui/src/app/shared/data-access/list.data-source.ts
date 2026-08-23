import { BehaviorSubject, Observable, ReplaySubject, Subject, take } from 'rxjs';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { Direction, PageResponse } from '@core/models/page-response.model';
import { SearchService } from './search-service.model';

export class ListDataSource<S extends SearchService<F, E>, F, E> extends DataSource<E> {
    public readonly totalElementCountSubject: Subject<number> = new ReplaySubject<number>(1);
    private readonly elementsSubject: Subject<E[]> = new BehaviorSubject<E[]>([]);

    public constructor(private readonly searchService: S) {
        super();
    }

    public override connect(ignoredCollectionViewer: CollectionViewer): Observable<E[]> {
        return this.elementsSubject.asObservable();
    }

    public override disconnect(ignoredCollectionViewer: CollectionViewer): void {
        this.totalElementCountSubject.complete();
        this.elementsSubject.complete();
    }

    public load(filter: F, sortProperty: string, sortDirection: string, pageIndex: number, pageSize: number): void {
        const direction: Direction = sortDirection.toUpperCase() as Direction;
        const observable: Observable<PageResponse<E>> = this.searchService.search(filter, sortProperty, direction, pageIndex, pageSize);
        observable.pipe(take(1)).subscribe((pageResponse: PageResponse<E>): void => {
            this.elementsSubject.next(pageResponse.elements);
            this.totalElementCountSubject.next(pageResponse.totalElements);
        });
    }
}
