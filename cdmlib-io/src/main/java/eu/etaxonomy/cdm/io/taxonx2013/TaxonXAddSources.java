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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author pkelbert
 \* @since 20 déc. 2013
 *
 */
public class TaxonXAddSources {

    private Reference sourceUrlRef;
    private TaxonXImport importer;
    private TaxonXImportState configState;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonXAddSources.class);

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
    public Reference getSourceUrlRef() {
        return sourceUrlRef;
    }

    /**
     * @param sourceUrlRef the sourceUrlRef to set
     */
    public void setSourceUrlRef(Reference sourceUrlRef) {
        this.sourceUrlRef = sourceUrlRef;
    }


    protected IdentifiableSource getIdentifiableSource(Reference reference, Set<IdentifiableSource> sources, boolean original){
//        logger.info("getIdentifiableSource");
        boolean sourceExists=false;
        IdentifiableSource source=null;
        for (IdentifiableSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.getTitleCache().equals(reference.getTitleCache())) {
                sourceExists=true;
                break;
            }
        }
        if(!sourceExists) {
            if(original) {
                source = IdentifiableSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource,null,null,reference,null);
            } else {
                source = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null,reference,null);
            }
        }
        return source;
    }

    protected DescriptionElementSource getDescriptionElementSource(Reference reference, Set<DescriptionElementSource> sources, boolean original){
        //logger.info("getDescriptionElementSource");
        boolean sourceExists=false;
        DescriptionElementSource source=null;
        for (DescriptionElementSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.getTitleCache().equals(reference.getTitleCache())) {
                sourceExists=true;
                break;
            }
        }
        if(!sourceExists) {
            if(original) {
                source = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource,null,null,reference,null);
            } else {
                source = DescriptionElementSource.NewInstance(OriginalSourceType.Import,null,null,reference,null);
            }
        }
        return source;

    }

    /**
     * @param refMods
     * @param synonym
     */
    protected void addSource(Reference refMods, Synonym synonym) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef,synonym.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods,synonym.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec,synonym.getSources(), false);
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
    protected IndividualsAssociation addSource(Reference refMods, IndividualsAssociation indAssociation) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        DescriptionElementSource id = getDescriptionElementSource(sourceUrlRef, indAssociation.getSources(), false);
        DescriptionElementSource id2 = getDescriptionElementSource(refMods, indAssociation.getSources(), true);
        DescriptionElementSource id3 = getDescriptionElementSource(sec, indAssociation.getSources(), false);

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
    protected void addSource(Reference refMods, Taxon acceptedTaxon) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, acceptedTaxon.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods, acceptedTaxon.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec, acceptedTaxon.getSources(), false);
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
    protected void addSource(Reference refMods, TaxonName nameToBeFilled) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, nameToBeFilled.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods, nameToBeFilled.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec, nameToBeFilled.getSources(), false);

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
    protected void addSource(Reference refMods, TextData textData) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        DescriptionElementSource id = getDescriptionElementSource(sourceUrlRef, textData.getSources(), false);
        DescriptionElementSource id2 = getDescriptionElementSource(refMods, textData.getSources(), true);
        DescriptionElementSource id3 = getDescriptionElementSource(sec, textData.getSources(), false);

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
    protected void addAndSaveSource(Reference refMods, TaxonDescription taxonDescription, Reference currentRef) {
        //logger.info("addAndSaveSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, taxonDescription.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods, taxonDescription.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec, taxonDescription.getSources(), false);

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
    protected void addAndSaveSource(Reference refMods, DerivedUnit derivedUnitBase) {
        //logger.info("addAndSaveSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, derivedUnitBase.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods, derivedUnitBase.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec, derivedUnitBase.getSources(), false);

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


    /**
     * @param refMods
     * @param taxonDescription
     */
    public void addSource(Reference refMods, TaxonDescription taxonDescription) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        IdentifiableSource id = getIdentifiableSource(sourceUrlRef, taxonDescription.getSources(), false);
        IdentifiableSource id2 = getIdentifiableSource(refMods, taxonDescription.getSources(), true);
        IdentifiableSource id3 = getIdentifiableSource(sec, taxonDescription.getSources(), false);

        if( id!=null) {
            taxonDescription.addSource(id);
        }
        if( id2!=null) {
            taxonDescription.addSource(id2);
        }
        if(!configState.getConfig().doKeepOriginalSecundum() &&  id3!=null) {
            taxonDescription.addSource(id3);
        }
        importer.getDescriptionService().saveOrUpdate(taxonDescription);

    }


    /**
     * @param reference
     * @param textData
     * @param name
     * @param ref
     */
    public void addSource(Reference reference, TextData textData, TaxonName name, Reference refMods) {
        //logger.info("addSource");
        sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
        Reference sec = CdmBase.deproxy(configState.getConfig().getSecundum(), Reference.class);
        DescriptionElementSource id1 = getDescriptionElementSource(refMods, textData.getSources(),name, true);
        DescriptionElementSource id2 = getDescriptionElementSource(reference, textData.getSources(),name, false);
        if( id1!=null) {
            textData.addSource(id1);
        }

        if( id2!=null) {
            textData.addSource(id2);
        }

    }


    @SuppressWarnings({ "unused", "rawtypes" })
    private DescriptionElementSource getDescriptionElementSource(Reference reference, Set<DescriptionElementSource> sources,
            TaxonName originalname, boolean original){
        //logger.info("getDescriptionElementSource");
        boolean sourceExists=false;
        DescriptionElementSource source=null;
        for (DescriptionElementSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            TaxonName oname = src.getNameUsedInSource();
            try {
                if (r.getTitleCache().equals(reference.getTitleCache())) {
                    if (oname.getTitleCache().equalsIgnoreCase(originalname.getTitleCache())) {
                            sourceExists=true;
                            break;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(!sourceExists) {
            if(original) {
                source = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource, null,null, reference, null, originalname, null);
            } else {
                source = DescriptionElementSource.NewInstance(OriginalSourceType.Import, null,null, reference, null, originalname, null);
            }
        }
        return source;
    }



}
