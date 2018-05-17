/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * A class for handling DOIs (http://www.doi.org).
 * It offers parsing and formatting functionality as well as validation.
 * A {@link DOI} object can only be created by syntactic valid input.
 * It internally stores a doi 2 strings, the first one being the registrant number
 * (including sub numbers), the second being the suffix.
 * 
 * 
 * @author a.mueller
 * @since 2013-09-04
 */
public final class DOI implements java.io.Serializable{
	
	/**
     * Explicit serialVersionUID for interoperability.
     */
	private static final long serialVersionUID = -3871039785359980553L;

	public static final int MAX_LENGTH = 1000;

	/**
	 * The default public DOI proxy server
	 */
	public static final String HTTP_DOI_ORG = "http://doi.org/";

	/**
	 * The former default public DOI proxy server, still supported but no longer preferred.
	 * @see #HTTP_DOI_ORG
	 */
	public static final String HTTP_OLD_DOI_ORG = "http://dx.doi.org/";
	
    private volatile transient int hashCode = -1;	// Zero ==> undefined

	//http://www.doi.org/doi_handbook/2_Numbering.html#2.2.1
//	prefix + suffix, no defined length, case-insensitive, any printable characters

	
//********************************* VARIABLES *************************************/	
	
	/**
	 * The directory indicator for DOIs as registered at 
	 */
	public static final String DIRECTORY_INDICATOR = "10";
	private String prefix_registrantCode;

	private String suffix;

// ***************************** FACTORY METHODS ***************************************/
	
	public static DOI fromString(String doi) throws IllegalArgumentException{
		return new DOI(doi);
	}
	
	public static DOI fromRegistrantCodeAndSuffix(String registrantCode, String suffix) throws IllegalArgumentException{
		return new DOI(registrantCode, suffix);
	}
	
	
// ******************************* CONSTRUCTOR ************************************/	
	private DOI(){}; //empty constructor required for JAXB
	
	
    /**
     * Creates a doi by its registrantCode and its suffix
     * @param registrantCode the registrant code, the is the part following the directoryIndicator "10." 
     * 	and preceding the first forward slash (followed by the suffix)
     * @param suffix the suffix is the part of the DOI following the first forward slash. It is provided 
     * by the registrant
     */
    private DOI(String registrantCode, String suffix) {
    	//preliminary until prefix_registrantCode and suffix validation is implemented
		this("10." + registrantCode + "/" + suffix);
		
		//use only after validation of both parts
//		this.prefix_registrantCode = registrantCode;
//		this.suffix = suffix;
	}

    private DOI(String doiString) {
		super();
		parseDoiString(doiString);
	}

//************************************ GETTER ***********************************/    
	
	public String getPrefix() {
		return makePrefix();
	}
    
	public String getPrefix_registrantCode() {
		return prefix_registrantCode;
	}

	public String getSuffix() {
		return suffix;
	}

	private static Pattern doiPattern = Pattern.compile("^doi:\\s*", Pattern.CASE_INSENSITIVE); 
	
// ********************************************* PARSER *******************************/
    
	private void parseDoiString(String doi){
		boolean isUrn = false;
		if (StringUtils.isBlank(doi)){
			throw new IllegalArgumentException("Doi string must not be null or blank");
		}
		doi = doi.trim();
		if (doi.startsWith("https") ){
			doi = doi.replaceFirst("https", "http").trim();
		}
		Matcher matcher = doiPattern.matcher(doi);
		if (matcher.find()){
			doi = matcher.replaceFirst("").trim();
		}

		
		//replace URI prefix
		if (doi.startsWith(HTTP_DOI_ORG)){
			doi = doi.replaceFirst(HTTP_DOI_ORG,"");
		}else if (doi.startsWith(HTTP_OLD_DOI_ORG)){
			doi = doi.replaceFirst(HTTP_OLD_DOI_ORG,"");
		}
		
		

		//handle URN prefix
		if (doi.startsWith("urn:doi:")){
			doi = doi.replaceFirst("urn:doi:","");
			isUrn = true;
		}
		
		
		//now we should have the pure doi
		if (doi.length() > MAX_LENGTH){
			//for persistence reason we currently restrict the length of DOIs to 1000
			throw new IllegalArgumentException("DOIs may have a maximum length of 1000 in the CDM.");
		}
		
		if (! doi.startsWith("10.")){
			throw new IllegalArgumentException("DOI not parsable. DOI must start with 10. or an URI or URN prefix ");
		}
		doi = doi.substring(3);
		String sep = isUrn? ":" : "/";
		
//		registrant
		String registrant = doi.split(sep)[0];
		if (!registrant.matches("[0-9]{2,}(?:[.][0-9]+)*")){   //per definition the number of digits may also be 1, however the lowest known number is 3 so we may be on the safe side here 
			String message = "Invalid prefix '10.%s'";
			throw new IllegalArgumentException(String.format(message, registrant));
		}
		//suffix
		String suffix = doi.replaceFirst(registrant + sep,"");
		if (! suffix.matches("\\p{Print}+")){
			String message = "Suffix should only include printable characters";
			throw new IllegalArgumentException(message);
		}
		if (isUrn){
			//TODO do some other replacements according to http://www.doi.org/doi_handbook/2_Numbering.html#2.6.3
			//e.g. slash becomes : in URN
			//TODO do we need this also for other URIs? According to http://www.doi.org/doi_handbook/2_Numbering.html#2.6 it is only required for URNs
			suffix = UrlUtf8Coder.unescape(suffix);
		}
		//success
		this.prefix_registrantCode = registrant;
		this.suffix = suffix;
			
	}
	
	
	private String makePrefix(){
		return DIRECTORY_INDICATOR + "." + this.prefix_registrantCode;
	}
	
	private String makeDoi(){
		return makePrefix() + "/" + this.suffix;
	}
	
	public String asURI(){
		return HTTP_DOI_ORG + makePrefix() + "/" + uriEncodedSuffix();
	}
	
	private String uriEncodedSuffix() {
		String result = UrlUtf8Coder.encode(this.suffix);
		return result;
	}

//************************************************* toString/equals /hashCode *********************/	

	
	
	@Override
	public int hashCode() {
		if (hashCode == -1) {
            hashCode = 31 * prefix_registrantCode.toUpperCase().hashCode() + suffix.toUpperCase().hashCode();
        }
        return hashCode;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DOI){
			DOI doi = (DOI)obj;
			if (this.prefix_registrantCode.toUpperCase().equals(doi.prefix_registrantCode.toUpperCase()) &&
					this.suffix.toUpperCase().equals(doi.suffix.toUpperCase())){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString(){
		return makeDoi();
	}
}
