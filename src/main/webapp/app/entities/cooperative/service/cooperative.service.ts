import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICooperative, getCooperativeIdentifier } from '../cooperative.model';

export type EntityResponseType = HttpResponse<ICooperative>;
export type EntityArrayResponseType = HttpResponse<ICooperative[]>;

@Injectable({ providedIn: 'root' })
export class CooperativeService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/cooperatives');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(cooperative: ICooperative): Observable<EntityResponseType> {
    return this.http.post<ICooperative>(this.resourceUrl, cooperative, { observe: 'response' });
  }

  update(cooperative: ICooperative): Observable<EntityResponseType> {
    return this.http.put<ICooperative>(`${this.resourceUrl}/${getCooperativeIdentifier(cooperative) as number}`, cooperative, {
      observe: 'response',
    });
  }

  partialUpdate(cooperative: ICooperative): Observable<EntityResponseType> {
    return this.http.patch<ICooperative>(`${this.resourceUrl}/${getCooperativeIdentifier(cooperative) as number}`, cooperative, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICooperative>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICooperative[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addCooperativeToCollectionIfMissing(
    cooperativeCollection: ICooperative[],
    ...cooperativesToCheck: (ICooperative | null | undefined)[]
  ): ICooperative[] {
    const cooperatives: ICooperative[] = cooperativesToCheck.filter(isPresent);
    if (cooperatives.length > 0) {
      const cooperativeCollectionIdentifiers = cooperativeCollection.map(cooperativeItem => getCooperativeIdentifier(cooperativeItem)!);
      const cooperativesToAdd = cooperatives.filter(cooperativeItem => {
        const cooperativeIdentifier = getCooperativeIdentifier(cooperativeItem);
        if (cooperativeIdentifier == null || cooperativeCollectionIdentifiers.includes(cooperativeIdentifier)) {
          return false;
        }
        cooperativeCollectionIdentifiers.push(cooperativeIdentifier);
        return true;
      });
      return [...cooperativesToAdd, ...cooperativeCollection];
    }
    return cooperativeCollection;
  }
}
