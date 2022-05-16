import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ITypePlat } from '../type-plat.model';
import { TypePlatService } from '../service/type-plat.service';

@Component({
  templateUrl: './type-plat-delete-dialog.component.html',
})
export class TypePlatDeleteDialogComponent {
  typePlat?: ITypePlat;

  constructor(protected typePlatService: TypePlatService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.typePlatService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
