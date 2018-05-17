/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.reference.ISection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);

	//source Refs
	public static final UUID uuidSourceRefEuroMed = UUID.fromString("0603a84a-f024-4454-ab92-9e2ac0139126");

	//ranks
	public static UUID uuidRankCollSpecies = UUID.fromString("e14630ee-9446-4bb4-a7b7-4c3881bc5d94");
	public static UUID uuidRankProles = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
	public static UUID uuidRankRace = UUID.fromString("196dee39-cfd8-4460-8bf0-88b83da27f62");
	public static UUID uuidRankSublusus = UUID.fromString("1fafa596-a8e7-4e62-a378-3cc8cb3627ca");

	//named areas
	public static UUID euroMedUuid = UUID.fromString("9fe09988-58c0-4c06-8474-f660a0c50014");

	public static UUID uuidEasternEuropeanRussia = UUID.fromString("3f013375-0e0a-40c3-8a14-84c0535fab40");
	public static UUID uuidSerbiaMontenegro = UUID.fromString("8926dbe6-863e-47a9-98a0-7dc9ed2c57f7");
	public static UUID uuidSerbia = UUID.fromString("4ffed197-3d7e-4cd9-8984-e64b8dee9512");
	public static UUID uuidLebanonSyria = UUID.fromString("0c45f250-99da-4b19-aa89-c3e56cfdf103");
	public static UUID uuidUssr = UUID.fromString("a512e00a-45f3-4be5-82fa-bba8d675696f");
	public static UUID uuidSicilyMalta = UUID.fromString("424d81ee-d272-4ae8-9600-0a334049cd72");

	public static UUID uuidDesertas = UUID.fromString("36f5e93e-34e8-45b5-a401-f0e0faad21cf");
	public static UUID uuidMadeira = UUID.fromString("086e27ee-78ff-4236-aca9-9850850cd355");
	public static UUID uuidPortoSanto = UUID.fromString("1f9ab6a0-a402-4dfe-8c5b-b1844eb4d8e5");
	//azores
	public static UUID uuidFlores = UUID.fromString("ef0067c2-8bbb-4e37-8462-97b03f51ba43");
	public static UUID uuidCorvo = UUID.fromString("b1e6117c-2be1-43a3-9233-227dd90bdee9");
	public static UUID uuidFaial = UUID.fromString("14f6dcdb-6524-4700-b80c-66424952ef2b");
	public static UUID uuidGraciosa = UUID.fromString("05f93a7b-d813-4833-864f-eedbba747133");
	public static UUID uuidSaoJorge = UUID.fromString("578e0ecd-b5e3-4c87-8ecc-0fc4c7e217d9");
	public static UUID uuidSaoMiguel = UUID.fromString("0e2f6ad8-7afb-4f01-b134-4f71991e877a");
	public static UUID uuidPico = UUID.fromString("98cc566f-4110-43d5-830f-68436a009f49");
	public static UUID uuidSantaMaria = UUID.fromString("bd5e5d4a-22b7-41aa-8e58-1d1d73a9954d");
	public static UUID uuidTerceira = UUID.fromString("6fc257c0-a131-41f0-b6c3-51ef9c4fa962");
	//Canyry islands
	public static UUID uuidGranCanaria = UUID.fromString("a0240c35-0e05-4157-8321-67ba8e31fdb9");
	public static UUID uuidFuerteventura = UUID.fromString("549ce2c1-6d49-4bf3-b75d-cf3f4b5a1398");
	public static UUID uuidGomera = UUID.fromString("895fab09-7478-4210-b42a-423d23c6f85e");
	public static UUID uuidHierro = UUID.fromString("d137b6a5-31bc-418c-9403-f042017dc04b");
	public static UUID uuidLanzaroteWithGraciosa = UUID.fromString("c9b08dc1-f301-4d9d-b447-b8744602c776");
	public static UUID uuidLaPalma = UUID.fromString("fdb3f2b2-f154-4f04-9f31-240a47a0e780");
	public static UUID uuidTenerife = UUID.fromString("73658c7e-a568-465e-bd84-4554fc93ca56");
	//Baleares
	public static UUID uuidIbizaWithFormentera = UUID.fromString("1cda2a33-3469-49d5-8e77-cb5451110519");
	public static UUID uuidMallorca = UUID.fromString("a10cba04-b4b5-4a4b-b69a-fcd4b3916fec");
	public static UUID uuidMenorca = UUID.fromString("9f6ede48-27f8-4270-bf4e-c97eaa86aab7");

	//russia
	public static UUID uuidRussiaNorthern = UUID.fromString("c23bc1c9-a775-4426-b883-07d4d7d47eed");
	public static UUID uuidRussiaBaltic = UUID.fromString("579dad44-9439-4b19-8716-ab90d8f27944");
	public static UUID uuidRussiaCentral = UUID.fromString("8bbc8c6a-2ef2-4024-ad51-66fe34c70092");
	public static UUID uuidRussiaSouthWest = UUID.fromString("daa5c207-5567-4690-8742-5e4d153b6a64");
	public static UUID uuidRussiaSouthEast = UUID.fromString("e8516598-b529-489e-9ee8-63bbbd295c1b");
	public static UUID uuidEastAegeanIslands = UUID.fromString("1c429593-c493-46e6-971a-0d70be690da8");
	public static UUID uuidTurkishEastAegeanIslands = UUID.fromString("ba35dba3-ac70-41ae-81c2-2070943f44f2");
	public static UUID uuidBalticStates = UUID.fromString("bf9d64f6-3183-4fa5-8e90-73090e7a2282");
	public static final UUID uuidTurkey = UUID.fromString("d344ee2c-14c8-438d-b03d-11538edb1268");
	public static final UUID uuidCaucasia = UUID.fromString("ebfd3fd1-3859-4e5e-95c7-f66010599d7e");



	//E+M areas
	public static UUID uuidVocEuroMedAreas = UUID.fromString("625a4962-c211-4597-816e-5804083efe26");
	public static UUID uuidEM = UUID.fromString("111BDF38-7A32-440A-9808-8AF1C9E54B51");
	public static UUID uuidEUR = UUID.fromString("80B3CEEE-2F78-45CE-B4F4-E473F5ED8343");
	public static UUID uuid14 = UUID.fromString("4FF83A35-97DE-4C39-BEDE-27EE9ECEFB45");
	public static UUID uuid20 = UUID.fromString("19D93AD1-59E7-49ED-B513-D7D493EDC4DE");
	public static UUID uuid21 = UUID.fromString("1B98DAF0-A709-4871-9A8B-CFDD09F41763");
	public static UUID uuid33 = UUID.fromString("6B719260-1E48-4D85-8BC3-320852E1B331");
	public static UUID uuidDa = UUID.fromString("867871C9-9931-47D0-AEF7-468B9519EBB2");
	public static UUID uuidFe = UUID.fromString("C4EF26AF-11E0-4888-9FC3-5E56F4422BB4");
	public static UUID uuidFa = UUID.fromString("70426E7E-2EAA-4987-95FB-5CB6E56980F3");
	public static UUID uuidBr = UUID.fromString("A22329C4-0B81-4E00-BCC3-2F44A6CA56D0");
	public static UUID uuidIs = UUID.fromString("3D2950E9-C2FD-462E-9697-DCB7241D514E");
	public static UUID uuidHb = UUID.fromString("65E405E7-B3D4-44B6-BE67-4881582EA274");
	public static UUID uuidNo = UUID.fromString("2D3F1181-6838-4034-96CB-F254A558572A");
	public static UUID uuidSb = UUID.fromString("BC847AF8-30C3-48B2-A881-2B2DEAC0A137");
	public static UUID uuidSu = UUID.fromString("F064AB9F-EDDB-47D8-8E54-B9E8AD19111D");
	public static UUID uuidAu = UUID.fromString("37C77C23-9673-4ABC-87C2-B29E9FA87FA8");
	public static UUID uuidBe = UUID.fromString("4840373B-F949-4630-B655-9B37F845CFDA");
	public static UUID uuidCz = UUID.fromString("02E67A70-C192-4A01-A311-99F8C42F9D51");
	public static UUID uuidGe = UUID.fromString("F617DDEA-51C9-4EF1-B7F4-ED22F871631D");
	public static UUID uuidHu = UUID.fromString("AD7EDDB1-C089-436F-88BA-BC9115BBD6E0");
	public static UUID uuidHo = UUID.fromString("00B8FE6D-D04B-4CAC-ADCE-32B44ABB385A");
	public static UUID uuidPo = UUID.fromString("F23864DF-DFD6-4ABC-B7D0-FD97408AE0A9");
	public static UUID uuidHe = UUID.fromString("7C28D878-F16D-4043-A94E-750BEF910064");
	public static UUID uuidBl = UUID.fromString("A4326211-3219-4655-83AD-599D635DB638");
	public static UUID uuidCo = UUID.fromString("3EE3CB3E-23DE-491F-94DF-35DB7A1D3AC0");
	public static UUID uuidGa = UUID.fromString("28591C04-60E3-432A-9D05-CBC945462D2E");
	public static UUID uuidLu = UUID.fromString("205512B0-AA22-4041-B92B-336FEB79FA4F");
	public static UUID uuidSa = UUID.fromString("11368A92-2362-4029-A26A-E35CEC0798C1");
	public static UUID uuidHs = UUID.fromString("6AD92C01-1442-4F2F-BD58-F73075B26C27");
	public static UUID uuidAl = UUID.fromString("635B63DA-0AAF-446C-843E-BEB52E2A1B90");
	public static UUID uuidBu = UUID.fromString("75F3CF66-B272-464E-9B1E-35B8E511936D");
	public static UUID uuidGr = UUID.fromString("34DF8B89-7DFA-4265-9A3E-D540DB72AA77");
	public static UUID uuidIt = UUID.fromString("06B8F41D-B9A8-4B55-8AAB-DE90EB3D0A9C");
	public static UUID uuidCr = UUID.fromString("20A8A822-1C87-43F0-A5EF-2AF1CC5DC0FC");
	public static UUID uuidRm = UUID.fromString("EAB29D40-E3B2-4920-BB1F-8757DFBC9E86");
	public static UUID uuidSi_S = UUID.fromString("DD861671-930F-4C9E-92D8-EADDC28EB6B7");
	public static UUID uuidTu_E = UUID.fromString("2B732609-55DB-4F25-BC73-618FFF515ADC");
	public static UUID uuidJu = UUID.fromString("BD5E6833-7E0E-41A8-83D6-149A31F2CE05");
	public static UUID uuidUk_K = UUID.fromString("40B7219D-E38C-44EB-9877-E311A2734022");
	public static UUID uuidUk = UUID.fromString("7A8DF5D0-3D18-4386-891E-96E11BDB6FEB");
	public static UUID uuidAg = UUID.fromString("548E583C-DBE7-463F-A01B-2A966F2B32D2");
	public static UUID uuidEg = UUID.fromString("9EDDDE2E-95B7-4443-89A8-C30D031E16A5");
	public static UUID uuidLi = UUID.fromString("2F506902-2A56-40C8-84CB-B436C84ED258");
	public static UUID uuidMa = UUID.fromString("D639A7AC-F873-414E-8869-73D10B9CF842");
	public static UUID uuidTn = UUID.fromString("AF4D86D4-2CA0-48A2-BA15-0D74454D1EAD");
	public static UUID uuidAz = UUID.fromString("CEC2EBD3-DFD0-4CE6-827F-BFF8FBAF5283");
	public static UUID uuidSg = UUID.fromString("BC4A0307-B81A-4233-B8BD-EFB9CEDFD530");
	public static UUID uuidAb = UUID.fromString("D3744C2D-2777-4E85-98BF-04D2FD589EBF");
	public static UUID uuidAr = UUID.fromString("535FED1E-3EC9-4563-AF55-E753AEFCFBFE");
	public static UUID uuidAb_A = UUID.fromString("0F4C98BF-AF7B-4CDA-B62C-AD6A1909BFA0");
	public static UUID uuidGg = UUID.fromString("DA1CCDA8-5867-4098-A709-100A66E2150A");
	public static UUID uuidAb_N = UUID.fromString("AA75B0CA-49C9-4F8E-8CC2-2A343EB2FFF4");
	public static UUID uuidCy = UUID.fromString("36EFDF69-09C0-4160-A502-9EEFBC22A984");
	public static UUID uuidAE_G = UUID.fromString("76F5F3F6-9C3E-47F5-8E85-55360C50273C");
	public static UUID uuidLe = UUID.fromString("639F5D97-EC9E-4EE2-ADFC-DFF73F7CC970");
	public static UUID uuidSn = UUID.fromString("5D8E0B00-96CE-4ACC-AF02-62A1B9866144");
	public static UUID uuidTu_A = UUID.fromString("F7B59D79-15C2-47C9-91B0-DEC1F388CB62");
	public static UUID uuidTu = UUID.fromString("DB98809B-EF22-413B-B1EA-A79C4E1C4903");
	public static UUID uuidAu_A = UUID.fromString("AE65867C-00F6-406C-A315-B3E4CC9A93D2");
	public static UUID uuidAu_L = UUID.fromString("78146B6E-E71A-46DA-8DBC-244CC648BBE7");
	public static UUID uuidAz_C = UUID.fromString("D35B8259-CC76-4FB0-AFC0-6A23D657EE3E");
	public static UUID uuidAz_F = UUID.fromString("0F8F470D-CA2A-4130-842A-0A0C6912A123");
	public static UUID uuidAz_G = UUID.fromString("CFF9FC6D-E3B2-45B1-87A9-6FEFB029A12F");
	public static UUID uuidAz_P = UUID.fromString("C8000982-19E8-492E-912D-59EB370E52C0");
	public static UUID uuidAz_S = UUID.fromString("BD852931-47B3-466C-A422-4F312B913CA2");
	public static UUID uuidAz_J = UUID.fromString("4A5CED9F-F078-44B3-94BF-F3EE79315236");
	public static UUID uuidAz_M = UUID.fromString("B8C76F37-6483-474F-85AB-96399219DE57");
	public static UUID uuidAz_T = UUID.fromString("7A52595F-DA5E-440A-B2BD-F63999CE979E");
	public static UUID uuidBe_B = UUID.fromString("9E263401-ACEC-4E2E-AA89-4AAF56AE7180");
	public static UUID uuidBe_L = UUID.fromString("AF71559C-7765-493C-8C9A-1248DFF28789");
	public static UUID uuidBl_I = UUID.fromString("121B07A0-8031-4F22-B9B7-C334FC3204CE");
	public static UUID uuidBl_M = UUID.fromString("E8E8EFDC-E9EB-4B26-9711-F986265AD114");
	public static UUID uuidBl_N = UUID.fromString("D96DD8A7-4BD8-4ADF-8CDB-05873686DB1A");
	public static UUID uuidBH = UUID.fromString("6A109EF5-3AC1-4C59-8599-08F944ABA499");
	public static UUID uuidBy = UUID.fromString("5D972AF7-6AE2-44D1-840B-EDB1DBE8B7AC");
	public static UUID uuidCa = UUID.fromString("CFA26682-B0AB-4FEB-9191-6AA098638382");
	public static UUID uuidCa_F = UUID.fromString("0D8ED2C4-0313-464C-A5B4-EC0A52E45ADF");
	public static UUID uuidCa_G = UUID.fromString("865D1319-B157-4D1A-BBF0-F56B7EB2ED96");
	public static UUID uuidCa_C = UUID.fromString("486FCBED-84C4-4673-8724-9A8A4A6613AD");
	public static UUID uuidCa_H = UUID.fromString("0B57C693-DB09-4D7F-9FA9-0DBBEFF3B3B6");
	public static UUID uuidCa_L = UUID.fromString("A88D6DF4-7924-4BEC-BFA6-071BDA82A4B3");
	public static UUID uuidCa_P = UUID.fromString("BBF04D1A-1FC0-4A7B-BF0B-7D7BF69D8392");
	public static UUID uuidCa_T = UUID.fromString("BBAD170B-285D-4BEA-8C3F-C43894FB75FF");
	public static UUID uuidCs = UUID.fromString("2ADA45F0-C7CC-4026-A8B1-B816971F0753");
	public static UUID uuidCt = UUID.fromString("09B5728A-4775-4530-B362-9B1EF4A9E8C3");
	public static UUID uuidEs = UUID.fromString("1011D427-401B-47EE-A42C-4C1698957D55");
	public static UUID uuidGa_C = UUID.fromString("3653A4DC-1ADE-4237-A62C-4F0AC11E576F");
	public static UUID uuidGa_F = UUID.fromString("860F92B6-5CDA-456B-964C-6162D1D08161");
	public static UUID uuidGg_A = UUID.fromString("5FAD859B-7929-4D5F-B92C-95E3E0469BB2");
	public static UUID uuidGg_D = UUID.fromString("6091C975-B946-4EF3-A18F-2E148EAE6A06");
	public static UUID uuidGg_G = UUID.fromString("048799B0-D7B9-44C6-B2D1-5CA2A49FA175");
	public static UUID uuidHs_A = UUID.fromString("EAED6C21-42E5-496D-B43E-C121F96FA672");
	public static UUID uuidHs_G = UUID.fromString("35350D75-6952-48BD-B265-C005BC1B2909");
	public static UUID uuidHs_S = UUID.fromString("264649F7-192D-4AE5-9840-81FC782F59F0");
	public static UUID uuidIr = UUID.fromString("A3B35528-5FFD-43B1-B605-711807C1EC9F");
	public static UUID uuidIt_I = UUID.fromString("E3BC327E-0B42-4439-811B-595BC55A8FF8");
	public static UUID uuidIt_S = UUID.fromString("57F35807-8CFA-4698-BB04-AAD3549C12EB");
	public static UUID uuidJo = UUID.fromString("567A537D-F2FA-43EF-A20A-AEC76723E269");
	public static UUID uuidKz = UUID.fromString("F8ABE715-D859-4B6F-B8F1-DB1A847DEAC4");
	public static UUID uuidLa = UUID.fromString("8E338882-2631-4AD6-BC53-799C698C807D");
	public static UUID uuidLt = UUID.fromString("ECF200B6-B1DF-414F-B215-EDAC503B1A65");
	public static UUID uuidMa_E = UUID.fromString("80980607-FBD8-46E3-BE56-05D997F2D331");
	public static UUID uuidMa_S = UUID.fromString("B743AE57-DC0B-4CCC-B4D9-7BF51E579E8E");
	public static UUID uuidMk = UUID.fromString("0CE83170-2FA7-4C72-81F7-7FFF17343E48");
	public static UUID uuidMd = UUID.fromString("2CFB90B1-0BA0-4578-AD11-AAD5AAB62899");
	public static UUID uuidMd_D = UUID.fromString("DBE1F3CA-4C7C-4062-AC1E-ADE0C17DCA52");
	public static UUID uuidMd_M = UUID.fromString("4658DADD-60FE-46D8-94E3-C6A6C4646105");
	public static UUID uuidMd_P = UUID.fromString("2C41EB67-A330-4214-B452-6E6741262CE5");
	public static UUID uuidSi_M = UUID.fromString("2920D738-54A1-49CE-AF3A-9CB742064587");
	public static UUID uuidMo = UUID.fromString("64F98B98-1050-42C6-B2C7-F72DA642E4D5");
	public static UUID uuidRf = UUID.fromString("4B6BDFEE-2BCB-4638-99CF-0F9612FA4787");
	public static UUID uuidRf_C = UUID.fromString("1FFAF0B5-6311-4BF6-977F-8940622C4986");
	public static UUID uuidRf_E = UUID.fromString("4280A0F6-298E-4B3C-8CE8-56A1208183F7");
	public static UUID uuidRf_K = UUID.fromString("D9559D9A-1798-4496-8671-DE667CC30EC0");
	public static UUID uuidRf_CS = UUID.fromString("6E886ACC-22C9-4C63-BEA5-A51AD84AF3D1");
	public static UUID uuidRf_N = UUID.fromString("B0D8F65E-B5A4-4C0B-A5EF-54CC9F378CC6");
	public static UUID uuidRf_NW = UUID.fromString("06CC9983-B444-4322-A03E-9A7A2AF1D4AD");
	public static UUID uuidRf_A = UUID.fromString("1786C13D-D26D-49E3-BA60-E3F3E7852713");
	public static UUID uuidRf_S = UUID.fromString("6AAB10D5-05E9-470A-8AF5-B6F8D48A71EC");
	public static UUID uuidSk = UUID.fromString("09267309-E771-4BD7-A67F-B6B4321546E0");
	public static UUID uuidSl = UUID.fromString("BC242D21-98CA-402C-BDB7-3ED347C7BDFD");
	public static UUID uuidSy = UUID.fromString("A5974D4B-F878-422C-A7A8-A1D8268109EF");
	public static UUID uuidUk_U = UUID.fromString("4A6C5155-154E-4B0D-AC73-550B51CCE374");
	public static UUID uuidSM = UUID.fromString("AC360FC1-30F4-444A-92EB-B55BF98B8E97");
	public static UUID uuidYu_K = UUID.fromString("F3F05E88-99AF-48FA-92EF-1A169BCC7ACF");
	public static UUID uuidCg = UUID.fromString("38A4FD15-40D6-43F4-9685-F87465FDBD3F");
	public static UUID uuidSr = UUID.fromString("468CFEA2-008F-40CF-B2C8-ADF5C09C0FA4");
	public static UUID uuidIJ = UUID.fromString("9351B972-0C0B-4A97-87B1-4CC11E67D21E");
	public static UUID uuidLS = UUID.fromString("F7BFDAE0-3DD9-4FC4-9B0B-0BF203B94031");
	public static UUID uuidRs = UUID.fromString("C4A898CE-0F32-44FE-A8A3-278E11A4BA53");
	public static UUID uuidSi = UUID.fromString("DB1FACF2-58A5-483E-9B2A-EB4290CA1B71");
	public static UUID uuidAz_L = UUID.fromString("F5AEF252-C4F8-4ECC-9B6E-4821DB7ADECF");
	public static UUID uuidHb_E = UUID.fromString("1D400E37-F39A-4CAE-8885-CF485B900CC5");
	public static UUID uuidHb_N = UUID.fromString("B1D514DA-B9E6-4F0E-ACE4-18FB061FC132");
	public static UUID uuidGa_M = UUID.fromString("E73942F0-292A-4F6A-8B2F-15AFE8634319");
	public static UUID uuidMa_M = UUID.fromString("D9F7DFFC-0E76-4790-9AAE-B7AEB5AD76ED");
	public static UUID uuidRs_N = UUID.fromString("44F262E3-5091-4D28-8081-440D3978FB0B");
	public static UUID uuidRs_B = UUID.fromString("A575D608-DD53-4C01-B2AF-5067D0711F64");
	public static UUID uuidRs_C = UUID.fromString("DA4E9CC3-B1CC-403A-81FF-BCC5D9FADBD1");
	public static UUID uuidRs_W = UUID.fromString("EFABC8FD-0B3C-475B-B532-E1CA0BA0BDBB");
	public static UUID uuidRs_E = UUID.fromString("7E0F8FA3-5DB9-48F0-9FA8-87FCAB3EAA53");
	public static UUID uuidAE = UUID.fromString("C8FCD4E0-E1A2-4A7D-8EE2-6F397F5C546C");
	public static UUID uuidAE_T = UUID.fromString("AF83B475-BB35-4594-8380-EA64B4313091");
	public static UUID uuidRs_K = UUID.fromString("2188E3A5-0446-47C8-B11B-B4B2B9A71C75");
	public static UUID uuidCc = UUID.fromString("05B0DD06-30F8-477D-BF4C-30D9DEF56320");
	public static UUID uuidBt = UUID.fromString("EE13FB74-F3AC-46B1-9F23-6A25AC504446");
	public static UUID uuidTcs = UUID.fromString("904C3980-B98D-422E-A195-95F4F41FC734");


	//salvador
	public static UUID uuidSalvadorAreas = UUID.fromString("8ef90ca3-77d7-4adc-8bbc-1eb354e61b65");



	//language areas
