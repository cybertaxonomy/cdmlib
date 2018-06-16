/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.term.TermAllGroupsCollector;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermSecondPassGroupingCollector;
import org.apache.lucene.util.BytesRef;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 *
 * @author Andreas Kohlbecker
 * @since Dec 21, 2011
 *
 */
public class LuceneSearch {

    protected String groupByField = "id";

    public final static String ID_FIELD = "id";

    public static final Logger logger = Logger.getLogger(LuceneSearch.class);

    protected ILuceneIndexToolProvider toolProvider;

    protected IndexSearcher searcher;

    protected SortField[] sortFields;

    private Class<? extends CdmBase> directorySelectClass;

    private BooleanQuery filter = null;

    protected Class<? extends CdmBase> getDirectorySelectClass() {
        return pushAbstractBaseTypeDown(directorySelectClass);
    }

    /**
     * classFilter
     */
    protected Class<? extends CdmBase> cdmTypeRestriction;


    public Class<? extends CdmBase> getCdmTypRestriction() {
        return cdmTypeRestriction;
    }

    /**
     * @return the filter
     */
    public BooleanQuery getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(BooleanQuery filter) {
        this.filter = filter;
    }

    /**
     * Sets the Class to use as filter criterion, in case the supplied Class equals the
     * <code>directorySelectClass</code> the Class is set to <code>null</code>
     * @param clazz
     */
    public void setCdmTypRestriction(Class<? extends CdmBase> clazz) {

        /*
         * NOTE:
         * we must not use the getter of directorySelectClass
         * since we need the abstract base classes here!!!!
         */
        if(clazz != null && clazz.equals(directorySelectClass)){
            clazz = null;
        }
        this.cdmTypeRestriction = clazz;
    }

    /**
     * The MAX_HITS_ALLOWED value must be one less than Integer.MAX_VALUE
     * otherwise PriorityQueue will produce an exception since it
     * will always add 1 to the maxhits so Integer.MAX_VALUE
     * would become Integer.MIN_VALUE
     */
    public final int MAX_HITS_ALLOWED = 10000;

    protected BooleanQuery query;

    protected String[] highlightFields = new String[0];

    private int maxDocsPerGroup = 10;


    public int getMaxDocsPerGroup() {
        return maxDocsPerGroup;
    }

    public void setMaxDocsPerGroup(int maxDocsPerGroup) {
        this.maxDocsPerGroup = maxDocsPerGroup;
    }

    /**
     * @param session
     */
    public LuceneSearch(ILuceneIndexToolProvider toolProvider, Class<? extends CdmBase> directorySelectClass) {
         this.toolProvider = toolProvider;
         this.directorySelectClass = directorySelectClass;
    }

    /**
     * @param session
     */
    public LuceneSearch(ILuceneIndexToolProvider toolProvider, String groupByField, Class<? extends CdmBase> directorySelectClass) {
        this.toolProvider = toolProvider;
        this.directorySelectClass = directorySelectClass;
        this.groupByField = groupByField;
    }

    /**
     * TODO the abstract base class DescriptionElementBase can not be used, so
     * we are using an arbitrary subclass to find the DirectoryProvider, future
     * versions of hibernate search my allow using abstract base classes see
     * {@link http://stackoverflow.com/questions/492184/how-do-you-find-all-subclasses-of-a-given-class-in-java}
     *
     * @param type must not be null
     * @return
     */
    private Class<? extends CdmBase> pushAbstractBaseTypeDown(Class<? extends CdmBase> type) {
        Class<? extends CdmBase> returnType = type;
        if (type.equals(DescriptionElementBase.class)) {
            returnType = TextData.class;
        }
        if (type.equals(TaxonBase.class)) {
            returnType = Taxon.class;
        }
//        if (type.equals(TaxonName.class)) {
//            returnType = NonViralName.class;
//        }
        return returnType;
    }

    protected LuceneSearch() {

    }

