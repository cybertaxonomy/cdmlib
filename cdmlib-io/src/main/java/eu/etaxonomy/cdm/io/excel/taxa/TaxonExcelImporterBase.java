/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

//import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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
	 * ------------------
	 * Id           
	 * ParentId
	 * Rank
	 * ScientificName
	 * Author
	 * NameStatus
	 * VernacularName
	 * Language
	 */
	/*
	 * Not yet supported columns:
	 * --------------------------
	 * Reference
	 */

	protected static final String ID_COLUMN = "Id";
	protected static final String PARENT_ID_COLUMN = "ParentId";
	protected static final String RANK_COLUMN = "Rank";
	protected static final String AUTHOR_COLUMN = "Author";
	protected static final String NAME_STATUS_COLUMN = "NameStatus";
	protected static final String VERNACULAR_NAME_COLUMN = "VernacularName";
	protected static final String LANGUAGE_COLUMN = "Language";
	protected static final String REFERENCE_COLUMN = "Reference";
	
	/** Already processed taxa */
	private HashMap<NormalExplicitRow, UUID> taxaMap = new HashMap<NormalExplicitRow, UUID>();
    /** Already processed authors */
	private HashSet<String> authors = new HashSet<String>();
	/** Previous taxon */
	private UUID previousTaxonUuid = null;
    /** Taxon "light" containing all string info from columns */
	private NormalExplicitRow normalExplicitRow = null;
	
	
	// TODO: This enum is for future use (perhaps).
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
	

	/**
	 * @return the taxa
	 */
	public HashMap<NormalExplicitRow, UUID> getTaxaMap() {
		return taxaMap;
	}

	/**
	 * @param taxa the taxa to set
	 */
	public void setTaxaMap(HashMap<NormalExplicitRow, UUID> taxaMap) {
		this.taxaMap = taxaMap;
	}

	/**
	 * @return the previousTaxon
	 */
	public UUID getPreviousTaxonUuid() {
		return previousTaxonUuid;
	}

	/**
	 * @param previousTaxon the previousTaxon to set
	 */
	public void setPreviousTaxonUuid(UUID uuid) {
		this.previousTaxonUuid = uuid;
	}

	/**
	 * @return the normalExplicitRow
	 */
	public NormalExplicitRow getTaxonLight() {
		return normalExplicitRow;
	}

	/**
	 * @param normalExplicitRow the normalExplicitRow to set
	 */
	public void setTaxonLight(NormalExplicitRow normalExplicitRow) {
		this.normalExplicitRow = normalExplicitRow;
	}

	/**
	 * @return the author
	 */
	public HashSet<String> getAuthors() {
		return authors;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthors(HashSet<String> authors) {
		this.authors = authors;
	}
}

