// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.spatial.impl.Point;
import org.hibernate.search.spatial.impl.Rectangle;

import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * QueryFactory creates queries for a specific lucene index that means queries
 * specific to the various CDM base types. Therefore the QueryFactory hold a
 * reference to a {@link LuceneSearch} instance which has been created for a
 * CDM base type.<br>
 * The field names used in queries created on free text fields are remembered
 * and can be accessed by {@link #getTextFieldNames()} or {@link #getTextFieldNamesAsArray()}.
 * This is useful for highlighting the matches with {@link LuceneSearch#setHighlightFields(String[])}
 * <p>
 * The index specific methods from {@link LuceneSearch} which are
 * used by QueryFactory directly or indirectly are:
 * <ul>
 * <li>{@link LuceneSearch#getAnalyzer()}</li>
 * </ul>
 *
 *
 * @author a.kohlbecker
 * @date Sep 14, 2012
 *
 */
public class QueryFactory {

    public static final Logger logger = Logger.getLogger(QueryFactory.class);

    protected ILuceneIndexToolProvider toolProvider;

    Set<String> textFieldNames = new HashSet<String>();

    Map<Class<? extends CdmBase>, IndexSearcher> indexSearcherMap = new HashMap<Class<? extends CdmBase>, IndexSearcher>();

    private final Class<? extends CdmBase> cdmBaseType;

    public Set<String> getTextFieldNames() {
        return textFieldNames;
    }

    public String[] getTextFieldNamesAsArray() {
        return textFieldNames.toArray(new String[textFieldNames.size()]);
    }

    public QueryFactory(ILuceneIndexToolProvider toolProvider, Class<? extends CdmBase> cdmBaseType){
        this.cdmBaseType = cdmBaseType;
        this.toolProvider = toolProvider;
    }

    /**
     * Creates a new Term query. Depending on whether <code>isTextField</code> is set true or not the
     * supplied <code>queryString</code> will be parsed by using the according analyzer or not.
     * Setting <code>isTextField</code> to <code>false</code> is useful for searching for uuids etc.
     *
     * @param fieldName
     * @param queryString
     * @param isTextField whether this field is a field containing free text in contrast to e.g. ID fields.
     *     If <code>isTextField</code> is set <code>true</code> the <code>queryString</code> will be parsed by
     *     using the according analyzer.
     * @return the resulting <code>TermQuery</code> or <code>null</code> in case of an <code>ParseException</code>
     *
     * TODO consider throwing the ParseException !!!!
     */
    public Query newTermQuery(String fieldName, String queryString, boolean isTextField) {

        String luceneQueryString = fieldName + ":(" + queryString + ")";
        if (isTextField) {
            textFieldNames.add(fieldName);
            // in order to support the full query syntax we must use the parser
            // here
            try {
                return toolProvider.getQueryParserFor(cdmBaseType).parse(luceneQueryString);
            } catch (ParseException e) {
                logger.error(e);
            }
            return null;
        } else {
            return new TermQuery(new Term(fieldName, queryString));
        }
    }

    /**
     * only to be used for text fields, see {@link #newTermQuery(String, String, boolean)}
     * @param fieldName
     * @param queryString
     * @return a {@link TermQuery} or a {@link WildcardQuery}
     */
    public Query newTermQuery(String fieldName, String queryString){
        return newTermQuery(fieldName, queryString, true);
    }

    /**
     * DefinedTerms are stored in the Lucene index by the
     * {@link DefinedTermBaseClassBridge} in a consistent way. One field per
     * language and also in one additional field for all languages. This method
     * is a convenient means to retrieve a Lucene query string for such the
     * fields.
     *
     * @param name
     *            name of the term field as in the Lucene index. The field must
     *            have been written to Lucene document by
     *            {@link DefinedTermBaseClassBridge}
     *
     * @param languages
     *            the languages to search for exclusively. Can be
     *            <code>null</code> to search in all languages
     * @return
     */
    public Query newDefinedTermQuery(String name, String queryString, List<Language> languages) {

        BooleanQuery localizedTermQuery = new BooleanQuery();
        localizedTermQuery.add(newTermQuery(name + ".label", queryString), Occur.SHOULD);
        if(languages == null || languages.size() == 0){
            localizedTermQuery.add(newTermQuery(name + ".representation.text.ALL", queryString), Occur.SHOULD);
            localizedTermQuery.add(newTermQuery(name + ".representation.label.ALL", queryString), Occur.SHOULD);
            localizedTermQuery.add(newTermQuery(name + ".representation.abbreviatedLabel.ALL", queryString), Occur.SHOULD);

        } else {
            for(Language lang : languages){
                localizedTermQuery.add(newTermQuery(name + ".representation.text." + lang.getUuid().toString(), queryString), Occur.SHOULD);
                localizedTermQuery.add(newTermQuery(name + ".representation.label." + lang.getUuid().toString(), queryString), Occur.SHOULD);
                localizedTermQuery.add(newTermQuery(name + ".representation.abbreviatedLabel." + lang.getUuid().toString(), queryString), Occur.SHOULD);
            }
        }
        return localizedTermQuery;
    }

    /**
     * MultilanguageString maps are stored in the Lucene index by the
     * {@link MultilanguageTextFieldBridge } in a consistent way. One field per
     * language and also in one additional field for all languages. This method
     * is a convenient means to retrieve a Lucene query string for such the
     * fields.
     *
     * @param name
     *            name of the term field as in the Lucene index. The field must
     *            have been written to Lucene document by
     *            {@link DefinedTermBaseClassBridge}
     * @param languages
     *            the languages to search for exclusively. Can be
     *            <code>null</code> to search in all languages
     * @return
     */
    public Query newMultilanguageTextQuery(String name, String queryString, List<Language> languages) {

        BooleanQuery localizedTermQuery = new BooleanQuery();
        localizedTermQuery.add(newTermQuery(name + ".label", queryString), Occur.SHOULD);
        if(languages == null || languages.size() == 0){
            localizedTermQuery.add(newTermQuery(name + ".ALL", queryString), Occur.SHOULD);
        } else {
            for(Language lang : languages){
                localizedTermQuery.add(newTermQuery(name + "." + lang.getUuid().toString(), queryString), Occur.SHOULD);
            }
        }
        return localizedTermQuery;
    }

    /**
     * @param idFieldName
     * @param entitiy
     * @return
     */
    public Query newEntityIdQuery(String idFieldName, CdmBase entitiy){
        return newTermQuery(idFieldName, String.valueOf(entitiy.getId()), false);
    }

    /**
     * @param idFieldName
     * @param entitiy
     * @return
     */
    public Query newEntityIdsQuery(String idFieldName, List<? extends CdmBase> entities){
        BooleanQuery idInQuery = new BooleanQuery();
        if(entities != null && entities.size() > 0 ){
            for(CdmBase entity : entities){
                idInQuery.add(newEntityIdQuery(idFieldName, entity), Occur.SHOULD);
            }
        }
        return idInQuery;
    }

    /**
     * @param idFieldName
     * @return
     */
    public Query newIsNotNullQuery(String idFieldName){
        return new TermQuery(new Term(NotNullAwareIdBridge.notNullField(idFieldName), NotNullAwareIdBridge.NOT_NULL_VALUE));
    }

    /**
     * @param uuidFieldName
     * @param entity
     * @return
     */
    public Query newEntityUuidQuery(String uuidFieldName, IdentifiableEntity entity) {
        return newTermQuery(uuidFieldName, entity.getUuid().toString(), false);
    }

    /**
     * creates a query for searching for documents in which the field specified by <code>uuidFieldName</code> matches at least one of the uuid
     * of the <code>entities</code>, the sql equivalent of this is <code>WHERE uuidFieldName IN (uuid_1, uuid_2, ...) </code>.
     * @param uuidFieldName
     * @param entities
     * @return
     */
    public Query newEntityUuidsQuery(String uuidFieldName, List<? extends IdentifiableEntity> entities){

        BooleanQuery uuidInQuery = new BooleanQuery();
        if(entities != null && entities.size() > 0 ){
            for(IdentifiableEntity entity : entities){
                uuidInQuery.add(newEntityUuidQuery(uuidFieldName, entity), Occur.SHOULD);
            }
        }
        return uuidInQuery;
    }


    /**
     * creates a query for searching for documents in which the field specified by <code>uuidFieldName</code> matches at least one of the
     * supplied <code>uuids</code>
     * the sql equivalent of this is <code>WHERE uuidFieldName IN (uuid_1, uuid_2, ...) </code>.
     * @param uuidFieldName
     * @param entities
     * @return
     */
    public Query newUuidQuery(String uuidFieldName, List<UUID> uuids){

        BooleanQuery uuidInQuery = new BooleanQuery();
        if(uuids != null && uuids.size() > 0 ){
            for(UUID uuid : uuids){
                uuidInQuery.add(newTermQuery(uuidFieldName, uuids.toString(), false), Occur.SHOULD);
            }
        }
        return uuidInQuery;
    }


    /**
     * Returns a Lucene Query which rely on double numeric range query
     * on Latitude / Longitude
     *
     *(+/- copied from {@link SpatialQueryBuilderFromPoint#buildSpatialQueryByRange(Point, double, String)})
     *
     * @param center center of the search discus
     * @param radius distance max to center in km
     * @param fieldName name of the Lucene Field implementing Coordinates
     * @return Lucene Query to be used in a search
     * @see Query
     * @see org.hibernate.search.spatial.Coordinates
     */
    public static Query buildSpatialQueryByRange(Rectangle boundingBox, String fieldName) {

        String latitudeFieldName = fieldName + "_HSSI_Latitude";
        String longitudeFieldName = fieldName + "_HSSI_Longitude";

        Query latQuery= NumericRangeQuery.newDoubleRange(
                latitudeFieldName, boundingBox.getLowerLeft().getLatitude(),
                boundingBox.getUpperRight().getLatitude(), true, true
        );

        Query longQuery= null;
        if ( boundingBox.getLowerLeft().getLongitude() <= boundingBox.getUpperRight().getLongitude() ) {
            longQuery = NumericRangeQuery.newDoubleRange( longitudeFieldName, boundingBox.getLowerLeft().getLongitude(),
                    boundingBox.getUpperRight().getLongitude(), true, true );
        }
        else {
            longQuery= new BooleanQuery();
            ( (BooleanQuery) longQuery).add( NumericRangeQuery.newDoubleRange( longitudeFieldName, boundingBox.getLowerLeft().getLongitude(),
                    180.0, true, true ), BooleanClause.Occur.SHOULD );
            ( (BooleanQuery) longQuery).add( NumericRangeQuery.newDoubleRange( longitudeFieldName, -180.0,
                    boundingBox.getUpperRight().getLongitude(), true, true ), BooleanClause.Occur.SHOULD );
        }

        BooleanQuery boxQuery = new BooleanQuery();
        boxQuery.add( latQuery, BooleanClause.Occur.MUST );
        boxQuery.add( longQuery, BooleanClause.Occur.MUST );

        return new FilteredQuery(
                new MatchAllDocsQuery(),
                new QueryWrapperFilter( boxQuery )
        );
    }

    /**
     *
     * @param fromField
     * @param toField
     * @param joinFromQuery
     * @param fromType
     * @return
     * @throws IOException
     */
    public Query newJoinQuery(String fromField, String toField, Query joinFromQuery,
            Class<? extends CdmBase> fromType) throws IOException {
            boolean multipleValuesPerDocument = true;
            ScoreMode scoreMode = ScoreMode.Max;
            return JoinUtil.createJoinQuery(
                    // need to use the sort field of the id field since
                    // ScoreMode.Max forces the fromField to be a docValue
                    // field of type [SORTED, SORTED_SET]
                    fromField + "__sort",
                    multipleValuesPerDocument, toField,
                    joinFromQuery, indexSearcherFor(fromType), scoreMode);
    }

    /**
     * Creates a class restriction query and wraps the class restriction
     * query and the given <code>query</code> into a BooleanQuery where both must match.
     * <p>
     * TODO instead of using a BooleanQuery for the class restriction it would be much more
     *  performant to use a {@link Filter} instead.
     *
     * @param cdmTypeRestriction
     * @param query
     * @return
     */
    public static Query addTypeRestriction(Query query, Class<? extends CdmBase> cdmTypeRestriction) {

        Query fullQuery;
        BooleanQuery filteredQuery = new BooleanQuery();
        BooleanQuery classFilter = new BooleanQuery();

        Term t = new Term(ProjectionConstants.OBJECT_CLASS, cdmTypeRestriction.getName());
        TermQuery termQuery = new TermQuery(t);

        classFilter.setBoost(0);
        classFilter.add(termQuery, BooleanClause.Occur.SHOULD);

        filteredQuery.add(query, BooleanClause.Occur.MUST);
        filteredQuery.add(classFilter, BooleanClause.Occur.MUST);

        fullQuery = filteredQuery;
        return fullQuery;
    }

    /**
     * @param clazz
     * @return
     */
    private IndexSearcher indexSearcherFor(Class<? extends CdmBase> clazz) {
        if(indexSearcherMap.get(clazz) == null){

            IndexReader indexReader = toolProvider.getIndexReaderFor(clazz);
            IndexSearcher searcher = new IndexSearcher(indexReader);
//            searcher.setDefaultFieldSortScoring(true, true);
            indexSearcherMap.put(clazz, searcher);
        }
        IndexSearcher indexSearcher = indexSearcherMap.get(clazz);
        return indexSearcher;
    }

}
