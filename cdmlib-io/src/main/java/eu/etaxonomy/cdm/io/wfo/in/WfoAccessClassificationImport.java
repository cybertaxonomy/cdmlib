/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.in;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.io.csv.in.CsvImportBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @since 15.11.2017
 */
@Component
public class WfoAccessClassificationImport<STATE extends WfoAccessImportState>
        extends CsvImportBase<WfoAccessImportConfigurator, STATE, TaxonName>{

    /**
     *
     */
    private static final String PSEUDO_SUFFIX = "_PSEUDO";

    /**
     *
     */
    private static final String WFO_DOUBTFUL = "__WFO_Doubtful";

    private static final long serialVersionUID = 8721691506017004260L;

    private static final String TAXON_ID = "taxonID";
    private static final String PARENT_NAME_USAGE_ID = "parentNameUsageID";
    private static final String FAMILY = "family";
    private static final String GENUS = "genus";
    private static final String SPECIFIC_EPITHET = "specificEpithet";
    private static final String INFRA_SPECIFIC_EPITHET = "infraspecificEpithet";
    private static final String TAXONOMIC_STATUS = "taxonomicStatus";
    private static final String ACCEPTED_NAME_USAGE_ID = "acceptedNameUsageID";
    private static final String ORIGINAL_NAME_USAGE_ID = "originalNameUsageID";

    private Map<String,TaxonName> nameMap;
    private Map<UUID, TaxonNode> pseudoParentMap;
    private Classification classification;


    public enum TaxStatus{
        ACC,
        SYN,
        DOUBT;
        public static TaxStatus from(String statusStr){
            if ("Accepted".equals(statusStr)){
                return ACC;
            }else if ("Synonym".equals(statusStr)){
                return SYN;
            }else if ("Doubtful".equals(statusStr)){
                return DOUBT;
            }else{
                throw new RuntimeException("Taxonomic status not recognized: " + statusStr);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleSingleLine(STATE state) {

        Map<String, String> record = state.getCurrentRecord();
        String taxonId = record.get(TAXON_ID);
        if (state.isExistingWfoID(taxonId)){
            return;
        }
        TaxonName name = getName(state, TAXON_ID);
        TaxStatus status =  TaxStatus.from(record.get(TAXONOMIC_STATUS));
        if (status == TaxStatus.ACC){
            handleAccepted(state, name);
        }else if (status == TaxStatus.SYN){
            handleSynonym(state, name);
        }else if (status == TaxStatus.DOUBT){
            handleDoubtful(state, name);
        }
    }

    /**
     * @param state
     * @param name
     * @param cdmTaxon
     */
    private void handleAccepted(STATE state, TaxonName name) {
        TaxonBase<?> cdmTaxon = getCdmTaxon(state, name, TAXON_ID, Taxon.class);

        Reference sec = this.getTransactionalSourceReference(state);
        if (cdmTaxon == null){
            String message = "Taxon does not exist. This should not happen.";
            state.getResult().addError(message, state.getRow());
            return;
        }else if (cdmTaxon.isInstanceOf(Taxon.class)){
            Taxon taxon = CdmBase.deproxy(cdmTaxon, Taxon.class);
            TaxonName parentName = getName(state, PARENT_NAME_USAGE_ID);
            TaxonBase<?> parentBase = getCdmTaxon(state, parentName, PARENT_NAME_USAGE_ID, Taxon.class);
            TaxonNode newNode;
            if (parentBase == null){
                //parent does not exist
                String message = "Parent taxon does not exist. This should not hapen.";
                state.getResult().addWarning(message, state.getRow());
//                TaxonNode wfoParentGenus = getWfoGenus(state, state.getCurrentRecord().get(GENUS), Rank.GENUS());
//                newNode = wfoParentGenus.addChildTaxon(taxon, sec, null);
                newNode = null;
            }else if (parentBase.isInstanceOf(Synonym.class)){
                //parent is synonym
//                o   >bitte die im Import-File akzeptierte Gattung [mit Suffix _wfo] als Taxon einrichten, aber ohne wfo-ID.
//                o   Also im Beispiel Abies_wfo als Gattung unter der Familie (zusätzlich zum Synonym Abies)

                //TODO
                Synonym synonymParent = CdmBase.deproxy(parentBase, Synonym.class);
                TaxonNode wfoParentGenus = getWfoGenus(state, synonymParent.getName().getNameCache(), synonymParent.getName().getRank());
                newNode = wfoParentGenus.addChildTaxon(taxon, sec, null);
            }else{ //existing parent is accepted
                Taxon parentTaxon = CdmBase.deproxy(parentBase, Taxon.class);
                Set<TaxonNode> parentNodes = parentTaxon.getTaxonNodes();
                if (parentNodes.size() == 0){
                    //TODO
                    Classification classification = getClassification(state);
                    newNode = classification.addParentChild(parentTaxon, taxon, sec, null);
                    getTaxonNodeService().saveOrUpdate(newNode.getParent());
                }else if (parentNodes.size() == 1){
                    Classification classification = getClassification(state);
                    newNode = classification.addParentChild(parentTaxon, taxon, sec, null);
                    getTaxonNodeService().saveOrUpdate(newNode.getParent());
//                    newNode = parentNodes.iterator().next().addChildTaxon(taxon, sec, null);
                }else{//multiple nodes for parent taxon
                    String message = "Parent taxon belongs to multiple nodes. This is unexpected and not handled. Add new node to classification";
                    state.getResult().addWarning(message, state.getRow());
                    Classification classification = getClassification(state);
                    newNode = classification.addParentChild(parentTaxon, taxon, sec, null);
                }
            }
            if (newNode != null){
                getTaxonNodeService().save(newNode);
            }

            //handle basionym
            if (state.getCurrentRecord().get(ORIGINAL_NAME_USAGE_ID)!= null){
                TaxonName basionym;
                try {
                    basionym = getName(state, ORIGINAL_NAME_USAGE_ID);
                    name.addBasionym(basionym, sec, null, null);
                    TaxonBase<?> basionymTaxon = getCdmTaxon(state, basionym, ORIGINAL_NAME_USAGE_ID, Synonym.class);
                    if (basionymTaxon.isInstanceOf(Synonym.class)){
                        Synonym basioSynonym = CdmBase.deproxy(basionymTaxon, Synonym.class);
                        if (basioSynonym.getAcceptedTaxon() != null){
                            if (basioSynonym.getAcceptedTaxon().equals(taxon)){
                                basioSynonym.setType(SynonymType.HOMOTYPIC_SYNONYM_OF());
                            }else{
                                String message = "Taxon's basionym already has an accepted taxon (%s), but the accepted taxon is not this taxon (%s).";
                                message = String.format(message, basioSynonym.getAcceptedTaxon().getTitleCache(), taxon.getTitleCache());
                                state.getResult().addError(message, state.getRow());
                            }
                        }else{
                            taxon.addHomotypicSynonym(basioSynonym);
                        }
                    }else{
                        String message = "Basionym %s of accepted %s is also accepted. This should not happen";
                        message = String.format(message, basionymTaxon.getName().getTitleCache(), taxon.getName().getTitleCache());
                        state.getResult().addWarning(message, state.getRow());
                    }
                } catch (Exception e) {
                    String message = "Problem when handling basionym for " + name.getTitleCache();
                    state.getResult().addError(message, state.getRow());
                }
            }
        }else{
            String message = "TaxonBase of accepted name is synonym. This should not happen anymore";
            state.getResult().addError(message, state.getRow());
        }
    }


    /**
     * @param state
     * @return
     */
    private Classification getClassification(STATE state) {
        if (classification == null){
            //TODO FIXME quick and dirty
            UUID uuidClassification = UUID.fromString("9edc58b5-de3b-43aa-9f31-1ede7c009c2b");
            classification = getClassificationService().find(uuidClassification);
            if (classification == null){
                String message = "Classification could not be found.";
                state.getResult().addError(message, state.getRow());
                throw new RuntimeException(message);
            }
        }
        return classification;
    }

    /**
     * @param state
     * @return
     */
    private TaxonNode getWfoGenus(STATE state, String taxonName, Rank rank) {
        Map<String, String> record = state.getCurrentRecord();
        String key = taxonName + "WFO";
        UUID uuid = state.getTaxonNodeUuid(key);
        TaxonNode node = getTaxonNodeMap().get(uuid);
        if (uuid != null && node == null){
            node = getTaxonNodeService().find(uuid);
            if (node != null){
                getTaxonNodeMap().put(node.getUuid(), node);
            }
        }
        if (node == null){
            if (uuid != null){
                String message = "Node for given parent uuid does not exist. This should not happen.";
                state.getResult().addWarning(message, state.getRow());
            }
            Reference sec = this.getTransactionalSourceReference(state);
            TaxonName name = TaxonNameFactory.NewNameInstance(state.getConfig().getNomenclaturalCode(), Rank.GENUS());
            name.setTitleCache(taxonName + "_WFO", true);
            addSourceReference(state, name);

            Taxon wfoTaxon = Taxon.NewInstance(name, sec);
            addSourceReference(state, wfoTaxon);

            TaxonNode familyNode = getFamilyParent(state, record.get(FAMILY));
            node = familyNode.addChildTaxon(wfoTaxon, sec, null);
            state.putTaxonNodeUuid(key, node.getUuid());
            getTaxonNodeMap().put(node.getUuid(), node);
            getTaxonNodeService().save(node);
        }
        return node;
    }

    /**
     * @param state
     * @param name
     * @param cdmTaxon
     */
    private void handleSynonym(STATE state, TaxonName name) {
        TaxonBase<?> cdmTaxon = getCdmTaxon(state, name, TAXON_ID, Synonym.class);

        if (cdmTaxon == null){
            String message = "Synonym does not exist. This should not happen.";
            state.getResult().addError(message, state.getRow());
        }else if (cdmTaxon.isInstanceOf(Synonym.class)){
            Synonym syn = CdmBase.deproxy(cdmTaxon, Synonym.class);
            TaxonName accName = getName(state, ACCEPTED_NAME_USAGE_ID);

            if (syn.getAcceptedTaxon()== null){
                 //TODO is this correct? might be WFO taxon
                TaxonBase<?> existingAccepted = getCdmTaxon(state, accName, ACCEPTED_NAME_USAGE_ID, Taxon.class);
                Taxon taxon;
                if (existingAccepted == null){
                    String message = "Accepted taxon does not exist. Can't add synonym to accepted";
                    state.getResult().addError(message, state.getRow());
                    taxon = null; //or throw exception
                }else if (existingAccepted.isInstanceOf(Synonym.class)){
                    Synonym existingSynonym = CdmBase.deproxy(existingAccepted, Synonym.class);
                    taxon = existingSynonym.getAcceptedTaxon();
                    taxon.addSynonym(syn, SynonymType.HETEROTYPIC_SYNONYM_OF());
                }else{
                    taxon = CdmBase.deproxy(existingAccepted, Taxon.class);
                }
                if (taxon != null){
                    taxon.addSynonym(syn, SynonymType.HETEROTYPIC_SYNONYM_OF());
                }
            }else{ //accepted taxon exists
                Taxon taxon = syn.getAcceptedTaxon();
                if (!taxon.getName().equals(accName)){
                    String message = "Synonym already has an accepted taxon (%s), but the accepted taxon is not the expected one (%s).";
                    message = String.format(message, taxon.getTitleCache(), accName.getTitleCache());
                    state.getResult().addError(message, state.getRow());
                }
            }

            //handle basionym
            if (state.getCurrentRecord().get(ORIGINAL_NAME_USAGE_ID)!= null){
                try{
                    TaxonName basionym = getName(state, ORIGINAL_NAME_USAGE_ID);
                    name.addBasionym(basionym, getTransactionalSourceReference(state), null, null);

                    TaxonBase<?> basionymTaxon = getCdmTaxon(state, basionym, ORIGINAL_NAME_USAGE_ID, TaxonBase.class);
                    if (basionymTaxon.isInstanceOf(Taxon.class)){
                        Taxon basioTaxon = CdmBase.deproxy(basionymTaxon, Taxon.class);
                        if (syn.getAcceptedTaxon().equals(basioTaxon)){
                            syn.setType(SynonymType.HOMOTYPIC_SYNONYM_OF());
                        }else{
                            String message = "Synonyms(%s) basionym is accepted (%s), but synonym has another accepted taxon (%s).";
                            message = String.format(message, syn.getName().getTitleCache(), basionymTaxon.getName().getTitleCache(),
                                    syn.getAcceptedTaxon().getName().getTitleCache());
                            state.getResult().addError(message, state.getRow());
                        }
                    }else{
                        //basionym is also synonym, for now nothing to do
                    }
                } catch (Exception e) {
                    String message = "Problem when handling basionym for " + name.getTitleCache();
                    state.getResult().addError(message, state.getRow());
                }
            }
        }else{  //is accepted
            String message = "TaxonBase of synonym is accepted. This should not happen anymore";
            state.getResult().addError(message, state.getRow());
        }
    }

    /**
     * @param state
     * @param name
     * @param taxonId
     * @return
     */
    private TaxonBase<?> getCdmTaxon(STATE state, TaxonName name, String fieldName,
            Class<? extends TaxonBase> expectedClass) {
        Map<String, String> record = state.getCurrentRecord();
        String taxonId = record.get(fieldName);

        @SuppressWarnings("rawtypes")
        Set<TaxonBase> cdmTaxa = name.getTaxonBases();
        if (cdmTaxa.isEmpty()){
            return null;
        }else if (cdmTaxa.size()==1){
            return cdmTaxa.iterator().next();
        }else{
            Set<TaxonBase<?>> cdmTaxa2 = new HashSet<>();
            for (TaxonBase<?> x : cdmTaxa){
                if (x.isInstanceOf(expectedClass)){
                    cdmTaxa2.add(x);
                }
            }
            if (cdmTaxa2.size() == 1){
                //TODO preliminary
                return cdmTaxa2.iterator().next();
            }
            String message = "Name %s (%s) has more then 1 existing taxon. Can't define existing taxon. Return arbitrary.";
            message = String.format(message, name.getTitleCache(), taxonId);
            state.getResult().addError(message, state.getRow());
            return cdmTaxa.iterator().next();
        }
    }

    /**
     * @param state
     * @param name
     * @param cdmTaxon
     */
    private void handleDoubtful(STATE state, TaxonName name) {
        TaxonBase<?> cdmTaxon = getCdmTaxon(state, name, TAXON_ID, Taxon.class);

        Reference sec = getTransactionalSourceReference(state);
//        Map<String, String> record = state.getCurrentRecord();
//        String taxonId = record.get(TAXON_ID);
//        if (state.isOriginalNameOfDoubful(taxonId)) {
//            //will be handled with doubtful accepted
//            //newTaxon = Synonym.NewInstance(name, sec);
//
//        }else{
//            Taxon newTaxon = Taxon.NewInstance(name, sec);
//            addSourceReference(state, newTaxon);
//            newTaxon.setDoubtful(true);
        if (cdmTaxon.isInstanceOf(Synonym.class)){
            String message = "Doubtful taxon was synonym. This should not happen";
            state.getResult().addError(message, state.getRow());
        }else{
            Taxon taxon = CdmBase.deproxy(cdmTaxon, Taxon.class);
            TaxonNode parent = getDoubtfulPseudoParent(state, sec);
            TaxonNode newChild = parent.addChildTaxon(taxon, sec, null);
            String originalNameIdStr = state.getCurrentRecord().get(ORIGINAL_NAME_USAGE_ID);
            if (isNotBlank(originalNameIdStr)){
//                TaxonName basionym = makeDoubtfulBasionym(state, ORIGINAL_NAME_USAGE_ID, sec);
                  try {
                      TaxonName basionym = getName(state, ORIGINAL_NAME_USAGE_ID);
                      name.addBasionym(basionym, sec, null, null);
                } catch (Exception e) {
                    String message = "Problem when handling basionym for " + name.getTitleCache();
                    state.getResult().addError(message, state.getRow());
                }
//                Synonym synonym = newTaxon.addHomotypicSynonymName(basionym);
//                synonym.setDoubtful(true);
//                addSourceReference(state, synonym);
            }
            getTaxonNodeService().saveOrUpdate(newChild);

        }
//        }
}



    /**
     * @param state
     * @param originalNameStr
     * @param sec
     * @return
     */
    private TaxonName makeDoubtfulBasionym(STATE state, String attrName, Reference sec) {
        TaxonName basionymName = getName(state, attrName);
        if (basionymName.getTaxonBases().size()>0){
            String message = "Doubtful basionym should not have a taxon attached yet: " + basionymName.getTitleCache();
            state.getResult().addError(message, state.getRow());
        }
        return basionymName;
    }

    /**
     * @param state
     * @return
     */
    private TaxonNode getDoubtfulPseudoParent(STATE state, Reference sec) {
        Map<String, String> record = state.getCurrentRecord();
        String family = record.get(FAMILY);
        String familyPseudo = family + PSEUDO_SUFFIX;
        UUID pseudoUuid = state.getTaxonNodeUuid(familyPseudo);
        TaxonNode pseudoParent = getTaxonNodeMap().get(pseudoUuid);
        if (pseudoParent == null){
            pseudoParent = getTaxonNodeService().find(pseudoUuid);
        }
        if (pseudoParent == null){
            TaxonNode familyParent = getFamilyParent(state, family);
            TaxonName pseudoName = TaxonNameFactory.NewNameInstance(state.getConfig().getNomenclaturalCode(),
                    Rank.SUPRAGENERICTAXON());
            addSourceReference(state, pseudoName);

            pseudoName.setTitleCache(WFO_DOUBTFUL + "_" + family, true);
            Taxon pseudoTaxon = Taxon.NewInstance(pseudoName, sec);
            addSourceReference(state, pseudoTaxon);

            pseudoParent = familyParent.addChildTaxon(pseudoTaxon, sec, null);
            state.putTaxonNodeUuid(familyPseudo, pseudoParent.getUuid());
            getTaxonNodeService().save(pseudoParent);
        }
        getTaxonNodeMap().put(pseudoParent.getUuid(), pseudoParent);  // in case not yet in
        return pseudoParent;
    }

    /**
     * @param state
     * @param family2
     * @return
     */
    private TaxonNode getFamilyParent(STATE state, String familyStr) {
        //FIXME TODO nasty quick and dirty find uuid for families/Dianthus
        UUID familyUuid;
        if (familyStr.equals("Amaranthaceae")){
            familyUuid = UUID.fromString("5f778f37-c60e-45e4-a3b3-123eec218428");
        }else if (familyStr.equals("Caryophyllaceae")){
            familyUuid = UUID.fromString("9078bb8e-eb68-4944-8a81-e855bfbe20c6"); //is Dianthus uuid
        }else if (familyStr.equals("Chenopodiaceae")){
            familyUuid = UUID.fromString("dec80465-49ca-47d7-9280-863bcb891877");
        }else if (familyStr.equals("Polygonaceae")){
            familyUuid = UUID.fromString("e2c836db-8ff3-42c5-ae76-60cd5da2bd0c");
        }else{
            String message = "Family not yet handled in code: " +  familyStr;
            throw new RuntimeException(message);
        }
        TaxonNode familyNode = getTaxonNodeMap().get(familyStr);
        if (familyNode == null){
            familyNode = getTaxonNodeService().find(familyUuid);
            getTaxonNodeMap().put(familyUuid, familyNode);
        }
        if (familyNode == null){
            String message = "Family node not found in database: " +  familyStr;
            throw new RuntimeException(message);
        }
        return familyNode;
    }

    /**
     * @param state
     * @return
     */
    private TaxonName getName(STATE state, String attr) {
        Map<String, String> record = state.getCurrentRecord();

        String taxonId = record.get(attr);
        TaxonName name;
        if (taxonId != null){
            name = getNameMap(state).get(taxonId);
//            name = getExistingTaxonName(state, taxonId);
        }else{
            String message = "TaxonID could not be found. Maybe format needs to be changed to UTF8 without BOM";
            state.getResult().addError(message, state.getRow());
            throw new RuntimeException(message);
//            name = null;
        }
        if (name != null){
            return name;
        }else{
            String message = "Taxon name for taxonID %s could not be found. This must not happen during taxon import phase.";
            message = String.format(message, taxonId);
            state.getResult().addError(message, state.getRow());
            throw new RuntimeException(message);
        }
    }


    /**
     * @param state
     * @return
     */
    private Map<String, TaxonName> getNameMap(STATE state) {
        if (nameMap == null){
            refreshNameMap(state);
        }
        return nameMap;
    }

    private Map<UUID, TaxonNode> getTaxonNodeMap() {
        if (pseudoParentMap == null){
            pseudoParentMap = new HashMap<>();
        }
        return pseudoParentMap;
    }



    /**
     * @param state
     * @param name
     */
    private void addSourceReference(STATE state, IdentifiableEntity<?> entity) {
        entity.addImportSource(null, null, getTransactionalSourceReference(state), "line " + state.getLine());
    }


    @Override
    protected void refreshTransactionStatus(STATE state) {
        super.refreshTransactionStatus(state);
        refreshNameMap(state);
        pseudoParentMap = new HashMap<>();
        classification = null;

    }


    /**
     * @param state
     */
    protected void refreshNameMap(STATE state) {
        nameMap = new HashMap<>();
        DefinedTerm wfoType = DefinedTerm.IDENTIFIER_NAME_WFO();
        Pager<IdentifiedEntityDTO<TaxonName>> identifiedNamePager = getNameService().findByIdentifier(TaxonName.class,
                "*", wfoType, MatchMode.EXACT, true, null, null, null);
        for (IdentifiedEntityDTO<TaxonName> dto : identifiedNamePager.getRecords()){
            TaxonName name = dto.getCdmEntity().getEntity();
            String identifier = dto.getIdentifier().getIdentifier();
            TaxonName oldName = nameMap.put(identifier, name);
            if (oldName != null){
                String message = "There are multiple names with same WFO identifier %s. ID1=%d, ID2=%d";
                message = String.format(message, identifier, name.getId(), oldName.getId());
                state.getResult().addWarning(message, state.getRow());
            }
        }
    }


}
