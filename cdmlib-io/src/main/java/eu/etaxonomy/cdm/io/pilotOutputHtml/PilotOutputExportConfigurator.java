/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pilotOutputHtml;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author h.fradin (from a.babadshanjan JaxbExportConfigurator)
 * @created 09.12.2008
 */
public class PilotOutputExportConfigurator
        extends XmlExportConfiguratorBase<PilotOutputExportState> {

    private static final long serialVersionUID = 8545847974141343807L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PilotOutputExportConfigurator.class);

	private int maxRows = 0;

	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
	private boolean doOccurrence = true;
	private boolean doReferencedEntities = true;
	private boolean doSynonyms = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;
	private boolean doAuthors = true;
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	private boolean doTaxonNames = true;
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;

	//TODO
	private static IExportTransformer defaultTransformer = null;


	public static PilotOutputExportConfigurator NewInstance(ICdmDataSource source, String url, String destinationFolder) {
		return new PilotOutputExportConfigurator(source, destinationFolder + File.separator + url);
	}


	public int getMaxRows() {
		return maxRows;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}


	public boolean isDoAgentData() {
		return doAgentData;
	}

	public void setDoAgentData(boolean doAgentData) {
		this.doAgentData = doAgentData;
	}

	public boolean isDoLanguageData() {
		return doLanguageData;
	}

	public void setDoLanguageData(boolean doLanguageData) {
		this.doLanguageData = doLanguageData;
	}

	public boolean isDoFeatureData() {
		return doFeatureData;
	}

	public void setDoFeatureData(boolean doFeatureData) {
		this.doFeatureData = doFeatureData;
	}

	public boolean isDoDescriptions() {
		return doDescriptions;
	}

	public void setDoDescriptions(boolean doDescriptions) {
		this.doDescriptions = doDescriptions;
	}

	public boolean isDoMedia() {
		return doMedia;
	}

	public void setDoMedia(boolean doMedia) {
		this.doMedia = doMedia;
	}

	public boolean isDoReferencedEntities() {
		return doReferencedEntities;
	}

	public void setDoReferencedEntities(boolean doReferencedEntities) {
		this.doReferencedEntities = doReferencedEntities;
	}


	public boolean isDoSynonyms() {
		return doSynonyms;
	}

	public void setDoSynonyms(boolean doSynonyms) {
		this.doSynonyms = doSynonyms;
	}


	public boolean isDoTerms() {
		return doTerms;
	}

	public void setDoTerms(boolean doTerms) {
		this.doTerms = doTerms;
	}

	public boolean isDoTermVocabularies() {
		return doTermVocabularies;
	}

	public void setDoTermVocabularies(boolean doTermVocabularies) {
		this.doTermVocabularies = doTermVocabularies;
	}

	public boolean isDoHomotypicalGroups() {
		return doHomotypicalGroups;
	}

	public void setDoHomotypicalGroups(boolean doHomotypicalGroups) {
		this.doHomotypicalGroups = doHomotypicalGroups;
	}

	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}

	public boolean isDoAuthors() {
		return doAuthors;
	}
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}

	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}



//	@SuppressWarnings("unchecked")
	@Override
    protected void makeIoClassList() {
		ioClassList = new Class[] {
				PilotOutputDescriptionExporter.class,
		};
	}




	/**
	 * @param url
	 * @param destination
	 */
	private PilotOutputExportConfigurator(ICdmDataSource source, String url) {
		super(new File(url), source, defaultTransformer);
//		setDestination(url);
//		setSource(source);
	}


	@Override
    public File getDestination() {
		File file = super.getDestination();
		return file;
//		return super.getDestination();
	}


	/**
	 * @param file
	 */
	@Override
    public void setDestination(File fileName) {
		super.setDestination(fileName);
	}

	@Override
    public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return this.getDestination().toString();
		}
	}


	@Override
    public PilotOutputExportState getNewState() {
		return new PilotOutputExportState(this);
	}

}
