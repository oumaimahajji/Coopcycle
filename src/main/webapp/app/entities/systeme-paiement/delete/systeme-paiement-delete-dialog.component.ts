import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ISystemePaiement } from '../systeme-paiement.model';
import { SystemePaiementService } from '../service/systeme-paiement.service';

@Component({
  templateUrl: './systeme-paiement-delete-dialog.component.html',
})
export class SystemePaiementDeleteDialogComponent {
  systemePaiement?: ISystemePaiement;

  constructor(protected systemePaiementService: SystemePaiementService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.systemePaiementService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