    /**
     * @return
     */
    public IndexSearcher getSearcher() {
        if(searcher == null){
            searcher = new IndexSearcher(toolProvider.getIndexReaderFor(directorySelectClass));
//            searcher.setDefaultFieldSortScoring(true, true);
        }
        return searcher;
    }

    /**
     * Convenience method which delegated the call to the available
     * {@link ILuceneIndexToolProvider#getAnalyzerFor(Class)} method.
     *
     * @return the Analyzer suitable for the <code>directorySelectClass</code>
     * of the LuceneSearch
     */
    public Analyzer getAnalyzer() {
        return toolProvider.getAnalyzerFor(directorySelectClass);
    }

    /**
     * @param luceneQueryString
     * @param cdmTypeRestriction the type as additional filter criterion
     * @param pageSize if the page size is null or in an invalid range it will be set to MAX_HITS_ALLOWED
     * @param pageNumber a 0-based index of the page to return, will default to 0 if null or negative.
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public TopGroups<BytesRef> executeSearch(String luceneQueryString, Integer pageSize, Integer pageNumber) throws ParseException, IOException {

        Query luceneQuery = parse(luceneQueryString);
        setQuery(luceneQuery);
        return executeSearch(pageSize, pageNumber);
    }

    /**
     * @param luceneQueryString
     * @return
     * @throws ParseException
     */
    public Query parse(String luceneQueryString) throws ParseException {
        logger.debug("luceneQueryString to be parsed: " + luceneQueryString);
        Query luceneQuery = toolProvider.getQueryParserFor(directorySelectClass, false).parse(luceneQueryString);
        return luceneQuery;
    }

