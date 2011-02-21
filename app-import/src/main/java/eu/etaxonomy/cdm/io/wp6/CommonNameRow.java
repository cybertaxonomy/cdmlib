/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.wp6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class CommonNameRow {
	private static final Logger logger = Logger.getLogger(CommonNameRow.class);


	private String species;
	private String reference;
	private String area;
	private String nameUsedInSource;
	
	private Map<String, List<String>> commonNames = new HashMap<String, List<String>>();
	
	
	public CommonNameRow() {
		this.species = "";
		this.reference =  "";
		this.area =  "";
		commonNames = new HashMap<String, List<String>>();
	}
	
// **************************** GETTER / SETTER *********************************/	
	
	public void setCommonNames(String commonNamesString){
		commonNamesString = makeNameUsedInSource(commonNamesString);
		String[] split = commonNamesString.split(";");
		for (String oneLanguage : split){
			oneLanguage = oneLanguage.trim();
			String reLangPattern = "\\((\\*|[a-z]{2,3}|.{2,},\\sno\\sISO-Code)\\)$";
			String pattern = ".+" + reLangPattern;
			if (! oneLanguage.matches(pattern)){
				logger.warn("Common name does not match: "  + oneLanguage);
			}else{
				Pattern langPattern = Pattern.compile(reLangPattern);
				Matcher matcher = langPattern.matcher(oneLanguage);
				if (matcher.find()){
					String lang = matcher.group().substring(1);
					lang = lang.substring(0, lang.length()-1);
					String names = "";
					try {
						names = oneLanguage.substring(0,matcher.start()-1);
					} catch (Exception e) {
						e.printStackTrace();
					}
					String[] splitNames = names.split(",");
					List<String> nameList = new ArrayList<String>();
					for (String singleName : splitNames){
						if (StringUtils.isNotBlank(singleName)){
							nameList.add(singleName.trim());
						}
					}
					if (!nameList.isEmpty()){
						this.commonNames.put(lang, nameList);
					}
				}else{
					logger.warn("Common name does not match: "  + oneLanguage);
				}
			}
		}
	}
	
	private String makeNameUsedInSource(String commonNamesString) {
		String[] split = commonNamesString.split(":");
		if (split.length > 1){
			logger.info("NameUsedInSource: " + split[0]);
			this.nameUsedInSource = split[0].trim();
			if (split.length > 2){
				logger.warn("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX:" + commonNamesString);
			}
			return split[1].trim();
		}else{
			return split[0].trim();
		}
	}

	public Map<String, List<String>> getCommonNames() {
		return commonNames;
	}
	
	public void parseSpecies(String species){
		INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
		BotanicalName name = (BotanicalName)parser.parseFullName(species, NomenclaturalCode.ICBN, null);
		if (name.isProtectedTitleCache()){
			logger.warn("Name could not be parsed: " + species);
		}
		this.species = species;
	}

	public String getSpecies() {
		return species;
	}
	
	public void setSpecies(String species) {
		this.species = species;
	}

	public void setReference(String reference) {
		reference = reference.replace(".", "");
		if (! reference.matches("\\d{7}")){
			logger.warn("Unexpected reference");
		}
		this.reference = reference.substring(0,6);
	}

	public String getReference() {
		return reference;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	public String getArea() {
		return area;
	}

	public void setNameUsedInSource(String nameUsedInSource) {
		this.nameUsedInSource = nameUsedInSource;
	}

	public String getNameUsedInSource() {
		return nameUsedInSource;
	}


	
}
