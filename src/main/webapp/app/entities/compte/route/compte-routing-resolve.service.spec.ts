jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { ICompte, Compte } from '../compte.model';
import { CompteService } from '../service/compte.service';

import { CompteRoutingResolveService } from './compte-routing-resolve.service';

describe('Service Tests', () => {
  describe('Compte routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: CompteRoutingResolveService;
    let service: CompteService;
    let resultCompte: ICompte | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(CompteRoutingResolveService);
      service = TestBed.inject(CompteService);
      resultCompte = undefined;
    });

    describe('resolve', () => {
      it('should return ICompte returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCompte = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultCompte).toEqual({ id: 123 });
      });

      it('should return new ICompte if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCompte = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultCompte).toEqual(new Compte());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultCompte = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultCompte).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
