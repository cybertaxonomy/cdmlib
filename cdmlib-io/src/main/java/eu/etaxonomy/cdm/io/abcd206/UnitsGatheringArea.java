package eu.etaxonomy.cdm.io.abcd206;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

public class UnitsGatheringArea {

	private NamedArea area = NamedArea.NewInstance();
	private ArrayList<NamedArea> areas = new ArrayList<NamedArea>();


	/*
	 * Constructor
	 * Set/create country
	 * @param isoCountry (try to used the isocode first)
	 * @param country
	 * @param app
	 */
	public UnitsGatheringArea(String isoCountry, String country, SpecimenImportConfigurator config){
		this.setCountry(isoCountry, country, config);
	}
	
	/*
	 * Constructor
	 * Set a list of NamedAreas
	 */
	public UnitsGatheringArea(ArrayList<String> namedAreas){
		this.setAreaNames(namedAreas);
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
	public void setAreaNames(ArrayList<String> namedAreas){
		for (int i=0; i< namedAreas.size(); i++){
			this.area.setLabel(namedAreas.get(i));
			this.areas.add(this.area);
			this.area = NamedArea.NewInstance();
		}
	}
	
	/*
	 * Set the current Country
	 * Search in the DB if the isoCode is known
	 * If not, search if the country name is in the DB
	 * If not, create a new Label with the Level Country
	 * @param iso: the country iso code
	 * @param fullName: the country's full name
	 * @param app: the CDM application controller
	 */
	public void setCountry(String iso, String fullName, SpecimenImportConfigurator config){
		WaterbodyOrCountry country=null;
		List<WaterbodyOrCountry>countries;
		if (iso != null)
			country = config.getCdmAppController().getOccurrenceService().getCountryByIso(iso);
		if (country != null)
			this.area.addWaterbodyOrCountry(country);

		else{
			countries = config.getCdmAppController().getOccurrenceService().getWaterbodyOrCountryByName(fullName);
			if (countries.size() >0)
				this.area.addWaterbodyOrCountry(countries.get(0));
			else{
				this.area.setLabel(fullName);
				this.area.setLevel(NamedAreaLevel.COUNTRY()); 
			}
		}
	}
	
}
