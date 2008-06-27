/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import java.lang.reflect.Method;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Upmost abstract class for the use of a taxon name by a reference either
 * as a taxon ("accepted/correct" name) or as a (junior) synonym.
 * For instance: "Juncus longirostris Kuvaev sec. Kirschner, J. et al. 2002".
 * Within a taxonomic view/treatment a taxon name can be used only once.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonBase", propOrder = {
    "name",
    "sec"
})
@Entity
@Table(appliesTo="TaxonBase", indexes = { @Index(name = "taxonBaseTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class TaxonBase extends IdentifiableEntity {
	
	static Logger logger = Logger.getLogger(TaxonBase.class);
	
	private static Method methodTaxonNameAddTaxonBase;
	
	private static void initMethods()  { 
		if (methodTaxonNameAddTaxonBase == null){
			try {
				methodTaxonNameAddTaxonBase = TaxonNameBase.class.getDeclaredMethod("addTaxonBase", TaxonBase.class);
				methodTaxonNameAddTaxonBase.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO handle exception
			}
		}
	}
	
	protected TaxonBase(){
		super();
	}
	
	protected TaxonBase(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super();
		if (taxonNameBase != null){
			initMethods(); 
			this.invokeSetMethod(methodTaxonNameAddTaxonBase, taxonNameBase);  
		}
		this.setSec(sec);
	}
	
	//The assignment to the Taxon or to the Synonym class is not definitive
    @XmlAttribute(name = "isDoubtful")
	private boolean isDoubtful;
	
    @XmlElement(name = "Name", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private TaxonNameBase name;
	
	// The concept reference
    @XmlElement(name = "Sec")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private ReferenceBase sec;

	@Override
	public String generateTitle() {
		String title;
		if (name != null && name.getTitleCache() != null){
			title = name.getTitleCache() + " sec. ";
			if (sec != null){
				title += sec.getTitleCache();
			}else{
				title += "???";
			}
		}else{
			title = this.toString();
		}
		return title;
	}
	
	@ManyToOne
	@JoinColumn(name="taxonName_fk")
	@Cascade(CascadeType.SAVE_UPDATE)
	public TaxonNameBase getName(){
		return this.name;
	}
	@Deprecated //for hibernate use only, use taxon.addDescription() instead
	private void setName(TaxonNameBase newName){
		this.name = newName;
	}
	
	@Transient
	public HomotypicalGroup getHomotypicGroup(){
		if (this.getName() == null){
			return null;
		}else{
			return this.getName().getHomotypicalGroup();
		}
	}

	public boolean isDoubtful(){
		return this.isDoubtful;
	}
	public void setDoubtful(boolean isDoubtful){
		this.isDoubtful = isDoubtful;
	}

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public ReferenceBase getSec() {
		return sec;
	}

	public void setSec(ReferenceBase sec) {
		this.sec = sec;
	}
	
	@Transient
	public boolean isSaveable(){
		if (  (this.getName() == null)  ||  (this.getSec() == null)  ){
			return false;
		}else{
			this.toString();
			return true;
		}
	}
	

}