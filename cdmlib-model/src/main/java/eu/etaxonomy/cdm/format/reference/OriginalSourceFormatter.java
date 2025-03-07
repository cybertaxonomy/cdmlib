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
 * Formatter for original sources. The default instance formats the source in short form
 * (primarily author and year) and without brackets around the year.
 * <BR>
 *
 * @author a.mueller
 * @since 13.05.2021
 */
public class OriginalSourceFormatter extends CdmFormatterBase<OriginalSourceBase>{

    private final boolean withYearBrackets;

    private final boolean longForm;

    public static OriginalSourceFormatter INSTANCE = new OriginalSourceFormatter(false, false);

    //this can be used e.g. for in-text references, like "... following the opinion of Autor (2000: 22) we posit that ..."
    public static OriginalSourceFormatter INSTANCE_WITH_YEAR_BRACKETS = new OriginalSourceFormatter(true, false);

    public static OriginalSourceFormatter INSTANCE_LONG_CITATION = new OriginalSourceFormatter(false, true);

    /**
      * @param withYearBrackets if <code>false</code> the result comes without brackets (default is <code>false</code>)
      */
    private OriginalSourceFormatter(boolean withYearBrackets, boolean longForm) {
        this.withYearBrackets = withYearBrackets;
        this.longForm = longForm;
    }

    @Override
    public String format(OriginalSourceBase source) {
        if (source == null){
            return null;
        }
        Reference reference = source.getCitation();
        String microReference = source.getCitationMicroReference();
        TimePeriod accessed = source.getAccessed();
        if (reference == null && isBlank(microReference) && accessed == null){
            return null;
        }
        return format(reference, microReference, accessed );
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
        return format(reference, microReference, null, null);
    }

    public String format(Reference reference, String microReference, TimePeriod accessed){
        return format(reference, microReference, accessed, null);
    }

    public String format(Reference reference, String microReference, TimePeriod accessed, String uniqueString){
        if (reference == null){
            return null;
        }

        if(reference.isProtectedTitleCache()){
            return handleCitationDetailInTitleCache(reference, microReference, accessed, uniqueString);
        }

        if (longForm) {
            return handleLongformCitation(reference, microReference, accessed);
        }
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String authorStr = "";
        if (authorship == null) {
            return handleCitationDetailInTitleCache(reference, microReference, accessed, uniqueString);
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
        String result = CdmUtils.concat(" ", authorStr, getShortCitationDateAndDetail(reference, microReference, accessed, uniqueString));

        return result;
    }

    /**
     * Implementation is temporarily. May change or be further improved in future.
     */
    private String handleLongformCitation(Reference reference, String microReference, TimePeriod accessed) {

        ReferenceDefaultCacheStrategy referenceFormatter = ReferenceDefaultCacheStrategy.NewInstance();

        String refCitation = referenceFormatter.getTitleCache(reference);
        if (isPage(microReference)) {
            microReference = "p " + microReference;
        }

        String fullRefCitation = CdmUtils.concat(". ", refCitation, microReference);
        String accessedStr = TimePeriod.isBlank(accessed) ? null : "[accessed: "+accessed.toString()+"]";

        String withDateCitation = CdmUtils.concat(" ", fullRefCitation, accessedStr);
        if (withDateCitation != null) {
            withDateCitation = withDateCitation.replace("..", ".");
        }
        return withDateCitation;
    }

    /**
     * Implementation is temporarily. May change in future.
     */
    private boolean isPage(String microReference) {
        if (microReference == null) {
            return false;
        }else if (microReference.matches("\\d{1,5}")) {
            return true;
        }else {
            return false;
        }
    }

    private String getShortCitationDateAndDetail(Reference reference, String microReference, TimePeriod accessed, String uniqueString) {
        String dateStr = getDateString(reference, accessed);
        dateStr = CdmUtils.concat("", dateStr, uniqueString);
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
        return Nz(result);
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
    private String handleCitationDetailInTitleCache(Reference reference, String citationDetail, TimePeriod accessed, String uniqueString) {
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
            return getShortCitationDateAndDetail(reference, citationDetail, accessed, uniqueString);
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

        String dateAndDetail = getShortCitationDateAndDetail(reference, citationDetail, accessed, uniqueString);
        if (isNotBlank(dateAndDetail)) {
            //remove date (and detail?) if it is contained in titleCache already
            if (titleCache.endsWith(dateAndDetail) || titleCache.contains(dateAndDetail + ": ") || titleCache.contains(" " + dateAndDetail + ".")){
                dateAndDetail = "";
            }
        }
        String result = titleCache + (dateAndDetail.startsWith(":")? "": " ") + dateAndDetail;
        return result.trim();
    }

    private boolean isEmpty(TimePeriod timePeriod) {
        return timePeriod == null || timePeriod.isEmpty();
    }
}