import { IProduit } from 'app/entities/produit/produit.model';

export interface IRestaurant {
  id?: number;
  restaurantName?: string | null;
  deliveryPrice?: string | null;
  restaurantAddress?: string | null;
  restaurantCity?: string | null;
  proposes?: IProduit[] | null;
}

export class Restaurant implements IRestaurant {
  constructor(
    public id?: number,
    public restaurantName?: string | null,
    public deliveryPrice?: string | null,
    public restaurantAddress?: string | null,
    public restaurantCity?: string | null,
    public proposes?: IProduit[] | null
  ) {}
}

export function getRestaurantIdentifier(restaurant: IRestaurant): number | undefined {
  return restaurant.id;
}
