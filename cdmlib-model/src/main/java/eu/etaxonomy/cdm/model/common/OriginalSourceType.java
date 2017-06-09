/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonName;



/**
 * The original source type is used to define the type of an {@link OriginalSourceBase original source}.<BR>
 * It is used to distinguish e.g. data lineage when importing data from one database to another from e.g. content oriented
 * sources such as the citation in a book.
 * In future they may come further source types.
 * @author a.mueller
 * @since 15.05.2013
 */
@XmlEnum
public enum OriginalSourceType implements IEnumTerm<OriginalSourceType>, Serializable{

	//0
	/**
	 * Unknown provenance is the type to be used if no information is available about the type
	 * of activity that happened.
	 *
	 */
	@XmlEnumValue("Unknown")
	Unknown(UUID.fromString("b48a443c-05f2-47ff-b885-1d3bd31118e1"), "Unknown Provenance", "UNK", null),

	//1
	/**
	 * Primary Taxonomic Source describes the sources a taxonomist uses to gather certain information.
	 * E.g. a taxonomist may have used three books/articles/other references to gather information
	 * about the distribution status of a taxon.
	 * He/she will store these references as original source of type Primary Taxonomic Source.
	 * This is a specialization of PROV-O Primary Source
	 * ({@link http://www.w3.org/TR/2013/REC-prov-o-20130430/#PrimarySource})
	 *
	 */
	@XmlEnumValue("Primary Taxonomic Source")
	PrimaryTaxonomicSource(UUID.fromString("c990beb3-3bc9-4dad-bbdf-9c11683493da"), "Primary Taxonomic Source", "PTS", null),

	//2
	/**
	 * Data Lineage describes the data life cycle of electronically available data. A typical
	 * use-case for data lineage is a data import from one database to another. Sources of
	 * type data lineage will store information about the original database and the identifier
	 * and table (->namespace) used in the original database.
	 * There are multiple types of data lineage: Blackbox, Dispatcher, Aggregator
	 * ({@link http://de.wikipedia.org/wiki/Data-Lineage})
	 */
	@XmlEnumValue("Data Lineage")
	Lineage(UUID.fromString("4f9fdf9a-f3b5-490c-96f0-90e050599b0e"), "Data Lineage", "DLI", null),

	//3
	/**
	 * Database Import is a specialization of {@value #Lineage}. It describes the electronic
	 * import of data from an external datasource into the given datasource. This step may
	 * include data transformations also but the primary process is the import of data.
	*/
	@XmlEnumValue("Database Import")
	Import(UUID.fromString("2a3902ff-06a7-4307-b542-c743e664b8f2"), "Database Import", "DIM", Lineage),

	//4
	/**
	 * Data Transformation is a specialization of {@value #Lineage} and describes a data
	 * transformation process that happens primarily on the given dataset but may also
	 * include external data.
	 */
	@XmlEnumValue("Data Transformation")
	Transformation(UUID.fromString("d59e80e5-cbb7-4658-b74d-0626bbb0da7f"), "Data Transformation", "TRA", Lineage),


	//5
	/**
	 * Data aggregation is a specification of {@value #Lineage} and describes the
	 * data transformation process that primarily includes data aggregation processes
	 * but may also include data imports and transformations.
	 */
	@XmlEnumValue("Data Aggregation")
	Aggregation(UUID.fromString("944f2f40-5144-4c81-80d9-f61aa10507b8"), "Data Aggregation", "DAG", Lineage),

	//6
	/**
	 * Primary Media Source describes the original source for any media file.
	 * E.g. a media may be copy of figure in book. The book itself will then be the primary media source.
     *
	 * This is a specialization of PROV-O Primary Source
	 * ({@link http://www.w3.org/TR/2013/REC-prov-o-20130430/#PrimarySource})
	 *
	 */
	@XmlEnumValue("Primary Media Source")
	PrimaryMediaSource(UUID.fromString("72be3615-a6da-4728-948a-b3c5797fa4bc"), "Primary Media Source", "PMS", null),

	//6
    /**
     * Nomenclatural reference as used for {@link TaxonName taxon names} and
     * type designations.
     * E.g. a media may be copy of figure in book. The book itself will then be the primary media source.
     *
     */
    @XmlEnumValue("Nomenclatural Reference")
    NomenclaturalReference(UUID.fromString(""), "Nomenclatural Reference", "NOR", null),


	//7
	/**
	 * <code>Other</code> is the type to be used if none of the other types is applicable.
	 */
	@XmlEnumValue("Other")
	Other(UUID.fromString("b7c4b7fe-0aef-428a-bb7b-9153a11bf845"), "Other", "OTH", null),

	;


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OriginalSourceType.class);


	private OriginalSourceType(UUID uuid, String defaultString, String key, OriginalSourceType parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}



// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<OriginalSourceType> delegateVoc;
	private IEnumTerm<OriginalSourceType> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(OriginalSourceType.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	@Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}

	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public OriginalSourceType getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<OriginalSourceType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(OriginalSourceType ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<OriginalSourceType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


	public static OriginalSourceType getByKey(String key){return delegateVoc.getByKey(key);}
    public static OriginalSourceType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


}
