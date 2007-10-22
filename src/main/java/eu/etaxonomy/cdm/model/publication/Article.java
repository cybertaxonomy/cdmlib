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
 * @created 15-Aug-2007 18:35:59
 */
@Entity
public class Article extends PublicationBase implements NomenclaturalReference {
	static Logger logger = Logger.getLogger(Article.class);

	private String series;
	private String volume;
	private Journal inJournal;

	public Journal getInJournal(){
		return inJournal;
	}

	public String getSeries(){
		return series;
	}

	public String getVolume(){
		return volume;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInJournal(Journal newVal){
		inJournal = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSeries(String newVal){
		series = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setVolume(String newVal){
		volume = newVal;
	}

}