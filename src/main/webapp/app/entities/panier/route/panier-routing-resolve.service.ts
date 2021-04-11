import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPanier, Panier } from '../panier.model';
import { PanierService } from '../service/panier.service';

@Injectable({ providedIn: 'root' })
export class PanierRoutingResolveService implements Resolve<IPanier> {
  constructor(protected service: PanierService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IPanier> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((panier: HttpResponse<Panier>) => {
          if (panier.body) {
            return of(panier.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Panier());
  }
}
