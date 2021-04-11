jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { CooperativeService } from '../service/cooperative.service';
import { ICooperative, Cooperative } from '../cooperative.model';

import { CooperativeUpdateComponent } from './cooperative-update.component';

describe('Component Tests', () => {
  describe('Cooperative Management Update Component', () => {
    let comp: CooperativeUpdateComponent;
    let fixture: ComponentFixture<CooperativeUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let cooperativeService: CooperativeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [CooperativeUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(CooperativeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CooperativeUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      cooperativeService = TestBed.inject(CooperativeService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should update editForm', () => {
        const cooperative: ICooperative = { id: 456 };

        activatedRoute.data = of({ cooperative });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(cooperative));
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const cooperative = { id: 123 };
        spyOn(cooperativeService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ cooperative });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: cooperative }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(cooperativeService.update).toHaveBeenCalledWith(cooperative);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const cooperative = new Cooperative();
        spyOn(cooperativeService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ cooperative });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: cooperative }));
        saveSubject.complete();

        // THEN
        expect(cooperativeService.create).toHaveBeenCalledWith(cooperative);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const cooperative = { id: 123 };
        spyOn(cooperativeService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ cooperative });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(cooperativeService.update).toHaveBeenCalledWith(cooperative);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });
  });
});
