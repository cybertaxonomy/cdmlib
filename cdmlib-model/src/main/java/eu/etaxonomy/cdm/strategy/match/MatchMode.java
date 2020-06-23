/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.ICheckEmpty;


/**
 * Enumeration for matching modes.
 * @author a.mueller
 * @since 31.07.2009
 */
public enum MatchMode {
	EQUAL_REQUIRED,  //parameters must be equal and not null
	EQUAL,			 //parameters must be equal or both null
	EQUAL_OR_ONE_NULL,   //parameters must be equal or at least one parameter is null
	EQUAL_OR_FIRST_NULL, //parameters must be equal or first may be null
    EQUAL_OR_SECOND_NULL,
	IGNORE,			//matches always
	MATCH_REQUIRED,
	MATCH_OR_ONE_NULL,
	MATCH_OR_FIRST_NULL,
    MATCH_OR_SECOND_NULL,
	MATCH,//matches if parameter match (parameters must implement IMatchable)
	CACHE
	;
    //a possible further MatchMode could be NULL, saying that all instances of the given type should have a null
    //value, otherwise dirty data which should not be matched
    //e.g. NewParsedJournalInstance-authorship

	private static final Logger logger = Logger.getLogger(MatchMode.class);

	public MatchResult matches(Object obj1, Object obj2, IMatchStrategy matchStrategy, String fieldName, boolean failAll) throws MatchException{
		if (this == EQUAL_REQUIRED){
			return matchesEqualRequired(obj1, obj2, fieldName);
		}else if(this == EQUAL){
			return matchesEqual(obj1, obj2, fieldName);
		}else if (this == EQUAL_OR_ONE_NULL){
			return matchesEqualOrOneNull(obj1, obj2, fieldName);
		}else if (this == EQUAL_OR_SECOND_NULL){
			return matchesEqualOrSecondNull(obj1, obj2, fieldName);
	     }else if (this == EQUAL_OR_FIRST_NULL){
	            return matchesEqualOrSecondNull(obj2, obj1, fieldName);
		}else if(this == IGNORE){
			return matchesIgnore(obj1, obj2);
		}else if(this == MATCH){
			return matchesMatch(obj1, obj2, matchStrategy, fieldName, failAll);
		}else if(this == MATCH_REQUIRED){
			return matchesMatchRequired(obj1, obj2, matchStrategy, fieldName, failAll);
		}else if(this == MATCH_OR_ONE_NULL){
			return matchesMatchOrOneNull(obj1, obj2, matchStrategy, fieldName, failAll);
		}else if(this == MATCH_OR_SECOND_NULL){
			return matchesMatchOrSecondNull(obj1, obj2, matchStrategy, fieldName, failAll);
        }else if(this == MATCH_OR_FIRST_NULL){
            return matchesMatchOrSecondNull(obj2, obj1, matchStrategy, fieldName, failAll);
		}else if(this == CACHE){
			return matchCache(obj1, obj2, fieldName);
		}else {
			throw new MatchException("Match mode not handled yet: " + this);
		}
	}

    private MatchResult matchCache(Object obj1, Object obj2, String fieldName) {
        if (StringUtils.isBlank((String)obj1)){
            return MatchResult.NewInstance(fieldName, this,obj1, obj2);
        }else{
            return matchesEqualRequired(obj1, obj2, fieldName);
        }
    }

