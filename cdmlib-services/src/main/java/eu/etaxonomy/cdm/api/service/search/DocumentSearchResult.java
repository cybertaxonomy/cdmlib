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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.engine.ProjectionConstants;

/**
 * TODO class description
 *
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
public class DocumentSearchResult {

    private float score = 0;

    private float maxScore = 0;


    public DocumentSearchResult() {
    }

    /**
     * key will be a combination of DocumentBuilder.CLASS_FIELDNAME and id field: ID_FIELD
     */
    private final Map<String, Document> docs = new HashMap<String, Document>();
    private Map<String,String[]> fieldHighlightMap;


    public double getScore() {
        return score;
    }


    public void setScore(float score) {
        this.score = score;
    }


    public Map<String,String[]> getFieldHighlightMap() {
        return fieldHighlightMap;
    }

    public void setFieldHighlightMap(Map<String,String[]> fieldHighlightMap) {
        this.fieldHighlightMap = fieldHighlightMap;
    }



    public float getMaxScore() {
        return maxScore;
    }


    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }


    public Collection<Document> getDocs() {
        return docs.values();
    }


    public void addDoc(Document doc) {
        String key = doc.getValues(ProjectionConstants.OBJECT_CLASS)[0] + "." + doc.getValues(LuceneSearch.ID_FIELD)[0];
        this.docs.put(key, doc);
    }



}
