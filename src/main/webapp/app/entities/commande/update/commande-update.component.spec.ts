jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { CommandeService } from '../service/commande.service';
import { ICommande, Commande } from '../commande.model';
import { IPanier } from 'app/entities/panier/panier.model';
import { PanierService } from 'app/entities/panier/service/panier.service';
import { IProduit } from 'app/entities/produit/produit.model';
import { ProduitService } from 'app/entities/produit/service/produit.service';
import { ICompte } from 'app/entities/compte/compte.model';
import { CompteService } from 'app/entities/compte/service/compte.service';

import { CommandeUpdateComponent } from './commande-update.component';

describe('Component Tests', () => {
  describe('Commande Management Update Component', () => {
    let comp: CommandeUpdateComponent;
    let fixture: ComponentFixture<CommandeUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let commandeService: CommandeService;
    let panierService: PanierService;
    let produitService: ProduitService;
    let compteService: CompteService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [CommandeUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(CommandeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CommandeUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      commandeService = TestBed.inject(CommandeService);
      panierService = TestBed.inject(PanierService);
      produitService = TestBed.inject(ProduitService);
      compteService = TestBed.inject(CompteService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should call price query and add missing value', () => {
        const commande: ICommande = { id: 456 };
        const price: IPanier = { id: 35418 };
        commande.price = price;

        const priceCollection: IPanier[] = [{ id: 72578 }];
        spyOn(panierService, 'query').and.returnValue(of(new HttpResponse({ body: priceCollection })));
        const expectedCollection: IPanier[] = [price, ...priceCollection];
        spyOn(panierService, 'addPanierToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        expect(panierService.query).toHaveBeenCalled();
        expect(panierService.addPanierToCollectionIfMissing).toHaveBeenCalledWith(priceCollection, price);
        expect(comp.pricesCollection).toEqual(expectedCollection);
      });

      it('Should call Produit query and add missing value', () => {
        const commande: ICommande = { id: 456 };
        const produits: IProduit[] = [{ id: 29683 }];
        commande.produits = produits;

        const produitCollection: IProduit[] = [{ id: 83813 }];
        spyOn(produitService, 'query').and.returnValue(of(new HttpResponse({ body: produitCollection })));
        const additionalProduits = [...produits];
        const expectedCollection: IProduit[] = [...additionalProduits, ...produitCollection];
        spyOn(produitService, 'addProduitToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        expect(produitService.query).toHaveBeenCalled();
        expect(produitService.addProduitToCollectionIfMissing).toHaveBeenCalledWith(produitCollection, ...additionalProduits);
        expect(comp.produitsSharedCollection).toEqual(expectedCollection);
      });

      it('Should call Compte query and add missing value', () => {
        const commande: ICommande = { id: 456 };
        const deliveredBy: ICompte = { id: 73060 };
        commande.deliveredBy = deliveredBy;

        const compteCollection: ICompte[] = [{ id: 59840 }];
        spyOn(compteService, 'query').and.returnValue(of(new HttpResponse({ body: compteCollection })));
        const additionalComptes = [deliveredBy];
        const expectedCollection: ICompte[] = [...additionalComptes, ...compteCollection];
        spyOn(compteService, 'addCompteToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        expect(compteService.query).toHaveBeenCalled();
        expect(compteService.addCompteToCollectionIfMissing).toHaveBeenCalledWith(compteCollection, ...additionalComptes);
        expect(comp.comptesSharedCollection).toEqual(expectedCollection);
      });

      it('Should update editForm', () => {
        const commande: ICommande = { id: 456 };
        const price: IPanier = { id: 77157 };
        commande.price = price;
        const produits: IProduit = { id: 97545 };
        commande.produits = [produits];
        const deliveredBy: ICompte = { id: 42697 };
        commande.deliveredBy = deliveredBy;

        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(commande));
        expect(comp.pricesCollection).toContain(price);
        expect(comp.produitsSharedCollection).toContain(produits);
        expect(comp.comptesSharedCollection).toContain(deliveredBy);
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const commande = { id: 123 };
        spyOn(commandeService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: commande }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(commandeService.update).toHaveBeenCalledWith(commande);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const commande = new Commande();
        spyOn(commandeService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: commande }));
        saveSubject.complete();

        // THEN
        expect(commandeService.create).toHaveBeenCalledWith(commande);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const commande = { id: 123 };
        spyOn(commandeService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ commande });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(commandeService.update).toHaveBeenCalledWith(commande);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });

    describe('Tracking relationships identifiers', () => {
      describe('trackPanierById', () => {
        it('Should return tracked Panier primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackPanierById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });

      describe('trackProduitById', () => {
        it('Should return tracked Produit primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackProduitById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });

      describe('trackCompteById', () => {
        it('Should return tracked Compte primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackCompteById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });
    });

    describe('Getting selected relationships', () => {
      describe('getSelectedProduit', () => {
        it('Should return option if no Produit is selected', () => {
          const option = { id: 123 };
          const result = comp.getSelectedProduit(option);
          expect(result === option).toEqual(true);
        });

        it('Should return selected Produit for according option', () => {
          const option = { id: 123 };
          const selected = { id: 123 };
          const selected2 = { id: 456 };
          const result = comp.getSelectedProduit(option, [selected2, selected]);
          expect(result === selected).toEqual(true);
          expect(result === selected2).toEqual(false);
          expect(result === option).toEqual(false);
        });

        it('Should return option if this Produit is not selected', () => {
          const option = { id: 123 };
          const selected = { id: 456 };
          const result = comp.getSelectedProduit(option, [selected]);
          expect(result === option).toEqual(true);
          expect(result === selected).toEqual(false);
        });
      });
    });
  });
});
