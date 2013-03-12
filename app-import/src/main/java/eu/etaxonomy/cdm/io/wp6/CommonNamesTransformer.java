// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.wp6;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class CommonNamesTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CommonNamesTransformer.class);
	

	//named area
	public static final UUID uuidCentralAfrica =  UUID.fromString("45f4e8a4-5145-4d87-af39-122495c08fe3");
	public static final UUID uuidCentralAndEastAfrica =  UUID.fromString("3ea55ef5-7428-4c3f-a1a8-4b6c306add0f");
	public static final UUID uuidCentralAsiaAndMiddleEast =  UUID.fromString("99c2c1e8-dfda-47a4-9835-312336e8ef0e");
	public static final UUID uuidCentralEastAndSouthernAfrica =  UUID.fromString("16972365-a1f7-49ac-89f5-700f3f186263");
	public static final UUID uuidEastAfrica =  UUID.fromString("3b548c0f-8d5d-4f03-b1f2-0c1cd1aa28d2");
	public static final UUID uuidEastAndSouthernAfrica =  UUID.fromString("4b785977-0878-4919-8b80-3b57e64eaa22");
	public static final UUID uuidMascareneIslands =  UUID.fromString("317ad421-5d3e-4e80-b048-15f703de7cee");
	public static final UUID uuidMiddleEast =  UUID.fromString("6575628a-95fa-46ba-aeab-14dbc1300e35");
	public static final UUID uuidNorthEastAfrica =  UUID.fromString("d27cd317-2bd5-4129-8762-40d313d21bed");
	public static final UUID uuidSeychellesMadagascar =  UUID.fromString("36874d33-033e-4b91-9200-96c00e6ef981");
	public static final UUID uuidSeychellesMadagascarMauritius = UUID.fromString("c0d14467-1c8a-4c12-bb1f-8745daa14ab2");
	public static final UUID uuidSomaliaEthiopia =  UUID.fromString("3b4ac59c-b9d6-4cf3-97b0-dff4df7ab839");
	public static final UUID uuidSouthAfrica =  UUID.fromString("12288119-7cea-4cb2-a460-92d9eb8500fb");
	public static final UUID uuidSouthAsia =  UUID.fromString("7127dfb4-1e4b-48b0-9876-204c54a74814");
	public static final UUID uuidSouthEastAsia =  UUID.fromString("ba137511-7137-4692-816d-bf2823c52219");
	public static final UUID uuidWestAfrica =  UUID.fromString("49add437-63c8-4d12-ac32-00988ccde0e7");
	public static final UUID uuidWestAndCentralAfrica =  UUID.fromString("29027ab6-6d21-413b-8d3d-8548d40d5801");
	public static final UUID uuidWestCentralEastAfrica =  UUID.fromString("a94d2b9d-c58e-41df-9587-e3e01714b000");
	public static final UUID uuidWesternAndEasternAfrica =  UUID.fromString("19ffdae5-622c-459d-af29-c19914e0e3da");

	
   // Languages
	public static final UUID uuidAge = UUID.fromString("7915d555-72b3-4862-b8de-d6037dc581f0");
	public static final UUID uuidArb = UUID.fromString("5e4ff341-a5fd-4ae7-9228-c60cb5c668fa");
	public static final UUID uuidBnc =  UUID.fromString("f9deb1c6-da95-46a3-a9eb-046fae544850");
	public static final UUID uuidCrs =  UUID.fromString("78677bcd-3fc1-4de3-a898-40464a221f82");
	public static final UUID uuidDsh =  UUID.fromString("60520bb9-7e29-4b58-b84a-d70f9c14650c");
	public static final UUID uuidEnq =  UUID.fromString("a24d01b4-77ad-4931-b4a0-49bc6706ac55");
	public static final UUID uuidFvr =  UUID.fromString("31bda027-cdf6-4fb9-bb11-effbe63bb4e8");
	public static final UUID uuidHad =  UUID.fromString("edff1b18-4d3f-4f46-af66-90d8e85759f1");
	public static final UUID uuidHke =  UUID.fromString("39170103-fc88-481e-80fe-c8702dd645b6");
	public static final UUID uuidHre =  UUID.fromString("19467133-c821-4e7e-9d39-4dd372c7442b");
	public static final UUID uuidIbg =  UUID.fromString("4843b8d4-0b9e-4641-90b1-05087536c034");
	public static final UUID uuidIvv =  UUID.fromString("65d65904-e8fd-4cd3-ae3e-86e98cdbe71f");
	public static final UUID uuidJms =  UUID.fromString("1639d843-17f5-497a-ab30-0da1180a9583");
	public static final UUID uuidKue =  UUID.fromString("39131dc6-3ebb-46c2-925f-27e53da6c529");
	public static final UUID uuidKxc =  UUID.fromString("dd66af4c-a0be-4b2c-8bcc-d834550e70f9");
	public static final UUID uuidLaj =  UUID.fromString("5699cede-1dcd-4fb5-8c93-60e4488156a5");
	public static final UUID uuidMed =  UUID.fromString("387cdf74-8c8b-49b8-b94c-0edebc07f381");
	public static final UUID uuidMtv =  UUID.fromString("80f60d66-17e7-4bcb-ad8c-629a8025d018");
	public static final UUID uuidNnb =  UUID.fromString("a1ad6c52-ff29-48be-b71a-f6cc518621be");
	public static final UUID uuidNyj =  UUID.fromString("0c850ba6-f256-4b5d-aad3-11a18e4a5fbb");
	public static final UUID uuidNyu =  UUID.fromString("81bd3221-3bf7-4456-8ee9-b9f8de6ce7c2");
	public static final UUID uuidOku =  UUID.fromString("4038e14f-1521-4467-869f-1869d96a9102");
	public static final UUID uuidRng =  UUID.fromString("3276075d-c1db-40fa-8ef1-849c94dc5ed3");
	public static final UUID uuidScl =  UUID.fromString("52da3217-6a41-409e-86fa-780466be67b5");
	public static final UUID uuidSeh =  UUID.fromString("019a1d37-5a11-4550-8423-133f54f64ab2");
	public static final UUID uuidTeo =  UUID.fromString("7c07979a-5a90-47e0-89b1-5ae0c21a8ce4");
	public static final UUID uuidTra =  UUID.fromString("819d7ab3-7f7c-4af5-bc31-d05e6a01bc33");
	public static final UUID uuidTsz =  UUID.fromString("62fce168-47e2-40ad-baf9-b1e28a53cb06");
	public static final UUID uuidTya =  UUID.fromString("e134c069-aea4-4b08-b545-fce120e7e37b");
	public static final UUID uuidVmw =  UUID.fromString("5e4a5ec1-2d6c-4afb-a453-32e7522c69bf");
	public static final UUID uuidWgi =  UUID.fromString("6f3aafcf-a7ca-4edb-b7f3-3eade674f62e");
	public static final UUID uuidWni =  UUID.fromString("e5843f50-86f9-42b1-a542-4201f73333c9");
	// no iso-code
	public static final UUID uuidBabua =  UUID.fromString("a69e9d9f-ffaf-4815-b730-df9ceff5522b");
	public static final UUID uuidGur =  UUID.fromString("9ec8ba64-8911-4b92-89eb-e0092b4113e4");
	public static final UUID uuidKilur =  UUID.fromString("89f1286f-e869-4f10-a43a-d86ef729833e");
	public static final UUID uuidNgwaka =  UUID.fromString("67dfc889-0084-4932-9b21-ed54bbfe341f");
	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getMarkerTypeByKey(java.lang.String)
	 */
	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("Australia")){return WaterbodyOrCountry.AUSTRALIACOMMONWEALTHOF();
		}else if (key.equalsIgnoreCase("Azores")){return TdwgArea.getAreaByTdwgAbbreviation("AZO-OO");
		}else if (key.equalsIgnoreCase("Canary Islands")){return TdwgArea.getAreaByTdwgAbbreviation("CNY-OO");
		}else if (key.equalsIgnoreCase("North America")){return TdwgArea.getAreaByTdwgAbbreviation("7");
		}else if (key.equalsIgnoreCase("Tansania")){return TdwgArea.getAreaByTdwgAbbreviation("TAN-OO");
		
		}else{
			return null;
		}
	}

	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("Central Africa")){return uuidCentralAfrica;
		}else if (key.equalsIgnoreCase("Central and East Africa")){return uuidCentralAndEastAfrica;
		}else if (key.equalsIgnoreCase("Central Asia and Middle East")){return uuidCentralAsiaAndMiddleEast;
		}else if (key.equalsIgnoreCase("Central, East and Southern Africa")){return uuidCentralEastAndSouthernAfrica;
		}else if (key.equalsIgnoreCase("East and Southern Africa")){return uuidEastAndSouthernAfrica;
		}else if (key.equalsIgnoreCase("East Africa")){return uuidEastAfrica;
		}else if (key.equalsIgnoreCase("Mascarene Islands")){return uuidMascareneIslands;
		}else if (key.equalsIgnoreCase("Middle East")){return uuidMiddleEast;
		}else if (key.equalsIgnoreCase("North East Africa")){return uuidNorthEastAfrica;
		}else if (key.equalsIgnoreCase("Seychelles and Madagascar")){return uuidSeychellesMadagascar;
		}else if (key.equalsIgnoreCase("Seychelles, Madagascar and Mauritius")){return uuidSeychellesMadagascarMauritius;
		}else if (key.equalsIgnoreCase("Somalia and Ethiopia")){return uuidSomaliaEthiopia;
		}else if (key.equalsIgnoreCase("South Africa")){return uuidSouthAfrica;
		}else if (key.equalsIgnoreCase("Southeast Asia")){return uuidSouthEastAsia;
		}else if (key.equalsIgnoreCase("West Africa")){return uuidWestAfrica;
		}else if (key.equalsIgnoreCase("West and Central Africa")){return uuidWestAndCentralAfrica;
		}else if (key.equalsIgnoreCase("West, Central and East Africa")){return uuidWestCentralEastAfrica;
		}else if (key.equalsIgnoreCase("South Asia")){return uuidSouthAsia;
		}else if (key.equalsIgnoreCase("Western Africa")){return uuidWestAfrica;
		}else if (key.equalsIgnoreCase("Western and Eastern Africa")){return uuidWesternAndEasternAfrica;
		}else if (key.equalsIgnoreCase("Western Central Africa")){return uuidWestAndCentralAfrica;
		
		
		}else{
			return null;
		}

	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getLanguageByKey(java.lang.String)
	 */
	@Override
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equals("age")){return Language.NewInstance(uuidAge, "Angal", key);
		}else if (key.equals("arb")){return Language.NewInstance(uuidArb, "Standard Arabic", key);
		}else if (key.equals("bnc")){return Language.NewInstance(uuidBnc, "Bontok", key);
		}else if (key.equals("crs")){return Language.NewInstance(uuidCrs, "Seselwa Creole French", key);
		}else if (key.equals("deu")){return Language.GERMAN();
		}else if (key.equals("dsh")){return Language.NewInstance(uuidDsh, "Daasanach", key);
		}else if (key.equals("ell")){return Language.GREEK_MODERN();
		}else if (key.equals("enq")){return Language.NewInstance(uuidEnq, "Enga", key);
		}else if (key.equals("fra")){return Language.FRENCH();
		}else if (key.equals("fvr")){return Language.NewInstance(uuidFvr, "Fur", key);
		}else if (key.equals("had")){return Language.NewInstance(uuidHad, "Hatam", key);
		}else if (key.equals("hke")){return Language.NewInstance(uuidHke, "Hunde", key);
		}else if (key.equals("hre")){return Language.NewInstance(uuidHre, "Hre", key);
		}else if (key.equals("ibg")){return Language.NewInstance(uuidIbg, "Ibanag", key);
		}else if (key.equals("ivv")){return Language.NewInstance(uuidIvv, "Ivatan", key);
		}else if (key.equals("jms")){return Language.NewInstance(uuidJms, "Mashi (Nigeria)", key);
		}else if (key.equals("kue")){return Language.NewInstance(uuidKue, "Kuman", key);
		}else if (key.equals("kxc")){return Language.NewInstance(uuidKxc, "Konso", key);
		}else if (key.equals("laj")){return Language.NewInstance(uuidLaj, "Lango (Uganda)", key);
		}else if (key.equals("med")){return Language.NewInstance(uuidMed, "Melpa", key);
		}else if (key.equals("mri")){return Language.MAORI();
		}else if (key.equals("mtv")){return Language.NewInstance(uuidMtv, "Asaro'o", key);
		}else if (key.equals("nld")){return Language.DUTCH_FLEMISH();
		}else if (key.equals("nnb")){return Language.NewInstance(uuidNnb, "Nande", key);
		}else if (key.equals("nyj")){return Language.NewInstance(uuidNyj, "Nyanga", key);
		}else if (key.equals("nyu")){return Language.NewInstance(uuidNyu, "Nyungwe", key);
		}else if (key.equals("oku")){return Language.NewInstance(uuidOku, "Oku", key);
		}else if (key.equals("rng")){return Language.NewInstance(uuidRng, "Ronga", key);
		}else if (key.equals("scl")){return Language.NewInstance(uuidScl, "Shina", key);
		}else if (key.equals("seh")){return Language.NewInstance(uuidSeh, "Sena", key);
		}else if (key.equals("teo")){return Language.NewInstance(uuidTeo, "Teso", key);
		}else if (key.equals("tra")){return Language.NewInstance(uuidTra, "Tirahi", key);
		}else if (key.equals("tsz")){return Language.NewInstance(uuidTsz, "Purepecha", key);
		}else if (key.equals("tya")){return Language.NewInstance(uuidTya, "Tauya", key);
		}else if (key.equals("vmw")){return Language.NewInstance(uuidVmw, "Makhuwa", key);
		}else if (key.equals("wgi")){return Language.NewInstance(uuidWgi, "Wahgi", key);
		}else if (key.equals("wni")){return Language.NewInstance(uuidWni, "Ndzwani Comorian", key);
		// no iso-code
		}else if (key.equals("Babua")){return Language.NewInstance(uuidBabua, "Babua", null);
		}else if (key.equals("Kilur")){return Language.NewInstance(uuidKilur, "Kilur", null);
		}else if (key.equals("Ngwaka")){return Language.NewInstance(uuidNgwaka, "Ngwaka", null);
		}else if (key.equals("Gur")){return Language.NewInstance(uuidGur, "Gur", null);
		}else{
			return null;
		}
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getLanguageUuid(java.lang.String)
	 */
	@Override
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("Babua")){return uuidBabua;
		}else if (key.equalsIgnoreCase("Kilur")){return uuidKilur;
		}else if (key.equalsIgnoreCase("Ngwaka")){return uuidNgwaka;
		}else if (key.equalsIgnoreCase("Gur")){return uuidGur;
		
		}else{
			return null;
		}

	}

	
	
}
