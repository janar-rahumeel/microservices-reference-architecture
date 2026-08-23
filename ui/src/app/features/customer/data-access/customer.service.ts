import {inject, Injectable} from '@angular/core';
import {catchError, Observable, of} from 'rxjs';
import {Direction, PageResponse} from '@core/models/page-response.model';
import {EntityService} from '@shared/data-access/entity-service.model';
import {SearchService} from '@shared/data-access/search-service.model';
import {Customer, CustomerFilter, CustomerListElement, CustomerSearchRequest} from '../models/customer.model';
import {CustomerRepository} from './customer.repository';

@Injectable()
export class CustomerService implements SearchService<CustomerFilter, CustomerListElement>, EntityService<Customer, string> {
    private static readonly EMPTY_PAGE_RESPONSE: PageResponse<CustomerListElement> = {
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 0,
        elements: [] as CustomerListElement[],
    };

    private readonly repository: CustomerRepository = inject(CustomerRepository);

    public search(
        filter: CustomerFilter,
        sortProperty: string,
        sortDirection: Direction,
        pageIndex = 0,
        pageSize = 20,
    ): Observable<PageResponse<CustomerListElement>> {
        const request: CustomerSearchRequest = {
            partialName: filter.partialName || undefined,
            partialCode: filter.partialCode || undefined,
            sort: [`${sortProperty},${sortDirection}`],
            page: pageIndex,
            size: pageSize,
        };
        return this.repository.search(request).pipe(
            catchError(() => of(CustomerService.EMPTY_PAGE_RESPONSE)),
        );
    }

    public insert(customer: Customer): Observable<Customer> {
        return this.repository.insert(customer);
    }

    public update(ignored: Customer): Observable<Customer> {
        throw new Error('Update is not supported!');
    }

    public delete(customerSearchElement: CustomerListElement): Observable<unknown> {
        return this.repository.delete(customerSearchElement.id);
    }
}
