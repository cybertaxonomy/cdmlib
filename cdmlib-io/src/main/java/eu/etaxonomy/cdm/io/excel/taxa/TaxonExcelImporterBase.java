/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

//import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
/**
 * @author a.babadshanjan
 * @created 09.01.2009
 * @version 1.0
 */
public abstract class TaxonExcelImporterBase extends ExcelImporterBase {

	//private static final Logger logger = Logger.getLogger(TaxonExcelImporterBase.class);

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
	protected static final String REFERENCE_COLUMN = "Reference";
	
	// Stores already processed taxa
	private HashMap<Integer, TaxonLight> taxa = new HashMap<Integer, TaxonLight>();

	// TODO: This enum is for future use, perhaps.
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
	
	public String getAuthor() {
		
		return this.author;
	}
	
	public void setAuthor(String author) {
	
		this.author = author;
	}
	
	public String getNameStatus() {
		
		return this.nameStatus;
	}
	
	public void setNameStatus(String nameStatus) {
	
		this.nameStatus = nameStatus;
	}
	
	public String getCommonName() {
		
		return this.commonName;
	}
	
	public void setCommonName(String commonName) {
	
		this.commonName = commonName;
	}

	public String getLanguage() {
		
		return this.language;
	}
	
	public void setLanguage(String language) {
	
		this.language = language;
	}
	
	public String getReference() {
		
		return this.reference;
	}
	
	public void setReference(String reference) {
	
		this.reference = reference;
	}

	/**
	 * @return the taxa
	 */
	public HashMap<Integer, TaxonLight> getTaxa() {
		return taxa;
	}

	/**
	 * @param taxa the taxa to set
	 */
	public void setTaxa(HashMap<Integer, TaxonLight> taxa) {
		this.taxa = taxa;
	}
}

