/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase;

/**
 * @author a.babadshanjan
 * @since 09.01.2009
 */
public abstract class TaxonExcelImportBase
        extends ExcelTaxonOrSpecimenImportBase<TaxonExcelImportState, ExcelImportConfiguratorBase, ExcelRowBase> {

    private static final long serialVersionUID = -9012570426721006621L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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

	protected static final String VERSION_COLUMN = "Version";
	protected static final String ID_COLUMN = "Id";

	protected static final String PARENT_ID_COLUMN = "ParentId";
	protected static final String RANK_COLUMN = "Rank";
	protected static final String AUTHOR_COLUMN = "Author";
	protected static final String AUTHORS_COLUMN = "Authors";
	protected static final String AUTHORSHIP_COLUMN = "Authorship";

    protected static final String BASIONYM_AUTHOR_COLUMN = "Basionymauthor";
    protected static final String PUBLISHING_AUTHOR_COLUMN = "Publishingauthor";
    protected static final String NAMESTATUS_COLUMN = "NameStatus";
    protected static final String NOMENCLATURAL_STATUS_COLUMN = "Nomenclaturalstatus";

	protected static final String VERNACULAR_NAME_COLUMN = "VernacularName";
	protected static final String LANGUAGE_COLUMN = "Language";
	protected static final String REFERENCE_COLUMN = "Reference";

	protected static final String PROTOLOGUE_COLUMN = "Protologue";
	protected static final String IMAGE_COLUMN = "Image";
	protected static final String TDWG_COLUMN = "TDWG";
	protected static final String COUNTRY_COLUMN = "Country";

	protected static final String SYNONYM_COLUMN = "Synonym";

	protected static final String DATE_COLUMN = "Date";
	protected static final String YEAR_COLUMN = "Year";
	protected static final String FAMILY_COLUMN = "Family";

	protected static final String BASIONYM_COLUMN = "Basionym";
    protected static final String REPLACED_SYNONYM_COLUMN = "ReplacedSynonym";
    protected static final String NOMENCLATURAL_SYNONYM_COLUMN = "NomenclaturalSynonym";
    protected static final String ACCEPTED_ID_COLUMN = "AcceptedID";
    protected static final String TAXONOMIC_STATUS = "TaxonomicStatusInTPL";

    protected static final String INFRA_FAMILY_COLUMN = "InfraFamily";
    protected static final String GENUS_COLUMN = "Genus";
    protected static final String INFRA_GENUS_COLUMN = "Infragenus";
    protected static final String SPECIES_COLUMN = "Species";
    protected static final String INFRA_SPECIES_COLUMN = "InfraSpecies";
    protected static final String INFRA_SPECIES_EPITHET_COLUMN = "Infraspecificepithet";

    protected static final String INFRA_SPECIES_RANK_COLUMN = "Infraspecificrank";
    protected static final String FULLNAME_COLUMN = "Fullnamewithoutfamilyandauthors";
    protected static final String HYBRID_GENUS_COLUMN = "HybridGenus";
    protected static final String HYBRID_COLUMN = "Hybrid";

    protected static final String PUBLICATION_COLUMN = "Publication";
    protected static final String COLLATION_COLUMN = "Collation";
    protected static final String PAGE_COLUMN = "Page";
    protected static final String PUBLICATION_YEAR_COLUMN = "PublicationYearFull";
    protected static final String REMARKS_COLUMN = "Remarks";
    protected static final String DISTRIBUTION_COLUMN = "Distribution";
    protected static final String CITATION_TYPE_COLUMN = "Citationtype";

    protected static final String IPNI_ID_COLUMN = "IPNIid";
    protected static final String SOURCE_COLUMN = "Source";
    protected static final String SOURCE_ID_COLUMN = "SourceId";
}