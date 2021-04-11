import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISystemePaiement, SystemePaiement } from '../systeme-paiement.model';
import { SystemePaiementService } from '../service/systeme-paiement.service';

@Injectable({ providedIn: 'root' })
export class SystemePaiementRoutingResolveService implements Resolve<ISystemePaiement> {
  constructor(protected service: SystemePaiementService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ISystemePaiement> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((systemePaiement: HttpResponse<SystemePaiement>) => {
          if (systemePaiement.body) {
            return of(systemePaiement.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new SystemePaiement());
  }
}
