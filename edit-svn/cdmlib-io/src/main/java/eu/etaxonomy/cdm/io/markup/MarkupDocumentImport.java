/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.XmlImportBase;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * 
 */
/**
 * @author pesiimport
 *
 */
@Component
public class MarkupDocumentImport extends XmlImportBase<MarkupImportConfigurator, MarkupImportState> implements ICdmIO<MarkupImportState> {
	private static final Logger logger = Logger.getLogger(MarkupDocumentImport.class);


	// TODO make part of state, but state is renewed when invoking the import a
	// second time
	private UnmatchedLeads unmatchedLeads;


	@Autowired
	private IEditGeoService editGeoService;
	
	public MarkupDocumentImport() {
		super();
	}

	@Override
	public boolean doCheck(MarkupImportState state) {
		state.setCheck(true);
		doInvoke(state);
		state.setCheck(false);
		return state.isSuccess();
	}

	@Override
	public void doInvoke(MarkupImportState state) { 
		fireProgressEvent("Start import markup document", "Before start of document");
		
		Queue<CdmBase> outputStream = new LinkedList<CdmBase>();

		TransactionStatus tx = startTransaction();
		// FIXME reset state
		doAllTheOldOtherStuff(state);

		// START
		try {
			// StAX
			XMLEventReader reader = getStaxReader(state);
			state.setReader(reader);
			
			// start document
			if (!validateStartOfDocument(reader)) {
				state.setUnsuccessfull();
				return;
			}

			MarkupDocumentImportNoComponent x = new MarkupDocumentImportNoComponent(this);
			x.doInvoke(state);
			
			commitTransaction(tx);

			// //SAX
			// ImportHandlerBase handler= new PublicationHandler(this);
			// parseSAX(state, handler);

		} catch (FactoryConfigurationError e1) {
			fireWarningEvent("Some error occurred while setting up xml factory. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
		} catch (XMLStreamException e1) {
			fireWarningEvent("An XMLStreamException occurred while parsing. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
		}
		
		return;

	}


	/**
	 * This comes from the old version, needs to be checked on need
	 * 
	 * @param state
	 */
	private void doAllTheOldOtherStuff(MarkupImportState state) {
		state.putTree(null, null);
		if (unmatchedLeads == null) {
			unmatchedLeads = UnmatchedLeads.NewInstance();
		}
		state.setUnmatchedLeads(unmatchedLeads);

		// TransactionStatus tx = startTransaction();
		unmatchedLeads.saveToSession(getPolytomousKeyNodeService());

		// TODO generally do not store the reference object in the config
		Reference<?> sourceReference = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common
	 * .IImportConfigurator)
	 */
	protected boolean isIgnore(MarkupImportState state) {
		return !state.getConfig().isDoTaxa();
	}
	

// ************************* OPEN AFTER REFACTORING ****************************************/	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#fireWarningEvent(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void fireWarningEvent(String message, String dataLocation, Integer severity) {
		super.fireWarningEvent(message, dataLocation, severity, 1);
	}
	
	public void fireWarningEvent(String message, String dataLocation, Integer severity, int stackDepth) {
		super.fireWarningEvent(message, dataLocation, severity, stackDepth + 1);
	}
	
	public void fireSchemaConflictEventExpectedStartTag(String elName, XMLEventReader reader) throws XMLStreamException {
		super.fireSchemaConflictEventExpectedStartTag(elName, reader);
	}

// ************************* END Events ************************************************************
	
	
	public boolean isStartingElement(XMLEvent event, String elName) throws XMLStreamException {
		return super.isStartingElement(event, elName);
	}

	public boolean isEndingElement(XMLEvent event, String elName) throws XMLStreamException {
		return super.isEndingElement(event, elName);
	}	
	
	public void fillMissingEpithetsForTaxa(Taxon parentTaxon, Taxon childTaxon) {
		super.fillMissingEpithetsForTaxa(parentTaxon, childTaxon);
	}
	
	public Feature getFeature(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<Feature> voc){
		return super.getFeature(state, uuid, label, text, labelAbbrev, voc);
	}
	
	public ExtensionType getExtensionType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev){
		return super.getExtensionType(state, uuid, label, text, labelAbbrev);
	}
	
	public AnnotationType getAnnotationType(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<AnnotationType> voc){
		return super.getAnnotationType(state, uuid, label, text, labelAbbrev, voc);
	}
	
	public TextData getFeaturePlaceholder(MarkupImportState state, DescriptionBase<?> description, Feature feature, boolean createIfNotExists) {
		return super.getFeaturePlaceholder(state, description, feature, createIfNotExists);
	}
	
	public NamedAreaLevel getNamedAreaLevel(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary<NamedAreaLevel> voc){
		return super.getNamedAreaLevel(state, uuid, label, text, labelAbbrev, voc);
	}
	
	public NamedArea getNamedArea(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc, TermMatchMode matchMode){
		return super.getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level, voc, matchMode);
	}
	
	public Language getLanguage(MarkupImportState state, UUID uuid, String label, String text, String labelAbbrev, TermVocabulary voc){
		return super.getLanguage(state, uuid, label, text, labelAbbrev, voc);
	}
	
	public boolean getReadMediaData(){
		return this.READ_MEDIA_DATA;
	}
	
	public Media getImageMedia(String uriString, boolean readMediaData, boolean isFigure) throws MalformedURLException {
		return super.getImageMedia(uriString, readMediaData, isFigure);
	}

	public IEditGeoService getEditGeoService() {
		return editGeoService;
	}
	
	
		
	
}
