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
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;


/**
 * The abstract class for such {@link AgentBase agents} ({@link Person persons} or {@link Team teams}) who might also be used
 * for authorship of {@link eu.etaxonomy.cdm.model.reference.Reference references} or of {@link eu.etaxonomy.cdm.model.name.TaxonName taxon names}.
 *
 * @author a.mueller
 * @since 17-APR-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamOrPersonBase", propOrder = {
    "nomenclaturalTitleCache",
    "collectorTitleCache"
})
@Entity
@Audited
public abstract class TeamOrPersonBase<T extends TeamOrPersonBase<T>>
            extends AgentBase<INomenclaturalAuthorCacheStrategy<T>>
            implements INomenclaturalAuthor {

    private static final long serialVersionUID = 5216821307314001961L;
    public static final Logger logger = Logger.getLogger(TeamOrPersonBase.class);

    //under construction #4311
    @XmlElement(name="CollectorTitleCache")
    @Field(index=Index.YES)
    @Column(length=800)//see #1592
    protected String collectorTitleCache;

    //under construction #9664
    @XmlElement(name="NomenclaturalTitleCache")
    @Field(index=Index.YES)
    @Column(length=800)//see #1592
    protected String nomenclaturalTitleCache;

//  from E+M import (still needed?)
//    @Column(length=255)
//    protected String originalNomenclaturalTitle;
//    public String getOriginalNomenclaturalTitle() {return originalNomenclaturalTitle;}
//    public void setOriginalNomenclaturalTitle(String originalNomenclaturalTitle) {this.originalNomenclaturalTitle = originalNomenclaturalTitle;}

    //#9664
    @Override
    public String getNomenclaturalTitleCache() {
        // is title dirty, i.e. equal NULL?
        if (nomenclaturalTitleCache == null){
            this.nomenclaturalTitleCache = generateNomenclaturalTitleCache();
            this.nomenclaturalTitleCache = getTruncatedCache(this.nomenclaturalTitleCache) ;
        }
        return nomenclaturalTitleCache;
    }

    //#4311
    public String getCollectorTitleCache() {
        // is title dirty, i.e. equal NULL?
        if (collectorTitleCache == null){
            this.collectorTitleCache = generateCollectorTitleCache();
            this.collectorTitleCache = getTruncatedCache(this.collectorTitleCache) ;
        }
        return collectorTitleCache;
    }
    public void setCollectorTitleCache(String collectorTitleCache) {
        //TODO
        this.collectorTitleCache = collectorTitleCache;
    }

    @SuppressWarnings("unchecked")
    private String generateNomenclaturalTitleCache() {
        if (getCacheStrategy() == null){
            return this.getClass() + ": " + this.getUuid();
        }else{
            return getCacheStrategy().getNomenclaturalTitleCache((T)this);
        }
    }

    @SuppressWarnings("unchecked")
    private String generateCollectorTitleCache() {
        if (getCacheStrategy() == null){
            return this.getClass() + ": " + this.getUuid();
        }else{
            return getCacheStrategy().getCollectorTitleCache((T)this);
        }
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
        String result = super.getTitleCache();
        result = replaceEmptyTitleByNomTitle(result);
        return result;
    }

    @Transient
    public String getFullTitle() {
        @SuppressWarnings("unchecked")
        T agent = (T)this;
        if (agent.isProtectedTitleCache()){
            return agent.getTitleCache();
        }else{
            return this.getCacheStrategy().getFullTitle(agent);
        }
    }

    protected String replaceEmptyTitleByNomTitle(String result) {
        if (isBlank(result)){
            result = nomenclaturalTitleCache;
        }
        if (isBlank(result)){
            result = super.getTitleCache();
        }
        return result;
    }

    @Override
    public TeamOrPersonBase<T> clone() throws CloneNotSupportedException {
        TeamOrPersonBase<T> result = (TeamOrPersonBase<T>)super.clone();

        //nothing to do: collectorTitle, nomenclaturalTitle;
        return result;
    }
}
