import { ChangeDetectionStrategy, Component, inject, Signal, viewChild } from '@angular/core';
import { MatInput, MatInputModule } from '@angular/material/input';
import { map, Observable, take } from 'rxjs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { AbstractSimpleListDirective } from '@shared/list/abstract-simple-list.directive';
import { ListDataSource } from '@shared/data-access/list.data-source';
import { DialogService } from '@shared/dialog/dialog.service';
import { OnSuccessfulSubmit } from '@shared/models/submit-result.model';
import { CustomerService } from '../../data-access/customer.service';
import { CustomerFilter, CustomerListElement } from '../../models/customer.model';
import { CustomerFormComponent } from '../../components/customer-form/customer-form.component';

@Component({
  selector: 'mra-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
    MatToolbarModule,
    MatTooltipModule,
  ],
  providers: [CustomerService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerListComponent extends AbstractSimpleListDirective<CustomerService, CustomerFilter, CustomerListElement> {
  private readonly customerService: CustomerService = inject(CustomerService);
  private readonly dialogService: DialogService = inject(DialogService);
  private readonly partialNameMatInput: Signal<MatInput> = viewChild.required('partialNameMatInput', { read: MatInput });
  private readonly partialCodeMatInput: Signal<MatInput> = viewChild.required('partialCodeMatInput', { read: MatInput });

  public constructor() {
    super(new ListDataSource(inject(CustomerService)), ['type', 'name', 'code', 'actions']);
  }

  public override getListTitle(): string {
    return 'Customers';
  }

  protected onSearchButtonClicked(): void {
    this.setFilter({
      partialName: this.partialNameMatInput().value,
      partialCode: this.partialCodeMatInput().value,
    });
  }

  protected onCreateButtonClicked(): void {
    this.subscribeOnce(this.dialogService.openCreateEntityDialog(CustomerFormComponent).afterClosed(), (): void => this.loadList());
  }

  protected onDeleteButtonClicked(customerSearchElement: CustomerListElement): void {
    const onSuccessfulSubmit: OnSuccessfulSubmit = {
      feedbackMessage: 'Deleted',
      closeContainer: true,
    };
    const action: Observable<OnSuccessfulSubmit> = this.customerService.delete(customerSearchElement).pipe(
      take(1),
      map((): OnSuccessfulSubmit => onSuccessfulSubmit),
    );
    this.subscribeOnce(this.dialogService.openDeleteConfirmationDialog(action).afterClosed(), (): void => this.loadList());
  }
}
