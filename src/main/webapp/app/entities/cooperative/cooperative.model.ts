import { ICompte } from 'app/entities/compte/compte.model';

export interface ICooperative {
  id?: number;
  cooperativeName?: string | null;
  cooperativeCity?: string | null;
  cooperativeAddress?: string | null;
  comptes?: ICompte[] | null;
}

export class Cooperative implements ICooperative {
  constructor(
    public id?: number,
    public cooperativeName?: string | null,
    public cooperativeCity?: string | null,
    public cooperativeAddress?: string | null,
    public comptes?: ICompte[] | null
  ) {}
}

export function getCooperativeIdentifier(cooperative: ICooperative): number | undefined {
  return cooperative.id;
}
