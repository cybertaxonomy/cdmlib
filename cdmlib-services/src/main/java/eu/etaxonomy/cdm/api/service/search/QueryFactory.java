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
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
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
            queryString = queryString.trim();
            boolean isComplexPhraseQuery = queryString.matches("^\\\".*\\s+.*[\\*].*\\\"$");
            textFieldNames.add(fieldName);
            // in order to support the full query syntax we must use the parser
            // here
            try {
                return toolProvider.getQueryParserFor(cdmBaseType, isComplexPhraseQuery).parse(luceneQueryString);
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

        Builder localizedTermQueryBuilder = new Builder();
        localizedTermQueryBuilder.add(newTermQuery(name + ".label", queryString), Occur.SHOULD);
        if(languages == null || languages.size() == 0){
            localizedTermQueryBuilder.add(newTermQuery(name + ".representation.text.ALL", queryString), Occur.SHOULD);
            localizedTermQueryBuilder.add(newTermQuery(name + ".representation.label.ALL", queryString), Occur.SHOULD);
            localizedTermQueryBuilder.add(newTermQuery(name + ".representation.abbreviatedLabel.ALL", queryString), Occur.SHOULD);

        } else {
            for(Language lang : languages){
                localizedTermQueryBuilder.add(newTermQuery(name + ".representation.text." + lang.getUuid().toString(), queryString), Occur.SHOULD);
                localizedTermQueryBuilder.add(newTermQuery(name + ".representation.label." + lang.getUuid().toString(), queryString), Occur.SHOULD);
                localizedTermQueryBuilder.add(newTermQuery(name + ".representation.abbreviatedLabel." + lang.getUuid().toString(), queryString), Occur.SHOULD);
            }
        }
        return localizedTermQueryBuilder.build();
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

        Builder localizedTermQueryBuilder = new Builder();
        localizedTermQueryBuilder.add(newTermQuery(name + ".label", queryString), Occur.SHOULD);
        if(languages == null || languages.size() == 0){
            localizedTermQueryBuilder.add(newTermQuery(name + ".ALL", queryString), Occur.SHOULD);
        } else {
            for(Language lang : languages){
                localizedTermQueryBuilder.add(newTermQuery(name + "." + lang.getUuid().toString(), queryString), Occur.SHOULD);
            }
        }
        return localizedTermQueryBuilder.build();
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
        Builder idInQueryBuilder = new Builder();
        if(entities != null && entities.size() > 0 ){
            for(CdmBase entity : entities){
                idInQueryBuilder.add(newEntityIdQuery(idFieldName, entity), Occur.SHOULD);
            }
        }
        return idInQueryBuilder.build();
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

        Builder uuidInQueryBuilder = new Builder();
        if(entities != null && entities.size() > 0 ){
            for(IdentifiableEntity entity : entities){
                uuidInQueryBuilder.add(newEntityUuidQuery(uuidFieldName, entity), Occur.SHOULD);
            }
        }
        return uuidInQueryBuilder.build();
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

        Builder uuidInQueryBuilder = new Builder();
        if(uuids != null && uuids.size() > 0 ){
            for(UUID uuid : uuids){
                uuidInQueryBuilder.add(newTermQuery(uuidFieldName, uuids.toString(), false), Occur.SHOULD);
            }
        }
        return uuidInQueryBuilder.build();
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

        Builder longQueryBuilder = new Builder();
        if ( boundingBox.getLowerLeft().getLongitude() <= boundingBox.getUpperRight().getLongitude() ) {
            longQueryBuilder.add(NumericRangeQuery.newDoubleRange( longitudeFieldName, boundingBox.getLowerLeft().getLongitude(),
                    boundingBox.getUpperRight().getLongitude(), true, true ), Occur.MUST);
        }
        else {
            longQueryBuilder.add( NumericRangeQuery.newDoubleRange( longitudeFieldName, boundingBox.getLowerLeft().getLongitude(),
                    180.0, true, true ), BooleanClause.Occur.SHOULD );
            longQueryBuilder.add( NumericRangeQuery.newDoubleRange( longitudeFieldName, -180.0,
                    boundingBox.getUpperRight().getLongitude(), true, true ), BooleanClause.Occur.SHOULD );
        }

        Builder boxQueryBuilder = new Builder();
        boxQueryBuilder.add( latQuery, BooleanClause.Occur.MUST );
        boxQueryBuilder.add( longQueryBuilder.build(), BooleanClause.Occur.MUST );

        return new FilteredQuery(
                new MatchAllDocsQuery(),
                new QueryWrapperFilter( boxQueryBuilder.build() )
        );
    }

    /**
     * Warning! JoinQuery do currently not work with numeric fields, see https://issues.apache.org/jira/browse/LUCENE-4824
     * @param fromType
     * @param fromField
     * @param fromFieldIsMultivalued TODO
     * @param fromQuery
     * @param toField
     * @param toType
     *      Optional parameter. Only used for debugging only, can be left null otherwise.
     * @param scoreMode TODO
     * @return
     * @throws IOException
     */
    public Query newJoinQuery(Class<? extends CdmBase> fromType, String fromField, boolean fromFieldIsMultivalued,
            Query fromQuery, String toField, Class<? extends CdmBase> toType, ScoreMode scoreMode) throws IOException {
            boolean multipleValuesPerDocument = false;
            Query joinQuery = JoinUtil.createJoinQuery(
                    // need to use the sort field of the id field since
                    // ScoreMode.Max forces the fromField to be a docValue
                    // field of type [SORTED, SORTED_SET]
                    fromField + "__sort",
                    multipleValuesPerDocument, toField,
                    fromQuery, indexSearcherFor(fromType), scoreMode);
            if(logger.isDebugEnabled()) {
                logger.debug("joinQuery: " + joinQuery);
                if(toType != null) {
                    TopDocs result = indexSearcherFor(toType).search(joinQuery, 10);
                    ScoreDoc[] docs = result.scoreDocs;
                    logger.debug("joinQuery '" + fromType.getSimpleName() + ". " + fromField + "=" + toField + " where " + fromType.getSimpleName() + " matches "+ fromQuery + "' has " + result.totalHits + " results:");
                    for(ScoreDoc doc : docs) {
                        logger.debug("    toType doc: " + doc);
                            IndexReader indexReader = toolProvider.getIndexReaderFor(toType);
                            logger.debug("              : " + indexReader.document(doc.doc));
                        }
                    }
            }
            return joinQuery;
    }

    /**
     * Creates a class restriction query and wraps the class restriction
     * query and the given <code>query</code> into a BooleanQuery where both must match.
     * <p>
     *
     * @param cdmTypeRestriction
     * @param query
     * @return
     */
    public static BooleanQuery.Builder addTypeRestriction(Query query, Class<? extends CdmBase> cdmTypeRestriction) {

        BooleanQuery fullQuery;
        Builder filteredQueryBuilder = new Builder();
        Builder classFilterBuilder = new Builder();

        Term t = new Term(ProjectionConstants.OBJECT_CLASS, cdmTypeRestriction.getName());
        TermQuery termQuery = new TermQuery(t);

        classFilterBuilder.add(termQuery, Occur.SHOULD);
        BooleanQuery classFilter = classFilterBuilder.build();
        classFilter.setBoost(0);

        filteredQueryBuilder.add(query, Occur.MUST);
        filteredQueryBuilder.add(classFilter, Occur.MUST); // TODO using Occur.FILTER might be improve performance but causes wrong results

        return filteredQueryBuilder;
    }

    /**
     * @param clazz
     * @return
     */
    private IndexSearcher indexSearcherFor(Class<? extends CdmBase> clazz) {

        if(indexSearcherMap.get(clazz) == null){
            IndexReader indexReader = toolProvider.getIndexReaderFor(clazz);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            indexSearcherMap.put(clazz, searcher);
        }
        IndexSearcher indexSearcher = indexSearcherMap.get(clazz);
        return indexSearcher;
    }

}
