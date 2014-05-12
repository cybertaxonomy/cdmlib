/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;



/**
 *
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0
 * @created 11.12.2007 12:11:19
 *
 */
public class TaggedText {
	
	private String text;
	private TagEnum type;
	
	
	public static TaggedText NewWhitespaceInstance(){
		return new TaggedText(TagEnum.separator, " ");
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public TagEnum getType() {
		return type;
	}
	public void setType(TagEnum type) {
		this.type = type;
	}
	public TaggedText() {
		super();
	}

	public TaggedText(TagEnum type, String text) {
		super();
		this.text = text;
		this.type = type;
	}

// *************************** DELEGATES ************************************/

	@Transient
	public boolean isName() {
		return type.isName();
	}
	@Transient
	public boolean isRank() {
		return type.isRank();
	}
	@Transient
	public boolean isAuthors() {
		return type.isAuthors();
	}
	@Transient
	public boolean isAppendedPhrase() {
		return type.isAppendedPhrase();
	}
	@Transient
	public boolean isReference() {
		return type.isReference();
	}
	@Transient
	public boolean isYear() {
		return type.isYear();
	}
	@Transient
	public boolean isFullName() {
		return type.isFullName();
	}
	@Transient
	public boolean isNomStatus() {
		return type.isNomStatus();
	}
	@Transient
	public boolean isSeparator() {
		return type.isSeparator();
	}
	@Transient
	public boolean isHybridSign() {
		return type.isHybridSign();
	}
	
	
// ********************** toString() ***********************************************/	
	
	@Override
	public String toString(){
		String result = CdmUtils.concat(":", type.toString(), text);
		if (StringUtils.isBlank(result)){
			return super.toString();
		}else{
			return result;
		}
	}
	
}
