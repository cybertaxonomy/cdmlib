/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;


/**
 * The abstract class for such {@link AgentBase agents} ({@link Person persons} or {@link Team teams}) who might also be used
 * for authorship of {@link eu.etaxonomy.cdm.model.reference.Reference references} or of {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names}.
 *
 * @author a.mueller
 * @created 17-APR-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamOrPersonBase", propOrder = {
    "nomenclaturalTitle",
    "collectorTitle"
})
@Entity
@Audited
public abstract class TeamOrPersonBase<T extends TeamOrPersonBase<T>>
            extends AgentBase<INomenclaturalAuthorCacheStrategy<T>>
            implements INomenclaturalAuthor {

    private static final long serialVersionUID = 5216821307314001961L;
    public static final Logger logger = Logger.getLogger(TeamOrPersonBase.class);

    @XmlElement(name="NomenclaturalTitle")
    @Field(index=Index.YES)
  //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    protected String nomenclaturalTitle;

    //under construction #4311
    @XmlElement(name="CollectorTitle")
    @Field(index=Index.YES)
    @Column(length=255)
    protected String collectorTitle;


    @Transient
    @XmlTransient
    protected boolean isGeneratingTitleCache = false;  //state variable to avoid recursions when generating title cache and nomenclatural title

    /**
     * Returns the identification string (nomenclatural abbreviation) used in
     * nomenclature for this {@link Person person} or this {@link Team team}.
     *
     * @see  INomenclaturalAuthor#getNomenclaturalTitle()
     */
    @Override
    @Transient
    public String getNomenclaturalTitle() {
        String result = nomenclaturalTitle;
        if (isBlank(nomenclaturalTitle) && (isGeneratingTitleCache == false)){
            result = getTitleCache();
        }
        return result;
    }

    /**
     * @see     #getNomenclaturalTitle()
     */
    @Override
    public void setNomenclaturalTitle(String nomenclaturalTitle) {
        this.nomenclaturalTitle = isBlank(nomenclaturalTitle) ? null : nomenclaturalTitle;
    }


    @Override
    @Transient
    /*
        TODO  is the transient annotation still needed, can't we remove this ??
        @Transient is an absolutely special case and thus leads to several
        special implementations in order to harmonize this exception again
        in other parts of the library:
         - eu.etaxonomy.cdm.remote.controller.AgentController.doGetTitleCache()
         - eu.etaxonomy.cdm.remote.json.processor.bean.TeamOrPersonBaseBeanProcessor

        [a.kohlbecker May 2011]
    */
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
        if (isBlank(result)){
            result = nomenclaturalTitle;
        }
        if (isBlank(result)){
            result = super.getTitleCache();
        }
        return result;
    }

}
