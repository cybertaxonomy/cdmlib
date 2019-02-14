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

/**
 * @author a.mueller
 * @since 17.10.2018
 *
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
        IParsedMatchStrategy parsedPersonMatchStrategy = NewParsedInstance(Person.class);
        try {
            parsedPersonMatchStrategy.setMatchMode("nomenclaturalTitle", MatchMode.EQUAL_REQUIRED);
            return parsedPersonMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed person match strategy.", e);
        }

    }

    public static IParsedMatchStrategy NewParsedTeamInstance(){
        IParsedMatchStrategy parsedTeamMatchStrategy = NewParsedInstance(Team.class);
        try {
            //TODO how to initialize this cache field in a generic way
            parsedTeamMatchStrategy.setMatchMode("nomenclaturalTitle", MatchMode.EQUAL_REQUIRED);
            parsedTeamMatchStrategy.setMatchMode("teamMembers", MatchMode.MATCH, NewParsedPersonInstance());

//            FieldMatcher membersMatcher = parsedTeamMatchStrategy.getMatching().getFieldMatcher("teamMembers");
//            membersMatcher.setMatchStrategy(NewParsedPersonInstance());

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
        IParsedMatchStrategy parsedBookMatchStrategy = NewParsedInstance(Reference.class);
        try {
            parsedBookMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL_REQUIRED);
            //TODO date published could als be more detailed in first then in second, e.g. Apr 2008 <-> 2008
            parsedBookMatchStrategy.setMatchMode("datePublished", MatchMode.EQUAL);
            parsedBookMatchStrategy.setMatchMode("edition", MatchMode.EQUAL);
            parsedBookMatchStrategy.setMatchMode("editor", MatchMode.EQUAL);
            parsedBookMatchStrategy.setMatchMode("volume", MatchMode.EQUAL);
            parsedBookMatchStrategy.setMatchMode("seriesPart", MatchMode.EQUAL);
            parsedBookMatchStrategy.setMatchMode("title", MatchMode.EQUAL_OR_SECOND_NULL);

            //TODO
//            parsedBookMatchStrategy.setMatchMode("inReference", MatchMode.EQUAL);
//            parsedBookMatchStrategy.setMatchMode("placePublished", MatchMode.EQUAL);  //required, if parser parses place published

            parsedBookMatchStrategy.setMatchMode("authorship", MatchMode.MATCH, NewParsedTeamOrPersonInstance());
//          FieldMatcher authorshipMatcher.getMatching().getFieldMatcher("authorship");
//          authorshipMatcher.setMatchStrategy(NewParsedTeamOrPersonInstance());

            return parsedBookMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed team match strategy.", e);
        }
    }

    /**
     * As the book secion is not clearly defined if parsed (usually only the author and publication date is known),
     * it can only match exactly. If pages, title, abbrevTitle are known for the more complete references
     * it will match only if they are also known for the parsed instance.
     * @return
     */
    public static IParsedMatchStrategy NewParsedBookSectionInstance(){
        IParsedMatchStrategy parsedBookSectionMatchStrategy = (IParsedMatchStrategy)NewDefaultInstance(Reference.class);
        try {
            //author (or either title or authorship match, but usually only authorship is known for parsed instances)
            parsedBookSectionMatchStrategy.setMatchMode("authorship", MatchMode.MATCH_REQUIRED, NewParsedTeamOrPersonInstance());
            //in-reference
            parsedBookSectionMatchStrategy.setMatchMode("inReference", MatchMode.MATCH, NewParsedBookInstance());
            //title, not required
            parsedBookSectionMatchStrategy.setMatchMode("title", MatchMode.EQUAL);

            return parsedBookSectionMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed team match strategy.", e);
        }
    }


    public static IParsedMatchStrategy NewParsedArticleInstance(){ //or should it better be Section (in Journal)
        try {
            IParsedMatchStrategy articleMatchStrategy = (IParsedMatchStrategy)NewDefaultInstance(Reference.class);
            articleMatchStrategy.setMatchMode("authorship", MatchMode.MATCH, NewParsedTeamOrPersonInstance());
//            articleMatchStrategy.setMatchMode("title", MatchMode.EQUAL);
//            articleMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL);
//            articleMatchStrategy.setMatchMode("volume", MatchMode.EQUAL);
//            articleMatchStrategy.setMatchMode("seriesPart", MatchMode.EQUAL);
//            articleMatchStrategy.setMatchMode("datePublished", MatchMode.EQUAL);
//            //TODO improvement, check page number
//            articleMatchStrategy.setMatchMode("pages", MatchMode.EQUAL);

            articleMatchStrategy.setMatchMode("inReference", MatchMode.MATCH, NewParsedJournalInstance());

            //title, not required
            articleMatchStrategy.setMatchMode("title", MatchMode.EQUAL);

//            articleMatchStrategy.setMatchMode("placePublished", MatchMode.EQUAL_OR_SECOND_NULL);

            return articleMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Problems when creating parsed article match strategy.");
        }
    }


    public static IParsedMatchStrategy NewParsedJournalInstance(){
        IParsedMatchStrategy parsedJournalMatchStrategy = NewParsedInstance(Reference.class);
        try {
            parsedJournalMatchStrategy.setMatchMode("abbrevTitle", MatchMode.EQUAL_REQUIRED);
            parsedJournalMatchStrategy.setMatchMode("title", MatchMode.EQUAL_OR_SECOND_NULL);
            //TODO date published could also be more detailed in first then in second, e.g. Apr 2008 <-> 2008

            //undecided
            parsedJournalMatchStrategy.setMatchMode("edition", MatchMode.EQUAL);
            parsedJournalMatchStrategy.setMatchMode("editor", MatchMode.EQUAL);
            parsedJournalMatchStrategy.setMatchMode("seriesPart", MatchMode.EQUAL);

            //should be null for journals, therefore not match with dirty data
            parsedJournalMatchStrategy.setMatchMode("datePublished", MatchMode.EQUAL);
            parsedJournalMatchStrategy.setMatchMode("volume", MatchMode.EQUAL);
            //journals must not have an author, better MatchMode would be MatchMode.NULL
            parsedJournalMatchStrategy.setMatchMode("authorship", MatchMode.EQUAL);

            //TODO
//            parsedBookMatchStrategy.setMatchMode("inReference", MatchMode.EQUAL);
//            parsedBookMatchStrategy.setMatchMode("placePublished", MatchMode.EQUAL);  //required, if parser parses place published

//          FieldMatcher authorshipMatcher = parsedJournalMatchStrategy.getMatching().getFieldMatcher("authorship");
//          authorshipMatcher.setMatchStrategy(NewParsedTeamOrPersonInstance());


            return parsedJournalMatchStrategy;
        } catch (MatchException e) {
            throw new RuntimeException("Exception when creating parsed journal match strategy.", e);
        }
    }

    public static IParsedMatchStrategy NewParsedReferenceInstance(){
        IParsedMatchStrategy refStrat = ParsedReferenceMatchStrategy.INSTANCE();
        return refStrat;
    }



    //  public static IMatchStrategy NewParsedNameInstance(){
    //      IMatchStrategy result = new DefaultMatchStrategy(TaxonName.class);
    //      return result;
    //  }
}
