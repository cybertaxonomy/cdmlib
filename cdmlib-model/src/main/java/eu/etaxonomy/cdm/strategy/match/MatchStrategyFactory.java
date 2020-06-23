/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @since 17.10.2018
 */
public class MatchStrategyFactory {

    public static IMatchStrategyEqual NewDefaultInstance(Class<? extends IMatchable> matchClass){
        return new DefaultMatchStrategy(matchClass);
    }

    public static IParsedMatchStrategy NewParsedInstance(Class<? extends IMatchable> matchClass){
        IParsedMatchStrategy parsedMatchStrategy = new ParsedBaseMatchStrategy(matchClass);
        return parsedMatchStrategy;
    }

    public static IParsedMatchStrategy NewParsedPersonInstance(){
        try {
            IParsedMatchStrategy parsedPersonMatchStrategy = NewParsedInstance(Person.class);

            addParsedAgentBaseMatchModes(parsedPersonMatchStrategy);

            //FIXME adapt for inRef authors
            parsedPersonMatchStrategy.setMatchMode("familyName", MatchMode.EQUAL_OR_FIRST_NULL);

            //TODO lifespan may implement MATCH_OR_ONE_NULL
            String[] equalOrNullParams = new String[]{"collectorTitle","givenName","initials",
                    "lifespan","orcid","prefix","suffix"};
            for(String param : equalOrNullParams){
                parsedPersonMatchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_FIRST_NULL);
            }

            String[] ignoreParams = new String[]{"institutionalMemberships"};
            for(String param : ignoreParams){
                parsedPersonMatchStrategy.setMatchMode(param, MatchMode.IGNORE);
            }

            return parsedPersonMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed person match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedTeamInstance(){
        IParsedMatchStrategy parsedTeamMatchStrategy = NewParsedInstance(Team.class);
        try {
            addParsedAgentBaseMatchModes(parsedTeamMatchStrategy);

            parsedTeamMatchStrategy.setMatchMode("teamMembers", MatchMode.MATCH, NewParsedPersonInstance());

            parsedTeamMatchStrategy.setMatchMode("hasMoreMembers", MatchMode.EQUAL);

            //TODO is this correct?
            parsedTeamMatchStrategy.setMatchMode("protectedCollectorTitleCache", MatchMode.EQUAL_OR_FIRST_NULL);
            parsedTeamMatchStrategy.setMatchMode("protectedNomenclaturalTitleCache", MatchMode.EQUAL);

            String[] equalOrNullParams = new String[]{"collectorTitle"};
            for(String param : equalOrNullParams){
                parsedTeamMatchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_FIRST_NULL);
            }

            String[] ignoreParams = new String[]{};
            for(String param : ignoreParams){
                parsedTeamMatchStrategy.setMatchMode(param, MatchMode.IGNORE);
            }

            return parsedTeamMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed team match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedTeamOrPersonInstance(){
        @SuppressWarnings("rawtypes")
        SubClassMatchStrategy<TeamOrPersonBase> parsedAuthorMatchStrategy = SubClassMatchStrategy.NewInstance(TeamOrPersonBase.class);
        try {
            parsedAuthorMatchStrategy.putStrategy(Person.class, NewParsedPersonInstance());
            parsedAuthorMatchStrategy.putStrategy(Team.class, NewParsedTeamInstance());
            return parsedAuthorMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed author match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedBookInstance(){
        try {
            IParsedMatchStrategy parsedBookMatchStrategy = NewParsedInstance(Reference.class);

            addParsedReferenceMatchModes(parsedBookMatchStrategy);

            //author (as it should always be known for a parsed book)
            parsedBookMatchStrategy.setMatchMode("authorship", MatchMode.MATCH_REQUIRED, NewParsedTeamOrPersonInstance());
            //in-reference , parsed book has no in-Reference
            parsedBookMatchStrategy.setMatchMode("inReference", MatchMode.EQUAL_OR_FIRST_NULL);
            parsedBookMatchStrategy.setMatchMode("title", MatchMode.EQUAL_OR_FIRST_NULL);  //not available in parsed book
            parsedBookMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL_REQUIRED);

            //TODO caches

            return parsedBookMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed team match strategy.", e);
        }
    }

    /**
     * As the book section is not clearly defined if parsed (usually only the author and publication date is known),
     * it can only match exactly. If pages, title, abbrevTitle are known for the more complete references
     * it will match only if they are also known for the parsed instance.
     */
    public static IParsedMatchStrategy NewParsedBookSectionInstance(){
        try {
            IParsedMatchStrategy parsedBookSectionMatchStrategy = (IParsedMatchStrategy)NewDefaultInstance(Reference.class);

            addParsedReferenceMatchModes(parsedBookSectionMatchStrategy);

            //author (or either title or authorship match, but usually only authorship is known for parsed instances)
            parsedBookSectionMatchStrategy.setMatchMode("authorship", MatchMode.MATCH_REQUIRED, NewParsedTeamOrPersonInstance());
            //in-reference
            parsedBookSectionMatchStrategy.setMatchMode("inReference", MatchMode.MATCH_REQUIRED, NewParsedBookInstance());
            //see comment on title and abbrevTitle in #NewParsedArticleInstance
            parsedBookSectionMatchStrategy.setMatchMode("title", MatchMode.EQUAL);
            parsedBookSectionMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL);

            //TODO caches

            return parsedBookSectionMatchStrategy;

        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed team match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedArticleInstance(){ //or should it better be Section (in Journal)
        try {
            IParsedMatchStrategy articleMatchStrategy = (IParsedMatchStrategy)NewDefaultInstance(Reference.class);

            addParsedReferenceMatchModes(articleMatchStrategy);

            //if a title or abbrevTitle exists for the existing (first) article
            //we can not guarantee that the article is really the one that is
            //meant in the context, there could be other articles with the same
            //author.
            //Having a soft matching for possible candidates may solve this issue.
            //Also comparing the detail (page) with a filled field "pages" may solve
            //it in many cases.
            //However, we need to accept that articles as nomenclatural references are not
            //clearly enough defined so exact matching with rich data is not possible at
            //this point
            //TODO improvement, check page number
            articleMatchStrategy.setMatchMode("title", MatchMode.EQUAL);
            articleMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL);

            articleMatchStrategy.setMatchMode("authorship", MatchMode.MATCH_REQUIRED, NewParsedTeamOrPersonInstance());
            articleMatchStrategy.setMatchMode("inReference", MatchMode.MATCH, NewParsedJournalInstance());

            //TODO caches

            return articleMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Problems when creating parsed article match strategy.");
        }
    }

    /**
     * Adds all typical parsed reference match modes which are equal for journal, article,
     * book section, book (and generic?).
     * The following fields need to be handled explicitly by each calling method:<BR>
     *
     * <LI>authorship</LI>
     * <LI>title</LI>
     * <LI>abbrevTitle</LI>
     * <LI>titleCache</LI>
     * <LI>abbrevTitleCache</LI>
     * <LI>protectedTitleCache</LI>
     * <LI>protectedAbbrevTitleCache</LI>
     * <LI>inReference</LI>
     *
     * @param referenceMatchStrategy the strategy to fill
     * @throws MatchException
     */
    private static void addParsedReferenceMatchModes(IParsedMatchStrategy referenceMatchStrategy) throws MatchException {

        //"placePublished" should be MatchMode.EQUAL if parser parses place published
        //TODO datePublished could also be more detailed in first then in second, e.g. Apr 2008 <-> 2008,
        //something like MatchMode.IncludedIn is needed here

        //TODO externally managed

        addParsedIdentifiableEntityModes(referenceMatchStrategy);

        String[] equalOrNullParams = new String[]{"accessed","doi",
                "institution","isbn","issn","organization",
                "pages","publisher","placePublished",
                "referenceAbstract","school","uri"};
        for(String param : equalOrNullParams){
            referenceMatchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_FIRST_NULL);
        }

        String[] equalParams = new String[]{"datePublished","edition",
                "editor","seriesPart","volume"};
        for(String param : equalParams){
            referenceMatchStrategy.setMatchMode(param, MatchMode.EQUAL);
        }
        String[] matchOrNullParams = new String[]{"institution"};
        for(String param : matchOrNullParams){
            referenceMatchStrategy.setMatchMode(param, MatchMode.MATCH_OR_FIRST_NULL);
        }
    }

    private static void addParsedAgentBaseMatchModes(IParsedMatchStrategy matchStrategy) throws MatchException {

        //FIXME adapt for inRef authors
        //TODO how to initialize this cache field in a generic way
        matchStrategy.setMatchMode("nomenclaturalTitle", MatchMode.EQUAL_REQUIRED);

        addParsedIdentifiableEntityModes(matchStrategy);

        //TODO contact does not yet works, also not with EQUAL_OR_ONE_NULL, leads to agent.id=? or agent.id is null query
        //better should be even handled with MATCH.Equal_OR_ONE_NULL
        String[] equalOrNullParams = new String[]{"collectorTitle"};
        for(String param : equalOrNullParams){
            matchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_FIRST_NULL);
        }
        String[] ignoreCollectionParams = new String[]{"media"};
        for(String param : ignoreCollectionParams){
            matchStrategy.setMatchMode(param, MatchMode.IGNORE);
        }
    }

    private static void addParsedIdentifiableEntityModes(IParsedMatchStrategy matchStrategy) throws MatchException {

        addAnnotatableEntityModes(matchStrategy);

        //TODO titleCache and protectedTitleCache

        //TODO lsid should be handled with MATCH
        String[] equalOrNullParams = new String[]{"lsid"};
        for(String param : equalOrNullParams){
            matchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_FIRST_NULL);
        }
        String[] ignoreCollectionParams = new String[]{"credits","extensions","identifiers","links","rights"};
        for(String param : ignoreCollectionParams){
            matchStrategy.setMatchMode(param, MatchMode.IGNORE);
        }
    }

