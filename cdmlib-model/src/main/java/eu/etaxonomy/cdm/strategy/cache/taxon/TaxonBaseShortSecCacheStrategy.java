package eu.etaxonomy.cdm.strategy.cache.taxon;


	/**
	* Copyright (C) 2015 EDIT
	* European Distributed Institute of Taxonomy
	* http://www.e-taxonomy.eu
	*
	* The contents of this file are subject to the Mozilla Public License Version 1.1
	* See LICENSE.TXT at the top of this package for the full license terms.
	*/ 

	

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

	public class TaxonBaseShortSecCacheStrategy<T extends TaxonBase> extends StrategyBase implements
			IIdentifiableEntityCacheStrategy<T> {

		final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");
		
		@Override
		protected UUID getUuid() {
			return uuid;
		}

		public String getTitleCache(T taxonBase) {
			String title;
			if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
				String namePart = getNamePart(taxonBase);
				
				title = namePart + " sec. ";
				title += getSecundumPart(taxonBase);
			}else{
				title = taxonBase.toString();
			}
			if (taxonBase.isDoubtful()){
				title = "?" + title;
			}
			return title;
		}

		/**
		 * @param taxonBase
		 * @param title
		 * @return
		 */
		private String getSecundumPart(T taxonBase) {
			String result = "???";
			Reference<?> sec = taxonBase.getSec();
			sec = HibernateProxyHelper.deproxy(sec, Reference.class);
			if (sec != null){
				if (sec.isProtectedTitleCache()){
					return sec.getTitleCache();
				}
				if (sec.getAuthorship() != null){
					
					if (sec.getAuthorship().isInstanceOf(Team.class)){
						Team authorTeam = HibernateProxyHelper.deproxy(sec.getAuthorship(), Team.class);
						if (authorTeam.getTeamMembers().size() > 2){
							if (authorTeam.getTeamMembers().get(0).getLastname() != null){
						        result = authorTeam.getTeamMembers().get(0).getLastname() + " & al.";
						    } else {
						        result = authorTeam.getTeamMembers().get(0).getTitleCache();
						        result = result + " & al.";
						    }
						} else if (authorTeam.getTeamMembers().size() == 2){
							String firstAuthor;
							if (authorTeam.getTeamMembers().get(0).getLastname() != null){
								firstAuthor = authorTeam.getTeamMembers().get(0).getLastname();
							}else{
								firstAuthor = authorTeam.getTeamMembers().get(0).getTitleCache();
							}
							String secondAuthor;
							if (authorTeam.getTeamMembers().get(1).getLastname() != null){
								secondAuthor = authorTeam.getTeamMembers().get(1).getLastname();
							}else{
								secondAuthor = authorTeam.getTeamMembers().get(1).getTitleCache();
							}
							result = firstAuthor + " & " + secondAuthor;

						} else{
							if (authorTeam.getTeamMembers().get(0).getLastname() != null){
						        result = authorTeam.getTeamMembers().get(0).getLastname();
						    } else {
						        result = authorTeam.getTeamMembers().get(0).getTitleCache();
						    }
						}
						
					} else {
						Person author = HibernateProxyHelper.deproxy(sec.getAuthorship(), Person.class);
						if (author.getLastname() != null){
							result = author.getLastname();
						} else{
							result = author.getTitleCache();
						}
					}
					if (result != null){
						result = result.replaceAll("[A-Z]\\.", "");
					}
					if (sec.getYear() != null && result != null){
						result = result.concat(" (" + sec.getYear()+")");
					}
				}else{
					result = taxonBase.getSec().getTitleCache();
				}
			}
			return result;
		}

		/**
		 * @param name
		 */
		private String getNamePart(TaxonBase<?> taxonBase) {
			TaxonNameBase<?,?> nameBase = taxonBase.getName();
			String result = nameBase.getTitleCache();
			//use name cache instead of title cache if required
			if (taxonBase.isUseNameCache() && nameBase.isInstanceOf(NonViralName.class)){
				NonViralName<?> nvn = HibernateProxyHelper.deproxy(nameBase, NonViralName.class);
				result = nvn.getNameCache();
			}
			if (StringUtils.isNotBlank(taxonBase.getAppendedPhrase())){
				result = result.trim() + " " +  taxonBase.getAppendedPhrase().trim(); 
			}
			return result;
		}

	}


