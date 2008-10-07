package eu.etaxonomy.cdm.io.unitsPortal;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

public class UnitsGatheringEvent {

	private GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
	CdmApplicationController app;

	public UnitsGatheringEvent(CdmApplicationController app, String locality, String languageIso, Double longitude, Double latitude, ArrayList<String> collectorNames){
		this.setLocality(locality, languageIso);
		this.setCoordinates(longitude, latitude);
		this.setCollector(collectorNames);
		this.app = app;
	}

	public GatheringEvent getGatheringEvent(){
		return this.gatheringEvent;
	}
//	protected GatheringEvent MyGatheringEvent(){
//		//create gathering event
//		gatheringEvent = GatheringEvent.NewInstance();
//		return gatheringEvent;
//	}
//
//	private GatheringEvent getInstance(){
//		if (gatheringEvent == null)
//			gatheringEvent = MyGatheringEvent();
//		return gatheringEvent;
//	}

	public void setLocality(String locality, String languageIso){
		LanguageStringBase loc;
		if (languageIso == null)
			loc = LanguageString.NewInstance(locality,Language.DEFAULT());
		else
			loc = LanguageString.NewInstance(locality,app.getTermService().getLanguageByIso(languageIso));
		this.gatheringEvent.setLocality(loc);
	}

	public void setCoordinates(Double longitude, Double latitude){
		//create coordinates point
		Point coordinates = Point.NewInstance();
		//add coordinates
		coordinates.setLongitude(longitude);
		coordinates.setLatitude(latitude);
		this.gatheringEvent.setExactLocation(coordinates);
	}

	public void setElevation(Integer elevation){
		this.gatheringEvent.setAbsoluteElevation(elevation);
	}

	/**/

	public void addArea(NamedArea area){
		this.gatheringEvent.addCollectingArea(area);
	}

	/*
	 * If the collector already exists, then use it
	 * if not, create a new collector
	 */
	public void setCollector(ArrayList<String> collectorNames,boolean getExisting){
		//create collector
		Agent collector;
		ListIterator<String> collectors = collectorNames.listIterator();
		//add the collectors
		String collName;
		while (collectors.hasNext()){
			collName = collectors.next();
			/*check if the collector does already exist*/
			try{
				List<Agent> col = app.getAgentService().findAgentsByTitle(collName);
				collector=col.get(0);
			}catch (Exception e) {
				collector = Person.NewInstance();
				collector.setTitleCache(collName);
			}
			this.gatheringEvent.setCollector(collector);
		}
	}

	/*
	 * Create a new collector or collector's team
	 */
	public void setCollector(ArrayList<String> collectorNames){
		Person collector;
		String collName;

		if (collectorNames.size()>1){
			Team team = new Team();
			for (int i=0;i<collectorNames.size();i++){
				collName = collectorNames.get(i);
				collector = Person.NewInstance();
				collector.setTitleCache(collName);
				team.addTeamMember(collector);
				this.gatheringEvent.setCollector(team);
			}
		}
		else if (collectorNames.size() == 1) {
			collName = collectorNames.get(1);
			collector = Person.NewInstance();
			collector.setTitleCache(collName);
			this.gatheringEvent.setCollector(collector);
		}

	}


}
