import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    Signal,
    signal,
    viewChild,
    WritableSignal,
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelect, MatSelectChange, MatSelectModule } from '@angular/material/select';
import { AbstractEditEntityDirective } from '@shared/edit-entity/abstract-edit-entity.directive';
import { EditEntityStateService } from '@shared/edit-entity/edit-entity-state.service';
import { FieldErrorDirective } from '@shared/directives/field-error.directive';
import { CustomerService } from '../../data-access/customer.service';
import { Customer, CustomerType } from '../../models/customer.model';

@Component({
    selector: 'mra-customer-form',
    templateUrl: './customer-form.component.html',
    imports: [FieldErrorDirective, MatFormFieldModule, MatInputModule, MatSelectModule, ReactiveFormsModule],
    providers: [EditEntityStateService, CustomerService],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerFormComponent extends AbstractEditEntityDirective<CustomerService, Customer, string> implements AfterViewInit {
    protected readonly types: Signal<CustomerType[]> = signal([CustomerType.Person, CustomerType.LegalEntity]);
    protected readonly selectedType: WritableSignal<CustomerType> = signal(CustomerType.Person);
    protected readonly entityForm: Signal<FormGroup> = computed(() =>
        this.selectedType() === CustomerType.Person ? this.personForm : this.legalEntityForm,
    );

    private readonly typeMatSelect: Signal<MatSelect> = viewChild.required('typeMatSelect');
    private readonly personForm: FormGroup = new FormGroup({
        type: new FormControl(CustomerType.Person, Validators.required),
        firstName: new FormControl(undefined, Validators.required),
        lastName: new FormControl(undefined, Validators.required),
        personalIdentificationCode: new FormControl(undefined, Validators.required),
    });
    private readonly legalEntityForm: FormGroup = new FormGroup({
        type: new FormControl(CustomerType.LegalEntity, Validators.required),
        name: new FormControl(undefined, Validators.required),
        registrationCode: new FormControl(undefined, Validators.required),
    });

    public constructor() {
        super(inject(CustomerService));
    }

    public override getDisplayWidth(): number {
        return 300;
    }

    public ngAfterViewInit(): void {
        this.subscribe(this.typeMatSelect().selectionChange, (matSelectChange: MatSelectChange): void => {
            this.selectedType.set(matSelectChange.value as CustomerType);
        });
    }

    protected onInit(): void {
        // Do nothing
    }

    protected override getTitle(): string {
        return 'Customer';
    }

    protected isPersonFieldsVisible(): boolean {
        return this.selectedType() === CustomerType.Person;
    }

    protected isLegalEntityFieldsVisible(): boolean {
        return this.selectedType() === CustomerType.LegalEntity;
    }
}
