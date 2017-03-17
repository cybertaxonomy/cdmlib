/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class OutputModelClassificationExport
            extends CdmExportBase<OutputModelConfigurator, OutputModelExportState, IExportTransformer>
            implements ICdmExport<OutputModelConfigurator, OutputModelExportState>{


    private static final long serialVersionUID = 2518643632756927053L;

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
                for (TaxonNode child : root.getChildNodes()){
                    handleTaxon(state, child);
                    //TODO progress monitor
                }
            }
        }
      //  state.getProcessor().createFinalResult(ExportResult)
    }

    /**
     * @param state
     * @param taxon
     */
    private void handleTaxon(OutputModelExportState state, TaxonNode taxonNode) {
        Taxon taxon = taxonNode.getTaxon();
        TaxonNameBase name = taxon.getName();
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

        state.getProcessor().put(table, taxon, csvLine);
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
       TaxonNameBase name = syn.getName();
       handleName(state, name);

       OutputModelTable table = OutputModelTable.SYNONYM;
       String[] csvLine = new String[table.getSize()];

       csvLine[table.getIndex(OutputModelTable.SYNONYM_ID)] = getId(state, syn);
       csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, syn.getAcceptedTaxon());
       csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, name);
       csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE_FK)] = getId(state, syn.getSec());
       csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE)] = getTitleCache(syn.getSec());

       state.getProcessor().put(table, syn, csvLine);
    }

    /**
     * @param state
     * @param name
     */
    private void handleName(OutputModelExportState state, TaxonNameBase name) {
        Rank rank = name.getRank();
        OutputModelTable table = OutputModelTable.SCIENTIFIC_NAME;
        String[] csvLine = new String[table.getSize()];

        csvLine[table.getIndex(OutputModelTable.NAME_ID)] = getId(state, name);
        csvLine[table.getIndex(OutputModelTable.RANK)] = getTitleCache(rank);
        csvLine[table.getIndex(OutputModelTable.RANK_SEQUENCE)] = String.valueOf(rank.getOrderIndex());
        csvLine[table.getIndex(OutputModelTable.FULL_NAME_WITH_AUTHORS)] = getTropicosTitleCache(name);
        csvLine[table.getIndex(OutputModelTable.FULL_NAME_NO_AUTHORS)] = name.getNameCache();
        csvLine[table.getIndex(OutputModelTable.GENUS_UNINOMIAL)] = name.getGenusOrUninomial();
        csvLine[table.getIndex(OutputModelTable.INFRAGENERIC_EPITHET)] = name.getInfraGenericEpithet();
        csvLine[table.getIndex(OutputModelTable.SPECIFIC_EPITHET)] = name.getSpecificEpithet();
        csvLine[table.getIndex(OutputModelTable.INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();
        csvLine[table.getIndex(OutputModelTable.BAS_AUTHORTEAM_FK)] = getId(state,name.getBasionymAuthorship());
        csvLine[table.getIndex(OutputModelTable.BAS_EX_AUTHORTEAM_FK)] = getId(state, name.getExBasionymAuthorship());
        csvLine[table.getIndex(OutputModelTable.COMB_AUTHORTEAM_FK)] = getId(state,name.getCombinationAuthorship());
        csvLine[table.getIndex(OutputModelTable.COMB_EX_AUTHORTEAM_FK)] = getId(state, name.getExCombinationAuthorship());
        csvLine[table.getIndex(OutputModelTable.AUTHOR_TEAM_STRING)] = name.getAuthorshipCache();
        Reference nomRef = (Reference)name.getNomenclaturalReference();
        if (nomRef != null){
            csvLine[table.getIndex(OutputModelTable.PUBLICATION_TYPE)] = nomRef.getType().name();
        }else{
            csvLine[table.getIndex(OutputModelTable.PUBLICATION_TYPE)] = "";
        }
        if (nomRef.getInReference() != null){
            Reference inReference = nomRef.getInReference();
            csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = CdmUtils.Nz(inReference.getAbbrevTitle());
            csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitle());

            TeamOrPersonBase author = inReference.getAuthorship();
            if (author != null){
                csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getNomenclaturalTitle());
                csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getTitleCache());
            }else{
                csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
                csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
            }
        }else{
            csvLine[table.getIndex(OutputModelTable.ABBREV_TITLE)] = "";
            csvLine[table.getIndex(OutputModelTable.FULL_TITLE)] = "";
            csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)]= "";
            csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
            csvLine[table.getIndex(OutputModelTable.ABBREV_REF_AUTHOR)] = "";
        }
        if (state.getActualTaxonBase() instanceof Taxon ){
            Taxon actualTaxon = (Taxon)state.getActualTaxonBase();
            Set<TaxonDescription> descriptions = actualTaxon.getDescriptions();
            String protologueUriString = "";
            for (TaxonDescription description : descriptions){
                if (!description.getElements().isEmpty()){
                    for (DescriptionElementBase element : description.getElements()){
                        if (element.getFeature().equals(Feature.PROTOLOGUE())){
                            if (!element.getMedia().isEmpty()){
                                List<Media> media = element.getMedia();
                                for (Media mediaElement: media){
                                    Iterator<MediaRepresentation> it =  mediaElement.getRepresentations().iterator();
                                    while(it.hasNext()){
                                        MediaRepresentation rep = it.next();
                                        List<MediaRepresentationPart> parts = rep.getParts();
                                        for (MediaRepresentationPart part: parts){
                                            protologueUriString += ", " +part.getUri().toString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else{
            csvLine[table.getIndex(OutputModelTable.PROTOLOGUE_URI)] = "";
        }

        state.getProcessor().put(table, name, csvLine);
/*
 * EditName_ID
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
ProtologueURI
NomenclaturalStatus
NomenclaturalStatusAbbreviation
HomotypicGroup_Fk
HomotypicGroupSequenceNumber
TypeString

 *
 */
    }

    /**
     * @param name
     * @return
     */
    private String getTropicosTitleCache(TaxonNameBase name) {
        String basionymStart = "(";
        String basionymEnd = ") ";
        String exAuthorSeperator = " ex ";
        TeamOrPersonBase combinationAuthor = name.getCombinationAuthorship();
        TeamOrPersonBase exCombinationAuthor = name.getExCombinationAuthorship();
        TeamOrPersonBase basionymAuthor = name.getBasionymAuthorship();
        TeamOrPersonBase exBasionymAuthor = name.getExBasionymAuthorship();

        String combinationAuthorString = null;
        if (combinationAuthor instanceof Team){
            for (Person teamMember:((Team)combinationAuthor).getTeamMembers()){
                combinationAuthorString = createTropicosAuthorString(teamMember);
            }
        }else{
            combinationAuthorString = createTropicosAuthorString((Person)combinationAuthor);
        }
        String exCombinationAuthorString = null;
        if (exCombinationAuthor instanceof Team){
            for (Person teamMember:((Team)exCombinationAuthor).getTeamMembers()){
                exCombinationAuthorString = createTropicosAuthorString(teamMember);
            }
        }else{
            exCombinationAuthorString = createTropicosAuthorString((Person)exCombinationAuthor);
        }

        String basionymAuthorString = null;
        if (basionymAuthor instanceof Team){
            for (Person teamMember:((Team)basionymAuthor).getTeamMembers()){
                basionymAuthorString = createTropicosAuthorString(teamMember);
            }
        }else{
            basionymAuthorString = createTropicosAuthorString((Person)basionymAuthor);
        }

        String exBasionymAuthorString = null;
        if (exBasionymAuthor instanceof Team){
            for (Person teamMember:((Team)exBasionymAuthor).getTeamMembers()){
                exBasionymAuthorString = createTropicosAuthorString(teamMember);
            }
        }else{
            exBasionymAuthorString = createTropicosAuthorString((Person)exBasionymAuthor);
        }
        String completeAuthorString =  basionymStart + CdmUtils.Nz(exBasionymAuthorString) +exAuthorSeperator + CdmUtils.Nz(exBasionymAuthorString) + basionymEnd + CdmUtils.Nz(exCombinationAuthorString) + exAuthorSeperator + CdmUtils.Nz(combinationAuthorString);

        return completeAuthorString;
    }

    /**
     * @param teamMember
     */
    private String createTropicosAuthorString(Person teamMember) {
        String nomAuthorString = "";
        String[] splittedAuthorString = teamMember.getNomenclaturalTitle().split("\\s");
        int index = 0;
        for (String split: splittedAuthorString){
            if ( index < splittedAuthorString.length-1 && (split.length()==1 || split.endsWith("."))){
                nomAuthorString += split;
            }else{
                nomAuthorString = nomAuthorString +" "+ split;
            }
            index++;
        }
        return nomAuthorString;
    }

    /**
     * @param state
     * @param name
     */
    private void handleReference(OutputModelExportState state, Reference reference) {
        OutputModelTable table = OutputModelTable.REFERENCE;

        String[] csvLine = new String[table.getSize()];
        csvLine[table.getIndex(OutputModelTable.REFERENCE_ID)] = getId(state, reference);
        //TODO short citations correctly
        String shortCitation = reference.getAbbrevTitleCache();  //Should be Author(year) like in Taxon.sec
        csvLine[table.getIndex(OutputModelTable.BIBLIO_SHORT_CITATION)] = shortCitation;
        //TODO get preferred title
        csvLine[table.getIndex(OutputModelTable.REF_TITLE)] = reference.getTitle();
        csvLine[table.getIndex(OutputModelTable.DATE_PUBLISHED)] = reference.getDatePublishedString();
        //TBC

        state.getProcessor().put(table, reference, csvLine);

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
