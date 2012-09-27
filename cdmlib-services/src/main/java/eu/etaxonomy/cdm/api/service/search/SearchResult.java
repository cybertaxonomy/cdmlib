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

import java.util.Map;

import org.apache.lucene.document.Document;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * TODO class description
 *
 * @author Andreas Kohlbecker
 * @date Jan 6, 2012
 *
 */
public class SearchResult<T extends CdmBase> {

    private float score = 0;

    private float maxScore = 0;

    private Document doc;

    private T entity;

    private Map<String,String[]> fieldHighlightMap;


    public double getScore() {
        return score;
    }


    public void setScore(float score) {
        this.score = score;
    }

    public Document getDoc() {
        return doc;
    }


    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public Map<String,String[]> getFieldHighlightMap() {
        return fieldHighlightMap;
    }

    public void setFieldHighlightMap(Map<String,String[]> fieldHighlightMap) {
        this.fieldHighlightMap = fieldHighlightMap;
    }

    /**
     * @param doc
     * @param entity
     */
    public SearchResult(Document doc) {
        this.doc = doc;
    }


    public float getMaxScore() {
        return maxScore;
    }


    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }







}
