/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public class ZooNameDefaultCacheStrategy
            extends NonViralNameDefaultCacheStrategy<IZoologicalName> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZooNameDefaultCacheStrategy.class);
	private static final long serialVersionUID = 6640953957903705560L;

	final static UUID uuid = UUID.fromString("950c4236-8156-4675-b866-785df33bc4d9");

	protected String AuthorYearSeperator = ", ";

	@Override
	public UUID getUuid(){
		return uuid;
	}


	/**
	 * Factory method
	 * @return
	 */
	public static ZooNameDefaultCacheStrategy NewInstance(){
		return new ZooNameDefaultCacheStrategy();
	}

	/**
	 * Constructor
	 */
	protected ZooNameDefaultCacheStrategy(){
		super();
	}



	/**
	 * @return Strings that separates the author part and the year part
	 * @return
	 */
	public String getAuthorYearSeperator() {
		return AuthorYearSeperator;
	}


	public void setAuthorYearSeperator(String authorYearSeperator) {
		AuthorYearSeperator = authorYearSeperator;
	}

	@Override
	protected String getNonCacheAuthorshipCache(IZoologicalName nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result = "";
		INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorship();
		INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorship();
		INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorship();
		INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorship();
		Integer publicationYear = nonViralName.getPublicationYear();
		Integer originalPublicationYear = nonViralName.getOriginalPublicationYear();

		String basionymPart = "";
		String authorPart = "";
		//basionym
		if (basionymAuthor != null || exBasionymAuthor != null || originalPublicationYear != null ){
			String authorAndEx = getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor);
			String originalPublicationYearString = originalPublicationYear == null ? null : String.valueOf(originalPublicationYear);
			String authorAndExAndYear = CdmUtils.concat(AuthorYearSeperator, authorAndEx, originalPublicationYearString );
			basionymPart = BasionymStart + authorAndExAndYear +BasionymEnd;
		}
		if (combinationAuthor != null || exCombinationAuthor != null){
			String authorAndEx = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
			String publicationYearString = publicationYear == null ? null : String.valueOf(publicationYear);
			authorPart = CdmUtils.concat(AuthorYearSeperator, authorAndEx, publicationYearString);
		}
		result = CdmUtils.concat(BasionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
		if (result == null){
			result = "";
		}
		return result;
	}


	@Override
	protected List<TaggedText> getInfraSpeciesTaggedNameCache(IZoologicalName nonViralName){
		boolean includeMarker = ! (nonViralName.isAutonym());
		return getInfraSpeciesTaggedNameCache(nonViralName, includeMarker);
	}

}
