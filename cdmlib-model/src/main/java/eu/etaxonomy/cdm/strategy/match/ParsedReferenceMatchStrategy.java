/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @since 20.10.2018
 */
public class ParsedReferenceMatchStrategy implements IParsedMatchStrategy{

    private static ParsedReferenceMatchStrategy instance;

    private IParsedMatchStrategy articleStrategy = MatchStrategyFactory.NewParsedArticleInstance();
    private IParsedMatchStrategy bookStrategy = MatchStrategyFactory.NewParsedBookInstance();
    private IParsedMatchStrategy bookSectionStrategy = MatchStrategyFactory.NewParsedBookSectionInstance();
    private IParsedMatchStrategy journalStrategy = MatchStrategyFactory.NewParsedJournalInstance();
    //TODO no generic reference yet
    private IMatchStrategy genericStrategy = MatchStrategyFactory.NewDefaultInstance(Reference.class);

    /**
     * Immutable singleton instance.
     * @return
     */
    public static ParsedReferenceMatchStrategy INSTANCE(){
        if (instance == null){
            instance = new ParsedReferenceMatchStrategy();
        }
        return instance;
    }

    @Override
    public void setMatchMode(String propertyName, MatchMode matchMode) throws MatchException {
        setMatchMode(propertyName, matchMode, null);
    }

    @Override
    public void setMatchMode(String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy)
            throws MatchException {
        throw new MatchException("ParsedReferenceMatchStrategy is immutable");
    }

    @Override
    public Matching getMatching() {
        //why does it not throw MatchException?
        throw new RuntimeException("getMatching not yet implemented");
    }

    @Override
    public Matching getMatching(IMatchable instance) {
        if (instance instanceof Reference){
            IMatchStrategy innerStrategy = getStrategyByType(((Reference)instance).getType());
            if (innerStrategy != null){
                return innerStrategy.getMatching();
            }
        }
        //why does it not throw MatchException?
        throw new RuntimeException("getMatching not yet implemented");
    }

    @Override
    public <T extends IMatchable> MatchResult invoke(T fullInstance, T parsedInstance) throws MatchException {
        return invoke(fullInstance, parsedInstance, false);
    }

    @Override
    public <T extends IMatchable> MatchResult invoke(T fullInstance, T parsedInstance, boolean failAll)
            throws MatchException {
        MatchResult matchResult = new MatchResult();
        invoke(fullInstance, parsedInstance, matchResult, false);
        return matchResult;
    }

    @Override
    public <T extends IMatchable> void invoke(T fullInstance, T parsedInstance, MatchResult matchResult,
            boolean failAll)
            throws MatchException {
        if (!fullInstance.isInstanceOf(Reference.class) || !parsedInstance.isInstanceOf(Reference.class)){
            throw new MatchException("ParsedReferenceMatchStrategy only supports type Reference");
        }
        Reference fullRef = CdmBase.deproxy(fullInstance, Reference.class);
        Reference parsedRef = CdmBase.deproxy(parsedInstance, Reference.class);
        if (fullRef == null || parsedRef == null){
            matchResult.addNullMatching(fullRef, parsedRef);
            return;
        }else if (!fullRef.getType().equals(parsedRef.getType())){
            matchResult.addNoTypeMatching(fullRef.getType(), parsedRef.getType());
            return;
        }else{
            ReferenceType type = fullRef.getType();
            IMatchStrategy strategy = getStrategyByType(type);
            strategy.invoke(fullRef, parsedRef, matchResult, failAll);
            return;
        }
    }

    public IMatchStrategy getStrategyByType(ReferenceType type) {
        if (type.equals(ReferenceType.Article)){
            return articleStrategy;
        }else if (type.equals(ReferenceType.Book)){
            return bookStrategy;
        }else if (type.equals(ReferenceType.BookSection)){
            return bookSectionStrategy;
        }else if (type.equals(ReferenceType.Journal)){
            return journalStrategy;
        }else if (type.equals(ReferenceType.Generic)){
            //TODO
            return genericStrategy;
        }else{
            //TODO
            return genericStrategy;
        }
    }
}
