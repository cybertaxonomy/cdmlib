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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.search.spatial.impl.Point;
import org.hibernate.search.spatial.impl.Rectangle;
import org.hibernate.search.spatial.impl.SpatialQueryBuilderFromPoint;

import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.kohlbecker
 * @date Sep 14, 2012
 *
 */
public class QueryFactory {

    public static final Logger logger = Logger.getLogger(QueryFactory.class);

    private final LuceneSearch luceneSearch;

    Set<String> textFieldNames = new HashSet<String>();

    private BooleanQuery finalQuery;

    public Set<String> getTextFieldNames() {
        return textFieldNames;
    }

    public String[] getTextFieldNamesAsArray() {
        return textFieldNames.toArray(new String[textFieldNames.size()]);
    }


    public QueryFactory(LuceneSearch luceneSearch){
        this.luceneSearch = luceneSearch;
    }

    /**
     * Creates a new Term query. Depending on whether <code>isTextField</code> is set true or not the
     * supplied <code>queryString</code> will be parsed by using the according analyzer or not.
     * Setting <code>isTextField</code> to <code>false</code> is useful for searching for uuids etc.
     *
     * @param fieldName
     * @param queryString
     * @param isTextField whether this field is a field containing free text in contrast to e.g. ID fields.
     *     If <code>isTextField</code> to <code>true</code> the <code>queryString</code> will be parsed by
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
                return luceneSearch.parse(luceneQueryString);
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
     * @return
     */
    public Query newIsNotNullQuery(String idFieldName){
        return new TermQuery(new Term(NotNullAwareIdBridge.notNullField(idFieldName), NotNullAwareIdBridge.NOT_NULL_VALUE));
  }

    /**
     * creates a query for searching for documents in which the field specified by <code>uuidFieldName</code> matches at least one of the uuid
     * of the <code>entities</code>, the sql equivalent of this is <code>WHERE uuidFieldName IN (uuid_1, uuid_2, ...) </code>.
     * @param uuidFieldName
     * @param entities
     * @return
     */
    public Query newEntityUuidQuery(String uuidFieldName, List<? extends IdentifiableEntity> entities){

        BooleanQuery uuidInQuery = new BooleanQuery();
        if(entities != null && entities.size() > 0 ){
            for(IdentifiableEntity entity : entities){
                uuidInQuery.add(newTermQuery(uuidFieldName, entity.getUuid().toString(), false), Occur.SHOULD);
            }
        }
        return uuidInQuery;
    }

    public void setFinalQuery(BooleanQuery finalQuery) {
        this.finalQuery = finalQuery;
    }

    public BooleanQuery getFinalQuery(){
        return finalQuery;
    }

    public LuceneSearch getLuceneSearch() {
        return luceneSearch;
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

}
