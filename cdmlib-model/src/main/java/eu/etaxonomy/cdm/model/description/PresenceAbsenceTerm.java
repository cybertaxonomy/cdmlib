/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * This class represents terms describing the {@link AbsenceTerm absence}
 * (like "extinct") or the {@link PresenceTerm presence} (like "cultivated") of a
 * {@link Taxon taxon} in a {@link NamedArea named area}. These terms are only
 * used for {@link Distribution distributions}.

 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 * @author a.mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PresenceAbsenceTerm")
@XmlRootElement(name = "PresenceAbsenceTerm")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class PresenceAbsenceTerm extends OrderedTermBase<PresenceAbsenceTerm> {
	private static final long serialVersionUID = 1036807546935584396L;
	private static final Logger logger = Logger.getLogger(PresenceAbsenceTerm.class);


	//presence base
	private static final UUID uuidP=UUID.fromString("cef81d25-501c-48d8-bbea-542ec50de2c2");
	private static final UUID uuidPD=UUID.fromString("75a60279-a4c2-4f53-bc57-466028a4b3db");

	//presence
	private static final UUID uuidN=UUID.fromString("ddeac4f2-d8fa-43b8-ad7e-ca13abdd32c7");
	private static final UUID uuidNQ=UUID.fromString("925662c1-bb10-459a-8c53-da5a738ac770");
	private static final UUID uuidND=UUID.fromString("310373bf-7df4-4d02-8cb3-bcc7448805fc");
	private static final UUID uuidC=UUID.fromString("9eb99fe6-59e2-4445-8e6a-478365bd0fa9");
	private static final UUID uuidI=UUID.fromString("643cf9d1-a5f1-4622-9837-82ef961e880b");
	private static final UUID uuidIQ=UUID.fromString("83eb0aa0-1a45-495a-a3ca-bf6958b74366");
	private static final UUID uuidID=UUID.fromString("0c54761e-4887-4788-9dfa-7190c88746e3");
	private static final UUID uuidIP=UUID.fromString("da159544-b0dd-4599-a9c9-640826af8c17");
	private static final UUID uuidIA=UUID.fromString("42946bd6-9c22-45ad-a910-7427e8f60bfd");
	private static final UUID uuidIN=UUID.fromString("e191e89a-a751-4b0c-b883-7f1de70915c9");
	private static final UUID uuidIC=UUID.fromString("fac8c347-8262-44a1-b0a4-db4de451c021");
	private static final UUID uuidE=UUID.fromString("c3ee7048-15b7-4be1-b687-9ce9c1a669d6");
	private static final UUID uuidNA=UUID.fromString("4e04990a-66fe-4fdf-856c-f40772fbcf0a");
	private static final UUID uuidIV=UUID.fromString("dc536e3d-a753-4bbe-a386-dd8aff35c234");

	//absence
	private static final UUID uuidAbsence=UUID.fromString("59709861-f7d9-41f9-bb21-92559cedd598");
	private static final UUID uuidNF=UUID.fromString("61cee840-801e-41d8-bead-015ad866c2f1");
	private static final UUID uuidIF=UUID.fromString("aeec2947-2700-4623-8e32-9e3a430569d1");
	private static final UUID uuidCF=UUID.fromString("9d4d3431-177a-4abe-8e4b-1558573169d6");
	private static final UUID uuidNE=UUID.fromString("5c397f7b-59ef-4c11-a33c-45691ceda91b");
	private static final UUID uuidIE=UUID.fromString("b74dc30b-ee93-496d-8c00-4d00abae1ec7");


	protected static Map<UUID, PresenceAbsenceTerm> termMap = null;

    private String defaultColor = "000000";

    @XmlAttribute(name = "absenceTerm")
    private boolean absenceTerm = false;

	//********* METHODS **************************************/
	/**
	 * Creates a new empty presence term.
	 *
	 * @see #NewInstance(String, String, String)
	 */
	public static PresenceAbsenceTerm NewInstance(){
		return new PresenceAbsenceTerm();
	}

	/**
	 * Creates a new presence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #NewInstance()
	 */
	public static PresenceAbsenceTerm NewPresenceInstance(String term, String label, String labelAbbrev){
		return new PresenceAbsenceTerm(term, label, labelAbbrev, false);
	}

	/**
	 * Creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #NewInstance()
	 */
	public static PresenceAbsenceTerm NewAbsenceInstance(String term, String label, String labelAbbrev){
		return new PresenceAbsenceTerm(term, label, labelAbbrev, true);
	}


//********************************** Constructor *******************************************************************/

  	//for hibernate use only
  	@Deprecated
  	protected PresenceAbsenceTerm() {
    	super(TermType.PresenceAbsenceTerm);
    }

    /**
     * Class constructor: creates a new presence or absence term with a description
     * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new presence or absence term to be created
     * @param	label  		 the string identifying the new presence or absence term to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new presence or absence term to be created
     * @param   isAbsenceTerm  boolean value indicating if this term represents an absence or not
     * @see 				 #PresenceAbsenceTermBase()
     */
    protected PresenceAbsenceTerm(String term, String label, String labelAbbrev, boolean isAbsenceTerm) {
        super(TermType.PresenceAbsenceTerm, term, label, labelAbbrev);
        this.setAbsenceTerm(isAbsenceTerm);
    }


  //******************************* STATIC METHODS *****************************************

  	protected static PresenceAbsenceTerm getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(PresenceAbsenceTerm.class, uuid);
        } else {
  			return termMap.get(uuid);
  		}
  	}

   	//TODO read from label(abbrevs) like in TDWGArea
  	public static PresenceAbsenceTerm getPresenceAbsenceTermByAbbreviation(String abbrev) {
  		if (abbrev == null) { throw new NullPointerException("abbrev is 'null' in getPresenceTermByAbbreviation");
  		} else if (abbrev.equalsIgnoreCase("c"))  { return PresenceAbsenceTerm.CULTIVATED();
  		} else if (abbrev.equalsIgnoreCase("e"))  { return PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
  		} else if (abbrev.equalsIgnoreCase("i"))  { return PresenceAbsenceTerm.INTRODUCED();
  		} else if (abbrev.equalsIgnoreCase("ia")) { return PresenceAbsenceTerm.INTRODUCED_ADVENTITIOUS();
  		} else if (abbrev.equalsIgnoreCase("ic")) { return PresenceAbsenceTerm.INTRODUCED_CULTIVATED();
  		} else if (abbrev.equalsIgnoreCase("id")) { return PresenceAbsenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
  		} else if (abbrev.equalsIgnoreCase("in")) { return PresenceAbsenceTerm.INTRODUCED_NATURALIZED();
  		} else if (abbrev.equalsIgnoreCase("ip")) { return PresenceAbsenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
  		} else if (abbrev.equalsIgnoreCase("iq")) { return PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
  		} else if (abbrev.equalsIgnoreCase("n"))  { return PresenceAbsenceTerm.NATIVE();
  		} else if (abbrev.equalsIgnoreCase("nd")) { return PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE();
  		} else if (abbrev.equalsIgnoreCase("nq")) { return PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
  		} else if (abbrev.equalsIgnoreCase("p"))  { return PresenceAbsenceTerm.PRESENT();
  		} else if (abbrev.equalsIgnoreCase("na"))  { return PresenceAbsenceTerm.NATURALISED();
  		} else if (abbrev.equalsIgnoreCase("iv"))  { return PresenceAbsenceTerm.INVASIVE();
  		//absence
		} else if (abbrev.equalsIgnoreCase("cf")) { return PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("if")) { return PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("nf")) { return PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("ne")) { return PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE();
		} else if (abbrev.equalsIgnoreCase("ie")) { return PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
		} else {
			logger.warn("Unknown presence or absence status term abbreviation: " + abbrev);
			return null;
		}
  	}


	public static final PresenceAbsenceTerm PRESENT(){
		return getTermByUuid(uuidP);
	}
	public static final PresenceAbsenceTerm PRESENT_DOUBTFULLY(){
		return getTermByUuid(uuidPD);
	}
	public static final PresenceAbsenceTerm CULTIVATED(){
		return getTermByUuid(uuidC);
	}
	public static final PresenceAbsenceTerm ENDEMIC_FOR_THE_RELEVANT_AREA(){
		return getTermByUuid(uuidE);
	}
		public static final PresenceAbsenceTerm INTRODUCED(){
		return getTermByUuid(uuidI);
	}
		public static final PresenceAbsenceTerm INTRODUCED_ADVENTITIOUS(){
		return getTermByUuid(uuidIA);
	}

	public static final PresenceAbsenceTerm INTRODUCED_CULTIVATED(){
		return getTermByUuid(uuidIC);
	}

	public static final PresenceAbsenceTerm INTRODUCED_DOUBTFULLY_INTRODUCED(){
		return getTermByUuid(uuidID);
	}

	public static final PresenceAbsenceTerm INTRODUCED_NATURALIZED(){
		return getTermByUuid(uuidIN);
	}

	public static final PresenceAbsenceTerm INTRODUCED_PRESENCE_QUESTIONABLE(){
		return getTermByUuid(uuidIQ);
	}

	public static final PresenceAbsenceTerm INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(){
		return getTermByUuid(uuidIP);
	}

	public static final PresenceAbsenceTerm NATIVE(){
		return getTermByUuid(uuidN);
	}

	public static final PresenceAbsenceTerm NATIVE_DOUBTFULLY_NATIVE(){
		return getTermByUuid(uuidND);
	}

	public static final PresenceAbsenceTerm NATIVE_PRESENCE_QUESTIONABLE(){
		return getTermByUuid(uuidNQ);
	}

	public static final PresenceAbsenceTerm INVASIVE(){
		return getTermByUuid(uuidIV);
	}

	public static final PresenceAbsenceTerm NATURALISED(){
		return getTermByUuid(uuidNA);
	}

	public static final PresenceAbsenceTerm ABSENT(){
		return getTermByUuid(uuidAbsence);
	}

	public static final PresenceAbsenceTerm NATIVE_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidNF);
	}

	public static final PresenceAbsenceTerm CULTIVATED_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidCF);
	}

	public static final PresenceAbsenceTerm INTRODUCED_REPORTED_IN_ERROR(){
		return getTermByUuid(uuidIF);
	}

	public static final PresenceAbsenceTerm NATIVE_FORMERLY_NATIVE(){
		return getTermByUuid(uuidNE);
	}

	public static final PresenceAbsenceTerm INTRODUCED_FORMERLY_INTRODUCED(){
		return getTermByUuid(uuidIE);
	}


