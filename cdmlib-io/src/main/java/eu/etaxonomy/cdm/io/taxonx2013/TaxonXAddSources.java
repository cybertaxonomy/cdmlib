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

import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
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
    private TaxonXImportState configState;

    /**
     * @param importer
     */
    public void setImporter(TaxonXImport importer) {
        this.importer=importer;
    }


    /**
     * @param configState the configState to set
     */
    public void setConfigState(TaxonXImportState configState) {
        this.configState = configState;
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


    private IdentifiableSource getIdentifiableSource(Reference<?> reference, Set<IdentifiableSource> sources){
        boolean sourceExists=false;
        IdentifiableSource source=null;
        for (IdentifiableSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference<?> r = src.getCitation();
            if (r.getTitleCache().equals(reference.getTitleCache()) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            source = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null,reference,null);
        }
        return source;
    }

    private DescriptionElementSource getDescriptionElementSource(Reference<?> reference, Set<DescriptionElementSource> sources){
        boolean sourceExists=false;
        DescriptionElementSource source=null;
        for (DescriptionElementSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference<?> r = src.getCitation();
            if (r.getTitleCache().equals(reference.getTitleCache()) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            source = DescriptionElementSource.NewInstance(OriginalSourceType.Import,null,null,reference,null);
        }
        return source;

    }

    /**
     * @param refMods
     * @param synonym
     */
    protected void addSource(Reference<?> refMods, Synonym synonym) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef,synonym.getSources());
        IdentifiableSource id2 = getIdentifiableSource(refMods,synonym.getSources());
        IdentifiableSource id3 = getIdentifiableSource(sec,synonym.getSources());
        if( id!=null) {
            synonym.addSource(id);
        }
        if( id2!=null) {
            synonym.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            synonym.addSource(id3);
        }
    }


    /**
     * @param refMods
     * @param indAssociation
     */
    protected IndividualsAssociation addSource(Reference<?> refMods, IndividualsAssociation indAssociation) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        DescriptionElementSource id = getDescriptionElementSource(sourceUrlRef, indAssociation.getSources());
        DescriptionElementSource id2 = getDescriptionElementSource(refMods, indAssociation.getSources());
        DescriptionElementSource id3 = getDescriptionElementSource(sec, indAssociation.getSources());

        if(id!=null) {
            indAssociation.addSource(id);
        }
        if( id2!=null) {
            indAssociation.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            indAssociation.addSource(id3);
        }
        return indAssociation;
    }

    /**
     * @param refMods
     * @param acceptedTaxon
     */
    protected void addSource(Reference<?> refMods, Taxon acceptedTaxon) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, acceptedTaxon.getSources());
        IdentifiableSource id2 = getIdentifiableSource(refMods, acceptedTaxon.getSources());
        IdentifiableSource id3 = getIdentifiableSource(sec, acceptedTaxon.getSources());
        if( id!=null) {
            acceptedTaxon.addSource(id);
        }
        if( id2!=null) {
            acceptedTaxon.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            acceptedTaxon.addSource(id3);
        }
    }

    /**
     * @param refMods
     * @param nameToBeFilled
     */
    protected void addSource(Reference<?> refMods, NonViralName<?> nameToBeFilled) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, nameToBeFilled.getSources());
        IdentifiableSource id2 = getIdentifiableSource(refMods, nameToBeFilled.getSources());
        IdentifiableSource id3 = getIdentifiableSource(sec, nameToBeFilled.getSources());

        if( id!=null) {
            nameToBeFilled.addSource(id);
        }
        if( id2!=null) {
            nameToBeFilled.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            nameToBeFilled.addSource(id3);
        }

    }

    /**
     * @param refMods
     * @param textData
     */
    protected void addSource(Reference<?> refMods, TextData textData) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        DescriptionElementSource id = getDescriptionElementSource(sourceUrlRef, textData.getSources());
        DescriptionElementSource id2 = getDescriptionElementSource(refMods, textData.getSources());
        DescriptionElementSource id3 = getDescriptionElementSource(sec, textData.getSources());

        if( id!=null) {
            textData.addSource(id);
        }
        if( id2!=null) {
            textData.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            textData.addSource(id3);
        }

    }

    /**
     * @param refMods
     * @param taxonDescription
     * @param currentRef
     */
    protected void addAndSaveSource(Reference<?> refMods, TaxonDescription taxonDescription, Reference<?> currentRef) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, taxonDescription.getSources());
        IdentifiableSource id2 = getIdentifiableSource(refMods, taxonDescription.getSources());
        IdentifiableSource id3 = getIdentifiableSource(sec, taxonDescription.getSources());

        if( id!=null) {
            taxonDescription.addSource(id);
        }
        if( id2!=null) {
            taxonDescription.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() && id3!=null) {
            taxonDescription.addSource(id3);
        }

        importer.getDescriptionService().saveOrUpdate(taxonDescription);
    }

    /**
     * @param refMods
     * @param derivedUnitBase
     */
    protected void addAndSaveSource(Reference<?> refMods, DerivedUnit derivedUnitBase) {
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, derivedUnitBase.getSources());
        IdentifiableSource id2 = getIdentifiableSource(refMods, derivedUnitBase.getSources());
        IdentifiableSource id3 = getIdentifiableSource(sec, derivedUnitBase.getSources());

        if( id!=null) {
            derivedUnitBase.addSource(id);
        }
        if( id2!=null) {
            derivedUnitBase.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() &&  id3!=null) {
            derivedUnitBase.addSource(id3);
        }
        importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);
        derivedUnitBase= CdmBase.deproxy(derivedUnitBase, DerivedUnit.class);
    }




}