    /**
     * @param maxNoOfHits
     * @return
     * @throws IOException
     */
    public TopDocs executeSearch(int maxNoOfHits) throws IOException {
        BooleanQuery fullQuery = expandQuery();
        logger.info("lucene query string to be parsed: " + fullQuery.toString());
        return getSearcher().search(fullQuery, maxNoOfHits, Sort.RELEVANCE, true, true);

    }
    /**
     * @param pageSize if the page size is null or in an invalid range it will be set to MAX_HITS_ALLOWED
     * @param pageNumber a 0-based index of the page to return, will default to 0 if null or negative.
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public TopGroups<BytesRef> executeSearch(Integer pageSize, Integer pageNumber) throws ParseException, IOException {


        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }
        if(pageSize == null || pageSize <= 0 || pageSize > MAX_HITS_ALLOWED){
            pageSize = MAX_HITS_ALLOWED;
            logger.info("limiting pageSize to MAX_HITS_ALLOWED = " + MAX_HITS_ALLOWED + " items");
        }

        BooleanQuery fullQuery = expandQuery();
        logger.info("final query: " + fullQuery.toString());

        int offset = pageNumber * pageSize;
        int limit = (pageNumber + 1) * pageSize - 1 ;
        logger.debug("start: " + offset + "; limit:" + limit);

        // sorting
        Sort groupSort = null;
        Sort withinGroupSort = Sort.RELEVANCE;
        if(sortFields != null && sortFields.length > 0){
            groupSort = new Sort(sortFields);
        } else {
            groupSort = Sort.RELEVANCE; // == SortField.FIELD_SCORE !!
        }

        // perform the search (needs two passes for grouping)
        if(logger.isDebugEnabled()){
            logger.debug("Grouping: sortFields=" + Arrays.toString(sortFields) + ", groupByField=" + groupByField +
                    ", groupSort=" + groupSort + ", withinGroupSort=" + withinGroupSort + ", limit=" + limit + ", maxDocsPerGroup="+ maxDocsPerGroup);
        }
        // - first pass
        TermFirstPassGroupingCollector firstPassCollector = new TermFirstPassGroupingCollector(groupByField, groupSort, limit);

        getSearcher().search(fullQuery, firstPassCollector);
        Collection<SearchGroup<BytesRef>> topGroups = firstPassCollector.getTopGroups(0, true); // no offset here since we need the first item for the max score

        if (topGroups == null) {
              return null;
        }
        // - flags for second pass
        boolean getScores = false;
        boolean getMaxScores = true;
        if(groupSort.getSort()[0] != SortField.FIELD_SCORE){
            getMaxScores = false;
            // see inner class TopGroupsWithMaxScore
            logger.error("Fist sort field must be SortField.FIELD_SCORE otherwise the max score value will not be correct! MaxScore calculation will be skipped");
        }
        boolean fillFields = true;
        TermAllGroupsCollector allGroupsCollector = new TermAllGroupsCollector(groupByField);
        TermSecondPassGroupingCollector secondPassCollector = new TermSecondPassGroupingCollector(
                groupByField, topGroups, groupSort, withinGroupSort, maxDocsPerGroup , getScores,
                getMaxScores, fillFields
                );
        getSearcher().search(fullQuery, MultiCollector.wrap(secondPassCollector, allGroupsCollector));

        TopGroups<BytesRef> groupsResult = secondPassCollector.getTopGroups(0); // no offset here since we need the first item for the max score

        // --- set the max score for the group results

        // get max score from very first result
        float maxScore = groupsResult.groups[0].maxScore;

        if(logger.isDebugEnabled()){
            logger.debug("TopGroups: maxScore=" + maxScore + ", offset=" + offset +
                    ", totalGroupCount=" + allGroupsCollector.getGroupCount() +
                    ", totalGroupedHitCount=" + groupsResult.totalGroupedHitCount);
        }

        TopGroups<BytesRef> newTopGroups;
        if(offset > 0){
            GroupDocs<BytesRef>[] newGroupDocs = new GroupDocs[groupsResult.groups.length - offset];
            for(int i = offset; i < groupsResult.groups.length; i++){
                newGroupDocs[i - offset] = groupsResult.groups[i];
            }
            newTopGroups = new TopGroups<BytesRef>(
                    groupsResult.groupSort,
                    groupsResult.withinGroupSort,
                    groupsResult.totalHitCount,
                    groupsResult.totalGroupedHitCount,
                        newGroupDocs,
                        maxScore);
        } else {
            newTopGroups = groupsResult;
        }
        TopGroups<BytesRef> topGroupsWithMaxScore = new TopGroups<>(newTopGroups, allGroupsCollector.getGroupCount());
        // --- done with max score for the group results

        return topGroupsWithMaxScore;
    }

    /**
     * expands the query by adding a type restriction if the
     * <code>cdmTypeRestriction</code> is not <code>NULL</code>
     * and adds the <code>filter</code> as Boolean query
     * clause with {@link Occur#FILTER}
     */
    protected BooleanQuery expandQuery() {
        BooleanQuery fullQuery = null;
        Builder fullQueryBuilder = null;

        if(cdmTypeRestriction != null){
            fullQueryBuilder = QueryFactory.addTypeRestriction(query, cdmTypeRestriction);
        }

        if(filter != null) {
            if(fullQueryBuilder == null) {
                fullQueryBuilder = new Builder();
                fullQueryBuilder.add(this.query, Occur.MUST);
            }
            fullQueryBuilder.add(filter, Occur.FILTER);
        }

        if(fullQueryBuilder != null) {
            fullQuery = fullQueryBuilder.build();
        } else {
            fullQuery = this.query;
        }

        logger.debug("expandedQuery: " + fullQuery.toString());
        return fullQuery;
    }

    public void setQuery(Query query) {
        if( query instanceof BooleanQuery) {
            this.query = (BooleanQuery)query;
        } else {
            Builder builder = new Builder();
            this.query = builder.add(query, Occur.MUST).build();
        }
    }

    public BooleanQuery getQuery() {
        return query;
    }

    public BooleanQuery getExpandedQuery() {
        expandQuery();
        return query;
    }

    public SortField[] getSortFields() {
        return sortFields;
    }

    public void setSortFields(SortField[] sortFields) {
        this.sortFields = sortFields;
    }

    public void setHighlightFields(String[] textFieldNamesAsArray) {
        this.highlightFields = textFieldNamesAsArray;
    }

    public String[] getHighlightFields() {
        return this.highlightFields;
    }

}
