/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.net.URI;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.ReferencedMediaBase;

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
public class Figure extends ReferencedMediaBase {
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
     * Factory method which creates a new figure, adds a reprsentation including mime type and suffix information
     * and adds to the later a representation part for a given uri and size
     * Returns <code>null</code> if uri is empty
     * @return Media
     */
   public static Media NewInstance(URI uri, Integer size, String mimeType, String suffix){
    	MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix, uri, size, ImageFile.class);
        if (representation == null){
            return null;
        }
        Figure figure = Figure.NewInstance();
        figure.addRepresentation(representation);
        return figure;
    }
	
	/**
	 * Constructor
	 */
	protected Figure() {
		super();
	}
	
}