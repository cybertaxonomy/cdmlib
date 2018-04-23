/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 \* @since 03.06.2010
 *
 */
public class DerivedUnitFacadeFieldUnitCacheStrategy extends StrategyBase implements IIdentifiableEntityCacheStrategy<FieldUnit> {
	private static final long serialVersionUID = 1578628591216605619L;
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeFieldUnitCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("df4672c1-ce5c-4724-af6d-91e2b326d4a4");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	private boolean includeEmptySeconds = false;
	private boolean includeReferenceSystem = true;

	@Override
	public String getTitleCache(FieldUnit fieldUnit) {
		DerivedUnitFacade facade;
		String result = "";
		DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
		config.setFirePropertyChangeEvents(false);
		facade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.FieldUnit, fieldUnit, config);
		result = getFieldData(facade);
		result = addPlantDescription(result, facade);
		facade.close();
		return result;
	}


	protected String getFieldData(DerivedUnitFacade facade) {
		String ALTITUDE_PREFIX = "alt. ";
//		String ALTITUDE_POSTFIX = " m";

		String result = "";


		//country
		String strCountry = null;
		NamedArea country = facade.getCountry();
		Representation repCountry = country == null ? null : country.getRepresentation(Language.DEFAULT());
		strCountry = repCountry == null ? null: repCountry.getLabel();
		result = CdmUtils.concat(", ", result, strCountry);

		//locality
		result = CdmUtils.concat(", ", result, facade.getLocalityText());

		//elevation
		if (StringUtils.isNotBlank(facade.absoluteElevationToString())){
			result = CdmUtils.concat(", " , result, ALTITUDE_PREFIX);
			result += facade.absoluteElevationToString();
		}

		//exact locality
		if (facade.getExactLocation() != null){
			String exactLocation = facade.getExactLocation().toSexagesimalString(this.includeEmptySeconds, this.includeReferenceSystem);
			result = CdmUtils.concat(", ", result, exactLocation);
		}

		//ecology
		result = CdmUtils.concat(", ", result, facade.getEcology());

		//gathering period
		//TODO period.toString ??
		TimePeriod gatheringPeriod = facade.getGatheringPeriod();
		result = CdmUtils.concat(", ", result, (gatheringPeriod == null? null : gatheringPeriod.toString()));

		//collector (team) and field number
		String collectorAndFieldNumber = getCollectorAndFieldNumber(facade);
		result = CdmUtils.concat(", ", result, collectorAndFieldNumber);


		return result;
	}


	protected String addPlantDescription(String result, DerivedUnitFacade facade) {

		//plant description
		result = CdmUtils.concat("; ", result, facade.getPlantDescription());
		if (StringUtils.isNotBlank(result) && !result.endsWith(".")){
			result += ".";
		}

		return result;
	}



	private String getCollectorAndFieldNumber(DerivedUnitFacade facade) {
		String result = "";
		AgentBase<?> collector = facade.getCollector();
		String fieldNumber = facade.getFieldNumber();
		Person primaryCollector = facade.getPrimaryCollector();

		if (collector == null){
			return fieldNumber;
		}else if(collector.isProtectedTitleCache()){
			return  CdmUtils.concat(" ", collector.getTitleCache(), fieldNumber);
		}else{
			result = "";
			Team collectorTeam;
			if (collector.isInstanceOf(Person.class)){
				collectorTeam = Team.NewInstance();
				if (primaryCollector == null){
					primaryCollector = CdmBase.deproxy(collector, Person.class);
				}
				collectorTeam.addTeamMember(primaryCollector);
			} else if (collector.isInstanceOf(Team.class) && ! collector.isProtectedTitleCache() ){
				collectorTeam = CdmBase.deproxy(collector, Team.class);
			}else{
				return  CdmUtils.concat(" ", collector.getTitleCache(), fieldNumber);
			}

			int counter = 0;
//			int teamSize = collectorTeam.getTeamMembers().size();
			boolean fieldNumberAdded = false;
			List<Person> teamMembers = collectorTeam.getTeamMembers();
			for (Person member : teamMembers){
				counter++;
				String concatString = TeamDefaultCacheStrategy.concatString(collectorTeam, teamMembers, counter);
				//(counter >= teamSize)? " & " : ", ";
				result = CdmUtils.concat(concatString, result, getMemberString(member) );
				if (member.equals(primaryCollector)){
					result = addFieldNumber(result, fieldNumber);
					fieldNumberAdded = true;
				}
			}
			if (collectorTeam.isHasMoreMembers()){
			    result = TeamDefaultCacheStrategy.addHasMoreMembers(result);
			}
			if (! fieldNumberAdded){
				result = addFieldNumber(result, fieldNumber);
			}
			return result;
		}

	}

	private String addFieldNumber(String str, String fieldNumber) {
		String result = CdmUtils.concat(" ", str, fieldNumber);
		return result;
	}



	/**
	 * Strategy to format a collector team member name
	 * @param member
	 * @return
	 */
	private String getMemberString(Person member) {
		if (StringUtils.isNotBlank(member.getLastname()) && ! member.isProtectedTitleCache() ){
			String result = member.getLastname();
			if  (StringUtils.isNotBlank(member.getFirstname())){
				result = member.getFirstname().substring(0,1) + ". " + result;
			}
			return result;
		}else{
			return member.getTitleCache();
		}
	}



	private boolean testPrimaryCollectorInCollectorTeam(AgentBase collector, Person primaryCollector) {
		if (collector.isInstanceOf(Person.class)){
			return collector.equals(primaryCollector);
		}else if (collector.isInstanceOf(Team.class)){
			Team collectorTeam = CdmBase.deproxy(collector, Team.class);
			return collectorTeam.getTeamMembers().contains(primaryCollector);
		}else{
			logger.warn("Collector is not of type person or team");
			return false;
		}
	}



	/**
	 * @param facade
	 */
	private String getCode(DerivedUnitFacade facade) {
		String code = "";
		if(facade.getCollection() != null){
			code = facade.getCollection().getCode();
			if (isBlank(code)){
				Institution institution = facade.getCollection().getInstitute();
				if (institution != null){
					code = institution.getCode();
				}
				if (isBlank(code)){
					Collection superCollection = facade.getCollection().getSuperCollection();
					if (superCollection != null){
						code = superCollection.getCode();
					}
				}
			}
		}
		return code;
	}

// ************************** GETTER / SETTER ******************************************************

	public boolean isIncludeSeconds() {
		return includeEmptySeconds;
	}



	public void setIncludeSeconds(boolean includeSeconds) {
		this.includeEmptySeconds = includeSeconds;
	}



	public void setIncludeReferenceSystem(boolean includeReferenceSystem) {
		this.includeReferenceSystem = includeReferenceSystem;
	}



	public boolean isIncludeReferenceSystem() {
		return includeReferenceSystem;
	}


}
