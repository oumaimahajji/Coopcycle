import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICompte, getCompteIdentifier } from '../compte.model';

export type EntityResponseType = HttpResponse<ICompte>;
export type EntityArrayResponseType = HttpResponse<ICompte[]>;

@Injectable({ providedIn: 'root' })
export class CompteService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/comptes');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(compte: ICompte): Observable<EntityResponseType> {
    return this.http.post<ICompte>(this.resourceUrl, compte, { observe: 'response' });
  }

  update(compte: ICompte): Observable<EntityResponseType> {
    return this.http.put<ICompte>(`${this.resourceUrl}/${getCompteIdentifier(compte) as number}`, compte, { observe: 'response' });
  }

  partialUpdate(compte: ICompte): Observable<EntityResponseType> {
    return this.http.patch<ICompte>(`${this.resourceUrl}/${getCompteIdentifier(compte) as number}`, compte, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICompte>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICompte[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addCompteToCollectionIfMissing(compteCollection: ICompte[], ...comptesToCheck: (ICompte | null | undefined)[]): ICompte[] {
    const comptes: ICompte[] = comptesToCheck.filter(isPresent);
    if (comptes.length > 0) {
      const compteCollectionIdentifiers = compteCollection.map(compteItem => getCompteIdentifier(compteItem)!);
      const comptesToAdd = comptes.filter(compteItem => {
        const compteIdentifier = getCompteIdentifier(compteItem);
        if (compteIdentifier == null || compteCollectionIdentifiers.includes(compteIdentifier)) {
          return false;
        }
        compteCollectionIdentifiers.push(compteIdentifier);
        return true;
      });
      return [...comptesToAdd, ...compteCollection];
    }
    return compteCollection;
  }
}
