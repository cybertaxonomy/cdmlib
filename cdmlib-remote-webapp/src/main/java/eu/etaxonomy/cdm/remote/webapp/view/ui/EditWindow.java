// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.webapp.view.ui;


import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.tools.ant.types.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.TaxonServiceImpl;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.oppermann
 * @date 18.04.2013
 *
 */
public class EditWindow extends CustomComponent implements View{

 	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
 
 	@AutoGenerated
 	private AbsoluteLayout mainLayout;
 	@AutoGenerated
 	private TabSheet editTabSheet;
 	@AutoGenerated
 	private TwinColSelect twinColSelect_1;
 	@AutoGenerated
 	private AbsoluteLayout editSynonymTabLayout;
 	@AutoGenerated
 	private Button cancelSynonymButton;
 	@AutoGenerated
 	private Button saveSynonymButton;
 	@AutoGenerated
 	private TextField editSynonymTextField;
 	@AutoGenerated
 	private AbsoluteLayout editTaxonTabLayout;
 	@AutoGenerated
 	private NativeButton cancelTaxonButton;
 	@AutoGenerated
 	private NativeButton saveTaxonButton;
 	@AutoGenerated
 	private TextField editTaxonTextField;
 	private TaxonBase taxonBase;
 	private ITaxonService taxonService = new TaxonServiceImpl();//        NewInstance();
 	private INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
 	
 	private static final Logger logger = Logger.getLogger(EditWindow.class);
 
 	/**
 	 * The constructor should first build the main layout, set the
 	 * composition root and then do any custom initialization.
 	 *
 	 * The constructor will not be automatically regenerated by the
 	 * visual editor.
 	 */
 	public EditWindow(TaxonBase taxonBase) {
 		if(taxonBase != null && parser != null && taxonService != null){
 			this.taxonBase = taxonBase;
 //			this.parser = parser;
 //			this.taxonService = taxonService;
 		}
 		buildMainLayout();
 		handleListener();
 		setCompositionRoot(mainLayout);
 
 		// TODO add user code here
 	}
 
 	/* (non-Javadoc)
 	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
 	 */
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
 
