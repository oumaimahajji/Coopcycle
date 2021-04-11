import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { CooperativeDetailComponent } from './cooperative-detail.component';

describe('Component Tests', () => {
  describe('Cooperative Management Detail Component', () => {
    let comp: CooperativeDetailComponent;
    let fixture: ComponentFixture<CooperativeDetailComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [CooperativeDetailComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: { data: of({ cooperative: { id: 123 } }) },
          },
        ],
      })
        .overrideTemplate(CooperativeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CooperativeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load cooperative on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.cooperative).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
