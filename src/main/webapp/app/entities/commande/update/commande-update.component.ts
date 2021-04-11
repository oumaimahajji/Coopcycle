import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import * as dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { ICommande, Commande } from '../commande.model';
import { CommandeService } from '../service/commande.service';
import { IPanier } from 'app/entities/panier/panier.model';
import { PanierService } from 'app/entities/panier/service/panier.service';
import { IProduit } from 'app/entities/produit/produit.model';
import { ProduitService } from 'app/entities/produit/service/produit.service';
import { ICompte } from 'app/entities/compte/compte.model';
import { CompteService } from 'app/entities/compte/service/compte.service';

@Component({
  selector: 'jhi-commande-update',
  templateUrl: './commande-update.component.html',
})
export class CommandeUpdateComponent implements OnInit {
  isSaving = false;

  pricesCollection: IPanier[] = [];
  produitsSharedCollection: IProduit[] = [];
  comptesSharedCollection: ICompte[] = [];

  editForm = this.fb.group({
    id: [],
    created: [null, [Validators.required]],
    state: [],
    timeStarted: [],
    timeEnded: [],
    price: [],
    produits: [],
    deliveredBy: [],
  });

  constructor(
    protected commandeService: CommandeService,
    protected panierService: PanierService,
    protected produitService: ProduitService,
    protected compteService: CompteService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ commande }) => {
      if (commande.id === undefined) {
        const today = dayjs().startOf('day');
        commande.created = today;
        commande.timeStarted = today;
        commande.timeEnded = today;
      }

      this.updateForm(commande);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const commande = this.createFromForm();
    if (commande.id !== undefined) {
      this.subscribeToSaveResponse(this.commandeService.update(commande));
    } else {
      this.subscribeToSaveResponse(this.commandeService.create(commande));
    }
  }

  trackPanierById(index: number, item: IPanier): number {
    return item.id!;
  }

  trackProduitById(index: number, item: IProduit): number {
    return item.id!;
  }

  trackCompteById(index: number, item: ICompte): number {
    return item.id!;
  }

  getSelectedProduit(option: IProduit, selectedVals?: IProduit[]): IProduit {
    if (selectedVals) {
      for (const selectedVal of selectedVals) {
        if (option.id === selectedVal.id) {
          return selectedVal;
        }
      }
    }
    return option;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICommande>>): void {
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

  protected updateForm(commande: ICommande): void {
    this.editForm.patchValue({
      id: commande.id,
      created: commande.created ? commande.created.format(DATE_TIME_FORMAT) : null,
      state: commande.state,
      timeStarted: commande.timeStarted ? commande.timeStarted.format(DATE_TIME_FORMAT) : null,
      timeEnded: commande.timeEnded ? commande.timeEnded.format(DATE_TIME_FORMAT) : null,
      price: commande.price,
      produits: commande.produits,
      deliveredBy: commande.deliveredBy,
    });

    this.pricesCollection = this.panierService.addPanierToCollectionIfMissing(this.pricesCollection, commande.price);
    this.produitsSharedCollection = this.produitService.addProduitToCollectionIfMissing(
      this.produitsSharedCollection,
      ...(commande.produits ?? [])
    );
    this.comptesSharedCollection = this.compteService.addCompteToCollectionIfMissing(this.comptesSharedCollection, commande.deliveredBy);
  }

  protected loadRelationshipsOptions(): void {
    this.panierService
      .query({ 'commandeId.specified': 'false' })
      .pipe(map((res: HttpResponse<IPanier[]>) => res.body ?? []))
      .pipe(map((paniers: IPanier[]) => this.panierService.addPanierToCollectionIfMissing(paniers, this.editForm.get('price')!.value)))
      .subscribe((paniers: IPanier[]) => (this.pricesCollection = paniers));

    this.produitService
      .query()
      .pipe(map((res: HttpResponse<IProduit[]>) => res.body ?? []))
      .pipe(
        map((produits: IProduit[]) =>
          this.produitService.addProduitToCollectionIfMissing(produits, ...(this.editForm.get('produits')!.value ?? []))
        )
      )
      .subscribe((produits: IProduit[]) => (this.produitsSharedCollection = produits));

    this.compteService
      .query()
      .pipe(map((res: HttpResponse<ICompte[]>) => res.body ?? []))
      .pipe(
        map((comptes: ICompte[]) => this.compteService.addCompteToCollectionIfMissing(comptes, this.editForm.get('deliveredBy')!.value))
      )
      .subscribe((comptes: ICompte[]) => (this.comptesSharedCollection = comptes));
  }

  protected createFromForm(): ICommande {
    return {
      ...new Commande(),
      id: this.editForm.get(['id'])!.value,
      created: this.editForm.get(['created'])!.value ? dayjs(this.editForm.get(['created'])!.value, DATE_TIME_FORMAT) : undefined,
      state: this.editForm.get(['state'])!.value,
      timeStarted: this.editForm.get(['timeStarted'])!.value
        ? dayjs(this.editForm.get(['timeStarted'])!.value, DATE_TIME_FORMAT)
        : undefined,
      timeEnded: this.editForm.get(['timeEnded'])!.value ? dayjs(this.editForm.get(['timeEnded'])!.value, DATE_TIME_FORMAT) : undefined,
      price: this.editForm.get(['price'])!.value,
      produits: this.editForm.get(['produits'])!.value,
      deliveredBy: this.editForm.get(['deliveredBy'])!.value,
    };
  }
}
