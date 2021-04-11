import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import * as dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { ISystemePaiement, SystemePaiement } from '../systeme-paiement.model';
import { SystemePaiementService } from '../service/systeme-paiement.service';

@Component({
  selector: 'jhi-systeme-paiement-update',
  templateUrl: './systeme-paiement-update.component.html',
})
export class SystemePaiementUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    creditCard: [null, [Validators.minLength(16), Validators.maxLength(16)]],
    experationDate: [null, [Validators.required]],
    typeCard: [null, [Validators.maxLength(20)]],
    amount: [],
    billDate: [null, [Validators.required]],
  });

  constructor(
    protected systemePaiementService: SystemePaiementService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ systemePaiement }) => {
      if (systemePaiement.id === undefined) {
        const today = dayjs().startOf('day');
        systemePaiement.experationDate = today;
        systemePaiement.billDate = today;
      }

      this.updateForm(systemePaiement);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const systemePaiement = this.createFromForm();
    if (systemePaiement.id !== undefined) {
      this.subscribeToSaveResponse(this.systemePaiementService.update(systemePaiement));
    } else {
      this.subscribeToSaveResponse(this.systemePaiementService.create(systemePaiement));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISystemePaiement>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(systemePaiement: ISystemePaiement): void {
    this.editForm.patchValue({
      id: systemePaiement.id,
      creditCard: systemePaiement.creditCard,
      experationDate: systemePaiement.experationDate ? systemePaiement.experationDate.format(DATE_TIME_FORMAT) : null,
      typeCard: systemePaiement.typeCard,
      amount: systemePaiement.amount,
      billDate: systemePaiement.billDate ? systemePaiement.billDate.format(DATE_TIME_FORMAT) : null,
    });
  }

  protected createFromForm(): ISystemePaiement {
    return {
      ...new SystemePaiement(),
      id: this.editForm.get(['id'])!.value,
      creditCard: this.editForm.get(['creditCard'])!.value,
      experationDate: this.editForm.get(['experationDate'])!.value
        ? dayjs(this.editForm.get(['experationDate'])!.value, DATE_TIME_FORMAT)
        : undefined,
      typeCard: this.editForm.get(['typeCard'])!.value,
      amount: this.editForm.get(['amount'])!.value,
      billDate: this.editForm.get(['billDate'])!.value ? dayjs(this.editForm.get(['billDate'])!.value, DATE_TIME_FORMAT) : undefined,
    };
  }
}
