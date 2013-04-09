/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * @author p.kelbert
 * @created 20.10.2008
 */
public class UnitsGatheringArea {

	private NamedArea area = NamedArea.NewInstance();
	private final ArrayList<NamedArea> areas = new ArrayList<NamedArea>();


	/**
	 * Constructor.
	 * Set/create country.
	 * Saves the area to the application
	 * @param isoCountry (try to used the isocode first)
	 * @param country
	 * @param app
	 */
	public UnitsGatheringArea(String isoCountry, String country, ICdmApplicationConfiguration cdmApp){
		this.setCountry(isoCountry, country, cdmApp);
//		cdmApp.getTermService().saveOrUpdate(area);
	}

	/*
	 * Constructor
	 * Set a list of NamedAreas
	 */
	public UnitsGatheringArea(List<String> namedAreaList){
		this.setAreaNames(namedAreaList);
	}

	/*
	 * Return the current NamedArea
	 */
	public NamedArea getArea(){
		return this.area;
	}

	/*
	 * Return the current list of NamedAreas
	 */
	public ArrayList<NamedArea> getAreas(){
		return this.areas;
	}

	/*
	 * Set the list of NamedAreas
	 * @param namedAreas
	 */
	public void setAreaNames(List<String> namedAreas){
		for (String strNamedArea : namedAreas){
			this.area.setLabel(strNamedArea);
			this.areas.add(this.area);
			this.area = NamedArea.NewInstance();
		}
	}

	/**
	 * Set the current Country
	 * Search in the DB if the isoCode is known
	 * If not, search if the country name is in the DB
	 * If not, create a new Label with the Level Country
	 * @param iso: the country iso code
	 * @param fullName: the country's full name
	 * @param app: the CDM application controller
	 */
	public void setCountry(String iso, String fullName, ICdmApplicationConfiguration cdmApp){
		WaterbodyOrCountry country = null;
		List<WaterbodyOrCountry> countries = new ArrayList<WaterbodyOrCountry>();
		if (StringUtils.isNotBlank(iso)){
			//TODO move to termservice
			country = cdmApp.getOccurrenceService().getCountryByIso(iso);
		}
		if (country != null){
			area.addWaterbodyOrCountry(country);
			area.setLabel(country.getTitleCache());
		}
		else{
			if (!fullName.isEmpty()){
				//TODO move to termservice
				countries = cdmApp.getOccurrenceService().getWaterbodyOrCountryByName(fullName);
			}
			if (countries.size() >0){
				area.addWaterbodyOrCountry(countries.get(0));
				area.setLabel(countries.get(0).getTitleCache());
			}
			else{
				area.setLabel(fullName);
				area.setLevel(NamedAreaLevel.COUNTRY());
			}
		}
		
	}

}
