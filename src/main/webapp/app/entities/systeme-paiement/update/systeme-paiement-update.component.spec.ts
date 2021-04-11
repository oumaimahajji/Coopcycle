jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { SystemePaiementService } from '../service/systeme-paiement.service';
import { ISystemePaiement, SystemePaiement } from '../systeme-paiement.model';

import { SystemePaiementUpdateComponent } from './systeme-paiement-update.component';

describe('Component Tests', () => {
  describe('SystemePaiement Management Update Component', () => {
    let comp: SystemePaiementUpdateComponent;
    let fixture: ComponentFixture<SystemePaiementUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let systemePaiementService: SystemePaiementService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [SystemePaiementUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(SystemePaiementUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(SystemePaiementUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      systemePaiementService = TestBed.inject(SystemePaiementService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const systemePaiement: ISystemePaiement = { id: 456 };

        activatedRoute.data = of({ systemePaiement });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(systemePaiement));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const systemePaiement = { id: 123 };
        spyOn(systemePaiementService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ systemePaiement });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: systemePaiement }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(systemePaiementService.update).toHaveBeenCalledWith(systemePaiement);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const systemePaiement = new SystemePaiement();
        spyOn(systemePaiementService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ systemePaiement });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: systemePaiement }));
        saveSubject.complete();

        // THEN
        expect(systemePaiementService.create).toHaveBeenCalledWith(systemePaiement);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const systemePaiement = { id: 123 };
        spyOn(systemePaiementService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ systemePaiement });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(systemePaiementService.update).toHaveBeenCalledWith(systemePaiement);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