//	public static final UUID uuidUkraineAndCrimea = UUID.fromString("99d4d1c2-09f6-416e-86a3-bdde5cae52af");
	public static final UUID uuidAzerbaijanNakhichevan = UUID.fromString("232fbef0-9f4a-4cab-8ac1-e14c717e9de6");

	//Marker Types
	public static final UUID uuidMisappliedCommonName = UUID.fromString("25f5cfc3-16ab-4aba-a008-0db0f2cf7f9d");
	public static final UUID uuidEurArea = UUID.fromString("71dd0368-835c-4b53-889a-2bf316e10297");
	public static final UUID uuidEurMedArea = UUID.fromString("aa6a5b25-3ee3-4771-b4d1-b91918f23fa5");
	public static final UUID uuidHiddenArea = UUID.fromString("0318c67d-e323-4e9c-bffb-bc0c7f8f9f40");
	public static final UUID uuidTaxonomicallyValueless = UUID.fromString("e07060cc-5a51-471f-863b-01011e3142fb");
	public static final UUID uuidProbablyTaxonomicallyValueless = UUID.fromString("3b8b620e-986d-4a87-8070-1884a51c6bad");

	//Extension Types
	public static final UUID uuidSpeciesExpertName = UUID.fromString("2e8153d2-7412-49e4-87e1-5c38f4c5153a");
	public static final UUID uuidExpertName = UUID.fromString("24becb79-a90c-47d3-be35-efc87bb48fd3");

	public static final UUID DETAIL_EXT_UUID = UUID.fromString("c3959b4f-d876-4b7a-a739-9260f4cafd1c");
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");
	public static final UUID uuidIsoCode = UUID.fromString("048b8153-e3ee-451c-a72c-f1c8bc291c3e");
	public static final UUID uuidTdwgAreaCode = UUID.fromString("73ad0288-b71b-4a14-9c2e-7f81f1e64a36");
	public static final UUID uuidMclCode = UUID.fromString("aa27083a-6a96-42aa-a2f8-5541cf057067");

	//Area Level
	public static final UUID uuidEuroMedAreaLevelTop = UUID.fromString("190d5758-4b96-4016-9412-6dc9c36ef5fd");
	public static final UUID uuidEuroMedAreaLevelFirst = UUID.fromString("d21503e3-875e-4abc-82ec-f812e3cfea84");
	public static final UUID uuidEuroMedAreaLevelSecond = UUID.fromString("97ac0bf5-b31c-487a-8ed5-a576f46c902c");

	//languages
	public static final UUID uuidLangMajorcan = UUID.fromString("82d696d7-cb4e-49de-ac89-63a0e12ca766");

	//REFERENCES
	public static int REF_ARTICLE = 1;
	public static int REF_PART_OF_OTHER_TITLE = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_CD = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNKNOWN = 10;
	public static int REF_PRINT_SERIES = 55;
	public static int REF_CONFERENCE_PROCEEDINGS = 56;
	public static int REF_JOURNAL_VOLUME = 57;



	//NameStatus
	public static int NAME_ST_NOM_INVAL = 1;
	public static int NAME_ST_NOM_ILLEG = 2;
	public static int NAME_ST_NOM_NUD = 3;
	public static int NAME_ST_NOM_REJ = 4;
	public static int NAME_ST_NOM_REJ_PROP = 5;
	public static int NAME_ST_NOM_UTIQUE_REJ = 6;
	public static int NAME_ST_NOM_UTIQUE_REJ_PROP = 7;
	public static int NAME_ST_NOM_CONS = 8;
	public static int NAME_ST_NOM_CONS_PROP = 9;
	public static int NAME_ST_ORTH_CONS = 10;
	public static int NAME_ST_ORTH_CONS_PROP = 11;
	public static int NAME_ST_NOM_SUPERFL = 12;
	public static int NAME_ST_NOM_AMBIG = 13;
	public static int NAME_ST_NOM_PROVIS = 14;
	public static int NAME_ST_NOM_DUB = 15;
	public static int NAME_ST_NOM_NOV = 16;
	public static int NAME_ST_NOM_CONFUS = 17;
	public static int NAME_ST_NOM_ALTERN = 18;
	public static int NAME_ST_COMB_INVAL = 19;


	//NameRelationShip
	public static int NAME_REL_IS_BASIONYM_FOR = 1;
	public static int NAME_REL_IS_LATER_HOMONYM_OF = 2;
	public static int NAME_REL_IS_REPLACED_SYNONYM_FOR = 3;
	public static int NAME_REL_IS_VALIDATION_OF = 4;
	public static int NAME_REL_IS_LATER_VALIDATION_OF = 5;
	public static int NAME_REL_IS_TYPE_OF = 6;
	public static int NAME_REL_IS_CONSERVED_TYPE_OF =7;
	public static int NAME_REL_IS_REJECTED_TYPE_OF = 8;
	public static int NAME_REL_IS_FIRST_PARENT_OF = 9;
	public static int NAME_REL_IS_SECOND_PARENT_OF = 10;
	public static int NAME_REL_IS_FEMALE_PARENT_OF = 11;
	public static int NAME_REL_IS_MALE_PARENT_OF = 12;
	public static int NAME_REL_IS_CONSERVED_AGAINST =13;
	public static int NAME_REL_IS_REJECTED_IN_FAVOUR_OF = 14;
	public static int NAME_REL_IS_TREATED_AS_LATER_HOMONYM_OF = 15;
	public static int NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF = 16;
	public static int NAME_REL_IS_ALTERNATIVE_NAME_FOR = 17;
	public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
	public static int NAME_REL_IS_LECTOTYPE_OF = 61;
	public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;

	//NameFacts
	public static String NAME_FACT_PROTOLOGUE = "Protologue";
	public static String NAME_FACT_ALSO_PUBLISHED_IN = "Also published in";
	public static String NAME_FACT_BIBLIOGRAPHY = "Bibliography";

	//TaxonRelationShip
	public static int TAX_REL_IS_INCLUDED_IN = 1;
	public static int TAX_REL_IS_SYNONYM_OF = 2;
	public static int TAX_REL_IS_MISAPPLIED_NAME_OF = 3;
	public static int TAX_REL_IS_PROPARTE_SYN_OF = 4;
	public static int TAX_REL_IS_PARTIAL_SYN_OF = 5;
	public static int TAX_REL_IS_HETEROTYPIC_SYNONYM_OF = 6;
	public static int TAX_REL_IS_HOMOTYPIC_SYNONYM_OF = 7;
	public static int TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF = 101;
	public static int TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF = 102;
	public static int TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF = 103;
	public static int TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF = 104;
	public static int TAX_REL_IS_PRO_PARTE_MISAPPLIED_NAME_OF = 110;  //does not really exist but as only used in BM export which is not in use anymore it serves as a placeholder for consistency



	//TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;


	//Facts
	public static int FACT_DESCRIPTION = 1;
	public static int FACT_GROWTH_FORM = 2;
	public static int FACT_HARDINESS = 3;
	public static int FACT_ECOLOGY = 4;
	public static int FACT_PHENOLOGY = 5;
	public static int FACT_KARYOLOGY = 6;
	public static int FACT_ILLUSTRATION = 7;
	public static int FACT_IDENTIFICATION = 8;
	public static int FACT_OBSERVATION = 9;
	public static int FACT_DISTRIBUTION_EM = 10;
	public static int FACT_DISTRIBUTION_WORLD = 11;
	//E+M
	public static final UUID uuidFeatureMaps = UUID.fromString("8367730e-f3c3-4361-8360-a2057e4295ed");
	public static final UUID uuidFeatureConservationStatus = UUID.fromString("a32f33cd-1966-4a22-986c-94c5e688bbd1");
	public static final UUID uuidFeatureUse = UUID.fromString("199bbbd8-2db6-4335-b454-2e92ae02b699");
	public static final UUID uuidFeatureComments = UUID.fromString("31cc2b92-5cad-44e9-b50f-b8af591a527c");
	public static final UUID uuidFeatureDistrEM = UUID.fromString("a5ba7e7f-ca7f-4f50-afc7-73e76b3231d4");
	public static final UUID uuidFeatureDistrWorld = UUID.fromString("e4e24080-7017-47e6-924e-d2560fa68fb8");
	public static final UUID uuidFeatureEditorBrackets = UUID.fromString("b3b5bc1a-7ba8-4a39-9c0d-63ba599eb5d8");
	public static final UUID uuidFeatureEditorParenthesis = UUID.fromString("6ee10a2e-ff02-4cf4-a520-89630edc5b44");
	public static final UUID uuidFeatureInedited = UUID.fromString("c93e2968-bc52-4165-9755-ce37611faf01");
	public static final UUID uuidFeatureCommentsEditing = UUID.fromString("7a155021-158a-48bb-81d0-9a72b718e2de");

	//Salvador
	public static final UUID uuidFeatureDistributionGlobal = UUID.fromString("9bd09ada-7bb3-4fe5-be35-dc4564bdd161");
	public static final UUID uuidReporteParaElSalvador = UUID.fromString("1869a1a6-becb-468e-bc62-b2473f7d9391");
	public static final UUID uuidFeatureOtherReferences = UUID.fromString("f5dc30bd-a5b1-436d-8cc0-69a999e61590");
	public static final UUID uuidFeatureTaxonIllustrationReferences = UUID.fromString("6062122b-07c7-44e4-a9af-7daea0005819");
    public static final UUID uuidFeatureSpecimenNotes = UUID.fromString("3c657936-f51a-4bfa-9ea4-287926ac63e5");
    public static final UUID uuidFeatureEditorialNotes = UUID.fromString("5f971b66-1bc7-4b12-923e-2b4ee8b3737d");
    public static final UUID uuidFeatureHabitatSalvador = UUID.fromString("d03a5e2c-fd93-4fff-b0b3-e16cefbb9847");

	public static UUID uuidNomStatusCombIned = UUID.fromString("dde8a2e7-bf9e-42ec-b186-d5bde9c9c128");
	public static UUID uuidNomStatusSpNovIned = UUID.fromString("1a359ca1-9364-43bc-93e4-834bdcd52b72");
	public static UUID uuidNomStatusNomOrthCons = UUID.fromString("0f838183-ffa0-4014-928e-0e3a27eb3918");

	static NomenclaturalStatusType nomStatusCombIned;
	static NomenclaturalStatusType nomStatusSpNovIned;
	static NomenclaturalStatusType nomStatusNomOrthCons;

	public static NomenclaturalStatusType nomStatusTypeAbbrev2NewNomStatusType(String nomStatus){
		NomenclaturalStatusType result = null;
		if (nomStatus == null){
			return null;
		}else if (nomStatus.equalsIgnoreCase("comb. ined.")){
			if (nomStatusCombIned == null){
				nomStatusCombIned = NomenclaturalStatusType.NewInstance("comb. ined.", "comb. ined.", "comb. ined.", Language.LATIN());
				nomStatusCombIned.setUuid(uuidNomStatusCombIned);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusCombIned);
			}
			result = nomStatusCombIned;
		}else if (nomStatus.equalsIgnoreCase("sp. nov. ined.")){
			if (nomStatusSpNovIned == null){
				nomStatusSpNovIned = NomenclaturalStatusType.NewInstance("sp. nov. ined.", "sp. nov. ined.", "sp. nov. ined.", Language.LATIN());
				nomStatusSpNovIned.setUuid(uuidNomStatusSpNovIned);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusSpNovIned);
			}
			result = nomStatusSpNovIned;
		}else if (nomStatus.equalsIgnoreCase("nom. & orth. cons.")){
			if (nomStatusNomOrthCons == null){
				nomStatusNomOrthCons = NomenclaturalStatusType.NewInstance("nom. & orth. cons.", "nom. & orth. cons.", "nom. & orth. cons.", Language.LATIN());
				nomStatusNomOrthCons.setUuid(uuidNomStatusNomOrthCons);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusNomOrthCons);
			}
			result = nomStatusNomOrthCons;
		}
		return result;
	}


	public static NomenclaturalStatus nomStatusFkToNomStatus(int nomStatusFk, String nomStatusLabel)  throws UnknownCdmTypeException{
		if (nomStatusFk == NAME_ST_NOM_INVAL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID());
		}else if (nomStatusFk == NAME_ST_NOM_ILLEG){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
		}else if (nomStatusFk == NAME_ST_NOM_NUD){
			 return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM());
		}else if (nomStatusFk == NAME_ST_NOM_REJ){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED());
		}else if (nomStatusFk == NAME_ST_NOM_REJ_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED());
		}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_CONS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED());
		}else if (nomStatusFk == NAME_ST_NOM_CONS_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED_PROP());
		}else if (nomStatusFk == NAME_ST_ORTH_CONS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED());
		}else if (nomStatusFk == NAME_ST_ORTH_CONS_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_SUPERFL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS());
		}else if (nomStatusFk == NAME_ST_NOM_AMBIG){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS());
		}else if (nomStatusFk == NAME_ST_NOM_PROVIS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.PROVISIONAL());
		}else if (nomStatusFk == NAME_ST_NOM_DUB){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.DOUBTFUL());
		}else if (nomStatusFk == NAME_ST_NOM_NOV){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM());
		}else if (nomStatusFk == NAME_ST_NOM_CONFUS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONFUSUM());
		}else if (nomStatusFk == NAME_ST_NOM_ALTERN){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE());
		}else if (nomStatusFk == NAME_ST_COMB_INVAL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_INVALID());
		}else {
			NomenclaturalStatusType statusType = nomStatusTypeAbbrev2NewNomStatusType(nomStatusLabel);
			NomenclaturalStatus result = NomenclaturalStatus.NewInstance(statusType);
			if (result != null){
				return result;
			}
			throw new UnknownCdmTypeException("Unknown NomenclaturalStatus (id=" + Integer.valueOf(nomStatusFk).toString() + ")");
		}
	}

	public static UUID getEMAreaUuid(String areaId){
		if (areaId == null){
			logger.warn("No AreaId given");
			return null;
		}else if (areaId.equals("EM")){return uuidEM;
		}else if (areaId.equals("EUR")){return uuidEUR;
		}else if (areaId.equals("14")){return uuid14;
		}else if (areaId.equals("20")){return uuid20;
		}else if (areaId.equals("21")){return uuid21;
		}else if (areaId.equals("33")){return uuid33;
		}else if (areaId.equals("Da")){return uuidDa;
		}else if (areaId.equals("Fe")){return uuidFe;
		}else if (areaId.equals("Fa")){return uuidFa;
		}else if (areaId.equals("Br")){return uuidBr;
		}else if (areaId.equals("Is")){return uuidIs;
		}else if (areaId.equals("Hb")){return uuidHb;
		}else if (areaId.equals("No")){return uuidNo;
		}else if (areaId.equals("Sb")){return uuidSb;
		}else if (areaId.equals("Su")){return uuidSu;
		}else if (areaId.equals("Au")){return uuidAu;
		}else if (areaId.equals("Be")){return uuidBe;
		}else if (areaId.equals("Cz")){return uuidCz;
		}else if (areaId.equals("Ge")){return uuidGe;
		}else if (areaId.equals("Hu")){return uuidHu;
		}else if (areaId.equals("Ho")){return uuidHo;
		}else if (areaId.equals("Po")){return uuidPo;
		}else if (areaId.equals("He")){return uuidHe;
		}else if (areaId.equals("Bl")){return uuidBl;
		}else if (areaId.equals("Co")){return uuidCo;
		}else if (areaId.equals("Ga")){return uuidGa;
		}else if (areaId.equals("Lu")){return uuidLu;
		}else if (areaId.equals("Sa")){return uuidSa;
		}else if (areaId.equals("Hs")){return uuidHs;
		}else if (areaId.equals("Al")){return uuidAl;
		}else if (areaId.equals("Bu")){return uuidBu;
		}else if (areaId.equals("Gr")){return uuidGr;
		}else if (areaId.equals("It")){return uuidIt;
		}else if (areaId.equals("Cr")){return uuidCr;
		}else if (areaId.equals("Rm")){return uuidRm;
		}else if (areaId.equals("Si(S)")){return uuidSi_S;
		}else if (areaId.equals("Tu(E)")){return uuidTu_E;
		}else if (areaId.equals("Ju")){return uuidJu;
		}else if (areaId.equals("Uk(K)")){return uuidUk_K;
		}else if (areaId.equals("Uk")){return uuidUk;
		}else if (areaId.equals("Ag")){return uuidAg;
		}else if (areaId.equals("Eg")){return uuidEg;
		}else if (areaId.equals("Li")){return uuidLi;
		}else if (areaId.equals("Ma")){return uuidMa;
		}else if (areaId.equals("Tn")){return uuidTn;
		}else if (areaId.equals("Az")){return uuidAz;
		}else if (areaId.equals("Sg")){return uuidSg;
		}else if (areaId.equals("Ab")){return uuidAb;
		}else if (areaId.equals("Ar")){return uuidAr;
		}else if (areaId.equals("Ab(A)")){return uuidAb_A;
		}else if (areaId.equals("Gg")){return uuidGg;
		}else if (areaId.equals("Ab(N)")){return uuidAb_N;
		}else if (areaId.equals("Cy")){return uuidCy;
		}else if (areaId.equals("AE(G)")){return uuidAE_G;
		}else if (areaId.equals("Le")){return uuidLe;
		}else if (areaId.equals("Sn")){return uuidSn;
		}else if (areaId.equals("Tu(A)")){return uuidTu_A;
		}else if (areaId.equals("Tu")){return uuidTu;
		}else if (areaId.equals("Au(A)")){return uuidAu_A;
		}else if (areaId.equals("Au(L)")){return uuidAu_L;
		}else if (areaId.equals("Az(C)")){return uuidAz_C;
		}else if (areaId.equals("Az(F)")){return uuidAz_F;
		}else if (areaId.equals("Az(G)")){return uuidAz_G;
		}else if (areaId.equals("Az(P)")){return uuidAz_P;
		}else if (areaId.equals("Az(S)")){return uuidAz_S;
		}else if (areaId.equals("Az(J)")){return uuidAz_J;
		}else if (areaId.equals("Az(M)")){return uuidAz_M;
		}else if (areaId.equals("Az(T)")){return uuidAz_T;
		}else if (areaId.equals("Be(B)")){return uuidBe_B;
		}else if (areaId.equals("Be(L)")){return uuidBe_L;
		}else if (areaId.equals("Bl(I)")){return uuidBl_I;
		}else if (areaId.equals("Bl(M)")){return uuidBl_M;
		}else if (areaId.equals("Bl(N)")){return uuidBl_N;
		}else if (areaId.equals("BH")){return uuidBH;
		}else if (areaId.equals("By")){return uuidBy;
		}else if (areaId.equals("Ca")){return uuidCa;
		}else if (areaId.equals("Ca(F)")){return uuidCa_F;
		}else if (areaId.equals("Ca(G)")){return uuidCa_G;
		}else if (areaId.equals("Ca(C)")){return uuidCa_C;
		}else if (areaId.equals("Ca(H)")){return uuidCa_H;
		}else if (areaId.equals("Ca(L)")){return uuidCa_L;
		}else if (areaId.equals("Ca(P)")){return uuidCa_P;
		}else if (areaId.equals("Ca(T)")){return uuidCa_T;
		}else if (areaId.equals("Cs")){return uuidCs;
		}else if (areaId.equals("Ct")){return uuidCt;
		}else if (areaId.equals("Es")){return uuidEs;
		}else if (areaId.equals("Ga(C)")){return uuidGa_C;
		}else if (areaId.equals("Ga(F)")){return uuidGa_F;
		}else if (areaId.equals("Gg(A)")){return uuidGg_A;
		}else if (areaId.equals("Gg(D)")){return uuidGg_D;
		}else if (areaId.equals("Gg(G)")){return uuidGg_G;
		}else if (areaId.equals("Hs(A)")){return uuidHs_A;
		}else if (areaId.equals("Hs(G)")){return uuidHs_G;
		}else if (areaId.equals("Hs(S)")){return uuidHs_S;
		}else if (areaId.equals("Ir")){return uuidIr;
		}else if (areaId.equals("It(I)")){return uuidIt_I;
		}else if (areaId.equals("It(S)")){return uuidIt_S;
		}else if (areaId.equals("Jo")){return uuidJo;
		}else if (areaId.equals("Kz")){return uuidKz;
		}else if (areaId.equals("La")){return uuidLa;
		}else if (areaId.equals("Lt")){return uuidLt;
		}else if (areaId.equals("Ma(E)")){return uuidMa_E;
		}else if (areaId.equals("Ma(S)")){return uuidMa_S;
		}else if (areaId.equals("Mk")){return uuidMk;
		}else if (areaId.equals("Md")){return uuidMd;
		}else if (areaId.equals("Md(D)")){return uuidMd_D;
		}else if (areaId.equals("Md(M)")){return uuidMd_M;
		}else if (areaId.equals("Md(P)")){return uuidMd_P;
		}else if (areaId.equals("Si(M)")){return uuidSi_M;
		}else if (areaId.equals("Mo")){return uuidMo;
		}else if (areaId.equals("Rf")){return uuidRf;
		}else if (areaId.equals("Rf(C)")){return uuidRf_C;
		}else if (areaId.equals("Rf(E)")){return uuidRf_E;
		}else if (areaId.equals("Rf(K)")){return uuidRf_K;
		}else if (areaId.equals("Rf(CS)")){return uuidRf_CS;
		}else if (areaId.equals("Rf(N)")){return uuidRf_N;
		}else if (areaId.equals("Rf(NW)")){return uuidRf_NW;
		}else if (areaId.equals("Rf(A)")){return uuidRf_A;
		}else if (areaId.equals("Rf(S)")){return uuidRf_S;
		}else if (areaId.equals("Sk")){return uuidSk;
		}else if (areaId.equals("Sl")){return uuidSl;
		}else if (areaId.equals("Sy")){return uuidSy;
		}else if (areaId.equals("Uk(U)")){return uuidUk_U;
		}else if (areaId.equals("SM")){return uuidSM;
		}else if (areaId.equals("Yu(K)")){return uuidYu_K;
		}else if (areaId.equals("Cg")){return uuidCg;
		}else if (areaId.equals("Sr")){return uuidSr;
		}else if (areaId.equals("IJ")){return uuidIJ;
		}else if (areaId.equals("LS")){return uuidLS;
		}else if (areaId.equals("Rs")){return uuidRs;
		}else if (areaId.equals("Si")){return uuidSi;
		}else if (areaId.equals("Az(L)")){return uuidAz_L;
		}else if (areaId.equals("Hb(E)")){return uuidHb_E;
		}else if (areaId.equals("Hb(N)")){return uuidHb_N;
		}else if (areaId.equals("Ga(M)")){return uuidGa_M;
		}else if (areaId.equals("Ma(M)")){return uuidMa_M;
		}else if (areaId.equals("Rs(N)")){return uuidRs_N;
		}else if (areaId.equals("Rs(B)")){return uuidRs_B;
		}else if (areaId.equals("Rs(C)")){return uuidRs_C;
		}else if (areaId.equals("Rs(W)")){return uuidRs_W;
		}else if (areaId.equals("Rs(E)")){return uuidRs_E;
		}else if (areaId.equals("AE")){return uuidAE;
		}else if (areaId.equals("AE(T)")){return uuidAE_T;
		}else if (areaId.equals("Rs(K)")){return uuidRs_K;
		}else if (areaId.equals("Cc")){return uuidCc;
		}else if (areaId.equals("Bt")){return uuidBt;
		}else if (areaId.equals("Tcs")){return uuidTcs;
        }else if (areaId.equals("Azores")){return null;  //these are duplicates and are handled differently
        }else if (areaId.equals("Canary Is.")){return null; //these are duplicates and are handled differently
        }else{
		    logger.warn("Area not found: " + areaId);
			return null;
		}

	}


	//TypeDesignation
	public static SpecimenTypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
		switch (typeStatusId){
			case 0: return null;
			case 1: return SpecimenTypeDesignationStatus.HOLOTYPE();
			case 2: return SpecimenTypeDesignationStatus.LECTOTYPE();
			case 3: return SpecimenTypeDesignationStatus.NEOTYPE();
			case 4: return SpecimenTypeDesignationStatus.EPITYPE();
			case 5: return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
			case 6: return SpecimenTypeDesignationStatus.ISONEOTYPE();
			case 7: return SpecimenTypeDesignationStatus.ISOTYPE();
			case 8: return SpecimenTypeDesignationStatus.PARANEOTYPE();
			case 9: return SpecimenTypeDesignationStatus.PARATYPE();
			case 10: return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
			case 11: return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
			case 12: return SpecimenTypeDesignationStatus.SYNTYPE();
			case 21: return SpecimenTypeDesignationStatus.ICONOTYPE();
			case 22: return SpecimenTypeDesignationStatus.PHOTOTYPE();
			default: {
				throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(typeStatusId).toString() + ")");
			}
		}
	}

	//TypeDesignation
	public static TaxonRelationshipType taxonRelId2TaxonRelType (int relTaxonTypeId, ResultWrapper<Boolean> isInverse, ResultWrapper<Boolean> isDoubtful)  throws UnknownCdmTypeException{
		isInverse.setValue(false);
		switch (relTaxonTypeId){
			case 0: return null;
			case 11: return TaxonRelationshipType.CONGRUENT_TO();
			case 12: isInverse.setValue(true); return TaxonRelationshipType.INCLUDES();
			case 13: isInverse.setValue(true); return TaxonRelationshipType.CONGRUENT_OR_INCLUDES();
			case 14: return TaxonRelationshipType.INCLUDES();
			case 15: return TaxonRelationshipType.CONGRUENT_OR_INCLUDES();
			case 16: return TaxonRelationshipType.INCLUDED_OR_INCLUDES();
			case 17: return TaxonRelationshipType.CONGRUENT_OR_INCLUDED_OR_INCLUDES();
			case 18: return TaxonRelationshipType.OVERLAPS();
			case 19: return TaxonRelationshipType.CONGRUENT_OR_OVERLAPS();
			case 20: isInverse.setValue(true); return TaxonRelationshipType.INCLUDES_OR_OVERLAPS();
			case 21: isInverse.setValue(true); return TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_OVERLAPS();
			case 22: return TaxonRelationshipType.INCLUDES_OR_OVERLAPS();
			case 23: return TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_OVERLAPS();
			case 24: return TaxonRelationshipType.INCLUDED_OR_INCLUDES_OR_OVERLAPS();

			case 26: return TaxonRelationshipType.EXCLUDES();

			//TODO other relationshipTypes

			case 40: return TaxonRelationshipType.NOT_CONGRUENT_TO();

			//how to implement this, what is a purely doubtful relationship ??
//			case 42: isDoubtful.setValue(true); return TaxonRelationshipType.();

			case 43: isDoubtful.setValue(true); return TaxonRelationshipType.CONGRUENT_TO();
			case 44: isInverse.setValue(true);isDoubtful.setValue(true); return TaxonRelationshipType.INCLUDES();
			case 46: isDoubtful.setValue(true); return TaxonRelationshipType.INCLUDES();
			case 48: isDoubtful.setValue(true); return TaxonRelationshipType.INCLUDED_OR_INCLUDES();
			case 50: isDoubtful.setValue(true); return TaxonRelationshipType.OVERLAPS();
			case 51: isDoubtful.setValue(true); return TaxonRelationshipType.CONGRUENT_OR_OVERLAPS();
			case 58: isDoubtful.setValue(true); return TaxonRelationshipType.EXCLUDES();


			default: {
				throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(relTaxonTypeId).toString() + ")");
			}
		}
	}

	//TypeDesignation
	public static HybridRelationshipType relNameId2HybridRel (int relNameId)  throws UnknownCdmTypeException{
		switch (relNameId){
			case 0: return null;
			case 9: return HybridRelationshipType.FIRST_PARENT();
			case 10: return HybridRelationshipType.SECOND_PARENT();
			case 11: return HybridRelationshipType.FEMALE_PARENT();
			case 12: return HybridRelationshipType.MALE_PARENT();
			default: {
				throw new UnknownCdmTypeException("Unknown HybridRelationshipType (id=" + Integer.valueOf(relNameId).toString() + ")");
			}
		}
	}

	//OccStatus
	public static PresenceAbsenceTerm occStatus2PresenceAbsence (int occStatusId)  throws UnknownCdmTypeException{
		switch (occStatusId){
			case 0: return null;
			case 110: return PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
			case 120: return PresenceAbsenceTerm.CULTIVATED();
			case 210: return PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
			case 220: return PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
			case 230: return PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
			case 240: return PresenceAbsenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
			case 250: return PresenceAbsenceTerm.INTRODUCED();
			case 260: return PresenceAbsenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
			case 270: return PresenceAbsenceTerm.CASUAL();
			case 280: return PresenceAbsenceTerm.NATURALISED();
			case 310: return PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR();
			case 320: return PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
			case 330: return PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE();
			case 340: return PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE();
			case 350: return PresenceAbsenceTerm.NATIVE();
			case 999: {
					logger.info("endemic for EM can not be transformed in legal status. Used 'PRESENT' instead");
					//TODO preliminary
					return PresenceAbsenceTerm.PRESENT();
				}
			default: {
				throw new UnknownCdmTypeException("Unknown occurrence status  (id=" + Integer.valueOf(occStatusId).toString() + ")");
			}
		}
	}


	//FactCategory
	public static Feature factCategory2Feature (int factCategoryId)  throws UnknownCdmTypeException{
		switch (factCategoryId){
			case 0: return null;
			case 1: return Feature.DESCRIPTION();
			case 4: return Feature.ECOLOGY();
			case 5: return Feature.PHENOLOGY();
			case 12: return Feature.COMMON_NAME();
			case 13: return Feature.OCCURRENCE();
			case 99: return Feature.CITATION();
			default: {
				throw new UnknownCdmTypeException("Unknown FactCategory (id=" + Integer.valueOf(factCategoryId).toString() + ")");
			}
		}
	}

	public static UUID getFeatureUuid(String key) {
		if (key == null){
			return null;
		}else if (key.equalsIgnoreCase("14-Maps")){ return uuidFeatureMaps;
		}else if (key.equalsIgnoreCase("301-Conservation Status")){ return uuidFeatureConservationStatus;
		}else if (key.equalsIgnoreCase("302-Use")){ return uuidFeatureUse;
		}else if (key.equalsIgnoreCase("303-Comments")){ return uuidFeatureComments;

		}else if (key.equalsIgnoreCase("10-general distribution (Euro+Med)")){ return uuidFeatureDistrEM;
		}else if (key.equalsIgnoreCase("11-general distribution (world)")){ return uuidFeatureDistrWorld;
		}else if (key.equalsIgnoreCase("250-Editor_Brackets")){ return uuidFeatureEditorBrackets;
		}else if (key.equalsIgnoreCase("251-Editor_Parenthesis")){ return uuidFeatureEditorParenthesis;
		}else if (key.equalsIgnoreCase("252-Inedited")){ return uuidFeatureInedited;
		}else if (key.equalsIgnoreCase("253-Comments on editing process")){ return uuidFeatureCommentsEditing;

		//salvador
		}else if (key.equalsIgnoreCase("302-Usos")){ return Feature.uuidUses;
		}else if (key.equalsIgnoreCase("303-Distribución global")){ return uuidFeatureDistributionGlobal;
		}else if (key.equalsIgnoreCase("306-Nombre(s) común(es)")){ return Feature.uuidCommonName;
		}else if (key.equalsIgnoreCase("307-Muestras de herbario")){ return Feature.uuidSpecimen;
        }else if (key.equalsIgnoreCase("309-Reporte para El Salvador")){ return uuidReporteParaElSalvador;
        }else if (key.equalsIgnoreCase("310-Otras referencias")){ return uuidFeatureOtherReferences;
        }else if (key.equalsIgnoreCase("311-Ilustración(es)")){ return uuidFeatureTaxonIllustrationReferences;
        }else if (key.equalsIgnoreCase("312-Imágen")){ return Feature.uuidImage;
        }else if (key.equalsIgnoreCase("350-Descripción*")){ return Feature.uuidDescription;
        }else if (key.equalsIgnoreCase("1800-Notas de muestras*")){ return uuidFeatureSpecimenNotes;
        }else if (key.equalsIgnoreCase("1900-Notas editoriales*")){ return uuidFeatureEditorialNotes;
        }else if (key.equalsIgnoreCase("2000-Habitat en El Salvador*")){ return uuidFeatureHabitatSalvador;







		}else{
			return null;
		}
	}


	static Rank collSpeciesRank;
	/**
	 * @param i
	 * @return
	 */
	private static Rank rankId2NewRank(Integer rankId, boolean switchRank) {
		Rank result = null;
		if (rankId == null){
			return null;
		}else if (rankId == 57){

			if (collSpeciesRank == null){
				collSpeciesRank = Rank.NewInstance(RankClass.SpeciesGroup, "Collective species", "Coll. species", "coll.");
				collSpeciesRank.setUuid(uuidRankCollSpecies);
				OrderedTermVocabulary<Rank> voc = (OrderedTermVocabulary<Rank>)Rank.SPECIES().getVocabulary();
				voc.addTermBelow(collSpeciesRank, Rank.SPECIESGROUP());
			}
			result = collSpeciesRank;
		}
		return result;
	}


	public static Rank rankId2Rank (ResultSet rs, boolean useUnknown, boolean switchSpeciesGroup) throws UnknownCdmTypeException{
		Rank result;
		try {
			int rankId = rs.getInt("rankFk");

			String abbrev = rs.getString("rankAbbrev");
			String rankName = rs.getString("rank");
			if (logger.isDebugEnabled()){logger.debug(rankId);}
			if (logger.isDebugEnabled()){logger.debug(abbrev);}
			if (logger.isDebugEnabled()){logger.debug(rankName);}

			if (switchSpeciesGroup){
				if (rankId == 59){
					rankId = 57;
				}else if (rankId == 57){
					rankId = 59;
				}
			}
			try {
				result = Rank.getRankByNameOrIdInVoc(abbrev);
			} catch (UnknownCdmTypeException e) {
				try {
					result = Rank.getRankByNameOrIdInVoc(rankName);
				} catch (UnknownCdmTypeException e1) {
					switch (rankId){
						case 0: return null;
						case 1: return Rank.KINGDOM();
						case 3: return Rank.SUBKINGDOM();
						case 5: return Rank.PHYLUM();
						case 7: return Rank.SUBPHYLUM();
						case 8: return Rank.DIVISION();
						case 9: return Rank.SUBDIVISION();
						case 10: return Rank.CLASS();
						case 13: return Rank.SUBCLASS();
						case 16: return Rank.SUPERORDER();
						case 18: return Rank.ORDER();
						case 19: return Rank.SUBORDER();
						case 20: return Rank.FAMILY();
						case 25: return Rank.SUBFAMILY();
						case 30: return Rank.TRIBE();
						case 35: return Rank.SUBTRIBE();
						case 40: return Rank.GENUS();
						case 42: return Rank.SUBGENUS();
						case 45: return Rank.SECTION_BOTANY();
						case 47: return Rank.SUBSECTION_BOTANY();
						case 50: return Rank.SERIES();
						case 52: return Rank.SUBSERIES();
						case 58: return Rank.SPECIESAGGREGATE();
						case 59: return Rank.SPECIESGROUP();
						case 60: return Rank.SPECIES();
						case 61: return Rank.GREX();
						case 65: return Rank.SUBSPECIES();
						case 66: {System.out.println("Rank 66 not yet implemented"); throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());}
						case 67: {System.out.println("Rank 67 not yet implemented"); throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());}
						case 68: return Rank.CONVAR();
						case 70: return Rank.VARIETY();
						case 73: return Rank.SUBVARIETY();
						case 80: return Rank.FORM();
						case 82: return Rank.SUBFORM();
						case 84: return Rank.SPECIALFORM();
						case 98: return Rank.INFRAGENERICTAXON();
						case 99: return Rank.INFRASPECIFICTAXON();

						case 750: return Rank.SUPERCLASS();
						case 780: return Rank.INFRACLASS();
						case 820: return Rank.INFRAORDER();

						case 830: return Rank.SUPERFAMILY();

						default: {
							Rank rank = rankId2NewRank(57, switchSpeciesGroup);
							if (rank != null){
								return rank;
							}
							if (useUnknown){
								logger.error("Rank unknown: " + rankId + ". Created UNKNOWN_RANK");
								return Rank.UNKNOWN_RANK();
							}
							throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());
						}
					}
				}
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warn("Exception occurred. Created UNKNOWN_RANK instead");
			return Rank.UNKNOWN_RANK();
		}
	}


	public static Integer rank2RankId (Rank rank){
		if (rank == null){
			return null;
		}
		else if (rank.equals(Rank.KINGDOM())){		return 1;}
		else if (rank.equals(Rank.SUBKINGDOM())){	return 3;}
		else if (rank.equals(Rank.PHYLUM())){		return 5;}
		else if (rank.equals(Rank.SUBPHYLUM())){	return 7;}
		else if (rank.equals(Rank.DIVISION())){		return 8;}
		else if (rank.equals(Rank.SUBDIVISION())){	return 9;}

		else if (rank.equals(Rank.CLASS())){		return 10;}
		else if (rank.equals(Rank.SUBCLASS())){		return 13;}
		else if (rank.equals(Rank.SUPERORDER())){	return 16;}
		else if (rank.equals(Rank.ORDER())){		return 18;}
		else if (rank.equals(Rank.SUBORDER())){		return 19;}
		else if (rank.equals(Rank.FAMILY())){		return 20;}
		else if (rank.equals(Rank.SUBFAMILY())){	return 25;}
		else if (rank.equals(Rank.TRIBE())){		return 30;}
		else if (rank.equals(Rank.SUBTRIBE())){		return 35;}
		else if (rank.equals(Rank.GENUS())){		return 40;}
		else if (rank.equals(Rank.SUBGENUS())){		return 42;}
		else if (rank.equals(Rank.SECTION_BOTANY())){		return 45;}
		else if (rank.equals(Rank.SUBSECTION_BOTANY())){	return 47;}
		else if (rank.equals(Rank.SERIES())){		return 50;}
		else if (rank.equals(Rank.SUBSERIES())){	return 52;}
		else if (rank.equals(Rank.SPECIESAGGREGATE())){	return 58;}
		//TODO
		//		else if (rank.equals(Rank.XXX())){	return 59;}
		else if (rank.equals(Rank.SPECIES())){		return 60;}
		else if (rank.equals(Rank.SUBSPECIES())){	return 65;}
		else if (rank.equals(Rank.CONVAR())){		return 68;}
		else if (rank.equals(Rank.VARIETY())){		return 70;}
		else if (rank.equals(Rank.SUBVARIETY())){	return 73;}
		else if (rank.equals(Rank.FORM())){			return 80;}
		else if (rank.equals(Rank.SUBFORM())){		return 82;}
		else if (rank.equals(Rank.SPECIALFORM())){	return 84;}
		else if (rank.equals(Rank.INFRAGENERICTAXON())){	return 98;}
		else if (rank.equals(Rank.INFRASPECIFICTAXON())){	return 99;}

		else if (rank.equals(Rank.SUPERCLASS())){	return 750;}
		else if (rank.equals(Rank.INFRACLASS())){	return 780;}
		else if (rank.equals(Rank.INFRAORDER())){	return 820;}
		else if (rank.equals(Rank.SUPERFAMILY())){	return 830;}

		else {
			//TODO Exception
			logger.warn("Rank not yet supported in Berlin Model: "+ rank.getLabel());
			return null;
		}
	}

	public static Integer textData2FactCategoryFk (Feature feature){
		if (feature == null){return null;}
		if (feature.equals(Feature.DESCRIPTION())){
			return 1;
		}else if (feature.equals(Feature.ECOLOGY())){
			return 4;
		}else if (feature.equals(Feature.PHENOLOGY())){
			return 5;
		}else if (feature.equals(Feature.COMMON_NAME())){
			return 12;
		}else if (feature.equals(Feature.OCCURRENCE())){
			return 13;
		}else if (feature.equals(Feature.CITATION())){
			return 99;
		}else{
			logger.debug("Unknown Feature.");
			return null;
		}
	}


	public static Integer taxonBase2statusFk (TaxonBase<?> taxonBase){
		if (taxonBase == null){return null;}
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_ACCEPTED;
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_UNRESOLVED;
		}
		//TODO
