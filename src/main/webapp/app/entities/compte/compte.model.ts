import { IPanier } from 'app/entities/panier/panier.model';
import { ICommande } from 'app/entities/commande/commande.model';
import { ICooperative } from 'app/entities/cooperative/cooperative.model';
import { Role } from 'app/entities/enumerations/role.model';

export interface ICompte {
  id?: number;
  name?: string;
  surname?: string;
  email?: string;
  categorie?: Role | null;
  phoneNumber?: string | null;
  address?: string | null;
  postalCode?: string | null;
  city?: string | null;
  paniers?: IPanier[] | null;
  commandes?: ICommande[] | null;
  memberOf?: ICooperative | null;
}

export class Compte implements ICompte {
  constructor(
    public id?: number,
    public name?: string,
    public surname?: string,
    public email?: string,
    public categorie?: Role | null,
    public phoneNumber?: string | null,
    public address?: string | null,
    public postalCode?: string | null,
    public city?: string | null,
    public paniers?: IPanier[] | null,
    public commandes?: ICommande[] | null,
    public memberOf?: ICooperative | null
  ) {}
}

export function getCompteIdentifier(compte: ICompte): number | undefined {
  return compte.id;
}
