// $Id$
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
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TermAllGroupsCollector;
import org.apache.lucene.search.grouping.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.TermSecondPassGroupingCollector;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.hibernate.search.ProjectionConstants;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;

import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;

/**
 *
 * @author Andreas Kohlbecker
 * @date Dec 21, 2011
 *
 */
public class LuceneSearch {

    private static final String GROUP_BY_FIELD = GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD;

    public final static String ID_FIELD = "id";

    public static final Logger logger = Logger.getLogger(LuceneSearch.class);

    protected Session session;

    protected IndexSearcher searcher;

    private SortField[] sortFields;

    private Class<? extends CdmBase> directorySelectClass;

    protected Class<? extends CdmBase> getDirectorySelectClass() {
        return pushAbstractBaseTypeDown(directorySelectClass);
    }

    /**
     * classFilter
     */
    private Class<? extends CdmBase> clazz;


    public Class<? extends CdmBase> getClazz() {
        return clazz;
    }

    /**
     * Sets the Class to use as filter criterion, in case the supplied Class equals the
     * <code>directorySelectClass</code> the Class is set to <code>null</code>
     * @param clazz
     */
    public void setClazz(Class<? extends CdmBase> clazz) {

        /*
         * NOTE:
         * we must not use the getter of directorySelectClass
         * since we need the abstract base classes here!!!!
         */
        if(clazz != null && clazz.equals(directorySelectClass)){
            clazz = null;
        }
        this.clazz = clazz;
    }

    /**
     * The MAX_HITS_ALLOWED value must be one less than Integer.MAX_VALUE
     * otherwise PriorityQueue will produce an exception since it
     * will always add 1 to the maxhits so Integer.MAX_VALUE
     * would become Integer.MIN_VALUE
     */
    public final int MAX_HITS_ALLOWED = 10000;

    protected Query query;

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
    public LuceneSearch(Session session, Class<? extends CdmBase> directorySelectClass) {
         this.session = session;
         this.directorySelectClass = directorySelectClass;
    }

    /**
     * TODO the abstract base class DescriptionElementBase can not be used, so
     * we are using an arbitraty subclass to find the DirectoryProvider, future
     * versions of hibernate search my allow using abstract base classes see
     * http
     * ://stackoverflow.com/questions/492184/how-do-you-find-all-subclasses-of
     * -a-given-class-in-java
     *
     * @param type must not be null
     * @return
     */
    protected Class<? extends CdmBase> pushAbstractBaseTypeDown(Class<? extends CdmBase> type) {
        if (type.equals(DescriptionElementBase.class)) {
            type = TextData.class;
        }
        if (type.equals(TaxonBase.class)) {
            type = Taxon.class;
        }
        return type;
    }

    protected LuceneSearch() {

    }

    /**
     * @return
     */
    public IndexSearcher getSearcher() {
        if(searcher == null){
            searcher = new IndexSearcher(getIndexReader());
            searcher.setDefaultFieldSortScoring(true, true);
        }
        return searcher;
    }

    /**
     * @return
     */
    public IndexReader getIndexReader() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();

//        OLD
//        DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders(getDirectorySelectClass());
//        logger.info(directoryProviders[0].getDirectory().toString());

//        ReaderProvider readerProvider = searchFactory.getReaderProvider();
//        IndexReader reader = readerProvider.openReader(directoryProviders[0]);
        
