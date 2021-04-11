import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { PanierComponent } from '../list/panier.component';
import { PanierDetailComponent } from '../detail/panier-detail.component';
import { PanierUpdateComponent } from '../update/panier-update.component';
import { PanierRoutingResolveService } from './panier-routing-resolve.service';

const panierRoute: Routes = [
  {
    path: '',
    component: PanierComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PanierDetailComponent,
    resolve: {
      panier: PanierRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PanierUpdateComponent,
    resolve: {
      panier: PanierRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PanierUpdateComponent,
    resolve: {
      panier: PanierRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(panierRoute)],
  exports: [RouterModule],
})
export class PanierRoutingModule {}
