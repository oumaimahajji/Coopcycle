import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { SystemePaiementDetailComponent } from './systeme-paiement-detail.component';

describe('Component Tests', () => {
  describe('SystemePaiement Management Detail Component', () => {
    let comp: SystemePaiementDetailComponent;
    let fixture: ComponentFixture<SystemePaiementDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [SystemePaiementDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ systemePaiement: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(SystemePaiementDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(SystemePaiementDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load systemePaiement on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.systemePaiement).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
