/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:06
 */
public class Paragraph extends VersionableEntity {
	static Logger logger = Logger.getLogger(Paragraph.class);

	@Description("")
	private String content;
	private TextFormat format;

	public TextFormat getFormat(){
		return format;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFormat(TextFormat newVal){
		format = newVal;
	}

	public String getContent(){
		return content;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setContent(String newVal){
		content = newVal;
	}

}