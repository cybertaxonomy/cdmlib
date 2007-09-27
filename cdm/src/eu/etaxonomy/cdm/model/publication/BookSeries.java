/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.publication;


import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:01
 */
@Entity
public class BookSeries extends PublicationBase {
	static Logger logger = Logger.getLogger(BookSeries.class);

	private String series;

	public String getSeries(){
		return series;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSeries(String newVal){
		series = newVal;
	}

}