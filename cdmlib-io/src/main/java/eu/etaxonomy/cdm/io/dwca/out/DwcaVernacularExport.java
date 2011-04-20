/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaVernacularExport extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState> {
	private static final Logger logger = Logger.getLogger(DwcaVernacularExport.class);

	/**
	 * Constructor
	 */
	public DwcaVernacularExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 * 
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		String dbname = config.getSource() != null ? config.getSource().getName() : "unknown";
    	String fileName = config.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			final String coreTaxFileName = "vernacular.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			
			List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
			for (TaxonNode node : allNodes){
				DwcaVernacularRecord record = new DwcaVernacularRecord();
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<? extends DescriptionBase> descriptions = taxon.getDescriptions();
				for (DescriptionBase description : descriptions){
					for (Object o : description.getElements()){
						DescriptionElementBase el = CdmBase.deproxy(o, DescriptionElementBase.class);
						if (el.isInstanceOf(CommonTaxonName.class)){
							CommonTaxonName commonTaxonName = CdmBase.deproxy(el, CommonTaxonName.class);
							handleCommonTaxonName(record, commonTaxonName, taxon);
						}else if (el.getFeature().equals(Feature.COMMON_NAME())){
							//TODO
							String message = "Vernacular name export for TextData not yet implemented";
							logger.warn(message);
						}
					}
				}
				
				record.write(writer);
				writer.flush();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		commitTransaction(txStatus);
		return true;
	}
	



	private void handleCommonTaxonName(DwcaVernacularRecord record, CommonTaxonName commonTaxonName, Taxon taxon) {
		record.setCoreid(taxon.getId());
		record.setVernacularName(commonTaxonName.getName());
		//TODO mulitple sources 
		record.setSource(null);
		record.setLanguage(commonTaxonName.getLanguage());
		// does not exist in CDM
		record.setTemporal(null);
		
		if (commonTaxonName.getArea() != null){
			NamedArea area = commonTaxonName.getArea();
			record.setLocationId(area.getId());
			record.setLocality(area.getLabel());
			if (area.isInstanceOf(WaterbodyOrCountry.class)){
				WaterbodyOrCountry country = CdmBase.deproxy(area, WaterbodyOrCountry.class);
				record.setCountryCode(country.getIso3166_A2());
			}
		}
	}

	/**
	 * @param record
	 * @param taxon
	 * @param name
	 * @param parent
	 * @param basionym
	 * @param type 
	 * @return
	 */
	private void handleTaxonBase(DwcaTaxRecord record, TaxonBase taxonBase, NonViralName<?> name, 
			Taxon acceptedTaxon, Taxon parent, TaxonNameBase<?, ?> basionym, 
			RelationshipTermBase<?> type) {
		//ids als UUIDs?
		record.setId(taxonBase.getId());
		record.setScientificNameId(name.getId());
		record.setAcceptedNameUsageId(acceptedTaxon.getId());
		record.setParentNameUsageId(parent == null ? null : parent.getId());
		// ??? - is not a name usage (concept)
//			record.setOriginalNameUsageId(basionym.getId());
		record.setNameAccordingToId(taxonBase.getSec().getId());
		record.setNamePublishedInId(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getId());
		// what is the difference to id
		record.setTaxonConceptId(taxonBase.getId());
		
		record.setScientificName(name.getTitleCache());
		// ???
		record.setAcceptedNameUsage(acceptedTaxon.getTitleCache());
		record.setParentNameUsage(parent == null ? null : parent.getTitleCache());
		// ??? is not a nameUsage (concept)
		record.setOriginalNameUsage(basionym == null ? null : basionym.getTitleCache());
		record.setNameAccordingTo(taxonBase.getSec().getTitleCache());
		record.setNamePublishedIn(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getTitleCache());
		
		//???
		record.setHigherClassification(null);
		//... higher ranks
		
		//TODO other subgneric ranks ??
		record.setSubgenus(name.getInfraGenericEpithet());
		record.setSpecificEpithet(name.getSpecificEpithet());
		record.setInfraspecificEpithet(name.getInfraSpecificEpithet());
		
		record.setTaxonRank(name.getRank());
		record.setVerbatimTaxonRank(name.getRank().getTitleCache());
		record.setScientificNameAuthorship(name.getAuthorshipCache());
		
		// ???
		record.setVernacularName(null);
		
		record.setNomenclaturalCode(name.getNomenclaturalCode());
		// ???
		record.setTaxonRemarks(null);
		// ??? which date is needed here (taxon, name, sec, ... ?)
		record.setModified(taxonBase.getUpdated());
		// ???
		record.setLanguage(null);
		
		//....
		
		return;
	}


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return false;
	}
	
}
