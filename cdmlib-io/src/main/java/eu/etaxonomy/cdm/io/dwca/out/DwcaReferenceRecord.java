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
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Rights;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public class DwcaReferenceRecord extends DwcaRecordBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaReferenceRecord.class);
	private Integer coreid;
	
	private String identifier;
	private String bibliographicCitation;
	private String title;
	private String creator;
	private TimePeriod date;
	private String source;
	private String description;
	private String subject;
	private Language language;
	private Set<Rights> rights;
	private String taxonRemarks;
	private String type;
	
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(identifier, writer, IS_NOT_FIRST);
		print(bibliographicCitation, writer, IS_NOT_FIRST);
		print(title, writer, IS_NOT_FIRST);
		print(creator, writer, IS_NOT_FIRST);
		//TODO
		print(getTimePeriod(date), writer, IS_NOT_FIRST);
		print(source, writer, IS_NOT_FIRST);
		print(description, writer, IS_NOT_FIRST);
		print(subject, writer, IS_NOT_FIRST);
		print(getLanguage(language), writer, IS_NOT_FIRST);
		print(rights, writer, IS_NOT_FIRST);
		print(taxonRemarks, writer, IS_NOT_FIRST);
		print(type, writer, IS_NOT_FIRST);
		writer.println();
	}


	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public Integer getCoreid() {
		return coreid;
	}

	public void setCoreid(Integer coreid) {
		this.coreid = coreid;
	}


}
