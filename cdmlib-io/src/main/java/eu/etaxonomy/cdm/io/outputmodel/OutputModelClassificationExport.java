/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupNameComparator;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeComparator;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
@Component
public class OutputModelClassificationExport
            extends CdmExportBase<OutputModelConfigurator, OutputModelExportState, IExportTransformer>
            implements ICdmExport<OutputModelConfigurator, OutputModelExportState>{


    private static final long serialVersionUID = 2518643632756927053L;
    private static final String STD_TEAM_CONCATINATION = ", ";
    private static final String FINAL_TEAM_CONCATINATION = " & ";

    public OutputModelClassificationExport() {
        super();
        this.ioName = this.getClass().getSimpleName();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(OutputModelExportState state) {
        OutputModelConfigurator config = state.getConfig();

        if (config.getClassificationUuids().isEmpty()){
            //TODO
            state.setEmptyData();
            return;
        }
        //TODO MetaData
        for (UUID classificationUuid : config.getClassificationUuids()){
            Classification classification = getClassificationService().find(classificationUuid);


            if (classification == null){
                String message = String.format("Classification for given classification UUID not found. No data imported for %s", classificationUuid.toString());
                //TODO
                state.getResult().addWarning(message);
            }else{
//                gtTaxonNodeService().
                TaxonNode root = classification.getRootNode();

                UUID uuid = root.getUuid();

                root = getTaxonNodeService().load(uuid);
                for (TaxonNode child : root.getChildNodes()){
                    handleTaxon(state, child);
                    //TODO progress monitor
                }
            }
        }
        state.getProcessor().createFinalResult(state);
    }

    /**
     * @param state
     * @param taxon
     */
    private void handleTaxon(OutputModelExportState state, TaxonNode taxonNode) {

        Taxon taxon = taxonNode.getTaxon();
        if (taxon == null){
            state.getResult().addError ("There was a taxon node without a taxon: " + taxonNode.getUuid(), "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
        }else{
             try{
                TaxonName name = taxon.getName();
                handleName(state, name);
                for (Synonym syn : taxon.getSynonyms()){
                    handleSynonym(state, syn);
                }


                OutputModelTable table = OutputModelTable.TAXON;
                String[] csvLine = new String[table.getSize()];

                csvLine[table.getIndex(OutputModelTable.TAXON_ID)] = getId(state, taxon);
                csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, name);
                Taxon parent = (taxonNode.getParent()==null) ? null : taxonNode.getParent().getTaxon();
                csvLine[table.getIndex(OutputModelTable.PARENT_FK)] = getId(state, parent);
                csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE_FK)] = getId(state, taxon.getSec());
                csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE)] = getTitleCache(taxon.getSec());
                csvLine[table.getIndex(OutputModelTable.CLASSIFICATION_ID)] = getId(state, taxonNode.getClassification());
                csvLine[table.getIndex(OutputModelTable.CLASSIFICATION_TITLE)] = taxonNode.getClassification().getTitleCache();

                state.getProcessor().put(table, taxon, csvLine, state);
                handleDescriptions(state, taxon);
             }catch(Exception e){
                 state.getResult().addError ("An unexpected problem occurred when trying to export taxon with id " + taxon.getId(), e);
                 state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
             }
       }
       for (TaxonNode child: taxonNode.getChildNodes()){
           handleTaxon(state, child);
       }

    }

    /**
     * @param state
     * @param taxon
     */
    private void handleDescriptions(OutputModelExportState state, CdmBase cdmBase) {
        if (cdmBase instanceof Taxon){
            Taxon taxon = HibernateProxyHelper.deproxy(cdmBase, Taxon.class);
            Set<TaxonDescription> descriptions = taxon.getDescriptions();
            List<DescriptionElementBase> simpleFacts = new ArrayList<>();
            List<DescriptionElementBase> specimenFacts = new ArrayList<>();
            List<DescriptionElementBase> distributionFacts = new ArrayList<>();
            List<DescriptionElementBase> commonNameFacts = new ArrayList<>();
            List<DescriptionElementBase> usageFacts = new ArrayList<>();
            for (TaxonDescription description: descriptions){
                if (description.getElements() != null){
                    for (DescriptionElementBase element: description.getElements()){
                        element = CdmBase.deproxy(element);
                        if (element.getFeature().equals(Feature.COMMON_NAME())){
                            commonNameFacts.add(element);
                        }else if (element.getFeature().equals(Feature.DISTRIBUTION())){
                            distributionFacts.add(element);
                        }else if (element instanceof IndividualsAssociation || isSpecimenFeature(element.getFeature())){
                            specimenFacts.add(element);
                        }else{
                            simpleFacts.add(element);
                        }
                    }
                }
            }
            if (!commonNameFacts.isEmpty()){
                handleCommonNameFacts(state, taxon, commonNameFacts);
            }
            if (!distributionFacts.isEmpty()){
                handleDistributionFacts(state, taxon, distributionFacts);
            }
            if (!specimenFacts.isEmpty()){
                handleSpecimenFacts(state, taxon, specimenFacts);
            }
            if (!simpleFacts.isEmpty()){
                handleSimpleFacts(state, taxon, simpleFacts);
            }
        } else if (cdmBase instanceof TaxonName){
            TaxonName name = CdmBase.deproxy(cdmBase, TaxonName.class);
            Set<TaxonNameDescription> descriptions = name.getDescriptions();
            List<DescriptionElementBase> simpleFacts = new ArrayList<>();
            for (TaxonNameDescription description: descriptions){
                if (description.getElements() != null){
                    for (DescriptionElementBase element: description.getElements()){
                        if (!element.getFeature().equals(Feature.PROTOLOGUE())){
                            simpleFacts.add(element);
                        }
                    }
                }

             }
            if (!simpleFacts.isEmpty()){
                handleSimpleFacts(state, name, simpleFacts);
            }
        }
    }


    /**
     * @param feature
     * @return
     */
    private boolean isSpecimenFeature(Feature feature) {
        //TODO allow user defined specimen features
        if (feature == null){
            return false;
        }else if (feature.isSupportsIndividualAssociation()){
            return true;
        }else{
            return feature.equals(Feature.SPECIMEN()) || feature.equals(Feature.INDIVIDUALS_ASSOCIATION())
                    || feature.equals(Feature.MATERIALS_EXAMINED()) || feature.equals(Feature.OBSERVATION())
                    || feature.equals(Feature.OCCURRENCE())
                     ;
        }
    }

    /**
     * @param state
     * @param taxon
     * @param simpleFacts
     */
    private void handleSimpleFacts(OutputModelExportState state, CdmBase cdmBase,
            List<DescriptionElementBase> simpleFacts) {
        try {
            OutputModelTable table = OutputModelTable.SIMPLE_FACT;

            for (DescriptionElementBase element: simpleFacts){
                handleSingleSimpleFact(state, cdmBase, table, element);
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling simple facts for " +
                    cdmBaseStr(cdmBase) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param cdmBase
     * @param table
     * @param element
     */
    private void handleSingleSimpleFact(OutputModelExportState state, CdmBase cdmBase, OutputModelTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, OutputModelTable.SIMPLE_FACT);

            if (element instanceof TextData){
               TextData textData = (TextData)element;
               csvLine = new String[table.getSize()];
               csvLine[table.getIndex(OutputModelTable.FACT_ID)] = getId(state, element);
               if (cdmBase instanceof Taxon){
                   csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, cdmBase);
                   csvLine[table.getIndex(OutputModelTable.NAME_FK)] = "";
               }else if (cdmBase instanceof TaxonName){
                   csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = "";
                   csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, cdmBase);
               }
               csvLine[table.getIndex(OutputModelTable.FACT_CATEGORY)] = textData.getFeature().getLabel();

               String mediaUris = "";
               for (Media media: textData.getMedia()){
                   String mediaString = extractMediaUris(media.getRepresentations().iterator());
                   if (!StringUtils.isBlank(mediaString)){
                       mediaUris +=  mediaString + ";";
                   }
                   else{
                       state.getResult().addWarning("Empty Media object for uuid: " +
                               cdmBase.getUuid() + " uuid of media: " + media.getUuid());
                   }
               }
               csvLine[table.getIndex(OutputModelTable.MEDIA_URI)] = mediaUris;
               if (textData.getFeature().equals(Feature.CITATION())){
                  // csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, cdmBase);
                   state.getProcessor().put(table, textData, csvLine, state);
               }else if (!textData.getMultilanguageText().isEmpty()){
                   for (Language language: textData.getMultilanguageText().keySet()){
                       String[] csvLineLanguage = csvLine.clone();
                       LanguageString langString = textData.getLanguageText(language);

                       csvLineLanguage[table.getIndex(OutputModelTable.FACT_TEXT)] = langString.getText();
                       csvLineLanguage[table.getIndex(OutputModelTable.LANGUAGE)] = language.getLabel();
                       state.getProcessor().put(table, textData, csvLineLanguage, state);
                   }
               } else{
                   state.getProcessor().put(table, textData, csvLine, state);
               }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact " +
                    cdmBaseStr(element) + ": " + e.getMessage());
        }
    }


    /**
     * @param state
     * @param specimenFacts
     */
    private void handleSpecimenFacts(OutputModelExportState state, Taxon taxon, List<DescriptionElementBase> specimenFacts) {
        OutputModelTable table = OutputModelTable.SPECIMEN_FACT;

        for (DescriptionElementBase element: specimenFacts){
            try {
                String[] csvLine = new String[table.getSize()];
                csvLine[table.getIndex(OutputModelTable.FACT_ID)] = getId(state, element);
                csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, taxon);
                handleSource(state, element, table);
                csvLine[table.getIndex(OutputModelTable.SPECIMEN_NOTES)] = createAnnotationsString(element.getAnnotations());

                if (element instanceof IndividualsAssociation){

                    IndividualsAssociation indAssociation = (IndividualsAssociation)element;
                    if (state.getSpecimenFromStore(indAssociation.getAssociatedSpecimenOrObservation().getId()) == null){
                        SpecimenOrObservationBase<?> specimenBase = HibernateProxyHelper.deproxy(indAssociation.getAssociatedSpecimenOrObservation());

                        if (specimenBase instanceof DerivedUnit){
                            DerivedUnit derivedUnit = (DerivedUnit)specimenBase;
                            handleSpecimen(state, derivedUnit);
                            csvLine[table.getIndex(OutputModelTable.SPECIMEN_FK)] = getId(state, indAssociation.getAssociatedSpecimenOrObservation());
                        }else{
                            //field units are not supported
                            state.getResult().addError("The associated Specimen of taxon " + taxon.getUuid() + " is not an DerivedUnit. Could not be exported.");
                        }
                    }
                } else if (element instanceof TextData){
                    TextData textData = HibernateProxyHelper.deproxy(element, TextData.class);
                    csvLine[table.getIndex(OutputModelTable.SPECIMEN_DESCRIPTION)] = createMultilanguageString(textData.getMultilanguageText());
                }
                state.getProcessor().put(table, element, csvLine, state);
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single specimen fact " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param multilanguageText
     * @return
     */
    private String createMultilanguageString(Map<Language, LanguageString> multilanguageText) {
       String text = "";
       int index = multilanguageText.size();
       for(LanguageString langString: multilanguageText.values()){
           text += langString.getText();
           if (index > 1){
               text += "; ";
           }
           index --;
       }

        return text;
    }

    /**
     * @param annotations
     * @return
     */
    private String createAnnotationsString(Set<Annotation> annotations) {
        StringBuffer strBuff = new StringBuffer();
        for (Annotation ann:annotations){
            if (ann.getAnnotationType() == null ||!ann.getAnnotationType().equals(AnnotationType.TECHNICAL())){
                strBuff.append(ann.getText());
                strBuff.append("; ");
            }
        }
        return strBuff.toString();
    }

    /**
     * @param state
     * @param taxon
     * @param element
     */
    private void handleSource(OutputModelExportState state, DescriptionElementBase element, OutputModelTable factsTable) {
        OutputModelTable table = OutputModelTable.FACT_SOURCES;
        Set<DescriptionElementSource> sources = element.getSources();
        for (DescriptionElementSource source: sources){
            try {
                String[] csvLine = new  String[table.getSize()];
                Reference ref = source.getCitation();
                if ((ref == null) && (source.getNameUsedInSource() == null)){
                    continue;
                }
                if (ref != null){
                    if (state.getReferenceFromStore(ref.getId()) == null){
                        handleReference(state, ref);
                        csvLine[table.getIndex(OutputModelTable.REFERENCE_FK)] = getId(state, ref);
                    }
                }
                csvLine[table.getIndex(OutputModelTable.FACT_FK)] = getId(state, element);

                csvLine[table.getIndex(OutputModelTable.NAME_IN_SOURCE_FK)] = getId(state, source.getNameUsedInSource());
                csvLine[table.getIndex(OutputModelTable.FACT_TYPE)] = factsTable.getTableName();
                if ( StringUtils.isBlank(csvLine[table.getIndex(OutputModelTable.REFERENCE_FK)])  && StringUtils.isBlank(csvLine[table.getIndex(OutputModelTable.NAME_IN_SOURCE_FK)])){
                    continue;
                }
                state.getProcessor().put(table, source, csvLine, state);
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single source " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }

    }

    /**
     * @param state
     * @param distributionFacts
     */
    private void handleDistributionFacts(OutputModelExportState state, Taxon taxon, List<DescriptionElementBase> distributionFacts) {
        OutputModelTable table = OutputModelTable.GEOGRAPHIC_AREA_FACT;

        for (DescriptionElementBase element: distributionFacts){
            try {
                if (element instanceof Distribution){
                    String[] csvLine = new  String[table.getSize()];
                    Distribution distribution = (Distribution)element;
                    csvLine[table.getIndex(OutputModelTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, taxon);
                    if (distribution.getArea() != null){
                        csvLine[table.getIndex(OutputModelTable.AREA_LABEL)] = distribution.getArea().getLabel();
                    }
                    if (distribution.getStatus() != null){
                        csvLine[table.getIndex(OutputModelTable.STATUS_LABEL)] = distribution.getStatus().getLabel();
                    }
                    state.getProcessor().put(table, distribution, csvLine, state);
                } else{
                    state.getResult().addError("The distribution description for the taxon " + taxon.getUuid() + " is not of type distribution. Could not be exported. UUID of the description element: " + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single distribution " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param state
     * @param commonNameFacts
     */
    private void handleCommonNameFacts(OutputModelExportState state, Taxon taxon, List<DescriptionElementBase> commonNameFacts) {
        OutputModelTable table = OutputModelTable.COMMON_NAME_FACT;

        for (DescriptionElementBase element: commonNameFacts){
            try {
                if (element instanceof CommonTaxonName){
                    String[] csvLine = new  String[table.getSize()];
                    CommonTaxonName commonName = (CommonTaxonName)element;
                    csvLine[table.getIndex(OutputModelTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, taxon);
                    if (commonName.getName() != null){csvLine[table.getIndex(OutputModelTable.FACT_TEXT)] = commonName.getName();}
                    if (commonName.getLanguage() != null){csvLine[table.getIndex(OutputModelTable.LANGUAGE)] = commonName.getLanguage().getLabel();}
                    if (commonName.getArea() != null){ csvLine[table.getIndex(OutputModelTable.AREA_LABEL)] = commonName.getArea().getLabel();}
                    state.getProcessor().put(table, commonName, csvLine, state);
                } else{
                    state.getResult().addError("The distribution description for the taxon " + taxon.getUuid() + " is not of type distribution. Could not be exported. UUID of the description element: " + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single common name " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param sec
     * @return
     */
    private String getTitleCache(IIdentifiableEntity identEntity) {
        if (identEntity == null){
            return "";
        }
        //TODO refresh?
        return identEntity.getTitleCache();
    }

    /**
     * @param state
     * @param taxon
     * @return
     */
    private String getId(OutputModelExportState state, ICdmBase cdmBase) {
        if (cdmBase == null){
            return "";
        }
        //TODO make configurable
        return cdmBase.getUuid().toString();
    }

    /**
     * @param state
     * @param syn
     */
    private void handleSynonym(OutputModelExportState state, Synonym syn) {
       try {
           TaxonName name = syn.getName();
           handleName(state, name);

           OutputModelTable table = OutputModelTable.SYNONYM;
           String[] csvLine = new String[table.getSize()];

           csvLine[table.getIndex(OutputModelTable.SYNONYM_ID)] = getId(state, syn);
           csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, syn.getAcceptedTaxon());
           csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, name);
           csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE_FK)] = getId(state, syn.getSec());
           csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE)] = getTitleCache(syn.getSec());

           state.getProcessor().put(table, syn, csvLine, state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym " +
                    cdmBaseStr(syn) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param name
     */
    private void handleName(OutputModelExportState state, TaxonName name) {
        try {
            Rank rank = name.getRank();
            OutputModelTable table = OutputModelTable.SCIENTIFIC_NAME;
            name = HibernateProxyHelper.deproxy(name);
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(OutputModelTable.NAME_ID)] = getId(state, name);
            if (name.getLsid() != null){
                csvLine[table.getIndex(OutputModelTable.LSID)] = name.getLsid().getLsid();
            }else{
                csvLine[table.getIndex(OutputModelTable.LSID)] = "";
            }

            handleIdentifier(state, name, csvLine, table);
            handleDescriptions(state, name);

            csvLine[table.getIndex(OutputModelTable.RANK)] = getTitleCache(rank);
            if (rank != null){
                csvLine[table.getIndex(OutputModelTable.RANK_SEQUENCE)] = String.valueOf(rank.getOrderIndex());
                if (rank.isInfraGeneric()){
                    try {
                        csvLine[table.getIndex(OutputModelTable.INFRAGENERIC_RANK)] = name.getRank().getInfraGenericMarker();
                    } catch (UnknownCdmTypeException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (rank.isInfraSpecific()){
                    csvLine[table.getIndex(OutputModelTable.INFRASPECIFIC_RANK)] = name.getRank().getAbbreviation();
                }
            }else{
                csvLine[table.getIndex(OutputModelTable.RANK_SEQUENCE)] = "";
            }
            if (name.isProtectedTitleCache()){
                csvLine[table.getIndex(OutputModelTable.FULL_NAME_WITH_AUTHORS)] =name.getTitleCache();
            }else{
                //TODO: adapt the tropicos titlecache creation
                csvLine[table.getIndex(OutputModelTable.FULL_NAME_WITH_AUTHORS)] = name.getTitleCache();
            }
            csvLine[table.getIndex(OutputModelTable.FULL_NAME_NO_AUTHORS)] = name.getNameCache();
            csvLine[table.getIndex(OutputModelTable.GENUS_UNINOMIAL)] = name.getGenusOrUninomial();

            csvLine[table.getIndex(OutputModelTable.INFRAGENERIC_EPITHET)] = name.getInfraGenericEpithet();
            csvLine[table.getIndex(OutputModelTable.SPECIFIC_EPITHET)] = name.getSpecificEpithet();

            csvLine[table.getIndex(OutputModelTable.INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();
            csvLine[table.getIndex(OutputModelTable.BAS_AUTHORTEAM_FK)] = getId(state,name.getBasionymAuthorship());
            if (name.getBasionymAuthorship() != null){
                if (state.getAuthorFromStore(name.getBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getBasionymAuthorship());
                }
            }
            csvLine[table.getIndex(OutputModelTable.BAS_EX_AUTHORTEAM_FK)] = getId(state, name.getExBasionymAuthorship());
            if (name.getExBasionymAuthorship() != null){
                if (state.getAuthorFromStore(name.getExBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExBasionymAuthorship());
                }

            }
            csvLine[table.getIndex(OutputModelTable.COMB_AUTHORTEAM_FK)] = getId(state,name.getCombinationAuthorship());
            if (name.getCombinationAuthorship() != null){
                if (state.getAuthorFromStore(name.getCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getCombinationAuthorship());
                }
            }
            csvLine[table.getIndex(OutputModelTable.COMB_EX_AUTHORTEAM_FK)] = getId(state, name.getExCombinationAuthorship());
            if (name.getExCombinationAuthorship() != null){
                if (state.getAuthorFromStore(name.getExCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExCombinationAuthorship());
                }

            }

            csvLine[table.getIndex(OutputModelTable.AUTHOR_TEAM_STRING)] = name.getAuthorshipCache();

            Reference nomRef = (Reference)name.getNomenclaturalReference();

            if (nomRef != null){
                if (state.getReferenceFromStore(nomRef.getId()) == null){
                    handleReference(state, nomRef);
                }
                csvLine[table.getIndex(OutputModelTable.REFERENCE_FK)] = getId(state, nomRef);
                csvLine[table.getIndex(OutputModelTable.PUBLICATION_TYPE)] = nomRef.getType().name();
                if (nomRef.getVolume() != null){
                    csvLine[table.getIndex(OutputModelTable.VOLUME_ISSUE)] = nomRef.getVolume();
                }
                if (nomRef.getDatePublished() != null){
                    csvLine[table.getIndex(OutputModelTable.DATE_PUBLISHED)] = nomRef.getDatePublishedString();
                    csvLine[table.getIndex(OutputModelTable.YEAR_PUBLISHED)] = nomRef.getDatePublished().getYear();
                }
                if (name.getNomenclaturalMicroReference() != null){
                    csvLine[table.getIndex(OutputModelTable.DETAIL)] = name.getNomenclaturalMicroReference();
                }
                nomRef = HibernateProxyHelper.deproxy(nomRef);
                if (nomRef.getInReference() != null){
                    Reference inReference = nomRef.getInReference();
                    if (inReference.getInReference() != null){
                        inReference = inReference.getInReference();
                    }
                    if (inReference.getAbbrevTitle() == null){
                        csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = CdmUtils.Nz(inReference.getAbbrevTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = CdmUtils.Nz(inReference.getAbbrevTitle());
                    }
                    if (inReference.getTitle() == null){
                        csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitle());
                    }


                    TeamOrPersonBase<?> author = inReference.getAuthorship();
                    if (author != null && (nomRef.isOfType(ReferenceType.BookSection) || nomRef.isOfType(ReferenceType.Section))){
                        csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(OutputModelTable.FULL_REF_AUTHOR)] = CdmUtils.Nz(author.getTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(OutputModelTable.FULL_REF_AUTHOR)] = "";
                    }
                }else{
                    if (nomRef.getAbbrevTitle() == null){
                        csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = CdmUtils.Nz(nomRef.getAbbrevTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = CdmUtils.Nz(nomRef.getAbbrevTitle());
                    }
                    if (nomRef.getTitle() == null){
                        csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = CdmUtils.Nz(nomRef.getTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = CdmUtils.Nz(nomRef.getTitle());
                    }
                    TeamOrPersonBase<?> author = nomRef.getAuthorship();
                    if (author != null ){
                        csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(OutputModelTable.FULL_REF_AUTHOR)] = CdmUtils.Nz(author.getTitleCache());
                    }else{
                        csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(OutputModelTable.FULL_REF_AUTHOR)] = "";
                    }

                }
            }else{
                csvLine[table.getIndex(OutputModelTable.PUBLICATION_TYPE)] = "";
            }



            /*
            * Collation

            Detail


            TitlePageYear
            */
            Set<TaxonNameDescription> descriptions = name.getDescriptions();
            String protologueUriString = extractURIs(state, descriptions, Feature.PROTOLOGUE());

            csvLine[table.getIndex(OutputModelTable.PROTOLOGUE_URI)] = protologueUriString;

            if (name.getStatus() == null || name.getStatus().isEmpty()){
                csvLine[table.getIndex(OutputModelTable.NOM_STATUS)] = "";
                csvLine[table.getIndex(OutputModelTable.NOM_STATUS_ABBREV)] = "";
            }else{

                String statusStringAbbrev = extractStatusString(state, name, true);
                String statusString = extractStatusString(state, name, false);

                csvLine[table.getIndex(OutputModelTable.NOM_STATUS)] = statusString.trim();
                csvLine[table.getIndex(OutputModelTable.NOM_STATUS_ABBREV)] = statusStringAbbrev.trim();
            }

            HomotypicalGroup group =name.getHomotypicalGroup();

            if (state.getHomotypicalGroupFromStore(group.getId()) == null){
                handleHomotypicalGroup(state, group);
            }
            csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_FK)] = getId(state, group);
            List<TaxonName> typifiedNames = new ArrayList<>();
            typifiedNames.addAll(group.getTypifiedNames());
            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(null, true));
            Integer  seqNumber= typifiedNames.indexOf(name);
            csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_SEQ)] = String.valueOf(seqNumber);
            state.getProcessor().put(table, name, csvLine, state);

            /*
             *
            Tropicos_ID
            IPNI_ID


            InfragenericRank


            InfraspecificRank
            Collation
            Volume (Issue)
            Detail
            DatePublished
            YearPublished
            TitlePageYear



            HomotypicGroupSequenceNumber


             *
             */
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym " +
                    cdmBaseStr(name) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param name
     */
    private void handleIdentifier(OutputModelExportState state, TaxonName name, String[] csvLine, OutputModelTable table) {
        try {
            Set<String>  IPNIidentifiers = name.getIdentifiers(DefinedTerm.IPNI_NAME_IDENTIFIER());
            Set<String>  tropicosIdentifiers = name.getIdentifiers(DefinedTerm.TROPICOS_NAME_IDENTIFIER());
            Set<String>  WFOIdentifiers = name.getIdentifiers(DefinedTerm.uuidWfoNameIdentifier);
            if (!IPNIidentifiers.isEmpty()){
                extractIdentifier(IPNIidentifiers, csvLine, table.getIndex(OutputModelTable.IPNI_ID));
            }
            if (!tropicosIdentifiers.isEmpty()){
                extractIdentifier(tropicosIdentifiers, csvLine, table.getIndex(OutputModelTable.TROPICOS_ID));
            }
            if (!WFOIdentifiers.isEmpty()){
                extractIdentifier(WFOIdentifiers,  csvLine, table.getIndex(OutputModelTable.WFO_ID));
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling identifiers for " +
                    cdmBaseStr(name) + ": " + e.getMessage());

        }
    }

    /**
     * @param tropicosIdentifiers
     */
    private void extractIdentifier(Set<String> identifierSet, String[] csvLine, int index) {
        String identifierString = "";
        for (String identifier: identifierSet){
            if (!StringUtils.isBlank(identifierString)){
                identifierString += ", ";
            }
            identifierString += identifier;
        }

        csvLine[index] = identifierString;
    }

    /**
     * @param state
     * @param descriptions
     * @return
     */
    private String extractURIs(OutputModelExportState state,
            Set<? extends DescriptionBase<?>> descriptionsSet, Feature feature) {
        String mediaUriString = "";
        SpecimenDescription specimenDescription;
        TaxonDescription taxonDescription;
        TaxonNameDescription nameDescription;
        Set<DescriptionElementBase> elements = new HashSet<>();
        for (DescriptionBase<?> description : descriptionsSet){
            try {
                if (!description.getElements().isEmpty()){
                    if (description instanceof SpecimenDescription){
                        specimenDescription = (SpecimenDescription)description;
                        elements = specimenDescription.getElements();
                    }else if (description instanceof TaxonDescription){
                        taxonDescription = (TaxonDescription) description;
                        elements = taxonDescription.getElements();
                    } else if (description instanceof TaxonNameDescription){
                        nameDescription = (TaxonNameDescription) description;
                        elements = nameDescription.getElements();
                    }

                    for (DescriptionElementBase element : elements){
                        Feature entityFeature = HibernateProxyHelper.deproxy(element.getFeature());
                        if (entityFeature.equals(feature)){
                            if (!element.getMedia().isEmpty()){
                                List<Media> media = element.getMedia();
                                for (Media mediaElement: media){
                                    Iterator<MediaRepresentation> it =  mediaElement.getRepresentations().iterator();
                                    mediaUriString = extractMediaUris(it);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when extracting media URIs for " +
                        cdmBaseStr(description) + ": " + e.getMessage());
            }
        }
        return mediaUriString;
    }

    /**
     * @param state
     * @param basionymAuthorship
     */
    private void handleAuthor(OutputModelExportState state, TeamOrPersonBase<?> author) {
        try {
            if (state.getAuthorFromStore(author.getId()) != null){
                return;
            }
            state.addAuthorToStore(author);
            OutputModelTable table = OutputModelTable.NOMENCLATURAL_AUTHOR;
            String[] csvLine = new String[table.getSize()];
            OutputModelTable tableAuthorRel = OutputModelTable.NOMENCLATURAL_AUTHOR_TEAM_RELATION;
            String[] csvLineRel = new String[tableAuthorRel.getSize()];
            String[] csvLineMember = new String[table.getSize()];
            csvLine[table.getIndex(OutputModelTable.AUTHOR_ID)] = getId(state, author);
            csvLine[table.getIndex(OutputModelTable.ABBREV_AUTHOR)] = author.getNomenclaturalTitle();
            csvLine[table.getIndex(OutputModelTable.AUTHOR_TITLE)] = author.getTitleCache();
            author = HibernateProxyHelper.deproxy(author);
            if (author instanceof Person){
                Person authorPerson = (Person)author;
                csvLine[table.getIndex(OutputModelTable.AUTHOR_FIRST_NAME)] = authorPerson.getFirstname();
                csvLine[table.getIndex(OutputModelTable.AUTHOR_LASTNAME)] = authorPerson.getLastname();
                csvLine[table.getIndex(OutputModelTable.AUTHOR_PREFIX)] = authorPerson.getPrefix();
                csvLine[table.getIndex(OutputModelTable.AUTHOR_SUFFIX)] = authorPerson.getSuffix();
            } else{
                // create an entry in rel table and all members in author table, check whether the team members already in author table

                Team authorTeam = (Team)author;
                int index = 0;
                for (Person member: authorTeam.getTeamMembers()){
                    csvLineRel = new String[tableAuthorRel.getSize()];
                    csvLineRel[tableAuthorRel.getIndex(OutputModelTable.AUTHOR_TEAM_FK)] = getId(state, authorTeam);
                    csvLineRel[tableAuthorRel.getIndex(OutputModelTable.AUTHOR_FK)] = getId(state, member);
                    csvLineRel[tableAuthorRel.getIndex(OutputModelTable.AUTHOR_TEAM_SEQ_NUMBER)] = String.valueOf(index);
                    state.getProcessor().put(tableAuthorRel, authorTeam.getId() +":" +member.getId(), csvLineRel, state);

                    if (state.getAuthorFromStore(member.getId()) == null){
                        state.addAuthorToStore(member);
                        csvLineMember = new String[table.getSize()];
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_ID)] = getId(state, member);
                        csvLineMember[table.getIndex(OutputModelTable.ABBREV_AUTHOR)] = member.getNomenclaturalTitle();
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_TITLE)] = member.getTitleCache();
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_FIRST_NAME)] = member.getFirstname();
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_LASTNAME)] = member.getLastname();
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_PREFIX)] = member.getPrefix();
                        csvLineMember[table.getIndex(OutputModelTable.AUTHOR_SUFFIX)] = member.getSuffix();
                        state.getProcessor().put(table, member, csvLineMember, state);
                    }
                    index++;

                }
            }
            state.getProcessor().put(table, author, csvLine, state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling author " +
                    cdmBaseStr(author) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param name
     * @param statusString
     * @return
     */
    private String extractStatusString(OutputModelExportState state, TaxonName name, boolean abbrev) {
        try {
            Set<NomenclaturalStatus> status = name.getStatus();
            if (status.isEmpty()){
                return "";
            }
            String statusString = "";
            for (NomenclaturalStatus nameStatus: status){
                if (nameStatus != null){
                    if (abbrev){
                        if (nameStatus.getType() != null){
                            statusString += nameStatus.getType().getIdInVocabulary();
                        }
                    }else{
                        if (nameStatus.getType() != null){
                            statusString += nameStatus.getType().getTitleCache();
                        }
                    }
                    if (!abbrev){

                        if (nameStatus.getRuleConsidered() != null && !StringUtils.isBlank(nameStatus.getRuleConsidered())){
                            statusString += " " + nameStatus.getRuleConsidered();
                        }
                        if (nameStatus.getCitation() != null){
                            statusString += " " + nameStatus.getCitation().getTitleCache();
                        }
                        if (nameStatus.getCitationMicroReference() != null && !StringUtils.isBlank(nameStatus.getCitationMicroReference())){
                            statusString += " " + nameStatus.getCitationMicroReference();
                        }
                    }
                    statusString += " ";
                }
            }
            return statusString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when extracting status string for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * @param group
     */
    private void handleHomotypicalGroup(OutputModelExportState state, HomotypicalGroup group) {
        try {
            state.addHomotypicalGroupToStore(group);
            OutputModelTable table = OutputModelTable.HOMOTYPIC_GROUP;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_ID)] = getId(state, group);
            List<TaxonName> typifiedNames = new ArrayList<>();
            typifiedNames.addAll(group.getTypifiedNames());
            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(null, true));
            String typifiedNamesString = "";
            for (TaxonName name: typifiedNames){
                //Concatenated output string for homotypic group (names and citations) + status + some name relations (e.g. “non”)
                //TODO: nameRelations, which and how to display


                typifiedNamesString += name.getTitleCache()+ extractStatusString(state, name, true) + "; ";
            }
            typifiedNamesString = typifiedNamesString.substring(0, typifiedNamesString.length()-2);
            if (typifiedNamesString != null){
                csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_STRING)] = typifiedNamesString.trim();
            }else{
                csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_STRING)] = "";
            }
            Set<TypeDesignationBase> typeDesigantions = group.getTypeDesignations();
            List<TypeDesignationBase> designationList = new ArrayList<>();
            designationList.addAll(typeDesigantions);
            Collections.sort(designationList, new TypeComparator());
            StringBuffer typeDesignationString = new StringBuffer();
            for (TypeDesignationBase typeDesignation: typeDesigantions){
                if (typeDesignation != null && typeDesignation.getTypeStatus() != null){
                    typeDesignationString.append(typeDesignation.getTypeStatus().getTitleCache() + ": ");
                }
                if (typeDesignation instanceof SpecimenTypeDesignation){
                    if (((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen() != null){
                        typeDesignationString.append(((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen().getTitleCache());
                        handleSpecimen(state, ((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen());
                    }
                }else{
                    if (((NameTypeDesignation)typeDesignation).getTypeName() != null){
                        typeDesignationString.append(((NameTypeDesignation)typeDesignation).getTypeName().getTitleCache());
                    }
                }
                if(typeDesignation.getCitation() != null ){
                    typeDesignationString.append(", "+typeDesignation.getCitation().getTitleCache());
                }
                //TODO...
                /*
                 * Sortierung:
                1.  Status der Typen: a) holo, lecto, neo, syn, b) epi, paralecto, c) para (wenn überhaupt) – die jeweiligen iso immer direct mit dazu
                2.  Land
                3.  Sammler
                4.  Nummer

                Aufbau der Typusinformationen:
                Land: Lokalität mit Höhe und Koordinaten; Datum; Sammler Nummer (Herbar/Barcode, Typusart; Herbar/Barcode, Typusart …)

                 */
            }
            String typeDesignations = typeDesignationString.toString();
            if (typeDesignations != null){
                csvLine[table.getIndex(OutputModelTable.TYPE_STRING)] = typeDesignations;
            }else{
                csvLine[table.getIndex(OutputModelTable.TYPE_STRING)] = "";
            }
            state.getProcessor().put(table, String.valueOf(group.getId()), csvLine, state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling homotypic group " +
                    cdmBaseStr(group) + ": " + e.getMessage());
        }
    }

    /**
     * @param name
     * @return
     */
    private String getTropicosTitleCache(OutputModelExportState state, TaxonName name) {
        try {
            String basionymStart = "(";
            String basionymEnd = ") ";
            String exAuthorSeperator = " ex ";
            TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
            TeamOrPersonBase<?> exCombinationAuthor = name.getExCombinationAuthorship();
            TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
            TeamOrPersonBase<?> exBasionymAuthor = name.getExBasionymAuthorship();

            String combinationAuthorString = "";
            if (combinationAuthor != null){
                combinationAuthor = HibernateProxyHelper.deproxy(combinationAuthor);
                if (combinationAuthor instanceof Team){
                    combinationAuthorString = createTropicosTeamTitle(combinationAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(combinationAuthor, Person.class);
                    combinationAuthorString = createTropicosAuthorString(person);
                }
            }
            String exCombinationAuthorString = "";
            if (exCombinationAuthor != null){
                exCombinationAuthor = HibernateProxyHelper.deproxy(exCombinationAuthor);
                if (exCombinationAuthor instanceof Team){
                   exCombinationAuthorString = createTropicosTeamTitle(exCombinationAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(exCombinationAuthor, Person.class);
                    exCombinationAuthorString = createTropicosAuthorString(person);
                }
            }

            String basionymAuthorString = "";
            if (basionymAuthor != null){
                basionymAuthor = HibernateProxyHelper.deproxy(basionymAuthor);
                if (basionymAuthor instanceof Team){
                    basionymAuthorString =  createTropicosTeamTitle(basionymAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(basionymAuthor, Person.class);
                    basionymAuthorString = createTropicosAuthorString(person);
                }
            }

            String exBasionymAuthorString = "";

            if (exBasionymAuthor != null){
                exBasionymAuthor = HibernateProxyHelper.deproxy(exBasionymAuthor);
                if (exBasionymAuthor instanceof Team){
                    exBasionymAuthorString = createTropicosTeamTitle(exBasionymAuthor);

                }else{
                    Person person = HibernateProxyHelper.deproxy(exBasionymAuthor, Person.class);
                    exBasionymAuthorString = createTropicosAuthorString(person);
                }
            }
            String completeAuthorString =  name.getNameCache() + " ";

            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString) || !CdmUtils.isBlank(basionymAuthorString)) ? basionymStart: "";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString)) ? (CdmUtils.Nz(exBasionymAuthorString) + exAuthorSeperator): "" ;
            completeAuthorString += (!CdmUtils.isBlank(basionymAuthorString))? CdmUtils.Nz(basionymAuthorString):"";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString) || !CdmUtils.isBlank(basionymAuthorString)) ?  basionymEnd:"";
            completeAuthorString += (!CdmUtils.isBlank(exCombinationAuthorString)) ? (CdmUtils.Nz(exCombinationAuthorString) + exAuthorSeperator): "" ;
            completeAuthorString += (!CdmUtils.isBlank(combinationAuthorString))? CdmUtils.Nz(combinationAuthorString):"";


            return completeAuthorString;
        } catch (ClassCastException e) {
            state.getResult().addException(e, "An unexpected error occurred when handling tropicos title cache for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * @param combinationAuthor
     * @return
     */
    private String createTropicosTeamTitle(TeamOrPersonBase<?> combinationAuthor) {
        String combinationAuthorString;
        Team team = HibernateProxyHelper.deproxy(combinationAuthor, Team.class);
        Team tempTeam = Team.NewInstance();
        for (Person teamMember:team.getTeamMembers()){
            combinationAuthorString = createTropicosAuthorString(teamMember);
            Person tempPerson = Person.NewTitledInstance(combinationAuthorString);
            tempTeam.addTeamMember(tempPerson);
        }
        combinationAuthorString = tempTeam.generateTitle();
        return combinationAuthorString;
    }

    /**
     * @param teamMember
     */
    private String createTropicosAuthorString(Person teamMember) {
        String nomAuthorString = "";
        String[] splittedAuthorString = null;
        if (teamMember == null){
            return nomAuthorString;
        }

        if (teamMember.getFirstname() != null){
            String firstNameString = teamMember.getFirstname().replaceAll("\\.", "\\. ");
            splittedAuthorString = firstNameString.split("\\s");
            for (String split: splittedAuthorString){
                if (!StringUtils.isBlank(split)){
                    nomAuthorString += split.substring(0, 1);
                    nomAuthorString += ".";
                }
            }
        }
        if (teamMember.getLastname() != null){
            String lastNameString = teamMember.getLastname().replaceAll("\\.", "\\. ");
            splittedAuthorString = lastNameString.split("\\s");
            for (String split: splittedAuthorString){
                nomAuthorString += " " +split;
            }
        }
        if (StringUtils.isBlank(nomAuthorString.trim())){
            if (teamMember.getTitleCache() != null) {
                String titleCacheString = teamMember.getTitleCache().replaceAll("\\.", "\\. ");
                splittedAuthorString = titleCacheString.split("\\s");
            }


            int index = 0;
            for (String split: splittedAuthorString){
                if ( index < splittedAuthorString.length-1 && (split.length()==1 || split.endsWith("."))){
                    nomAuthorString += split;
                }else{
                    nomAuthorString = nomAuthorString +" "+ split;
                }
                index++;
            }
        }
        return nomAuthorString.trim();
    }

    /**
     * @param state
     * @param name
     */
    private void handleReference(OutputModelExportState state, Reference reference) {
        try {
            state.addReferenceToStore(reference);
            OutputModelTable table = OutputModelTable.REFERENCE;

            String[] csvLine = new String[table.getSize()];
            csvLine[table.getIndex(OutputModelTable.REFERENCE_ID)] = getId(state, reference);
            //TODO short citations correctly
            String shortCitation = createShortCitation(reference);  //Should be Author(year) like in Taxon.sec
            csvLine[table.getIndex(OutputModelTable.BIBLIO_SHORT_CITATION)] = shortCitation;
            //TODO get preferred title
            csvLine[table.getIndex(OutputModelTable.REF_TITLE)] = reference.getTitle();
            csvLine[table.getIndex(OutputModelTable.ABBREV_REF_TITLE)] = reference.getTitle();
            csvLine[table.getIndex(OutputModelTable.DATE_PUBLISHED)] = reference.getDatePublishedString();
            //TBC
            csvLine[table.getIndex(OutputModelTable.EDITION)] = reference.getEdition();
            csvLine[table.getIndex(OutputModelTable.EDITOR)] = reference.getEditor();
            csvLine[table.getIndex(OutputModelTable.ISBN)] = reference.getIsbn();
            csvLine[table.getIndex(OutputModelTable.ISSN)] = reference.getIssn();
            csvLine[table.getIndex(OutputModelTable.ORGANISATION)] = reference.getOrganization();
            csvLine[table.getIndex(OutputModelTable.PAGES)] = reference.getPages();
            csvLine[table.getIndex(OutputModelTable.PLACE_PUBLISHED)] = reference.getPlacePublished();
            csvLine[table.getIndex(OutputModelTable.PUBLISHER)] = reference.getPublisher();
            csvLine[table.getIndex(OutputModelTable.REF_ABSTRACT)] = reference.getReferenceAbstract();
            csvLine[table.getIndex(OutputModelTable.SERIES_PART)] = reference.getSeriesPart();
            csvLine[table.getIndex(OutputModelTable.VOLUME)] = reference.getVolume();
            csvLine[table.getIndex(OutputModelTable.YEAR)] = reference.getYear();
            if ( reference.getAuthorship() != null){
                csvLine[table.getIndex(OutputModelTable.AUTHORSHIP_TITLE)] = reference.getAuthorship().getTitleCache();
                csvLine[table.getIndex(OutputModelTable.AUTHOR_FK)] = getId(state,reference.getAuthorship());
            }

            csvLine[table.getIndex(OutputModelTable.IN_REFERENCE)] = getId(state, reference.getInReference());
            if (reference.getInReference() != null && state.getReferenceFromStore(reference.getInReference().getId()) == null){
                handleReference(state, reference.getInReference());
            }
            if ( reference.getInstitution() != null){ csvLine[table.getIndex(OutputModelTable.INSTITUTION)] = reference.getInstitution().getTitleCache();}
            if ( reference.getLsid() != null){ csvLine[table.getIndex(OutputModelTable.LSID)] = reference.getLsid().getLsid();}
            if ( reference.getSchool() != null){ csvLine[table.getIndex(OutputModelTable.SCHOOL)] = reference.getSchool().getTitleCache();}
            if ( reference.getUri() != null){ csvLine[table.getIndex(OutputModelTable.URI)] = reference.getUri().toString();}
            csvLine[table.getIndex(OutputModelTable.REF_TYPE)] = reference.getType().getKey();

            state.getProcessor().put(table, reference, csvLine, state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling reference " +
                    cdmBaseStr(reference) + ": " + e.getMessage());
        }

    }


    /**
     * @param reference
     * @return
     */
    private String createShortCitation(Reference reference) {
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String shortCitation = "";
        if (authorship == null) {
            return null;
        }
        authorship = HibernateProxyHelper.deproxy(authorship);
        if (authorship instanceof Person){ shortCitation = ((Person)authorship).getLastname();}
        else if (authorship instanceof Team){

            Team authorTeam = HibernateProxyHelper.deproxy(authorship, Team.class);
            int index = 0;

            for (Person teamMember : authorTeam.getTeamMembers()){
                index++;
                String concat = concatString(authorTeam, authorTeam.getTeamMembers(), index);
                shortCitation += concat + teamMember.getLastname();
            }

        }
        if (reference.getYear() != null){
            shortCitation = shortCitation + "(" + reference.getYear() + ")";
        }
        return shortCitation;
    }

    public static String concatString(Team team, List<Person> teamMembers, int i) {
        String concat;
        if (i <= 1){
            concat = "";
        }else if (i < teamMembers.size() || ( team.isHasMoreMembers() && i == teamMembers.size())){
            concat = STD_TEAM_CONCATINATION;
        }else{
            concat = FINAL_TEAM_CONCATINATION;
        }
        return concat;
    }

    /*
     * TypeDesignation table
     * Specimen_Fk
     *  EditName_Fk
     *   TypeVerbatimCitation
     *   TypeCategory
     *   TypeDesignatedByString
     *   TypeDesignatedByRef_Fk
     */

    private void handleSpecimenTypeDesignations(OutputModelExportState state, TaxonName name){
       try {
           Set<SpecimenTypeDesignation> typeDesignations = name.getSpecimenTypeDesignations();
           OutputModelTable table = OutputModelTable.TYPE_DESIGNATION;
           String nameId = getId(state, name);
           String[] csvLine = new String[table.getSize()];
            for (SpecimenTypeDesignation specimenType: typeDesignations){
                csvLine = new String[table.getSize()];
                DerivedUnit specimen = specimenType.getTypeSpecimen();
                if (state.getSpecimenFromStore(specimen.getId()) == null){
                    handleSpecimen(state, specimen);
                }
                csvLine[table.getIndex(OutputModelTable.SPECIMEN_FK)] = getId(state, specimenType.getTypeSpecimen());
                csvLine[table.getIndex(OutputModelTable.NAME_FK)] = nameId;
                csvLine[table.getIndex(OutputModelTable.TYPE_VERBATIM_CITATION)] = specimenType.getTypeSpecimen().generateTitle();
                //TODO: add link to existing Vorcabulary
                csvLine[table.getIndex(OutputModelTable.TYPE_CATEGORY)] = "";
                csvLine[table.getIndex(OutputModelTable.TYPE_DESIGNATED_BY_STRING)] = specimenType.getCitation().getTitleCache();
                csvLine[table.getIndex(OutputModelTable.TYPE_DESIGNATED_BY_REF_FK)] = getId(state, specimenType.getCitation());
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling specimen type designations for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param specimen
     */
    private void handleSpecimen(OutputModelExportState state, DerivedUnit specimen) {
        try {
            state.addSpecimenToStore(specimen);
            OutputModelTable table = OutputModelTable.SPECIMEN;
            String specimenId = getId(state, specimen);
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(OutputModelTable.SPECIMEN_ID)] = specimenId;
            csvLine[table.getIndex(OutputModelTable.SPECIMEN_CITATION)] = specimen.getTitleCache();
            csvLine[table.getIndex(OutputModelTable.SPECIMEN_IMAGE_URIS)] = extractURIs(state, specimen.getDescriptions(), Feature.IMAGE());
            if (specimen.getCollection() != null){ csvLine[table.getIndex(OutputModelTable.HERBARIUM_ABBREV)] = specimen.getCollection().getCode();}
            if (specimen instanceof MediaSpecimen){
                MediaSpecimen mediaSpecimen = (MediaSpecimen) specimen;
                Iterator<MediaRepresentation> it = mediaSpecimen.getMediaSpecimen().getRepresentations().iterator();
                String mediaUris = extractMediaUris(it);
                csvLine[table.getIndex(OutputModelTable.MEDIA_SPECIMEN_URL)] = mediaUris;

            }

            if (specimen.getDerivedFrom() != null){
                for (SpecimenOrObservationBase<?> original: specimen.getDerivedFrom().getOriginals()){
                    //TODO: What to do if there are more then one FieldUnit??
                    if (original instanceof FieldUnit){
                        FieldUnit fieldUnit = (FieldUnit)original;
                        csvLine[table.getIndex(OutputModelTable.COLLECTOR_NUMBER)] = fieldUnit.getFieldNumber();

                        GatheringEvent gathering = fieldUnit.getGatheringEvent();
                        if (gathering != null){
                            if (gathering.getLocality() != null){ csvLine[table.getIndex(OutputModelTable.LOCALITY)] = gathering.getLocality().getLanguageLabel();}
                            if (gathering.getCountry() != null){csvLine[table.getIndex(OutputModelTable.COUNTRY)] = gathering.getCountry().getLabel();}
                            csvLine[table.getIndex(OutputModelTable.COLLECTOR_STRING)] = createCollectorString(state, gathering, fieldUnit);
                            addCollectingAreas(state, gathering);
                            if (gathering.getGatheringDate() != null){csvLine[table.getIndex(OutputModelTable.COLLECTION_DATE)] = gathering.getGatheringDate().toString();}
                            if (!gathering.getCollectingAreas().isEmpty()){
                                int index = 0;
                                csvLine[table.getIndex(OutputModelTable.FURTHER_AREAS)] = "0";
                                for (NamedArea area: gathering.getCollectingAreas()){
                                    if (index == 0){
                                        csvLine[table.getIndex(OutputModelTable.AREA_CATEGORY1)] = area.getTermType().getKey();
                                        csvLine[table.getIndex(OutputModelTable.AREA_NAME1)] = area.getLabel();
                                    }
                                    if (index == 1){
                                        csvLine[table.getIndex(OutputModelTable.AREA_CATEGORY2)] = area.getTermType().getKey();
                                        csvLine[table.getIndex(OutputModelTable.AREA_NAME2)] = area.getLabel();
                                    }
                                    if (index == 2){
                                        csvLine[table.getIndex(OutputModelTable.AREA_CATEGORY3)] = area.getTermType().getKey();
                                        csvLine[table.getIndex(OutputModelTable.AREA_NAME3)] = area.getLabel();
                                    }
                                    if (index == 3){
                                        csvLine[table.getIndex(OutputModelTable.FURTHER_AREAS)] = "1";
                                        break;
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                }
            }

            state.getProcessor().put(table, specimen, csvLine, state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling specimen " +
                    cdmBaseStr(specimen) + ": " + e.getMessage());
        }
    }

    /**
     * @param it
     */
    private String extractMediaUris(Iterator<MediaRepresentation> it) {
        String mediaUriString = "";
        boolean first = true;
        while(it.hasNext()){
            MediaRepresentation rep = it.next();
            List<MediaRepresentationPart> parts = rep.getParts();
            for (MediaRepresentationPart part: parts){
                if (first){
                    if (part.getUri() != null){
                        mediaUriString += part.getUri().toString();
                        first = false;
                    }
                }else{
                    if (part.getUri() != null){
                        mediaUriString += ", " +part.getUri().toString();
                    }
                }
            }
        }
        return mediaUriString;
    }

    /**
     * @param state
     * @param gathering
     */
    private void addCollectingAreas(OutputModelExportState state, GatheringEvent gathering) {
        // TODO implement !!!

        if (!gathering.getCollectingAreas().isEmpty()){
            state.getResult().addWarning("Collecting areas not yet implemented but gathering " +
                    cdmBaseStr(gathering) + " has collecting areas.");
        }

    }

    /**
     * @param gathering
     * @return
     */
    private String createCollectorString(OutputModelExportState state, GatheringEvent gathering, FieldUnit fieldUnit) {
        try {
            String collectorString = "";
            AgentBase<?> collectorA = CdmBase.deproxy(gathering.getCollector());
            if (gathering.getCollector() != null){
               if (collectorA instanceof TeamOrPersonBase
                       && state.getConfig().isHighLightPrimaryCollector()){

                   Person primaryCollector = fieldUnit.getPrimaryCollector();
                   if (collectorA instanceof Team){
                       Team collectorTeam = (Team)collectorA;
                       boolean isFirst = true;
                       for (Person member: collectorTeam.getTeamMembers()){
                           if (!isFirst){
                               collectorString += "; ";
                           }
                           if (member.equals(primaryCollector)){
                               //highlight
                               collectorString += "<b>" + member.getTitleCache() + "</b>";
                           }else{
                               collectorString += member.getTitleCache();
                           }
                       }
                   }
               } else{
                   collectorString = collectorA.getTitleCache();
               }
           }
           return collectorString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when creating collector string for " +
                    cdmBaseStr(fieldUnit) + ": " + e.getMessage());
            return "";
        }
    }


    /**
     * Returns a string representation of the {@link CdmBase cdmBase} object
     * for result messages.
     */
    private String cdmBaseStr(CdmBase cdmBase) {
        if (cdmBase == null){
            return "-no object available-";
        }else{
            return cdmBase.getClass().getSimpleName() + ": " + cdmBase.getUuid();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(OutputModelExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(OutputModelExportState state) {
        return false;
    }

}
