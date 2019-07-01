/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
@Repository
public class RegistrationDaoHibernateImpl
            extends AnnotatableDaoImpl<Registration>
            implements IRegistrationDao {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RegistrationDaoHibernateImpl.class);

    /**
     * @param type
     */
    public RegistrationDaoHibernateImpl() {
        super(Registration.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Long count(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus) {
        //return 0 for detached volatile references
        if (isVolatile(reference)){
            return Long.valueOf(0);
        }
        Query query = makeReferenceQuery(reference, includedStatus, true);
        @SuppressWarnings("unchecked")
        List<Long> list = query.list();
        return list.isEmpty()? Long.valueOf(0) : list.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> list(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer limit, Integer start, List<String> propertyPaths) {

        if (isVolatile(reference)){
            return Collections.emptyList();
        }

        Query query = makeReferenceQuery(reference, includedStatus, false);

        // TODO complete ....
        if(limit != null /*&&  !doCount*/) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        //TODO order hints do not work with queries

        @SuppressWarnings("unchecked")
        List<Registration> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    /**
     * @param reference
     * @return
     */
    private boolean isVolatile(Optional<Reference> reference) {
        return reference != null && reference.isPresent() && reference.get().getId() == 0;
    }

    /**
     * @param reference
     * @param includedStatus
     * @param isCount
     * @return
     */
    private Query makeReferenceQuery(Optional<Reference> reference,
            Collection<RegistrationStatus> includedStatus,
            boolean isCount) {

        String select = "SELECT " + (isCount? " count(DISTINCT r) as cn ": "DISTINCT r ");
        String from = " FROM Registration r LEFT JOIN r.typeDesignations desig "
                + "     LEFT JOIN r.name n ";
        String where = " WHERE (1=1) ";
        String orderBy = isCount ? "" : " ORDER BY r.id ";

        ReferenceType refTypeParameter = null;

        if (reference == null){
            //do nothing
        }else if (reference.isPresent()){
           from += "   LEFT JOIN n.nomenclaturalReference nomRef "
                   + " LEFT JOIN desig.citation desigRef ";
           where += " AND ("
                   + "     nomRef =:ref "
                   + "     OR (nomRef.type =:refType AND nomRef.inReference =:ref) "
                   + "     OR desigRef =:ref "
                   + "     OR (desigRef.type =:refType AND desigRef.inReference =:ref)"
                   + ")";
           refTypeParameter = ReferenceType.Section;
        }else{  //ref is null
           where += " AND ((r.name IS NULL AND size(r.typeDesignations) = 0 ) "
                   + "     OR (n IS NOT NULL AND r.name.nomenclaturalReference IS NULL ) "
                   + "     OR (size(r.typeDesignations) > 0 AND desig.citation IS NULL )"
                   + ") "
                   ;
        }
        boolean hasStatus = includedStatus != null && !includedStatus.isEmpty();
        if (hasStatus){
            where += " AND r.status IN (:status) ";
        }

        String hql = select + from + where + orderBy;
        Query query = getSession().createQuery(hql);
        if (reference != null && reference.isPresent()){
            query.setParameter("ref", reference.get());
        }
        if(refTypeParameter != null){
            query.setParameter("refType", refTypeParameter);
        }
        if (hasStatus){
            query.setParameterList("status", includedStatus);
        }
        return query;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long count(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids) {
        Query query = makeFilteredSearchQuery(submitterUuid, includedStatus, identifierFilterPattern,
                taxonNameFilterPattern, referenceFilterPattern, typeDesignationStatusUuids, true, null);
        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG);
        @SuppressWarnings("unchecked")
        List<Long> list = query.list();
        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.WARN);
        return list.isEmpty()? Long.valueOf(0) : list.get(0);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> list(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Query query = makeFilteredSearchQuery(submitterUuid, includedStatus, identifierFilterPattern,
                taxonNameFilterPattern, referenceFilterPattern, typeDesignationStatusUuids, false, orderHints);

        if(limit != null /*&&  !doCount*/) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG);
        @SuppressWarnings("unchecked")
        List<Registration> results = query.list();
        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.WARN);
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }


    /**
     * @param submitterUuid
     * @param includedStatus
     * @param identifierFilterPattern
     * @param taxonNameFilterPattern
     * @param typeDesignationStatusUuids
     * @param isCount
     * @return
     */
    private Query makeFilteredSearchQuery(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String identifierFilterPattern, String taxonNameFilterPattern, String referenceFilterPattern,
            Collection<UUID> typeDesignationStatusUuids, boolean isCount, List<OrderHint> orderHints) {

        Map<String, Object> parameters = new HashMap<>();

        boolean doNameFilter = StringUtils.isNoneBlank(taxonNameFilterPattern);
        boolean doReferenceFilter = StringUtils.isNoneBlank(referenceFilterPattern);
        boolean doTypeStatusFilter = typeDesignationStatusUuids != null && typeDesignationStatusUuids.size() > 0;

        String select = "SELECT " + (isCount? " count(DISTINCT r) as cn ": "DISTINCT r ");
        String from = " FROM Registration r "
                + "     LEFT JOIN r.typeDesignations desig "
                + "     LEFT JOIN r.name n "
                + (doNameFilter ?  " LEFT JOIN desig.typifiedNames typifiedNames ":"")
                + (doTypeStatusFilter ? " LEFT JOIN desig.typeStatus typeStatus":"")  // without this join hibernate would make a cross join here
                + (doReferenceFilter ? " LEFT JOIN desig.citation typeDesignationCitation LEFT JOIN n.nomenclaturalReference nomRef":"")
            ;
        // further JOIN
        String where = " WHERE (1=1) ";
        String orderBy = "";
        if(!isCount){
            orderBy = orderByClause("r", orderHints).toString();
        }

        if(submitterUuid != null){
            from += " LEFT JOIN r.submitter submitter "; // without this join hibernate would make a cross join here
            where += " AND submitter.uuid =:submitterUuid";
            parameters.put("submitterUuid", submitterUuid);
        }
        if(includedStatus != null && includedStatus.size() > 0) {
            where += " AND r.status in (:includedStatus)";
            parameters.put("includedStatus", includedStatus);
        }
        if(StringUtils.isNoneBlank(identifierFilterPattern)){
            where += " AND r.identifier LIKE :identifierFilterPattern";
            parameters.put("identifierFilterPattern", MatchMode.ANYWHERE.queryStringFrom(identifierFilterPattern));
        }
        if(doNameFilter){
            where += " AND (r.name.titleCache LIKE :taxonNameFilterPattern OR typifiedNames.titleCache LIKE :taxonNameFilterPattern)";
            parameters.put("taxonNameFilterPattern", MatchMode.ANYWHERE.queryStringFrom(taxonNameFilterPattern));
        }
        if(doReferenceFilter){
            where += " AND (typeDesignationCitation.titleCache LIKE :referenceFilterPattern OR nomRef.titleCache LIKE :referenceFilterPattern)";
            parameters.put("referenceFilterPattern", MatchMode.ANYWHERE.queryStringFrom(referenceFilterPattern));
        }
        if(doTypeStatusFilter){
            where += " AND typeStatus.uuid in (:typeDesignationStatusUuids)";
            parameters.put("typeDesignationStatusUuids", typeDesignationStatusUuids);
        }
        String hql = select + from + where + orderBy;
        Query query = getSession().createQuery(hql);

        for(String paramName : parameters.keySet()){
            Object value = parameters.get(paramName);
            if(value instanceof Collection){
                query.setParameterList(paramName, (Collection)value);
            } else {
                query.setParameter(paramName, value);
            }
        }

        return query;
    }


}
