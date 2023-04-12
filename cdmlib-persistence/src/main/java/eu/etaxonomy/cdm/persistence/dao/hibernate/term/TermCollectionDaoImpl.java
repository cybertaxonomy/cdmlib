/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermGraphBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @date 12.04.2023
 */
@Repository
public class TermCollectionDaoImpl
        extends IdentifiableDaoBase<TermCollection> implements ITermCollectionDao {

    public TermCollectionDaoImpl() {
        super(TermCollection.class);
    }

    @Override
    public <TERM extends DefinedTermBase> Set<TERM> listTerms(Class<TERM> type, List<TermGraphBase> graphs,
            Integer limit, String pattern){
        return listTerms(type, graphs, limit, limit, pattern, MatchMode.BEGINNING);
    }

//    TODO add to interface once ready
    public <TERM extends DefinedTermBase> Set<TERM> listTerms(Class<TERM> clazz, List<TermGraphBase> graphs,
            Integer pageNumber, Integer limit, String pattern, MatchMode matchMode){

        if (clazz == null){
            clazz = (Class)DefinedTermBase.class;
        }
        matchMode = matchMode == null ? MatchMode.EXACT : matchMode;

        String op = matchMode.getMatchOperator();
        String hql = "SELECT DISTINCT term FROM TermCollection tc JOIN tc.termRelations rel JOIN rel.term term LEFT JOIN term.representations rep "
                + " WHERE "
                + "    (term.titleCache "+op+" :pattern OR rep.label "+op+" :pattern ) AND"
                + "      tc.id IN :collectionIDs ";

        Query<DefinedTermBase> query = getSession().createQuery(hql, DefinedTermBase.class);

        query.setParameter("pattern", matchMode.queryStringFrom(pattern));
        Set<Integer> collectionIDs = graphs.stream().map(g->g.getId()).collect(Collectors.toSet());
        query.setParameterList("collectionIDs", collectionIDs);

        //TODO limit + pageNumber, but then we need ordering
        Set result = new HashSet<>();
        List<DefinedTermBase> list = query.list();
        result.addAll(query.list());
//        List<S> results = deduplicateResult(query.list());
        return result;
    }

//   not yet clear if we want TermCollectionDaoImpl also as base class for other dao-s
//    protected TermCollectionDaoImpl(Class<TermCollection> type) {
//        super(type);
//    }
}
