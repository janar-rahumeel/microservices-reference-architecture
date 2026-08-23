import { Observable } from 'rxjs';

export interface EntityService<E, I> {
  get?(id: I): Observable<E>;

  insert(entity: E): Observable<E>;

  update(entity: E): Observable<E>;
}
