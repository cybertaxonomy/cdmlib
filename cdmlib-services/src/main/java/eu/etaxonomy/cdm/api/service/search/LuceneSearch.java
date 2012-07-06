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
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.hibernate.Session;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 *
 * @author Andreas Kohlbecker
 * @date Dec 21, 2011
 *
 */
public class LuceneSearch {

    public static final Logger logger = Logger.getLogger(LuceneSearch.class);

    private Session session;

    private Searcher searcher;

    private Class<? extends CdmBase> type;


    /**
     * The MAX_HITS_ALLOWED value must be one less than Integer.MAX_VALUE
     * otherwise PriorityQueue will produce an exception since it
     * will always add 1 to the maxhits so Integer.MAX_VALUE
     * would become Integer.MIN_VALUE
     */
    public final int MAX_HITS_ALLOWED = 10000;


    /**
     * @param session
     */
    public LuceneSearch(Session session, Class<? extends CdmBase> type) {
         this.session = session;

         //TODO the abstract base class DescriptionElementBase can not be used, so we are using an arbitraty
         ///    subclass to find the DirectoryProvider,
         //     future versions of hibernate search my allow using abstract base classes
         // see http://stackoverflow.com/questions/492184/how-do-you-find-all-subclasses-of-a-given-class-in-java
         if(type.equals(DescriptionElementBase.class)) {
             type = TextData.class;
         }
         this.type = type;
    }

    /**
     * @return
     */
    public Searcher getSearcher() {
        if(searcher == null){
            SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();

            DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders(type);
            logger.info(directoryProviders[0].getDirectory().toString());

            ReaderProvider readerProvider = searchFactory.getReaderProvider();
            IndexReader reader = readerProvider.openReader(directoryProviders[0]);
            searcher = new IndexSearcher(reader);
        }
        return searcher;
    }

    /**
     * @return
     */
    public QueryParser getQueryParser() {
        Analyzer analyzer = getAnalyzer();
        QueryParser parser = new QueryParser("titleCache", analyzer);
        return parser;
    }

    /**
     * @return
     */
    public Analyzer getAnalyzer() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
        Analyzer analyzer = searchFactory.getAnalyzer(type);
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
    public TopDocs executeSearch(String luceneQueryString, Class<? extends CdmBase> clazz, Integer pageSize,
            Integer pageNumber, SortField[] sortFields) throws ParseException, IOException {

        Query luceneQuery = parse(luceneQueryString);

        return executeSearch(luceneQuery, clazz, pageSize, pageNumber, sortFields);
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
    public TopDocs executeSearch(Query luceneQuery, Class<? extends CdmBase> clazz, Integer pageSize,
            Integer pageNumber, SortField[] sortFields) throws ParseException, IOException {

        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }
        if(pageSize == null || pageSize <= 0 || pageSize > MAX_HITS_ALLOWED){
            pageSize = MAX_HITS_ALLOWED;
            logger.info("limiting pageSize to MAX_HITS_ALLOWED = " + MAX_HITS_ALLOWED + " items");
        }

        Query query;

        if(clazz != null){
            BooleanQuery classFilter = new BooleanQuery();
            classFilter.setBoost(0);
            Term t = new Term(DocumentBuilder.CLASS_FIELDNAME, clazz.getName());
            TermQuery termQuery = new TermQuery(t);
            classFilter.add(termQuery, BooleanClause.Occur.SHOULD);
            BooleanQuery filteredQuery = new BooleanQuery();
            filteredQuery.add(luceneQuery, BooleanClause.Occur.MUST);
            filteredQuery.add(classFilter, BooleanClause.Occur.MUST);
            query = filteredQuery;
        } else {
            query = luceneQuery;
        }
        logger.info("final query: " + query.toString());

        int start = pageNumber * pageSize;
        int limit = (pageNumber + 1) * pageSize - 1 ;

        logger.debug("start: " + start + "; limit:" + limit);

        TopDocs topDocs;
        if(sortFields != null && sortFields.length > 0){
            Sort sort = new Sort(sortFields);
            topDocs = getSearcher().search(query, null, limit, sort);
        } else {
            topDocs = getSearcher().search(query, null, limit);
        }


        //TODO when switched to Lucene 3.x which is included in hibernate 4.x
        //     use TopDocCollector.topDocs(int start, int howMany);
        //     since this method might be more memory save than our own implementation
//        TopDocs topDocs = hitCollector.topDocs();
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        int docsAvailableInPage = Math.min(scoreDocs.length - start, pageSize);
        logger.debug("docsAvailableInPage:" + docsAvailableInPage);

        ScoreDoc[] pagedDocs = new ScoreDoc[docsAvailableInPage];
        for(int i = 0; i < docsAvailableInPage; i++){
            pagedDocs[i] = scoreDocs[start + i];
        }
        TopDocs pagedTopDocs = new TopDocs(docsAvailableInPage, pagedDocs, topDocs.getMaxScore());
        //
        /////////////////////////////////////////////

        return pagedTopDocs;
    }








}
