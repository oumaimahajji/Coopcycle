import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISystemePaiement } from '../systeme-paiement.model';

@Component({
  selector: 'jhi-systeme-paiement-detail',
  templateUrl: './systeme-paiement-detail.component.html',
})
export class SystemePaiementDetailComponent implements OnInit {
  systemePaiement: ISystemePaiement | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ systemePaiement }) => {
      this.systemePaiement = systemePaiement;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
