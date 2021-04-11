import * as dayjs from 'dayjs';
import { IPanier } from 'app/entities/panier/panier.model';
import { IProduit } from 'app/entities/produit/produit.model';
import { ICompte } from 'app/entities/compte/compte.model';
import { EtatCommande } from 'app/entities/enumerations/etat-commande.model';

export interface ICommande {
  id?: number;
  created?: dayjs.Dayjs;
  state?: EtatCommande | null;
  timeStarted?: dayjs.Dayjs | null;
  timeEnded?: dayjs.Dayjs | null;
  price?: IPanier | null;
  produits?: IProduit[] | null;
  deliveredBy?: ICompte | null;
}

export class Commande implements ICommande {
  constructor(
    public id?: number,
    public created?: dayjs.Dayjs,
    public state?: EtatCommande | null,
    public timeStarted?: dayjs.Dayjs | null,
    public timeEnded?: dayjs.Dayjs | null,
    public price?: IPanier | null,
    public produits?: IProduit[] | null,
    public deliveredBy?: ICompte | null
  ) {}
}

export function getCommandeIdentifier(commande: ICommande): number | undefined {
  return commande.id;
}
