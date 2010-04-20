/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;


/**
 * The abstract class for such {@link AgentBase agents} ({@link Person persons} or {@link Team teams}) who might also be used
 * for authorship of {@link eu.etaxonomy.cdm.model.reference.ReferenceBase references} or of {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names}.
 * 
 * @author a.mueller
 * @version 1.0
 * @created 17-APR-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamOrPersonBase", propOrder = {
	"nomenclaturalTitle"
})
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.agent.AgentBase")
@Audited
public abstract class TeamOrPersonBase<T extends TeamOrPersonBase<?>> extends AgentBase<INomenclaturalAuthorCacheStrategy<T>> implements INomenclaturalAuthor {
	private static final long serialVersionUID = 5216821307314001961L;
	public static final Logger logger = Logger.getLogger(TeamOrPersonBase.class);

	@XmlElement(name="NomenclaturalTitle")
	@Field(index=Index.TOKENIZED)
	@NullOrNotEmpty
    @Size(max = 255)
	protected String nomenclaturalTitle;

	@Transient
	@XmlTransient
	protected boolean isGeneratingTitleCache = false;
	
	/**
	 * Returns the identification string (nomenclatural abbreviation) used in
	 * nomenclature for this {@link Person person} or this {@link Team team}.
	 * 
	 * @see  INomenclaturalAuthor#getNomenclaturalTitle()
	 */
	@Transient
	public String getNomenclaturalTitle() {
		String result = nomenclaturalTitle;
		if (CdmUtils.isEmpty(nomenclaturalTitle) && (isGeneratingTitleCache == false)){
			result = getTitleCache();
		}
		return result;
	}

	/** 
	 * @see     #getNomenclaturalTitle()
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle) {
		this.nomenclaturalTitle = nomenclaturalTitle;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 */
	@Override
	@Transient //TODO a.kohlbecker remove??
	public String getTitleCache() {
		isGeneratingTitleCache = true;
		String result = super.getTitleCache();
		result = replaceEmptyTitleByNomTitle(result);
		isGeneratingTitleCache = false;
		return result;
	}

	/**
	 * @param result
	 * @return
	 */
	protected String replaceEmptyTitleByNomTitle(String result) {
		if (CdmUtils.isEmpty(result)){
			result = nomenclaturalTitle;
		}
		if (CdmUtils.isEmpty(result)){
			result = super.getTitleCache();
		}
		return result;
	}
	
	
}
