import * as dayjs from 'dayjs';
import { IPanier } from 'app/entities/panier/panier.model';

export interface ISystemePaiement {
  id?: number;
  creditCard?: string | null;
  experationDate?: dayjs.Dayjs;
  typeCard?: string | null;
  amount?: string | null;
  billDate?: dayjs.Dayjs;
  paniers?: IPanier[] | null;
}

export class SystemePaiement implements ISystemePaiement {
  constructor(
    public id?: number,
    public creditCard?: string | null,
    public experationDate?: dayjs.Dayjs,
    public typeCard?: string | null,
    public amount?: string | null,
    public billDate?: dayjs.Dayjs,
    public paniers?: IPanier[] | null
  ) {}
}

export function getSystemePaiementIdentifier(systemePaiement: ISystemePaiement): number | undefined {
  return systemePaiement.id;
}
