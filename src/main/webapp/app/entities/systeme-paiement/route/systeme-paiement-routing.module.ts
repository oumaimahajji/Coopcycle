import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { SystemePaiementComponent } from '../list/systeme-paiement.component';
import { SystemePaiementDetailComponent } from '../detail/systeme-paiement-detail.component';
import { SystemePaiementUpdateComponent } from '../update/systeme-paiement-update.component';
import { SystemePaiementRoutingResolveService } from './systeme-paiement-routing-resolve.service';

const systemePaiementRoute: Routes = [
  {
    path: '',
    component: SystemePaiementComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SystemePaiementDetailComponent,
    resolve: {
      systemePaiement: SystemePaiementRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SystemePaiementUpdateComponent,
    resolve: {
      systemePaiement: SystemePaiementRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SystemePaiementUpdateComponent,
    resolve: {
      systemePaiement: SystemePaiementRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(systemePaiementRoute)],
  exports: [RouterModule],
})
export class SystemePaiementRoutingModule {}
