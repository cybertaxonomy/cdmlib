/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * The class for the five nomenclature codes (ICNB, ICBN, ICNCP, ICZN and ICVCN)
 * ruling {@link TaxonName taxon names}.
 * <P>
 * The standard set of nomenclature code instances will be automatically created
 * as the project starts. But this class allows to extend this standard set by
 * creating new instances of additional nomenclature codes if unlikely needed.
 * <P>
 * This class corresponds to: <ul>
 * <li> NomenclaturalCodeTerm according to the TDWG ontology
 * <li> NomenclaturalCodesEnum according to the TCS
 * <li> CodeOfNomenclatureEnum according to the ABCD schema
 * </ul>
 *
 * @author a.mueller
 * @created 19.05.2008
 */

@XmlType(name = "NomenclaturalCode")
@XmlEnum
public enum NomenclaturalCode implements IEnumTerm<NomenclaturalCode> {
	//0
	/**
	 * International Code of Nomenclature of Bacteria
	*/
	@XmlEnumValue("ICNB")
	ICNB(UUID.fromString("ff4b0979-7abf-4b40-95c0-8b8b1e8a4d5e"), "ICNB","BacterialName"),

	//1
	/**
	 * International Code of Nomenclature for algae, fungi, and plants
	 * Former International Code of Botanical Nomenclature
	 */
	@XmlEnumValue("ICNAFP")
	ICNAFP(UUID.fromString("540fc02a-8a8e-4813-89d2-581dad4dd482"), "ICNAFP","BotanicalName"),

	//2
	/**
	 * International Code of Cultivated Plants
	 */
	@XmlEnumValue("ICNCP")
	ICNCP(UUID.fromString("65a432b5-92b1-4c9a-8090-2a185e423d2e"),"ICNCP","CultivarPlantName"),

	//3
	/**
	 * International Code of Zoological Nomenclature
	 */
	@XmlEnumValue("ICZN")
	ICZN(UUID.fromString("b584c2f8-dbe5-4454-acad-2b45e63ec11b"), "ICZN","ZoologicalName"),

	//4
	/**
	 * International Code for Virus Classification and Nomenclature
	 */
	@XmlEnumValue("ICVCN")
	ICVCN(UUID.fromString("e9d6d6b4-ccb7-4f28-b828-0b1501f8c75a"), "ICVCN","ViralName"),
//
//	//Any
//	@XmlEnumValue("Any")
//    Any(UUID.fromString("348f2a2f-366f-4c8c-bb15-c90b937886ca"), "Any taxon name","TaxonName"),


	//NonViral
    @XmlEnumValue("NonViral")
    NonViral(UUID.fromString("04f88497-a66a-41b1-9b98-0dd22df6307f"), "NonViral","TaxonName"),

	//Fungi
    @XmlEnumValue("Fungus")
	Fungi(UUID.fromString("c6bb280d-2468-4738-bb29-973f74412100"), "Fungus", "BotanicalName"),

//	//Plant
//    @XmlEnumValue("Plant")
//	Plant(UUID.fromString("9669c889-25c5-48ab-9f8e-fd47a6218f83"), "Plant", "BotanicalName"),
//
//	//Algae
//    @XmlEnumValue("Algae")
//	Algae(UUID.fromString("abc09250-ea76-449b-b292-90acd61f8659"), "Algae", "BotanicalName"),
    ;

	private static final Logger logger = Logger.getLogger(NomenclaturalCode.class);

	@Deprecated
	private String dtype;