 	@SuppressWarnings({ "rawtypes", "serial"})
 	private void handleListener(){
 		saveSynonymButton.addClickListener(new ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				String saveStringAsSynonym = editSynonymTextField.getValue();
 				TaxonNameBase tnb = parser.parseFullName(saveStringAsSynonym, null, null);
 				if(tnb.hasProblem()){
 					int i = 0;
 					Iterator iterator = tnb.getParsingProblems().iterator();
 					while(iterator.hasNext() == true){
 						logger.info(iterator);
 						iterator.next();
 					}
 					logger.info(tnb.getParsingProblems());
 				}
 				Synonym synonym = Synonym.NewInstance(tnb, null);
 				logger.info(synonym + "  name: " + synonym.getTitleCache());
 				
 				logger.info("Parsing Errors " + tnb.getParsingProblems());
 
 				Taxon taxon = (Taxon) taxonService.load(taxonBase.getUuid());
 				if(taxon.hasSynonyms()){
 					//iterate over all synonyms
 					//check if synonym already exists
 					//if not save else notification
 				}else{
 						taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
 						taxonService.saveOrUpdate(taxon);
 				}
 			}
 		});
 	}
 
 	//getter setter
 	public TextField getEditSynonymTextField() {
 		return editSynonymTextField;
 	}
 
 	public void setEditSynonymTextField(String editSynonymTextString) {
 		this.editSynonymTextField.setValue(editSynonymTextString);
 	}
 
 	public TextField getEditTaxonTextField() {
 		return editTaxonTextField;
 	}
 
 	public void setEditTaxonTextField(String editTaxonTextString) {
 		this.editTaxonTextField.setValue(editTaxonTextString);
 	}
 
 	@AutoGenerated
 	private AbsoluteLayout buildMainLayout() {
 		// common part: create layout
 		mainLayout = new AbsoluteLayout();
 		mainLayout.setImmediate(false);
 		mainLayout.setWidth("100%");
 		mainLayout.setHeight("100%");
 		
 		// top-level component properties
 		setWidth("100.0%");
 		setHeight("100.0%");
 		
 		// editTabSheet
 		editTabSheet = buildEditTabSheet();
 		mainLayout.addComponent(editTabSheet,
 				"top:0.0px;right:7.0px;bottom:8.0px;left:0.0px;");
 		
 		return mainLayout;
 	}
 
 	@AutoGenerated
 	private TabSheet buildEditTabSheet() {
 		// common part: create layout
 		editTabSheet = new TabSheet();
 		editTabSheet.setStyleName("Taxon");
 		editTabSheet.setImmediate(true);
 		editTabSheet.setWidth("100.0%");
 		editTabSheet.setHeight("100.0%");
 		
 		// editTaxonTabLayout
 		editTaxonTabLayout = buildEditTaxonTabLayout();
 		editTabSheet.addTab(editTaxonTabLayout, "Edit Taxon", null);
 		
 		// editSynonymTabLayout
 		editSynonymTabLayout = buildEditSynonymTabLayout();
 		editTabSheet.addTab(editSynonymTabLayout, "Edit Synonym", null);
 		
 		// twinColSelect_1
 		twinColSelect_1 = new TwinColSelect();
 		twinColSelect_1.setImmediate(false);
 		twinColSelect_1.setWidth("100.0%");
 		twinColSelect_1.setHeight("100.0%");
 		editTabSheet.addTab(twinColSelect_1, "Tab", null);
 		
 		return editTabSheet;
 	}
 
 	@AutoGenerated
 	private AbsoluteLayout buildEditTaxonTabLayout() {
 		// common part: create layout
 		editTaxonTabLayout = new AbsoluteLayout();
 		editTaxonTabLayout.setCaption("Edit Taxon");
 		editTaxonTabLayout.setImmediate(false);
 		editTaxonTabLayout.setWidth("100.0%");
 		editTaxonTabLayout.setHeight("100.0%");
 		
 		// editTaxonTextField
 		editTaxonTextField = new TextField();
 		editTaxonTextField.setCaption("Enter taxon name:");
 		editTaxonTextField.setImmediate(false);
 		editTaxonTextField.setWidth("100.0%");
 		editTaxonTextField.setHeight("-1px");
 		editTaxonTabLayout.addComponent(editTaxonTextField,
 				"top:48.0px;right:21.0px;left:19.0px;");
 		
 		// saveTaxonButton
 		saveTaxonButton = new NativeButton();
 		saveTaxonButton.setCaption("Save");
 		saveTaxonButton.setImmediate(true);
 		saveTaxonButton.setWidth("-1px");
 		saveTaxonButton.setHeight("-1px");
 		editTaxonTabLayout.addComponent(saveTaxonButton,
 				"top:88.0px;left:19.0px;");
 		
 		// cancelTaxonButton
 		cancelTaxonButton = new NativeButton();
 		cancelTaxonButton.setCaption("Cancel");
 		cancelTaxonButton.setImmediate(true);
 		cancelTaxonButton.setWidth("-1px");
 		cancelTaxonButton.setHeight("-1px");
 		editTaxonTabLayout.addComponent(cancelTaxonButton,
 				"top:88.0px;left:79.0px;");
 		
 		return editTaxonTabLayout;
 	}
 
 	@AutoGenerated
 	private AbsoluteLayout buildEditSynonymTabLayout() {
 		// common part: create layout
 		editSynonymTabLayout = new AbsoluteLayout();
 		editSynonymTabLayout.setCaption("Edit Synonym");
 		editSynonymTabLayout.setImmediate(false);
 		editSynonymTabLayout.setWidth("100.0%");
 		editSynonymTabLayout.setHeight("100.0%");
 		
 		// editSynonymTextField
 		editSynonymTextField = new TextField();
 		editSynonymTextField.setCaption("Enter synonym name:");
 		editSynonymTextField.setImmediate(false);
 		editSynonymTextField.setWidth("100.0%");
 		editSynonymTextField.setHeight("-1px");
 		editSynonymTabLayout.addComponent(editSynonymTextField,
 				"top:48.0px;right:21.0px;left:19.0px;");
 		
 		// saveSynonymButton
 		saveSynonymButton = new Button();
 		saveSynonymButton.setCaption("Save");
 		saveSynonymButton.setImmediate(true);
 		saveSynonymButton.setWidth("-1px");
 		saveSynonymButton.setHeight("-1px");
 		editSynonymTabLayout.addComponent(saveSynonymButton,
 				"top:88.0px;left:19.0px;");
 		
 		// cancelSynonymButton
 		cancelSynonymButton = new Button();
 		cancelSynonymButton.setCaption("Cancel");
 		cancelSynonymButton.setImmediate(true);
 		cancelSynonymButton.setWidth("-1px");
 		cancelSynonymButton.setHeight("-1px");
 		editSynonymTabLayout.addComponent(cancelSynonymButton,
 				"top:88.0px;left:79.0px;");
 		
 		return editSynonymTabLayout;
 	}
 	
}
