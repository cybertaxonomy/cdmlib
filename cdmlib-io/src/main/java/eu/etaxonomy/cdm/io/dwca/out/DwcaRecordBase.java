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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaRecordBase.class);

	protected static String FIELD_ENCLOSER = "\"";

	final protected boolean IS_FIRST = false;
	final protected boolean IS_NOT_FIRST = true;
	
	protected String SEP = ",";
	
	
	
	public abstract void write(PrintWriter writer);
	

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
	
	protected String getTimePeriod(TimePeriod period) {
		if (period == null){
			return "";
		}else{
			//TODO
			return period.toString();
		}
	}
	
	protected String getTimePeriodPart(TimePeriod period, boolean useEnd) {
		Partial date = useEnd? period.getEnd(): period.getStart();
		if (date == null){
			return "";
		}else{
			//TODO
			return date.toString();
		}
	}
	
}
