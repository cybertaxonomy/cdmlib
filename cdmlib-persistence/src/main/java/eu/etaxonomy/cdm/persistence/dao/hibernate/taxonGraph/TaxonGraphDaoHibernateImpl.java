/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmPreferenceCache;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;

/**
 * Implementation of the abstract {@link AbstractHibernateTaxonGraphProcessor}
 * business logic to provide a <b>purely read only access</b> to graphs
 * consisting of {@link eu.etaxonomy.cdm.model.taxon.Taxon Taxa} and
 * {@link eu.etaxonomy.cdm.model.taxon.TaxonRelationship TaxonRelationships}.
 * These graphs allow for managing classification fragments in a graph
 * structure. All <code>write operation</code>, that is all modification of the
 * graph is done automatically solely by
 * {@link eu.etaxonomy.cdm.api.service.taxonGraph.TaxonGraphBeforeTransactionCompleteProcess}.
 * <p>
 * The conceptual idea for the resulting graph is described in <a href=
 * "https://dev.e-taxonomy.eu/redmine/issues/6173#6-N1T-Higher-taxon-graphs-with-includedIn-relations-taxon-relationships">#6173
 * 6) [N1T] Higher taxon-graphs with includedIn relations taxon
 * relationships}</a>. The
 * <code>TaxonGraphBeforeTransactionCompleteProcess</code> is instantiated and
 * used in the {@link TaxonGraphHibernateListener}.
 *
 * @author a.kohlbecker
 * @since Sep 26, 2018
 */
@Repository("taxonGraphDao")
@Transactional(readOnly = true) //NOTE by AM (2022-5): not sure if this is really needed, tests run without transactional, usually transaction borders are not defined in persistence, but this class is used a bit differently so I am not sure; keep it as it is for now to not brake working code
public class TaxonGraphDaoHibernateImpl
        extends AbstractHibernateTaxonGraphProcessor
        implements ITaxonGraphDao, InitializingBean {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonGraphDaoHibernateImpl.class);

    public TaxonGraphDaoHibernateImpl(IPreferenceDao preferenceDao) {
        super(preferenceDao);
    }

    private TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

    public static final PrefKey CDM_PREF_KEY_SEC_REF_UUID = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.TaxonGraphSecRefUuid);

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private IPreferenceDao preferenceDao;

    @Override
    protected TaxonRelationshipType relType() {
        if(relType == null){
            relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
        }
        return relType;
    }

    @Override
    public List<TaxonGraphEdgeDTO> listTaxonGraphEdgeDTOs(UUID fromTaxonUuid, UUID toTaxonUuid, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageIndex) {

        Query<TaxonGraphEdgeDTO> query = prepareTaxonGraphEdgeDTOs(
                fromTaxonUuid, toTaxonUuid, type, includeUnpublished, false, TaxonGraphEdgeDTO.class);


        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageIndex != null) {
                query.setFirstResult(pageIndex * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }

        List<TaxonGraphEdgeDTO> result = query.list();
        return result;
    }

    @Override
    public long countTaxonGraphEdgeDTOs(UUID fromTaxonUuid, UUID toTaxonUuid, TaxonRelationshipType type,
            boolean includeUnpublished) {

        Query<Long> query = prepareTaxonGraphEdgeDTOs(fromTaxonUuid, toTaxonUuid, type, includeUnpublished, true, Long.class);
        Long count = query.uniqueResult();
        return count;
    }

    protected <R extends Object> Query<R> prepareTaxonGraphEdgeDTOs(UUID fromTaxonUuid, UUID toTaxonUuid, TaxonRelationshipType type,
            boolean includeUnpublished, boolean doCount, Class<R> returnedClass) {

        Session session = getSession();
        String hql = "";
        if(doCount){
            hql = "COUNT(tr.id)";
        } else {
            hql += "SELECT new eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO("
                    + " fromT.uuid, fromN.titleCache, fromN_R.idInVocabulary, fromN_R.orderIndex, "
                    + " toT.uuid, toN.titleCache, toN_R.idInVocabulary, toN_R.orderIndex, "
                    + " c.uuid, c.titleCache"
                    + ")";
        }
        hql += " FROM TaxonRelationship as tr "
                + " JOIN tr.source as s"
                + " JOIN s.citation as c"
                + " JOIN tr.relatedFrom as fromT"
                + " JOIN tr.relatedTo as toT"
                + " JOIN fromT.name as fromN"
                + " JOIN toT.name as toN"
                + " JOIN fromN.rank as fromN_R"
                + " JOIN toN.rank as toN_R"
                + " LEFT OUTER JOIN toN.registrations as toN_Reg"
                + " LEFT OUTER JOIN fromN.registrations as fromN_Reg"
                + " WHERE tr.type = :reltype"
                + " AND (fromN_Reg IS NULL OR fromN_Reg.status = :regStatus)"
                + " AND (toN_Reg IS NULL OR toN_Reg.status = :regStatus) "
                ;

        if(fromTaxonUuid != null){
            hql += " AND fromT.uuid = :fromTaxonUuid";
            if(!includeUnpublished){
                hql += " AND fromT.publish is true";
            }
        }
        if(toTaxonUuid != null){
            hql += " AND toT.uuid = :toTaxonUuid";
            if(!includeUnpublished){
                hql += " AND toT.publish is true";
            }
        }

        Query<R> query = session.createQuery(hql, returnedClass);
        query.setParameter("reltype", type);
        query.setParameter("regStatus", RegistrationStatus.PUBLISHED);
        if(fromTaxonUuid != null){
            query.setParameter("fromTaxonUuid", fromTaxonUuid);
        }
        if(toTaxonUuid != null){
            query.setParameter("toTaxonUuid", toTaxonUuid);
        }
        return query;
    }

    @Override
    public List<TaxonGraphEdgeDTO> edges(TaxonName fromName, TaxonName toName, boolean includeUnpublished) throws TaxonGraphException{
        UUID fromTaxonUUID = null;
        UUID toTaxonUUID = null;
        if(fromName != null){
            fromTaxonUUID = assureSingleTaxon(fromName).getUuid();
        }
        if(toName != null){
            toTaxonUUID = assureSingleTaxon(toName).getUuid();
        }
        return listTaxonGraphEdgeDTOs(fromTaxonUUID, toTaxonUUID, relType(), includeUnpublished, null, null);
    }

    @Override
    public List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException{
        return listTaxonGraphEdgeDTOs(fromtaxonUuid, toTaxonUuid, relType(), includeUnpublished, null, null);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        CdmPreferenceCache.instance(preferenceDao);
    }

    @Override
    public Session getSession() {
        return taxonDao.getSession();
    }

}
