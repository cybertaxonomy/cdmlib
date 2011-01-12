// $Id: TaxonBaseBeanProcessor.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NameCacheStrategyBase;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 *
 */
public class TaxonNameBaseBeanProcessor extends AbstractCdmBeanProcessor<TaxonNameBase> {

	public static final Logger logger = Logger.getLogger(TaxonNameBaseBeanProcessor.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				// ignore nameRelations to avoid LazyLoadingExceptions coming 
				// from NameRelationshipBeanProcessor.secondStep() in which 
				// the transient field fromName is added to the serialization
				"relationsFromThisName",
				"relationsToThisName",
				"combinationAuthorTeam",
				"basionymAuthorTeam",
				"exCombinationAuthorTeam",
				"exBasionymAuthorTeam"
		});
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStage(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TaxonNameBase bean, JSONObject json, JsonConfig jsonConfig) {
		if(logger.isDebugEnabled()){
			logger.debug("processing second step" + bean);
		}
		json.element("taggedName", getTaggedName(bean), jsonConfig);
		return json;
	}
	
	/**
	 * FIXME ugly method - this functionality mainly be performed by ChachStrategies ?
	 * 
	 * @param taxonNameBase
	 * @return
	 */
	public static List<TaggedText> getTaggedName(TaxonNameBase<TaxonNameBase<?,?>, INameCacheStrategy<TaxonNameBase<?,?>>> taxonNameBase){
		
		List<TaggedText> tags = new ArrayList<TaggedText>();
		
		/** 
		 * taxonNameBase.getHibernateLazyInitializer().getImplementation();
		 * class eu.etaxonomy.cdm.model.name.TaxonNameBase$$EnhancerByCGLIB$$3683183d
		 * @link( CGLIBLazyInitializer.getImplementation())
		 */
//		if(taxonNameBase instanceof HibernateProxy) {
//			LazyInitializer lazyInitializer = ((HibernateProxy)taxonNameBase).getHibernateLazyInitializer();
//			taxonNameBase = (TaxonNameBase)lazyInitializer.getImplementation();
//		}
		taxonNameBase = HibernateProxyHelper.deproxy(taxonNameBase, TaxonNameBase.class);
		
		//FIXME rude hack:
		if(!(taxonNameBase instanceof NonViralName)){
			return tags;
		}
		
		// --- end of rude hack
		//FIXME infrageneric epithets are not jet handled!
		//   - infraGenericEpithet	"Cicerbita"	
        //   - infraSpecificEpithet	null	

		logger.debug(taxonNameBase.getTitleCache()); // DELETE
		List<Object> taggedName = taxonNameBase.getCacheStrategy().getTaggedName(taxonNameBase);
		for (Object token : taggedName){
			TaggedText tag = new TaggedText();
			if (String.class.isInstance(token)){
				tag.setText((String)token);
				tag.setType(TagEnum.name);
			}
			else if (Rank.class.isInstance(token)){
				Rank r = (Rank)token;
				tag.setText(r.getAbbreviation());
				tag.setType(TagEnum.rank);
			}
			else if (Date.class.isInstance(token)){
				Date d = (Date)token;
				tag.setText(String.valueOf(d.getYear()));
				tag.setType(TagEnum.year);
			}
			else if (Team.class.isInstance(token)){
				Team t = (Team)token;
				tag.setText(String.valueOf(t.getTitleCache()));
				tag.setType(TagEnum.authors);
			}

			if (tag!=null){
				tags.add(tag);
			}
		}
		return tags;
	}

	

}