//		public static int T_STATUS_PARTIAL_SYN = 3;
//		public static int T_STATUS_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_UNRESOLVED = 5;
//		public static int T_STATUS_ORPHANED = 6;
	}

	public static Integer ref2refCategoryId (Reference ref){
		if (ref == null){
			return null;
		}
		else if (ref.getType().equals(ReferenceType.Article)){		return REF_ARTICLE;}
		else if (ref instanceof ISection){	return REF_PART_OF_OTHER_TITLE;}
		else if (ref.getType().equals(ReferenceType.Book)){	return REF_BOOK;}
		else if (ref.getType().equals(ReferenceType.Database)){	return REF_DATABASE;}
//		else if (ref instanceof SectionBas){	return REF_INFORMAL;}
//		else if (ref instanceof SectionBas){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.WebPage)){	return REF_WEBSITE;}
		else if (ref.getType().equals(ReferenceType.CdDvd)){	return REF_CD;}
		else if (ref.getType().equals(ReferenceType.Journal)){	return REF_JOURNAL;}
		else if (ref.getType().equals(ReferenceType.Generic)){	return REF_UNKNOWN;}
		else if (ref.getType().equals(ReferenceType.PrintSeries)){
			logger.warn("Print Series is not a standard Berlin Model category");
			return REF_PRINT_SERIES;
		}
		else if (ref.getType().equals(ReferenceType.Proceedings)){
			logger.warn("Proceedings is not a standard Berlin Model category");
			return REF_CONFERENCE_PROCEEDINGS;
		}
//		else if (ref instanceof ){	return REF_JOURNAL_VOLUME;}
		else if (ref.getType().equals(ReferenceType.Patent)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.PersonalCommunication)){	return REF_INFORMAL;}
		else if (ref.getType().equals(ReferenceType.Report)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.Thesis)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.Report)){	return REF_NOT_APPLICABLE;}

		else {
			//TODO Exception
			logger.warn("Reference type not yet supported in Berlin Model: "+ ref.getClass().getSimpleName());
			return null;
		}
	}


	public static Integer taxRelation2relPtQualifierFk (RelationshipBase<?,?,?> rel){
		if (rel == null){
			return null;
		}
//		else if (rel instanceof SynonymRelationship){
//			return ;
//		}else if (rel instanceof TaxonRelationship){
			RelationshipTermBase<?> type = rel.getType();
			if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {return TAX_REL_IS_INCLUDED_IN;
			}else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {return TAX_REL_IS_MISAPPLIED_NAME_OF;
			}else if (type.equals(TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR())) {return TAX_REL_IS_PRO_PARTE_MISAPPLIED_NAME_OF;
            }else if (type.equals(SynonymType.SYNONYM_OF())) {return TAX_REL_IS_SYNONYM_OF;
			}else if (type.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())) {return TAX_REL_IS_HOMOTYPIC_SYNONYM_OF;
			}else if (type.equals(SynonymType.HETEROTYPIC_SYNONYM_OF())) {return TAX_REL_IS_HETEROTYPIC_SYNONYM_OF;
			}else if (type.equals(TaxonRelationshipType.CONGRUENT_TO())) {return 11;
//			public static int TAX_REL_IS_PROPARTE_SYN_OF = 4;
//			public static int TAX_REL_IS_PARTIAL_SYN_OF = 5;
//			public static int TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF = 101;
//			public static int TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF = 102;
//			public static int TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF = 103;
//			public static int TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF = 104;

			}else {
				//TODO Exception
				logger.warn("Relationship type not yet supported by Berlin Model export: "+ rel.getType());
				return null;
		}
	}

	public static Integer nomStatus2nomStatusFk (NomenclaturalStatusType status){
		if (status == null){
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_NOM_CONS;

		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_NOM_NOV;

		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_COMB_INVAL;
		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by Berlin Model export: "+ status);
			return null;
		}
	}



	public static Integer nameRel2RelNameQualifierFk (RelationshipBase<?,?,?> rel){
		if (rel == null){
			return null;
		}
		RelationshipTermBase<?> type = rel.getType();
		if (type.equals(NameRelationshipType.BASIONYM())) {return NAME_REL_IS_BASIONYM_FOR;
		}else if (type.equals(NameRelationshipType.LATER_HOMONYM())) {return NAME_REL_IS_LATER_HOMONYM_OF;
		}else if (type.equals(NameRelationshipType.REPLACED_SYNONYM())) {return NAME_REL_IS_REPLACED_SYNONYM_FOR;
		//TODO
		}else if (type.equals(NameRelationshipType.VALIDATED_BY_NAME())) {return NAME_REL_IS_VALIDATION_OF;
		}else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {return NAME_REL_IS_LATER_VALIDATION_OF;
		}else if (type.equals(NameRelationshipType.CONSERVED_AGAINST())) {return NAME_REL_IS_CONSERVED_AGAINST;


		}else if (type.equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())) {return NAME_REL_IS_TREATED_AS_LATER_HOMONYM_OF;
		}else if (type.equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT())) {return NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
		}else {
			//TODO Exception
			logger.warn("Relationship type not yet supported by Berlin Model export: "+ rel.getType());
			return null;
	}

			//NameRelationShip

