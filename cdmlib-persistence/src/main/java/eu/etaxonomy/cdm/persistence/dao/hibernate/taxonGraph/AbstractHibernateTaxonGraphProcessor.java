/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;

/**
 * @author a.kohlbecker
 * @since Oct 4, 2018
 *
 */
public abstract class AbstractHibernateTaxonGraphProcessor {

    private static final Logger logger = Logger.getLogger(AbstractHibernateTaxonGraphProcessor.class);

    private Reference secReference = null;

    /**
     * MUST ONLY BE USED IN TESTS
     */
    @Deprecated
    protected UUID secReferenceUUID;


    /**
     * MUST ONLY BE USED IN TESTS
     */
    @Deprecated
    protected void setSecReferenceUUID(UUID uuid){
        secReferenceUUID = uuid;
    }

    public UUID getSecReferenceUUID(){
        if(secReferenceUUID != null){
            return secReferenceUUID;
        } else {
            CdmPreference pref = CdmPreferenceLookup.instance().get(TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID);
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

    protected Reference secReference(){
        if(secReference == null){
            Query q = getSession().createQuery("SELECT r FROM Reference r WHERE r.uuid = :uuid");
            q.setParameter("uuid", getSecReferenceUUID());
            secReference = (Reference) q.uniqueResult();
        }
        return secReference;
    }

    abstract Session getSession();

    public Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {

        UUID secRefUuid = getSecReferenceUUID();
        Session session = getSession();
        TaxonName taxonNamePersisted = session.load(TaxonName.class, taxonName.getId());
        Taxon taxon;
        if(taxonName.getTaxa().size() == 0){
            if(taxonNamePersisted != null){
            Reference secRef = secReference();
                taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
                session.saveOrUpdate(taxon);
                return taxon;
            } else {
                throw new TaxonGraphException("Can't create taxon for deleted name: " + taxonName);
            }
        } else if(taxonName.getTaxa().size() == 1){
            taxon = taxonName.getTaxa().iterator().next();
            if(taxon.getSec() == null){
                taxon = session.load(Taxon.class, taxon.getId());
                taxon.setSec(secReference());
                session.saveOrUpdate(taxon);
                return taxon;
            } else if(!secRefUuid.equals(taxon.getSec().getUuid())){
                throw new TaxonGraphException("The taxon for a name to be used in a taxon graph must have the default sec reference [secRef uuid: "+ secRefUuid.toString() +"]");
            }
        } else {
            for(Taxon t : taxonName.getTaxa()){
                if(secRefUuid.equals(t.getSec().getUuid())){
                    taxon = t;
                }
            }
            throw new TaxonGraphException("A name to be used in a taxon graph must only have one taxon with the default sec reference [secRef uuid: "+ secRefUuid.toString() +"]");
        }
        return session.load(Taxon.class, taxon.getId());
    }

}
