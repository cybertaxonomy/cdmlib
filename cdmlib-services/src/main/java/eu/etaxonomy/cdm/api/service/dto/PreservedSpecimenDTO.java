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
import eu.etaxonomy.cdm.model.name.TaxonName;
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

    public static PreservedSpecimenDTO newInstance(DerivedUnit derivedUnit, TaxonName name ){
        PreservedSpecimenDTO newInstance = new PreservedSpecimenDTO();
        newInstance.setUuid(derivedUnit.getUuid());
        newInstance.setTitleCache(derivedUnit.getTitleCache());
        newInstance.accessionNumber = derivedUnit.getAccessionNumber();
        newInstance.preferredStableUri = derivedUnit.getPreferredStableUri();

        newInstance.setCollectioDTo(new CollectionDTO(HibernateProxyHelper.deproxy(derivedUnit.getCollection())));
        newInstance.setBarcode(derivedUnit.getBarcode());
        newInstance.setCatalogNumber(derivedUnit.getCatalogNumber());
        newInstance.setCollectorsNumber(derivedUnit.getCollectorsNumber());
        if (derivedUnit.getDerivedFrom() != null){
            newInstance.setDerivationEvent(new DerivationEventDTO(derivedUnit.getDerivedFrom() ));
        }
        if (derivedUnit.getPreservation()!= null){
            newInstance.setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
        }
        newInstance.setRecordBase(derivedUnit.getRecordBasis().getMessage());
        newInstance.setSources(derivedUnit.getSources());
        newInstance.setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());

        return newInstance;
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