    private static void addAnnotatableEntityModes(IParsedMatchStrategy matchStrategy) throws MatchException {

//        //TODO titleCache and protectedTitleCache
//
//        //TODO lsid should be handled with MATCH
//        String[] equalOrNullParams = new String[]{"lsid"};
//        for(String param : equalOrNullParams){
//            matchStrategy.setMatchMode(param, MatchMode.EQUAL_OR_ONE_NULL);
//        }
        String[] ignoreCollectionParams = new String[]{"sources","annotations","markers"};
        for(String param : ignoreCollectionParams){
            matchStrategy.setMatchMode(param, MatchMode.IGNORE);
        }
    }

    public static IParsedMatchStrategy NewParsedJournalInstance(){

        try {
            IParsedMatchStrategy parsedJournalMatchStrategy = NewParsedInstance(Reference.class);
            addParsedReferenceMatchModes(parsedJournalMatchStrategy);

            //journals must not have an author, better MatchMode would be MatchMode.NULL
            parsedJournalMatchStrategy.setMatchMode("authorship", MatchMode.MATCH, NewParsedTeamOrPersonInstance());
            //in-reference, parsed journal has no in-reference
            parsedJournalMatchStrategy.setMatchMode("inReference", MatchMode.EQUAL_OR_FIRST_NULL);
            parsedJournalMatchStrategy.setMatchMode("title", MatchMode.EQUAL_OR_FIRST_NULL);  //full title not available in parsed journal
            parsedJournalMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL_REQUIRED);

            //TODO caches

            return parsedJournalMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed journal match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedReferenceInstance(){
        IParsedMatchStrategy refStrat = ParsedReferenceMatchStrategy.INSTANCE();
        return refStrat;
    }

    /**
     * Returns the best matching strategy for a given reference
     * with a given reference type
     * @return the matching strategy
     * @throws NullPointerException if ref is <code>null</code>
     */
    public static IMatchStrategy NewParsedReferenceInstance(Reference ref) {
        ReferenceType type = ref.getType();
        if (type.equals(ReferenceType.Article)){
            return NewParsedArticleInstance();
        }else if (type.equals(ReferenceType.Book)){
            return NewParsedBookInstance();
        }else if (type.equals(ReferenceType.BookSection)){
            return NewParsedBookSectionInstance();
        }else if (type.equals(ReferenceType.Journal)){
            return NewParsedJournalInstance();
        }else if (type.equals(ReferenceType.Generic)){
            //TODO
            return NewDefaultInstance(Reference.class);
        }else{
            //TODO
            return NewDefaultInstance(Reference.class);
        }
    }

    //  public static IMatchStrategy NewParsedNameInstance(){
    //      IMatchStrategy result = new DefaultMatchStrategy(TaxonName.class);
    //      return result;
    //  }
}
