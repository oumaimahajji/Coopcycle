import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dayjs from 'dayjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISystemePaiement, getSystemePaiementIdentifier } from '../systeme-paiement.model';

export type EntityResponseType = HttpResponse<ISystemePaiement>;
export type EntityArrayResponseType = HttpResponse<ISystemePaiement[]>;

@Injectable({ providedIn: 'root' })
export class SystemePaiementService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/systeme-paiements');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(systemePaiement: ISystemePaiement): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(systemePaiement);
    return this.http
      .post<ISystemePaiement>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(systemePaiement: ISystemePaiement): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(systemePaiement);
    return this.http
      .put<ISystemePaiement>(`${this.resourceUrl}/${getSystemePaiementIdentifier(systemePaiement) as number}`, copy, {
        observe: 'response',
      })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(systemePaiement: ISystemePaiement): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(systemePaiement);
    return this.http
      .patch<ISystemePaiement>(`${this.resourceUrl}/${getSystemePaiementIdentifier(systemePaiement) as number}`, copy, {
        observe: 'response',
      })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ISystemePaiement>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ISystemePaiement[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addSystemePaiementToCollectionIfMissing(
    systemePaiementCollection: ISystemePaiement[],
    ...systemePaiementsToCheck: (ISystemePaiement | null | undefined)[]
  ): ISystemePaiement[] {
    const systemePaiements: ISystemePaiement[] = systemePaiementsToCheck.filter(isPresent);
    if (systemePaiements.length > 0) {
      const systemePaiementCollectionIdentifiers = systemePaiementCollection.map(
        systemePaiementItem => getSystemePaiementIdentifier(systemePaiementItem)!
      );
      const systemePaiementsToAdd = systemePaiements.filter(systemePaiementItem => {
        const systemePaiementIdentifier = getSystemePaiementIdentifier(systemePaiementItem);
        if (systemePaiementIdentifier == null || systemePaiementCollectionIdentifiers.includes(systemePaiementIdentifier)) {
          return false;
        }
        systemePaiementCollectionIdentifiers.push(systemePaiementIdentifier);
        return true;
      });
      return [...systemePaiementsToAdd, ...systemePaiementCollection];
    }
    return systemePaiementCollection;
  }

  protected convertDateFromClient(systemePaiement: ISystemePaiement): ISystemePaiement {
    return Object.assign({}, systemePaiement, {
      experationDate: systemePaiement.experationDate?.isValid() ? systemePaiement.experationDate.toJSON() : undefined,
      billDate: systemePaiement.billDate?.isValid() ? systemePaiement.billDate.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.experationDate = res.body.experationDate ? dayjs(res.body.experationDate) : undefined;
      res.body.billDate = res.body.billDate ? dayjs(res.body.billDate) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((systemePaiement: ISystemePaiement) => {
        systemePaiement.experationDate = systemePaiement.experationDate ? dayjs(systemePaiement.experationDate) : undefined;
        systemePaiement.billDate = systemePaiement.billDate ? dayjs(systemePaiement.billDate) : undefined;
      });
    }
    return res;
  }
}
