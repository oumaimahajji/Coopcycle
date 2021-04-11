import { ICompte } from 'app/entities/compte/compte.model';
import { ISystemePaiement } from 'app/entities/systeme-paiement/systeme-paiement.model';

export interface IPanier {
  id?: number;
  price?: string | null;
  madeBy?: ICompte | null;
  paidBy?: ISystemePaiement | null;
}

export class Panier implements IPanier {
  constructor(public id?: number, public price?: string | null, public madeBy?: ICompte | null, public paidBy?: ISystemePaiement | null) {}
}

export function getPanierIdentifier(panier: IPanier): number | undefined {
  return panier.id;
}
