// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

/**
 * @author a.kohlbecker
 * @date 29.03.2011
 *
 * @deprecated use List<TaggedText> methods in according cache strategies instead. Will be removed with version 3.1.
 */
@Deprecated
public class TaggedTextGenerator {

	/**
		 * FIXME ugly method - this functionality mainly be performed by ChachStrategies ?
		 * 
		 * @param taxonNameBase
		 * @return
		 * @deprecated use List<TaggedText> methods in according cache strategies instead. Will be removed with next major version.
		 */
		@Deprecated
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
			//FIXME infrageneric epithets are not yet handled!
			//   - infraGenericEpithet	"Cicerbita"	
	        //   - infraSpecificEpithet	null	
	
			List<Object> taggedName = taxonNameBase.getCacheStrategy().getTaggedNameDeprecated(taxonNameBase);
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
