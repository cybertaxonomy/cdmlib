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
 * @author a.babadshanjan
 * @since 03.03.2009
 * @version 1.0
 */
public enum MatchMode {

    /**
     * translates into <code>field = term</code>
     */
    EXACT("="),
    /**
     * translates into <code>field LIKE term</code>
     */
    LIKE("LIKE"),
    /**
     * translates into <code>field LIKE term%</code>
     */
    BEGINNING("LIKE"),
    /**
     * translates into <code>field LIKE %term%</code>
     */
    ANYWHERE("LIKE"),
    /**
     * translates into <code>field LIKE %term</code>
     */
    END("LIKE");

    private static final char STAR_WILDCARD = '*';
    private static final char SQL_WILDCARD = '%';
    private String matchOperator;
    private final String wildcardStr = "" + SQL_WILDCARD;

    MatchMode(String matchOperator){
        this.matchOperator = matchOperator;
    }

    public String queryStringFrom(String queryString){
        if(queryString == null){
            return "";
        }
        queryString = queryString.replace(STAR_WILDCARD, SQL_WILDCARD);
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
            case EXACT:
            	break;  //Nothing to do
            case LIKE:
                break;  //Nothing to do
            default:
            	throw new RuntimeException ("Unsupported Matchmode: " + this.toString());
        }
        return queryString;
    }

    /**
     * @param queryString
     * @return
     */
    private String prependWildcard(String queryString) {
        if(!queryString.startsWith(wildcardStr)){
            queryString = wildcardStr + queryString;
        }
        return queryString;
    }

    /**
     * @param queryString
     * @return
     */
    private String appendWildcard(String queryString) {
        if(!queryString.endsWith(wildcardStr)){
            queryString += wildcardStr;
        }
        return queryString;
    }

    public String getMatchOperator(){
        return matchOperator;
    }
}
