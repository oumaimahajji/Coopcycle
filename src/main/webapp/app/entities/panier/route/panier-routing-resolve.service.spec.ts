jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { IPanier, Panier } from '../panier.model';
import { PanierService } from '../service/panier.service';

import { PanierRoutingResolveService } from './panier-routing-resolve.service';

describe('Service Tests', () => {
  describe('Panier routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: PanierRoutingResolveService;
    let service: PanierService;
    let resultPanier: IPanier | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(PanierRoutingResolveService);
      service = TestBed.inject(PanierService);
      resultPanier = undefined;
    });

    describe('resolve', () => {
      it('should return IPanier returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultPanier = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultPanier).toEqual({ id: 123 });
      });

      it('should return new IPanier if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultPanier = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultPanier).toEqual(new Panier());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultPanier = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultPanier).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
