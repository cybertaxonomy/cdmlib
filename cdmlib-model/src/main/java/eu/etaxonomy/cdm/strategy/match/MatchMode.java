/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;


/**
 * Enumeration for matching modes.
 * @author a.mueller
 * @since 31.07.2009
 */
public enum MatchMode {
	EQUAL_REQUIRED,  //parameters must be equal and not null
	EQUAL,			 //parameters must be equal or both null
	EQUAL_OR_ONE_NULL,   //parameters must be equal or at least one parameter is null
	EQUAL_OR_SECOND_NULL,
	IGNORE,			//matches always
	MATCH_REQUIRED,
	MATCH_OR_ONE_NULL,
	MATCH_OR_SECOND_NULL,
	MATCH,//matches if parameter match (parameters must implement IMatchable)
	CACHE
	;
	private static final Logger logger = Logger.getLogger(MatchMode.class);

	public boolean matches(Object obj1, Object obj2, IMatchStrategy matchStrategy) throws MatchException{
		if (this == EQUAL_REQUIRED){
			return matchesEqualRequired(obj1, obj2);
		}else if(this == EQUAL){
			return matchesEqual(obj1, obj2);
		}else if (this == EQUAL_OR_ONE_NULL){
			return matchesEqualOrOneNull(obj1, obj2);
		}else if (this == EQUAL_OR_SECOND_NULL){
			return matchesEqualOrSecondNull(obj1, obj2);
		}else if(this == IGNORE){
			return matchesIgnore(obj1, obj2);
		}else if(this == MATCH){
			return matchesMatch(obj1, obj2, matchStrategy);
		}else if(this == MATCH_REQUIRED){
			return matchesMatchRequired(obj1, obj2, matchStrategy);
		}else if(this == MATCH_OR_ONE_NULL){
			return matchesMatchOrOneNull(obj1, obj2, matchStrategy);
		}else if(this == MATCH_OR_SECOND_NULL){
			return matchesMatchOrSecondNull(obj1, obj2, matchStrategy);
		}else if(this == CACHE){
			return matchesEqualRequired(obj1, obj2) && CdmUtils.isNotEmpty((String)obj1);
		}else {
			throw new MatchException("Match mode not handled yet: " + this);
		}
	}



	/**
	 * @param obj1
	 * @param obj2
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	private boolean matchesMatchRequired(Object obj1, Object obj2, IMatchStrategy matchStrategy) throws MatchException {
		if (obj1 == null || obj2 == null ){
			return false;
		}else if (! (obj1 instanceof IMatchable  && obj2 instanceof IMatchable) ){
			logger.warn("Match objects are not of type IMatchable");
			return matchesEqualRequired(obj1, obj2);
		}else{
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance((Class<? extends IMatchable>) obj1.getClass());
			}
			return matchStrategy.invoke((IMatchable)obj1, (IMatchable)obj2);
		}
	}


	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 * @throws MatchException
	 */
	private boolean matchesMatchOrOneNull(Object obj1, Object obj2, IMatchStrategy matchStrategy) throws MatchException {
		if (obj1 == null || obj2 == null ){
			return true;
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy);
		}
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 * @throws MatchException
	 */
	private boolean matchesMatchOrSecondNull(Object obj1, Object obj2, IMatchStrategy matchStrategy) throws MatchException {
		if (obj1 == null ){
			return true;
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy);
		}
	}


	/**
	 * @param obj1
	 * @param obj2
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	private boolean matchesMatch(Object obj1, Object obj2, IMatchStrategy matchStrategy) throws MatchException {
		if (obj1 == null && obj2 == null ){
			return true;
		}else {
			return matchesMatchRequired(obj1, obj2, matchStrategy);
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
	private boolean matchesEqualRequired(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || ! obj1.equals(obj2)){
			return false;
		}else{
			return true;
		}
	}


	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesEqualOrOneNull(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null){
			return true;
		}else{
			return matchesEqualRequired(obj1, obj2);
		}
	}


	/**
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean matchesEqualOrSecondNull(Object obj1, Object obj2) {
		if (obj2 == null){
			return true;
		}else{
			return matchesEqual(obj1, obj2);
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
		}else if (first == null && (isXOrOneNull())){
				return true;
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
	 * Returns true if a null value is required for retrieveing
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
			return ! isXOrOneNull();
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
				(this == IGNORE));
	}

	/**
	 * Returns true, if this match mode is of type MATCHXXX
	 * @return
	 */
	public boolean isMatch(){
		return ((this == MATCH_REQUIRED) || (this == MATCH_OR_ONE_NULL) ||
				(this == MATCH)|| (this == MATCH_OR_SECOND_NULL) ||
				(this == MATCH_OR_ONE_NULL));
	}

	/**
	 * Returns true, if this match mode is of type EQUALXXX
	 * @return
	 */
	public boolean isEqual(){
		return ((this == EQUAL_REQUIRED) || (this == EQUAL_OR_ONE_NULL) ||
				(this == EQUAL)|| (this == EQUAL_OR_SECOND_NULL) ||
				(this == EQUAL_OR_ONE_NULL));
	}

	/**
	 * Returns true, if this match mode is of type XXX_OR_ONE_NULL
	 * @return
	 */
	public boolean isXOrOneNull(){
		return (this == EQUAL_OR_ONE_NULL) || (this == MATCH_OR_ONE_NULL);

	}
}
