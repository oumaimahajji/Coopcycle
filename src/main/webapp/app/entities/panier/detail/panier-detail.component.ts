import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IPanier } from '../panier.model';

@Component({
  selector: 'jhi-panier-detail',
  templateUrl: './panier-detail.component.html',
})
export class PanierDetailComponent implements OnInit {
  panier: IPanier | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ panier }) => {
      this.panier = panier;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
