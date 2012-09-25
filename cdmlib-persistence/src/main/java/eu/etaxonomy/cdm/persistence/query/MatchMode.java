// $Id$
/**

* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;


/**
 *
 *
 * @author a.babadshanjan
 * @created 03.03.2009
 * @version 1.0
 */
public enum MatchMode {

    /**
     * translates into <code>field = term</code>
     */
    EXACT("="),
    /**
     * translates into <code>field LIKE %term</code>
     */
    BEGINNING("LIKE"),
    /**
     * translates into <code>field LIKE %term%</code>
     */
    ANYWHERE("LIKE"),
    /**
     * translates into <code>field LIKE term%</code>
     */
    END("LIKE");

    private String matchOperator;

    MatchMode(String matchOperator){
        this.matchOperator = matchOperator;
    }

    public String queryStringFrom(String queryString){
        if(queryString == null){
            return "";
        }
        queryString = queryString.replace('*', '%');
        switch(this){
            case BEGINNING:
                queryString = appendWildcard(queryString);
                break;
            case END:
                queryString = prependWildcard(queryString);
                break;
            case ANYWHERE:
                queryString = appendWildcard(queryString);
                queryString = prependWildcard(queryString);
                break;
        }
        return queryString;
    }

    /**
     * @param queryString
     * @return
     */
    private String prependWildcard(String queryString) {
        if(!queryString.startsWith("%")){
            queryString += "%";
        }
        return queryString;
    }

    /**
     * @param queryString
     * @return
     */
    private String appendWildcard(String queryString) {
        if(!queryString.endsWith("%")){
            queryString += "%";
        }
        return queryString;
    }

    public String getMatchOperator(){
        return matchOperator;
    }
}
