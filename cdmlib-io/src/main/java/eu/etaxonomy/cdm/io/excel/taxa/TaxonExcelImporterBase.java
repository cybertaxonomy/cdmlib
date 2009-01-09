/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IIoConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 09.01.2009
 * @version 1.0
 */
public abstract class TaxonExcelImporterBase extends ExcelImporterBase {

	private static final Logger logger = Logger.getLogger(TaxonExcelImporterBase.class);

	/*
	 * Supported Columns:
	 * Id           
	 * ParentId     - create taxon relationship  
	 * Rank         - create taxon name of this rank
	 * ScientificName
	 * Author
	 * NameStatus
	 * VernacularName
	 * Language
	 */

	protected static final String ID_COLUMN = "Id";
	protected static final String PARENT_ID_COLUMN = "ParentId";
	protected static final String RANK_COLUMN = "Rank";
	protected static final String AUTHOR_COLUMN = "Author";
	protected static final String NAME_STATUS_COLUMN = "NameStatus";
	protected static final String VERNACULAR_NAME_COLUMN = "VernacularName";
	protected static final String LANGUAGE_COLUMN = "Language";
	
	protected enum Columns { 
		Id("Id"), 
		ParentId("ParentId"), 
		Rank("Rank"),
		ScientificName("ScientificName"),
		Author("Author"),
		NameStatus("NameStatus"),
		VernacularName("VernacularName"),
		Language("Language");
		
	private String head;
	private String value;

	Columns(String head) {
		this.head = head;
	}
	
	public String head() {
		return this.head;
	}

	public String value() {
		return this.value;
	}
	}
	
	private String rank = "";
	private String nameStatus = "";
	private String commonName = "";
	private String author = "";
	private String language = "";
	private String reference = "";
	private int id = 0;
	private int parentId = 0;
	
	/* Need already processed records? */
	
	public String getRank() {
		
		return this.rank;
	}
	
	public void setRank(String rank) {
	
		this.rank = rank;
	}
	
	public int getId() {
		
		return this.id;
	}
	
	public void setId(int id) {
	
		this.id = id;
	}
	
	public int getParentId() {
		
		return this.parentId;
	}
	
	public void setParentId(int parentId) {
	
		this.parentId = parentId;
	}
}
