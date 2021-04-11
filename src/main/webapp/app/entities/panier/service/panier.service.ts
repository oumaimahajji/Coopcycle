import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPanier, getPanierIdentifier } from '../panier.model';

export type EntityResponseType = HttpResponse<IPanier>;
export type EntityArrayResponseType = HttpResponse<IPanier[]>;

@Injectable({ providedIn: 'root' })
export class PanierService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/paniers');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(panier: IPanier): Observable<EntityResponseType> {
    return this.http.post<IPanier>(this.resourceUrl, panier, { observe: 'response' });
  }

  update(panier: IPanier): Observable<EntityResponseType> {
    return this.http.put<IPanier>(`${this.resourceUrl}/${getPanierIdentifier(panier) as number}`, panier, { observe: 'response' });
  }

  partialUpdate(panier: IPanier): Observable<EntityResponseType> {
    return this.http.patch<IPanier>(`${this.resourceUrl}/${getPanierIdentifier(panier) as number}`, panier, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPanier>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPanier[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addPanierToCollectionIfMissing(panierCollection: IPanier[], ...paniersToCheck: (IPanier | null | undefined)[]): IPanier[] {
    const paniers: IPanier[] = paniersToCheck.filter(isPresent);
    if (paniers.length > 0) {
      const panierCollectionIdentifiers = panierCollection.map(panierItem => getPanierIdentifier(panierItem)!);
      const paniersToAdd = paniers.filter(panierItem => {
        const panierIdentifier = getPanierIdentifier(panierItem);
        if (panierIdentifier == null || panierCollectionIdentifiers.includes(panierIdentifier)) {
          return false;
        }
        panierCollectionIdentifiers.push(panierIdentifier);
        return true;
      });
      return [...paniersToAdd, ...panierCollection];
    }
    return panierCollection;
  }
}
