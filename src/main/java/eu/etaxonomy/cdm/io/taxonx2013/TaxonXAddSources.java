// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx2013;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author pkelbert
 * @date 20 déc. 2013
 *
 */
public class TaxonXAddSources {

    private Reference<?> sourceUrlRef;
    private TaxonXImport importer;

    /**
     * @param importer
     */
    public void setImporter(TaxonXImport importer) {
        this.importer=importer;

    }


    /**
     * @return the sourceUrlRef
     */
    public Reference<?> getSourceUrlRef() {
        return sourceUrlRef;
    }

    /**
     * @param sourceUrlRef the sourceUrlRef to set
     */
    public void setSourceUrlRef(Reference<?> sourceUrlRef) {
        this.sourceUrlRef = sourceUrlRef;
    }

    /**
     * @param refMods
     * @param synonym
     */
    protected void addSource(Reference<?> refMods, Synonym synonym) {
        synonym.addSource(OriginalSourceType.Import,null,null,refMods,null);
        synonym.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    }


    /**
     * @param refMods
     * @param indAssociation
     */
    protected IndividualsAssociation addSource(Reference<?> refMods, IndividualsAssociation indAssociation) {
        indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);
        indAssociation.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
        return indAssociation;
    }

    /**
     * @param refMods
     * @param acceptedTaxon
     */
    protected void addSource(Reference<?> refMods, Taxon acceptedTaxon) {
        acceptedTaxon.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
        acceptedTaxon.addSource(OriginalSourceType.Import, null,null,refMods,null);
    }

    /**
     * @param refMods
     * @param nameToBeFilled
     */
    protected void addSource(Reference<?> refMods, NonViralName<?> nameToBeFilled) {
        nameToBeFilled.addSource(OriginalSourceType.Import,null,null,refMods,null);
        nameToBeFilled.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    }

    /**
     * @param refMods
     * @param textData
     */
    protected void addSource(Reference<?> refMods, TextData textData) {
        textData.addSource(OriginalSourceType.Import, null,null,refMods,null);
        textData.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    }

    /**
     * @param refMods
     * @param taxonDescription
     * @param currentRef
     */
    protected void addAndSaveSource(Reference<?> refMods, TaxonDescription taxonDescription, Reference<?> currentRef) {
        taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);
        taxonDescription.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);

        if(currentRef != null && currentRef != refMods) {
            taxonDescription.addSource(OriginalSourceType.Import,null,null,currentRef,null);
        }

        importer.getDescriptionService().saveOrUpdate(taxonDescription);
    }

    /**
     * @param refMods
     * @param derivedUnitBase
     */
    protected void addAndSaveSource(Reference<?> refMods, DerivedUnit derivedUnitBase) {
        derivedUnitBase.addSource(OriginalSourceType.Import, null,null,refMods,null);
        derivedUnitBase.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
        importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);
        derivedUnitBase= CdmBase.deproxy(derivedUnitBase, DerivedUnit.class);
    }




}
