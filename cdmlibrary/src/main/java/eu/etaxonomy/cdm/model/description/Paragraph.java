/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Paragraph extends VersionableEntity {
	static Logger logger = Logger.getLogger(Paragraph.class);
	private String content;
	private TextFormat format;

	public TextFormat getFormat(){
		return this.format;
	}

	/**
	 * 
	 * @param format    format
	 */
	public void setFormat(TextFormat format){
		this.format = format;
	}

	public String getContent(){
		return this.content;
	}

	/**
	 * 
	 * @param content    content
	 */
	public void setContent(String content){
		this.content = content;
	}

}