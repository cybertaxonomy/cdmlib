/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
public class UnitsGatheringEvent {

	private static final Logger logger = Logger.getLogger(UnitsGatheringEvent.class);
	private GatheringEvent gatheringEvent = GatheringEvent.NewInstance();

	/*
	 * Constructor
	 * Fill in the locality, coordinates and the collector(s) for the current GatheringEvent
	 * @param app: the CDM Application Controller
	 * @param locality
	 * @param languageIso
	 * @param longitude
	 * @param latitude
	 * @param collectorNames
	 */
	public UnitsGatheringEvent(ICdmApplicationConfiguration config, String locality, String languageIso, Double longitude, Double latitude, ArrayList<String> collectorNames){
		this.setLocality(config, locality, languageIso);
		this.setCoordinates(longitude, latitude);
		this.setCollector(collectorNames);
	}

	public GatheringEvent getGatheringEvent(){
		return this.gatheringEvent;
	}

	/*
	 * Set the locality for the current GatheringEvent
	 * @param locality
	 * @param langageIso
	 */
	public void setLocality(ICdmApplicationConfiguration config, String locality, String languageIso){
		LanguageString loc;
		if (languageIso == null || config.getTermService().getLanguageByIso(languageIso) == null){
			if (languageIso != null && config.getTermService().getLanguageByIso(languageIso) == null )
				logger.info("unknown iso used for the locality: "+languageIso);
			loc = LanguageString.NewInstance(locality,Language.DEFAULT());
		}
		else
			loc = LanguageString.NewInstance(locality,config.getTermService().getLanguageByIso(languageIso));
		this.gatheringEvent.setLocality(loc);
	}

	/*
	 * return the locality associated to the GatheringEvent
	 */
	public LanguageString getLocality(){
		return this.gatheringEvent.getLocality();
	}

	/*
	 * Set the coordinates for the current GatheringEvent
	 * @param: longitude
	 * @param: latitude
	 */
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

	/*
	 * Add a NamedArea to the GatheringEvent
	 * @param area: the NamedArea to add
	 */

	public void addArea(NamedArea area){
		this.gatheringEvent.addCollectingArea(area);
	}

	/*
	 * If the collector already exists, then use it
	 * if not, create a new collector
	 * NOT USED
	 */
	public void setCollector(ICdmApplicationConfiguration config, ArrayList<String> collectorNames,boolean getExisting){
		//create collector
		AgentBase collector;
		ListIterator<String> collectors = collectorNames.listIterator();
		//add the collectors
		String collName;
		while (collectors.hasNext()){
			collName = collectors.next();
			/*check if the collector does already exist*/
			try{
				List<AgentBase> col = config.getAgentService().findByTitle(null,collName,null,null,null,null, null,null).getRecords();
				collector=col.get(0);
			}catch (Exception e) {
				collector = Person.NewInstance();
				collector.setTitleCache(collName, true);
			}
			this.gatheringEvent.setCollector(collector);
		}
	}

	/*
	 * Create a new collector or collector's team
	 * @param: collectorNames: the list of names to add as collector/collectorTeam
	 * USED - create each time a new Collector
	 */
	public void setCollector(ArrayList<String> collectorNames){
		Person collector;
		String collName;

		if (collectorNames.size()>1){
			Team team = new Team();
			for (int i=0;i<collectorNames.size();i++){
				collName = collectorNames.get(i);
				collector = Person.NewInstance();
				collector.setTitleCache(collName, true);
				team.addTeamMember(collector);
				this.gatheringEvent.setCollector(team);
			}
		}
		else if (collectorNames.size() == 1) {
			collName = collectorNames.get(0);
			collector = Person.NewInstance();
			collector.setTitleCache(collName, true);
			this.gatheringEvent.setCollector(collector);
		}

	}


}