	private NomenclaturalCode(UUID uuid, String titleCache, @Deprecated String dtype){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, titleCache, titleCache, null);
		this.dtype = dtype;
	}

    public String getTitleCache() {
        return getMessage();
    }


	@Override
	public String toString() {
		return this.name();
	}

    public boolean isNonViral() {
        return this != ICVCN;
    }
    public boolean isZoological() {
        return this == ICZN;
    }
    public boolean isBotanical() {
        return this == NomenclaturalCode.ICNAFP || this == ICNCP || this == Fungi;
    }
    public boolean isCultivar() {
        return this == NomenclaturalCode.ICNCP;
    }
    public boolean isBacterial() {
        return this == NomenclaturalCode.ICNB;
     }
     public boolean isViral() {
         return this == NomenclaturalCode.ICVCN;
     }
     public boolean isFungus() {
         return this == NomenclaturalCode.Fungi;
     }


	public static NomenclaturalCode fromString(String string){
		for(NomenclaturalCode code : NomenclaturalCode.values()){
			if(code.name().equalsIgnoreCase(string)) {
				return code;
			}
		}
		if ("ICBN".equals(string)){ //former name of the ICNAFP
			return ICNAFP;
		}
		return null;
	}

	/**
	 * @deprecated not relevant anymore, used only by NomenclaturalCodeUpdater
	 * which is used for 4.0->4.1 schema update.
	 */
	@Deprecated
    public static NomenclaturalCode fromDtype(String string){
        for(NomenclaturalCode code : NomenclaturalCode.values()){
            if(code.dtype.equalsIgnoreCase(string)) {
                return code;
            }
        }
        return null;
    }


	/**
	 * Creates a new particular {@link TaxonName taxon name} (botanical, zoological,
	 * cultivar plant, bacterial or viral name) instance depending on <i>this</i>
	 * nomenclature code only containing the given {@link Rank rank}.
	 *
	 * @param	rank	the rank of the new taxon name instance
	 * @see 			TaxonName#NewBotanicalInstance(Rank)
	 * @see 			TaxonName#NewZoologicalInstance(Rank)
	 * @see 			TaxonName#NewCultivarInstance(Rank)
	 * @see 			TaxonName#NewBacterialInstance(Rank)
	 * @see 			TaxonName#NewViralInstance(Rank)
	 */
	public TaxonName getNewTaxonNameInstance(Rank rank){
		TaxonName result;

		switch (this){
		case ICNAFP:
			result = TaxonNameFactory.NewBotanicalInstance(rank);
			break;
		case ICZN:
			result = TaxonNameFactory.NewZoologicalInstance(rank);
			break;
		case ICNCP:
			result = TaxonNameFactory.NewCultivarInstance(rank);
			break;
		case ICNB:
			result = TaxonNameFactory.NewBacterialInstance(rank);
			break;
		case ICVCN:
			result = TaxonNameFactory.NewViralInstance(rank);
			break;
		case NonViral:
            result = TaxonNameFactory.NewNonViralInstance(rank);
            break;
		case Fungi:
            result = TaxonNameFactory.NewFungusInstance(rank);
            break;
        default:
			logger.warn("Unknown nomenclatural code: " + this.getUuid());
			result = null;
		}
		return result;
	}

	/**
	 * Returns the recommended value for the accepted taxon status according to
	 * http://code.google.com/p/darwincore/wiki/Taxon#taxonomicStatus
	 */
	public String acceptedTaxonStatusLabel(){
		switch(this){
		case ICNAFP:
			return "accepted";
		case ICZN:
			return "valid";
		default:
			logger.warn("AcceptedTaxonStatusLabel not yet implemented for " + name());
			return "accepted";
		}
	}

	/**
	 * Returns the recommended value for the accepted taxon status according to
	 * http://code.google.com/p/darwincore/wiki/Taxon#taxonomicStatus
	 */
	public String synonymStatusLabel(){
		switch(this){
		case ICNAFP:
			return "synonym";
		case ICZN:
			return "invalid";
		default:
		    logger.warn("SynonymStatusLabel not yet implemented for " + name());
            return "synonym";
		}
	}


// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<NomenclaturalCode> delegateVoc;
	private IEnumTerm<NomenclaturalCode> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(NomenclaturalCode.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	@Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}


	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public NomenclaturalCode getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<NomenclaturalCode> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(NomenclaturalCode ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<NomenclaturalCode> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


	public static NomenclaturalCode getByKey(String key){return delegateVoc.getByKey(key);}
    public static NomenclaturalCode getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
