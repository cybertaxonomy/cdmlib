/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author muellera
 * @since 16.10.2024
 */
public class WikiDataItemId implements java.io.Serializable{

    private static final long serialVersionUID = -6255965775873612289L;


    public static final String WIKIDATA_ORG = "www.wikidata.org/wiki/";

    /**
     * The default public wikidata database
     */
    public static final String HTTP_WIKIDATA_ORG = "http://" + WIKIDATA_ORG;

    private volatile transient int hashCode = -1;   // Zero ==> undefined


//********************************* VARIABLES *************************************/

    /**
     * The base digits without prefix, checksum and hyphens("-")
     */
    private String identifierWithoutQ;

// ***************************** FACTORY METHODS ***************************************/

    public static WikiDataItemId fromString(String wikiDataItemId) throws IllegalArgumentException{
        return new WikiDataItemId(wikiDataItemId);
    }

// ******************************* CONSTRUCTOR ************************************/

    private WikiDataItemId() {} //empty constructor required for JAXB

    private WikiDataItemId(String wikiDataItemIdString) {
        parseWikiDataItemIdString(wikiDataItemIdString);
    }

//************************************ GETTER ***********************************/


    /**
     * The pure number representation, without Q prefix.
     */
    public String getDigitsOnly() {
        return identifierWithoutQ;
    }

    public String getIdentifierWithQ() {
        return "Q" + identifierWithoutQ;
    }

// ********************************************* PARSER *******************************/

    private static Pattern wikidataItemIdPattern = Pattern.compile("^Q?[1-9][0-9]*$");

    private void parseWikiDataItemIdString(String wikiDataItemId){
        if (StringUtils.isBlank(wikiDataItemId)){
            throw new IllegalArgumentException("Wikidata item ID string must not be null or blank");
        }
        wikiDataItemId = wikiDataItemId.trim();
        if (wikiDataItemId.startsWith("https:") ){
            //TODO
            wikiDataItemId = wikiDataItemId.replaceFirst("https:", "http:").trim();
        }

        //replace URI prefix
        if (wikiDataItemId.startsWith(HTTP_WIKIDATA_ORG)){
            wikiDataItemId = wikiDataItemId.replaceFirst(HTTP_WIKIDATA_ORG, "");
        }else if (wikiDataItemId.startsWith(WIKIDATA_ORG)){
            wikiDataItemId = wikiDataItemId.replaceFirst(WIKIDATA_ORG, "");
        }

        Matcher matcher = wikidataItemIdPattern.matcher(wikiDataItemId);
        if (!matcher.find()){
            throw new IllegalArgumentException("Wikidata item ID can not be parsed. "
                    + "It must have a Q followed by an arbitrary number of digits, "
                    + " where the first digit must not be 0.");
        }

        this.identifierWithoutQ = wikiDataItemId.substring(0,1).equals("Q") ?
                wikiDataItemId.substring(1):
                wikiDataItemId;
    }

    private String makeWikidataId(){
        return "Q" + identifierWithoutQ;
    }

    public String asURI(){
        return HTTP_WIKIDATA_ORG + makeWikidataId();
    }


//************************************************* toString/equals /hashCode *********************/

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = 31 * identifierWithoutQ.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WikiDataItemId){
            return this.identifierWithoutQ.equals(((WikiDataItemId)obj).identifierWithoutQ);
        }
        return false;
    }

    @Override
    public String toString(){
        return asURI();
    }
}