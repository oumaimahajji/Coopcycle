import { IRestaurant } from 'app/entities/restaurant/restaurant.model';
import { ICommande } from 'app/entities/commande/commande.model';

export interface IProduit {
  id?: number;
  productName?: string | null;
  description?: string | null;
  productPrice?: string;
  imageContentType?: string | null;
  image?: string | null;
  restaurants?: IRestaurant[] | null;
  commandes?: ICommande[] | null;
}

export class Produit implements IProduit {
  constructor(
    public id?: number,
    public productName?: string | null,
    public description?: string | null,
    public productPrice?: string,
    public imageContentType?: string | null,
    public image?: string | null,
    public restaurants?: IRestaurant[] | null,
    public commandes?: ICommande[] | null
  ) {}
}

export function getProduitIdentifier(produit: IProduit): number | undefined {
  return produit.id;
}
