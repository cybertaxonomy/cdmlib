// $Id$
/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.search;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
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
    private QueryParser getQueryParser() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
        Analyzer analyzer = searchFactory.getAnalyzer(type);
        QueryParser parser = new QueryParser("titleCache", analyzer);
        return parser;
    }

    /**
     * @param luceneQueryString
     * @param clazz the type as additional filter criterion
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public TopDocs executeSearch(String luceneQueryString, Class<? extends CdmBase> clazz) throws ParseException, IOException {

        Query query;
        logger.debug("luceneQueryString given: " + luceneQueryString);
        Query luceneQuery = getQueryParser().parse(luceneQueryString);

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
        logger.debug("final query: " + query.toString());
        TopDocs topDocsResultSet = getSearcher().search(query, null, 100);

        return topDocsResultSet;
    }








}
