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
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceDefaultCacheStrategy;

/**
 * @author a.mueller
 * @since 13.05.2021
 */
public class OriginalSourceFormatter extends CdmFormatterBase<OriginalSourceBase>{

    private final boolean withYearBrackets;

    public static OriginalSourceFormatter INSTANCE = new OriginalSourceFormatter(false);

    //this can be used e.g. for in-text references, like "... following the opinion of Autor (2000: 22) we posit that ..."
    public static OriginalSourceFormatter INSTANCE_WITH_YEAR_BRACKETS = new OriginalSourceFormatter(true);

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
    public String format(Reference reference, String microReference){
        return format(reference, microReference, null);
    }

    public String format(Reference reference, String microReference, TimePeriod accessed){
        if (reference == null){
            return null;
        }

        if(reference.isProtectedTitleCache()){
            return handleCitationDetailInTitleCache(reference, microReference, accessed);
        }
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String authorStr = "";
        if (authorship == null) {
            return handleCitationDetailInTitleCache(reference, microReference, accessed);
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
        String result = CdmUtils.concat(" ", authorStr, getShortCitationDateAndDetail(reference, microReference, accessed));

        return result;
    }

    private String getShortCitationDateAndDetail(Reference reference, String microReference, TimePeriod accessed) {
        String dateStr = getDateString(reference, accessed);
        return getShortTimePeriodAndDetail(dateStr, microReference, withYearBrackets);
    }

    private String getDateString(Reference reference, TimePeriod accessed) {
        String result = null;
        if (!isEmpty(accessed)) {
            return timePeriodString(accessed);
        } else if (reference.getAccessed() != null) {
            TimePeriod refAccessed = TimePeriod.NewInstance(reference.getAccessed());
            return timePeriodString(refAccessed);
        }else if (!isEmpty(reference.getDatePublished())) {
            return timePeriodString(reference.getDatePublished()) ;
        }else if (reference.getInReference() != null){
            return getDateString(reference.getInReference(), null);
        }else {
            return result;
        }
    }

    private String getShortTimePeriodAndDetail(String dateStr, String microReference, boolean withBrackets) {
        String result = dateStr;
        if (isNotBlank(microReference)){
            result = Nz(result) + ": " + microReference;
        }
        if (isNotBlank(result) && withBrackets){
            result = "(" + result + ")";
        }
        return result;
    }

    private String timePeriodString(TimePeriod timePeriod) {
        String result = null;
        if (timePeriod != null) {
            if (isNotBlank(timePeriod.getFreeText())){
                result = timePeriod.getFreeText();
            }else if (isNotBlank(timePeriod.getYear()) ){
                result = timePeriod.getYear();
            }
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
    private String handleCitationDetailInTitleCache(Reference reference, String citationDetail, TimePeriod accessed) {
        String titleCache = reference.getTitleCache();

        //remove reference.accessed
        if (reference.cacheStrategy() instanceof ReferenceDefaultCacheStrategy) {
            String accessedPart = ((ReferenceDefaultCacheStrategy)reference.cacheStrategy()).getAccessedPart(reference);
            if (isNotBlank(accessedPart) && titleCache.contains(accessedPart)) {
                titleCache = titleCache.replace(accessedPart, "").trim();
            }
        }

        //remove date published
        String datePublishedStr = timePeriodString(reference.getDatePublished());
        if (isNotBlank(datePublishedStr)) {
            String compareStr = datePublishedStr + ": ";
            if (titleCache.startsWith(compareStr)){
                titleCache = titleCache.substring(compareStr.length());
            }
        }

        String dateStr = getDateString(reference, accessed);
        if (isBlank(citationDetail) && isBlank(dateStr)){
            return titleCache;
        }
        if (isBlank(titleCache)){
            return getShortCitationDateAndDetail(reference, citationDetail, accessed);
        }

        //is citationDetail included in titleCache?
        if (isNotBlank(citationDetail)) {
            if (citationDetail.length() <= 3){
                if (titleCache.contains(": " + citationDetail)){
                    citationDetail = null;
                }
            }else{
                if (titleCache.contains(citationDetail)){
                    citationDetail = null;
                }
            }
        }

        //is accessed date included in titleCache?
        if (isNotBlank(dateStr)) {
            if (titleCache.endsWith(dateStr) || titleCache.contains(dateStr + ": ")){
                dateStr = null;
            }
        }

        String dateAndDetail = getShortCitationDateAndDetail(reference, citationDetail, accessed);
        return titleCache + " " + dateAndDetail;
    }

    private boolean isEmpty(TimePeriod timePeriod) {
        return timePeriod == null || timePeriod.isEmpty();
    }
}