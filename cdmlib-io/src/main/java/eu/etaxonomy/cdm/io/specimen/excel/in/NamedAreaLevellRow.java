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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 08.04.2011
 */
public class NamedAreaLevellRow {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private UUID uuid = null;
	private String label = null;
	private String abbreviation = null;
	private String description = null;
	private String postfix = null;
	private String geoserverLabel = null;
	private String geoServerAttribute = null;
	private String orderIndex = null;

	public NamedAreaLevellRow() {
	}

// **************************** GETTER / SETTER *********************************/

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public UUID getUuid() {
		return uuid;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getPostfix() {
		return postfix;
	}
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	public String getGeoserverLabel() {
		return geoserverLabel;
	}
	public void setGeoserverLabel(String geoserverLabel) {
		this.geoserverLabel = geoserverLabel;
	}

	public String getGeoServerAttribute() {
		return geoServerAttribute;
	}
	public void setGeoServerAttribute(String geoServerAttribute) {
		this.geoServerAttribute = geoServerAttribute;
	}

	public String getOrderIndex() {
		return orderIndex;
	}
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