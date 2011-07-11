/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.media.ReferencedMedia;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Figure")
@Entity
@Audited
public class Figure extends ReferencedMedia {
	private static final long serialVersionUID = -1712467725277327725L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Figure.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static Figure NewInstance(){
		return new Figure();
	}
	
	/**
	 * Constructor
	 */
	protected Figure() {
		super();
	}
	
}