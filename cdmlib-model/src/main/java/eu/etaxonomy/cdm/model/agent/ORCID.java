/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.common.UTF8;


/**
 * A class for handling ORCIDs (http://https://orcid.org/, https://support.orcid.org/hc/en-us/articles/360006897674).
 * It offers parsing and formatting functionality as well as validation.
 * A {@link ORCID} object can only be created by syntactic valid input.
 * It internally stores 1 strings (length=16).
 *
 *
 * @author a.mueller
 * @since 2019-11-08
 */
public final class ORCID implements java.io.Serializable{

    /**
     * Explicit serialVersionUID for interoperability.
     */
    private static final long serialVersionUID = 4304992020966546747L;

    public static final String ORCID_ORG = "orcid.org/";

	/**
	 * The default public ORCID proxy server
	 */
	public static final String HTTP_ORCID_ORG = "https://" + ORCID_ORG;

    private volatile transient int hashCode = -1;	// Zero ==> undefined


//********************************* VARIABLES *************************************/

	/**
	 * The base digits without prefix, checksum and hyphens("-")
	 */
	private String baseNumber;

	/**
	 * The checksum.
	 * @see #checkDigit()
	 */
	private String checkSum;

// ***************************** FACTORY METHODS ***************************************/

	public static ORCID fromString(String orcid) throws IllegalArgumentException{
		return new ORCID(orcid);
	}

// ******************************* CONSTRUCTOR ************************************/

	private ORCID(){} //empty constructor required for JAXB

//    /**
//     * Creates a doi by its registrantCode and its suffix
//     * @param registrantCode the registrant code, the is the part following the directoryIndicator "10."
//     * 	and preceding the first forward slash (followed by the suffix)
//     * @param suffix the suffix is the part of the DOI following the first forward slash. It is provided
//     * by the registrant
//     */
//    private ORCID(String registrantCode, String suffix) {
//    	//preliminary until prefix_registrantCode and suffix validation is implemented
//		this("10." + registrantCode + "/" + suffix);
//
//		//use only after validation of both parts
////		this.prefix_registrantCode = registrantCode;
////		this.suffix = suffix;
//	}

    private ORCID(String doiString) {
		parseOrcidString(doiString);
	}

//************************************ GETTER ***********************************/


	/**
	 * The pure number representation, including the checksum (this maybe 'X'
	 * so it's not only digits
	 * @return
	 */
	public String getDigitsOnly() {
		return baseNumber+checkSum;
	}


// ********************************************* PARSER *******************************/

	private static Pattern orcidPattern = Pattern.compile("^(\\d{4}("+UTF8.ANY_DASH_RE()+")?){3}\\d{3}[0-9Xx]?$");

	private void parseOrcidString(String orcid){
		if (StringUtils.isBlank(orcid)){
			throw new IllegalArgumentException("ORCID string must not be null or blank");
		}
		orcid = orcid.trim();
		if (orcid.startsWith("http:") ){
		    orcid = orcid.replaceFirst("http:", "https:").trim();  //https is the current display standard
		}

		//replace URI prefix
		if (orcid.startsWith(HTTP_ORCID_ORG)){
			orcid = orcid.replaceFirst(HTTP_ORCID_ORG, "");
		}else if (orcid.startsWith(ORCID_ORG)){
		    orcid = orcid.replaceFirst(ORCID_ORG, "");
        }

		//now we should have the pure orcid
		if (orcid.length() != 15 && orcid.length() != 16 && orcid.length() != 18 && orcid.length() != 19){
			//for persistence reason we currently restrict the length of DOIs to 1000
			throw new IllegalArgumentException("ORCIDs must have exactly 16 digits. 3 dashes ('-') may be included after each group of 4 digits.");
		}

		Matcher matcher = orcidPattern.matcher(orcid);
		if (!matcher.find()){
            throw new IllegalArgumentException("ORCID can not be parsed. It must have exactly 16 digits. 3 dashes ('-') may be included after each group of 4 digits.");
		}

		orcid = orcid.replaceAll(UTF8.ANY_DASH_RE(), "");

		if (orcid.length() == 16){
	        this.baseNumber = orcid.substring(0, 15);
		    this.checkSum = orcid.substring(15);
		    if (!checkDigit(baseNumber).equals(checkSum)){
		        throw new IllegalArgumentException("ORCID checksum not correct (last digit is checksum, see https://support.orcid.org/hc/en-us/articles/360006897674-Structure-of-the-ORCID-Identifier).");
		    }
		}else{
		    this.baseNumber = orcid;
		    this.checkSum = checkDigit(baseNumber);
		}
	}

	private String makeOrcid(){
		return baseNumber.substring(0,4) + "-" + baseNumber.substring(4,8) + "-"
		        + baseNumber.substring(8,12) + "-" + baseNumber.substring(12,15) + checkSum;
	}

	public String asURI(){
		return HTTP_ORCID_ORG + makeOrcid();
	}

	/**
     * Generates check digit as per ISO 7064 11,2.
     * (code from https://support.orcid.org/hc/en-us/articles/360006897674)
     */
	public String checkDigit(){
	    return checkDigit(baseNumber);
	}

	/**
	  * @see #checkDigit()
	  * @param baseDigits the base digits without the checkSum digit
	  */
	private static String checkDigit(String baseDigits) {
	    int total = 0;
	    for (int i = 0; i < baseDigits.length(); i++) {
	        int digit = Character.getNumericValue(baseDigits.charAt(i));
	        total = (total + digit) * 2;
	    }
	    int remainder = total % 11;
	    int result = (12 - remainder) % 11;
	    return result == 10 ? "X" : String.valueOf(result);
	}

//************************************************* toString/equals /hashCode *********************/

	@Override
	public int hashCode() {
		if (hashCode == -1) {
            hashCode = 31 * baseNumber.hashCode();
        }
        return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ORCID){
			return this.baseNumber.equals(((ORCID)obj).baseNumber) &&
			        this.checkSum.equals(((ORCID)obj).checkSum);
		}
		return false;
	}

	@Override
	public String toString(){
		return asURI();
	}
}