        IndexReader reader = searchFactory.getIndexReaderAccessor().open(getDirectorySelectClass());
        return reader;
    }

    /**
     * @return
     */
    public QueryParser getQueryParser() {
        Analyzer analyzer = getAnalyzer();
        Version version = AlternativeSpellingSuggestionParser.version;
        QueryParser parser = new QueryParser(version,  "titleCache", analyzer);
        return parser;
    }

    /**
     * @return
     */
    public Analyzer getAnalyzer() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
        Analyzer analyzer = searchFactory.getAnalyzer(getDirectorySelectClass());
        return analyzer;
    }

    /**
     * @param luceneQueryString
     * @param clazz the type as additional filter criterion
     * @param pageSize if the page size is null or in an invalid range it will be set to MAX_HITS_ALLOWED
     * @param pageNumber a 0-based index of the page to return, will default to 0 if null or negative.
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public TopGroupsWithMaxScore executeSearch(String luceneQueryString, Integer pageSize, Integer pageNumber) throws ParseException, IOException {

        Query luceneQuery = parse(luceneQueryString);
        this.query = luceneQuery;

        return executeSearch(pageSize, pageNumber);
    }

    /**
     * @param luceneQueryString
     * @return
     * @throws ParseException
     */
    public Query parse(String luceneQueryString) throws ParseException {
        logger.debug("luceneQueryString to be parsed: " + luceneQueryString);
        Query luceneQuery = getQueryParser().parse(luceneQueryString);
        return luceneQuery;
    }

    /**
     * @param luceneQuery
     * @param clazz the type as additional filter criterion
     * @param pageSize if the page size is null or in an invalid range it will be set to MAX_HITS_ALLOWED
     * @param pageNumber a 0-based index of the page to return, will default to 0 if null or negative.
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public TopGroupsWithMaxScore executeSearch(Integer pageSize, Integer pageNumber) throws ParseException, IOException {


        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }
        if(pageSize == null || pageSize <= 0 || pageSize > MAX_HITS_ALLOWED){
            pageSize = MAX_HITS_ALLOWED;
            logger.info("limiting pageSize to MAX_HITS_ALLOWED = " + MAX_HITS_ALLOWED + " items");
        }

        Query fullQuery = expandQuery();
        logger.info("final query: " + fullQuery.toString());

        int offset = pageNumber * pageSize;
        int limit = (pageNumber + 1) * pageSize - 1 ;
        logger.debug("start: " + offset + "; limit:" + limit);

        // sorting
        Sort groupSort = null;
        Sort withinGroupSort = Sort.RELEVANCE;
        if(sortFields != null && sortFields.length > 0){
            if(sortFields[0] != SortField.FIELD_SCORE){
                throw new RuntimeException("Fist sort field must be SortField.FIELD_SCORE");
            }
            groupSort = new Sort(sortFields);
        } else {
            groupSort = Sort.RELEVANCE; // == SortField.FIELD_SCORE !!
        }

        // perform the search (needs two passes for grouping)
        // - first pass
        TermFirstPassGroupingCollector firstPassCollector = new TermFirstPassGroupingCollector(GROUP_BY_FIELD, withinGroupSort, limit);
        getSearcher().search(fullQuery, firstPassCollector);
        Collection<SearchGroup<String>> topGroups = firstPassCollector.getTopGroups(0, true); // no offset here since we need the first item for the max score

        if (topGroups == null) {
              return null;
        }
        // - second pass
        boolean getScores = true;
        boolean getMaxScores = true;
        boolean fillFields = true;
        TermAllGroupsCollector allGroupsCollector = new TermAllGroupsCollector(GROUP_BY_FIELD);
        TermSecondPassGroupingCollector secondPassCollector = new TermSecondPassGroupingCollector(GROUP_BY_FIELD, topGroups, groupSort, withinGroupSort, maxDocsPerGroup , getScores, getMaxScores, fillFields);
        getSearcher().search(fullQuery, MultiCollector.wrap(secondPassCollector, allGroupsCollector));

        TopGroups<String> groupsResult = secondPassCollector.getTopGroups(0); // no offset here since we need the first item for the max score

        // get max score from very first result
        float maxScore = groupsResult.groups[0].maxScore;
        TopGroupsWithMaxScore topGroupsWithMaxScore = new TopGroupsWithMaxScore(groupsResult, offset, allGroupsCollector.getGroupCount(), maxScore);

        return topGroupsWithMaxScore;
    }

    /**
     * @param clazz
     */
    protected Query expandQuery() {
        Query fullQuery;
        if(clazz != null){
            BooleanQuery filteredQuery = new BooleanQuery();
            BooleanQuery classFilter = new BooleanQuery();

            Term t = new Term(ProjectionConstants.OBJECT_CLASS, clazz.getName());
            TermQuery termQuery = new TermQuery(t);

            classFilter.setBoost(0);
            classFilter.add(termQuery, BooleanClause.Occur.SHOULD);

            filteredQuery.add(this.query, BooleanClause.Occur.MUST);
            filteredQuery.add(classFilter, BooleanClause.Occur.MUST);

            fullQuery = filteredQuery;
        } else {
            fullQuery = this.query;
        }
        return fullQuery;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public Query getExpandedQuery() {
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

    /**
     * may become obsolete with lucene 4.x when the TopGroups has a field for maxScore.
     *
     * @author a.kohlbecker
     * @date Oct 4, 2012
     *
     */
    public class TopGroupsWithMaxScore{
        public TopGroups<String> topGroups;
        public float maxScore = Float.NaN;

        TopGroupsWithMaxScore(TopGroups<String> topGroups, int offset, int totalGroupCount, float maxScore){
            this.maxScore = maxScore;
            TopGroups<String> newTopGroups;
            if(offset > 0){
                GroupDocs<String>[] newGroupDocs = new GroupDocs[topGroups.groups.length - offset];
                for(int i = offset; i < topGroups.groups.length; i++){
                    newGroupDocs[i - offset] = topGroups.groups[i];
                }
                newTopGroups = new TopGroups<String>(
                            topGroups.groupSort,
                            topGroups.withinGroupSort,
                            topGroups.totalHitCount,
                            topGroups.totalGroupedHitCount,
                            newGroupDocs);
            } else {
                newTopGroups = topGroups;
            }
            this.topGroups = new TopGroups<String>(newTopGroups, totalGroupCount);
        }

    }

}
