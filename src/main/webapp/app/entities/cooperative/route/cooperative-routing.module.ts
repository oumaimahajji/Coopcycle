import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { CooperativeComponent } from '../list/cooperative.component';
import { CooperativeDetailComponent } from '../detail/cooperative-detail.component';
import { CooperativeUpdateComponent } from '../update/cooperative-update.component';
import { CooperativeRoutingResolveService } from './cooperative-routing-resolve.service';

const cooperativeRoute: Routes = [
  {
    path: '',
    component: CooperativeComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CooperativeDetailComponent,
    resolve: {
      cooperative: CooperativeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CooperativeUpdateComponent,
    resolve: {
      cooperative: CooperativeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CooperativeUpdateComponent,
    resolve: {
      cooperative: CooperativeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(cooperativeRoute)],
  exports: [RouterModule],
})
export class CooperativeRoutingModule {}