	private MatchResult matchesMatchRequired(Object obj1, Object obj2, IMatchStrategy matchStrategy, String fieldName, boolean failAll) throws MatchException {
		if (obj1 == null || obj2 == null ){
			return MatchResult.NewInstance(fieldName, this, obj1, obj2);
		}else if (! (obj1 instanceof IMatchable  && obj2 instanceof IMatchable) ){
			logger.warn("Match objects are not of type IMatchable");
			return matchesEqualRequired(obj1, obj2, fieldName);
		}else{
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance((Class<? extends IMatchable>) obj1.getClass());
			}
			return matchStrategy.invoke((IMatchable)obj1, (IMatchable)obj2, failAll);
		}
	}

	private MatchResult matchesMatchOrOneNull(Object obj1, Object obj2,
	        IMatchStrategy matchStrategy, String fieldName, boolean failAll) throws MatchException {
		if (obj1 == null || obj2 == null ){
			return MatchResult.SUCCESS();
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy, fieldName, failAll);
		}
	}

	private MatchResult matchesMatchOrSecondNull(Object obj1, Object obj2,
	        IMatchStrategy matchStrategy, String fieldName, boolean failAll) throws MatchException {
		if (obj2 == null ){
		    return MatchResult.SUCCESS();
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy, fieldName, failAll);
		}
	}

	private MatchResult matchesMatch(Object obj1, Object obj2,
	        IMatchStrategy matchStrategy, String fieldName, boolean failAll) throws MatchException {
		if (obj1 == null && obj2 == null ){
			return MatchResult.SUCCESS();
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy, fieldName, failAll);
		}
	}

	private MatchResult matchesIgnore(Object obj1, Object obj2) {
		return MatchResult.SUCCESS();
	}

	private MatchResult matchesEqualRequired(Object obj1, Object obj2, String fieldName) {
		if (obj1 == null || obj2 == null){
		    return MatchResult.NewInstance(fieldName, this, obj1, obj2);
		}else if (! obj1.equals(obj2)) {
		    return MatchResult.NewInstance(fieldName, this, obj1, obj2);
		}else{
			return MatchResult.SUCCESS();
		}
	}

	private MatchResult matchesEqualOrOneNull(Object obj1, Object obj2, String fieldName) {
		if (obj1 == null || obj2 == null){
			return MatchResult.SUCCESS();
		}else{
			return matchesEqualRequired(obj1, obj2, fieldName);
		}
	}

	private MatchResult matchesEqualOrSecondNull(Object obj1, Object obj2, String fieldName) {
		if (obj2 == null){
			return MatchResult.SUCCESS();
		}else{
			return matchesEqual(obj1, obj2, fieldName);
		}
	}

	private MatchResult matchesEqual(Object obj1, Object obj2, String fieldName) {
		if (obj1 == null && obj2 == null){
			return MatchResult.SUCCESS();
		}else {
			return matchesEqualRequired(obj1, obj2, fieldName);
		}
	}

	//not needed?
	public boolean allowsSecondNull(Object first){
		if (isRequired()){
			return false;
		}else if(first == null){
			return true;
		}else {
			return allowsExactlyOneNull();
		}
	}

	/**
	 * Returns true is this match mode can be ignored for retrieving
	 * matching objects
	 * @param first
	 * @return
	 */
	public boolean isIgnore(Object first){
		if (this == IGNORE){
			return true;
		}else if (isNullOrEmpty(first) && (isXOrOneNull() || isXOrFirstNull()) ){
				return true;
		}else{
			return false;
		}
	}

    private boolean isNullOrEmpty(Object value) {
        if (value == null){
            return true;
        }else if(value instanceof ICheckEmpty){
            return ((ICheckEmpty)value).checkEmpty();
        }else{
            return false;
        }
    }

	/**
	 * Returns true if a non-null value is required for finding
	 * matching objects
	 * @param first
	 * @return
	 */
	public boolean requiresSecondValue(Object first){
		if (first == null){
			return false;
		}else{
			return ! allowsExactlyOneNull() ;
		}
	}

	/**
	 * Returns true if a null value is required for retrieving
	 * matching objects
	 * @param first
	 * @return
	 * @throws MatchException if first is null and matching a non-null value is required
	 */
	public boolean requiresSecondNull(Object first) throws MatchException{
		if (first != null){
			return false;
		}else if (isRequired()){
			throw new MatchException("MatchMode " + this + " does not allow (null)");
		}else{
			return ! isXOrOneNull() && ! isXOrFirstNull();
		}
	}

	/**
	 * Returns true if a non-null value is required, independent from the first value
	 * @return
	 */
	public boolean isRequired(){
		return ((this == EQUAL_REQUIRED) || (this == MATCH_REQUIRED));
	}

	/**
	 * Returns true, if this match mode allows that one value is null and the other is not null.
	 * @return
	 */
	private boolean allowsExactlyOneNull(){
		return (isXOrOneNull() ||
				(this == EQUAL_OR_SECOND_NULL)|| (this == MATCH_OR_SECOND_NULL) ||
				(this == EQUAL_OR_FIRST_NULL) || (this == MATCH_OR_FIRST_NULL) ||
				(this == IGNORE));
	}

	/**
	 * Returns true, if this match mode is of type MATCHXXX
	 */
	public boolean isMatch(){
		return ((this == MATCH_REQUIRED) || (this == MATCH_OR_ONE_NULL) ||
				(this == MATCH)|| (this == MATCH_OR_SECOND_NULL) ||
				(this == MATCH_OR_FIRST_NULL));
	}

	/**
	 * Returns true, if this match mode is of type EQUALXXX
	 * @return
	 */
	public boolean isEqual(){
		return ((this == EQUAL_REQUIRED) || (this == EQUAL_OR_ONE_NULL) ||
				(this == EQUAL)|| (this == EQUAL_OR_SECOND_NULL) ||
				(this == EQUAL_OR_FIRST_NULL));
	}

	/**
	 * Returns <code>true</code>, if this match mode is of type XXX_OR_ONE_NULL
	 */
	public boolean isXOrOneNull(){
		return (this == EQUAL_OR_ONE_NULL) || (this == MATCH_OR_ONE_NULL);
	}

	/**
     * Returns <code>true</code>, if this match mode is of type XXX_OR_FIRST_NULL
     */
	public boolean  isXOrFirstNull(){
	    return (this == EQUAL_OR_FIRST_NULL) || (this == MATCH_OR_FIRST_NULL);
	}
}