//******************************** METHODS ****************************/

	@Override
	public void resetTerms(){
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<PresenceAbsenceTerm> termVocabulary) {
		termMap = new HashMap<UUID, PresenceAbsenceTerm>();
		for (PresenceAbsenceTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

    @Override
    public PresenceAbsenceTerm readCsvLine(Class<PresenceAbsenceTerm> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId){
    	PresenceAbsenceTerm newInstance = super.readCsvLine(termClass, csvLine, terms, abbrevAsId);
        String abbreviatedLabel = csvLine.get(4);
//		String uuid = (String)csvLine.get(0);
//		map.put(abbreviatedLabel, UUID.fromString(uuid));
        String color = csvLine.get(5);
        try {
			newInstance.setDefaultColor(color);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
        boolean isAbsence = csvLine.get(6).equals("1") ? true : false;
        newInstance.setAbsenceTerm(isAbsence);

        newInstance.getRepresentation(Language.DEFAULT()).setAbbreviatedLabel(abbreviatedLabel);
        return newInstance;
    }

    /**
     * @return the defaultColor
     */
    public String getDefaultColor() {
        return defaultColor;
    }

    /**
     * @param defaultColor the defaultColor to set
     * @throws ParseException
     */
    //TODO make format checking a hibernate validation rule
    public void setDefaultColor(String defaultColor) throws ParseException  {
    	String regEx = "[0-9a-fA-F]{6}";
    	if (StringUtils.isEmpty(defaultColor)){
    		this.defaultColor = null;
    	}else if (StringUtils.isEmpty(defaultColor) || defaultColor.matches(regEx)){
        		this.defaultColor = defaultColor;
    	}else{
    		throw new java.text.ParseException("Default color is not of correct format. Required is 'FFFFFF'", -1);
    	}
    }

    /**
     * Compares this OrderedTermBase with the specified OrderedTermBase for
     * order. Returns a -1, 0, or +1 if the orderId of this object is greater
     * than, equal to, or less than the specified object.
     * <p>
     * <b>Note:</b> The compare logic of this method is the <b>inverse logic</b>
     * of the the one implemented in
     * {@link java.lang.Comparable#compareTo(java.lang.Object)}
     *
     * @param orderedTerm
     *            the OrderedTermBase to be compared
     * @param skipVocabularyCheck
     *            whether to skip checking if both terms to compare are in the
     *            same vocabulary
     * @throws NullPointerException
     *             if the specified object is null
     */
    @Override
    protected int performCompareTo(PresenceAbsenceTerm presenceAbsenceTerm, boolean skipVocabularyCheck) {

    	PresenceAbsenceTerm presenceAbsenceTermLocal = CdmBase.deproxy(presenceAbsenceTerm, PresenceAbsenceTerm.class);
        if(! skipVocabularyCheck){
            if (this.vocabulary == null || presenceAbsenceTermLocal.vocabulary == null){
                throw new IllegalStateException("An ordered term (" + this.toString() + " or " +
                		presenceAbsenceTermLocal.toString() + ") of class " + this.getClass() + " or " +
                		presenceAbsenceTermLocal.getClass() + " does not belong to a vocabulary and therefore "
                		+ "can not be compared");
            }
            if (presenceAbsenceTermLocal.isAbsenceTerm() != this.isAbsenceTerm() ){
              if (presenceAbsenceTermLocal.isAbsenceTerm()){
            		return 1;
            	}else{
            		return -1;
            	}

            }
        }

        int orderThat;
        int orderThis;
        try {
            orderThat = presenceAbsenceTermLocal.orderIndex;//OLD: this.getVocabulary().getTerms().indexOf(orderedTerm);
            orderThis = orderIndex; //OLD: this.getVocabulary().getTerms().indexOf(this);
        } catch (RuntimeException e) {
            throw e;
        }
        if (orderThis > orderThat){
            return -1;
        }else if (orderThis < orderThat){
            return 1;
        }else {
            return 0;
        }
    }


	public boolean isAbsenceTerm() {
		return absenceTerm;
	}

	public void setAbsenceTerm(boolean isAbsenceTerm) {
		this.absenceTerm = isAbsenceTerm;
	}


}