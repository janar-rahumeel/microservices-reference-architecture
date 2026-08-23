import { EMPTY, Observable, switchMap, take } from 'rxjs';
import { AfterViewInit, DestroyRef, Directive, effect, inject, OnInit, signal, Signal, viewChild, WritableSignal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatSort, Sort } from '@angular/material/sort';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { ListDataSource } from '@shared/data-access/list.data-source';
import { SearchService } from '@shared/data-access/search-service.model';

@Directive()
export abstract class AbstractSimpleListDirective<S extends SearchService<F, E>, F, E>
  implements OnInit, AfterViewInit, SimpleListComponent<F>
{
  protected viewSort: Signal<MatSort | undefined> = viewChild(MatSort);
  protected viewPaginator: Signal<MatPaginator | undefined> = viewChild(MatPaginator);
  protected filter: WritableSignal<F | undefined> = signal(undefined);
  protected listDataSource: ListDataSource<S, F, E>;
  protected displayedColumns: string[];

  private readonly destroyReference: DestroyRef = inject(DestroyRef);
  private readonly sort: Signal<Sort | undefined>;
  private readonly page: Signal<PageEvent | undefined>;
  private readonly totalItemCount: Signal<number>;

  protected constructor(listDataSource: ListDataSource<S, F, E>, displayedColumns: string[]) {
    this.listDataSource = listDataSource;
    this.displayedColumns = displayedColumns;

    this.sort = toSignal(
      toObservable(this.viewSort).pipe(
        switchMap((sort: MatSort | undefined): Observable<Sort | undefined> => (sort ? sort.sortChange : EMPTY)),
      ),
      { initialValue: undefined },
    );
    this.page = toSignal(
      toObservable(this.viewPaginator).pipe(
        switchMap((paginator: MatPaginator | undefined): Observable<PageEvent | undefined> => (paginator ? paginator.page : EMPTY)),
      ),
      { initialValue: undefined },
    );
    this.totalItemCount = toSignal(this.listDataSource.totalElementCountSubject.asObservable(), { initialValue: 0 });

    effect((): void => {
      if (this.filter()) {
        const viewPaginator: MatPaginator | undefined = this.viewPaginator();
        if (viewPaginator) {
          viewPaginator.pageIndex = 0;
        }
        this.loadList();
      }
    });

    effect((): void => {
      if (this.sort()) {
        const viewPaginator: MatPaginator | undefined = this.viewPaginator();
        if (viewPaginator) {
          viewPaginator.pageIndex = 0;
        }
        this.loadList();
      }
    });

    effect((): void => {
      if (this.page()) {
        this.loadList();
      }
    });

    effect((): void => {
      const totalItemCount: number = this.totalItemCount();
      const viewPaginator: MatPaginator | undefined = this.viewPaginator();
      if (viewPaginator) {
        viewPaginator.length = totalItemCount;
      }
    });
  }

  public abstract getListTitle(): string;

  public ngOnInit(): void {
    this.onInit();
  }

  protected onInit(): void {
    // Do nothing
  }

  public ngAfterViewInit(): void {
    this.onAfterViewInit();
  }

  protected onAfterViewInit(): void {
    // Do nothing
  }

  public getDisplayWidth(): number {
    return 700; // Default
  }

  public setFilter(filter: F): void {
    setTimeout((): void => {
      this.filter.set(filter); // TODO Sometimes paginator is loaded too late
    });
  }

  protected loadList(): void {
    this.listDataSource.load(
      this.filter()!,
      this.viewSort()?.active ?? '',
      this.viewSort()?.direction ?? '',
      this.viewPaginator()?.pageIndex ?? 0,
      this.viewPaginator()?.pageSize ?? 0,
    );
  }

  protected subscribe<T>(observable: Observable<T>, callback: (context: T) => void): void {
    observable.pipe(takeUntilDestroyed(this.destroyReference)).subscribe(callback);
  }

  protected subscribeOnce<T>(observable: Observable<T>, callback: (context: T) => void): void {
    observable.pipe(take(1)).subscribe(callback);
  }
}

export interface SimpleListComponent<F> {
  getListTitle(): string;

  getDisplayWidth(): number;

  setFilter(filter: F): void;
}
