/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @since 07.08.2009
 * @version 1.0
 */
public abstract class FieldMatcherBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FieldMatcherBase.class);
	
	private String propertyName ; 
	private Field field;
	private MatchMode matchMode;
	
	protected FieldMatcherBase (String propertyname, Field field, MatchMode matchMode){
		this.propertyName = propertyname;
		this.field = field;
		this.matchMode = matchMode;
	}

	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(Field field) {
		this.field = field;
	}

	
	
	/**
	 * @return the matchMode
	 */
	public MatchMode getMatchMode() {
		return matchMode;
	}

	/**
	 * @param matchMode the matchMode to set
	 */
	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
	
	@Override
	public String toString(){
		if (propertyName == null || matchMode == null){
			return super.toString();
		}
		return "[" + propertyName + "->" + matchMode.toString() +"]";
	}
	
	
}
