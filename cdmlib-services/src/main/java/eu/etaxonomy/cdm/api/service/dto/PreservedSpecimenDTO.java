/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.envers.tools.Pair;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author pplitzner
 * @since Mar 26, 2015
 *
 */
public class PreservedSpecimenDTO extends DerivateDTO{

    private String accessionNumber;
    private URI preferredStableUri;

    private List<Pair<UUID, String>> associatedTaxa;
    private Map<String, List<String>> types;





//    public PreservedSpecimenDTO(DerivedUnit derivedUnit){
//        super();
//        this.setUuid(derivedUnit.getUuid());
//        this.setTitleCache(derivedUnit.getTitleCache());
//        this.setAccessionNumber(derivedUnit.getAccessionNumber());
//        this.setPreferredStableUri(derivedUnit.getPreferredStableUri());
//
//        this.setCollectioDTo(new CollectionDTO(HibernateProxyHelper.deproxy(derivedUnit.getCollection())));
//        this.setBarcode(derivedUnit.getBarcode());
//        this.setCatalogNumber(derivedUnit.getCatalogNumber());
//        this.setCollectorsNumber(derivedUnit.getCollectorsNumber());
//        if (derivedUnit.getDerivedFrom() != null){
//            this.setDerivationEvent(new DerivationEventDTO(derivedUnit.getDerivedFrom() ));
//        }
//        if (derivedUnit.getPreservation()!= null){
//            this.setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
//        }
//        this.setRecordBase(derivedUnit.getRecordBasis().getMessage());
//        this.setSources(derivedUnit.getSources());
//        this.setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
//
//    }
//
//    public static PreservedSpecimenDTO newInstance(DerivedUnit derivedUnit ){
//        PreservedSpecimenDTO newInstance = new PreservedSpecimenDTO(derivedUnit);
//
//        return newInstance;
//    }


    /**
     * @param derivedUnit
     */
    public PreservedSpecimenDTO(DerivedUnit derivedUnit) {
        super(derivedUnit);
        accessionNumber = derivedUnit.getAccessionNumber();
        preferredStableUri = derivedUnit.getPreferredStableUri();
        if (derivedUnit.getCollection() != null){
            setCollectioDTo(new CollectionDTO(HibernateProxyHelper.deproxy(derivedUnit.getCollection())));
        }
        setBarcode(derivedUnit.getBarcode());
        setCatalogNumber(derivedUnit.getCatalogNumber());
        listLabel = derivedUnit.getCatalogNumber();
        setCollectorsNumber(derivedUnit.getCollectorsNumber());
        if (derivedUnit.getDerivedFrom() != null){
            setDerivationEvent(new DerivationEventDTO(derivedUnit.getDerivedFrom() ));
        }
        if (derivedUnit.getPreservation()!= null){
            setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
        }
        setRecordBase(derivedUnit.getRecordBasis().getMessage());
        setSources(derivedUnit.getSources());
        setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
    }



    public String getAccessionNumber() {
        return accessionNumber;
    }
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Map<String, List<String>> getTypes() {
        return types;
    }
    public void addTypes(String typeStatus, List<String> typedTaxa){
        if(types==null){
            types = new HashMap<String, List<String>>();
        }
        types.put(typeStatus, typedTaxa);
    }

    public List<Pair<UUID, String>> getAssociatedTaxa() {
        return associatedTaxa;
    }
    public void addAssociatedTaxon(Taxon taxon){
        if(associatedTaxa==null){
            associatedTaxa = new ArrayList<Pair<UUID, String>>();
        }
        associatedTaxa.add(new Pair<UUID, String>(taxon.getUuid(), taxon.getTitleCache()));
    }

    public void setPreferredStableUri(URI preferredStableUri) {
        this.preferredStableUri = preferredStableUri;
    }

    public URI getPreferredStableUri() {
        return preferredStableUri;
    }

}
