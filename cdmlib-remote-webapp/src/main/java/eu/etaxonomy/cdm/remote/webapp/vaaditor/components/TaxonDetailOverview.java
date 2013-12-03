package eu.etaxonomy.cdm.remote.webapp.vaaditor.components;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.Rank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

import com.gargoylesoftware.htmlunit.javascript.host.Text;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.TaxonServiceImpl;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
//import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.RedlistDTO;


/**
 * 
 * @author a.oppermann
 *
 */
@Component
@Scope("request")
public class TaxonDetailOverview extends CustomComponent {

	
	@Autowired
	private ITaxonService taxonService;
	
	@Autowired
	IDescriptionService descriptionService;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	
	
	/**
	 * automatically generated ID
	 */
	private static final long serialVersionUID = 5790735647945143529L;
	
	Logger logger = Logger.getLogger(TaxonDetailOverview.class);
	
	private transient Taxon taxon; 
	BeanItemContainer taxonBaseContainer;
	
	private TaxonTable taxonTable;
	
	public TaxonDetailOverview(Taxon taxon, TaxonTable taxonTable) {
		this.taxon = taxon;
		this.taxonTable = taxonTable;
//		constructTable();
	}
	
	@SuppressWarnings("unchecked")
	public Table constructTable(){
		taxonBaseContainer = new BeanItemContainer(RedlistDTO.class);
		Collection list = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, null);
		RedlistDTO redlistDTO = new RedlistDTO(taxon, list);
		taxonBaseContainer.addBean(redlistDTO);

		Table table = new Table();
		table.setSizeFull();
		table.setContainerDataSource(taxonBaseContainer);
		table.setSelectable(true);
		setSizeFull();
//		setCompositionRoot(table);
		return table;
	}
	
	public FormLayout constructForm(){
		Collection list = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, null);

		RedlistDTO redlistDTO = new RedlistDTO(taxon, list);

		final BeanFieldGroup<RedlistDTO> binder = new BeanFieldGroup<RedlistDTO>(RedlistDTO.class);
		binder.setItemDataSource(redlistDTO);
		
		final FormLayout form = new FormLayout();
		
		final Field<?> taxonField = binder.buildAndBind("Taxon Name: ", "fullTitleCache");
		taxonField.setSizeFull();
//		TextField rankField = (TextField) binder.buildAndBind("Rank: ", "rank");

//		rankField.setConverter(Rank.class);
		
//		form.addComponent(rankField);
		Button okButton = new Button("OK");
		okButton.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					BeanItem<RedlistDTO> beanItem = (BeanItem<RedlistDTO>) binder.getItemDataSource();
					binder.commit();
					logger.info(taxonField.getValue());
					RedlistDTO redlist = beanItem.getBean();
					logger.info("check das Taxon: "+ redlist.getTaxon());
					TaxonBase tnb = redlist.getTaxon();
//					taxonService.saveOrUpdate(tnb);
					TaxonServiceImpl taxService = (TaxonServiceImpl) applicationContext.getBean("taxonServiceImpl");
//					TaxonServiceImpl taxService = new TaxonServiceImpl();
					taxService.saveOrUpdate(tnb);
					taxonBaseContainer.addItem(beanItem.getBean());
					taxonTable.getTaxonBaseContainer().addItem(beanItem.getBean());
					taxonTable.markAsDirtyRecursive();
				} catch (CommitException e) {
					// TODO Auto-generated catch block
					logger.info("Commit Exception: "+e);
				}
				
			}
		});
		
		form.addComponent(taxonField);
		form.addComponent(okButton);
		binder.setBuffered(true);
		form.setImmediate(true);
		taxonField.commit();
		form.setSizeFull();
		
		return form;
	}

}


