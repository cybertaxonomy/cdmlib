// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class creates heterotypic synonymy relationships to the accepted taxon for 
 * basionym synonyms. 
 * 
 * @author a.babadshanjan
 * @created 22.09.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaHeterotypicSynonymImport extends CdmImportBase<FaunaEuropaeaImportConfigurator, FaunaEuropaeaImportState>
implements ICdmImport<FaunaEuropaeaImportConfigurator, FaunaEuropaeaImportState> {
	private static final Logger logger = Logger
			.getLogger(FaunaEuropaeaHeterotypicSynonymImport.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		logger.warn("Checking for heterotypic synonyms for basionyms not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doInvoke(FaunaEuropaeaImportState state) {
		
		TransactionStatus txStatus = null;
		List<Synonym> synonymList = null;
		Set<Taxon> taxonSet = null;
		int i = 0;
		int start = 0;

		int limit = state.getConfig().getLimitSave();
		int nbrOfSynonyms = getTaxonService().count(Synonym.class);
		if (logger.isInfoEnabled()) {
			logger.info("Number of synonyms = " + nbrOfSynonyms);
		}
		
		while (i < nbrOfSynonyms) {

			try {
				if ((i++ % limit) == 0) {

					start = (i == 1) ? 0 : i;
					if (logger.isInfoEnabled()) {
						logger.info("Retrieving synonyms starting from: " + start);
					}
					txStatus = startTransaction();
					synonymList = getTaxonService().getAllSynonyms(limit, start);
					taxonSet = new HashSet<Taxon>(limit);
				}

				if (((i % limit) == 0 && i != 1 ) || i == nbrOfSynonyms) { 

					HomotypicalGroup homotypicalGroup = null;
					Set<TaxonNameBase> basionyms = null;
					Set<NameRelationship> nameRelations = null;
					TaxonNameBase basionym = null;
					Set<TaxonBase> taxonBases = null;
					TaxonBase taxonBase = null;
					Taxon acceptedTaxon = null;
					TaxonNameBase synonymName = null;
					NameRelationship nameRelation = null;
					TaxonNameBase acceptedName = null;
					
					for (Synonym synonym : synonymList) {
						synonymName = synonym.getName();
						if (synonymName.isGroupsBasionym()) {
							nameRelations = synonymName.getNameRelations();
							if (nameRelations != null && nameRelations.iterator().hasNext()) {
								nameRelation = nameRelations.iterator().next();
								acceptedName = nameRelation.getToName();
								logger.info("SynonymName: " + synonymName + " titleCache of synonym: "+synonym.getTitleCache() + " name of acceptedTaxon: " + acceptedName.getTitleCache());
								if (logger.isTraceEnabled()) {
									logger.trace("toName: " + acceptedName);
									logger.trace("fromName: " + nameRelation.getFromName());
								}
								taxonBases = acceptedName.getTaxa();
								if (taxonBases != null && taxonBases.iterator().hasNext()) {
									taxonBase = taxonBases.iterator().next();
									acceptedTaxon = taxonBase.deproxy(taxonBase, Taxon.class);
									Set <Synonym> synonyms = acceptedTaxon.getSynonyms();
									if (!synonyms.contains(synonym)){
									//TODO: Achtung!!!!! dies wird auch bei homotypischen Synonymen aufgerufen! Dadurch wird ein weiteres Synonym erzeugt
									acceptedTaxon.addHeterotypicSynonymName(synonymName);
									taxonSet.add(acceptedTaxon);
								}
							}
						}
					}
					}
						
					getTaxonService().save((Collection)taxonSet);
					taxonSet = null;
					synonymList = null;
					commitTransaction(txStatus);
					if(logger.isInfoEnabled()) { 
						logger.info("i = " + i + " - Transaction committed"); 
					}
				}

			} catch (Exception e) {
				logger.warn("An exception occurred when creating heterotypic synonym relationship # " + i );
				e.printStackTrace();
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(FaunaEuropaeaImportState state) {
		return !(state.getConfig().isDoHeterotypicSynonymsForBasionyms());
	}
}