package eu.etaxonomy.cdm.io.unitsPortal;

import java.util.List;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

public class UnitsGatheringArea {

	private NamedArea area = NamedArea.NewInstance();


//	protected NamedArea MyArea(){
//		//create gathering event
//		area = NamedArea.NewInstance();
//		return area;
//	}
//
//	private NamedArea getInstance(){
//		if (area == null)
//			area = MyArea();
//		return area;
//	}
	
	public UnitsGatheringArea(String isoCountry, String country, CdmApplicationController app){
		this.setCountry(isoCountry, country, app);
	}

	public NamedArea getArea(){
		return this.area;
	}
	
	public void setCountry(String iso, String fullName, CdmApplicationController app){
		WaterbodyOrCountry country=null;
		List<WaterbodyOrCountry>countries;
		if (iso != null)
			country = app.getOccurrenceService().getCountryByIso(iso);
		if (country != null)
			this.area.addWaterbodyOrCountry(country);

		else{
			countries = app.getOccurrenceService().getWaterbodyOrCountryByName(fullName);
			if (countries.size() >0)
				this.area.addWaterbodyOrCountry(countries.get(0));
		}
	}
	
	public void setAreaName(String areaName, String langIso, CdmApplicationController app){
		Language language = app.getTermService().getLanguageByIso(langIso); 
//		for (int i=0;i<areaNames.size();i++)
//			this.getInstance().setLabel(areaNames.get(i), language);
		this.area.setLabel(areaName, language);
	}

}
