import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dayjs from 'dayjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICommande, getCommandeIdentifier } from '../commande.model';

export type EntityResponseType = HttpResponse<ICommande>;
export type EntityArrayResponseType = HttpResponse<ICommande[]>;

@Injectable({ providedIn: 'root' })
export class CommandeService {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/commandes');

  constructor(protected http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(commande: ICommande): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(commande);
    return this.http
      .post<ICommande>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(commande: ICommande): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(commande);
    return this.http
      .put<ICommande>(`${this.resourceUrl}/${getCommandeIdentifier(commande) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(commande: ICommande): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(commande);
    return this.http
      .patch<ICommande>(`${this.resourceUrl}/${getCommandeIdentifier(commande) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICommande>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICommande[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addCommandeToCollectionIfMissing(commandeCollection: ICommande[], ...commandesToCheck: (ICommande | null | undefined)[]): ICommande[] {
    const commandes: ICommande[] = commandesToCheck.filter(isPresent);
    if (commandes.length > 0) {
      const commandeCollectionIdentifiers = commandeCollection.map(commandeItem => getCommandeIdentifier(commandeItem)!);
      const commandesToAdd = commandes.filter(commandeItem => {
        const commandeIdentifier = getCommandeIdentifier(commandeItem);
        if (commandeIdentifier == null || commandeCollectionIdentifiers.includes(commandeIdentifier)) {
          return false;
        }
        commandeCollectionIdentifiers.push(commandeIdentifier);
        return true;
      });
      return [...commandesToAdd, ...commandeCollection];
    }
    return commandeCollection;
  }

  protected convertDateFromClient(commande: ICommande): ICommande {
    return Object.assign({}, commande, {
      created: commande.created?.isValid() ? commande.created.toJSON() : undefined,
      timeStarted: commande.timeStarted?.isValid() ? commande.timeStarted.toJSON() : undefined,
      timeEnded: commande.timeEnded?.isValid() ? commande.timeEnded.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.created = res.body.created ? dayjs(res.body.created) : undefined;
      res.body.timeStarted = res.body.timeStarted ? dayjs(res.body.timeStarted) : undefined;
      res.body.timeEnded = res.body.timeEnded ? dayjs(res.body.timeEnded) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((commande: ICommande) => {
        commande.created = commande.created ? dayjs(commande.created) : undefined;
        commande.timeStarted = commande.timeStarted ? dayjs(commande.timeStarted) : undefined;
        commande.timeEnded = commande.timeEnded ? dayjs(commande.timeEnded) : undefined;
      });
    }
    return res;
  }
}
