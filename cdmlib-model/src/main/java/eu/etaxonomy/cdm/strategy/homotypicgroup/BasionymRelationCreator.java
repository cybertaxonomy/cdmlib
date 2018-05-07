// $Id$
/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.homotypicgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * This class tries to guess all basionym relationships for the synonyms of a given taxon
 * by evaluating the name parts including authors.
 * It adds all {@link TaxonName taxon names} that seem to belong to the same
 * basionym to the homotypic group of this basionym and creates the basionym relationship
 * if not yet added/created.<BR>
 * Also it changes the {@link SynonymType synonym type} of the synonyms
 * that are homotypic to the accepted taxon to
 * {@link SynonymType#HOMOTYPIC_SYNONYM_OF() homotypic synonym of}.
 *
 * NOTE: It is still unclear where to put this kind of operations.
 * The base class, package and even the module may change in future.
 *
 * @author a.mueller
 * @since 22.04.2017
 *
 */
public class BasionymRelationCreator extends StrategyBase {

    private static final long serialVersionUID = -4711438819176248413L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BasionymRelationCreator.class);

    private UUID uuid = UUID.fromString("e9e1d1f5-e398-4ba7-81a6-92875573d7cb");

    /**
     * {@inheritDoc}
     */
    @Override
    protected UUID getUuid() {
        return uuid;
    }

    public void invoke (Taxon taxon){
        Set<Synonym> synonyms = taxon.getSynonyms();

        //compare accepted against synonyms
        for (Synonym synonym: synonyms){
            TaxonName basionym = compareHomotypic(taxon.getName(), synonym.getName());
            if (basionym != null){
                synonym.setType(SynonymType.HOMOTYPIC_SYNONYM_OF());
                adaptHomotypicGroup(basionym, taxon.getName(), synonym.getName());
            }
        }
        List<Synonym> synonymList = new ArrayList<>(synonyms);

        //compareEachSynonymAgainstEachOther;
        for (int i = 0; i < synonymList.size()-1; i++){
            for (int j = i + 1; j < synonymList.size(); j++){
                Synonym syn1 = synonymList.get(i);
                Synonym syn2 = synonymList.get(j);
                TaxonName basionym = compareHomotypic(syn1.getName(), syn2.getName());
                if (basionym != null){
                    adaptHomotypicGroup(basionym, syn1.getName(), syn2.getName());
                    if (taxon.getName().getBasionyms().contains(basionym)){
                        syn1.setType(SynonymType.HOMOTYPIC_SYNONYM_OF());
                        syn2.setType(SynonymType.HOMOTYPIC_SYNONYM_OF());
                    }
                }
            }
        }
    }

    /**
     * @param basionym
     * @param name
     * @param name2
     */
    private void adaptHomotypicGroup(TaxonName basionym,
            TaxonName name1, TaxonName name2) {
        if (basionym.equals(name1)){
            if (!name2.getBasionyms().contains(name1)){
                name2.addBasionym(name1);
            }
        }else if (basionym.equals(name2)){
            if (!name1.getBasionyms().contains(name2)){
                name1.addBasionym(name2);
            }
        }
    }

    /**
     * @param name
     * @param name2
     */
    private TaxonName compareHomotypic(TaxonName name1, TaxonName name2) {
        if (name1 == null || name2 == null){
            return null;
        }
        TaxonName basionymCandidate = checkAuthors(name1, name2);
        if (basionymCandidate == null){
            return null;
        }else{
            TaxonName newCombinationCandidate
                = basionymCandidate == name1? name2: name1;
            boolean isBasionym = compareNameParts(basionymCandidate, newCombinationCandidate);
            if (isBasionym){
                return basionymCandidate;
            }else{
                return null;
            }
        }
    }

    /**
     * @param basionymCandiate
     * @param newCombinationCandidate
     */
    private boolean compareNameParts(TaxonName basionymCandidate,
            TaxonName newCombinationCandidate) {
        if (basionymCandidate.isGenusOrSupraGeneric() || newCombinationCandidate.isGenusOrSupraGeneric()){
            return false;
        }else if (matchFamilyNamePart(basionymCandidate, newCombinationCandidate)){
            return true;
        }
        return false;
    }

    /**
     * @param name1
     * @param name2
     * @return
     */
    private TaxonName checkAuthors(TaxonName name1, TaxonName name2) {
        if (hasBasionymAuthorOf(name1, name2)){
            return name1;
        }else if (hasBasionymAuthorOf(name2, name1)){
            return name2;
        }else{
            return null;
        }
    }

    /**
     * @param name1
     * @param name2
     * @return
     */
    private boolean hasBasionymAuthorOf(TaxonName name1, TaxonName name2) {
        TeamOrPersonBase<?> basAuthor2 = name2.getBasionymAuthorship();
        TeamOrPersonBase<?> combinationAuthor = name1.getCombinationAuthorship();
        TeamOrPersonBase<?> basAuthor1 = name1.getBasionymAuthorship();
        if (basAuthor2 != null && basAuthor1 == null){
            if (matches(basAuthor2, combinationAuthor)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param basAuthor
     * @param combinationAuthor
     * @return
     */
    private boolean matches(TeamOrPersonBase<?> basAuthor, TeamOrPersonBase<?> combinationAuthor) {
        //TODO better do with a CDM matcher that also compares other fields and
        //returns false if other fields are contradictory
        if (basAuthor == null || combinationAuthor == null){
            return false;
        }else if (basAuthor == combinationAuthor || basAuthor.equals(combinationAuthor)){
            return true;
        }else if (CdmUtils.nonEmptyEquals(basAuthor.getNomenclaturalTitle(), combinationAuthor.getNomenclaturalTitle())){
            return true;
        }else{
            return false;
        }
    }

    /**
     * @param basionymName
     * @param newCombination
     * @return
     */
    public static boolean matchFamilyNamePart(TaxonName name1, TaxonName name2) {
        String familyNamePart1 = name1.getFamilyNamePart();
        String familyNamePart2 = name2.getFamilyNamePart();
        if (familyNamePart1 != null && familyNamePart2 != null){
            familyNamePart1 = normalizeBasionymNamePart(familyNamePart1);
            familyNamePart2 = normalizeBasionymNamePart(familyNamePart2);
            return (familyNamePart1.equals(familyNamePart2));
        }else{
            return false;
        }
    }

    /**
     * @param familyNamePart
     * @return
     */
    private static  String normalizeBasionymNamePart(String familyNamePart) {
        String namePart = familyNamePart.toLowerCase()
                .replaceAll("(um|us|a|is|e|os|on|or)$", "")
                .replaceAll("er$", "r")    //e.g. ruber <-> rubra
                .replaceAll("ese$", "s");  //e.g.  cayanensis <-> cayanenese
                //TODO tampensis / tampense
        return namePart;
    }

}
