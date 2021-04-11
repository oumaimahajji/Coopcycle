jest.mock('@angular/router');

import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { ISystemePaiement, SystemePaiement } from '../systeme-paiement.model';
import { SystemePaiementService } from '../service/systeme-paiement.service';

import { SystemePaiementRoutingResolveService } from './systeme-paiement-routing-resolve.service';

describe('Service Tests', () => {
  describe('SystemePaiement routing resolve service', () => {
    let mockRouter: Router;
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let routingResolveService: SystemePaiementRoutingResolveService;
    let service: SystemePaiementService;
    let resultSystemePaiement: ISystemePaiement | undefined;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [Router, ActivatedRouteSnapshot],
      });
      mockRouter = TestBed.inject(Router);
      mockActivatedRouteSnapshot = TestBed.inject(ActivatedRouteSnapshot);
      routingResolveService = TestBed.inject(SystemePaiementRoutingResolveService);
      service = TestBed.inject(SystemePaiementService);
      resultSystemePaiement = undefined;
    });

    describe('resolve', () => {
      it('should return ISystemePaiement returned by find', () => {
        // GIVEN
        service.find = jest.fn(id => of(new HttpResponse({ body: { id } })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSystemePaiement = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSystemePaiement).toEqual({ id: 123 });
      });

      it('should return new ISystemePaiement if id is not provided', () => {
        // GIVEN
        service.find = jest.fn();
        mockActivatedRouteSnapshot.params = {};

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSystemePaiement = result;
        });

        // THEN
        expect(service.find).not.toBeCalled();
        expect(resultSystemePaiement).toEqual(new SystemePaiement());
      });

      it('should route to 404 page if data not found in server', () => {
        // GIVEN
        spyOn(service, 'find').and.returnValue(of(new HttpResponse({ body: null })));
        mockActivatedRouteSnapshot.params = { id: 123 };

        // WHEN
        routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
          resultSystemePaiement = result;
        });

        // THEN
        expect(service.find).toBeCalledWith(123);
        expect(resultSystemePaiement).toEqual(undefined);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
      });
    });
  });
});
