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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class SpecimenCdmExcelImportState extends ExcelImportState<SpecimenCdmExcelImportConfigurator, SpecimenRow>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImportState.class);

	private SpecimenRow specimenRow;
	private NamedAreaLevellRow namedAreaLevelRow;

	private Map<String, Reference> references = new HashMap<String, Reference>();
	private Map<String, Collection> collections = new HashMap<String, Collection>();
	private Map<String, Person> persons = new HashMap<String, Person>();
	private Map<String, Team> teams = new HashMap<String, Team>();
	private Map<String, TaxonNameBase<?, ?>> names = new HashMap<String, TaxonNameBase<?,?>>();
	private Map<String, UUID> areas = new HashMap<String, UUID>();


	private Map<String, NamedAreaLevel> postfixLevels = new HashMap<String, NamedAreaLevel>();
	private Map<String, ExtensionType> postfixExtensionTypes = new HashMap<String, ExtensionType>();



	public SpecimenCdmExcelImportState(SpecimenCdmExcelImportConfigurator config) {
		super(config);
	}

	public Reference getReference(String key) {
		return references.get(key);
	}

	public Reference putReference(String key, Reference value){
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

	public TaxonNameBase getName(String key) {
		return names.get(key);
	}

	public TaxonNameBase putName(String key, TaxonNameBase value){
		return this.names.put(key, value);
	}

	public void setNamedAreaLevelRow(NamedAreaLevellRow namedAreaLabelRow) {
		this.namedAreaLevelRow = namedAreaLabelRow;
	}

	public NamedAreaLevellRow getNamedAreaLevelRow() {
		return namedAreaLevelRow;
	}

	public NamedAreaLevel getPostfixLevel(String postfix){
		return this.postfixLevels.get(postfix);
	}

	public NamedAreaLevel putPostfixLevel(String postfix, NamedAreaLevel level) {
		return this.postfixLevels.put(postfix, level);

	}

	public ExtensionType getPostfixExtensionType(String postfix){
		return this.postfixExtensionTypes.get(postfix);
	}

	public ExtensionType putPostfixExtensionType(String postfix, ExtensionType type) {
		return this.postfixExtensionTypes.put(postfix, type);

	}

	public UUID putArea(String key, UUID areaUuid) {
		return this.areas.put(key, areaUuid);
	}

	public UUID getArea(String key) {
		return areas.get(key);
	}



}
