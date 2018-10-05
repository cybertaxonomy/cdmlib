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
import eu.etaxonomy.cdm.model.reference.ReferenceType;
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
@Repository("taxonGraphDao")
@Transactional(readOnly = true)
public class TaxonGraphDaoHibernateImpl extends AbstractHibernateTaxonGraphProcessor implements ITaxonGraphDao {

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
    protected TaxonRelationshipType relType() {
        if(relType == null){
            relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
        }
        return relType;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Session getSession() {
        return taxonDao.getSession();
    }


}
