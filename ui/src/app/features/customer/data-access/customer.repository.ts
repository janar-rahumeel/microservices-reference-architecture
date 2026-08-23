import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '@core/models/page-response.model';
import { environment } from '@env/environment';
import { Customer, CustomerListElement, CustomerSearchRequest, NewCustomer } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerRepository {
    private readonly httpClient: HttpClient = inject(HttpClient);

    public search(request: CustomerSearchRequest): Observable<PageResponse<CustomerListElement>> {
        let params: HttpParams = new HttpParams();

        if (request.partialName) {
            params = params.set('partialName', request.partialName);
        }

        if (request.partialCode) {
            params = params.set('partialCode', request.partialCode);
        }

        params = params.set('page', request.page);
        params = params.set('size', request.size);

        request.sort?.forEach((sort: string): void => {
            params = params.append('sort', sort);
        });

        return this.httpClient.get<PageResponse<CustomerListElement>>(`${environment.apiUrl}/api/v2/customers`, { params });
    }

    public insert(newCustomer: NewCustomer): Observable<Customer> {
        return this.httpClient.post<Customer>(`${environment.apiUrl}/api/v2/customers`, newCustomer);
    }

    public delete(id: string): Observable<unknown> {
        return this.httpClient.delete<unknown>(`${environment.apiUrl}/api/v2/customers/${id}`);
    }
}
