/**
 * 
 */
package eu.etaxonomy.cdm.common;

/**
 * This class is a constant holder for commonly used UTF-8 characters.
 *  
 * @author a.mueller
 * @since 19.06.2013
 */
public enum UTF8 {
	
	
	EN_DASH("\u2013"),   // https://de.wikipedia.org/wiki/Halbgeviertstrich
	SPATIUM("\u202F"),   //very short non-breaking space
	EN_DASH_SPATIUM("\u202F\u2013\u202F"),
	HYBRID ("\u00D7"),   // hybrid sign
	SHARP_S("\u00DF"),
	;

	private String value;
	
	private UTF8(String value) {
		this.value = value;
	}
	
	public String toString(){
		return value;
	}
	
}
