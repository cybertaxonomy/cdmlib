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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Annotation;
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

	//TODO Collection_SEPARATOR
	protected static final CharSequence COLLECTION_SEPARATOR = "@";
	protected static final String FIELD_ENCLOSER = "\"";
	protected static final boolean IS_FIRST = false;
	protected static final boolean IS_NOT_FIRST = true;
	protected static final String SEP = ",";
	
	protected Map<String, URI> knownFields = new HashMap<String, URI>();
	protected Set<TermUris> knownTermFields = new HashSet<TermUris>();
	
	public abstract void write(PrintWriter writer);
	protected abstract void registerKnownFields();
	
	protected int count;
	private DwcaMetaDataRecord metaDataRecord;
	protected DwcaTaxExportConfigurator config;

	private Integer id;
	private UUID uuid;

	
	protected DwcaRecordBase(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		this.metaDataRecord = metaDataRecord;
		this.count = metaDataRecord.inc();
		this.config = config;
	}
	
	
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	protected void printNotes(Set<Annotation> notes, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		printNotes(notes, writer, addSeparator, fieldKey.getUriString());
	}
	protected void printNotes(Set<Annotation> notes, PrintWriter writer, boolean addSeparator, String fieldKey) {
		//FIXME handles annotations correctly
		String value = null;
		print(value, writer, addSeparator, fieldKey);
	}
	
//	protected void print(Object object, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
//		print(object == null ? null : object.toString(), writer, addSeparator, fieldKey);
//	}
	protected void print(DwcaId dwcaId, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(dwcaId == null ? null : dwcaId.getId(), writer, addSeparator, fieldKey);
	}
	protected void print(UUID uuid, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(uuid == null ? null : uuid.toString(), writer, addSeparator, fieldKey);
	}
	protected void print(AgentBase<?> agent, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(agent, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(AgentBase<?> agent, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(agent == null ? null : getAgent(agent), writer, addSeparator, fieldKey);
	}

	
	protected void print(Language language, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(language, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(Language language, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(language == null ? null : getLanguage(language), writer, addSeparator, fieldKey);
	}
	protected void print(LSID lsid, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(lsid, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(LSID lsid, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(lsid == null ? null : String.valueOf(lsid.toString()), writer, addSeparator, fieldKey);
	}
	
	protected void print(Set<Rights> rights, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(rights, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(Set<Rights> rights, PrintWriter writer, boolean addSeparator, String fieldKey) {
		String rightsString = getRights(rights);
		print(rightsString, writer, addSeparator, fieldKey);
	}
	protected void print(URI uri, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(uri, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(URI uri, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(uri == null ? null : String.valueOf(uri), writer, addSeparator, fieldKey);
	}
	
	protected void print(Point point, PrintWriter writer, boolean addSeparator, TermUris latitudeKey, TermUris longitudeKey) {
		print(point, writer, addSeparator, latitudeKey.getUriString(), longitudeKey.getUriString());
	}
	
	protected void print(Point point, PrintWriter writer, boolean addSeparator, String latitudeKey, String longitudeKey) {
		if (point == null){
			String toPrint = null;
			print(toPrint, writer, addSeparator, latitudeKey);
			print(toPrint, writer, addSeparator, longitudeKey);
		}else{
			String latitude = point.getLatitude().toString();
			String longitude = point.getLongitude().toString();
			print(latitude, writer, addSeparator, latitudeKey);
			print(longitude, writer, addSeparator, longitudeKey);
		}
	}
	protected void print(Boolean boolValue, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(boolValue, writer, addSeparator, fieldKey.getUriString());
	}	
	protected void print(Boolean boolValue, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(boolValue == null ? null : String.valueOf(boolValue), writer, addSeparator, fieldKey);
	}

	protected void print(Integer intValue, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(intValue, writer, addSeparator, fieldKey.getUriString());
	}
	protected void print(Integer intValue, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(intValue == null ? null : String.valueOf(intValue), writer, addSeparator, fieldKey);
	}
	
	protected void printId(Integer intValue, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(intValue == null ? null : String.valueOf(intValue), writer, addSeparator, fieldKey);
	}
	protected void printId(UUID uuid, PrintWriter writer, boolean addSeparator, String fieldKey) {
		print(uuid == null ? null : String.valueOf(uuid), writer, addSeparator, fieldKey);
	}

	protected void print(String value, PrintWriter writer, boolean addSeparator, TermUris fieldKey) {
		print(value, writer, addSeparator, fieldKey.getUriString());
	}
	
	protected void print(String value, PrintWriter writer, boolean addSeparator, String fieldKey) {
		if (count == 1 && addSeparator == IS_NOT_FIRST){
			registerFieldKey(URI.create(fieldKey));
		}
		String strToPrint = addSeparator ? SEP : "";
		if (StringUtils.isNotBlank(value)){
			strToPrint += FIELD_ENCLOSER + value + FIELD_ENCLOSER;
		}
		writer.print(strToPrint);
	}
	
	private void registerFieldKey(URI key) {
		this.metaDataRecord.addFieldEntry(key);
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

	protected String getOccurrenceStatus(PresenceAbsenceTermBase<?> status) {
		if (status == null){
			return "";
		}else{
			//TODO
			return status.getTitleCache();
		}
	}
	
	protected String getAgent(AgentBase<?> agent) {
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
	

	protected String getDesignationType(TypeDesignationStatusBase<?> status) {
		if (status == null){
			return "";
		}else{
			//TODO
			return status.getLabel();
		}
	}
	
	
	protected void addKnownField(String string, String uri) throws URISyntaxException {
		this.knownFields.put(string, new URI(uri));
	}
	
	protected void addKnownField(TermUris term) throws URISyntaxException {
		this.knownTermFields.add(term);
	}
}
