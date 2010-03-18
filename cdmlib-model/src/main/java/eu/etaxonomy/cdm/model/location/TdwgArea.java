/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 * @created 15.07.2008
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TdwgArea")
@XmlRootElement(name = "TdwgArea")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class TdwgArea extends NamedArea {
	private static final long serialVersionUID = 4662215686356109015L;
	private static final Logger logger = Logger.getLogger(TdwgArea.class);

	
	private static Map<String, UUID> abbrevMap = null;
	private static Map<String, UUID> labelMap = null;
	
	protected static Map<UUID, TdwgArea> termMap = null;		

	private static final UUID uuidABBREV_None = UUID.fromString("1fb40504-d1d7-44b0-9731-374fbe6cac77");
	private static final UUID uuidABBREV_1 = UUID.fromString("e860871c-3a14-4ef2-9367-bbd92586c95b");
	private static final UUID uuidABBREV_2 = UUID.fromString("9444016a-b334-4772-8795-ed4019552087");
	private static final UUID uuidABBREV_3 = UUID.fromString("e63e4a1a-b51b-4efa-bf99-c4a389d4aab3");
	private static final UUID uuidABBREV_4 = UUID.fromString("3ed00d6a-a186-48ff-bed2-860c9481ff7d");
	private static final UUID uuidABBREV_5 = UUID.fromString("1319898d-e0f4-4d95-a043-ba838ee7c652");
	private static final UUID uuidABBREV_6 = UUID.fromString("d7fa4cbf-b5cf-4d0a-bc3c-db8706657dfd");
	private static final UUID uuidABBREV_7 = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
	private static final UUID uuidABBREV_8 = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");
	private static final UUID uuidABBREV_9 = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");
	private static final UUID uuidABBREV_10 = UUID.fromString("22524ba2-6e57-4b71-89ab-89fc50fba6b4");
	private static final UUID uuidABBREV_11 = UUID.fromString("d292f237-da3d-408b-93a1-3257a8c80b97");
	private static final UUID uuidABBREV_12 = UUID.fromString("6e980d6d-5748-4535-ad7a-95d35542910d");
	private static final UUID uuidABBREV_13 = UUID.fromString("5f3059a8-a4fc-434c-b6f2-292919967553");
	private static final UUID uuidABBREV_14 = UUID.fromString("4f1abfb5-b166-47cf-a5bb-b0e6588f5389");
	private static final UUID uuidABBREV_20 = UUID.fromString("0a4bf6d1-52d6-4eff-b5d9-0b3c270291f1");
	private static final UUID uuidABBREV_21 = UUID.fromString("521ba4f9-0d74-4ec0-addc-fab9d72dca07");
	private static final UUID uuidABBREV_22 = UUID.fromString("931164ad-ec16-4133-afab-bdef25d67636");
	private static final UUID uuidABBREV_23 = UUID.fromString("1f29eaea-ef04-40ee-96bd-f2f433c78c8c");
	private static final UUID uuidABBREV_24 = UUID.fromString("b49bf097-5b36-47ff-8d8f-640bb14bdab2");
	private static final UUID uuidABBREV_25 = UUID.fromString("4b2e7043-640e-4bf4-8436-7436ed1f7137");
	private static final UUID uuidABBREV_26 = UUID.fromString("e4f41a32-a056-4628-94f2-23b3ad821135");
	private static final UUID uuidABBREV_27 = UUID.fromString("a1081898-e631-40b5-a225-fc24809adf8c");
	private static final UUID uuidABBREV_28 = UUID.fromString("06f367c2-e657-4894-8427-d164343dbd75");
	private static final UUID uuidABBREV_29 = UUID.fromString("b68c6158-96ff-4ef9-8fe6-d27cc4809d25");
	private static final UUID uuidABBREV_30 = UUID.fromString("6692581b-fa7a-4b13-ae39-b1a673d0b252");
	private static final UUID uuidABBREV_31 = UUID.fromString("ff2bb4b1-4190-4f04-b556-7c64bed0140a");
	private static final UUID uuidABBREV_32 = UUID.fromString("0cd59844-44e3-430c-9986-49edd1c338be");
	private static final UUID uuidABBREV_33 = UUID.fromString("f998eeb4-b105-4875-ae32-0de41cf389e4");
	private static final UUID uuidABBREV_34 = UUID.fromString("a8c5b44e-21ec-4d40-b847-cd1884968c92");
	private static final UUID uuidABBREV_35 = UUID.fromString("4b35ae1c-4f54-4818-a4c5-8323a3aea5fc");
	private static final UUID uuidABBREV_36 = UUID.fromString("16183741-4d45-48f4-a367-710b6ce82fee");
	private static final UUID uuidABBREV_37 = UUID.fromString("3dfa2c8d-5c09-44b1-8ee9-c5b0dad3c56f");
	private static final UUID uuidABBREV_38 = UUID.fromString("a883b11e-7ec4-486d-8ee3-2844a0bf1535");
	private static final UUID uuidABBREV_40 = UUID.fromString("7ceb3526-3fe2-4d4b-8b7c-e0a267b1a9a0");
	private static final UUID uuidABBREV_41 = UUID.fromString("4739866e-3919-4d76-875e-585d45be8c88");
	private static final UUID uuidABBREV_42 = UUID.fromString("07cd4677-f025-4bd3-80f6-7762056740ba");
	private static final UUID uuidABBREV_43 = UUID.fromString("173e9075-85c9-433d-9115-dc87d61d21f4");
	private static final UUID uuidABBREV_50 = UUID.fromString("37cc7b35-a580-47a2-aedf-bd06a5afe8e5");
	private static final UUID uuidABBREV_51 = UUID.fromString("cfc163fb-54e5-48a7-ab14-cc982323f0c5");
	private static final UUID uuidABBREV_60 = UUID.fromString("9f38ea6f-8f82-4697-9603-7877865caaac");
	private static final UUID uuidABBREV_61 = UUID.fromString("aa33248e-2a35-443c-923a-bf6a813fa9f7");
	private static final UUID uuidABBREV_62 = UUID.fromString("6a20ac07-161d-410c-9bca-87be8392306d");
	private static final UUID uuidABBREV_63 = UUID.fromString("ee23fe88-8ee2-47bf-a628-8f88a6e134e0");
	private static final UUID uuidABBREV_70 = UUID.fromString("09bc05d8-badb-4788-b1d8-47bd16fb475b");
	private static final UUID uuidABBREV_71 = UUID.fromString("519b890b-6b7d-4bfe-8112-6a80bd55102b");
	private static final UUID uuidABBREV_72 = UUID.fromString("56674a86-81bd-4bba-90a0-a73b95ca9a0a");
	private static final UUID uuidABBREV_73 = UUID.fromString("d264452e-995b-4e23-a11e-b55b22e43f19");
	private static final UUID uuidABBREV_74 = UUID.fromString("617ded5e-6b96-487a-b706-3fffc81104d9");
	private static final UUID uuidABBREV_75 = UUID.fromString("b6ed282f-76d1-44e3-aafb-2025fbaa001f");
	private static final UUID uuidABBREV_76 = UUID.fromString("32e8f7dd-76be-480e-8ff9-32ffd0be20eb");
	private static final UUID uuidABBREV_77 = UUID.fromString("8c7f2488-5b40-414e-b8de-e854755c69c4");
	private static final UUID uuidABBREV_78 = UUID.fromString("f1911c44-8925-4a8a-b25e-8d2379ac7195");
	private static final UUID uuidABBREV_79 = UUID.fromString("a4243f05-52b0-4c2f-8e2c-7650a1c5d8b0");
	private static final UUID uuidABBREV_80 = UUID.fromString("38128f22-3908-44aa-8c5d-cdc9570f29f0");
	private static final UUID uuidABBREV_81 = UUID.fromString("b5ef1405-89f6-455b-adb1-01dfbff84663");
	private static final UUID uuidABBREV_82 = UUID.fromString("97df9967-f9d0-4153-ab27-83abf7e508fd");
	private static final UUID uuidABBREV_83 = UUID.fromString("f25135c1-f77c-46a7-ac4f-7bad9446cfef");
	private static final UUID uuidABBREV_84 = UUID.fromString("85067b09-9bdf-4cb1-9ec6-95cf884aba63");
	private static final UUID uuidABBREV_85 = UUID.fromString("87964033-5404-42ac-8f18-09baa388c14e");
	private static final UUID uuidABBREV_90 = UUID.fromString("09714b3f-a0d1-479e-914e-3a207c9e1b19");
	private static final UUID uuidABBREV_91 = UUID.fromString("0670bfb2-5bb2-4bc5-8486-1eacc85a87b0");
	private static final UUID uuidABBREV_ABT = UUID.fromString("c31dadb8-0cda-4a6a-b7ec-2787d8a25c67");
	private static final UUID uuidABBREV_AFG = UUID.fromString("122977c7-d5e3-4d26-a658-162ce805d1a8");
	private static final UUID uuidABBREV_AGE = UUID.fromString("00ae742b-f8b1-4f24-8dd7-57c157676734");
	private static final UUID uuidABBREV_AGS = UUID.fromString("707c485f-dcad-41a5-bc8b-7c81d5249c9f");
	private static final UUID uuidABBREV_AGW = UUID.fromString("2cac2091-77d0-4a0d-a202-609cb64b096b");
	private static final UUID uuidABBREV_ALA = UUID.fromString("e097b877-517c-4308-9f4c-a6bfa928238d");
	private static final UUID uuidABBREV_ALB = UUID.fromString("4fdd8662-7635-4401-94e5-6a457a8593bb");
	private static final UUID uuidABBREV_ALD = UUID.fromString("4f890bf8-36e8-444a-92db-59976740ac61");
	private static final UUID uuidABBREV_ALG = UUID.fromString("73755400-0105-416c-b2f4-b10e3c55b0ec");
	private static final UUID uuidABBREV_ALT = UUID.fromString("8cfc1722-e1e8-49d3-95a7-9879de6de490");
	private static final UUID uuidABBREV_ALU = UUID.fromString("50b79b3c-41b2-4be3-a1cd-e7d360abfbec");
	private static final UUID uuidABBREV_AMU = UUID.fromString("fe4be234-5bbb-48ee-87c5-f0d893a34889");
	private static final UUID uuidABBREV_AND = UUID.fromString("6af14e8b-2e1e-4012-980c-9bfe59ddb59d");
	private static final UUID uuidABBREV_ANG = UUID.fromString("bac38903-3c49-4004-8566-50e684196b47");
	private static final UUID uuidABBREV_ANT = UUID.fromString("4f147ab5-9667-4406-a537-4ded4c4ccd63");
	private static final UUID uuidABBREV_ARI = UUID.fromString("9d3579a2-8bc6-4c72-904e-2961a6f84de1");
	private static final UUID uuidABBREV_ARK = UUID.fromString("ebb5d419-66f5-47d8-aa84-7d986a3d9dc8");
	private static final UUID uuidABBREV_ARU = UUID.fromString("747ebd42-2c2a-4db5-8a70-bc1d7a3026ab");
	private static final UUID uuidABBREV_ASC = UUID.fromString("c1a294af-54b1-43ac-8699-49ff1a166a08");
	private static final UUID uuidABBREV_ASK = UUID.fromString("72a08d3b-d333-4cb3-841e-c339a1e15172");
	private static final UUID uuidABBREV_ASP = UUID.fromString("dba9e336-a2b8-4332-baff-ef6fb592a30e");
	private static final UUID uuidABBREV_ASS = UUID.fromString("bac86590-6107-4bb5-b947-780a918b19a1");
	private static final UUID uuidABBREV_ATP = UUID.fromString("f7754519-6799-4dd8-bccf-d48370c6dc30");
	private static final UUID uuidABBREV_AUT = UUID.fromString("2f10ca0f-92bc-465b-a7da-dabd94f92e2a");
	private static final UUID uuidABBREV_AZO = UUID.fromString("4d176728-ada1-41f3-8b2c-123b8bf133e4");
	private static final UUID uuidABBREV_BAH = UUID.fromString("d9ebede1-ebe7-4b50-bb95-315e93fa7802");
	private static final UUID uuidABBREV_BAL = UUID.fromString("a17bc439-3ee0-4bd0-83b8-8ec975bebb60");
	private static final UUID uuidABBREV_BAN = UUID.fromString("6b8ee963-f7f6-4173-bda2-0dc1492edac1");
	private static final UUID uuidABBREV_BEN = UUID.fromString("6ddb07e2-e227-4111-8138-7d321530f0b2");
	private static final UUID uuidABBREV_BER = UUID.fromString("a58940b2-d7e9-4a99-b255-d3eee930f2b4");
	private static final UUID uuidABBREV_BGM = UUID.fromString("948264a0-fd65-4423-8a0a-2bf063a3995d");
	private static final UUID uuidABBREV_BIS = UUID.fromString("020043d9-33ee-4ed2-8411-7e744a28a8d9");
	private static final UUID uuidABBREV_BKN = UUID.fromString("dc3ff939-0271-40f0-b77f-c3dc911e8d5b");
	private static final UUID uuidABBREV_BLR = UUID.fromString("7c015a65-7cd0-49f5-8cc8-0832b56edf61");
	private static final UUID uuidABBREV_BLT = UUID.fromString("3c3fcc8a-9ea9-45c4-b929-b4cd3a269171");
	private static final UUID uuidABBREV_BLZ = UUID.fromString("e4c75529-237f-4e74-bf7d-31888c35b40c");
	private static final UUID uuidABBREV_BOL = UUID.fromString("5c993136-e6e8-4cb7-8628-5a3dc72e685d");
	private static final UUID uuidABBREV_BOR = UUID.fromString("e615e7e6-5b64-4903-b794-816baab689b3");
	private static final UUID uuidABBREV_BOT = UUID.fromString("fb4ab229-0054-43eb-a897-ef9b78c3c006");
	private static final UUID uuidABBREV_BOU = UUID.fromString("83512b5e-f591-4e70-9138-865ad9684475");
	private static final UUID uuidABBREV_BRC = UUID.fromString("6e961fee-a1f9-43f8-bd2c-9d7cefc0f3af");
	private static final UUID uuidABBREV_BRY = UUID.fromString("ecc89b7e-7425-4719-8704-f48a171fc5a9");
	private static final UUID uuidABBREV_BUL = UUID.fromString("6ca10d3d-50fe-4eb0-9027-4638178c7275");
	private static final UUID uuidABBREV_BUR = UUID.fromString("66be1451-ffa9-4c71-84ae-fa6ff6c52df0");
	private static final UUID uuidABBREV_BZC = UUID.fromString("72e32463-9fd7-40d4-8c32-5901cb7a8425");
	private static final UUID uuidABBREV_BZE = UUID.fromString("223be2d6-a7bb-4616-b0b6-3656cdd936c7");
	private static final UUID uuidABBREV_BZL = UUID.fromString("897e8a36-1201-44dc-aeea-7e11ce4599cb");
	private static final UUID uuidABBREV_BZN = UUID.fromString("50e0c0a8-d601-48a5-b6f7-ac1972a2bd8e");
	private static final UUID uuidABBREV_BZS = UUID.fromString("0e875804-b2e6-4dbe-941d-927d3539dd12");
	private static final UUID uuidABBREV_CAB = UUID.fromString("e34ba389-b603-4590-bd11-95e3715456e2");
	private static final UUID uuidABBREV_CAF = UUID.fromString("23e88955-3ee2-4d42-93ec-bd21f271e491");
	private static final UUID uuidABBREV_CAL = UUID.fromString("d569d6e0-5006-4636-b6f6-094e29dd8c52");
	private static final UUID uuidABBREV_CAY = UUID.fromString("efb2c2c5-c71a-4214-99e1-ff4b1ad68980");
	private static final UUID uuidABBREV_CBD = UUID.fromString("6d7a5e91-765e-4edd-8c25-71052cb0dcda");
	private static final UUID uuidABBREV_CGS = UUID.fromString("e2db82b4-45b7-4800-bc9c-708ef616495b");
	private static final UUID uuidABBREV_CHA = UUID.fromString("f92224d9-1754-4422-853e-e2bb5d981612");
	private static final UUID uuidABBREV_CHC = UUID.fromString("273e6b4f-22cf-442e-a19d-c0f9b1859e83");
	private static final UUID uuidABBREV_CHH = UUID.fromString("16977fc9-9309-4609-9776-13df4a12282e");
	private static final UUID uuidABBREV_CHI = UUID.fromString("719429fb-8f31-4bec-875e-ca5df76490ad");
	private static final UUID uuidABBREV_CHM = UUID.fromString("357360e8-2631-4f93-bc07-71e589cab7bb");
	private static final UUID uuidABBREV_CHN = UUID.fromString("8d75b680-9d66-4e82-b20a-9435ac4eeab5");
	private static final UUID uuidABBREV_CHQ = UUID.fromString("4b95d239-1c7d-45bd-87d0-c2db25f4d0b8");
	private static final UUID uuidABBREV_CHS = UUID.fromString("e9be3140-41ed-4f3b-85d2-883bee63b4a7");
	private static final UUID uuidABBREV_CHT = UUID.fromString("e626ea99-8e46-49c6-8c1a-96b234dffd47");
	private static final UUID uuidABBREV_CHX = UUID.fromString("7f8d9b4d-a1d3-4b74-9fa9-de45fd154db4");
	private static final UUID uuidABBREV_CKI = UUID.fromString("f17f271c-f95b-4db9-aa1f-31db1b99d79c");
	private static final UUID uuidABBREV_CLC = UUID.fromString("c0d9eb53-dccf-4963-b0d7-49c93cc9061b");
	private static final UUID uuidABBREV_CLM = UUID.fromString("8e7961af-b0bd-4959-8084-917e0e8e9b9a");
	private static final UUID uuidABBREV_CLN = UUID.fromString("03e4f2d6-3b1b-4b0c-8d64-ac81e02d6f1f");
	private static final UUID uuidABBREV_CLS = UUID.fromString("df168714-503f-4fd9-8813-01f23417c0c2");
	private static final UUID uuidABBREV_CMN = UUID.fromString("62c845f8-3d65-413b-a311-5bc9d6789a03");
	private static final UUID uuidABBREV_CNT = UUID.fromString("df0da18b-bb19-4e4a-a0af-eab75abde998");
	private static final UUID uuidABBREV_CNY = UUID.fromString("792e48a0-9b18-487a-b6da-33cbbb265873");
	private static final UUID uuidABBREV_COL = UUID.fromString("da98e966-4250-46ac-8624-c91dc05338dc");
	private static final UUID uuidABBREV_COM = UUID.fromString("3a02ef4b-f5df-4bd4-93c5-2ec4a24a8852");
	private static final UUID uuidABBREV_CON = UUID.fromString("da2f536b-6104-40b6-b198-b803118a1314");
	private static final UUID uuidABBREV_COO = UUID.fromString("c8f06fd3-616a-4d44-b57e-0a49a4d4c609");
	private static final UUID uuidABBREV_COR = UUID.fromString("91aaed46-a4a9-47dd-9bb7-8d254b3963cb");
	private static final UUID uuidABBREV_COS = UUID.fromString("bd243fa3-cd56-45c7-a445-1553e6e5e14d");
	private static final UUID uuidABBREV_CPI = UUID.fromString("8068a150-c71c-4e67-9e26-70c714bf6615");
	private static final UUID uuidABBREV_CPP = UUID.fromString("c755ce8e-3ad3-4e0e-bbff-7d108a933c58");
	private static final UUID uuidABBREV_CPV = UUID.fromString("546fc561-10b0-41f4-995f-cc1fe9fd8164");
	private static final UUID uuidABBREV_CRL = UUID.fromString("e37f35fc-53df-41f6-a510-af9cefd5e921");
	private static final UUID uuidABBREV_CRZ = UUID.fromString("c6582f9f-a0bb-4f76-a8c3-0562b78e723d");
	private static final UUID uuidABBREV_CTA = UUID.fromString("96886299-b8f5-4a44-abd7-3ee47c68bbe3");
	private static final UUID uuidABBREV_CTM = UUID.fromString("aadc5ca0-cf8e-48e5-a12e-8d34e6c7e4cb");
	private static final UUID uuidABBREV_CUB = UUID.fromString("12c3e2c6-432e-4087-bfe2-07c29df03c47");
	private static final UUID uuidABBREV_CVI = UUID.fromString("dccd00ae-9112-4c80-9591-09bd7ff7d4fd");
	private static final UUID uuidABBREV_CYP = UUID.fromString("da4cce9a-439b-4cc4-8073-85dc75bae169");
	private static final UUID uuidABBREV_CZE = UUID.fromString("234d24aa-963d-441e-afdc-a1cca6e85787");
	private static final UUID uuidABBREV_DEL = UUID.fromString("104ef871-3d5a-4e22-8ab3-05a4adec4641");
	private static final UUID uuidABBREV_DEN = UUID.fromString("2f755c36-e953-4f4a-bf9d-3128ec03ace8");
	private static final UUID uuidABBREV_DJI = UUID.fromString("04ec3382-0431-4430-afe8-1fc1c8ce38da");
	private static final UUID uuidABBREV_DOM = UUID.fromString("54d5b1d5-295a-4206-beaf-f56b1b3385a1");
	private static final UUID uuidABBREV_DSV = UUID.fromString("edf5e728-8e45-434d-ad82-50b39e2af896");
	private static final UUID uuidABBREV_EAI = UUID.fromString("3f0f9e9a-c65f-41c3-9f50-88393d43b48b");
	private static final UUID uuidABBREV_EAS = UUID.fromString("2288924d-3380-43b4-9787-143953c3d505");
	private static final UUID uuidABBREV_ECU = UUID.fromString("6de5f860-b77b-4a61-bbfe-b6718108cea0");
	private static final UUID uuidABBREV_EGY = UUID.fromString("9587540f-c501-47ad-b5bc-48e4c18eede4");
	private static final UUID uuidABBREV_EHM = UUID.fromString("edd055d7-76ea-4185-94da-3b44c4e03bd4");
	private static final UUID uuidABBREV_ELS = UUID.fromString("76604a11-1668-480f-9f29-498ff1b205fa");
	private static final UUID uuidABBREV_EQG = UUID.fromString("ccc35725-3334-4256-a0be-afc30df943f3");
	private static final UUID uuidABBREV_ERI = UUID.fromString("44651d37-9ad2-430a-be6b-3e7359f2da8a");
	private static final UUID uuidABBREV_ETH = UUID.fromString("66305c99-ffe4-4ecc-8d2e-8d1efe38c9ea");
	private static final UUID uuidABBREV_FAL = UUID.fromString("5f0cfd48-9afd-4704-8e5e-e58b377a5302");
	private static final UUID uuidABBREV_FIJ = UUID.fromString("09db7449-0d4a-4aed-911f-f88a5c57f93f");
	private static final UUID uuidABBREV_FIN = UUID.fromString("bd2d1a58-6e2f-4501-8d94-4b01615cf8d2");
	private static final UUID uuidABBREV_FLA = UUID.fromString("67fb45d0-3a51-48ba-875a-7694fb566f28");
	private static final UUID uuidABBREV_FOR = UUID.fromString("54e966a4-9a64-451c-9329-e2a45080f365");
	private static final UUID uuidABBREV_FRA = UUID.fromString("37529096-5d47-4926-8682-4fa8311d0ef4");
	private static final UUID uuidABBREV_FRG = UUID.fromString("84255b9c-ebd8-4f25-a18e-fb1e9343d5ba");
	private static final UUID uuidABBREV_GAB = UUID.fromString("1ec3e5c2-bdd8-4656-aa8a-282966667c68");
	private static final UUID uuidABBREV_GAL = UUID.fromString("108b8834-f38c-478c-9e5a-19e0926baaeb");
	private static final UUID uuidABBREV_GAM = UUID.fromString("7147ccee-4f7c-40b7-a406-63174617fbf8");
	private static final UUID uuidABBREV_GEO = UUID.fromString("5b8e5984-02da-47b3-b76a-3dec7abb75d4");
	private static final UUID uuidABBREV_GER = UUID.fromString("7b7c2db5-aa44-4302-bdec-6556fd74b0b9");
	private static final UUID uuidABBREV_GGI = UUID.fromString("404795df-e4c2-4c01-944c-2b8f7a900595");
	private static final UUID uuidABBREV_GHA = UUID.fromString("36f2a8a1-af75-4dd2-8376-a4535c6bb697");
	private static final UUID uuidABBREV_GIL = UUID.fromString("e724a05c-c53c-427c-b42c-32ca323d13f9");
	private static final UUID uuidABBREV_GNB = UUID.fromString("5f551283-af06-40cb-9f3f-3cccfb39d31b");
	private static final UUID uuidABBREV_GNL = UUID.fromString("f089837d-5d85-4e3c-8324-7111ae020b1d");
	private static final UUID uuidABBREV_GRB = UUID.fromString("efdb3684-9f82-4d9d-ae83-f262a4156be2");
	private static final UUID uuidABBREV_GRC = UUID.fromString("65c9b84e-dd19-4842-816a-e4e7a4b5143f");
	private static final UUID uuidABBREV_GST = UUID.fromString("8f2551f2-67fa-46fc-8a81-1de160171798");
	private static final UUID uuidABBREV_GUA = UUID.fromString("baa26afa-8dac-4a7b-b5b9-fca460ac3958");
	private static final UUID uuidABBREV_GUI = UUID.fromString("a742ebff-609b-4168-a85b-1825f4da98b4");
	private static final UUID uuidABBREV_GUY = UUID.fromString("4faf248e-8a45-42c8-b5f3-94b34b4e3794");
	private static final UUID uuidABBREV_HAI = UUID.fromString("460e57fa-9465-47bd-bf6e-c46dd29d6cdd");
	private static final UUID uuidABBREV_HAW = UUID.fromString("bda3f9fb-30cf-43fd-8d17-5e4d69545ed5");
	private static final UUID uuidABBREV_HBI = UUID.fromString("37f6aaf2-a6bb-4f4c-a4ad-aa25074babbf");
	private static final UUID uuidABBREV_HMD = UUID.fromString("e6e00b4e-5417-46aa-b2ed-b123e4a94fc7");
	private static final UUID uuidABBREV_HON = UUID.fromString("4e22114e-2990-429d-83af-252d428a11e8");
	private static final UUID uuidABBREV_HUN = UUID.fromString("93ab78d4-4c7e-4316-84f1-4dccbc70d71f");
	private static final UUID uuidABBREV_ICE = UUID.fromString("836cae07-03f0-4c1e-ac67-86c6f6457ac9");
	private static final UUID uuidABBREV_IDA = UUID.fromString("307e943a-1e0b-48e8-b4e1-34ebd67a1efe");
	private static final UUID uuidABBREV_ILL = UUID.fromString("3864694a-e0d7-40f5-b9db-0eecde047e9e");
	private static final UUID uuidABBREV_IND = UUID.fromString("235ff30d-2a25-4e96-b155-0fc0d08ec685");
	private static final UUID uuidABBREV_INI = UUID.fromString("51a2af3d-7a01-41a8-be18-7896eaf147ff");
	private static final UUID uuidABBREV_IOW = UUID.fromString("e480fd06-ec60-4a0f-81fd-faf832b06715");
	private static final UUID uuidABBREV_IRE = UUID.fromString("a9eead79-a85b-462b-a292-c0fb2929a3e5");
	private static final UUID uuidABBREV_IRK = UUID.fromString("18f7a5a9-739a-44bd-925c-68609d1c1835");
	private static final UUID uuidABBREV_IRN = UUID.fromString("33a2472d-d86b-45b1-83c0-e4cd78fb4915");
	private static final UUID uuidABBREV_IRQ = UUID.fromString("9e765dbf-aebb-47af-a193-a849f9daa63b");
	private static final UUID uuidABBREV_ITA = UUID.fromString("16444bc5-a042-4cd8-9d81-0eefd7925894");
	private static final UUID uuidABBREV_IVO = UUID.fromString("c9c406ee-0747-45b0-bbe7-e3045d468b5d");
	private static final UUID uuidABBREV_JAM = UUID.fromString("6e72dede-e94b-4051-bf96-348add8135e6");
	private static final UUID uuidABBREV_JAP = UUID.fromString("5446b13f-0cdd-4f60-9f17-444606f7678d");
	private static final UUID uuidABBREV_JAW = UUID.fromString("32708172-de90-418a-8637-1b29ca8ff22b");
	private static final UUID uuidABBREV_JNF = UUID.fromString("863a2c93-af7d-46fa-b8ec-9b081c972caa");
	private static final UUID uuidABBREV_KAM = UUID.fromString("c39e7ff9-2b52-4e54-82c4-de836ab94f32");
	private static final UUID uuidABBREV_KAN = UUID.fromString("57aa7e14-0e06-45ba-87b1-5f78c022134e");
	private static final UUID uuidABBREV_KAZ = UUID.fromString("8c815ecb-c0ac-49dc-bf9a-6af9fded206f");
	private static final UUID uuidABBREV_KEG = UUID.fromString("ae1a9ce9-8d54-48c8-bdc8-4ad49df5e2b5");
	private static final UUID uuidABBREV_KEN = UUID.fromString("a7947714-fb33-4aca-80ce-f418f8b89f55");
	private static final UUID uuidABBREV_KER = UUID.fromString("815a67da-1a22-45d3-bef3-65333795b509");
	private static final UUID uuidABBREV_KGZ = UUID.fromString("c0f1305e-95b1-4501-9bc9-90c5c3b800b5");
	private static final UUID uuidABBREV_KHA = UUID.fromString("722f6cba-f21a-4f4d-8151-173d3377fa04");
	private static final UUID uuidABBREV_KOR = UUID.fromString("09dc1f07-569c-4101-8ff1-0ae1e9046cfe");
	private static final UUID uuidABBREV_KRA = UUID.fromString("05eb3ea9-cc10-4704-9b77-17ce57dca797");
	private static final UUID uuidABBREV_KRI = UUID.fromString("eb0ecf71-52c9-420d-8b04-bd5f619a606c");
	private static final UUID uuidABBREV_KRY = UUID.fromString("690bd6d6-1132-4428-8bd4-a260c8b402b5");
	private static final UUID uuidABBREV_KTY = UUID.fromString("b839ad86-c11d-4e2b-b94c-5b8e56ec0190");
	private static final UUID uuidABBREV_KUR = UUID.fromString("b69dc332-a200-4185-90d6-56dee024f179");
	private static final UUID uuidABBREV_KUW = UUID.fromString("7161a2d8-cb8f-4dc5-9521-44daf5dc0a64");
	private static final UUID uuidABBREV_KZN = UUID.fromString("7697395d-bece-4d42-b5a9-af6d7e29875d");
	private static final UUID uuidABBREV_LAB = UUID.fromString("6828f3aa-613f-413d-b1f5-5f742b3ced2c");
	private static final UUID uuidABBREV_LAO = UUID.fromString("db3cb12f-d198-47f5-9ccf-da63e5325787");
	private static final UUID uuidABBREV_LBR = UUID.fromString("af7dc765-068d-4748-8dbd-f0dbf606285e");
	private static final UUID uuidABBREV_LBS = UUID.fromString("3cdf4b9f-ab0d-475c-a8ad-abaf50b6bc51");
	private static final UUID uuidABBREV_LBY = UUID.fromString("5de71808-0572-4513-ac0f-f21216132b99");
	private static final UUID uuidABBREV_LDV = UUID.fromString("ce0b678a-f762-4e81-af58-0bce22604dec");
	private static final UUID uuidABBREV_LEE = UUID.fromString("3429522d-f0c1-47ee-a2ed-5e7b11027b6a");
	private static final UUID uuidABBREV_LES = UUID.fromString("ec62407d-d0e1-4885-aa58-947ea319ce4c");
	private static final UUID uuidABBREV_LIN = UUID.fromString("c2537c9b-5f5f-4e49-975b-9098aa696232");
	private static final UUID uuidABBREV_LOU = UUID.fromString("ceaadd5d-0a56-4297-a583-a887d104bb68");
	private static final UUID uuidABBREV_LSI = UUID.fromString("2ddb5919-9b24-4429-b1b5-5b109ce32916");
	private static final UUID uuidABBREV_MAG = UUID.fromString("f9516ee3-c0d5-4d7c-bdb2-18629044c015");
	private static final UUID uuidABBREV_MAI = UUID.fromString("df0584e0-e291-4bbb-8f31-5dec7f8f8c11");
	private static final UUID uuidABBREV_MAN = UUID.fromString("af20dad3-d152-4b50-b862-60c01da19aa5");
	private static final UUID uuidABBREV_MAQ = UUID.fromString("8082a407-d26a-4f8f-b5ca-2d3fb00b5086");
	private static final UUID uuidABBREV_MAS = UUID.fromString("7173ffcc-d91b-4c15-a563-b1c69342945f");
	private static final UUID uuidABBREV_MAU = UUID.fromString("81837e67-e449-4434-b088-fa849c434344");
	private static final UUID uuidABBREV_MCI = UUID.fromString("4340ff7b-d1ba-4d0e-8857-3cf460886615");
	private static final UUID uuidABBREV_MCS = UUID.fromString("41d2ddce-22bb-4e06-a58a-377239113436");
	private static final UUID uuidABBREV_MDG = UUID.fromString("7442406d-0bed-4d0d-a959-b16d81c3e161");
	private static final UUID uuidABBREV_MDR = UUID.fromString("c7377fb4-abb1-4afd-96fb-111cac62c8f3");
	private static final UUID uuidABBREV_MDV = UUID.fromString("cd604ba0-d10b-44ac-acd9-7ec4d5370d5e");
	private static final UUID uuidABBREV_MIC = UUID.fromString("68fd0c55-a9e2-4184-ab52-9d1702b02bb7");
	private static final UUID uuidABBREV_MIN = UUID.fromString("03d1211d-d056-46c1-998f-c2514826eafd");
	private static final UUID uuidABBREV_MLI = UUID.fromString("29e6f3e7-ffa7-403a-8051-7d18e67e7a45");
	private static final UUID uuidABBREV_MLW = UUID.fromString("a09998ce-f3a8-4496-947e-518466697e99");
	private static final UUID uuidABBREV_MLY = UUID.fromString("7047b137-84f4-46b5-8b71-4469e3d75545");
	private static final UUID uuidABBREV_MNT = UUID.fromString("e8d3d3c6-c6e7-4dc6-b44a-77be24c97c42");
	private static final UUID uuidABBREV_MOL = UUID.fromString("fef7aa18-e35b-4ec4-8ba3-ed44033a38d4");
	private static final UUID uuidABBREV_MON = UUID.fromString("6b678353-a83e-44e1-92b0-c31433208f2e");
	private static final UUID uuidABBREV_MOR = UUID.fromString("85f6595b-c739-4a00-87a6-06dbf591d5f5");
	private static final UUID uuidABBREV_MOZ = UUID.fromString("9ebd353e-0ab6-4185-9a9f-af92e7d11567");
	private static final UUID uuidABBREV_MPE = UUID.fromString("7a391925-b331-4ffe-8bf5-ba5b2439a177");
	private static final UUID uuidABBREV_MRN = UUID.fromString("4f854b84-2d93-4b0d-a40d-9e05be7cbae1");
	private static final UUID uuidABBREV_MRQ = UUID.fromString("77e49b33-ce39-48e8-bef5-eaa16091f2ef");
	private static final UUID uuidABBREV_MRS = UUID.fromString("e4836aef-cb88-4782-8c41-aefc270a2502");
	private static final UUID uuidABBREV_MRY = UUID.fromString("72fe14e6-5631-4079-ac95-3d62fe1c3aab");
	private static final UUID uuidABBREV_MSI = UUID.fromString("3c90742c-f8bf-4344-9f23-2e3b73aee064");
	private static final UUID uuidABBREV_MSO = UUID.fromString("0249d5ed-e960-4efa-b66d-5f2bd15f7b1b");
	private static final UUID uuidABBREV_MTN = UUID.fromString("548abebd-9668-481e-a2c5-a93a600fedb9");
	private static final UUID uuidABBREV_MXC = UUID.fromString("ebeb5340-2838-4b72-99b1-616e8160d0ca");
	private static final UUID uuidABBREV_MXE = UUID.fromString("bf85121c-a686-46be-ba88-f400b37b3b25");
	private static final UUID uuidABBREV_MXG = UUID.fromString("7d3e6e07-245d-4201-a525-10802cda6dfe");
	private static final UUID uuidABBREV_MXI = UUID.fromString("adac7ea3-9960-411b-af26-246fa85ea19c");
	private static final UUID uuidABBREV_MXN = UUID.fromString("757b5d76-1d1d-4542-9c99-fa1640dce066");
	private static final UUID uuidABBREV_MXS = UUID.fromString("5c95c7c9-29cd-4e8a-85c6-cb9ac2060dde");
	private static final UUID uuidABBREV_MXT = UUID.fromString("602303d7-93ee-4375-9095-477aad95268a");
	private static final UUID uuidABBREV_MYA = UUID.fromString("e631e1d6-bdc4-4ece-a2b5-33b1eb22b069");
	private static final UUID uuidABBREV_NAM = UUID.fromString("642bfe17-30c1-420c-be51-665af6a07bff");
	private static final UUID uuidABBREV_NAT = UUID.fromString("da51d835-bdb4-46bd-b994-5bb9a33ff5e7");
	private static final UUID uuidABBREV_NBR = UUID.fromString("28369e9a-251e-4735-b9b8-4271b1321808");
	private static final UUID uuidABBREV_NCA = UUID.fromString("7a3946b0-104c-4cd2-af95-b24a4ce6d686");
	private static final UUID uuidABBREV_NCB = UUID.fromString("e301f2a0-2a7c-4b6a-bd86-df15ba340a5d");
	private static final UUID uuidABBREV_NCS = UUID.fromString("08ba6204-b699-4b91-8e16-424ed62dcc84");
	private static final UUID uuidABBREV_NDA = UUID.fromString("891381d8-cdd2-49b2-86dd-02eeaf47dc7b");
	private static final UUID uuidABBREV_NEB = UUID.fromString("70b98a5c-4785-4925-9325-ec92610b9117");
	private static final UUID uuidABBREV_NEP = UUID.fromString("145cb777-fd41-4ed5-b163-550784840bf7");
	private static final UUID uuidABBREV_NET = UUID.fromString("2ef12677-0d38-4ed9-a57a-f61a968f7a63");
	private static final UUID uuidABBREV_NEV = UUID.fromString("9bac52d4-5a10-4927-b630-b51273b9943b");
	private static final UUID uuidABBREV_NFK = UUID.fromString("c35e4381-1646-4632-ab6f-40c80127b490");
	private static final UUID uuidABBREV_NFL = UUID.fromString("61e4dc79-5847-4859-8f63-fa602de5743c");
	private static final UUID uuidABBREV_NGA = UUID.fromString("36cc02ea-68fc-4024-879b-df2ad7e1d500");
	private static final UUID uuidABBREV_NGR = UUID.fromString("2fd151ae-c0c5-4653-95ef-42c4addf088b");
	private static final UUID uuidABBREV_NIC = UUID.fromString("0cff1981-6dbb-418b-b5e5-b427e338f2c3");
	private static final UUID uuidABBREV_NLA = UUID.fromString("5bce0bb4-a584-4e3a-bfd9-7aa876e1ef44");
	private static final UUID uuidABBREV_NNS = UUID.fromString("7a4d50ba-1b53-4ed7-8453-5ae17919df86");
	private static final UUID uuidABBREV_NOR = UUID.fromString("4b2396e7-6e3d-41f8-b782-247a95d4b761");
	private static final UUID uuidABBREV_NRU = UUID.fromString("01176406-0e8f-4a43-a6e2-346f6769cd60");
	private static final UUID uuidABBREV_NSC = UUID.fromString("7662bec4-dc55-4483-8f37-a78d93cc8224");
	private static final UUID uuidABBREV_NSW = UUID.fromString("e8c18809-0ccf-4c68-9528-9925db6c55e7");
	private static final UUID uuidABBREV_NTA = UUID.fromString("d1c88525-0110-4f9d-a639-5b4d0009b393");
	private static final UUID uuidABBREV_NUE = UUID.fromString("ec2ae973-99e6-44ba-8417-e01e729e5dab");
	private static final UUID uuidABBREV_NUN = UUID.fromString("0936c6be-e397-4793-b47b-e08d992ba5d3");
	private static final UUID uuidABBREV_NWC = UUID.fromString("e9d6e328-6209-4d1f-94f0-61fd8592c09c");
	private static final UUID uuidABBREV_NWG = UUID.fromString("b5abcf7d-796f-431a-bf93-b771693ccdcf");
	private static final UUID uuidABBREV_NWH = UUID.fromString("36d4ef65-64d3-4733-8085-9daeba3b1aed");
	private static final UUID uuidABBREV_NWJ = UUID.fromString("ab3974aa-b195-4af7-8f19-b1049231ed4f");
	private static final UUID uuidABBREV_NWM = UUID.fromString("3a0a1cc3-8a71-43fb-86fc-0c92ddba73fc");
	private static final UUID uuidABBREV_NWT = UUID.fromString("86b0de03-5b68-454b-b7db-d20e6a7b0f20");
	private static final UUID uuidABBREV_NWY = UUID.fromString("0392fb7c-2028-40db-8372-4621a425fe3c");
	private static final UUID uuidABBREV_NZN = UUID.fromString("df68a983-a1ac-42f7-886d-9335c681430c");
	private static final UUID uuidABBREV_NZS = UUID.fromString("7f285f4c-a7e7-4021-98dd-1413930675a1");
	private static final UUID uuidABBREV_OFS = UUID.fromString("27f18891-4c9c-4e72-98a0-b3a90d8f0060");
	private static final UUID uuidABBREV_OGA = UUID.fromString("178f0de3-54bf-4469-a8d4-8e1974be8611");
	private static final UUID uuidABBREV_OHI = UUID.fromString("461ba6ca-4246-4fb9-bcd4-af7c965ae777");
	private static final UUID uuidABBREV_OKL = UUID.fromString("56e84e9c-9229-4286-b30c-5c5e68c8a144");
	private static final UUID uuidABBREV_OMA = UUID.fromString("b3dd0577-35c4-4915-a965-7c696dc9b83c");
	private static final UUID uuidABBREV_ONT = UUID.fromString("2842bcb3-9ce5-473f-b155-2d7519b3bb65");
	private static final UUID uuidABBREV_ORE = UUID.fromString("f634bb85-3c54-4ee5-815c-da689d840a57");
	private static final UUID uuidABBREV_PAK = UUID.fromString("de7db6b3-7d4f-47a5-8bb8-9fa93a760672");
	private static final UUID uuidABBREV_PAL = UUID.fromString("a6249157-4b39-49d2-9689-0778aec024c4");
	private static final UUID uuidABBREV_PAN = UUID.fromString("07aced24-02ed-4e0b-80f0-b20b83fd6561");
	private static final UUID uuidABBREV_PAR = UUID.fromString("97d0d28f-977e-41bd-a3b9-f788baaed364");
	private static final UUID uuidABBREV_PEI = UUID.fromString("2ad64695-b454-49af-8b48-92f891175cba");
	private static final UUID uuidABBREV_PEN = UUID.fromString("57343e24-9bf9-4ce6-aed5-27d563139d36");
	private static final UUID uuidABBREV_PER = UUID.fromString("eb69081d-1147-48a1-84e4-b96339a29492");
	private static final UUID uuidABBREV_PHI = UUID.fromString("a5a7e2f3-5c24-4b9c-b6ba-110b62896c95");
	private static final UUID uuidABBREV_PHX = UUID.fromString("adf502c8-d5ba-4f07-a65b-115c3b8f4c21");
	private static final UUID uuidABBREV_PIT = UUID.fromString("717549a3-1f67-4408-8c59-42724e6d8c0f");
	private static final UUID uuidABBREV_POL = UUID.fromString("587b53b3-c12d-411c-aa58-1f8ab1c19987");
	private static final UUID uuidABBREV_POR = UUID.fromString("3a22e22e-a68e-4215-8b7d-be3e39cfafa4");
	private static final UUID uuidABBREV_PRM = UUID.fromString("51b66943-c7b6-4b0f-8252-92e02eb5957f");
	private static final UUID uuidABBREV_PUE = UUID.fromString("4d9400b1-de4e-495b-8a04-9468e34b0610");
	private static final UUID uuidABBREV_QLD = UUID.fromString("857bec6a-b361-495b-abf1-f99cce2587c4");
	private static final UUID uuidABBREV_QUE = UUID.fromString("c72bf61f-5446-455c-bd73-652ce4ddf1b9");
	private static final UUID uuidABBREV_REU = UUID.fromString("24fd734c-aeb2-464f-bc25-db1d09e62084");
	private static final UUID uuidABBREV_RHO = UUID.fromString("beaff32e-3e14-4b21-8d70-5b6930077299");
	private static final UUID uuidABBREV_ROD = UUID.fromString("4f10f141-b2bf-4d11-b018-9ed973b32729");
	private static final UUID uuidABBREV_ROM = UUID.fromString("5120891c-0493-4b0d-8319-f6442765ec3b");
	private static final UUID uuidABBREV_RUC = UUID.fromString("27be3967-faa0-45b2-a182-6bb19ace908b");
	private static final UUID uuidABBREV_RUE = UUID.fromString("d06f68f8-5a20-4a93-88e0-09f533f3613f");
	private static final UUID uuidABBREV_RUN = UUID.fromString("9169948b-c465-47b6-8acf-c9c3b0a6fff8");
	private static final UUID uuidABBREV_RUS = UUID.fromString("8957c768-ee18-410d-9c43-c55cb799e205");
	private static final UUID uuidABBREV_RUW = UUID.fromString("d145aa3f-857c-4b6e-b691-ce8d172f37d9");
	private static final UUID uuidABBREV_RWA = UUID.fromString("50d2716e-328c-4fe1-9e9b-c3ed6ce60bb5");
	private static final UUID uuidABBREV_SAK = UUID.fromString("37622300-94ec-4438-8e65-09d299fb7ef3");
	private static final UUID uuidABBREV_SAM = UUID.fromString("1762ef94-4195-4068-8703-4d11e244653f");
	private static final UUID uuidABBREV_SAR = UUID.fromString("43bf5a60-eafc-440d-83e0-7dce0961aba8");
	private static final UUID uuidABBREV_SAS = UUID.fromString("7dfafd0c-5b59-45f1-a2d2-0757e555e530");
	private static final UUID uuidABBREV_SAU = UUID.fromString("7af350e4-7e8a-49b7-a728-3dfb3a7347ac");
	private static final UUID uuidABBREV_SCA = UUID.fromString("3dc78d7b-6cd8-4790-891d-e8d6ffa497bb");
	private static final UUID uuidABBREV_SCI = UUID.fromString("67f0df67-8161-448b-9188-058b57d37c9d");
	private static final UUID uuidABBREV_SCS = UUID.fromString("ef6cd955-105b-442c-95b0-6637545baaa5");
	private static final UUID uuidABBREV_SCZ = UUID.fromString("353f7509-cc51-4c47-b46a-15819bca9323");
	private static final UUID uuidABBREV_SDA = UUID.fromString("9ca35d05-ec92-41fe-ba42-ec6828ff6848");
	private static final UUID uuidABBREV_SEL = UUID.fromString("f3cde575-de0d-49f9-9615-6717526458ba");
	private static final UUID uuidABBREV_SEN = UUID.fromString("4a95dae4-c8a8-42e8-ba12-540d402e360f");
	private static final UUID uuidABBREV_SEY = UUID.fromString("89392d7c-251e-4160-8518-6f91d86e5010");
	private static final UUID uuidABBREV_SGE = UUID.fromString("dda86ec7-b0fc-44b9-89a9-fd14762796bf");
	private static final UUID uuidABBREV_SIC = UUID.fromString("4750f5f8-e7af-46d8-b497-2b68199366ba");
	private static final UUID uuidABBREV_SIE = UUID.fromString("ea7c2f1b-9d95-4129-b84f-9653e51e3da8");
	private static final UUID uuidABBREV_SIN = UUID.fromString("7720f433-5f3f-469a-b49b-b4e409064455");
	private static final UUID uuidABBREV_SOA = UUID.fromString("c28b59c7-f65d-469e-9d53-6fce34b35e6e");
	private static final UUID uuidABBREV_SOC = UUID.fromString("d8a1ea81-78db-46db-a27d-d8eb64bf7e41");
	private static final UUID uuidABBREV_SOL = UUID.fromString("4cf239da-1215-4689-8cb9-495a2735cb0a");
	private static final UUID uuidABBREV_SOM = UUID.fromString("15b4c85f-6e38-403e-a5d4-69cdc00230ea");
	private static final UUID uuidABBREV_SPA = UUID.fromString("4774d040-9901-4eed-aaa5-8b18dcd568bc");
	private static final UUID uuidABBREV_SRL = UUID.fromString("3a78d641-42c2-4041-919d-fe3601de6356");
	private static final UUID uuidABBREV_SSA = UUID.fromString("bf83496e-9a97-4215-839a-3df00e06916e");
	private static final UUID uuidABBREV_STH = UUID.fromString("7145cc3e-0893-442d-b56e-9c74f62ce430");
	private static final UUID uuidABBREV_SUD = UUID.fromString("08c2d94a-0cc2-4b6b-8184-a1c1b899b805");
	private static final UUID uuidABBREV_SUL = UUID.fromString("b42f9b9e-afe1-4d62-a3a9-6b53599f2f60");
	private static final UUID uuidABBREV_SUM = UUID.fromString("d7bbe237-0bae-4ef0-b8fb-332a90eee92c");
	private static final UUID uuidABBREV_SUR = UUID.fromString("ff18c8ac-970f-4a90-a6a8-27859dbc6218");
	private static final UUID uuidABBREV_SVA = UUID.fromString("34c3c6fc-e6e0-44b9-8886-157c992e9485");
	private static final UUID uuidABBREV_SWC = UUID.fromString("7ad3ccea-3cee-4046-b496-e694a2109fd9");
	private static final UUID uuidABBREV_SWE = UUID.fromString("289074c4-a7a9-4cb3-b74a-78ad2bc6fbce");
	private static final UUID uuidABBREV_SWI = UUID.fromString("df9fc338-1564-4517-8e39-43087bc068f2");
	private static final UUID uuidABBREV_SWZ = UUID.fromString("5d49cc9a-3a8d-44a3-a851-6cf0e8ca154b");
	private static final UUID uuidABBREV_TAI = UUID.fromString("eed9dbda-ed03-41d5-9322-94f985de97ee");
	private static final UUID uuidABBREV_TAN = UUID.fromString("caf1697c-10d1-4d51-ac62-a977171ec56e");
	private static final UUID uuidABBREV_TAS = UUID.fromString("4baeb0f7-7893-4edb-8c9b-7c0f225bf868");
	private static final UUID uuidABBREV_TCI = UUID.fromString("952ea175-c3f4-4ae9-b09a-029ba81ee923");
	private static final UUID uuidABBREV_TCS = UUID.fromString("ac7a6544-3f4b-463b-a19d-eea0a484da93");
	private static final UUID uuidABBREV_TDC = UUID.fromString("1cda9cee-21be-4a99-a983-520a749d7118");
	private static final UUID uuidABBREV_TEN = UUID.fromString("d1a6df1e-7e01-4ef8-9e34-295bd59037de");
	private static final UUID uuidABBREV_TEX = UUID.fromString("6d90e6d7-9f3d-48ee-8dc5-0fa0e2e102e0");
	private static final UUID uuidABBREV_THA = UUID.fromString("da8aac48-9795-4767-a1a0-323160158892");
	private static final UUID uuidABBREV_TKM = UUID.fromString("bfca42eb-4fae-4233-996d-ffff6b60bf15");
	private static final UUID uuidABBREV_TOG = UUID.fromString("39983171-9173-48ac-9564-9d30c189b90f");
	private static final UUID uuidABBREV_TOK = UUID.fromString("24ffcae2-2090-4106-8da6-f49a21a951de");
	private static final UUID uuidABBREV_TON = UUID.fromString("a3a4d577-ff17-4548-b766-c0b3579cde1c");
	private static final UUID uuidABBREV_TRT = UUID.fromString("a356333f-e25a-4fdd-8d69-154c24307a36");
	private static final UUID uuidABBREV_TUA = UUID.fromString("8ae1077d-ce28-4602-909a-227686b680e1");
	private static final UUID uuidABBREV_TUB = UUID.fromString("332abf62-47ab-43f1-a12a-3e320f4a8a6d");
	private static final UUID uuidABBREV_TUE = UUID.fromString("0cc0f22f-df09-48d2-a2e0-27911df17c8b");
	private static final UUID uuidABBREV_TUN = UUID.fromString("73d5c7bf-deb8-4152-b55c-5414db2c4261");
	private static final UUID uuidABBREV_TUR = UUID.fromString("48219cbc-82ab-447f-8a67-e97408736c23");
	private static final UUID uuidABBREV_TUV = UUID.fromString("df5a43be-4dbd-4585-889d-916dc4eb085c");
	private static final UUID uuidABBREV_TVA = UUID.fromString("b59ea7bf-6907-4275-9984-4a9b7621cae9");
	private static final UUID uuidABBREV_TVL = UUID.fromString("3d190a52-47b9-450d-b2b8-15bd9995c8e5");
	private static final UUID uuidABBREV_TZK = UUID.fromString("90b1b04c-7604-4bdf-b648-963e933b85f8");
	private static final UUID uuidABBREV_UGA = UUID.fromString("e06b10c6-1cb6-4654-94b0-49878cf88a6a");
	private static final UUID uuidABBREV_UKR = UUID.fromString("670977b8-9833-4ab4-8f68-15a5cb5f38d9");
	private static final UUID uuidABBREV_URU = UUID.fromString("4b8f9064-f413-41f5-8399-8a8530fea981");
	private static final UUID uuidABBREV_UTA = UUID.fromString("86d51169-ef60-4717-b4ec-c1522ebd4bb9");
	private static final UUID uuidABBREV_UZB = UUID.fromString("42fb1397-83df-4fe0-a313-a04845ce378c");
	private static final UUID uuidABBREV_VAN = UUID.fromString("205587f2-6d1b-4507-8d06-3588375b0cf2");
	private static final UUID uuidABBREV_VEN = UUID.fromString("a9a80485-407a-495d-9701-2bfff3025b37");
	private static final UUID uuidABBREV_VER = UUID.fromString("889993e0-507b-48c6-bc40-8bb2dd943960");
	private static final UUID uuidABBREV_VIC = UUID.fromString("62f519dc-d315-425d-9053-319a055c1468");
	private static final UUID uuidABBREV_VIE = UUID.fromString("ce26e090-99b0-407f-a177-3fbc316638f0");
	private static final UUID uuidABBREV_VNA = UUID.fromString("9f97925d-be79-48e5-ab65-25d0bc27503c");
	private static final UUID uuidABBREV_VRG = UUID.fromString("3a6df9f1-d33b-4fe7-a150-db130a038654");
	private static final UUID uuidABBREV_WAK = UUID.fromString("36d57602-8107-458c-a097-5f37ff49881e");
	private static final UUID uuidABBREV_WAL = UUID.fromString("b8810d5d-152f-47df-b570-83a84a37bad6");
	private static final UUID uuidABBREV_WAS = UUID.fromString("fcaa778d-12d7-4e35-9bad-05a05d80f563");
	private static final UUID uuidABBREV_WAU = UUID.fromString("cb709a6b-604a-4dcc-9ed4-7410750a3fb6");
	private static final UUID uuidABBREV_WDC = UUID.fromString("16939d8c-587d-4277-be04-26502fe61b28");
	private static final UUID uuidABBREV_WHM = UUID.fromString("bd730c6b-5659-445d-9b26-3a662186b1b5");
	private static final UUID uuidABBREV_WIN = UUID.fromString("8b56d6ca-77a5-436f-9839-888e4f0611ea");
	private static final UUID uuidABBREV_WIS = UUID.fromString("739fa687-ccc0-4ecd-b30e-216c1d0ae8c0");
	private static final UUID uuidABBREV_WSA = UUID.fromString("2a3887f9-739d-4969-8a05-be5dff943654");
	private static final UUID uuidABBREV_WSB = UUID.fromString("183869b8-7143-492e-abec-fc9aa585cb2e");
	private static final UUID uuidABBREV_WVA = UUID.fromString("8f1802cf-8c21-4951-8460-68f23117a7a8");
	private static final UUID uuidABBREV_WYO = UUID.fromString("c75e0ede-5a11-486d-bbd5-1aad2afc1c53");
	private static final UUID uuidABBREV_XMS = UUID.fromString("474ea264-4a0c-4e47-b2e3-b7d69161a887");
	private static final UUID uuidABBREV_YAK = UUID.fromString("258f6e7c-6acb-4ccd-b312-748e0502d758");
	private static final UUID uuidABBREV_YEM = UUID.fromString("d2c3f9e5-3c36-4a42-9e97-48927f63c1f4");
	private static final UUID uuidABBREV_YUG = UUID.fromString("705df0b8-452a-4868-bf12-bdc4dcde018a");
	private static final UUID uuidABBREV_YUK = UUID.fromString("26b4e43d-3756-4b28-872b-8ef1d65168a8");
	private static final UUID uuidABBREV_ZAI = UUID.fromString("91db183a-a8c3-45dc-8cc9-2b8c2a6c183d");
	private static final UUID uuidABBREV_ZAM = UUID.fromString("997ca9ce-29cb-4d9c-b9ca-b1ab98968785");
	private static final UUID uuidABBREV_ZIM = UUID.fromString("5b63e02e-2e96-4acc-86a9-ded2a0359f4b");
	private static final UUID uuidABBREV_ABT_OO = UUID.fromString("0103dfde-e203-40d7-bec8-bb484ae75bdf");
	private static final UUID uuidABBREV_AFG_OO = UUID.fromString("019fc551-10dc-43c2-9c5b-ca52a2d09e86");
	private static final UUID uuidABBREV_AGE_BA = UUID.fromString("c6af30b9-99b3-4a98-adfb-97444ce96e8b");
	private static final UUID uuidABBREV_AGE_CH = UUID.fromString("72f8db6a-0472-45c4-acff-adf8563a0772");
	private static final UUID uuidABBREV_AGE_CN = UUID.fromString("7f93e700-4e9f-4894-abe1-ffcbd5327bce");
	private static final UUID uuidABBREV_AGE_CO = UUID.fromString("69394eb2-c2f1-4433-8fcc-daabcf70a7d5");
	private static final UUID uuidABBREV_AGE_DF = UUID.fromString("356f5e7e-f8e5-4ff0-adc8-ff3ceed8b871");
	private static final UUID uuidABBREV_AGE_ER = UUID.fromString("8bd874ce-36f4-4c96-b1bc-00084bbb7793");
	private static final UUID uuidABBREV_AGE_FO = UUID.fromString("a12e56d3-e968-4c18-ba1e-feba731b071f");
	private static final UUID uuidABBREV_AGE_LP = UUID.fromString("b7eac6d2-22b0-4ca1-b139-0b377dbf877c");
	private static final UUID uuidABBREV_AGE_MI = UUID.fromString("e614e0b6-e09c-4802-b4dc-9186a3bbab2d");
	private static final UUID uuidABBREV_AGS_CB = UUID.fromString("9adc826c-ef68-403c-930b-d67027a6e253");
	private static final UUID uuidABBREV_AGS_NE = UUID.fromString("3a2a0a1e-f820-4f77-a2db-69d822eccda9");
	private static final UUID uuidABBREV_AGS_RN = UUID.fromString("84d9dfd7-1c32-433b-8f30-a07fd3f61f57");
	private static final UUID uuidABBREV_AGS_SC = UUID.fromString("53a83e7f-fb6a-437b-beab-47c389b68a41");
	private static final UUID uuidABBREV_AGS_SF = UUID.fromString("75ed8ca6-0b99-4f11-a5ab-766bd819d819");
	private static final UUID uuidABBREV_AGS_TF = UUID.fromString("b42abd5a-5181-4342-a9fb-cd6193d668fa");
	private static final UUID uuidABBREV_AGW_CA = UUID.fromString("44b8a99f-344f-4c1e-8299-9cbad6fb4043");
	private static final UUID uuidABBREV_AGW_JU = UUID.fromString("e38f2205-3e06-4b58-9c19-ca80903e7fbd");
	private static final UUID uuidABBREV_AGW_LR = UUID.fromString("104bc6db-9d96-4249-a277-ca21289af3c6");
	private static final UUID uuidABBREV_AGW_ME = UUID.fromString("901360e8-5b48-480a-831b-e3393d4198a8");
	private static final UUID uuidABBREV_AGW_SA = UUID.fromString("30dcfd8e-fd7b-4254-984b-107b8a60c57d");
	private static final UUID uuidABBREV_AGW_SE = UUID.fromString("4b530f04-5fc9-438e-909a-acc32cfd7986");
	private static final UUID uuidABBREV_AGW_SJ = UUID.fromString("2245ce8c-b3f5-4a01-96d9-f93ea9927b2a");
	private static final UUID uuidABBREV_AGW_SL = UUID.fromString("639c2a60-7858-4eaf-9e79-f90b29a1882a");
	private static final UUID uuidABBREV_AGW_TU = UUID.fromString("d332b58c-d30d-4cac-8982-8ab8885e6355");
	private static final UUID uuidABBREV_ALA_OO = UUID.fromString("3eb214c4-31a9-49c4-8163-a3a5e4d468a0");
	private static final UUID uuidABBREV_ALB_OO = UUID.fromString("c97096ec-d32f-4106-920a-45f69e359199");
	private static final UUID uuidABBREV_ALD_OO = UUID.fromString("b9ca970c-f676-40f4-8cbb-bd431b42a077");
	private static final UUID uuidABBREV_ALG_OO = UUID.fromString("c88a09f1-e3f9-46ba-8541-84f956131932");
	private static final UUID uuidABBREV_ALT_OO = UUID.fromString("13a03777-9e38-4e86-8ed2-a70f9b9279c3");
	private static final UUID uuidABBREV_ALU_OO = UUID.fromString("f553a336-03b4-499f-a0d3-5ac4d84a968c");
	private static final UUID uuidABBREV_AMU_OO = UUID.fromString("e8359d12-7331-445b-a015-d1a69684c172");
	private static final UUID uuidABBREV_AND_AN = UUID.fromString("770a5811-2b6e-4053-9ac5-35985c37a2d2");
	private static final UUID uuidABBREV_AND_CO = UUID.fromString("fed827ac-2946-4066-a08c-d542fd8634af");
	private static final UUID uuidABBREV_ANG_OO = UUID.fromString("36d86e81-0288-468e-ab7e-dd63d0b0a574");
	private static final UUID uuidABBREV_ANT_OO = UUID.fromString("0c4d8f41-9621-4912-a446-228b395ff78c");
	private static final UUID uuidABBREV_ARI_OO = UUID.fromString("7210d9dd-304c-4577-881a-02c73f153d7b");
	private static final UUID uuidABBREV_ARK_OO = UUID.fromString("757cb232-f714-4ed0-af91-aaf1f24d71e5");
	private static final UUID uuidABBREV_ARU_OO = UUID.fromString("9aaf0819-6f2c-4a10-bd1a-ea5f0764c878");
	private static final UUID uuidABBREV_ASC_OO = UUID.fromString("d1a32a01-9395-4952-a754-eeae34ab1dd7");
	private static final UUID uuidABBREV_ASK_OO = UUID.fromString("da4e0179-a2f1-42a9-9b81-a77e705c6c5c");
	private static final UUID uuidABBREV_ASP_OO = UUID.fromString("d8e8fb07-ca32-4f6e-99b2-7323d8e1ef32");
	private static final UUID uuidABBREV_ASS_AS = UUID.fromString("3f63e888-87bd-4ac7-8233-55e4f19bc67e");
	private static final UUID uuidABBREV_ASS_MA = UUID.fromString("113297af-2d4a-4201-a262-cddf0dd13202");
	private static final UUID uuidABBREV_ASS_ME = UUID.fromString("6b76fe81-1fdc-4765-b3fc-7d6387417e9a");
	private static final UUID uuidABBREV_ASS_MI = UUID.fromString("5b8034e3-3225-41e8-8ed1-7b4f2713f8db");
	private static final UUID uuidABBREV_ASS_NA = UUID.fromString("e6023624-a003-4791-a8c8-ad015215b15c");
	private static final UUID uuidABBREV_ASS_TR = UUID.fromString("f6046475-d928-4e87-8140-2435b642f2d7");
	private static final UUID uuidABBREV_ATP_OO = UUID.fromString("2a939b1b-e6aa-468e-a7f9-31ce3bc316d0");
	private static final UUID uuidABBREV_AUT_AU = UUID.fromString("f7659257-587d-4c9e-802b-150f3849f76a");
	private static final UUID uuidABBREV_AUT_LI = UUID.fromString("1167d228-e7ff-4702-960a-578118fd6ceb");
	private static final UUID uuidABBREV_AZO_OO = UUID.fromString("a17d5d28-db7b-4e7e-ab49-d24e0a8682d7");
	private static final UUID uuidABBREV_BAH_OO = UUID.fromString("1a79eb1b-734d-42d0-9887-a6fea70ab556");
	private static final UUID uuidABBREV_BAL_OO = UUID.fromString("bd621ff5-cb83-43de-adcb-600cb64e9c7f");
	private static final UUID uuidABBREV_BAN_OO = UUID.fromString("f69009bf-be76-4d4f-a601-25dbe52c73ec");
	private static final UUID uuidABBREV_BEN_OO = UUID.fromString("bf4dbc7d-2cc9-411d-abdc-d50cba248607");
	private static final UUID uuidABBREV_BER_OO = UUID.fromString("99c9fe8e-cdbb-46db-8e77-3cb4a95bb30d");
	private static final UUID uuidABBREV_BGM_BE = UUID.fromString("72afac75-ab5f-4f62-80b1-e5880dcf53f7");
	private static final UUID uuidABBREV_BGM_LU = UUID.fromString("afa00cef-9ac1-49f9-aca4-ffb01f5f634a");
	private static final UUID uuidABBREV_BIS_OO = UUID.fromString("d9a11144-447c-4e72-b75f-9daeb254e7c4");
	private static final UUID uuidABBREV_BKN_OO = UUID.fromString("6fd49a11-1f31-4e7b-abfc-4890b23a9fcf");
	private static final UUID uuidABBREV_BLR_OO = UUID.fromString("9b33d3db-f112-4332-aadd-af490d18e4f5");
	private static final UUID uuidABBREV_BLT_ES = UUID.fromString("caef95f0-536f-493b-bcc5-2ecf478ef4aa");
	private static final UUID uuidABBREV_BLT_KA = UUID.fromString("72bc25f9-5f20-4ad9-8734-ad38044b2897");
	private static final UUID uuidABBREV_BLT_LA = UUID.fromString("b788c4c4-52a7-45ec-9589-07887d88f5c6");
	private static final UUID uuidABBREV_BLT_LI = UUID.fromString("50dd15a9-af27-475d-a224-23a21104bf23");
	private static final UUID uuidABBREV_BLZ_OO = UUID.fromString("1e3433be-5490-463c-b988-d340603e8440");
	private static final UUID uuidABBREV_BOL_OO = UUID.fromString("64501083-0924-4dc3-be80-d281fa700804");
	private static final UUID uuidABBREV_BOR_BR = UUID.fromString("ca033ec6-5bd5-487a-b0f6-f363d429afa8");
	private static final UUID uuidABBREV_BOR_KA = UUID.fromString("05c6bce3-2e55-48cb-a414-707e936066d1");
	private static final UUID uuidABBREV_BOR_SB = UUID.fromString("8d34b675-7de1-4623-a16a-1e0ca989df0c");
	private static final UUID uuidABBREV_BOR_SR = UUID.fromString("bd44128d-ddf6-4b89-af1c-bfef6bafd155");
	private static final UUID uuidABBREV_BOT_OO = UUID.fromString("1e326747-f911-49ca-97e1-315cced55696");
	private static final UUID uuidABBREV_BOU_OO = UUID.fromString("4dac21e1-61f5-45a7-8899-04e2f50dd5a3");
	private static final UUID uuidABBREV_BRC_OO = UUID.fromString("2698d951-e48f-4869-8c79-1bc59db07cc4");
	private static final UUID uuidABBREV_BRY_OO = UUID.fromString("3c8c5292-df45-472d-98e0-8e48a5328227");
	private static final UUID uuidABBREV_BUL_OO = UUID.fromString("776d3bd4-a64e-4259-a3e6-540caab9a891");
	private static final UUID uuidABBREV_BUR_OO = UUID.fromString("d97c5986-3e62-4c30-8f4d-df6dd4074764");
	private static final UUID uuidABBREV_BZC_DF = UUID.fromString("2a37be56-a42c-4a9a-ae03-fd89eac4bac1");
	private static final UUID uuidABBREV_BZC_GO = UUID.fromString("0b51b937-6b73-4b71-892d-f01d467060e1");
	private static final UUID uuidABBREV_BZC_MS = UUID.fromString("bc51f7d2-776a-4ee1-9ff3-28e19cec10a7");
	private static final UUID uuidABBREV_BZC_MT = UUID.fromString("00b3e39f-5359-4b0b-80ef-1ca94da03aa2");
	private static final UUID uuidABBREV_BZE_AL = UUID.fromString("7876b019-46c4-425d-8d0c-1e57b4145e41");
	private static final UUID uuidABBREV_BZE_BA = UUID.fromString("fded854f-ed57-46ce-a664-e6d5173c6478");
	private static final UUID uuidABBREV_BZE_CE = UUID.fromString("39ad67bf-2b32-4831-99fd-0e669b3f78ca");
	private static final UUID uuidABBREV_BZE_FN = UUID.fromString("5b62e196-c4b3-4a67-b2f1-26bdb66dfda2");
	private static final UUID uuidABBREV_BZE_MA = UUID.fromString("39bc4948-c03f-4a93-b54f-35d2aeedde06");
	private static final UUID uuidABBREV_BZE_PB = UUID.fromString("6e1a0fbd-f8df-453a-bde3-2d7f75ea144b");
	private static final UUID uuidABBREV_BZE_PE = UUID.fromString("961744da-44de-4325-b051-96ece1e6d0d3");
	private static final UUID uuidABBREV_BZE_PI = UUID.fromString("5e72c1cc-fe4c-492e-927f-b1b1c4eb8235");
	private static final UUID uuidABBREV_BZE_RN = UUID.fromString("51cb58ce-6070-41fc-bb7c-e866e9fab336");
	private static final UUID uuidABBREV_BZE_SE = UUID.fromString("b34d164e-ff7c-4cd9-9d4b-b34832837113");
	private static final UUID uuidABBREV_BZL_ES = UUID.fromString("0d6fe90f-7f2e-4ac8-8e6b-ea543145a645");
	private static final UUID uuidABBREV_BZL_MG = UUID.fromString("60999bdf-79e6-4bc5-ab77-6f3477187f75");
	private static final UUID uuidABBREV_BZL_RJ = UUID.fromString("ed329818-e45c-4dfc-b116-3dad8c3cb923");
	private static final UUID uuidABBREV_BZL_SP = UUID.fromString("0c00b73c-7b40-48e8-ad8e-d627a250e664");
	private static final UUID uuidABBREV_BZL_TR = UUID.fromString("895e278b-51b9-4aa3-9529-15ff1cd3f205");
	private static final UUID uuidABBREV_BZN_AC = UUID.fromString("40987986-0481-4ea4-93e8-ea7266a74d35");
	private static final UUID uuidABBREV_BZN_AM = UUID.fromString("e98b37bc-5ead-4782-a561-163b14c0b6e8");
	private static final UUID uuidABBREV_BZN_AP = UUID.fromString("62b94eee-3620-46b3-a118-8899c0d32cb8");
	private static final UUID uuidABBREV_BZN_PA = UUID.fromString("66301e48-a27b-444e-b1b5-ce99f2ab1feb");
	private static final UUID uuidABBREV_BZN_RM = UUID.fromString("2b24d78c-c296-491c-a089-60d6fd25f769");
	private static final UUID uuidABBREV_BZN_RO = UUID.fromString("0a6e5446-aefc-46ed-9808-5e6fb651ab45");
	private static final UUID uuidABBREV_BZN_TO = UUID.fromString("3c2e1e30-cd13-4d4b-aba5-444d000005cd");
	private static final UUID uuidABBREV_BZS_PR = UUID.fromString("a755293a-e0af-4b73-bcc5-83327553ad51");
	private static final UUID uuidABBREV_BZS_RS = UUID.fromString("57feaa67-3a87-4462-939b-3ff0e5fb87fa");
	private static final UUID uuidABBREV_BZS_SC = UUID.fromString("2e81bb7d-c7f2-4cf9-a580-4ec8caaebba6");
	private static final UUID uuidABBREV_CAB_OO = UUID.fromString("aed8e86e-8a54-46e8-98ec-0dc41c52859b");
	private static final UUID uuidABBREV_CAF_OO = UUID.fromString("9b303a72-5147-4c03-8758-ee4dab1249ef");
	private static final UUID uuidABBREV_CAL_OO = UUID.fromString("175a2a3d-dd30-4992-adad-8c8966b911fd");
	private static final UUID uuidABBREV_CAY_OO = UUID.fromString("b4432cd1-630c-49fb-a191-271823f4f0d2");
	private static final UUID uuidABBREV_CBD_OO = UUID.fromString("192d277b-7559-4bb5-b212-a4d64810d973");
	private static final UUID uuidABBREV_CGS_OO = UUID.fromString("ddeadead-dd96-4a27-9289-b6fc197bdf08");
	private static final UUID uuidABBREV_CHA_OO = UUID.fromString("0e64f21a-8ea7-40aa-8513-63606447a546");
	private static final UUID uuidABBREV_CHC_CQ = UUID.fromString("fd0c336a-5081-444d-b8b2-6111ef74e26b");
	private static final UUID uuidABBREV_CHC_GZ = UUID.fromString("78afe8a9-fcaa-4f2a-8ad9-718f10e9881f");
	private static final UUID uuidABBREV_CHC_HU = UUID.fromString("f40ef648-3dbc-4f2e-b0a9-cb526dd88c80");
	private static final UUID uuidABBREV_CHC_SC = UUID.fromString("cbabc112-bc00-402a-81ee-57e945b6c298");
	private static final UUID uuidABBREV_CHC_YN = UUID.fromString("eedbd7df-cfa9-4506-91bc-8ab052183462");
	private static final UUID uuidABBREV_CHH_OO = UUID.fromString("62d6672c-13a4-4df8-b40d-f996e9d8e6b0");
	private static final UUID uuidABBREV_CHI_NM = UUID.fromString("b393d5ec-4865-45b0-b688-22d88bac137b");
	private static final UUID uuidABBREV_CHI_NX = UUID.fromString("629f51dd-27a1-49ac-9253-fa5ca793fdf0");
	private static final UUID uuidABBREV_CHM_HJ = UUID.fromString("93840e4a-d5de-468d-a35a-629430192791");
	private static final UUID uuidABBREV_CHM_JL = UUID.fromString("ac40926b-a478-48bb-8719-13119b3b2bcd");
	private static final UUID uuidABBREV_CHM_LN = UUID.fromString("a6cabaff-16b5-4207-878a-a4bf70260e83");
	private static final UUID uuidABBREV_CHN_BJ = UUID.fromString("0dd6f4f6-2d38-41fa-8ae1-6689bae0638a");
	private static final UUID uuidABBREV_CHN_GS = UUID.fromString("ea148213-215b-4548-a393-f472b39dc6fd");
	private static final UUID uuidABBREV_CHN_HB = UUID.fromString("2982aae9-e60f-4aed-ada1-29968277af2d");
	private static final UUID uuidABBREV_CHN_SA = UUID.fromString("96471c82-c993-4609-a272-f4bb7df1158f");
	private static final UUID uuidABBREV_CHN_SD = UUID.fromString("97ee7fee-940a-48fd-8d3a-42edb21b7591");
	private static final UUID uuidABBREV_CHN_SX = UUID.fromString("a0fac021-61c4-44d2-ac7e-d4d9a5c7927d");
	private static final UUID uuidABBREV_CHN_TJ = UUID.fromString("7b1cec1a-739e-437b-bf7b-bfde1b71828f");
	private static final UUID uuidABBREV_CHQ_OO = UUID.fromString("88dda871-eef1-4e00-b96d-7490703de84d");
	private static final UUID uuidABBREV_CHS_AH = UUID.fromString("5ce6089a-9212-4cbc-b42c-5b1197043c86");
	private static final UUID uuidABBREV_CHS_FJ = UUID.fromString("b1650734-9e24-4409-9dd3-8cb5f206d0ca");
	private static final UUID uuidABBREV_CHS_GD = UUID.fromString("599579c2-1e1a-4668-8bd6-1dabce3282d9");
	private static final UUID uuidABBREV_CHS_GX = UUID.fromString("0a662d74-ecb7-4bb0-a458-2442612f4df5");
	private static final UUID uuidABBREV_CHS_HE = UUID.fromString("76f0b8a6-edc1-43c4-8fc8-fbfbca101fee");
	private static final UUID uuidABBREV_CHS_HK = UUID.fromString("f0463996-504b-4398-a3bd-21b5afc9a736");
	private static final UUID uuidABBREV_CHS_HN = UUID.fromString("de638bfb-9703-491c-a66f-0113846a56b8");
	private static final UUID uuidABBREV_CHS_JS = UUID.fromString("4292f645-5741-49af-a747-38b36e91ff8d");
	private static final UUID uuidABBREV_CHS_JX = UUID.fromString("a7cf6e57-c704-4d96-9c6d-3fb1eaf474e3");
	private static final UUID uuidABBREV_CHS_KI = UUID.fromString("a40f5ee5-a595-46de-8b93-506cc61a30d7");
	private static final UUID uuidABBREV_CHS_MA = UUID.fromString("70877ae8-f708-4624-9caa-db789f5d26c2");
	private static final UUID uuidABBREV_CHS_MP = UUID.fromString("f2a0c8a0-b869-463d-8db1-942b73f7df64");
	private static final UUID uuidABBREV_CHS_SH = UUID.fromString("21162d82-eacf-4a45-99d8-19a32badad8e");
	private static final UUID uuidABBREV_CHS_ZJ = UUID.fromString("a3252a2b-1210-466b-8683-472fda15d50c");
	private static final UUID uuidABBREV_CHT_OO = UUID.fromString("fce7c291-a555-434d-a8e0-e06140bbaecf");
	private static final UUID uuidABBREV_CHX_OO = UUID.fromString("96433579-0c5f-4d67-9857-dd6ab34895c0");
	private static final UUID uuidABBREV_CKI_OO = UUID.fromString("89843ff7-05f0-455e-b97f-b22312964255");
	private static final UUID uuidABBREV_CLC_BI = UUID.fromString("18573a20-1b4d-41a7-995d-7ac2f68abff7");
	private static final UUID uuidABBREV_CLC_CO = UUID.fromString("51f7efdb-4632-4628-a395-6cc57047453e");
	private static final UUID uuidABBREV_CLC_LA = UUID.fromString("9be63577-5283-4bcb-9739-c0f560594d54");
	private static final UUID uuidABBREV_CLC_MA = UUID.fromString("dc6dd81a-cdb5-4c5c-9275-319ec6c535fd");
	private static final UUID uuidABBREV_CLC_OH = UUID.fromString("b43a3d93-f3ad-427b-a462-7500d8a615e9");
	private static final UUID uuidABBREV_CLC_SA = UUID.fromString("a94636e2-6490-4011-9bfa-efa3a3af0972");
	private static final UUID uuidABBREV_CLC_VA = UUID.fromString("b9c97d9c-8cf9-408e-a391-8d6cb0ba72a3");
	private static final UUID uuidABBREV_CLM_OO = UUID.fromString("eca8bb03-8336-4396-8e0b-2d98c5a62a32");
	private static final UUID uuidABBREV_CLN_AN = UUID.fromString("beac5efa-f57e-47ea-8772-7f9541fa8765");
	private static final UUID uuidABBREV_CLN_AT = UUID.fromString("3307fceb-69ff-4a20-88be-6d34ae89409d");
	private static final UUID uuidABBREV_CLN_TA = UUID.fromString("5ea2ef41-e327-4d59-9c92-760d9073646e");
	private static final UUID uuidABBREV_CLS_AI = UUID.fromString("9a63d984-b405-4bc0-8e08-dbb79109782d");
	private static final UUID uuidABBREV_CLS_LL = UUID.fromString("2f978481-3921-4173-8fe7-4b51c84e6649");
	private static final UUID uuidABBREV_CLS_MG = UUID.fromString("0a6165e5-31e5-4323-9d92-b25821194fcd");
	private static final UUID uuidABBREV_CMN_OO = UUID.fromString("6db5c74f-a5d3-423d-a678-7112be81f963");
	private static final UUID uuidABBREV_CNT_OO = UUID.fromString("275bb1e9-72de-4345-a7f6-c2e473ad850b");
	private static final UUID uuidABBREV_CNY_OO = UUID.fromString("336518de-0645-4454-b730-dd7099d00fdd");
	private static final UUID uuidABBREV_COL_OO = UUID.fromString("1320f903-d644-40b9-8ee2-275dfa384ad1");
	private static final UUID uuidABBREV_COM_CO = UUID.fromString("0a57ef70-bea5-40cb-a8e7-481478da5d06");
	private static final UUID uuidABBREV_COM_MA = UUID.fromString("99fe5d0f-56c9-4e18-b429-0971b8f12b25");
	private static final UUID uuidABBREV_CON_OO = UUID.fromString("67acff9a-257d-469c-8b17-607fdbceaa2e");
	private static final UUID uuidABBREV_COO_OO = UUID.fromString("318423a0-3cd7-47f4-8452-2eca950092b3");
	private static final UUID uuidABBREV_COR_OO = UUID.fromString("a7570889-39a4-4bb8-b28f-77bd153d44fa");
	private static final UUID uuidABBREV_COS_OO = UUID.fromString("c839afa6-1db2-4b48-9d79-250fcfed9f89");
	private static final UUID uuidABBREV_CPI_CL = UUID.fromString("70cf656c-21c0-4c67-a737-2331539d430f");
	private static final UUID uuidABBREV_CPI_CO = UUID.fromString("3af035d9-de46-42a6-af98-37b8bfcb5768");
	private static final UUID uuidABBREV_CPI_MA = UUID.fromString("f0775631-b985-48f9-847c-990f6d921c46");
	private static final UUID uuidABBREV_CPP_EC = UUID.fromString("2466ddca-9662-42ea-97ea-85b0405f8155");
	private static final UUID uuidABBREV_CPP_NC = UUID.fromString("70fb4828-7756-4f13-8813-4da957f6c424");
	private static final UUID uuidABBREV_CPP_WC = UUID.fromString("b3cf874d-745b-4574-8887-6c2cbe8e6188");
	private static final UUID uuidABBREV_CPV_OO = UUID.fromString("1a6e7f6b-824d-40e0-846c-895855640500");
	private static final UUID uuidABBREV_CRL_MF = UUID.fromString("98cd8286-032a-49db-8034-0d52aa6be72d");
	private static final UUID uuidABBREV_CRL_PA = UUID.fromString("387ee427-88af-4791-8357-335b8d75839f");
	private static final UUID uuidABBREV_CRZ_OO = UUID.fromString("c2c92549-0f3d-4f96-9950-6503bf1d45e1");
	private static final UUID uuidABBREV_CTA_OO = UUID.fromString("94d17555-90d0-43b6-92fa-a4ec0f776833");
	private static final UUID uuidABBREV_CTM_OO = UUID.fromString("de4e0b06-a751-4dfa-b838-504204213780");
	private static final UUID uuidABBREV_CUB_OO = UUID.fromString("66ce6bb4-b48e-483b-aed1-62646e9c80b8");
	private static final UUID uuidABBREV_CVI_OO = UUID.fromString("c5773847-3eb1-4a6a-ab97-30ea78651205");
	private static final UUID uuidABBREV_CYP_OO = UUID.fromString("9d447b51-e363-4dde-ae40-84c55679983c");
	private static final UUID uuidABBREV_CZE_CZ = UUID.fromString("cd9612b2-1a27-4895-966c-59fcfe7c49d2");
	private static final UUID uuidABBREV_CZE_SK = UUID.fromString("d4170b17-ebe4-4fb4-b5b1-3b8da07ae683");
	private static final UUID uuidABBREV_DEL_OO = UUID.fromString("2040cd9f-3fb0-4b47-8984-5a72fbfbcad9");
	private static final UUID uuidABBREV_DEN_OO = UUID.fromString("f818c97e-fd61-42fe-9d75-d433f8cb349c");
	private static final UUID uuidABBREV_DJI_OO = UUID.fromString("f8ea3076-0f5c-46aa-a8a3-2669e9ccc522");
	private static final UUID uuidABBREV_DOM_OO = UUID.fromString("f8265093-496d-4e00-b732-460f0ee2fca5");
	private static final UUID uuidABBREV_DSV_OO = UUID.fromString("5efab129-f8dc-4029-bdc3-5fbe3aaf71dc");
	private static final UUID uuidABBREV_EAI_OO = UUID.fromString("352c212b-c0bc-4294-b87e-c3d8fdbed724");
	private static final UUID uuidABBREV_EAS_OO = UUID.fromString("80b8d861-0e07-489d-9977-970a7e6d832a");
	private static final UUID uuidABBREV_ECU_OO = UUID.fromString("4545d099-32e6-44f8-bce9-8c29df82cde7");
	private static final UUID uuidABBREV_EGY_OO = UUID.fromString("6e2fd22b-7fcd-40d1-bc6f-cdc593b182ac");
	private static final UUID uuidABBREV_EHM_AP = UUID.fromString("babf2ddb-eba1-488a-bea8-21b098ddb315");
	private static final UUID uuidABBREV_EHM_BH = UUID.fromString("6a91ae3b-14af-4de6-8a12-be93bbd9166a");
	private static final UUID uuidABBREV_EHM_DJ = UUID.fromString("6ac84372-1882-4bc0-8154-8674ba925f10");
	private static final UUID uuidABBREV_EHM_SI = UUID.fromString("c086fb8d-ee90-404d-8bca-9412aaa6b0d9");
	private static final UUID uuidABBREV_ELS_OO = UUID.fromString("df21cb0e-ac98-4534-a603-0b18ba6059d4");
	private static final UUID uuidABBREV_EQG_OO = UUID.fromString("a561767e-348c-46cd-9942-264d41fb67b7");
	private static final UUID uuidABBREV_ERI_OO = UUID.fromString("001304b9-c267-4dcc-be52-16bbd936ea1f");
	private static final UUID uuidABBREV_ETH_OO = UUID.fromString("44a6ee56-767a-463f-b8ad-e924f0d2f3e5");
	private static final UUID uuidABBREV_FAL_OO = UUID.fromString("e2af7b11-9179-4cac-ba35-65e8ec9053ae");
	private static final UUID uuidABBREV_FIJ_OO = UUID.fromString("de779b91-732f-4aee-ace6-5647b1787509");
	private static final UUID uuidABBREV_FIN_OO = UUID.fromString("f71525d8-0913-4a5c-97f1-a8374a8843da");
	private static final UUID uuidABBREV_FLA_OO = UUID.fromString("c21e91c8-ba91-45ac-81fd-cd13452911d1");
	private static final UUID uuidABBREV_FOR_OO = UUID.fromString("08eb9360-bdfc-42cc-9c35-655cdc018f58");
	private static final UUID uuidABBREV_FRA_CI = UUID.fromString("7be07113-7c04-4fb2-9e63-6759bcc39a7a");
	private static final UUID uuidABBREV_FRA_FR = UUID.fromString("41c5129a-3465-42cc-b016-59ab9ffad71a");
	private static final UUID uuidABBREV_FRA_MO = UUID.fromString("d4d02666-40a0-45c3-894b-482f64b54f69");
	private static final UUID uuidABBREV_FRG_OO = UUID.fromString("0e3306bd-6f2d-4f31-be3f-7c97648352a9");
	private static final UUID uuidABBREV_GAB_OO = UUID.fromString("dd5be3e5-e7ff-48be-b1f3-32f93996bd38");
	private static final UUID uuidABBREV_GAL_OO = UUID.fromString("3489394e-a20c-4e24-93c8-869d54f522ca");
	private static final UUID uuidABBREV_GAM_OO = UUID.fromString("92389b5b-067c-4c8f-a5b7-2bf4ab5df860");
	private static final UUID uuidABBREV_GEO_OO = UUID.fromString("bdec0502-18da-47fa-b101-74fb3e2fa52a");
	private static final UUID uuidABBREV_GER_OO = UUID.fromString("2dabc6d7-7e2a-4adc-9c9c-7244d4099fd5");
	private static final UUID uuidABBREV_GGI_AN = UUID.fromString("5df6d614-b1f7-41b1-b430-b74692f5abd6");
	private static final UUID uuidABBREV_GGI_BI = UUID.fromString("47023991-ff3f-499b-903b-97f9d9d9aaa9");
	private static final UUID uuidABBREV_GGI_PR = UUID.fromString("d73cc73e-5e7e-43b5-9e15-d791b4f86f2f");
	private static final UUID uuidABBREV_GGI_ST = UUID.fromString("c64e07cc-0a58-44b3-ac91-c216d1b91c1f");
	private static final UUID uuidABBREV_GHA_OO = UUID.fromString("7987d14f-5346-4a6c-98ca-b739ccc1470b");
	private static final UUID uuidABBREV_GIL_OO = UUID.fromString("632ccb5f-6864-4f2d-a764-5e963cab6de9");
	private static final UUID uuidABBREV_GNB_OO = UUID.fromString("7dd4d6fb-ecd3-43c6-a7ef-1921162d107c");
	private static final UUID uuidABBREV_GNL_OO = UUID.fromString("3a27e7e5-16ea-47db-b783-9f9e60bbd2d4");
	private static final UUID uuidABBREV_GRB_OO = UUID.fromString("b7fc1c91-c4a3-487d-937e-cc8f763b717a");
	private static final UUID uuidABBREV_GRC_OO = UUID.fromString("60a7d818-1910-4a95-9620-8d83592f9bd8");
	private static final UUID uuidABBREV_GST_BA = UUID.fromString("e21b5be6-1492-4123-abfe-d45fd5136b25");
	private static final UUID uuidABBREV_GST_QA = UUID.fromString("ed84cd13-29d1-4348-bd36-ef4cb2d3b9a6");
	private static final UUID uuidABBREV_GST_UA = UUID.fromString("78a1681f-cbcd-4d5c-ad1c-b4b1dc30e9cb");
	private static final UUID uuidABBREV_GUA_OO = UUID.fromString("ae6f3667-5ee6-42b0-8af9-da33af7e22a4");
	private static final UUID uuidABBREV_GUI_OO = UUID.fromString("1e8c8b3b-b91a-4b4e-b445-9a5ac606b502");
	private static final UUID uuidABBREV_GUY_OO = UUID.fromString("97ab4587-7677-4cb2-b198-0ea99a25a694");
	private static final UUID uuidABBREV_HAI_HA = UUID.fromString("d6af812a-d9fd-46ab-91ca-595c2cdacfa0");
	private static final UUID uuidABBREV_HAI_NI = UUID.fromString("62e2f64a-d0dd-4a9b-9214-59ca95ffac30");
	private static final UUID uuidABBREV_HAW_HI = UUID.fromString("b5ccde93-c0fb-4b4c-a3b1-2a87829e84da");
	private static final UUID uuidABBREV_HAW_JI = UUID.fromString("3cc53cea-2323-4914-a663-0611c0636f13");
	private static final UUID uuidABBREV_HAW_MI = UUID.fromString("d3cb6808-e47d-4d10-a339-c9a93684d342");
	private static final UUID uuidABBREV_HBI_OO = UUID.fromString("c605878a-8b5f-454e-abfd-c2f9c77002ec");
	private static final UUID uuidABBREV_HMD_OO = UUID.fromString("3fea8c26-1fab-4b57-a89b-ec7ef2e532d2");
	private static final UUID uuidABBREV_HON_OO = UUID.fromString("d3d2765d-d059-4713-9123-baf8ca074a32");
	private static final UUID uuidABBREV_HUN_OO = UUID.fromString("6f8dc312-e77e-4935-8a76-f24969bc7078");
	private static final UUID uuidABBREV_ICE_OO = UUID.fromString("993ec1df-8c6b-4f75-a66c-738ce9d71957");
	private static final UUID uuidABBREV_IDA_OO = UUID.fromString("3887f38f-ae38-4ed8-b2a9-41f6b78a2d43");
	private static final UUID uuidABBREV_ILL_OO = UUID.fromString("9acf4863-3eac-4859-aea8-dc79134b33a8");
	private static final UUID uuidABBREV_IND_AP = UUID.fromString("a9931f3e-e059-4b32-a666-23c10c714407");
	private static final UUID uuidABBREV_IND_BI = UUID.fromString("d8d51792-9a41-44f8-bffb-f22ebaef3754");
	private static final UUID uuidABBREV_IND_CH = UUID.fromString("042ab3ee-9bcc-4925-88cf-35137e029b86");
	private static final UUID uuidABBREV_IND_CT = UUID.fromString("104d84f7-716c-44a2-a174-60c01ce0f9af");
	private static final UUID uuidABBREV_IND_DD = UUID.fromString("d5106e1d-40dc-405e-ab8a-5a854ebd1f86");
	private static final UUID uuidABBREV_IND_DE = UUID.fromString("66a7b6f2-da99-47b2-a603-eb6ddae91d50");
	private static final UUID uuidABBREV_IND_DI = UUID.fromString("f52dc5be-6b3c-440a-9089-f3eaeed5c857");
	private static final UUID uuidABBREV_IND_DM = UUID.fromString("e81dffef-c1db-4c3d-8dbb-dec57dc40eab");
	private static final UUID uuidABBREV_IND_GO = UUID.fromString("ae4e8d1a-4495-4d6e-bfbb-e28256349d85");
	private static final UUID uuidABBREV_IND_GU = UUID.fromString("8a858374-81e4-4585-ae5f-4cd618400fd9");
	private static final UUID uuidABBREV_IND_HA = UUID.fromString("ca200baf-24bd-4dfe-bdc9-4cd2869f0cea");
	private static final UUID uuidABBREV_IND_JK = UUID.fromString("4fed0fd3-8648-4104-88dc-b1133b5b5227");
	private static final UUID uuidABBREV_IND_KE = UUID.fromString("0bf52a90-8f06-4881-b90b-c940522e264c");
	private static final UUID uuidABBREV_IND_KL = UUID.fromString("ba304903-cad7-45e9-9955-501a7aa3fb54");
	private static final UUID uuidABBREV_IND_KT = UUID.fromString("f82fcc4b-1e50-4cd3-ace5-6c3bffdfb251");
	private static final UUID uuidABBREV_IND_MH = UUID.fromString("c2662a7d-e26a-447e-a130-22b39bef2f80");
	private static final UUID uuidABBREV_IND_MP = UUID.fromString("1fdd0e8a-0f7c-440a-9982-b5d97eb98f5f");
	private static final UUID uuidABBREV_IND_MR = UUID.fromString("f84c153b-360b-472d-a641-806c4200af35");
	private static final UUID uuidABBREV_IND_OR = UUID.fromString("ed5512f2-f762-422d-aaf5-57aa813a22df");
	private static final UUID uuidABBREV_IND_PO = UUID.fromString("c11458c6-81d4-4cb9-b800-74097c655cc8");
	private static final UUID uuidABBREV_IND_PU = UUID.fromString("94ede15d-c1b6-466e-af32-19dbdf090da3");
	private static final UUID uuidABBREV_IND_RA = UUID.fromString("0e1f785c-dfff-4137-86bd-5c4047894d8e");
	private static final UUID uuidABBREV_IND_TN = UUID.fromString("a4fb4d07-4c7a-4ce6-a928-131d0a34db17");
	private static final UUID uuidABBREV_IND_UP = UUID.fromString("54444bfd-cdc4-4043-9ebf-14d2b141395f");
	private static final UUID uuidABBREV_IND_WB = UUID.fromString("7a97e38a-1e5d-4e6d-91d2-ec9f6364cc4c");
	private static final UUID uuidABBREV_IND_YA = UUID.fromString("b287bb04-2d21-405b-bc93-bd207b38bd36");
	private static final UUID uuidABBREV_INI_OO = UUID.fromString("6ef83660-69ed-477c-b2ef-639852b104b1");
	private static final UUID uuidABBREV_IOW_OO = UUID.fromString("6e7245aa-e3cd-4251-90ee-a3f85aede88e");
	private static final UUID uuidABBREV_IRE_IR = UUID.fromString("a2e12f91-a168-492e-aaaf-22aae4cd2397");
	private static final UUID uuidABBREV_IRE_NI = UUID.fromString("419e3ef7-c1dc-47af-83f7-2b05c9ebae4c");
	private static final UUID uuidABBREV_IRK_OO = UUID.fromString("bb16684c-0638-4831-99df-f9a2dd7b7e6b");
	private static final UUID uuidABBREV_IRN_OO = UUID.fromString("f46d2d84-0387-41dd-9e77-28f9d7d06f16");
	private static final UUID uuidABBREV_IRQ_OO = UUID.fromString("497a208e-9337-4708-9f3a-17efef2f33a8");
	private static final UUID uuidABBREV_ITA_IT = UUID.fromString("d9ef6446-b38f-4176-874c-b90b7fe0458b");
	private static final UUID uuidABBREV_ITA_SM = UUID.fromString("a97d4f6b-7f86-4125-b3ff-f7c3ceb0f495");
	private static final UUID uuidABBREV_ITA_VC = UUID.fromString("0fcb6ed1-fb5f-4360-9ced-1fac46bdc8bb");
	private static final UUID uuidABBREV_IVO_OO = UUID.fromString("c36d5a55-033c-4f34-bbea-d435304ceeba");
	private static final UUID uuidABBREV_JAM_OO = UUID.fromString("6c30ac35-c486-4e72-a48b-4987585c9fde");
	private static final UUID uuidABBREV_JAP_HK = UUID.fromString("72a3ca2e-6c7d-451e-aa15-c7cd0e86a370");
	private static final UUID uuidABBREV_JAP_HN = UUID.fromString("11192c4f-e0c9-4f28-8c6d-064c9f694bb0");
	private static final UUID uuidABBREV_JAP_KY = UUID.fromString("264f2319-0749-47b8-9a39-17c2a57189d0");
	private static final UUID uuidABBREV_JAP_SH = UUID.fromString("494fb115-9cf0-4e34-9fed-612b64e3224e");
	private static final UUID uuidABBREV_JAW_OO = UUID.fromString("36b93871-0bee-4380-b47e-56a843ce6aa3");
	private static final UUID uuidABBREV_JNF_OO = UUID.fromString("9fd8ad23-d932-4cdc-8822-029685073c5c");
	private static final UUID uuidABBREV_KAM_OO = UUID.fromString("0cf7c7c4-fa55-41e2-8c25-b9a5fede449b");
	private static final UUID uuidABBREV_KAN_OO = UUID.fromString("e1120ec1-7ee1-475e-b880-f66631666f7a");
	private static final UUID uuidABBREV_KAZ_OO = UUID.fromString("2cc9b611-f48a-4a21-b28c-f0a580b6bf06");
	private static final UUID uuidABBREV_KEG_OO = UUID.fromString("8452f319-86c9-4065-88e4-7adf66e945fd");
	private static final UUID uuidABBREV_KEN_OO = UUID.fromString("742abf05-8d1c-4939-b97d-927b17341415");
	private static final UUID uuidABBREV_KER_OO = UUID.fromString("a7a53d93-6a43-4c0c-b9c5-f832f125c5bf");
	private static final UUID uuidABBREV_KGZ_OO = UUID.fromString("18e391d2-3a8c-4efd-83e5-605dc178b7eb");
	private static final UUID uuidABBREV_KHA_OO = UUID.fromString("84af6761-0b47-4ff6-bf66-7b03287ac0cf");
	private static final UUID uuidABBREV_KOR_NK = UUID.fromString("dd63dd30-16a9-46c4-a6a9-67dc83eaf2aa");
	private static final UUID uuidABBREV_KOR_SK = UUID.fromString("59f4887a-6724-42b5-af22-c9e7b009891f");
	private static final UUID uuidABBREV_KRA_OO = UUID.fromString("b69b568c-b8b5-49f3-8d1e-84a737d928ab");
	private static final UUID uuidABBREV_KRI_OO = UUID.fromString("ce577904-25e7-4172-b15d-eb5e7469167f");
	private static final UUID uuidABBREV_KRY_OO = UUID.fromString("f3253f0d-05ce-41d7-ae5a-10fa60c7f547");
	private static final UUID uuidABBREV_KTY_OO = UUID.fromString("31b3c605-0f49-4d73-b018-c86e4ca4de50");
	private static final UUID uuidABBREV_KUR_OO = UUID.fromString("5150bbcf-0c6f-4010-973a-e9856bcfc396");
	private static final UUID uuidABBREV_KUW_OO = UUID.fromString("df1b04c7-f978-4ad4-8bbf-618aed7e44a9");
	private static final UUID uuidABBREV_KZN_OO = UUID.fromString("dd5fb4eb-cf48-489f-9786-71600fd7517e");
	private static final UUID uuidABBREV_LAB_OO = UUID.fromString("928fc96c-6ea8-4679-943c-37bdad233324");
	private static final UUID uuidABBREV_LAO_OO = UUID.fromString("bd72f665-a9d0-4928-a293-bbb1d7f31fdf");
	private static final UUID uuidABBREV_LBR_OO = UUID.fromString("fc6dfd5e-43cf-47c5-a2a1-bad849a452f9");
	private static final UUID uuidABBREV_LBS_LB = UUID.fromString("2173fe0b-311b-440e-b4df-26acd1c70ca7");
	private static final UUID uuidABBREV_LBS_SY = UUID.fromString("aa80988d-a00b-4cf3-8c63-feb52ac85a81");
	private static final UUID uuidABBREV_LBY_OO = UUID.fromString("8b943f73-8988-43a8-88ba-3b048eea2827");
	private static final UUID uuidABBREV_LDV_OO = UUID.fromString("e062678f-23ba-4bad-bc46-c40e704422c4");
	private static final UUID uuidABBREV_LEE_AB = UUID.fromString("1d3fa83d-a348-4b47-b2fc-3105e476aca5");
	private static final UUID uuidABBREV_LEE_AG = UUID.fromString("070afba6-c7e1-4acc-9139-ebbdca9cd73f");
	private static final UUID uuidABBREV_LEE_AV = UUID.fromString("e7bb3a9d-2313-42ea-8929-a704bdda09ab");
	private static final UUID uuidABBREV_LEE_BV = UUID.fromString("9a51f2c5-3269-41c0-8fba-faaee4271bb5");
	private static final UUID uuidABBREV_LEE_GU = UUID.fromString("5608fea2-e246-4455-bb39-fd8c55ea411d");
	private static final UUID uuidABBREV_LEE_MO = UUID.fromString("5528904f-f645-42a0-9602-13a65c7073ad");
	private static final UUID uuidABBREV_LEE_NL = UUID.fromString("570bef0e-fbf4-47bc-801d-2245d592168f");
	private static final UUID uuidABBREV_LEE_SK = UUID.fromString("e294e779-8bdf-4c2e-8d78-46abd05453ca");
	private static final UUID uuidABBREV_LEE_SM = UUID.fromString("aa936910-fa3d-4139-9ab4-a82b7e8988d1");
	private static final UUID uuidABBREV_LEE_VI = UUID.fromString("cc6a1ace-bd07-4e38-80d5-5b7d1d9067fd");
	private static final UUID uuidABBREV_LES_OO = UUID.fromString("aa782397-e924-4189-b771-35df76f770c2");
	private static final UUID uuidABBREV_LIN_KI = UUID.fromString("34fd6efd-9061-48c5-946b-bd114a418723");
	private static final UUID uuidABBREV_LIN_US = UUID.fromString("6e72bebe-6709-4054-be0d-90bcdcffe272");
	private static final UUID uuidABBREV_LOU_OO = UUID.fromString("b7660ed9-e18b-4014-b0bf-efe0684ea878");
	private static final UUID uuidABBREV_LSI_BA = UUID.fromString("3cc15dc7-bc55-4a9b-85af-e1eb733ad845");
	private static final UUID uuidABBREV_LSI_ET = UUID.fromString("f2b95c95-199c-4627-ab18-3659b03033b8");
	private static final UUID uuidABBREV_LSI_LS = UUID.fromString("d2c7fdb0-4542-4a56-84a0-e2ab9818a145");
	private static final UUID uuidABBREV_MAG_OO = UUID.fromString("0a248a86-bf96-4af5-b95a-b84aae5a59b7");
	private static final UUID uuidABBREV_MAI_OO = UUID.fromString("300fac4e-e2bd-42e6-88c6-26c14cb053e0");
	private static final UUID uuidABBREV_MAN_OO = UUID.fromString("6a0598a7-5460-4c78-bf2f-91ebb688e431");
	private static final UUID uuidABBREV_MAQ_OO = UUID.fromString("a7e70b81-0202-4147-b189-f37a6eb1dfb6");
	private static final UUID uuidABBREV_MAS_OO = UUID.fromString("b7605e8e-a102-465e-9143-864d3512ff9c");
	private static final UUID uuidABBREV_MAU_OO = UUID.fromString("94a49266-6be8-457d-8fab-50c8c9a27d65");
	private static final UUID uuidABBREV_MCI_OO = UUID.fromString("63a0bcf3-ae45-4e54-8cd7-8f37b1745319");
	private static final UUID uuidABBREV_MCS_OO = UUID.fromString("7d8b6b44-1010-4cb6-8ccc-1e3148c9f6a7");
	private static final UUID uuidABBREV_MDG_OO = UUID.fromString("ec823c51-f92c-4d55-8c34-322c58b7c11b");
	private static final UUID uuidABBREV_MDR_OO = UUID.fromString("7b9c3af8-4821-451b-ad7e-84eff39d1316");
	private static final UUID uuidABBREV_MDV_OO = UUID.fromString("92b82e06-06f6-4f32-a2ec-f8820ba5e23f");
	private static final UUID uuidABBREV_MIC_OO = UUID.fromString("342c8ac5-153a-4654-af25-1c829e27affa");
	private static final UUID uuidABBREV_MIN_OO = UUID.fromString("fe01c101-9f13-4a4a-810a-1739da4bdc85");
	private static final UUID uuidABBREV_MLI_OO = UUID.fromString("8d0e548d-2bfd-4c1d-a0ab-2c89de3145fe");
	private static final UUID uuidABBREV_MLW_OO = UUID.fromString("a216d4de-6a7e-4173-ade7-59fd3a96f111");
	private static final UUID uuidABBREV_MLY_PM = UUID.fromString("9f95af33-ae9e-4031-92f7-9f79d22eecf2");
	private static final UUID uuidABBREV_MLY_SI = UUID.fromString("5863702c-ac74-41c4-83ff-ba446140034c");
	private static final UUID uuidABBREV_MNT_OO = UUID.fromString("b0ba9c44-74a9-41e6-b896-3b4492b5c6b0");
	private static final UUID uuidABBREV_MOL_OO = UUID.fromString("0ac8fc74-a081-45e5-b96d-aad367b5f48b");
	private static final UUID uuidABBREV_MON_OO = UUID.fromString("b7124d3b-fa04-4651-9267-6a2ad2a503bc");
	private static final UUID uuidABBREV_MOR_MO = UUID.fromString("3bd32d2a-b3b8-440e-8afb-e6bd2b00c887");
	private static final UUID uuidABBREV_MOR_SP = UUID.fromString("62868cfb-b94b-40fe-9128-8d4620d36619");
	private static final UUID uuidABBREV_MOZ_OO = UUID.fromString("2bdda236-f695-4a46-b4c5-a1041b428a85");
	private static final UUID uuidABBREV_MPE_OO = UUID.fromString("80f83d1a-3f0a-4ffd-9e72-91271472ad72");
	private static final UUID uuidABBREV_MRN_GU = UUID.fromString("aa220148-96c0-4580-a8ff-f0ecf6a66aa8");
	private static final UUID uuidABBREV_MRN_NM = UUID.fromString("2fa7b7f4-6427-49d8-87e7-107c15367df6");
	private static final UUID uuidABBREV_MRQ_OO = UUID.fromString("eb68f63a-c660-490e-9c2f-d8e161cf703b");
	private static final UUID uuidABBREV_MRS_OO = UUID.fromString("fd6c8607-20c1-436b-bd62-a425b0d7b4fd");
	private static final UUID uuidABBREV_MRY_OO = UUID.fromString("a270a0a7-4f1b-43db-9ebe-b71968ee3013");
	private static final UUID uuidABBREV_MSI_OO = UUID.fromString("dbd3c366-9bb8-4f38-8b52-a2f208af02ce");
	private static final UUID uuidABBREV_MSO_OO = UUID.fromString("0e78f84c-2830-49ee-bd03-0e605ed7220f");
	private static final UUID uuidABBREV_MTN_OO = UUID.fromString("e4f11abe-181d-4c15-b37a-28cec84d5622");
	private static final UUID uuidABBREV_MXC_DF = UUID.fromString("565751f1-613e-4ddc-bfbb-4b54f2267971");
	private static final UUID uuidABBREV_MXC_ME = UUID.fromString("006d80e0-d30c-4151-97c6-1551bfc4ee6a");
	private static final UUID uuidABBREV_MXC_MO = UUID.fromString("d7a02375-0f0a-4bf6-9b70-b3c6e01cf1a4");
	private static final UUID uuidABBREV_MXC_PU = UUID.fromString("72ad92cb-64b7-4e71-a8a3-9a07157e9ab2");
	private static final UUID uuidABBREV_MXC_TL = UUID.fromString("9e6a77a5-1b7c-4b2f-a6ef-e1ea8bdd5d6d");
	private static final UUID uuidABBREV_MXE_AG = UUID.fromString("ed5dd795-2869-4d22-aee4-00de70b27f5a");
	private static final UUID uuidABBREV_MXE_CO = UUID.fromString("d4e5a892-b9d1-499d-af26-cdc4aaadfdee");
	private static final UUID uuidABBREV_MXE_CU = UUID.fromString("4fa2b3d9-4bc5-405a-8c81-33cfd3b64181");
	private static final UUID uuidABBREV_MXE_DU = UUID.fromString("81ac3077-c182-4acc-9136-62dc8caaee00");
	private static final UUID uuidABBREV_MXE_GU = UUID.fromString("a50f3b86-2ea1-4ab3-a29b-e17859ef7bef");
	private static final UUID uuidABBREV_MXE_HI = UUID.fromString("28c68418-3220-418f-b48a-323ac65aa749");
	private static final UUID uuidABBREV_MXE_NL = UUID.fromString("fd6cd489-ac26-48e7-86be-93defbd2957b");
	private static final UUID uuidABBREV_MXE_QU = UUID.fromString("55bd3283-0fcc-4875-b47c-6182a7b1fdc9");
	private static final UUID uuidABBREV_MXE_SL = UUID.fromString("8d400ef1-b3a2-4abb-806c-58f442a20587");
	private static final UUID uuidABBREV_MXE_TA = UUID.fromString("9c6bc35f-2da8-419f-a7bf-08317e8ae0cb");
	private static final UUID uuidABBREV_MXE_ZA = UUID.fromString("6af99608-01a6-490d-95f5-36a41bc0059f");
	private static final UUID uuidABBREV_MXG_VC = UUID.fromString("de72ef9f-98ed-4f36-80e6-f830ec7622ca");
	private static final UUID uuidABBREV_MXI_GU = UUID.fromString("7f3f2d42-a712-4747-8344-1e99467ef434");
	private static final UUID uuidABBREV_MXI_RA = UUID.fromString("7d07cc5e-1a0d-4b6d-b747-4797a664d744");
	private static final UUID uuidABBREV_MXI_RG = UUID.fromString("6dfaa758-e76c-48cc-9c8b-08800defc8a4");
	private static final UUID uuidABBREV_MXN_BC = UUID.fromString("5c143c06-c456-4dc1-9ce9-7745d255c237");
	private static final UUID uuidABBREV_MXN_BS = UUID.fromString("ead11f6e-743e-4fc7-859c-d78acae4e88d");
	private static final UUID uuidABBREV_MXN_SI = UUID.fromString("6e1e23de-b355-417b-8ca7-fa7ddd387a92");
	private static final UUID uuidABBREV_MXN_SO = UUID.fromString("68718c56-846c-4994-ae7d-1f5cbb5c33bc");
	private static final UUID uuidABBREV_MXS_CL = UUID.fromString("b2ea91ed-c667-4fd2-81ac-9df65c394cc4");
	private static final UUID uuidABBREV_MXS_GR = UUID.fromString("b1b2d3b2-fe30-47e4-96c3-e109dd39473b");
	private static final UUID uuidABBREV_MXS_JA = UUID.fromString("2b07b916-ff4c-4c73-b6df-7c893852c33b");
	private static final UUID uuidABBREV_MXS_MI = UUID.fromString("3a30f064-6491-41cd-872a-2fec5017c2dd");
	private static final UUID uuidABBREV_MXS_NA = UUID.fromString("bb3838e9-8815-42e7-9471-3f43cb014194");
	private static final UUID uuidABBREV_MXS_OA = UUID.fromString("c17bdb19-2c0d-4745-b0c7-f234a38e039d");
	private static final UUID uuidABBREV_MXT_CA = UUID.fromString("47d81f12-741e-4ffe-aaa9-1fee6b2b4463");
	private static final UUID uuidABBREV_MXT_CI = UUID.fromString("3a88bbee-1381-4ce6-a921-8e75d3eccaa6");
	private static final UUID uuidABBREV_MXT_QR = UUID.fromString("e1b3c800-dde0-465e-acd8-7a155b88728b");
	private static final UUID uuidABBREV_MXT_TB = UUID.fromString("536476da-d49c-4c90-885c-1aaf6ef033e3");
	private static final UUID uuidABBREV_MXT_YU = UUID.fromString("1519ea7d-d7e4-48aa-9f24-00bbd744ea31");
	private static final UUID uuidABBREV_MYA_OO = UUID.fromString("fc6ff9e5-0e21-4109-aea9-832ec8aebaf3");
	private static final UUID uuidABBREV_NAM_OO = UUID.fromString("c9139aa6-a098-483e-be35-39c65f8d56ea");
	private static final UUID uuidABBREV_NAT_OO = UUID.fromString("550bb42c-37e2-4223-a00b-607348d8d5bb");
	private static final UUID uuidABBREV_NBR_OO = UUID.fromString("a7efd881-26d3-4d51-8eb7-ead43374c9f4");
	private static final UUID uuidABBREV_NCA_OO = UUID.fromString("c0f8789f-6c3f-4af8-8391-ad990fb40965");
	private static final UUID uuidABBREV_NCB_OO = UUID.fromString("1338a3d3-10c3-47ee-ab71-3c64c4ab2c78");
	private static final UUID uuidABBREV_NCS_CH = UUID.fromString("d60d5797-61b2-422a-a87c-66e577474ea2");
	private static final UUID uuidABBREV_NCS_DA = UUID.fromString("57588bf4-3157-4fd5-bf6a-369b59a33fc6");
	private static final UUID uuidABBREV_NCS_IN = UUID.fromString("3bff72cd-fef9-45f6-9bdc-d16c31210427");
	private static final UUID uuidABBREV_NCS_KB = UUID.fromString("37dcfe51-6e78-4fa5-9d03-a0d97273bb51");
	private static final UUID uuidABBREV_NCS_KC = UUID.fromString("48d3d918-8591-4117-a32c-56403761db7d");
	private static final UUID uuidABBREV_NCS_KR = UUID.fromString("d9548675-9a6d-4f5b-9efc-d8e499971be6");
	private static final UUID uuidABBREV_NCS_SO = UUID.fromString("22cd14fa-fa0d-4859-b8d6-b583befecb89");
	private static final UUID uuidABBREV_NCS_ST = UUID.fromString("ec8a2502-84f0-4d70-aa55-a663475f5c6b");
	private static final UUID uuidABBREV_NDA_OO = UUID.fromString("2286d950-3f8b-4e53-9d9d-cf0c448c6f5f");
	private static final UUID uuidABBREV_NEB_OO = UUID.fromString("8567fe51-9c84-4f57-94ee-c7f9f2ce7e3a");
	private static final UUID uuidABBREV_NEP_OO = UUID.fromString("aca8b819-54a8-4ee4-8e22-f9839df98975");
	private static final UUID uuidABBREV_NET_OO = UUID.fromString("f1d92cb8-9bb1-48d8-b395-eafa6d4162c3");
	private static final UUID uuidABBREV_NEV_OO = UUID.fromString("35be6728-3c0f-4aa6-aa2c-8e4d9e60b8fe");
	private static final UUID uuidABBREV_NFK_LH = UUID.fromString("2e33a45c-c121-47e6-bd9b-cabf3d930598");
	private static final UUID uuidABBREV_NFK_NI = UUID.fromString("97c96422-31c6-4b12-b820-31818e30673d");
	private static final UUID uuidABBREV_NFL_NE = UUID.fromString("15bc6921-9726-49c1-978a-f927c6966ea7");
	private static final UUID uuidABBREV_NFL_SP = UUID.fromString("8f19cf83-7a19-42c6-8876-4d628fe24671");
	private static final UUID uuidABBREV_NGA_OO = UUID.fromString("7e7a213d-f8d2-4220-98ca-247ca9ab68fa");
	private static final UUID uuidABBREV_NGR_OO = UUID.fromString("bb7c7af5-78ef-4197-b504-67ff1587a43d");
	private static final UUID uuidABBREV_NIC_OO = UUID.fromString("00fcf611-abaf-449a-a70b-79a5762e0736");
	private static final UUID uuidABBREV_NLA_BO = UUID.fromString("521f5c71-6c4c-490b-a042-eeca1f68ceeb");
	private static final UUID uuidABBREV_NLA_CU = UUID.fromString("4a3532c5-9d47-4d1d-ae56-2555384058e1");
	private static final UUID uuidABBREV_NNS_OO = UUID.fromString("db3d6a93-5ffa-4803-8004-9cfe08c5ce44");
	private static final UUID uuidABBREV_NOR_OO = UUID.fromString("57d16bae-777b-4bbb-a3b9-02b340239571");
	private static final UUID uuidABBREV_NRU_OO = UUID.fromString("0e5b9e48-f7c9-42ed-8baa-32cec6f13fb4");
	private static final UUID uuidABBREV_NSC_OO = UUID.fromString("e6914731-7bb6-4cdc-bd20-5f9830f921a9");
	private static final UUID uuidABBREV_NSW_CT = UUID.fromString("ab26e107-d801-46a3-9433-7f13f767a65b");
	private static final UUID uuidABBREV_NSW_NS = UUID.fromString("471a9abf-8230-4e75-b2ec-f12ce14736ee");
	private static final UUID uuidABBREV_NTA_OO = UUID.fromString("178cc276-24c8-4b9b-9afa-02431a1bec6c");
	private static final UUID uuidABBREV_NUE_OO = UUID.fromString("9e31a2ec-c729-4908-93fb-056321e79e34");
	private static final UUID uuidABBREV_NUN_OO = UUID.fromString("c316be0e-7711-4ede-b542-290ae3b8b986");
	private static final UUID uuidABBREV_NWC_OO = UUID.fromString("4d84243b-2b9c-4de9-8510-f78875cfab41");
	private static final UUID uuidABBREV_NWG_IJ = UUID.fromString("f1a7f887-741a-427c-bbbc-4c13a8c26659");
	private static final UUID uuidABBREV_NWG_PN = UUID.fromString("0145c796-4ebf-4398-bad6-614a264515b4");
	private static final UUID uuidABBREV_NWH_OO = UUID.fromString("bf3ee613-1c24-4cb9-9adc-4d06f9dcae33");
	private static final UUID uuidABBREV_NWJ_OO = UUID.fromString("52627b21-09d0-49ee-995b-773bcd2d2736");
	private static final UUID uuidABBREV_NWM_OO = UUID.fromString("f3bc6a47-5caf-410d-b418-8a48f3632c38");
	private static final UUID uuidABBREV_NWT_OO = UUID.fromString("71a7806f-9b2b-4b1f-92ef-7b104424e4df");
	private static final UUID uuidABBREV_NWY_OO = UUID.fromString("bfdc923e-b808-40bf-b22a-fad59cfe3517");
	private static final UUID uuidABBREV_NZN_OO = UUID.fromString("33ae2ea4-4472-41f8-92d0-b2d22c045dbc");
	private static final UUID uuidABBREV_NZS_OO = UUID.fromString("3add1095-b6a9-43f5-9ce8-2797b4db328b");
	private static final UUID uuidABBREV_OFS_OO = UUID.fromString("3ef48a2a-4ef5-4807-ab05-9559c5305ab8");
	private static final UUID uuidABBREV_OGA_OO = UUID.fromString("e8cd281f-eba5-4529-b496-bb7d04221d73");
	private static final UUID uuidABBREV_OHI_OO = UUID.fromString("8e842dc3-6d31-4d65-a250-63a495d9c46f");
	private static final UUID uuidABBREV_OKL_OO = UUID.fromString("54beb9ec-857c-4149-b38d-db30e257515d");
	private static final UUID uuidABBREV_OMA_OO = UUID.fromString("3c4f004b-51d2-488e-9d04-8c393cdba220");
	private static final UUID uuidABBREV_ONT_OO = UUID.fromString("979889b5-ff89-41f6-96bb-27c44cf764a3");
	private static final UUID uuidABBREV_ORE_OO = UUID.fromString("c8ba946f-6401-4b77-ac70-04a30d8ea65d");
	private static final UUID uuidABBREV_PAK_OO = UUID.fromString("5a80fd55-5fe7-44d9-bdf4-13633d81f270");
	private static final UUID uuidABBREV_PAL_IS = UUID.fromString("8cf6a459-8803-48a3-a7d6-e840681bb604");
	private static final UUID uuidABBREV_PAL_JO = UUID.fromString("620b8ef7-d129-44a4-aeab-9b82b58988e7");
	private static final UUID uuidABBREV_PAN_OO = UUID.fromString("077b8b6c-f1fa-4e43-abce-eb2c0942f215");
	private static final UUID uuidABBREV_PAR_OO = UUID.fromString("de12ce18-4f5e-49b8-92d6-d6ca633084af");
	private static final UUID uuidABBREV_PEI_OO = UUID.fromString("4365bc4c-8edf-4827-a727-3e3dc2a48c99");
	private static final UUID uuidABBREV_PEN_OO = UUID.fromString("2f071a87-ed8d-41ab-bc6c-4a2e86fa33b6");
	private static final UUID uuidABBREV_PER_OO = UUID.fromString("d58447cc-7425-43f1-b1a6-221dd5591e13");
	private static final UUID uuidABBREV_PHI_OO = UUID.fromString("9dee4b0d-d864-4b6f-bd41-39a1ea7c56c3");
	private static final UUID uuidABBREV_PHX_OO = UUID.fromString("02ab5796-bdfd-40c1-bf48-79d7f9b54c0b");
	private static final UUID uuidABBREV_PIT_OO = UUID.fromString("59152e23-5202-47c0-8897-6ecf021a2240");
	private static final UUID uuidABBREV_POL_OO = UUID.fromString("8284f453-33b7-457d-acf8-61981112aa26");
	private static final UUID uuidABBREV_POR_OO = UUID.fromString("a4841cd4-4bad-4930-a0c6-61197b15832b");
	private static final UUID uuidABBREV_PRM_OO = UUID.fromString("72486bfa-a330-42c2-8620-20bb6e2d0379");
	private static final UUID uuidABBREV_PUE_OO = UUID.fromString("44de1584-e6ed-4a0f-951e-2e7405180d45");
	private static final UUID uuidABBREV_QLD_CS = UUID.fromString("12ff3212-0a5c-4621-aa96-559bf91b38c0");
	private static final UUID uuidABBREV_QLD_QU = UUID.fromString("a8fb3a37-57a2-4c3e-b4a7-1df394bda83c");
	private static final UUID uuidABBREV_QUE_OO = UUID.fromString("0746cd53-e1fe-4d5e-928c-5a04885c7b3e");
	private static final UUID uuidABBREV_REU_OO = UUID.fromString("836fa04b-e2d7-4749-97f3-46b05f17b7c0");
	private static final UUID uuidABBREV_RHO_OO = UUID.fromString("e9ef0836-b09f-46d6-8d85-655d2a5d06ea");
	private static final UUID uuidABBREV_ROD_OO = UUID.fromString("3fc9befa-8bcf-4cdd-adc6-a8fd0046ad94");
	private static final UUID uuidABBREV_ROM_OO = UUID.fromString("fb4016ae-f667-4662-b3cd-c99aee263a36");
	private static final UUID uuidABBREV_RUC_OO = UUID.fromString("d0cd49af-1bfc-4deb-97e5-c02b0735f684");
	private static final UUID uuidABBREV_RUE_OO = UUID.fromString("d3f6ae29-1f97-4a95-b0ce-6e65cb2fe788");
	private static final UUID uuidABBREV_RUN_OO = UUID.fromString("54c211a3-6767-434f-8176-81099d9ac6b0");
	private static final UUID uuidABBREV_RUS_OO = UUID.fromString("76237f64-c306-4dd5-bff9-f9589507cbc7");
	private static final UUID uuidABBREV_RUW_OO = UUID.fromString("0bd7203a-4d11-45c5-abe4-c35ed0698a52");
	private static final UUID uuidABBREV_RWA_OO = UUID.fromString("35ee7d91-0e7f-402b-af1c-8b35f993de4b");
	private static final UUID uuidABBREV_SAK_OO = UUID.fromString("0ec14c42-9f9c-483f-8bac-49c042f4aac2");
	private static final UUID uuidABBREV_SAM_AS = UUID.fromString("fdcd45a4-495e-40cd-bb06-1944ca4e635a");
	private static final UUID uuidABBREV_SAM_WS = UUID.fromString("58045c98-4614-4ee7-a311-900bbf77a39e");
	private static final UUID uuidABBREV_SAR_OO = UUID.fromString("c2014fa0-f86a-4924-964e-f6693a39a88e");
	private static final UUID uuidABBREV_SAS_OO = UUID.fromString("758b3088-c93d-4625-9500-afe5ec5cfa23");
	private static final UUID uuidABBREV_SAU_OO = UUID.fromString("df0cbba2-e1e2-46f2-abad-71fe13521df8");
	private static final UUID uuidABBREV_SCA_OO = UUID.fromString("fd3ce6ee-65e6-46a7-a59f-e4b283477ce4");
	private static final UUID uuidABBREV_SCI_OO = UUID.fromString("86aaf6ea-8349-4190-9c71-7f7f99bf7ca9");
	private static final UUID uuidABBREV_SCS_PI = UUID.fromString("085f5edc-986f-4283-b787-dc8f870add81");
	private static final UUID uuidABBREV_SCS_SI = UUID.fromString("544c29d6-dfc2-4434-8493-d976eb1da751");
	private static final UUID uuidABBREV_SCZ_OO = UUID.fromString("f2b53289-c139-4aa5-8c7f-56b0a129c295");
	private static final UUID uuidABBREV_SDA_OO = UUID.fromString("023aaa47-04da-4d61-989f-5c9230f67bec");
	private static final UUID uuidABBREV_SEL_OO = UUID.fromString("688c1e74-c207-4b08-8fba-f36289bb1f71");
	private static final UUID uuidABBREV_SEN_OO = UUID.fromString("ecfc254e-ae37-42f6-a338-e8098d89cbac");
	private static final UUID uuidABBREV_SEY_OO = UUID.fromString("8b8ba8f2-e9d1-4561-a030-dda45611e7fd");
	private static final UUID uuidABBREV_SGE_OO = UUID.fromString("8ad99980-84cf-4ff7-b244-c2208c53841e");
	private static final UUID uuidABBREV_SIC_MA = UUID.fromString("4194946a-0642-47d7-80cc-f17701c87f73");
	private static final UUID uuidABBREV_SIC_SI = UUID.fromString("f991f85c-bcc7-497b-9868-1ab18c72b194");
	private static final UUID uuidABBREV_SIE_OO = UUID.fromString("3aca2f99-dbdb-4ade-a336-17a8b537607b");
	private static final UUID uuidABBREV_SIN_OO = UUID.fromString("b7fa88cd-05bf-4abe-8b90-5dbf38465b28");
	private static final UUID uuidABBREV_SOA_OO = UUID.fromString("c92577a8-51a3-459d-9ab0-41c5edeca450");
	private static final UUID uuidABBREV_SOC_OO = UUID.fromString("e2707cb9-8929-435a-8c61-1c10c7889d9a");
	private static final UUID uuidABBREV_SOL_NO = UUID.fromString("6969efec-5333-491c-9499-8db5ee2918ed");
	private static final UUID uuidABBREV_SOL_SO = UUID.fromString("e61558d4-7bcf-43ca-aa40-7ec7ffb74c1f");
	private static final UUID uuidABBREV_SOM_OO = UUID.fromString("176373e5-435f-40ac-8d8f-bf2b4963a9a6");
	private static final UUID uuidABBREV_SPA_AN = UUID.fromString("095b8b6d-44c5-4d46-9b18-99d8caf396ba");
	private static final UUID uuidABBREV_SPA_GI = UUID.fromString("fba94126-08ec-4a91-95d4-de10f2b27f03");
	private static final UUID uuidABBREV_SPA_SP = UUID.fromString("0113409c-7dd8-4601-b261-61d0ba43795d");
	private static final UUID uuidABBREV_SRL_OO = UUID.fromString("dd22e5da-092c-4610-816c-bc6c3ad91620");
	private static final UUID uuidABBREV_SSA_OO = UUID.fromString("b5be639a-1ec5-4be3-81e4-0f769a3a785c");
	private static final UUID uuidABBREV_STH_OO = UUID.fromString("97cc1d7f-18fa-4c77-b880-5d1ce07710da");
	private static final UUID uuidABBREV_SUD_OO = UUID.fromString("1db7ec41-05b3-4964-8da3-6d2d91332fc7");
	private static final UUID uuidABBREV_SUL_OO = UUID.fromString("bfc07508-8ee2-413f-841e-d44a0c09e010");
	private static final UUID uuidABBREV_SUM_OO = UUID.fromString("4395d5e0-64e2-4309-a2ec-b1240919c34d");
	private static final UUID uuidABBREV_SUR_OO = UUID.fromString("1e3af172-ad4b-4a4e-af30-ac4491b23b01");
	private static final UUID uuidABBREV_SVA_OO = UUID.fromString("faa8e353-60b2-4775-99eb-9873f5216490");
	private static final UUID uuidABBREV_SWC_CC = UUID.fromString("d9dadba7-eefd-42dc-99d0-7001b0316ea4");
	private static final UUID uuidABBREV_SWC_HC = UUID.fromString("78cf3647-af9f-4aff-a326-72dce23adae3");
	private static final UUID uuidABBREV_SWC_NC = UUID.fromString("bffeb089-afaf-4f04-b134-39cae5a45881");
	private static final UUID uuidABBREV_SWE_OO = UUID.fromString("1acae55b-f2ad-491d-8367-8ca76a3228e9");
	private static final UUID uuidABBREV_SWI_OO = UUID.fromString("0d353eb2-2fd8-4843-ba1f-c6873e703869");
	private static final UUID uuidABBREV_SWZ_OO = UUID.fromString("3e9fce6c-9f4b-42af-92b4-0a81e598c004");
	private static final UUID uuidABBREV_TAI_OO = UUID.fromString("17f23bcb-9dfb-46ed-aaee-742426008957");
	private static final UUID uuidABBREV_TAN_OO = UUID.fromString("06153999-40d8-4298-8ae1-0bb148e4b5a4");
	private static final UUID uuidABBREV_TAS_OO = UUID.fromString("0f422108-51d2-4dc9-b11b-03fc430a8994");
	private static final UUID uuidABBREV_TCI_OO = UUID.fromString("e293eda7-9bc3-4d2d-86bb-224f8f2f7e4a");
	private static final UUID uuidABBREV_TCS_AB = UUID.fromString("48da1791-103c-4505-bb69-cc780bdbac2d");
	private static final UUID uuidABBREV_TCS_AD = UUID.fromString("532a0762-7d64-4667-8590-5380ae39974c");
	private static final UUID uuidABBREV_TCS_AR = UUID.fromString("17ce9ce4-ca6e-4c13-b858-5f23a89c2eb7");
	private static final UUID uuidABBREV_TCS_AZ = UUID.fromString("6f85062e-1a30-4cbe-9c3a-96e3cd68ca61");
	private static final UUID uuidABBREV_TCS_GR = UUID.fromString("204ca32d-74c7-4288-ab0c-3013d24d92e1");
	private static final UUID uuidABBREV_TCS_NA = UUID.fromString("c3c37338-5f1f-447c-8948-bf20ce98f1f8");
	private static final UUID uuidABBREV_TCS_NK = UUID.fromString("68d8c6e8-7666-414e-9579-b1604592694d");
	private static final UUID uuidABBREV_TDC_OO = UUID.fromString("3ade1654-2d18-4dca-be14-f5c7647f7bc4");
	private static final UUID uuidABBREV_TEN_OO = UUID.fromString("b71f5f89-708d-4010-a1f3-107671105abe");
	private static final UUID uuidABBREV_TEX_OO = UUID.fromString("a5353ceb-adac-43e2-80b7-785a383da5c9");
	private static final UUID uuidABBREV_THA_OO = UUID.fromString("9806016c-be9c-4d35-ae67-d45abba590be");
	private static final UUID uuidABBREV_TKM_OO = UUID.fromString("f18f0d78-d2fb-4e24-86d5-783067a25291");
	private static final UUID uuidABBREV_TOG_OO = UUID.fromString("e93c18ac-ed8b-4b2f-8e22-6aa9bf03b1e5");
	private static final UUID uuidABBREV_TOK_MA = UUID.fromString("c7293bdb-28dc-429a-aa66-2732e0d12d1f");
	private static final UUID uuidABBREV_TOK_SW = UUID.fromString("5fa5df7b-f0f3-461c-9f29-11d40b183109");
	private static final UUID uuidABBREV_TOK_TO = UUID.fromString("c1e3819c-9c05-4559-b6de-a4d792c9edc1");
	private static final UUID uuidABBREV_TON_OO = UUID.fromString("acf51c02-d09e-4f57-8ee7-4ea82949f6a2");
	private static final UUID uuidABBREV_TRT_OO = UUID.fromString("d96d2d70-1959-45b4-90e5-692b0bc8468c");
	private static final UUID uuidABBREV_TUA_OO = UUID.fromString("f981d953-a271-4e39-ade7-c89e41e550be");
	private static final UUID uuidABBREV_TUB_OO = UUID.fromString("ceeabdbc-bf13-494a-869a-d476956b8b60");
	private static final UUID uuidABBREV_TUE_OO = UUID.fromString("b7ea03d2-a7f0-44bf-995f-c5a0e352480a");
	private static final UUID uuidABBREV_TUN_OO = UUID.fromString("c58cbd17-e60e-4adf-a970-2d37f3fedb77");
	private static final UUID uuidABBREV_TUR_OO = UUID.fromString("60a9219e-136e-4ac1-92a3-1b889e473c53");
	private static final UUID uuidABBREV_TUV_OO = UUID.fromString("e642ec34-2f94-4ccb-b830-e0278982919a");
	private static final UUID uuidABBREV_TVA_OO = UUID.fromString("0b87994a-c9dd-4dff-8d38-1a11abdad2ae");
	private static final UUID uuidABBREV_TVL_GA = UUID.fromString("7ef2a035-7ac4-44c8-aa40-84adb96041e3");
	private static final UUID uuidABBREV_TVL_MP = UUID.fromString("d82a9d1e-e16a-4612-9507-94757ec66227");
	private static final UUID uuidABBREV_TVL_NP = UUID.fromString("a577d31f-aae1-4aa8-aebc-2a1282b011f8");
	private static final UUID uuidABBREV_TVL_NW = UUID.fromString("b5f4b104-2732-4126-8bd7-b55fb3198a29");
	private static final UUID uuidABBREV_TZK_OO = UUID.fromString("ff5b262e-f7e3-468b-a5e0-a15e86af5c1d");
	private static final UUID uuidABBREV_UGA_OO = UUID.fromString("c87a4681-f30e-459a-b633-b3cf66822017");
	private static final UUID uuidABBREV_UKR_MO = UUID.fromString("ddac03eb-c533-4fd2-a83d-28d9dce0864d");
	private static final UUID uuidABBREV_UKR_UK = UUID.fromString("79870854-1fef-43fb-9eb5-19faecf600d3");
	private static final UUID uuidABBREV_URU_OO = UUID.fromString("91ce37ea-8af3-42e1-873d-902583539bdf");
	private static final UUID uuidABBREV_UTA_OO = UUID.fromString("1fd7ecde-8ecd-4cd3-b180-bb44db1fab1e");
	private static final UUID uuidABBREV_UZB_OO = UUID.fromString("1388a0b4-1833-48d1-94a0-73351999eebc");
	private static final UUID uuidABBREV_VAN_OO = UUID.fromString("5608de8e-f327-49d3-9a04-0ac0405d34ad");
	private static final UUID uuidABBREV_VEN_OO = UUID.fromString("0e049bd9-c6ce-4e17-85fa-83c61ff90621");
	private static final UUID uuidABBREV_VER_OO = UUID.fromString("216d17b2-9b14-4ee5-bfda-6e82a573b704");
	private static final UUID uuidABBREV_VIC_OO = UUID.fromString("519eb6b6-bed7-405c-9c5e-dd9f621be288");
	private static final UUID uuidABBREV_VIE_OO = UUID.fromString("fbb8ab2c-d526-472f-b557-c96cc03bc37c");
	private static final UUID uuidABBREV_VNA_OO = UUID.fromString("e7d392e6-5e48-4485-b18c-25fb140d3e1f");
	private static final UUID uuidABBREV_VRG_OO = UUID.fromString("24153857-fdf0-4d1d-adb0-b42ac71a992a");
	private static final UUID uuidABBREV_WAK_OO = UUID.fromString("f688e0c1-7245-4b78-9fff-be767a5bb232");
	private static final UUID uuidABBREV_WAL_OO = UUID.fromString("7fcf37f8-b846-4fff-a2d7-266483dfc70c");
	private static final UUID uuidABBREV_WAS_OO = UUID.fromString("dbc6bfc1-ede6-4620-84d5-890907c252e0");
	private static final UUID uuidABBREV_WAU_AC = UUID.fromString("63904a7f-67ca-458b-86b3-9b5a7e68923a");
	private static final UUID uuidABBREV_WAU_WA = UUID.fromString("97ac40c6-ee0e-4482-88da-8de66a73401b");
	private static final UUID uuidABBREV_WDC_OO = UUID.fromString("6bd2baf4-972a-467f-b6fa-8b89aee1f61a");
	private static final UUID uuidABBREV_WHM_HP = UUID.fromString("de8b64ce-3a33-4135-a850-cf109f1d48bd");
	private static final UUID uuidABBREV_WHM_JK = UUID.fromString("c90618b9-167d-49ec-9724-259029fa00c7");
	private static final UUID uuidABBREV_WHM_UT = UUID.fromString("b8d5a12f-e7e7-4c19-b64c-ead0fdc97b4e");
	private static final UUID uuidABBREV_WIN_BA = UUID.fromString("9095452e-59f7-4608-af53-341e5f400d2a");
	private static final UUID uuidABBREV_WIN_DO = UUID.fromString("70933eef-4cb9-403c-95cd-314d38d99a8e");
	private static final UUID uuidABBREV_WIN_GR = UUID.fromString("d91f8b52-afb4-4a05-9a2b-0091c1a2964e");
	private static final UUID uuidABBREV_WIN_MA = UUID.fromString("dfb9ea63-1947-4f42-b60e-0b3469e43e8c");
	private static final UUID uuidABBREV_WIN_SL = UUID.fromString("31468b28-6e4a-4810-a9a9-73ee02916f3c");
	private static final UUID uuidABBREV_WIN_SV = UUID.fromString("3d4c2754-2000-44f6-a0a8-0d9672cba17f");
	private static final UUID uuidABBREV_WIS_OO = UUID.fromString("74ff3c89-304c-4ca7-8e57-bda96cc64788");
	private static final UUID uuidABBREV_WSA_OO = UUID.fromString("f5e1293c-477f-4989-a51a-5573ebdf2163");
	private static final UUID uuidABBREV_WSB_OO = UUID.fromString("b171a390-4b69-4e57-89bf-8d334368aa70");
	private static final UUID uuidABBREV_WVA_OO = UUID.fromString("eec1ea9a-67fd-4ad5-a241-6cf73a02434c");
	private static final UUID uuidABBREV_WYO_OO = UUID.fromString("cc136338-64d2-4d20-bd45-d40fc92909a7");
	private static final UUID uuidABBREV_XMS_OO = UUID.fromString("932cacfd-e349-4448-8269-ff6b06c260ee");
	private static final UUID uuidABBREV_YAK_OO = UUID.fromString("2b8feedf-6fd4-4763-9f92-2f1dfd04c4b2");
	private static final UUID uuidABBREV_YEM_NY = UUID.fromString("a2a90c7d-905a-408f-afc2-9ee9165667df");
	private static final UUID uuidABBREV_YEM_SY = UUID.fromString("e4fd740f-2401-4744-aa51-38b66848221c");
	private static final UUID uuidABBREV_YUG_BH = UUID.fromString("156f41e9-7f2d-4c56-ace5-db9581e35b7e");
	private static final UUID uuidABBREV_YUG_CR = UUID.fromString("7d7e1b3b-77c0-4033-9fd0-3caa6370d8a9");
	private static final UUID uuidABBREV_YUG_KO = UUID.fromString("5b8e22d1-2438-4433-8853-fac9768aea58");
	private static final UUID uuidABBREV_YUG_MA = UUID.fromString("7dff4ac4-6c16-4834-9922-93f5af568833");
	private static final UUID uuidABBREV_YUG_MN = UUID.fromString("d18168ff-f59d-4209-8b23-bd3abeda1325");
	private static final UUID uuidABBREV_YUG_SE = UUID.fromString("dc59ecd3-0051-4e81-84a2-9fd459861011");
	private static final UUID uuidABBREV_YUG_SL = UUID.fromString("7d8e85ca-fc06-4a4b-9bb7-06d4b42c15f6");
	private static final UUID uuidABBREV_YUK_OO = UUID.fromString("0e4a1319-6c7d-48a6-af08-0d972542e3e2");
	private static final UUID uuidABBREV_ZAI_OO = UUID.fromString("023f83b8-dc99-4ac0-80b7-79fe2678518f");
	private static final UUID uuidABBREV_ZAM_OO = UUID.fromString("597b7676-a24d-4eaf-b7a9-dec4a2ecc712");
	private static final UUID uuidABBREV_ZIM_OO = UUID.fromString("4cc60186-d723-4ff6-913a-5dd8d293a92a");


	 public static final TdwgArea ABBREV_None () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_None );}
	 public static final TdwgArea ABBREV_1 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_1 );}
	 public static final TdwgArea ABBREV_2 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_2 );}
	 public static final TdwgArea ABBREV_3 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_3 );}
	 public static final TdwgArea ABBREV_4 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_4 );}
	 public static final TdwgArea ABBREV_5 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_5 );}
	 public static final TdwgArea ABBREV_6 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_6 );}
	 public static final TdwgArea ABBREV_7 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_7 );}
	 public static final TdwgArea ABBREV_8 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_8 );}
	 public static final TdwgArea ABBREV_9 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_9 );}
	 public static final TdwgArea ABBREV_10 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_10 );}
	 public static final TdwgArea ABBREV_11 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_11 );}
	 public static final TdwgArea ABBREV_12 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_12 );}
	 public static final TdwgArea ABBREV_13 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_13 );}
	 public static final TdwgArea ABBREV_14 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_14 );}
	 public static final TdwgArea ABBREV_20 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_20 );}
	 public static final TdwgArea ABBREV_21 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_21 );}
	 public static final TdwgArea ABBREV_22 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_22 );}
	 public static final TdwgArea ABBREV_23 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_23 );}
	 public static final TdwgArea ABBREV_24 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_24 );}
	 public static final TdwgArea ABBREV_25 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_25 );}
	 public static final TdwgArea ABBREV_26 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_26 );}
	 public static final TdwgArea ABBREV_27 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_27 );}
	 public static final TdwgArea ABBREV_28 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_28 );}
	 public static final TdwgArea ABBREV_29 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_29 );}
	 public static final TdwgArea ABBREV_30 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_30 );}
	 public static final TdwgArea ABBREV_31 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_31 );}
	 public static final TdwgArea ABBREV_32 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_32 );}
	 public static final TdwgArea ABBREV_33 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_33 );}
	 public static final TdwgArea ABBREV_34 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_34 );}
	 public static final TdwgArea ABBREV_35 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_35 );}
	 public static final TdwgArea ABBREV_36 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_36 );}
	 public static final TdwgArea ABBREV_37 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_37 );}
	 public static final TdwgArea ABBREV_38 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_38 );}
	 public static final TdwgArea ABBREV_40 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_40 );}
	 public static final TdwgArea ABBREV_41 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_41 );}
	 public static final TdwgArea ABBREV_42 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_42 );}
	 public static final TdwgArea ABBREV_43 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_43 );}
	 public static final TdwgArea ABBREV_50 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_50 );}
	 public static final TdwgArea ABBREV_51 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_51 );}
	 public static final TdwgArea ABBREV_60 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_60 );}
	 public static final TdwgArea ABBREV_61 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_61 );}
	 public static final TdwgArea ABBREV_62 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_62 );}
	 public static final TdwgArea ABBREV_63 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_63 );}
	 public static final TdwgArea ABBREV_70 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_70 );}
	 public static final TdwgArea ABBREV_71 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_71 );}
	 public static final TdwgArea ABBREV_72 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_72 );}
	 public static final TdwgArea ABBREV_73 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_73 );}
	 public static final TdwgArea ABBREV_74 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_74 );}
	 public static final TdwgArea ABBREV_75 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_75 );}
	 public static final TdwgArea ABBREV_76 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_76 );}
	 public static final TdwgArea ABBREV_77 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_77 );}
	 public static final TdwgArea ABBREV_78 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_78 );}
	 public static final TdwgArea ABBREV_79 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_79 );}
	 public static final TdwgArea ABBREV_80 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_80 );}
	 public static final TdwgArea ABBREV_81 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_81 );}
	 public static final TdwgArea ABBREV_82 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_82 );}
	 public static final TdwgArea ABBREV_83 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_83 );}
	 public static final TdwgArea ABBREV_84 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_84 );}
	 public static final TdwgArea ABBREV_85 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_85 );}
	 public static final TdwgArea ABBREV_90 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_90 );}
	 public static final TdwgArea ABBREV_91 () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_91 );}
	 public static final TdwgArea ABBREV_ABT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ABT );}
	 public static final TdwgArea ABBREV_AFG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AFG );}
	 public static final TdwgArea ABBREV_AGE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE );}
	 public static final TdwgArea ABBREV_AGS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS );}
	 public static final TdwgArea ABBREV_AGW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW );}
	 public static final TdwgArea ABBREV_ALA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALA );}
	 public static final TdwgArea ABBREV_ALB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALB );}
	 public static final TdwgArea ABBREV_ALD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALD );}
	 public static final TdwgArea ABBREV_ALG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALG );}
	 public static final TdwgArea ABBREV_ALT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALT );}
	 public static final TdwgArea ABBREV_ALU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALU );}
	 public static final TdwgArea ABBREV_AMU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AMU );}
	 public static final TdwgArea ABBREV_AND () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AND );}
	 public static final TdwgArea ABBREV_ANG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ANG );}
	 public static final TdwgArea ABBREV_ANT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ANT );}
	 public static final TdwgArea ABBREV_ARI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARI );}
	 public static final TdwgArea ABBREV_ARK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARK );}
	 public static final TdwgArea ABBREV_ARU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARU );}
	 public static final TdwgArea ABBREV_ASC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASC );}
	 public static final TdwgArea ABBREV_ASK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASK );}
	 public static final TdwgArea ABBREV_ASP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASP );}
	 public static final TdwgArea ABBREV_ASS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS );}
	 public static final TdwgArea ABBREV_ATP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ATP );}
	 public static final TdwgArea ABBREV_AUT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AUT );}
	 public static final TdwgArea ABBREV_AZO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AZO );}
	 public static final TdwgArea ABBREV_BAH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAH );}
	 public static final TdwgArea ABBREV_BAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAL );}
	 public static final TdwgArea ABBREV_BAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAN );}
	 public static final TdwgArea ABBREV_BEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BEN );}
	 public static final TdwgArea ABBREV_BER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BER );}
	 public static final TdwgArea ABBREV_BGM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BGM );}
	 public static final TdwgArea ABBREV_BIS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BIS );}
	 public static final TdwgArea ABBREV_BKN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BKN );}
	 public static final TdwgArea ABBREV_BLR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLR );}
	 public static final TdwgArea ABBREV_BLT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLT );}
	 public static final TdwgArea ABBREV_BLZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLZ );}
	 public static final TdwgArea ABBREV_BOL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOL );}
	 public static final TdwgArea ABBREV_BOR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOR );}
	 public static final TdwgArea ABBREV_BOT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOT );}
	 public static final TdwgArea ABBREV_BOU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOU );}
	 public static final TdwgArea ABBREV_BRC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BRC );}
	 public static final TdwgArea ABBREV_BRY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BRY );}
	 public static final TdwgArea ABBREV_BUL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BUL );}
	 public static final TdwgArea ABBREV_BUR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BUR );}
	 public static final TdwgArea ABBREV_BZC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZC );}
	 public static final TdwgArea ABBREV_BZE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE );}
	 public static final TdwgArea ABBREV_BZL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL );}
	 public static final TdwgArea ABBREV_BZN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN );}
	 public static final TdwgArea ABBREV_BZS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZS );}
	 public static final TdwgArea ABBREV_CAB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAB );}
	 public static final TdwgArea ABBREV_CAF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAF );}
	 public static final TdwgArea ABBREV_CAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAL );}
	 public static final TdwgArea ABBREV_CAY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAY );}
	 public static final TdwgArea ABBREV_CBD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CBD );}
	 public static final TdwgArea ABBREV_CGS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CGS );}
	 public static final TdwgArea ABBREV_CHA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHA );}
	 public static final TdwgArea ABBREV_CHC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC );}
	 public static final TdwgArea ABBREV_CHH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHH );}
	 public static final TdwgArea ABBREV_CHI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHI );}
	 public static final TdwgArea ABBREV_CHM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHM );}
	 public static final TdwgArea ABBREV_CHN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN );}
	 public static final TdwgArea ABBREV_CHQ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHQ );}
	 public static final TdwgArea ABBREV_CHS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS );}
	 public static final TdwgArea ABBREV_CHT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHT );}
	 public static final TdwgArea ABBREV_CHX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHX );}
	 public static final TdwgArea ABBREV_CKI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CKI );}
	 public static final TdwgArea ABBREV_CLC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC );}
	 public static final TdwgArea ABBREV_CLM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLM );}
	 public static final TdwgArea ABBREV_CLN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLN );}
	 public static final TdwgArea ABBREV_CLS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLS );}
	 public static final TdwgArea ABBREV_CMN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CMN );}
	 public static final TdwgArea ABBREV_CNT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CNT );}
	 public static final TdwgArea ABBREV_CNY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CNY );}
	 public static final TdwgArea ABBREV_COL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COL );}
	 public static final TdwgArea ABBREV_COM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COM );}
	 public static final TdwgArea ABBREV_CON () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CON );}
	 public static final TdwgArea ABBREV_COO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COO );}
	 public static final TdwgArea ABBREV_COR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COR );}
	 public static final TdwgArea ABBREV_COS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COS );}
	 public static final TdwgArea ABBREV_CPI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPI );}
	 public static final TdwgArea ABBREV_CPP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPP );}
	 public static final TdwgArea ABBREV_CPV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPV );}
	 public static final TdwgArea ABBREV_CRL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CRL );}
	 public static final TdwgArea ABBREV_CRZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CRZ );}
	 public static final TdwgArea ABBREV_CTA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CTA );}
	 public static final TdwgArea ABBREV_CTM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CTM );}
	 public static final TdwgArea ABBREV_CUB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CUB );}
	 public static final TdwgArea ABBREV_CVI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CVI );}
	 public static final TdwgArea ABBREV_CYP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CYP );}
	 public static final TdwgArea ABBREV_CZE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CZE );}
	 public static final TdwgArea ABBREV_DEL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DEL );}
	 public static final TdwgArea ABBREV_DEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DEN );}
	 public static final TdwgArea ABBREV_DJI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DJI );}
	 public static final TdwgArea ABBREV_DOM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DOM );}
	 public static final TdwgArea ABBREV_DSV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DSV );}
	 public static final TdwgArea ABBREV_EAI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EAI );}
	 public static final TdwgArea ABBREV_EAS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EAS );}
	 public static final TdwgArea ABBREV_ECU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ECU );}
	 public static final TdwgArea ABBREV_EGY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EGY );}
	 public static final TdwgArea ABBREV_EHM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EHM );}
	 public static final TdwgArea ABBREV_ELS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ELS );}
	 public static final TdwgArea ABBREV_EQG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EQG );}
	 public static final TdwgArea ABBREV_ERI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ERI );}
	 public static final TdwgArea ABBREV_ETH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ETH );}
	 public static final TdwgArea ABBREV_FAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FAL );}
	 public static final TdwgArea ABBREV_FIJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FIJ );}
	 public static final TdwgArea ABBREV_FIN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FIN );}
	 public static final TdwgArea ABBREV_FLA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FLA );}
	 public static final TdwgArea ABBREV_FOR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FOR );}
	 public static final TdwgArea ABBREV_FRA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRA );}
	 public static final TdwgArea ABBREV_FRG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRG );}
	 public static final TdwgArea ABBREV_GAB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAB );}
	 public static final TdwgArea ABBREV_GAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAL );}
	 public static final TdwgArea ABBREV_GAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAM );}
	 public static final TdwgArea ABBREV_GEO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GEO );}
	 public static final TdwgArea ABBREV_GER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GER );}
	 public static final TdwgArea ABBREV_GGI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GGI );}
	 public static final TdwgArea ABBREV_GHA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GHA );}
	 public static final TdwgArea ABBREV_GIL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GIL );}
	 public static final TdwgArea ABBREV_GNB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GNB );}
	 public static final TdwgArea ABBREV_GNL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GNL );}
	 public static final TdwgArea ABBREV_GRB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GRB );}
	 public static final TdwgArea ABBREV_GRC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GRC );}
	 public static final TdwgArea ABBREV_GST () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GST );}
	 public static final TdwgArea ABBREV_GUA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUA );}
	 public static final TdwgArea ABBREV_GUI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUI );}
	 public static final TdwgArea ABBREV_GUY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUY );}
	 public static final TdwgArea ABBREV_HAI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAI );}
	 public static final TdwgArea ABBREV_HAW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAW );}
	 public static final TdwgArea ABBREV_HBI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HBI );}
	 public static final TdwgArea ABBREV_HMD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HMD );}
	 public static final TdwgArea ABBREV_HON () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HON );}
	 public static final TdwgArea ABBREV_HUN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HUN );}
	 public static final TdwgArea ABBREV_ICE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ICE );}
	 public static final TdwgArea ABBREV_IDA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IDA );}
	 public static final TdwgArea ABBREV_ILL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ILL );}
	 public static final TdwgArea ABBREV_IND () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND );}
	 public static final TdwgArea ABBREV_INI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_INI );}
	 public static final TdwgArea ABBREV_IOW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IOW );}
	 public static final TdwgArea ABBREV_IRE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRE );}
	 public static final TdwgArea ABBREV_IRK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRK );}
	 public static final TdwgArea ABBREV_IRN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRN );}
	 public static final TdwgArea ABBREV_IRQ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRQ );}
	 public static final TdwgArea ABBREV_ITA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ITA );}
	 public static final TdwgArea ABBREV_IVO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IVO );}
	 public static final TdwgArea ABBREV_JAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAM );}
	 public static final TdwgArea ABBREV_JAP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAP );}
	 public static final TdwgArea ABBREV_JAW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAW );}
	 public static final TdwgArea ABBREV_JNF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JNF );}
	 public static final TdwgArea ABBREV_KAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAM );}
	 public static final TdwgArea ABBREV_KAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAN );}
	 public static final TdwgArea ABBREV_KAZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAZ );}
	 public static final TdwgArea ABBREV_KEG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KEG );}
	 public static final TdwgArea ABBREV_KEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KEN );}
	 public static final TdwgArea ABBREV_KER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KER );}
	 public static final TdwgArea ABBREV_KGZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KGZ );}
	 public static final TdwgArea ABBREV_KHA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KHA );}
	 public static final TdwgArea ABBREV_KOR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KOR );}
	 public static final TdwgArea ABBREV_KRA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRA );}
	 public static final TdwgArea ABBREV_KRI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRI );}
	 public static final TdwgArea ABBREV_KRY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRY );}
	 public static final TdwgArea ABBREV_KTY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KTY );}
	 public static final TdwgArea ABBREV_KUR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KUR );}
	 public static final TdwgArea ABBREV_KUW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KUW );}
	 public static final TdwgArea ABBREV_KZN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KZN );}
	 public static final TdwgArea ABBREV_LAB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LAB );}
	 public static final TdwgArea ABBREV_LAO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LAO );}
	 public static final TdwgArea ABBREV_LBR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBR );}
	 public static final TdwgArea ABBREV_LBS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBS );}
	 public static final TdwgArea ABBREV_LBY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBY );}
	 public static final TdwgArea ABBREV_LDV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LDV );}
	 public static final TdwgArea ABBREV_LEE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE );}
	 public static final TdwgArea ABBREV_LES () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LES );}
	 public static final TdwgArea ABBREV_LIN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LIN );}
	 public static final TdwgArea ABBREV_LOU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LOU );}
	 public static final TdwgArea ABBREV_LSI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LSI );}
	 public static final TdwgArea ABBREV_MAG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAG );}
	 public static final TdwgArea ABBREV_MAI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAI );}
	 public static final TdwgArea ABBREV_MAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAN );}
	 public static final TdwgArea ABBREV_MAQ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAQ );}
	 public static final TdwgArea ABBREV_MAS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAS );}
	 public static final TdwgArea ABBREV_MAU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAU );}
	 public static final TdwgArea ABBREV_MCI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MCI );}
	 public static final TdwgArea ABBREV_MCS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MCS );}
	 public static final TdwgArea ABBREV_MDG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDG );}
	 public static final TdwgArea ABBREV_MDR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDR );}
	 public static final TdwgArea ABBREV_MDV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDV );}
	 public static final TdwgArea ABBREV_MIC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MIC );}
	 public static final TdwgArea ABBREV_MIN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MIN );}
	 public static final TdwgArea ABBREV_MLI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLI );}
	 public static final TdwgArea ABBREV_MLW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLW );}
	 public static final TdwgArea ABBREV_MLY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLY );}
	 public static final TdwgArea ABBREV_MNT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MNT );}
	 public static final TdwgArea ABBREV_MOL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOL );}
	 public static final TdwgArea ABBREV_MON () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MON );}
	 public static final TdwgArea ABBREV_MOR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOR );}
	 public static final TdwgArea ABBREV_MOZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOZ );}
	 public static final TdwgArea ABBREV_MPE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MPE );}
	 public static final TdwgArea ABBREV_MRN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRN );}
	 public static final TdwgArea ABBREV_MRQ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRQ );}
	 public static final TdwgArea ABBREV_MRS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRS );}
	 public static final TdwgArea ABBREV_MRY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRY );}
	 public static final TdwgArea ABBREV_MSI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MSI );}
	 public static final TdwgArea ABBREV_MSO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MSO );}
	 public static final TdwgArea ABBREV_MTN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MTN );}
	 public static final TdwgArea ABBREV_MXC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC );}
	 public static final TdwgArea ABBREV_MXE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE );}
	 public static final TdwgArea ABBREV_MXG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXG );}
	 public static final TdwgArea ABBREV_MXI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXI );}
	 public static final TdwgArea ABBREV_MXN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXN );}
	 public static final TdwgArea ABBREV_MXS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS );}
	 public static final TdwgArea ABBREV_MXT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT );}
	 public static final TdwgArea ABBREV_MYA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MYA );}
	 public static final TdwgArea ABBREV_NAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NAM );}
	 public static final TdwgArea ABBREV_NAT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NAT );}
	 public static final TdwgArea ABBREV_NBR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NBR );}
	 public static final TdwgArea ABBREV_NCA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCA );}
	 public static final TdwgArea ABBREV_NCB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCB );}
	 public static final TdwgArea ABBREV_NCS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS );}
	 public static final TdwgArea ABBREV_NDA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NDA );}
	 public static final TdwgArea ABBREV_NEB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEB );}
	 public static final TdwgArea ABBREV_NEP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEP );}
	 public static final TdwgArea ABBREV_NET () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NET );}
	 public static final TdwgArea ABBREV_NEV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEV );}
	 public static final TdwgArea ABBREV_NFK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFK );}
	 public static final TdwgArea ABBREV_NFL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFL );}
	 public static final TdwgArea ABBREV_NGA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NGA );}
	 public static final TdwgArea ABBREV_NGR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NGR );}
	 public static final TdwgArea ABBREV_NIC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NIC );}
	 public static final TdwgArea ABBREV_NLA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NLA );}
	 public static final TdwgArea ABBREV_NNS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NNS );}
	 public static final TdwgArea ABBREV_NOR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NOR );}
	 public static final TdwgArea ABBREV_NRU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NRU );}
	 public static final TdwgArea ABBREV_NSC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NSC );}
	 public static final TdwgArea ABBREV_NSW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NSW );}
	 public static final TdwgArea ABBREV_NTA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NTA );}
	 public static final TdwgArea ABBREV_NUE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NUE );}
	 public static final TdwgArea ABBREV_NUN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NUN );}
	 public static final TdwgArea ABBREV_NWC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWC );}
	 public static final TdwgArea ABBREV_NWG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWG );}
	 public static final TdwgArea ABBREV_NWH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWH );}
	 public static final TdwgArea ABBREV_NWJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWJ );}
	 public static final TdwgArea ABBREV_NWM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWM );}
	 public static final TdwgArea ABBREV_NWT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWT );}
	 public static final TdwgArea ABBREV_NWY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWY );}
	 public static final TdwgArea ABBREV_NZN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NZN );}
	 public static final TdwgArea ABBREV_NZS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NZS );}
	 public static final TdwgArea ABBREV_OFS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OFS );}
	 public static final TdwgArea ABBREV_OGA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OGA );}
	 public static final TdwgArea ABBREV_OHI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OHI );}
	 public static final TdwgArea ABBREV_OKL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OKL );}
	 public static final TdwgArea ABBREV_OMA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OMA );}
	 public static final TdwgArea ABBREV_ONT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ONT );}
	 public static final TdwgArea ABBREV_ORE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ORE );}
	 public static final TdwgArea ABBREV_PAK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAK );}
	 public static final TdwgArea ABBREV_PAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAL );}
	 public static final TdwgArea ABBREV_PAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAN );}
	 public static final TdwgArea ABBREV_PAR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAR );}
	 public static final TdwgArea ABBREV_PEI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PEI );}
	 public static final TdwgArea ABBREV_PEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PEN );}
	 public static final TdwgArea ABBREV_PER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PER );}
	 public static final TdwgArea ABBREV_PHI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PHI );}
	 public static final TdwgArea ABBREV_PHX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PHX );}
	 public static final TdwgArea ABBREV_PIT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PIT );}
	 public static final TdwgArea ABBREV_POL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_POL );}
	 public static final TdwgArea ABBREV_POR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_POR );}
	 public static final TdwgArea ABBREV_PRM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PRM );}
	 public static final TdwgArea ABBREV_PUE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PUE );}
	 public static final TdwgArea ABBREV_QLD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_QLD );}
	 public static final TdwgArea ABBREV_QUE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_QUE );}
	 public static final TdwgArea ABBREV_REU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_REU );}
	 public static final TdwgArea ABBREV_RHO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RHO );}
	 public static final TdwgArea ABBREV_ROD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ROD );}
	 public static final TdwgArea ABBREV_ROM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ROM );}
	 public static final TdwgArea ABBREV_RUC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUC );}
	 public static final TdwgArea ABBREV_RUE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUE );}
	 public static final TdwgArea ABBREV_RUN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUN );}
	 public static final TdwgArea ABBREV_RUS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUS );}
	 public static final TdwgArea ABBREV_RUW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUW );}
	 public static final TdwgArea ABBREV_RWA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RWA );}
	 public static final TdwgArea ABBREV_SAK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAK );}
	 public static final TdwgArea ABBREV_SAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAM );}
	 public static final TdwgArea ABBREV_SAR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAR );}
	 public static final TdwgArea ABBREV_SAS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAS );}
	 public static final TdwgArea ABBREV_SAU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAU );}
	 public static final TdwgArea ABBREV_SCA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCA );}
	 public static final TdwgArea ABBREV_SCI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCI );}
	 public static final TdwgArea ABBREV_SCS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCS );}
	 public static final TdwgArea ABBREV_SCZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCZ );}
	 public static final TdwgArea ABBREV_SDA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SDA );}
	 public static final TdwgArea ABBREV_SEL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEL );}
	 public static final TdwgArea ABBREV_SEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEN );}
	 public static final TdwgArea ABBREV_SEY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEY );}
	 public static final TdwgArea ABBREV_SGE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SGE );}
	 public static final TdwgArea ABBREV_SIC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIC );}
	 public static final TdwgArea ABBREV_SIE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIE );}
	 public static final TdwgArea ABBREV_SIN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIN );}
	 public static final TdwgArea ABBREV_SOA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOA );}
	 public static final TdwgArea ABBREV_SOC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOC );}
	 public static final TdwgArea ABBREV_SOL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOL );}
	 public static final TdwgArea ABBREV_SOM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOM );}
	 public static final TdwgArea ABBREV_SPA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SPA );}
	 public static final TdwgArea ABBREV_SRL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SRL );}
	 public static final TdwgArea ABBREV_SSA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SSA );}
	 public static final TdwgArea ABBREV_STH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_STH );}
	 public static final TdwgArea ABBREV_SUD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUD );}
	 public static final TdwgArea ABBREV_SUL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUL );}
	 public static final TdwgArea ABBREV_SUM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUM );}
	 public static final TdwgArea ABBREV_SUR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUR );}
	 public static final TdwgArea ABBREV_SVA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SVA );}
	 public static final TdwgArea ABBREV_SWC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWC );}
	 public static final TdwgArea ABBREV_SWE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWE );}
	 public static final TdwgArea ABBREV_SWI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWI );}
	 public static final TdwgArea ABBREV_SWZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWZ );}
	 public static final TdwgArea ABBREV_TAI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAI );}
	 public static final TdwgArea ABBREV_TAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAN );}
	 public static final TdwgArea ABBREV_TAS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAS );}
	 public static final TdwgArea ABBREV_TCI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCI );}
	 public static final TdwgArea ABBREV_TCS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS );}
	 public static final TdwgArea ABBREV_TDC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TDC );}
	 public static final TdwgArea ABBREV_TEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TEN );}
	 public static final TdwgArea ABBREV_TEX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TEX );}
	 public static final TdwgArea ABBREV_THA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_THA );}
	 public static final TdwgArea ABBREV_TKM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TKM );}
	 public static final TdwgArea ABBREV_TOG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOG );}
	 public static final TdwgArea ABBREV_TOK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOK );}
	 public static final TdwgArea ABBREV_TON () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TON );}
	 public static final TdwgArea ABBREV_TRT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TRT );}
	 public static final TdwgArea ABBREV_TUA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUA );}
	 public static final TdwgArea ABBREV_TUB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUB );}
	 public static final TdwgArea ABBREV_TUE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUE );}
	 public static final TdwgArea ABBREV_TUN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUN );}
	 public static final TdwgArea ABBREV_TUR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUR );}
	 public static final TdwgArea ABBREV_TUV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUV );}
	 public static final TdwgArea ABBREV_TVA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVA );}
	 public static final TdwgArea ABBREV_TVL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVL );}
	 public static final TdwgArea ABBREV_TZK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TZK );}
	 public static final TdwgArea ABBREV_UGA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UGA );}
	 public static final TdwgArea ABBREV_UKR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UKR );}
	 public static final TdwgArea ABBREV_URU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_URU );}
	 public static final TdwgArea ABBREV_UTA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UTA );}
	 public static final TdwgArea ABBREV_UZB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UZB );}
	 public static final TdwgArea ABBREV_VAN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VAN );}
	 public static final TdwgArea ABBREV_VEN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VEN );}
	 public static final TdwgArea ABBREV_VER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VER );}
	 public static final TdwgArea ABBREV_VIC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VIC );}
	 public static final TdwgArea ABBREV_VIE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VIE );}
	 public static final TdwgArea ABBREV_VNA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VNA );}
	 public static final TdwgArea ABBREV_VRG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VRG );}
	 public static final TdwgArea ABBREV_WAK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAK );}
	 public static final TdwgArea ABBREV_WAL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAL );}
	 public static final TdwgArea ABBREV_WAS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAS );}
	 public static final TdwgArea ABBREV_WAU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAU );}
	 public static final TdwgArea ABBREV_WDC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WDC );}
	 public static final TdwgArea ABBREV_WHM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WHM );}
	 public static final TdwgArea ABBREV_WIN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN );}
	 public static final TdwgArea ABBREV_WIS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIS );}
	 public static final TdwgArea ABBREV_WSA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WSA );}
	 public static final TdwgArea ABBREV_WSB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WSB );}
	 public static final TdwgArea ABBREV_WVA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WVA );}
	 public static final TdwgArea ABBREV_WYO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WYO );}
	 public static final TdwgArea ABBREV_XMS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_XMS );}
	 public static final TdwgArea ABBREV_YAK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YAK );}
	 public static final TdwgArea ABBREV_YEM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YEM );}
	 public static final TdwgArea ABBREV_YUG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG );}
	 public static final TdwgArea ABBREV_YUK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUK );}
	 public static final TdwgArea ABBREV_ZAI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZAI );}
	 public static final TdwgArea ABBREV_ZAM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZAM );}
	 public static final TdwgArea ABBREV_ZIM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZIM );}
	 public static final TdwgArea ABBREV_ABT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ABT_OO );}
	 public static final TdwgArea ABBREV_AFG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AFG_OO );}
	 public static final TdwgArea ABBREV_AGE_BA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_BA );}
	 public static final TdwgArea ABBREV_AGE_CH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_CH );}
	 public static final TdwgArea ABBREV_AGE_CN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_CN );}
	 public static final TdwgArea ABBREV_AGE_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_CO );}
	 public static final TdwgArea ABBREV_AGE_DF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_DF );}
	 public static final TdwgArea ABBREV_AGE_ER () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_ER );}
	 public static final TdwgArea ABBREV_AGE_FO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_FO );}
	 public static final TdwgArea ABBREV_AGE_LP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_LP );}
	 public static final TdwgArea ABBREV_AGE_MI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGE_MI );}
	 public static final TdwgArea ABBREV_AGS_CB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_CB );}
	 public static final TdwgArea ABBREV_AGS_NE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_NE );}
	 public static final TdwgArea ABBREV_AGS_RN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_RN );}
	 public static final TdwgArea ABBREV_AGS_SC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_SC );}
	 public static final TdwgArea ABBREV_AGS_SF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_SF );}
	 public static final TdwgArea ABBREV_AGS_TF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGS_TF );}
	 public static final TdwgArea ABBREV_AGW_CA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_CA );}
	 public static final TdwgArea ABBREV_AGW_JU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_JU );}
	 public static final TdwgArea ABBREV_AGW_LR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_LR );}
	 public static final TdwgArea ABBREV_AGW_ME () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_ME );}
	 public static final TdwgArea ABBREV_AGW_SA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_SA );}
	 public static final TdwgArea ABBREV_AGW_SE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_SE );}
	 public static final TdwgArea ABBREV_AGW_SJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_SJ );}
	 public static final TdwgArea ABBREV_AGW_SL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_SL );}
	 public static final TdwgArea ABBREV_AGW_TU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AGW_TU );}
	 public static final TdwgArea ABBREV_ALA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALA_OO );}
	 public static final TdwgArea ABBREV_ALB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALB_OO );}
	 public static final TdwgArea ABBREV_ALD_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALD_OO );}
	 public static final TdwgArea ABBREV_ALG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALG_OO );}
	 public static final TdwgArea ABBREV_ALT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALT_OO );}
	 public static final TdwgArea ABBREV_ALU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ALU_OO );}
	 public static final TdwgArea ABBREV_AMU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AMU_OO );}
	 public static final TdwgArea ABBREV_AND_AN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AND_AN );}
	 public static final TdwgArea ABBREV_AND_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AND_CO );}
	 public static final TdwgArea ABBREV_ANG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ANG_OO );}
	 public static final TdwgArea ABBREV_ANT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ANT_OO );}
	 public static final TdwgArea ABBREV_ARI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARI_OO );}
	 public static final TdwgArea ABBREV_ARK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARK_OO );}
	 public static final TdwgArea ABBREV_ARU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ARU_OO );}
	 public static final TdwgArea ABBREV_ASC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASC_OO );}
	 public static final TdwgArea ABBREV_ASK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASK_OO );}
	 public static final TdwgArea ABBREV_ASP_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASP_OO );}
	 public static final TdwgArea ABBREV_ASS_AS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_AS );}
	 public static final TdwgArea ABBREV_ASS_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_MA );}
	 public static final TdwgArea ABBREV_ASS_ME () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_ME );}
	 public static final TdwgArea ABBREV_ASS_MI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_MI );}
	 public static final TdwgArea ABBREV_ASS_NA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_NA );}
	 public static final TdwgArea ABBREV_ASS_TR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ASS_TR );}
	 public static final TdwgArea ABBREV_ATP_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ATP_OO );}
	 public static final TdwgArea ABBREV_AUT_AU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AUT_AU );}
	 public static final TdwgArea ABBREV_AUT_LI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AUT_LI );}
	 public static final TdwgArea ABBREV_AZO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_AZO_OO );}
	 public static final TdwgArea ABBREV_BAH_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAH_OO );}
	 public static final TdwgArea ABBREV_BAL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAL_OO );}
	 public static final TdwgArea ABBREV_BAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BAN_OO );}
	 public static final TdwgArea ABBREV_BEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BEN_OO );}
	 public static final TdwgArea ABBREV_BER_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BER_OO );}
	 public static final TdwgArea ABBREV_BGM_BE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BGM_BE );}
	 public static final TdwgArea ABBREV_BGM_LU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BGM_LU );}
	 public static final TdwgArea ABBREV_BIS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BIS_OO );}
	 public static final TdwgArea ABBREV_BKN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BKN_OO );}
	 public static final TdwgArea ABBREV_BLR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLR_OO );}
	 public static final TdwgArea ABBREV_BLT_ES () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLT_ES );}
	 public static final TdwgArea ABBREV_BLT_KA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLT_KA );}
	 public static final TdwgArea ABBREV_BLT_LA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLT_LA );}
	 public static final TdwgArea ABBREV_BLT_LI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLT_LI );}
	 public static final TdwgArea ABBREV_BLZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BLZ_OO );}
	 public static final TdwgArea ABBREV_BOL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOL_OO );}
	 public static final TdwgArea ABBREV_BOR_BR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOR_BR );}
	 public static final TdwgArea ABBREV_BOR_KA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOR_KA );}
	 public static final TdwgArea ABBREV_BOR_SB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOR_SB );}
	 public static final TdwgArea ABBREV_BOR_SR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOR_SR );}
	 public static final TdwgArea ABBREV_BOT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOT_OO );}
	 public static final TdwgArea ABBREV_BOU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BOU_OO );}
	 public static final TdwgArea ABBREV_BRC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BRC_OO );}
	 public static final TdwgArea ABBREV_BRY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BRY_OO );}
	 public static final TdwgArea ABBREV_BUL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BUL_OO );}
	 public static final TdwgArea ABBREV_BUR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BUR_OO );}
	 public static final TdwgArea ABBREV_BZC_DF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZC_DF );}
	 public static final TdwgArea ABBREV_BZC_GO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZC_GO );}
	 public static final TdwgArea ABBREV_BZC_MS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZC_MS );}
	 public static final TdwgArea ABBREV_BZC_MT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZC_MT );}
	 public static final TdwgArea ABBREV_BZE_AL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_AL );}
	 public static final TdwgArea ABBREV_BZE_BA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_BA );}
	 public static final TdwgArea ABBREV_BZE_CE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_CE );}
	 public static final TdwgArea ABBREV_BZE_FN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_FN );}
	 public static final TdwgArea ABBREV_BZE_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_MA );}
	 public static final TdwgArea ABBREV_BZE_PB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_PB );}
	 public static final TdwgArea ABBREV_BZE_PE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_PE );}
	 public static final TdwgArea ABBREV_BZE_PI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_PI );}
	 public static final TdwgArea ABBREV_BZE_RN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_RN );}
	 public static final TdwgArea ABBREV_BZE_SE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZE_SE );}
	 public static final TdwgArea ABBREV_BZL_ES () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL_ES );}
	 public static final TdwgArea ABBREV_BZL_MG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL_MG );}
	 public static final TdwgArea ABBREV_BZL_RJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL_RJ );}
	 public static final TdwgArea ABBREV_BZL_SP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL_SP );}
	 public static final TdwgArea ABBREV_BZL_TR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZL_TR );}
	 public static final TdwgArea ABBREV_BZN_AC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_AC );}
	 public static final TdwgArea ABBREV_BZN_AM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_AM );}
	 public static final TdwgArea ABBREV_BZN_AP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_AP );}
	 public static final TdwgArea ABBREV_BZN_PA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_PA );}
	 public static final TdwgArea ABBREV_BZN_RM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_RM );}
	 public static final TdwgArea ABBREV_BZN_RO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_RO );}
	 public static final TdwgArea ABBREV_BZN_TO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZN_TO );}
	 public static final TdwgArea ABBREV_BZS_PR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZS_PR );}
	 public static final TdwgArea ABBREV_BZS_RS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZS_RS );}
	 public static final TdwgArea ABBREV_BZS_SC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_BZS_SC );}
	 public static final TdwgArea ABBREV_CAB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAB_OO );}
	 public static final TdwgArea ABBREV_CAF_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAF_OO );}
	 public static final TdwgArea ABBREV_CAL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAL_OO );}
	 public static final TdwgArea ABBREV_CAY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CAY_OO );}
	 public static final TdwgArea ABBREV_CBD_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CBD_OO );}
	 public static final TdwgArea ABBREV_CGS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CGS_OO );}
	 public static final TdwgArea ABBREV_CHA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHA_OO );}
	 public static final TdwgArea ABBREV_CHC_CQ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC_CQ );}
	 public static final TdwgArea ABBREV_CHC_GZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC_GZ );}
	 public static final TdwgArea ABBREV_CHC_HU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC_HU );}
	 public static final TdwgArea ABBREV_CHC_SC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC_SC );}
	 public static final TdwgArea ABBREV_CHC_YN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHC_YN );}
	 public static final TdwgArea ABBREV_CHH_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHH_OO );}
	 public static final TdwgArea ABBREV_CHI_NM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHI_NM );}
	 public static final TdwgArea ABBREV_CHI_NX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHI_NX );}
	 public static final TdwgArea ABBREV_CHM_HJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHM_HJ );}
	 public static final TdwgArea ABBREV_CHM_JL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHM_JL );}
	 public static final TdwgArea ABBREV_CHM_LN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHM_LN );}
	 public static final TdwgArea ABBREV_CHN_BJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_BJ );}
	 public static final TdwgArea ABBREV_CHN_GS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_GS );}
	 public static final TdwgArea ABBREV_CHN_HB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_HB );}
	 public static final TdwgArea ABBREV_CHN_SA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_SA );}
	 public static final TdwgArea ABBREV_CHN_SD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_SD );}
	 public static final TdwgArea ABBREV_CHN_SX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_SX );}
	 public static final TdwgArea ABBREV_CHN_TJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHN_TJ );}
	 public static final TdwgArea ABBREV_CHQ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHQ_OO );}
	 public static final TdwgArea ABBREV_CHS_AH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_AH );}
	 public static final TdwgArea ABBREV_CHS_FJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_FJ );}
	 public static final TdwgArea ABBREV_CHS_GD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_GD );}
	 public static final TdwgArea ABBREV_CHS_GX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_GX );}
	 public static final TdwgArea ABBREV_CHS_HE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_HE );}
	 public static final TdwgArea ABBREV_CHS_HK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_HK );}
	 public static final TdwgArea ABBREV_CHS_HN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_HN );}
	 public static final TdwgArea ABBREV_CHS_JS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_JS );}
	 public static final TdwgArea ABBREV_CHS_JX () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_JX );}
	 public static final TdwgArea ABBREV_CHS_KI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_KI );}
	 public static final TdwgArea ABBREV_CHS_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_MA );}
	 public static final TdwgArea ABBREV_CHS_MP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_MP );}
	 public static final TdwgArea ABBREV_CHS_SH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_SH );}
	 public static final TdwgArea ABBREV_CHS_ZJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHS_ZJ );}
	 public static final TdwgArea ABBREV_CHT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHT_OO );}
	 public static final TdwgArea ABBREV_CHX_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CHX_OO );}
	 public static final TdwgArea ABBREV_CKI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CKI_OO );}
	 public static final TdwgArea ABBREV_CLC_BI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_BI );}
	 public static final TdwgArea ABBREV_CLC_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_CO );}
	 public static final TdwgArea ABBREV_CLC_LA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_LA );}
	 public static final TdwgArea ABBREV_CLC_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_MA );}
	 public static final TdwgArea ABBREV_CLC_OH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_OH );}
	 public static final TdwgArea ABBREV_CLC_SA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_SA );}
	 public static final TdwgArea ABBREV_CLC_VA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLC_VA );}
	 public static final TdwgArea ABBREV_CLM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLM_OO );}
	 public static final TdwgArea ABBREV_CLN_AN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLN_AN );}
	 public static final TdwgArea ABBREV_CLN_AT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLN_AT );}
	 public static final TdwgArea ABBREV_CLN_TA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLN_TA );}
	 public static final TdwgArea ABBREV_CLS_AI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLS_AI );}
	 public static final TdwgArea ABBREV_CLS_LL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLS_LL );}
	 public static final TdwgArea ABBREV_CLS_MG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CLS_MG );}
	 public static final TdwgArea ABBREV_CMN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CMN_OO );}
	 public static final TdwgArea ABBREV_CNT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CNT_OO );}
	 public static final TdwgArea ABBREV_CNY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CNY_OO );}
	 public static final TdwgArea ABBREV_COL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COL_OO );}
	 public static final TdwgArea ABBREV_COM_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COM_CO );}
	 public static final TdwgArea ABBREV_COM_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COM_MA );}
	 public static final TdwgArea ABBREV_CON_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CON_OO );}
	 public static final TdwgArea ABBREV_COO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COO_OO );}
	 public static final TdwgArea ABBREV_COR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COR_OO );}
	 public static final TdwgArea ABBREV_COS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_COS_OO );}
	 public static final TdwgArea ABBREV_CPI_CL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPI_CL );}
	 public static final TdwgArea ABBREV_CPI_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPI_CO );}
	 public static final TdwgArea ABBREV_CPI_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPI_MA );}
	 public static final TdwgArea ABBREV_CPP_EC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPP_EC );}
	 public static final TdwgArea ABBREV_CPP_NC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPP_NC );}
	 public static final TdwgArea ABBREV_CPP_WC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPP_WC );}
	 public static final TdwgArea ABBREV_CPV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CPV_OO );}
	 public static final TdwgArea ABBREV_CRL_MF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CRL_MF );}
	 public static final TdwgArea ABBREV_CRL_PA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CRL_PA );}
	 public static final TdwgArea ABBREV_CRZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CRZ_OO );}
	 public static final TdwgArea ABBREV_CTA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CTA_OO );}
	 public static final TdwgArea ABBREV_CTM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CTM_OO );}
	 public static final TdwgArea ABBREV_CUB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CUB_OO );}
	 public static final TdwgArea ABBREV_CVI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CVI_OO );}
	 public static final TdwgArea ABBREV_CYP_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CYP_OO );}
	 public static final TdwgArea ABBREV_CZE_CZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CZE_CZ );}
	 public static final TdwgArea ABBREV_CZE_SK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_CZE_SK );}
	 public static final TdwgArea ABBREV_DEL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DEL_OO );}
	 public static final TdwgArea ABBREV_DEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DEN_OO );}
	 public static final TdwgArea ABBREV_DJI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DJI_OO );}
	 public static final TdwgArea ABBREV_DOM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DOM_OO );}
	 public static final TdwgArea ABBREV_DSV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_DSV_OO );}
	 public static final TdwgArea ABBREV_EAI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EAI_OO );}
	 public static final TdwgArea ABBREV_EAS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EAS_OO );}
	 public static final TdwgArea ABBREV_ECU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ECU_OO );}
	 public static final TdwgArea ABBREV_EGY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EGY_OO );}
	 public static final TdwgArea ABBREV_EHM_AP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EHM_AP );}
	 public static final TdwgArea ABBREV_EHM_BH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EHM_BH );}
	 public static final TdwgArea ABBREV_EHM_DJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EHM_DJ );}
	 public static final TdwgArea ABBREV_EHM_SI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EHM_SI );}
	 public static final TdwgArea ABBREV_ELS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ELS_OO );}
	 public static final TdwgArea ABBREV_EQG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_EQG_OO );}
	 public static final TdwgArea ABBREV_ERI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ERI_OO );}
	 public static final TdwgArea ABBREV_ETH_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ETH_OO );}
	 public static final TdwgArea ABBREV_FAL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FAL_OO );}
	 public static final TdwgArea ABBREV_FIJ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FIJ_OO );}
	 public static final TdwgArea ABBREV_FIN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FIN_OO );}
	 public static final TdwgArea ABBREV_FLA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FLA_OO );}
	 public static final TdwgArea ABBREV_FOR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FOR_OO );}
	 public static final TdwgArea ABBREV_FRA_CI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRA_CI );}
	 public static final TdwgArea ABBREV_FRA_FR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRA_FR );}
	 public static final TdwgArea ABBREV_FRA_MO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRA_MO );}
	 public static final TdwgArea ABBREV_FRG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_FRG_OO );}
	 public static final TdwgArea ABBREV_GAB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAB_OO );}
	 public static final TdwgArea ABBREV_GAL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAL_OO );}
	 public static final TdwgArea ABBREV_GAM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GAM_OO );}
	 public static final TdwgArea ABBREV_GEO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GEO_OO );}
	 public static final TdwgArea ABBREV_GER_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GER_OO );}
	 public static final TdwgArea ABBREV_GGI_AN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GGI_AN );}
	 public static final TdwgArea ABBREV_GGI_BI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GGI_BI );}
	 public static final TdwgArea ABBREV_GGI_PR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GGI_PR );}
	 public static final TdwgArea ABBREV_GGI_ST () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GGI_ST );}
	 public static final TdwgArea ABBREV_GHA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GHA_OO );}
	 public static final TdwgArea ABBREV_GIL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GIL_OO );}
	 public static final TdwgArea ABBREV_GNB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GNB_OO );}
	 public static final TdwgArea ABBREV_GNL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GNL_OO );}
	 public static final TdwgArea ABBREV_GRB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GRB_OO );}
	 public static final TdwgArea ABBREV_GRC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GRC_OO );}
	 public static final TdwgArea ABBREV_GST_BA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GST_BA );}
	 public static final TdwgArea ABBREV_GST_QA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GST_QA );}
	 public static final TdwgArea ABBREV_GST_UA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GST_UA );}
	 public static final TdwgArea ABBREV_GUA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUA_OO );}
	 public static final TdwgArea ABBREV_GUI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUI_OO );}
	 public static final TdwgArea ABBREV_GUY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_GUY_OO );}
	 public static final TdwgArea ABBREV_HAI_HA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAI_HA );}
	 public static final TdwgArea ABBREV_HAI_NI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAI_NI );}
	 public static final TdwgArea ABBREV_HAW_HI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAW_HI );}
	 public static final TdwgArea ABBREV_HAW_JI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAW_JI );}
	 public static final TdwgArea ABBREV_HAW_MI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HAW_MI );}
	 public static final TdwgArea ABBREV_HBI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HBI_OO );}
	 public static final TdwgArea ABBREV_HMD_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HMD_OO );}
	 public static final TdwgArea ABBREV_HON_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HON_OO );}
	 public static final TdwgArea ABBREV_HUN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_HUN_OO );}
	 public static final TdwgArea ABBREV_ICE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ICE_OO );}
	 public static final TdwgArea ABBREV_IDA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IDA_OO );}
	 public static final TdwgArea ABBREV_ILL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ILL_OO );}
	 public static final TdwgArea ABBREV_IND_AP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_AP );}
	 public static final TdwgArea ABBREV_IND_BI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_BI );}
	 public static final TdwgArea ABBREV_IND_CH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_CH );}
	 public static final TdwgArea ABBREV_IND_CT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_CT );}
	 public static final TdwgArea ABBREV_IND_DD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_DD );}
	 public static final TdwgArea ABBREV_IND_DE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_DE );}
	 public static final TdwgArea ABBREV_IND_DI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_DI );}
	 public static final TdwgArea ABBREV_IND_DM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_DM );}
	 public static final TdwgArea ABBREV_IND_GO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_GO );}
	 public static final TdwgArea ABBREV_IND_GU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_GU );}
	 public static final TdwgArea ABBREV_IND_HA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_HA );}
	 public static final TdwgArea ABBREV_IND_JK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_JK );}
	 public static final TdwgArea ABBREV_IND_KE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_KE );}
	 public static final TdwgArea ABBREV_IND_KL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_KL );}
	 public static final TdwgArea ABBREV_IND_KT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_KT );}
	 public static final TdwgArea ABBREV_IND_MH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_MH );}
	 public static final TdwgArea ABBREV_IND_MP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_MP );}
	 public static final TdwgArea ABBREV_IND_MR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_MR );}
	 public static final TdwgArea ABBREV_IND_OR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_OR );}
	 public static final TdwgArea ABBREV_IND_PO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_PO );}
	 public static final TdwgArea ABBREV_IND_PU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_PU );}
	 public static final TdwgArea ABBREV_IND_RA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_RA );}
	 public static final TdwgArea ABBREV_IND_TN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_TN );}
	 public static final TdwgArea ABBREV_IND_UP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_UP );}
	 public static final TdwgArea ABBREV_IND_WB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_WB );}
	 public static final TdwgArea ABBREV_IND_YA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IND_YA );}
	 public static final TdwgArea ABBREV_INI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_INI_OO );}
	 public static final TdwgArea ABBREV_IOW_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IOW_OO );}
	 public static final TdwgArea ABBREV_IRE_IR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRE_IR );}
	 public static final TdwgArea ABBREV_IRE_NI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRE_NI );}
	 public static final TdwgArea ABBREV_IRK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRK_OO );}
	 public static final TdwgArea ABBREV_IRN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRN_OO );}
	 public static final TdwgArea ABBREV_IRQ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IRQ_OO );}
	 public static final TdwgArea ABBREV_ITA_IT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ITA_IT );}
	 public static final TdwgArea ABBREV_ITA_SM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ITA_SM );}
	 public static final TdwgArea ABBREV_ITA_VC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ITA_VC );}
	 public static final TdwgArea ABBREV_IVO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_IVO_OO );}
	 public static final TdwgArea ABBREV_JAM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAM_OO );}
	 public static final TdwgArea ABBREV_JAP_HK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAP_HK );}
	 public static final TdwgArea ABBREV_JAP_HN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAP_HN );}
	 public static final TdwgArea ABBREV_JAP_KY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAP_KY );}
	 public static final TdwgArea ABBREV_JAP_SH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAP_SH );}
	 public static final TdwgArea ABBREV_JAW_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JAW_OO );}
	 public static final TdwgArea ABBREV_JNF_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_JNF_OO );}
	 public static final TdwgArea ABBREV_KAM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAM_OO );}
	 public static final TdwgArea ABBREV_KAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAN_OO );}
	 public static final TdwgArea ABBREV_KAZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KAZ_OO );}
	 public static final TdwgArea ABBREV_KEG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KEG_OO );}
	 public static final TdwgArea ABBREV_KEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KEN_OO );}
	 public static final TdwgArea ABBREV_KER_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KER_OO );}
	 public static final TdwgArea ABBREV_KGZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KGZ_OO );}
	 public static final TdwgArea ABBREV_KHA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KHA_OO );}
	 public static final TdwgArea ABBREV_KOR_NK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KOR_NK );}
	 public static final TdwgArea ABBREV_KOR_SK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KOR_SK );}
	 public static final TdwgArea ABBREV_KRA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRA_OO );}
	 public static final TdwgArea ABBREV_KRI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRI_OO );}
	 public static final TdwgArea ABBREV_KRY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KRY_OO );}
	 public static final TdwgArea ABBREV_KTY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KTY_OO );}
	 public static final TdwgArea ABBREV_KUR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KUR_OO );}
	 public static final TdwgArea ABBREV_KUW_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KUW_OO );}
	 public static final TdwgArea ABBREV_KZN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_KZN_OO );}
	 public static final TdwgArea ABBREV_LAB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LAB_OO );}
	 public static final TdwgArea ABBREV_LAO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LAO_OO );}
	 public static final TdwgArea ABBREV_LBR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBR_OO );}
	 public static final TdwgArea ABBREV_LBS_LB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBS_LB );}
	 public static final TdwgArea ABBREV_LBS_SY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBS_SY );}
	 public static final TdwgArea ABBREV_LBY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LBY_OO );}
	 public static final TdwgArea ABBREV_LDV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LDV_OO );}
	 public static final TdwgArea ABBREV_LEE_AB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_AB );}
	 public static final TdwgArea ABBREV_LEE_AG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_AG );}
	 public static final TdwgArea ABBREV_LEE_AV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_AV );}
	 public static final TdwgArea ABBREV_LEE_BV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_BV );}
	 public static final TdwgArea ABBREV_LEE_GU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_GU );}
	 public static final TdwgArea ABBREV_LEE_MO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_MO );}
	 public static final TdwgArea ABBREV_LEE_NL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_NL );}
	 public static final TdwgArea ABBREV_LEE_SK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_SK );}
	 public static final TdwgArea ABBREV_LEE_SM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_SM );}
	 public static final TdwgArea ABBREV_LEE_VI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LEE_VI );}
	 public static final TdwgArea ABBREV_LES_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LES_OO );}
	 public static final TdwgArea ABBREV_LIN_KI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LIN_KI );}
	 public static final TdwgArea ABBREV_LIN_US () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LIN_US );}
	 public static final TdwgArea ABBREV_LOU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LOU_OO );}
	 public static final TdwgArea ABBREV_LSI_BA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LSI_BA );}
	 public static final TdwgArea ABBREV_LSI_ET () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LSI_ET );}
	 public static final TdwgArea ABBREV_LSI_LS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_LSI_LS );}
	 public static final TdwgArea ABBREV_MAG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAG_OO );}
	 public static final TdwgArea ABBREV_MAI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAI_OO );}
	 public static final TdwgArea ABBREV_MAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAN_OO );}
	 public static final TdwgArea ABBREV_MAQ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAQ_OO );}
	 public static final TdwgArea ABBREV_MAS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAS_OO );}
	 public static final TdwgArea ABBREV_MAU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MAU_OO );}
	 public static final TdwgArea ABBREV_MCI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MCI_OO );}
	 public static final TdwgArea ABBREV_MCS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MCS_OO );}
	 public static final TdwgArea ABBREV_MDG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDG_OO );}
	 public static final TdwgArea ABBREV_MDR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDR_OO );}
	 public static final TdwgArea ABBREV_MDV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MDV_OO );}
	 public static final TdwgArea ABBREV_MIC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MIC_OO );}
	 public static final TdwgArea ABBREV_MIN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MIN_OO );}
	 public static final TdwgArea ABBREV_MLI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLI_OO );}
	 public static final TdwgArea ABBREV_MLW_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLW_OO );}
	 public static final TdwgArea ABBREV_MLY_PM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLY_PM );}
	 public static final TdwgArea ABBREV_MLY_SI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MLY_SI );}
	 public static final TdwgArea ABBREV_MNT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MNT_OO );}
	 public static final TdwgArea ABBREV_MOL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOL_OO );}
	 public static final TdwgArea ABBREV_MON_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MON_OO );}
	 public static final TdwgArea ABBREV_MOR_MO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOR_MO );}
	 public static final TdwgArea ABBREV_MOR_SP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOR_SP );}
	 public static final TdwgArea ABBREV_MOZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MOZ_OO );}
	 public static final TdwgArea ABBREV_MPE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MPE_OO );}
	 public static final TdwgArea ABBREV_MRN_GU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRN_GU );}
	 public static final TdwgArea ABBREV_MRN_NM () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRN_NM );}
	 public static final TdwgArea ABBREV_MRQ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRQ_OO );}
	 public static final TdwgArea ABBREV_MRS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRS_OO );}
	 public static final TdwgArea ABBREV_MRY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MRY_OO );}
	 public static final TdwgArea ABBREV_MSI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MSI_OO );}
	 public static final TdwgArea ABBREV_MSO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MSO_OO );}
	 public static final TdwgArea ABBREV_MTN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MTN_OO );}
	 public static final TdwgArea ABBREV_MXC_DF () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC_DF );}
	 public static final TdwgArea ABBREV_MXC_ME () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC_ME );}
	 public static final TdwgArea ABBREV_MXC_MO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC_MO );}
	 public static final TdwgArea ABBREV_MXC_PU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC_PU );}
	 public static final TdwgArea ABBREV_MXC_TL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXC_TL );}
	 public static final TdwgArea ABBREV_MXE_AG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_AG );}
	 public static final TdwgArea ABBREV_MXE_CO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_CO );}
	 public static final TdwgArea ABBREV_MXE_CU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_CU );}
	 public static final TdwgArea ABBREV_MXE_DU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_DU );}
	 public static final TdwgArea ABBREV_MXE_GU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_GU );}
	 public static final TdwgArea ABBREV_MXE_HI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_HI );}
	 public static final TdwgArea ABBREV_MXE_NL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_NL );}
	 public static final TdwgArea ABBREV_MXE_QU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_QU );}
	 public static final TdwgArea ABBREV_MXE_SL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_SL );}
	 public static final TdwgArea ABBREV_MXE_TA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_TA );}
	 public static final TdwgArea ABBREV_MXE_ZA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXE_ZA );}
	 public static final TdwgArea ABBREV_MXG_VC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXG_VC );}
	 public static final TdwgArea ABBREV_MXI_GU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXI_GU );}
	 public static final TdwgArea ABBREV_MXI_RA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXI_RA );}
	 public static final TdwgArea ABBREV_MXI_RG () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXI_RG );}
	 public static final TdwgArea ABBREV_MXN_BC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXN_BC );}
	 public static final TdwgArea ABBREV_MXN_BS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXN_BS );}
	 public static final TdwgArea ABBREV_MXN_SI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXN_SI );}
	 public static final TdwgArea ABBREV_MXN_SO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXN_SO );}
	 public static final TdwgArea ABBREV_MXS_CL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_CL );}
	 public static final TdwgArea ABBREV_MXS_GR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_GR );}
	 public static final TdwgArea ABBREV_MXS_JA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_JA );}
	 public static final TdwgArea ABBREV_MXS_MI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_MI );}
	 public static final TdwgArea ABBREV_MXS_NA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_NA );}
	 public static final TdwgArea ABBREV_MXS_OA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXS_OA );}
	 public static final TdwgArea ABBREV_MXT_CA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT_CA );}
	 public static final TdwgArea ABBREV_MXT_CI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT_CI );}
	 public static final TdwgArea ABBREV_MXT_QR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT_QR );}
	 public static final TdwgArea ABBREV_MXT_TB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT_TB );}
	 public static final TdwgArea ABBREV_MXT_YU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MXT_YU );}
	 public static final TdwgArea ABBREV_MYA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_MYA_OO );}
	 public static final TdwgArea ABBREV_NAM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NAM_OO );}
	 public static final TdwgArea ABBREV_NAT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NAT_OO );}
	 public static final TdwgArea ABBREV_NBR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NBR_OO );}
	 public static final TdwgArea ABBREV_NCA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCA_OO );}
	 public static final TdwgArea ABBREV_NCB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCB_OO );}
	 public static final TdwgArea ABBREV_NCS_CH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_CH );}
	 public static final TdwgArea ABBREV_NCS_DA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_DA );}
	 public static final TdwgArea ABBREV_NCS_IN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_IN );}
	 public static final TdwgArea ABBREV_NCS_KB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_KB );}
	 public static final TdwgArea ABBREV_NCS_KC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_KC );}
	 public static final TdwgArea ABBREV_NCS_KR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_KR );}
	 public static final TdwgArea ABBREV_NCS_SO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_SO );}
	 public static final TdwgArea ABBREV_NCS_ST () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NCS_ST );}
	 public static final TdwgArea ABBREV_NDA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NDA_OO );}
	 public static final TdwgArea ABBREV_NEB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEB_OO );}
	 public static final TdwgArea ABBREV_NEP_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEP_OO );}
	 public static final TdwgArea ABBREV_NET_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NET_OO );}
	 public static final TdwgArea ABBREV_NEV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NEV_OO );}
	 public static final TdwgArea ABBREV_NFK_LH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFK_LH );}
	 public static final TdwgArea ABBREV_NFK_NI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFK_NI );}
	 public static final TdwgArea ABBREV_NFL_NE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFL_NE );}
	 public static final TdwgArea ABBREV_NFL_SP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NFL_SP );}
	 public static final TdwgArea ABBREV_NGA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NGA_OO );}
	 public static final TdwgArea ABBREV_NGR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NGR_OO );}
	 public static final TdwgArea ABBREV_NIC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NIC_OO );}
	 public static final TdwgArea ABBREV_NLA_BO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NLA_BO );}
	 public static final TdwgArea ABBREV_NLA_CU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NLA_CU );}
	 public static final TdwgArea ABBREV_NNS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NNS_OO );}
	 public static final TdwgArea ABBREV_NOR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NOR_OO );}
	 public static final TdwgArea ABBREV_NRU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NRU_OO );}
	 public static final TdwgArea ABBREV_NSC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NSC_OO );}
	 public static final TdwgArea ABBREV_NSW_CT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NSW_CT );}
	 public static final TdwgArea ABBREV_NSW_NS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NSW_NS );}
	 public static final TdwgArea ABBREV_NTA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NTA_OO );}
	 public static final TdwgArea ABBREV_NUE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NUE_OO );}
	 public static final TdwgArea ABBREV_NUN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NUN_OO );}
	 public static final TdwgArea ABBREV_NWC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWC_OO );}
	 public static final TdwgArea ABBREV_NWG_IJ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWG_IJ );}
	 public static final TdwgArea ABBREV_NWG_PN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWG_PN );}
	 public static final TdwgArea ABBREV_NWH_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWH_OO );}
	 public static final TdwgArea ABBREV_NWJ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWJ_OO );}
	 public static final TdwgArea ABBREV_NWM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWM_OO );}
	 public static final TdwgArea ABBREV_NWT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWT_OO );}
	 public static final TdwgArea ABBREV_NWY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NWY_OO );}
	 public static final TdwgArea ABBREV_NZN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NZN_OO );}
	 public static final TdwgArea ABBREV_NZS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_NZS_OO );}
	 public static final TdwgArea ABBREV_OFS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OFS_OO );}
	 public static final TdwgArea ABBREV_OGA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OGA_OO );}
	 public static final TdwgArea ABBREV_OHI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OHI_OO );}
	 public static final TdwgArea ABBREV_OKL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OKL_OO );}
	 public static final TdwgArea ABBREV_OMA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_OMA_OO );}
	 public static final TdwgArea ABBREV_ONT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ONT_OO );}
	 public static final TdwgArea ABBREV_ORE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ORE_OO );}
	 public static final TdwgArea ABBREV_PAK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAK_OO );}
	 public static final TdwgArea ABBREV_PAL_IS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAL_IS );}
	 public static final TdwgArea ABBREV_PAL_JO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAL_JO );}
	 public static final TdwgArea ABBREV_PAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAN_OO );}
	 public static final TdwgArea ABBREV_PAR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PAR_OO );}
	 public static final TdwgArea ABBREV_PEI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PEI_OO );}
	 public static final TdwgArea ABBREV_PEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PEN_OO );}
	 public static final TdwgArea ABBREV_PER_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PER_OO );}
	 public static final TdwgArea ABBREV_PHI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PHI_OO );}
	 public static final TdwgArea ABBREV_PHX_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PHX_OO );}
	 public static final TdwgArea ABBREV_PIT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PIT_OO );}
	 public static final TdwgArea ABBREV_POL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_POL_OO );}
	 public static final TdwgArea ABBREV_POR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_POR_OO );}
	 public static final TdwgArea ABBREV_PRM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PRM_OO );}
	 public static final TdwgArea ABBREV_PUE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_PUE_OO );}
	 public static final TdwgArea ABBREV_QLD_CS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_QLD_CS );}
	 public static final TdwgArea ABBREV_QLD_QU () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_QLD_QU );}
	 public static final TdwgArea ABBREV_QUE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_QUE_OO );}
	 public static final TdwgArea ABBREV_REU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_REU_OO );}
	 public static final TdwgArea ABBREV_RHO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RHO_OO );}
	 public static final TdwgArea ABBREV_ROD_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ROD_OO );}
	 public static final TdwgArea ABBREV_ROM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ROM_OO );}
	 public static final TdwgArea ABBREV_RUC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUC_OO );}
	 public static final TdwgArea ABBREV_RUE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUE_OO );}
	 public static final TdwgArea ABBREV_RUN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUN_OO );}
	 public static final TdwgArea ABBREV_RUS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUS_OO );}
	 public static final TdwgArea ABBREV_RUW_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RUW_OO );}
	 public static final TdwgArea ABBREV_RWA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_RWA_OO );}
	 public static final TdwgArea ABBREV_SAK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAK_OO );}
	 public static final TdwgArea ABBREV_SAM_AS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAM_AS );}
	 public static final TdwgArea ABBREV_SAM_WS () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAM_WS );}
	 public static final TdwgArea ABBREV_SAR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAR_OO );}
	 public static final TdwgArea ABBREV_SAS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAS_OO );}
	 public static final TdwgArea ABBREV_SAU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SAU_OO );}
	 public static final TdwgArea ABBREV_SCA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCA_OO );}
	 public static final TdwgArea ABBREV_SCI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCI_OO );}
	 public static final TdwgArea ABBREV_SCS_PI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCS_PI );}
	 public static final TdwgArea ABBREV_SCS_SI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCS_SI );}
	 public static final TdwgArea ABBREV_SCZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SCZ_OO );}
	 public static final TdwgArea ABBREV_SDA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SDA_OO );}
	 public static final TdwgArea ABBREV_SEL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEL_OO );}
	 public static final TdwgArea ABBREV_SEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEN_OO );}
	 public static final TdwgArea ABBREV_SEY_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SEY_OO );}
	 public static final TdwgArea ABBREV_SGE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SGE_OO );}
	 public static final TdwgArea ABBREV_SIC_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIC_MA );}
	 public static final TdwgArea ABBREV_SIC_SI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIC_SI );}
	 public static final TdwgArea ABBREV_SIE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIE_OO );}
	 public static final TdwgArea ABBREV_SIN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SIN_OO );}
	 public static final TdwgArea ABBREV_SOA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOA_OO );}
	 public static final TdwgArea ABBREV_SOC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOC_OO );}
	 public static final TdwgArea ABBREV_SOL_NO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOL_NO );}
	 public static final TdwgArea ABBREV_SOL_SO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOL_SO );}
	 public static final TdwgArea ABBREV_SOM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SOM_OO );}
	 public static final TdwgArea ABBREV_SPA_AN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SPA_AN );}
	 public static final TdwgArea ABBREV_SPA_GI () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SPA_GI );}
	 public static final TdwgArea ABBREV_SPA_SP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SPA_SP );}
	 public static final TdwgArea ABBREV_SRL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SRL_OO );}
	 public static final TdwgArea ABBREV_SSA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SSA_OO );}
	 public static final TdwgArea ABBREV_STH_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_STH_OO );}
	 public static final TdwgArea ABBREV_SUD_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUD_OO );}
	 public static final TdwgArea ABBREV_SUL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUL_OO );}
	 public static final TdwgArea ABBREV_SUM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUM_OO );}
	 public static final TdwgArea ABBREV_SUR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SUR_OO );}
	 public static final TdwgArea ABBREV_SVA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SVA_OO );}
	 public static final TdwgArea ABBREV_SWC_CC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWC_CC );}
	 public static final TdwgArea ABBREV_SWC_HC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWC_HC );}
	 public static final TdwgArea ABBREV_SWC_NC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWC_NC );}
	 public static final TdwgArea ABBREV_SWE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWE_OO );}
	 public static final TdwgArea ABBREV_SWI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWI_OO );}
	 public static final TdwgArea ABBREV_SWZ_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_SWZ_OO );}
	 public static final TdwgArea ABBREV_TAI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAI_OO );}
	 public static final TdwgArea ABBREV_TAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAN_OO );}
	 public static final TdwgArea ABBREV_TAS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TAS_OO );}
	 public static final TdwgArea ABBREV_TCI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCI_OO );}
	 public static final TdwgArea ABBREV_TCS_AB () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_AB );}
	 public static final TdwgArea ABBREV_TCS_AD () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_AD );}
	 public static final TdwgArea ABBREV_TCS_AR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_AR );}
	 public static final TdwgArea ABBREV_TCS_AZ () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_AZ );}
	 public static final TdwgArea ABBREV_TCS_GR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_GR );}
	 public static final TdwgArea ABBREV_TCS_NA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_NA );}
	 public static final TdwgArea ABBREV_TCS_NK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TCS_NK );}
	 public static final TdwgArea ABBREV_TDC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TDC_OO );}
	 public static final TdwgArea ABBREV_TEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TEN_OO );}
	 public static final TdwgArea ABBREV_TEX_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TEX_OO );}
	 public static final TdwgArea ABBREV_THA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_THA_OO );}
	 public static final TdwgArea ABBREV_TKM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TKM_OO );}
	 public static final TdwgArea ABBREV_TOG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOG_OO );}
	 public static final TdwgArea ABBREV_TOK_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOK_MA );}
	 public static final TdwgArea ABBREV_TOK_SW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOK_SW );}
	 public static final TdwgArea ABBREV_TOK_TO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TOK_TO );}
	 public static final TdwgArea ABBREV_TON_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TON_OO );}
	 public static final TdwgArea ABBREV_TRT_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TRT_OO );}
	 public static final TdwgArea ABBREV_TUA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUA_OO );}
	 public static final TdwgArea ABBREV_TUB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUB_OO );}
	 public static final TdwgArea ABBREV_TUE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUE_OO );}
	 public static final TdwgArea ABBREV_TUN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUN_OO );}
	 public static final TdwgArea ABBREV_TUR_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUR_OO );}
	 public static final TdwgArea ABBREV_TUV_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TUV_OO );}
	 public static final TdwgArea ABBREV_TVA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVA_OO );}
	 public static final TdwgArea ABBREV_TVL_GA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVL_GA );}
	 public static final TdwgArea ABBREV_TVL_MP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVL_MP );}
	 public static final TdwgArea ABBREV_TVL_NP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVL_NP );}
	 public static final TdwgArea ABBREV_TVL_NW () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TVL_NW );}
	 public static final TdwgArea ABBREV_TZK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_TZK_OO );}
	 public static final TdwgArea ABBREV_UGA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UGA_OO );}
	 public static final TdwgArea ABBREV_UKR_MO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UKR_MO );}
	 public static final TdwgArea ABBREV_UKR_UK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UKR_UK );}
	 public static final TdwgArea ABBREV_URU_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_URU_OO );}
	 public static final TdwgArea ABBREV_UTA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UTA_OO );}
	 public static final TdwgArea ABBREV_UZB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_UZB_OO );}
	 public static final TdwgArea ABBREV_VAN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VAN_OO );}
	 public static final TdwgArea ABBREV_VEN_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VEN_OO );}
	 public static final TdwgArea ABBREV_VER_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VER_OO );}
	 public static final TdwgArea ABBREV_VIC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VIC_OO );}
	 public static final TdwgArea ABBREV_VIE_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VIE_OO );}
	 public static final TdwgArea ABBREV_VNA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VNA_OO );}
	 public static final TdwgArea ABBREV_VRG_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_VRG_OO );}
	 public static final TdwgArea ABBREV_WAK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAK_OO );}
	 public static final TdwgArea ABBREV_WAL_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAL_OO );}
	 public static final TdwgArea ABBREV_WAS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAS_OO );}
	 public static final TdwgArea ABBREV_WAU_AC () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAU_AC );}
	 public static final TdwgArea ABBREV_WAU_WA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WAU_WA );}
	 public static final TdwgArea ABBREV_WDC_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WDC_OO );}
	 public static final TdwgArea ABBREV_WHM_HP () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WHM_HP );}
	 public static final TdwgArea ABBREV_WHM_JK () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WHM_JK );}
	 public static final TdwgArea ABBREV_WHM_UT () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WHM_UT );}
	 public static final TdwgArea ABBREV_WIN_BA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_BA );}
	 public static final TdwgArea ABBREV_WIN_DO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_DO );}
	 public static final TdwgArea ABBREV_WIN_GR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_GR );}
	 public static final TdwgArea ABBREV_WIN_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_MA );}
	 public static final TdwgArea ABBREV_WIN_SL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_SL );}
	 public static final TdwgArea ABBREV_WIN_SV () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIN_SV );}
	 public static final TdwgArea ABBREV_WIS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WIS_OO );}
	 public static final TdwgArea ABBREV_WSA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WSA_OO );}
	 public static final TdwgArea ABBREV_WSB_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WSB_OO );}
	 public static final TdwgArea ABBREV_WVA_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WVA_OO );}
	 public static final TdwgArea ABBREV_WYO_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_WYO_OO );}
	 public static final TdwgArea ABBREV_XMS_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_XMS_OO );}
	 public static final TdwgArea ABBREV_YAK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YAK_OO );}
	 public static final TdwgArea ABBREV_YEM_NY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YEM_NY );}
	 public static final TdwgArea ABBREV_YEM_SY () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YEM_SY );}
	 public static final TdwgArea ABBREV_YUG_BH () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_BH );}
	 public static final TdwgArea ABBREV_YUG_CR () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_CR );}
	 public static final TdwgArea ABBREV_YUG_KO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_KO );}
	 public static final TdwgArea ABBREV_YUG_MA () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_MA );}
	 public static final TdwgArea ABBREV_YUG_MN () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_MN );}
	 public static final TdwgArea ABBREV_YUG_SE () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_SE );}
	 public static final TdwgArea ABBREV_YUG_SL () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUG_SL );}
	 public static final TdwgArea ABBREV_YUK_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_YUK_OO );}
	 public static final TdwgArea ABBREV_ZAI_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZAI_OO );}
	 public static final TdwgArea ABBREV_ZAM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZAM_OO );}
	 public static final TdwgArea ABBREV_ZIM_OO () { return (TdwgArea)termMap.get(TdwgArea.uuidABBREV_ZIM_OO );}

	
	protected static TdwgArea getTermByUuid(UUID uuid){
		if (termMap == null){
			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
			vocabularyStore.initialize();
		}
		return (TdwgArea)termMap.get(uuid);
	}
	
	/**
	 * FIXME This class should really be refactored into an interface and service implementation,
	 * relying on TermVocabularyDao / service (Ben)
	 * @param tdwgAbbreviation
	 * @return
	 */
	public static NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation){
		if (abbrevMap == null){
			initMaps();
		}
		UUID uuid = abbrevMap.get(tdwgAbbreviation);
		if (uuid == null){
			logger.warn("Unknown TDWG area: " + CdmUtils.Nz(tdwgAbbreviation));
			return null;
		}
		return TdwgArea.getTermByUuid(uuid);
	}
	
	/**
	 * FIXME This class should really be refactored into an interface and service implementation,
	 * relying on TermVocabularyDao / service (Ben)
	 * @param tdwgLabel
	 * @return
	 */
	public static NamedArea getAreaByTdwgLabel(String tdwgLabel){
		if (labelMap == null){
			initMaps();
		}
		UUID uuid = labelMap.get(tdwgLabel);
		if (uuid == null){
			logger.warn("Unknown TDWG area: " + CdmUtils.Nz(tdwgLabel));
			return null;
		}
		return TdwgArea.getTermByUuid(uuid);
	}
	
	public static boolean isTdwgAreaLabel(String label){
		if (labelMap.containsKey(label)){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isTdwgAreaAbbreviation(String abbrev){
		if (abbrevMap.containsKey(abbrev)){
			return true;
		}else{
			return false;
		}
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.location.NamedArea#setDefaultTerms(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
//	@Override
//	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
//		Set<NamedArea> terms = termVocabulary.getTerms();
//		for (NamedArea term : terms){
//			addTdwgArea(term);
//		}
//	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
		termMap = new HashMap<UUID, TdwgArea>();
		for (NamedArea term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (TdwgArea)term);  //TODO casting
			addTdwgArea(term);
		}
	}
	
	
	protected static void addTdwgArea(NamedArea area){
		if (area == null){
			logger.warn("tdwg area is null");
			return;
		}
		Language lang = Language.DEFAULT();
		Representation representation = area.getRepresentation(lang);
		String tdwgAbbrevLabel = representation.getAbbreviatedLabel();
		String tdwgLabel = representation.getLabel();
		if (tdwgAbbrevLabel == null){
			logger.warn("tdwgLabel = null");
			return;
		}
		//init map
		if (abbrevMap == null){
			abbrevMap = new HashMap<String, UUID>();
		}
		if (labelMap == null){
			labelMap = new HashMap<String, UUID>();
		}
		//add to map
		abbrevMap.put(tdwgAbbrevLabel, area.getUuid());
		labelMap.put(tdwgLabel, area.getUuid());
		//add type
		area.setType(NamedAreaType.ADMINISTRATION_AREA());
		//add level
		if (tdwgAbbrevLabel.trim().length()== 1){
			area.setLevel(NamedAreaLevel.TDWG_LEVEL1());
		}else if (tdwgAbbrevLabel.trim().length()== 2){
			area.setLevel(NamedAreaLevel.TDWG_LEVEL2());
		}else if (tdwgAbbrevLabel.trim().length()== 3){
			area.setLevel(NamedAreaLevel.TDWG_LEVEL3());
		}else if (tdwgAbbrevLabel.trim().length()== 6){
			area.setLevel(NamedAreaLevel.TDWG_LEVEL4());
		}else {
			logger.warn("Unknown TDWG Level " + tdwgAbbrevLabel + "! Unvalid string length (" +  tdwgAbbrevLabel.length() +")");
		}	
	}
	
	private static void initMaps(){
		labelMap = new HashMap<String, UUID>();
		abbrevMap = new HashMap<String, UUID>();
	}
	

//********************* OLD ******************************/
	
	
	private static NamedArea getNamedAreaByTdwgLabel(String tdwgLabel){
		if (tdwgLabel == null){
			return null;
		}
		InputStream file;
		try {
			file = CdmUtils.getReadableResourceStream("");
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
		Element root = XmlHelp.getRoot(file, "RDF");
		Namespace nsRdf = root.getNamespace("rdf");
		XmlHelp.getFirstAttributedChild(root, "", "ID", tdwgLabel.trim());
		
		//Filter filter = ;
		//root.getDescendants(filter);
		return null;
	}	
	
}
