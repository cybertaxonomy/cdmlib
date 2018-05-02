/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;

/**
 * @author a.mueller
 * @since 08.04.2011
 * @version 1.0
 */
public class NamedAreaLevellRow {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedAreaLevellRow.class);

	private UUID uuid = null; 
	private String label = null;
	private String abbreviation = null;
	private String description = null;
	private String postfix = null;
	private String geoserverLabel = null;
	private String geoServerAttribute = null;
	private String orderIndex = null;
	
	
	private TreeMap<Integer, IdentifiableSource> sources = new TreeMap<Integer, IdentifiableSource>();

	

	
	public NamedAreaLevellRow() {
	}

	
// **************************** GETTER / SETTER *********************************/	
	

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public UUID getUuid() {
		return uuid;
	}





	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}


	/**
	 * @return the abbreviation
	 */
	public String getAbbreviation() {
		return abbreviation;
	}


	/**
	 * @param abbreviation the abbreviation to set
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return the postfix
	 */
	public String getPostfix() {
		return postfix;
	}


	/**
	 * @param postfix the postfix to set
	 */
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}


	/**
	 * @return the geoserverLabel
	 */
	public String getGeoserverLabel() {
		return geoserverLabel;
	}


	/**
	 * @param geoserverLabel the geoserverLabel to set
	 */
	public void setGeoserverLabel(String geoserverLabel) {
		this.geoserverLabel = geoserverLabel;
	}


	/**
	 * @return the geoServerAttribute
	 */
	public String getGeoServerAttribute() {
		return geoServerAttribute;
	}


	/**
	 * @param geoServerAttribute the geoServerAttribute to set
	 */
	public void setGeoServerAttribute(String geoServerAttribute) {
		this.geoServerAttribute = geoServerAttribute;
	}


	/**
	 * @return the orderIndex
	 */
	public String getOrderIndex() {
		return orderIndex;
	}


	/**
	 * @param orderIndex the orderIndex to set
	 */
	public void setOrderIndex(String orderIndex) {
		this.orderIndex = orderIndex;
	}
	

	private<T extends Object> List<T> getOrdered(TreeMap<Integer, T> tree) {
		List<T> result = new ArrayList<T>();
		for (T value : tree.values()){
			result.add(value);
		}
		return result;
	}

	
}
