/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Continent")
@XmlRootElement(name = "Continent")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Continent extends NamedArea {
	private static final long serialVersionUID = 4650684072484353151L;
	private static final Logger logger = Logger.getLogger(Continent.class);

	protected static Map<UUID, Continent> termMap = null;		

	private static final UUID uuidEurope = UUID.fromString("3b69f979-408c-4080-b573-0ad78a315610");
	private static final UUID uuidAfrica = UUID.fromString("c204c529-d8d2-458f-b939-96f0ebd2cbe8");
	private static final UUID uuidAsiaTemperate = UUID.fromString("7f4f4f89-3b4c-475d-929f-144109bd8457");
	private static final UUID uuidAsiaTropical = UUID.fromString("f8039275-d2c0-4753-a1ab-0336642a1499");
	private static final UUID uuidNAmerica = UUID.fromString("81d8aca3-ddd7-4537-9f2b-5327c95b6e28");
	private static final UUID uuidSAmerica = UUID.fromString("12b861c9-c922-498c-8b1a-62afc26d19e3");
	private static final UUID uuidAustralasia = UUID.fromString("a2afdb9a-04a0-434c-9e75-d07dbeb86526");
	private static final UUID uuidPacific = UUID.fromString("c57adcff-5213-45f0-a5f0-97a9f5c0f1fe");
	private static final UUID uuidAntarctica = UUID.fromString("71fd9ab7-9b07-4eb6-8e54-c519aff56728");

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(){
		logger.debug("NewInstance of Continent");
		return new Continent();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(String term, String label, String labelAbbrev){
		return new Continent(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public Continent() {
	}
	
	private Continent(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	protected static Continent getTermByUuid(UUID uuid){
		if (termMap == null){
			return null;
		}else{
			return (Continent)termMap.get(uuid);
		}
	}


	public static final Continent EUROPE(){
		return getTermByUuid(uuidEurope);
	}

	public static final Continent AFRICA(){
		return getTermByUuid(uuidAfrica);
	}

	public static final Continent ASIA_TEMPERATE(){
		return getTermByUuid(uuidAsiaTemperate);
	}

	public static final Continent ASIA_TROPICAL(){
		return getTermByUuid(uuidAsiaTropical);
	}

	public static final Continent NORTH_AMERICA(){
		return getTermByUuid(uuidNAmerica);
	}

	public static final Continent ANTARCTICA(){
		return getTermByUuid(uuidAntarctica);
	}

	public static final Continent SOUTH_AMERICA(){
		return getTermByUuid(uuidSAmerica);
	}

	public static final Continent AUSTRALASIA(){
		return getTermByUuid(uuidAustralasia);
	}
	
	public static final Continent PACIFIC(){
		return getTermByUuid(uuidPacific);
	}
	@Override
	public NamedArea readCsvLine(Class<NamedArea> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		try {
			Continent newInstance = Continent.class.newInstance();
		    return DefinedTermBase.readCsvLine(newInstance, csvLine, Language.CSV_LANGUAGE());
		} catch (Exception e) {
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
		}
		
	    return null;
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
		termMap = new HashMap<UUID, Continent>();
		for (NamedArea term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (Continent)term);  //TODO casting
		}
	}

}