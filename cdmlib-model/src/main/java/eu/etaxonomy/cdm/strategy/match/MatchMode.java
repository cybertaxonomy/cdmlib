// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;


/**
 * Enumeration for matching modes.
 * @author a.mueller
 * @created 31.07.2009
 * @version 1.0
 */
public enum MatchMode {
	EQUAL_REQUIRED,  //parameters must be equal and not null
	EQUAL,			 //parameters must be equal or both null
	EQUAL_OR_ONE_NULL,   //parameters must be equal at least one parameter is null
	IGNORE
	;

	
	public boolean matches(Object obj1, Object obj2) throws MatchException{
		if (this == EQUAL_REQUIRED){
			return matchesEqualRequired(obj1, obj2);
		}else if(this == EQUAL){
			return matchesEqual(obj1, obj2);
		}else if (this == EQUAL_OR_ONE_NULL){
			return matchesEqualOrOneNull(obj1, obj2);
		}else if(this == IGNORE){
			return matchesIgnore(obj1, obj2);
		} else {
			throw new MatchException("Match mode not handled yet: " + this);
		}
	}
	
	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesIgnore(Object obj1, Object obj2) {
		return true;
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesEqualOrOneNull(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || obj1.equals(obj2)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesEqual(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null){
			return true;
		}else {
			return matchesEqualRequired(obj1, obj2);
		}
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesEqualRequired(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || ! obj1.equals(obj2)){
			return false;
		}else{
			return true;
		}
	}

	public boolean allowsBothNull(){
		if (this == EQUAL || this == EQUAL_OR_ONE_NULL || this == IGNORE){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean allowsFirstNull(){
		if (allowsBothNull() ){
			return true;
		}else{
			return false;
		}
	}
	
	
}
