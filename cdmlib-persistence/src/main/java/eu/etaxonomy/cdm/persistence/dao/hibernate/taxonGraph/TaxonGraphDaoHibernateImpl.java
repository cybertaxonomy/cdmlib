/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;

/**Ta
 * Provides the business logic to manage multiple classifications as
 * classification fragments in a graph of
 * {@link eu.etaxonomy.cdm.model.taxon.Taxon Taxa} and {@link eu.etaxonomy.cdm.model.taxon.TaxonRelationship TaxonRelationships}.
 *
 * For further details on the concept and related discussion see https://dev.e-taxonomy.eu/redmine/issues/6173
 *
 *
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
@Repository
@Transactional(readOnly = true)
public class TaxonGraphDaoHibernateImpl implements ITaxonGraphDao {

    private TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

    private EnumSet<ReferenceType> referenceSectionTypes = EnumSet.of(ReferenceType.Section, ReferenceType.BookSection);

    private static final Logger logger = Logger.getLogger(TaxonGraphDaoHibernateImpl.class);

    public static final PrefKey CDM_PREF_KEY_SEC_REF_UUID = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), PreferencePredicate.TaxonGraphSecRefUuid);

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private IReferenceDao referenceDao;

    @Autowired
    private ITaxonNameDao nameDao;

    private UUID secReferenceUUID;


    @Override
    @Deprecated
    public void setSecReferenceUUID(UUID uuid){
        // ONLY for tests
        secReferenceUUID = uuid;
    }

    public UUID getSecReferenceUUID(){
        if(secReferenceUUID != null){
            return secReferenceUUID;
        } else {
            CdmPreference pref = CdmPreferenceLookup.instance().get(CDM_PREF_KEY_SEC_REF_UUID);
            UUID uuid = null;
            if(pref != null && pref.getValue() != null){
                try {
                    uuid = UUID.fromString(pref.getValue());
                } catch (Exception e) {
                    // TODO is logging only ok?
                    logger.error(e);
                }
            }
            if(uuid == null){
                logger.error("missing cdm property: " + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getSubject() + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getPredicate());
            }
            return uuid;
        }
    }

    protected TaxonRelationshipType relType() {
        if(relType == null){
            relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
        }
        return relType;
    }

    /**
     * FIXME: this is a duplicate implementation of the same method as in {@link TaxonGraphBeforeTransactionCompleteProcess}
     *
     * @param taxonName
     * @return
     * @throws TaxonGraphException
     */
    protected Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {

        UUID secRefUUID = getSecReferenceUUID();
        Session session = taxonDao.getSession();
        TaxonName taxonNamePersisted = session.load(TaxonName.class, taxonName.getId());
        Taxon taxon;
        if(taxonName.getTaxa().size() == 0){
            if(taxonNamePersisted != null){
            Reference secRef = referenceDao.load(secRefUUID);
                taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
                session.saveOrUpdate(taxon);
            } else {
                throw new TaxonGraphException("Can't create taxon for deleted name: " + taxonName);
            }
        } else if(taxonName.getTaxa().size() == 1){
            taxon = taxonName.getTaxa().iterator().next();
            if(!secRefUUID.equals(taxon.getSec().getUuid())){
                throw new TaxonGraphException("The taxon for a name to be used in a taxon graph must have the default sec reference [secRef uuid: "+ secRefUUID.toString() +"]");
            }
        } else {
            for(Taxon t : taxonName.getTaxa()){
                if(secRefUUID.equals(t.getSec().getUuid())){
                    taxon = t;
                }
            }
            throw new TaxonGraphException("A name to be used in a taxon graph must only have one taxon with the default sec reference [secRef uuid: "+ secRefUUID.toString() +"]");
        }
        return taxon;
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
        return taxonDao.listTaxonGraphEdgeDTOs(fromTaxonUUID, toTaxonUUID, relType(), includeUnpublished, null, null);
    }

    @Override
    public List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException{
        return taxonDao.listTaxonGraphEdgeDTOs(fromtaxonUuid, toTaxonUuid, relType(), includeUnpublished, null, null);
    }


}
