import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { PanierDetailComponent } from './panier-detail.component';

describe('Component Tests', () => {
  describe('Panier Management Detail Component', () => {
    let comp: PanierDetailComponent;
    let fixture: ComponentFixture<PanierDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [PanierDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ panier: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(PanierDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(PanierDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load panier on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.panier).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
