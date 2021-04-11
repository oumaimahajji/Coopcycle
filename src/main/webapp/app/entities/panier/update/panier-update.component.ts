import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IPanier, Panier } from '../panier.model';
import { PanierService } from '../service/panier.service';
import { ICompte } from 'app/entities/compte/compte.model';
import { CompteService } from 'app/entities/compte/service/compte.service';
import { ISystemePaiement } from 'app/entities/systeme-paiement/systeme-paiement.model';
import { SystemePaiementService } from 'app/entities/systeme-paiement/service/systeme-paiement.service';

@Component({
  selector: 'jhi-panier-update',
  templateUrl: './panier-update.component.html',
})
export class PanierUpdateComponent implements OnInit {
  isSaving = false;

  comptesSharedCollection: ICompte[] = [];
  systemePaiementsSharedCollection: ISystemePaiement[] = [];

  editForm = this.fb.group({
    id: [],
    price: [],
    madeBy: [],
    paidBy: [],
  });

  constructor(
    protected panierService: PanierService,
    protected compteService: CompteService,
    protected systemePaiementService: SystemePaiementService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ panier }) => {
      this.updateForm(panier);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const panier = this.createFromForm();
    if (panier.id !== undefined) {
      this.subscribeToSaveResponse(this.panierService.update(panier));
    } else {
      this.subscribeToSaveResponse(this.panierService.create(panier));
    }
  }

  trackCompteById(index: number, item: ICompte): number {
    return item.id!;
  }

  trackSystemePaiementById(index: number, item: ISystemePaiement): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPanier>>): void {
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

  protected updateForm(panier: IPanier): void {
    this.editForm.patchValue({
      id: panier.id,
      price: panier.price,
      madeBy: panier.madeBy,
      paidBy: panier.paidBy,
    });

    this.comptesSharedCollection = this.compteService.addCompteToCollectionIfMissing(this.comptesSharedCollection, panier.madeBy);
    this.systemePaiementsSharedCollection = this.systemePaiementService.addSystemePaiementToCollectionIfMissing(
      this.systemePaiementsSharedCollection,
      panier.paidBy
    );
  }

  protected loadRelationshipsOptions(): void {
    this.compteService
      .query()
      .pipe(map((res: HttpResponse<ICompte[]>) => res.body ?? []))
      .pipe(map((comptes: ICompte[]) => this.compteService.addCompteToCollectionIfMissing(comptes, this.editForm.get('madeBy')!.value)))
      .subscribe((comptes: ICompte[]) => (this.comptesSharedCollection = comptes));

    this.systemePaiementService
      .query()
      .pipe(map((res: HttpResponse<ISystemePaiement[]>) => res.body ?? []))
      .pipe(
        map((systemePaiements: ISystemePaiement[]) =>
          this.systemePaiementService.addSystemePaiementToCollectionIfMissing(systemePaiements, this.editForm.get('paidBy')!.value)
        )
      )
      .subscribe((systemePaiements: ISystemePaiement[]) => (this.systemePaiementsSharedCollection = systemePaiements));
  }

  protected createFromForm(): IPanier {
    return {
      ...new Panier(),
      id: this.editForm.get(['id'])!.value,
      price: this.editForm.get(['price'])!.value,
      madeBy: this.editForm.get(['madeBy'])!.value,
      paidBy: this.editForm.get(['paidBy'])!.value,
    };
  }
}
