// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class SpecimenCdmExcelImportState extends ExcelImportState<SpecimenCdmExcelImportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImportState.class);

	private SpecimenRow specimenRow;

	private Map<String, Reference<?>> references = new HashMap<String, Reference<?>>();
	private Map<String, Collection> collections = new HashMap<String, Collection>();
	private Map<String, Person> persons = new HashMap<String, Person>();
	private Map<String, Team> teams = new HashMap<String, Team>();
	
	public SpecimenCdmExcelImportState(SpecimenCdmExcelImportConfigurator config) {
		super(config);
	}

	public SpecimenRow getSpecimenRow() {
		return specimenRow;
	}

	public void setSpecimenRow(SpecimenRow specimenRow) {
		this.specimenRow = specimenRow;
	}

	public Reference<?> getReference(String key) {
		return references.get(key);
	}
	
	public Reference<?> putReference(String key, Reference<?> value){
		return this.references.put(key, value);
	}

	public Collection getCollection(String key) {
		return collections.get(key);
	}
	
	public Collection putCollection(String key, Collection value){
		return this.collections.put(key, value);
	}
	
	public Person getPerson(String key) {
		return persons.get(key);
	}
	
	public Person putPerson(String key, Person value){
		return this.persons.put(key, value);
	}

	public Team getTeam(String key) {
		return teams.get(key);
	}
	
	public Team putTeam(String key, Team value){
		return this.teams.put(key, value);
	}


}
