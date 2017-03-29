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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
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
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupNameComparator;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeComparator;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
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
        state.getProcessor().createFinalResult();
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
        csvLine[table.getIndex(OutputModelTable.CLASSIFICATION_ID)] = getId(state, taxonNode.getClassification());
        csvLine[table.getIndex(OutputModelTable.CLASSIFICATION_TITLE)] = taxonNode.getClassification().getTitleCache();

        state.getProcessor().put(table, taxon, csvLine);
        for (TaxonNode child: taxonNode.getChildNodes()){
            handleTaxon(state, child);
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
        if (name.getLsid() != null){
            csvLine[table.getIndex(OutputModelTable.LSID)] = name.getLsid().getLsid();
        }else{
            csvLine[table.getIndex(OutputModelTable.LSID)] = "";
        }


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
        csvLine[table.getIndex(OutputModelTable.FULL_NAME_WITH_AUTHORS)] = getTropicosTitleCache(name);
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
        }else{
            csvLine[table.getIndex(OutputModelTable.PUBLICATION_TYPE)] = "";
        }



       /*
        * Collation

        Detail


        TitlePageYear
        */


        if (state.getActualTaxonBase() instanceof Taxon ){
            Taxon actualTaxon = (Taxon)state.getActualTaxonBase();
            Set<TaxonDescription> descriptions = actualTaxon.getDescriptions();
            String protologueUriString = "";
            boolean first = true;
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
                                            if (first){
                                                protologueUriString += part.getUri().toString();
                                                first = false;
                                            }else{
                                                protologueUriString += ", " +part.getUri().toString();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            csvLine[table.getIndex(OutputModelTable.PROTOLOGUE_URI)] = protologueUriString;
        }else{
            csvLine[table.getIndex(OutputModelTable.PROTOLOGUE_URI)] = "";
        }
        if (name.getStatus() != null || name.getStatus().isEmpty()){
            csvLine[table.getIndex(OutputModelTable.NOM_STATUS)] = "";
            csvLine[table.getIndex(OutputModelTable.NOM_STATUS_ABBREV)] = "";
        }else{

            String statusStringAbbrev = extractStatusString(name, true);
            String statusString = extractStatusString(name, false);

            csvLine[table.getIndex(OutputModelTable.NOM_STATUS)] = statusString.trim();
            csvLine[table.getIndex(OutputModelTable.NOM_STATUS_ABBREV)] = statusStringAbbrev.trim();
        }

        HomotypicalGroup group =name.getHomotypicalGroup();

        if (state.getHomotypicalGroupFromStore(group.getId()) == null){
            handleHomotypicalGroup(state, group);
        }
        csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_FK)] = getId(state, group);
        //csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_FK)] = String.valueOf(group.getTypifiedNames());
        state.getProcessor().put(table, name, csvLine);

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
    }

    /**
     * @param state
     * @param basionymAuthorship
     */
    private void handleAuthor(OutputModelExportState state, TeamOrPersonBase author) {
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
                state.getProcessor().put(tableAuthorRel, authorTeam.getId() +":" +member.getId(), csvLineRel);

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
                    state.getProcessor().put(table, member, csvLineMember);
                }
                index++;

            }
        }
        state.getProcessor().put(table, author, csvLine);




    }

    /**
     * @param name
     * @param statusString
     * @return
     */
    private String extractStatusString(TaxonNameBase name, boolean abbrev) {
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
    }

    /**
     * @param group
     */
    private void handleHomotypicalGroup(OutputModelExportState state, HomotypicalGroup group) {
        state.addHomotypicalGroupToStore(group);
        OutputModelTable table = OutputModelTable.HOMOTYPIC_GROUP;
        String[] csvLine = new String[table.getSize()];

        csvLine[table.getIndex(OutputModelTable.HOMOTYPIC_GROUP_ID)] = getId(state, group);
        List<TaxonNameBase> typifiedNames = new ArrayList<>();
        typifiedNames.addAll(group.getTypifiedNames());
        Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(null, true));
        String typifiedNamesString = "";
        for (TaxonNameBase name: typifiedNames){
            //Concatenated output string for homotypic group (names and citations) + status + some name relations (e.g. “non”)
//TODO: nameRelations, which and how to display


            typifiedNamesString += name.getTitleCache()+ extractStatusString(name, true) + "; ";
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
        state.getProcessor().put(table, String.valueOf(group.getId()), csvLine);

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
;            exBasionymAuthor = HibernateProxyHelper.deproxy(exBasionymAuthor);
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
    }

    /**
     * @param combinationAuthor
     * @return
     */
    private String createTropicosTeamTitle(TeamOrPersonBase combinationAuthor) {
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
