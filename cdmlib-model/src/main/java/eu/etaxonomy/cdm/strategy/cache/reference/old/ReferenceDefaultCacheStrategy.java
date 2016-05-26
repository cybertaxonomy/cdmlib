/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference.old;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;
/**
 * @author a.mueller
 * @created 08-Aug-2008 22:06:45
 */
public class ReferenceDefaultCacheStrategy extends StrategyBase implements IReferenceCacheStrategy {
	private static final long serialVersionUID = 4350124746874497766L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("763fe4a0-c79f-4f14-9693-631680225ec3");


	protected String beforeYear = ". ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";

	private String blank = " ";


// ****************** FACTORY *******************************/

	public static ReferenceDefaultCacheStrategy NewInstance(){
		return new ReferenceDefaultCacheStrategy();
	}

// ***************** CONSTRUCTOR ********************************/

	/**
	 * Constructor
	 */
	private ReferenceDefaultCacheStrategy(){
		super();
	}





	@Override
	public String getFullAbbrevTitleString(Reference reference) {
		return getTitleCache(reference, true);
	}

	@Override
	public String getTitleCache(Reference reference) {
		return getTitleCache(reference, false);
	}

	private String getTitleCache(Reference ref, boolean isAbbrev) {
		String result = "";
		if (ref == null){
			return null;
		}
		String titel = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
		//titelAbbrev
		if (isNotBlank(titel)){
			result = titel + blank;
		}
		//delete .
		while (result.endsWith(".")){
			result = result.substring(0, result.length()-1);
		}

		result = addYear(result, ref);
		TeamOrPersonBase<?> team = ref.getAuthorship();
		if (team != null){
			String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(), team.getNomenclaturalTitle(), isAbbrev, true);
			if (isNotBlank(author)){
				result = author + afterAuthor + result;
			}
		}
		return result;
	}

	protected String addYear(String string, Reference ref){
		String result;
		if (string == null){
			return null;
		}
		String year = CdmUtils.Nz(ref.getYear());
		if ("".equals(year)){
			result = string + afterYear;
		}else{
			result = string + beforeYear + year + afterYear;
		}
		return result;
	}

	@Override
	public String getCitation(Reference referenceBase) {
		StringBuilder stringBuilder = new StringBuilder();

		TeamOrPersonBase<?> team = referenceBase.getAuthorship();
		if (team != null &&  ! (team.getTitleCache() == null) && ! team.getTitleCache().trim().equals("")){
			//String author = CdmUtils.Nz(team == null? "" : team.getTitleCache());
			stringBuilder.append(team.getTitleCache() + afterAuthor);
		}

		String year = CdmUtils.Nz(referenceBase.getYear());
		if (!"".equals(year)){
			stringBuilder.append(beforeYear + year);
		}

		return stringBuilder.toString();
	}



	@Override
	protected UUID getUuid() {
		return UUID.fromString("919dbf70-33e7-11de-b418-0800200c9a66");
	}


	/**
	 *
	 * @param referenceTitleCache
	 * @param authorTitleCache
	 * @return
	 */
	public static String putAuthorToEndOfString(String referenceTitleCache, String authorTitleCache) {
		if(authorTitleCache != null){
			referenceTitleCache = referenceTitleCache.replace(authorTitleCache + ", ", "");
			referenceTitleCache += " - " + authorTitleCache;
		}
		return referenceTitleCache;
	}

}