//	}else if (type.equals(NameRelationshipType.())) {return NAME_REL_IS_REJECTED_IN_FAVOUR_OF;

//			public static int NAME_REL_IS_FIRST_PARENT_OF = 9;
//			public static int NAME_REL_IS_SECOND_PARENT_OF = 10;
//			public static int NAME_REL_IS_FEMALE_PARENT_OF = 11;
//			public static int NAME_REL_IS_MALE_PARENT_OF = 12;
//
//			public static int NAME_REL_IS_REJECTED_IN_FAVOUR_OF = 14;
//	}else if (type.equals(NameRelationshipType.)) {return NAME_REL_IS_REJECTED_TYPE_OF;
//
//			public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
//			public static int NAME_REL_IS_LECTOTYPE_OF = 61;
//			public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;

		//	}else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {return NAME_REL_IS_TYPE_OF;


	}

	public static UUID getWebMarkerUuid (int markerCategoryId){
		if (markerCategoryId == 1){
			return UUID.fromString("d8554418-d1ae-471d-a1bd-a0cbc7ab860c");  //any as not to find in cichorieae
		}else if (markerCategoryId == 2){
			return UUID.fromString("7f189c48-8632-4870-9ec8-e4d2489f324e");
		}else if (markerCategoryId == 3){
			return UUID.fromString("9a115e6b-8210-4dd3-825a-6fed11016c63");
		}else if (markerCategoryId == 4){
			return UUID.fromString("1d287011-2054-41c5-a919-17ac1d0a9270");
		}else if (markerCategoryId == 9){
			return UUID.fromString("cc5eca5c-1ae5-4feb-9a95-507fc167b0c9");
		//Salvador
		}else if (markerCategoryId == 5){
			return UUID.fromString("7d8875b3-107c-4189-97e5-cadb470e543a");
		}else if (markerCategoryId == 20){
			return UUID.fromString("3574e2b0-6431-4d71-b456-bc967c80f622");
		}else if (markerCategoryId == 30){
			return UUID.fromString("9924b27e-0dbe-4d95-ae9b-096fbbc3edcb");
		}else if (markerCategoryId == 40){
			return UUID.fromString("69241b97-f4d2-4f60-9aed-1c4ccb5bced5");
		}else if (markerCategoryId == 50){
			return UUID.fromString("f3b62ce3-0212-4542-a74c-0c68d08859b1");
		}else if (markerCategoryId == 55){
			return UUID.fromString("bec822f2-8242-425f-ad46-f11f0b82f69b");
		}else if (markerCategoryId == 60){
			return UUID.fromString("722fca60-0416-4bf0-aa4b-a07400f9710d");
		}else if (markerCategoryId == 70){
			return UUID.fromString("ed57857b-1001-4b09-b48e-d88ab146bb2b");
		}else if (markerCategoryId == 80){
			return UUID.fromString("637e8b77-1202-462b-9d77-1023f3c192d9");
		}else if (markerCategoryId == 90){
			return UUID.fromString("c769c231-6e76-46df-88f7-2c459342a3c2");
		}else if (markerCategoryId == 93){
			return UUID.fromString("075ee97e-246f-4f3d-aa96-16930df6268c");
		}else if (markerCategoryId == 100){
			return UUID.fromString("e17065bf-3e44-417a-b05c-f82929f680c3");
		}else if (markerCategoryId == 110){
			return UUID.fromString("e2b8de07-770e-4fda-b445-c4735f4e95c8");
		}else if (markerCategoryId == 900){
			return UUID.fromString("d029c3c9-944a-4bb9-bba6-12c95da94a04");
		}else if (markerCategoryId == 920){
			return UUID.fromString("98af97b1-e5e3-4ee4-902e-15032316bfc3");
		}else if (markerCategoryId == 930){
			return UUID.fromString("4b6c3130-4e50-4f45-8461-d2698cf5f80b");
		}else if (markerCategoryId == 950){
			return UUID.fromString("1e53f58c-6528-42c6-99ae-0f75a3c3c264");
		}else if (markerCategoryId == 960){
			return UUID.fromString("1903d460-94cc-4fc4-b2a8-a3fb0cfd69a0");
		}else{
			logger.warn("Unknown webMarker category: " + markerCategoryId);
			return null;
		}

	}

}
