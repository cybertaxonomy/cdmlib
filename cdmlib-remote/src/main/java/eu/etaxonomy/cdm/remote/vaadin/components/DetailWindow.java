package eu.etaxonomy.cdm.remote.vaadin.components;

import java.util.Collection;

import org.springframework.context.annotation.Scope;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;


@Scope("prototype")
public class DetailWindow extends CustomComponent{

	
	private Collection<DescriptionElementBase> listDescriptions;
	private Taxon taxon;

	public DetailWindow(Taxon taxon, Collection<DescriptionElementBase> listDescriptions) {
		this.taxon = taxon;
		this.listDescriptions = listDescriptions;
		
	}
	
	public Window createWindow(){
		Window window = new Window();
		window.setHeight("600px");
		window.setWidth("400px");
		window.setCaption(taxon.getName().getTitleCache());
		window.setContent(constructDescriptionTree(taxon));
		return window;
	}
	
	private Tree constructDescriptionTree(Taxon taxon){
		Tree tree = new Tree();
		tree.setSizeUndefined();
		String parent = "Descriptive Data";
		tree.setValue(parent);
		initDescriptionTree(tree, listDescriptions, parent);
		return tree;
	}
	
	private void initDescriptionTree(Tree tree, Collection<DescriptionElementBase>listDescriptions, Object parent) {
		//TODO: sorting List
		for (DescriptionElementBase deb : listDescriptions){
			tree.addItem(deb.getFeature());
			tree.setItemCaption(deb.getFeature(), deb.getFeature().getTitleCache());
			tree.setParent(deb.getFeature(), parent);
			tree.setChildrenAllowed(deb.getFeature(), true);
			
			if(deb instanceof CategoricalData){
				CategoricalData cd = (CategoricalData) deb;
				if(cd.getStatesOnly().size() <= 1){
					for(StateData st  : cd.getStateData()){
						tree.addItem(st);
						tree.setItemCaption(st, st.getState().getTitleCache());
						tree.setParent(st, deb.getFeature());
						tree.setChildrenAllowed(st, false);
					}
				}else{
					//TODO: implement recursion
				}
			}else if(deb instanceof TextData){
				TextData td = (TextData) deb;
				tree.addItem(td);
				tree.setItemCaption(td, td.getText(Language.GERMAN()));
				tree.setParent(td, deb.getFeature());
				tree.setChildrenAllowed(td, false);
			}else if(deb instanceof Distribution){
				Distribution db = (Distribution) deb;
				
				tree.addItem(db.toString());
				tree.setParent(db.toString(), deb.getFeature());
				tree.setChildrenAllowed(db.toString(), true);
				
				tree.addItem(db.getStatus().toString());
				tree.setParent(db.getStatus().toString(), db.toString());
				tree.setChildrenAllowed(db.getStatus().toString(), false);
			}
			tree.expandItemsRecursively(parent);
		}

	}

}
