// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public abstract class DwcaRecordBase {
	private static final Logger logger = Logger.getLogger(DwcaRecordBase.class);

	protected static final CharSequence COLLECTION_SEPARATOR = null;

	protected static String FIELD_ENCLOSER = "\"";

	final protected boolean IS_FIRST = false;
	final protected boolean IS_NOT_FIRST = true;
	
	protected String SEP = ",";
	
	public abstract void write(PrintWriter writer);
	
	
	protected void print(AgentBase<?> agent, PrintWriter writer, boolean addSeparator) {
		print(agent == null ? null : getAgent(agent), writer, addSeparator);
	}

	
	protected void print(Language language, PrintWriter writer, boolean addSeparator) {
		print(language == null ? null : getLanguage(language), writer, addSeparator);
	}

	protected void print(LSID lsid, PrintWriter writer, boolean addSeparator) {
		print(lsid == null ? null : String.valueOf(lsid.toString()), writer, addSeparator);
	}
	protected void print(Set<Rights> rights, PrintWriter writer, boolean addSeparator) {
		String rightsString = getRights(rights);
		print(rightsString, writer, addSeparator);
	}
	protected void print(URI uri, PrintWriter writer, boolean addSeparator) {
		print(uri == null ? null : String.valueOf(uri), writer, addSeparator);
	}
	
	protected void print(Point point, PrintWriter writer, boolean addSeparator) {
		if (point == null){
			String toPrint = null;
			print(toPrint, writer, addSeparator);
			print(toPrint, writer, addSeparator);
		}else{
			String latitude = point.getLatitude().toString();
			String longitude = point.getLongitude().toString();
			print(latitude, writer, addSeparator);
			print(longitude, writer, addSeparator);
		}
	}
	protected void print(Boolean boolValue, PrintWriter writer, boolean addSeparator) {
		print(boolValue == null ? null : String.valueOf(boolValue), writer, addSeparator);
	}
	protected void print(Integer intValue, PrintWriter writer, boolean addSeparator) {
		print(intValue == null ? null : String.valueOf(intValue), writer, addSeparator);
	}
	protected void print(String value, PrintWriter writer, boolean addSeparator) {
		String strToPrint = addSeparator ? SEP : "";
		if (StringUtils.isNotBlank(value)){
			strToPrint += FIELD_ENCLOSER + value + FIELD_ENCLOSER;
		}
		writer.print(strToPrint);
	}
	


	
	protected String getRights(Rights rights) {
		if (rights == null){
			return "";
		}else{
			//TODO
			return rights.getAbbreviatedText();
		}
	}

	protected String getLanguage(Language language) {
		if (language == null){
			return "";
		}else{
			//TODO
			return language.getIso639_2();
		}
	}

	protected String getDate(DateTime date) {
		if (date == null){
			return "";
		}else{
			//TODO
			return date.toString();
		}
	}

	protected String getNomStatus(NomenclaturalStatusType nomStatus) {
		if (nomStatus == null){
			return "";
		}else{
			//TODO
			return nomStatus.getLabel();
		}
	}

	protected String getNomCode(NomenclaturalCode nomCode) {
		if (nomCode == null){
			return "";
		}else{
			//TODO
			return nomCode.getTitleCache();
		}
	}

	protected String getRank(Rank rank) {
		if (rank == null){
			return "";
		}else{
			//TODO
			return rank.getTitleCache();
		}
	}
	
	protected String getSex(Sex sex) {
		if (sex == null){
			return "";
		}else{
			//TODO
			return sex.getTitleCache();
		}
	}
	
	protected String getLifeStage(Stage stage) {
		if (stage == null){
			return "";
		}else{
			//TODO
			return stage.getTitleCache();
		}
	}

	protected String getOccurrenceStatus(PresenceAbsenceTermBase status) {
		if (status == null){
			return "";
		}else{
			//TODO
			return status.getTitleCache();
		}
	}
	
	protected String getAgent(AgentBase agent) {
		if (agent == null){
			return "";
		}else{
			//TODO
			return agent.getTitleCache();
		}
	}
	

	protected String getFeature(Feature feature) {
		if (feature == null){
			return "";
		}else{
			//TODO
			return feature.getTitleCache();
		}
	}

	
	protected String getTimePeriod(TimePeriod period) {
		if (period == null){
			return "";
		}else{
			//TODO
			return period.toString();
		}
	}
	
	protected String getTimePeriodPart(TimePeriod period, boolean useEnd) {
		if (period == null){
			return "";
		}else{
			Partial date = useEnd? period.getEnd(): period.getStart();
			if (date == null){
				return "";
			}else{
				//TODO
				return date.toString();
			}
		}
	}

	private String getRights(Set<Rights> rights) {
		if (rights == null || rights.isEmpty()){
			return null;
		}else{
			String result = null;
			for (Rights right: rights){
				//TODO usi uri if available ??
				String message = "Rights not yet fully implemented";
				logger.warn(message);
				result = CdmUtils.concat(COLLECTION_SEPARATOR, result, right.getAbbreviatedText());
			}
			return result;
		}
	}
	

	protected String getDesignationType(TypeDesignationStatusBase status) {
		if (status == null){
			return "";
		}else{
			//TODO
			return status.getLabel();
		}
	}
}
