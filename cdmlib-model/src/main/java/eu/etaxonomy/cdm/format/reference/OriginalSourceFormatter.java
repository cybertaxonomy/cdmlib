/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.CdmFormatterBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * @author a.mueller
 * @since 13.05.2021
 */
public class OriginalSourceFormatter extends CdmFormatterBase<OriginalSourceBase>{

    private final boolean withYearBrackets;

    public static OriginalSourceFormatter INSTANCE = new OriginalSourceFormatter(false);
    public static OriginalSourceFormatter INSTANCE_WITH_BRACKETS = new OriginalSourceFormatter(true);

    /**
      * @param withYearBrackets if <code>false</code> the result comes without brackets (default is <code>false</code>)
      */
    private OriginalSourceFormatter(boolean withYearBrackets) {
        this.withYearBrackets = withYearBrackets;
    }

    @Override
    public String format(OriginalSourceBase source) {
        if (source == null){
            return null;
        }
        Reference reference = source.getCitation();
        String microReference = source.getCitationMicroReference();
        if (reference == null && isBlank(microReference)){
            return null;
        }
        return format(reference, microReference);
    }

    /**
     * Creates a citation in form <i>author year: detail</i> or <i>author (year: detail)</i>.
     * <BR>
     * If reference has protected titlecache only the titlecache is returned (may change in future).
     * <BR>
     * The author team is abbreviated with <code>et al.</code> if more than 2 authors exist in the team.
     *
     * @param reference the reference to format
     * @param citationDetail the microreference (page, figure, etc.), if <code>null</code> also the colon separator is not used
     */
    public String format(Reference reference, String citationDetail){
        if (reference == null){
            return null;
        }

        if(reference.isProtectedTitleCache()){
            return handleCitationDetailInTitleCache(reference.getTitleCache(), citationDetail);
        }
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String authorStr = "";
        if (authorship == null) {
            return handleCitationDetailInTitleCache(reference.getTitleCache(), citationDetail);
        }
        authorship = CdmBase.deproxy(authorship);
        if (authorship instanceof Person){
            authorStr = getPersonString((Person)authorship);
        }
        else if (authorship instanceof Team){

            Team team = CdmBase.deproxy(authorship, Team.class);
            if (team.isProtectedTitleCache()){
                authorStr = team.getTitleCache();
            }else{
                authorStr = TeamDefaultCacheStrategy.INSTANCE_ET_AL_2().getFamilyTitle(team);
            }
        }
        String result = CdmUtils.concat(" ", authorStr, getShortCitationDate(reference, withYearBrackets, citationDetail));

        return result;
    }

    private String getShortCitationDate(Reference reference, boolean withBrackets, String citationDetail) {
        String result = null;
        if (reference.getDatePublished() != null && !reference.getDatePublished().isEmpty()) {
            if (isNotBlank(reference.getDatePublished().getFreeText())){
                result = reference.getDatePublished().getFreeText();
            }else if (isNotBlank(reference.getYear()) ){
                result = reference.getYear();
            }
            if (isNotBlank(citationDetail)){
                result = Nz(result) + ": " + citationDetail;
            }
            if (isNotBlank(result) && withBrackets){
                result = "(" + result + ")";
            }
        }else if (reference.getInReference() != null){
            result = getShortCitationDate(reference.getInReference(), withBrackets, citationDetail);
        }
        return result;
    }

    private String getPersonString(Person person) {
        String shortCitation;
        shortCitation = person.getFamilyName();
        if (isBlank(shortCitation) ){
            shortCitation = person.getTitleCache();
        }
        return shortCitation;
    }

    /**
     * Adds the citationDetail to the protected titleCache assuming that this is not accurately parsed.
     * If the detail is contained in the titleCache it is not added again to the result, otherwise
     * it is concatenated with separator ":"
     * @return the concatenated string
     */
    private String handleCitationDetailInTitleCache(String titleCache, String citationDetail) {
        if (isBlank(citationDetail)){
            return titleCache;
        }else if (isBlank(titleCache)){
            return ": " + citationDetail;
        }else if (citationDetail.length() <= 3){
            if (titleCache.contains(": " + citationDetail)){
                return titleCache;
            }
        }else{
            if (titleCache.contains(citationDetail)){
                return titleCache;
            }
        }
        return titleCache + ": " + citationDetail;
    }
}