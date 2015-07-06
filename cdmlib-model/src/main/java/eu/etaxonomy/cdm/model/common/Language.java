/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * list of languages according to current internet best practices as given by IANA
 * or ISO codes.  http://www.ietf.org/rfc/rfc4646.txt
 * http://www.loc.gov/standards/iso639-2/php/English_list.php
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Language")
@XmlRootElement(name = "Language")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Language extends DefinedTermBase<Language> {
    private static final long serialVersionUID = -5030610079904074217L;
    private static final Logger logger = Logger.getLogger(Language.class);

    public static final UUID uuidLanguageVocabulary = UUID.fromString("45ac7043-7f5e-4f37-92f2-3874aaaef2de");

    public static final UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");

    private static final UUID uuidAfar = UUID.fromString("b3ad88e2-0080-466f-9bf4-b01ba4122563");
    private static final UUID uuidAbkhazian = UUID.fromString("0888079f-587e-4c3b-af63-97990e27c368");
    private static final UUID uuidAchinese = UUID.fromString("15c94e31-6bd2-4a42-92a7-59ec7fe2eb93");
    private static final UUID uuidAcoli = UUID.fromString("7d54e491-bda0-4b10-9cf8-285e020782ad");
    private static final UUID uuidAdangme = UUID.fromString("55b079b1-be19-43fc-8827-cb71b373862f");
    private static final UUID uuidAdyghe_Adygei = UUID.fromString("1a083800-0add-464e-8db3-618ec96c620b");
    private static final UUID uuidAfroAsiatic_Other = UUID.fromString("f85f8fcd-eb10-41bd-b296-22bb4aa25147");
    private static final UUID uuidAfrihili = UUID.fromString("eda7677e-2041-4c2b-b84b-43c88d21ef7b");
    private static final UUID uuidAfrikaans = UUID.fromString("b604349f-2bc0-44f5-92dd-a66dba3fe055");
    private static final UUID uuidAinu = UUID.fromString("7bebf196-0b7a-4d9b-92c6-a698b9407b7e");
    private static final UUID uuidAkan = UUID.fromString("c462c732-aa55-423b-96a9-c43bf10d53b3");
    private static final UUID uuidAkkadian = UUID.fromString("5f08f9a9-81f7-4613-935a-2be7713f8428");
    private static final UUID uuidAlbanian = UUID.fromString("ecfa8b2e-2517-4974-8431-b64b67fa5cda");
    private static final UUID uuidAleut = UUID.fromString("3241952f-d366-43c3-8817-57452c7a09e7");
    private static final UUID uuidAlgonquians = UUID.fromString("2d318a92-1e48-458e-b0cd-28165ee95cde");
    private static final UUID uuidSouthernAltai = UUID.fromString("4f6854d1-f650-4278-9f9e-2c41d9866852");
    private static final UUID uuidAmharic = UUID.fromString("1a4d87e6-ac4c-43ec-a4fb-452a19347684");
    private static final UUID uuidEnglishOld = UUID.fromString("9e433e6b-044e-49d0-8b87-87af9e249e04");
    private static final UUID uuidAngika = UUID.fromString("86b14ab7-2989-4448-b23b-0c6185281732");
    private static final UUID uuidApaches = UUID.fromString("d8bcbc07-f7af-439c-8ad9-89aada5898fb");
    private static final UUID uuidArabic = UUID.fromString("4d3ec2eb-536f-4aab-81c5-34e37a3edbba");
    private static final UUID uuidOfficialAramaic_ImperialAramaic = UUID.fromString("00d6849c-fe18-4b60-98b2-403f376ba884");
    private static final UUID uuidAragonese = UUID.fromString("01ea9821-6ccc-4547-bc0a-9bff15be7f98");
    private static final UUID uuidArmenian = UUID.fromString("7a0fde13-26e9-4382-a5c9-5640fc2b3334");
    private static final UUID uuidMapudungun_Mapuche = UUID.fromString("27dab6b2-23c2-4101-bd6c-9980d09939d3");
    private static final UUID uuidArapaho = UUID.fromString("4b8ae63d-0078-4454-95a6-2248c025d8a5");
    private static final UUID uuidArtificial_Other = UUID.fromString("79c5e0f8-7abf-4ca0-9d35-59a2448b9fc8");
    private static final UUID uuidArawak = UUID.fromString("1ea1a358-539a-4b2a-a5fe-2e184f272c6a");
    private static final UUID uuidAssamese = UUID.fromString("efe01e0d-8c64-4613-b1dd-b696cb809bc3");
    private static final UUID uuidAsturian_Bable_Leonese_Asturleonese = UUID.fromString("a03f2fe0-9a5b-4498-a45d-d52c5ae8034a");
    private static final UUID uuidAthapascans = UUID.fromString("5bc5c0dc-47cf-426b-bd81-743dc55ca0e5");
    private static final UUID uuidAustralians = UUID.fromString("14056109-b3df-45a2-a3bb-7f6760ce692d");
    private static final UUID uuidAvaric = UUID.fromString("d7e8d6f1-9ded-4984-a5e6-2cd87734cfa1");
    private static final UUID uuidAvestan = UUID.fromString("6ab9eb02-55d9-42f5-8b50-c7dfba9a19d7");
    private static final UUID uuidAwadhi = UUID.fromString("353409b1-30f1-4d34-b1c0-cf024d49c69e");
    private static final UUID uuidAymara = UUID.fromString("a63a0463-5d91-46fc-b62e-be4308178b64");
    private static final UUID uuidAzerbaijani = UUID.fromString("2fc29072-908d-4bd3-b12f-cd2587ab8d75");
    private static final UUID uuidBandas = UUID.fromString("008bde55-5aad-4d9c-98be-29d2cae5ec4c");
    private static final UUID uuidBamilekes = UUID.fromString("c054f189-c54c-4fb8-b797-72691ec42ec1");
    private static final UUID uuidBashkir = UUID.fromString("f998ead1-6619-4993-8c11-e41d6119d31a");
    private static final UUID uuidBaluchi = UUID.fromString("364ffc64-7248-4ad6-acdd-35daded04819");
    private static final UUID uuidBambara = UUID.fromString("8688cf91-11fe-4395-a7f8-bf22017d99d8");
    private static final UUID uuidBalinese = UUID.fromString("3c483fd1-07b7-4144-8b27-bba27a03e96e");
    private static final UUID uuidBasque = UUID.fromString("d9ed469d-9a2d-42e2-b23d-a09cdd8b53c5");
    private static final UUID uuidBasa = UUID.fromString("45ee4d82-3561-4095-bae0-c054383c651f");
    private static final UUID uuidBaltic_Other = UUID.fromString("49698678-dc5e-45d8-afb2-09f57d27e176");
    private static final UUID uuidBeja_Bedawiyet = UUID.fromString("d1c3e5ef-31f0-42b8-8220-65e667fef638");
    private static final UUID uuidBelorussian = UUID.fromString("6dbc6679-c2bb-43bb-9c22-c1c4097c14f0");
    private static final UUID uuidBemba = UUID.fromString("4bde6eea-75b8-4fd1-9b8f-59221a953487");
    private static final UUID uuidBengali = UUID.fromString("873e34fd-8f43-4df0-a8cc-03f0666262c2");
    private static final UUID uuidBerber_Other = UUID.fromString("e25fbf49-c4a1-4c98-944c-3999d3dc9b51");
    private static final UUID uuidBhojpuri = UUID.fromString("4fb5c849-a07a-4a5c-9886-4d957605d600");
    private static final UUID uuidBihari = UUID.fromString("6720fbd6-7668-418e-8d48-a1bfe2f56ac9");
    private static final UUID uuidBikol = UUID.fromString("dc4f5a15-8b94-47d4-bd3e-22c8b79e22af");
    private static final UUID uuidBini_Edo = UUID.fromString("2b024ede-442e-4915-9cce-9e61fa0f3574");
    private static final UUID uuidBislama = UUID.fromString("84633f7a-7dc6-41bf-a5c3-2882e691c974");
    private static final UUID uuidSiksika = UUID.fromString("876ed782-f285-4395-b4d9-c8871a18e822");
    private static final UUID uuidBantu_Other = UUID.fromString("8b4f894a-dac4-4a0a-957c-6ca9c5081672");
    private static final UUID uuidBosnian = UUID.fromString("e7e7f3c6-68b4-42aa-a7ce-e3748ecd504e");
    private static final UUID uuidBraj = UUID.fromString("374814fe-1c3d-46aa-9a95-e8d5fe7108e8");
    private static final UUID uuidBreton = UUID.fromString("7cafd5d8-b61d-4942-b9c9-c2dcadefb22c");
    private static final UUID uuidBataks = UUID.fromString("4c6285d7-befc-4329-b27f-3a213969c9cf");
    private static final UUID uuidBuriat = UUID.fromString("beb5c17f-d561-40ca-8ed9-4a643ea10f63");
    private static final UUID uuidBuginese = UUID.fromString("bce6190e-f45b-43bf-bc99-ca527b1d2598");
    private static final UUID uuidBulgarian = UUID.fromString("6a3409a6-4804-4b6f-9efb-179003523e11");
    private static final UUID uuidBurmese = UUID.fromString("2eee09b8-57eb-4087-9538-7bf4e8312594");
    private static final UUID uuidBlin_Bilin = UUID.fromString("32e8a660-ad9d-4402-ac2a-7c2c545a94f6");
    private static final UUID uuidCaddo = UUID.fromString("cceeeeba-158f-4b18-916a-a329cb03414e");
    private static final UUID uuidCentralAmericanIndian_Other = UUID.fromString("15f73328-e7ad-4a6b-a31e-6477bd978e87");
    private static final UUID uuidGalibi_Carib = UUID.fromString("f3964fe0-7625-431f-8155-b646d1671b44");
    private static final UUID uuidCatalan_Valencian = UUID.fromString("45d05a7a-ee78-462c-a177-6fd743cf4d03");
    private static final UUID uuidCaucasian_Other = UUID.fromString("f6d6297f-00c4-4833-8f7b-001d2c3418e5");
    private static final UUID uuidCebuano = UUID.fromString("394994b3-576a-484d-a57f-e207deece4c8");
    private static final UUID uuidCeltic_Other = UUID.fromString("ddb2fe99-1841-49ac-9686-0149771f0887");
    private static final UUID uuidChamorro = UUID.fromString("6b7505cc-4a1e-47be-8374-8612deaba8e7");
    private static final UUID uuidChibcha = UUID.fromString("5731c457-580c-4543-a227-b0ca0c0b9d0e");
    private static final UUID uuidChechen = UUID.fromString("569d40d8-5c04-4b0a-8606-61fd91d498b5");
    private static final UUID uuidChagatai = UUID.fromString("4c2cfc13-efbb-4aa1-88b5-960adc8c29a0");
    private static final UUID uuidChinese = UUID.fromString("a9fc2782-5b2a-466f-b9c3-64d9ca6614c4");
    private static final UUID uuidChuukese = UUID.fromString("f5199701-8265-492f-bbce-5d545642120d");
    private static final UUID uuidMari = UUID.fromString("f5a3dd7e-d3ff-4db2-8213-5fb36faad4e6");
    private static final UUID uuidChinook_jargon = UUID.fromString("c8e3e73c-fb9b-44e2-8bb3-7c91cd423b40");
    private static final UUID uuidChoctaw = UUID.fromString("3b3fd5bd-009a-401a-8136-9a6c0c2705d0");
    private static final UUID uuidChipewyan_Dene_Suline = UUID.fromString("13ef4b98-3a37-4bed-9bee-96d3a10eed02");
    private static final UUID uuidCherokee = UUID.fromString("617cb541-33a5-43ee-85fa-01823c1f1007");
    private static final UUID uuidChurchSlavic_OldSlavonic_etc = UUID.fromString("e1997c20-90c7-4b7a-a7c3-b75f9cff6aeb");
    private static final UUID uuidChuvash = UUID.fromString("514ab40c-1fa9-44ba-8af0-f432a20305fe");
    private static final UUID uuidCheyenne = UUID.fromString("0a3dc1c3-cade-473d-ac58-2777fc31078d");
    private static final UUID uuidChamics = UUID.fromString("076909b8-88f0-4341-921a-380a9f24c63b");
    private static final UUID uuidCoptic = UUID.fromString("af13a51b-3d91-4978-80db-985e4f9aefdd");
    private static final UUID uuidCornish = UUID.fromString("110474af-7886-4a36-9299-73871ce14efa");
    private static final UUID uuidCorsican = UUID.fromString("2f57ca56-69fe-4713-88d2-2d0d3744bf81");
    private static final UUID uuidCreolesAndPidginsEnglishBased_Other = UUID.fromString("c2412f89-94ec-4cec-b75b-218d6202138e");
    private static final UUID uuidCreolesAndPidginsFrenchBased_Other = UUID.fromString("ee8e984e-03d5-4aa7-aa52-95b014b2c7a2");
    private static final UUID uuidCreolesAndPidginsPortugueseBased_Other = UUID.fromString("35a5f90e-ea2d-4bf4-8a09-7b00a35b61bb");
    private static final UUID uuidCree = UUID.fromString("7f822594-8e97-456b-8e47-a421f557f222");
    private static final UUID uuidCrimeanTatar_CrimeanTurkish = UUID.fromString("6574b007-bc42-4ee2-9051-de3d2655578f");
    private static final UUID uuidCreolesAndPidgins_Other = UUID.fromString("296a223d-1705-4504-aff2-8cbc56ee9187");
    private static final UUID uuidKashubian = UUID.fromString("82c34ba6-a4ff-448b-a195-f56b31d6d824");
    private static final UUID uuidCushitic_Other = UUID.fromString("875dd186-fdfa-4b1e-af26-d03e736a8757");
    private static final UUID uuidCzech = UUID.fromString("ba95f29b-f11c-4d79-9973-6de75087c7b3");
    private static final UUID uuidDakota = UUID.fromString("36f105eb-466b-45e9-a182-3887736d5f86");
    private static final UUID uuidDanish = UUID.fromString("45e52b16-61b0-4b86-92ba-7f290dbb39b3");
    private static final UUID uuidDargwa = UUID.fromString("93c45f3f-4bf9-4732-a1fe-da32d28cd4a2");
    private static final UUID uuidLandDayaks = UUID.fromString("f86fb9c0-2557-4457-8c5f-85629361dfaa");
    private static final UUID uuidDelaware = UUID.fromString("4c8f21cf-b84b-4ce2-a1e5-755e6c9762a7");
    private static final UUID uuidSlaveAthapascan = UUID.fromString("e2769990-9f04-45fc-974b-ef4d8f77ec86");
    private static final UUID uuidDogrib = UUID.fromString("6a32c3a0-11b7-450c-8da6-f06ed18d17d3");
    private static final UUID uuidDinka = UUID.fromString("40704577-d65a-46de-8393-5aabc9a510e1");
    private static final UUID uuidDivehi_Dhivehi_Maldivian = UUID.fromString("fb78fb2e-2c98-461c-9b7c-9cc08f85a221");
    private static final UUID uuidDogri = UUID.fromString("39c5ae60-ace7-4525-a85b-47edb11f5de6");
    private static final UUID uuidDravidian_Other = UUID.fromString("3ffb59ff-d558-48f5-bec1-03fee76737f7");
    private static final UUID uuidLowerSorbian = UUID.fromString("f6dfb41f-c6c1-4d21-a51c-0fbd7cee2dff");
    private static final UUID uuidDuala = UUID.fromString("bbfffd3e-eada-4c2d-8824-b426cdd82ee6");
    private static final UUID uuidDutchMiddle = UUID.fromString("2b53c222-7752-4943-a366-35b9818f794c");
    private static final UUID uuidDutchFlemish = UUID.fromString("9965d79a-acf9-4921-a2c0-863b8c16c056");
    private static final UUID uuidDyula = UUID.fromString("ac6431db-b89f-445e-8b4c-c9835142bce7");
    private static final UUID uuidDzongkha = UUID.fromString("b049c1c1-ddb5-48f3-ad7a-c6aeb1cb1432");
    private static final UUID uuidEfik = UUID.fromString("627d5b24-121d-4d97-b531-d4ff26ea9f54");
    private static final UUID uuidEgyptianAncient = UUID.fromString("0901686b-fcab-45df-ae09-b8bf86d64ce6");
    private static final UUID uuidEkajuk = UUID.fromString("67f01415-52d8-4a86-86eb-484fdb0606b8");
    private static final UUID uuidElamite = UUID.fromString("4fed61b5-fef3-4e4c-8a45-360645a3b504");
    private static final UUID uuidEnglishMiddle = UUID.fromString("a3a2fc60-9f74-45e0-a97a-6737576071ef");
    private static final UUID uuidEsperanto = UUID.fromString("38060cd9-8d47-4a61-bf33-333583c8fef1");
    private static final UUID uuidEstonian = UUID.fromString("4fb12988-754d-430e-9840-561a258bb3e9");
    private static final UUID uuidEwe = UUID.fromString("b11d3bd9-daab-4399-b289-e6f0e2a0ec29");
    private static final UUID uuidEwondo = UUID.fromString("18f56fec-fa36-4317-853b-0b574df05efc");
    private static final UUID uuidFang = UUID.fromString("fefaa178-65ff-4add-bddf-c89be78fa5a8");
    private static final UUID uuidFaroese = UUID.fromString("64e01a12-06d5-44a4-8a83-52c7762e3d92");
    private static final UUID uuidFanti = UUID.fromString("5f2f0f6e-82ff-4c67-918a-a433ecaa2f04");
    private static final UUID uuidFijian = UUID.fromString("6355af54-4be2-40a3-a417-83921fb70dc7");
    private static final UUID uuidFilipinoPilipino = UUID.fromString("c82767ce-1623-4e86-a999-3f9baff8ace4");
    private static final UUID uuidFinnish = UUID.fromString("4606abdf-6dbe-4bfe-b3da-a1fa08f2f1e2");
    private static final UUID uuidFinnoUgrian_Other = UUID.fromString("e4fd3d5d-7715-4736-b4db-73ac8b129ddf");
    private static final UUID uuidFon = UUID.fromString("61cb4280-42d1-4fa0-92bf-fe7cf02c66d3");
    public static final UUID uuidFrench = UUID.fromString("7759a1d8-a5ea-454a-8c93-1dcfaae8cc21");
    private static final UUID uuidFrenchMiddle = UUID.fromString("bc2b8d06-94f8-4ddb-a4ad-769105868b73");
    private static final UUID uuidFrenchOld = UUID.fromString("27bd4b8b-07d0-425c-a843-61588c140f68");
    private static final UUID uuidNorthernFrisian = UUID.fromString("f5977654-d71b-4ff1-96cb-d2130e978aef");
    private static final UUID uuidEasternFrisian = UUID.fromString("0beb31ca-2479-47c7-b557-bd44dc620f16");
    private static final UUID uuidWesternFrisian = UUID.fromString("1b171828-16da-45ea-bfd9-5bf236481920");
    private static final UUID uuidFulah = UUID.fromString("0c27960e-1ba6-4b56-8429-c44334251653");
    private static final UUID uuidFriulian = UUID.fromString("65cb508a-c974-4417-8509-f199c0e074b9");
    private static final UUID uuidGa = UUID.fromString("0ddc00b2-b3ef-4074-a2c7-4b969dcd8e8e");
    private static final UUID uuidGayo = UUID.fromString("984b3d84-61f2-455a-9924-15f86f89dfb1");
    private static final UUID uuidGbaya = UUID.fromString("f8fb14ee-eb4e-49fa-b439-ed5deac59daa");
    private static final UUID uuidGermanic_Other = UUID.fromString("d32011e1-faf8-4310-80b1-a79476a8cecc");
    private static final UUID uuidGeorgian = UUID.fromString("fb64b07c-c079-4fda-a803-212a0beca61b");
    private static final UUID uuidGerman = UUID.fromString("d1131746-e58b-4e80-a865-f5182c9c3073");
    private static final UUID uuidGeez = UUID.fromString("f42c6f52-1a3c-4965-8dfa-2c828463741c");
    private static final UUID uuidGilbertese = UUID.fromString("b20fa600-5b9b-4714-8dcd-51275a6e1ab0");
    private static final UUID uuidGaelic_ScottishGaelic = UUID.fromString("b5d99800-b40b-4e3d-ada9-610475ffa68b");
    private static final UUID uuidIrish = UUID.fromString("11659dd0-9feb-447d-8144-c4c18f834450");
    private static final UUID uuidGalician = UUID.fromString("a29e944f-162f-46c1-bea9-3e472c5d5b48");
    private static final UUID uuidManx = UUID.fromString("a27c3cb8-8913-45f9-99a3-f3770a258f70");
    private static final UUID uuidGermanMiddleHigh = UUID.fromString("d37af6b4-3740-4190-800c-0e28bb0c10bf");
    private static final UUID uuidGermanOldHigh = UUID.fromString("088a4d6a-e048-4299-be2c-15425b2827cf");
    private static final UUID uuidGondi = UUID.fromString("49cb27e4-bd94-422e-9a9a-26b5b05371d7");
    private static final UUID uuidGorontalo = UUID.fromString("a50a5d26-8786-4549-99c1-b4280fd713a1");
    private static final UUID uuidGothic = UUID.fromString("64ebceaa-4aa8-48a2-ae9a-95c862c37a50");
    private static final UUID uuidGrebo = UUID.fromString("6503a6bb-497c-4712-b18d-38c8898c3b77");
    private static final UUID uuidGreekAncient = UUID.fromString("7330bcfc-0b4c-4c96-b3f8-4585fde000f5");
    private static final UUID uuidGreekModern = UUID.fromString("1b48a41b-cbf3-4041-b567-4782c854b41e");
    private static final UUID uuidGuarani = UUID.fromString("40d9eb34-361f-42d9-a6b3-70a512608bd3");
    private static final UUID uuidSwissGerman_Alemannic = UUID.fromString("e8a2f6b2-8f34-4514-8771-5a3ec93d2468");
    private static final UUID uuidGujarati = UUID.fromString("929c554c-03be-4327-86e1-8175b01a8995");
    private static final UUID uuidGwichin = UUID.fromString("1f3440d4-0a36-4a04-9694-259a64275934");
    private static final UUID uuidHaida = UUID.fromString("c7bef5c5-6fb6-4fd2-b464-fed4f052adcb");
    private static final UUID uuidHaitian_HaitianCreole = UUID.fromString("ce883d86-d6e9-427e-b79d-52b4df25d451");
    private static final UUID uuidHausa = UUID.fromString("b7ef8288-c37b-436f-a144-e4e50d66f919");
    private static final UUID uuidHawaiian = UUID.fromString("35ef668e-daea-45a7-909b-2e2ac08eff46");
    private static final UUID uuidHebrew = UUID.fromString("ecd35102-7cdd-4809-9261-4aa272d8051e");
    private static final UUID uuidHerero = UUID.fromString("845634a3-153e-4534-8362-ff087210b625");
    private static final UUID uuidHiligaynon = UUID.fromString("d051c901-fc8c-4a72-bffb-a9a7ca8f5cc4");
    private static final UUID uuidHimachali = UUID.fromString("31244589-2ac5-4b61-be85-2cea16991204");
    private static final UUID uuidHindi = UUID.fromString("0a1d9d1d-135d-4575-b172-669b51673c39");
    private static final UUID uuidHittite = UUID.fromString("0532c7c2-5c49-4730-ba7d-0512036a36ac");
    private static final UUID uuidHmong = UUID.fromString("fc81a52e-6e94-4f97-83dd-f179208a6592");
    private static final UUID uuidHiriMotu = UUID.fromString("a7b014f9-683b-46c4-8365-b08d7a9a5759");
    private static final UUID uuidUpperSorbian = UUID.fromString("fb30387d-a91a-4586-a801-bd04ea4f9e47");
    private static final UUID uuidHungarian = UUID.fromString("f817b594-98a7-49a2-a712-94b1148a4340");
    private static final UUID uuidHupa = UUID.fromString("bfbc8359-711d-430e-bfba-5e96c3a24fbb");
    private static final UUID uuidIban = UUID.fromString("e72b4d06-a496-4e55-8c5d-093594ab8da2");
    private static final UUID uuidIgbo = UUID.fromString("384d5bef-e085-4408-9c9c-73499c735ecf");
    private static final UUID uuidIcelandic = UUID.fromString("08ac9207-e338-46cd-aef3-3e49b7b34df9");
    private static final UUID uuidIdo = UUID.fromString("7e7f3bdb-3131-4428-a352-690f72196050");
    private static final UUID uuidSichuanYi_Nuosu = UUID.fromString("eca9d6fc-08fa-4c43-9fbb-2348baf75d77");
    private static final UUID uuidIjos = UUID.fromString("73b8dfb4-971c-4b93-9009-75ae79dc1cba");
    private static final UUID uuidInuktitut = UUID.fromString("bce2b666-d77c-4f49-a3c6-7e5cebb6cb79");
    private static final UUID uuidInterlingue_Occidental = UUID.fromString("76af0bce-e24e-4b71-9b6a-6234d89f447e");
    private static final UUID uuidIloko = UUID.fromString("fdd6cb21-e42f-4fb4-a8c5-bb6bc0d0073b");
    private static final UUID uuidInterlingua = UUID.fromString("72235dce-b0f2-4bbe-a400-f236cadeb86e");
    private static final UUID uuidIndic_Other = UUID.fromString("3d1d88e7-172b-440a-b763-da26a4a11d39");
    private static final UUID uuidIndonesian = UUID.fromString("fa367cfb-8aad-42fb-8a19-cd6582e58fd4");
    private static final UUID uuidIndoEuropean_Other = UUID.fromString("407cacc3-a6f5-452e-8984-8e68a701bee8");
    private static final UUID uuidIngush = UUID.fromString("83ea3620-caf2-4b85-8eb3-97428a73142c");
    private static final UUID uuidInupiaq = UUID.fromString("5d2452c1-2cee-45d7-b952-a5401b20b9f5");
    private static final UUID uuidIranian_Other = UUID.fromString("548e5159-98ce-49a8-bcce-b83f77790005");
    private static final UUID uuidIroquoians = UUID.fromString("465c09ba-cbce-4dab-a449-1c05094f4d2b");
    private static final UUID uuidItalian = UUID.fromString("fecbf0c7-fea9-465b-8a16-950517c5c0c4");
    private static final UUID uuidJavanese = UUID.fromString("5e8ddd48-cc3f-48a8-9c1a-ec6e11409b17");
    private static final UUID uuidLojban = UUID.fromString("ea9a2d74-f0cc-4271-93cd-f591db4efcb7");
    private static final UUID uuidJapanese = UUID.fromString("6778c7fb-c195-4dc1-ae3f-164201314e51");
    private static final UUID uuidJudeoPersian = UUID.fromString("091cc8f8-681c-4ce8-b9c4-d451fe13c024");
    private static final UUID uuidJudeoArabic = UUID.fromString("2097879c-db7a-4674-a459-4a4ef3c15b54");
    private static final UUID uuidKaraKalpak = UUID.fromString("cdd10fa8-e4c9-4eab-9f53-34227846c1e0");
    private static final UUID uuidKabyle = UUID.fromString("01706d2f-e221-4ade-95f2-1942d050bb94");
    private static final UUID uuidKachin_Jingpho = UUID.fromString("37230e36-55f9-4ba7-a191-703ab5a5e860");
    private static final UUID uuidKalaallisut_Greenlandic = UUID.fromString("a3504321-79ed-4d5d-9cbb-9e354084055b");
    private static final UUID uuidKamba = UUID.fromString("47a2576b-44e8-4a2d-b85b-6f13dfa77467");
    private static final UUID uuidKannada = UUID.fromString("d482a3d5-5a01-458a-aa6d-da17fa4bd90f");
    private static final UUID uuidKarens = UUID.fromString("78dc9e98-f56a-4bca-b791-8fe159183858");
    private static final UUID uuidKashmiri = UUID.fromString("d4876784-4fc9-4c56-8a72-7519186ce98c");
    private static final UUID uuidKanuri = UUID.fromString("7f8f18e9-dbc4-482e-a8cc-160d58823fd6");
    private static final UUID uuidKawi = UUID.fromString("53fa6337-d66c-4db5-89cd-d42555414b32");
    private static final UUID uuidKazakh = UUID.fromString("66f7ed40-4391-4c81-9785-539ec7c79249");
    private static final UUID uuidKabardian = UUID.fromString("928a8b3f-baaf-4106-a8b2-8611af982fbc");
    private static final UUID uuidKhasi = UUID.fromString("50a69d8b-d82e-4a71-afad-60dfcc41a5a7");
    private static final UUID uuidKhoisan_Other = UUID.fromString("ae787603-3070-4298-9ca6-4cbe73378122");
    private static final UUID uuidCentralKhmer = UUID.fromString("093412de-4e67-4413-9423-90e273408076");
    private static final UUID uuidKhotanese = UUID.fromString("c3c6cdef-a514-4e55-8628-b8d3e67cd781");
    private static final UUID uuidKikuyu_Gikuyu = UUID.fromString("ed994d78-c442-4ea8-aede-8723a2bee717");
    private static final UUID uuidKinyarwanda = UUID.fromString("53c05614-2669-4c30-93aa-217c19848eb8");
    private static final UUID uuidKirghiz_Kyrgyz = UUID.fromString("543a305e-1edb-4405-9c24-4305a187dc84");
    private static final UUID uuidKimbundu = UUID.fromString("b4c105cb-a53e-4734-acd8-3128ba76451b");
    private static final UUID uuidKonkani = UUID.fromString("3499eab3-428c-48a5-9c1c-f951a2d3fa40");
    private static final UUID uuidKomi = UUID.fromString("73f52d44-2fef-496a-ba6f-1315b1bb814e");
    private static final UUID uuidKongo = UUID.fromString("b52f5fcf-16fe-4641-8e42-642d95c1da36");
    private static final UUID uuidKorean = UUID.fromString("c999f0da-0bb9-465b-ad52-8b48354cb591");
    private static final UUID uuidKosraean = UUID.fromString("6c081f57-acbf-4eb6-826e-8d32b5b42fe4");
    private static final UUID uuidKpelle = UUID.fromString("193637cb-cfe6-4f6f-9e15-58d7d0e9edd9");
    private static final UUID uuidKarachayBalkar = UUID.fromString("e6000995-1f07-4dea-8cf6-aad23c8b513f");
    private static final UUID uuidKarelian = UUID.fromString("62ff33ff-a2e4-4cc0-bc77-477647dedec0");
    private static final UUID uuidKrus = UUID.fromString("14a934a5-de0c-4371-80f1-def859437bfb");
    private static final UUID uuidKurukh = UUID.fromString("e9f02ab1-d578-44fd-ad8e-addd938e2132");
    private static final UUID uuidKuanyama_Kwanyama = UUID.fromString("e431127a-da51-45bd-8b4e-d50068913097");
    private static final UUID uuidKumyk = UUID.fromString("99a4a641-f292-464e-8ef0-7480dd2e3d95");
    private static final UUID uuidKurdish = UUID.fromString("176c9e51-4106-4ac4-a3e8-19eff0969147");
    private static final UUID uuidKutenai = UUID.fromString("afbcfec6-22c4-4ec2-ad00-fb6381251076");
    private static final UUID uuidLadino = UUID.fromString("d8330a9e-05fb-4e89-871c-daa8d13be847");
    private static final UUID uuidLahnda = UUID.fromString("55f20809-946a-424c-8eda-f14b0e8fc6c0");
    private static final UUID uuidLamba = UUID.fromString("34f22b78-1cfe-4f4f-a1c0-e57ea7d47ee5");
    private static final UUID uuidLao = UUID.fromString("0b9fbbfa-b8e6-4d4d-a90d-a4988f6c3531");
    public static final UUID uuidLatin = UUID.fromString("160a5b6c-87f5-4422-9bda-78cd404c179e");
    private static final UUID uuidLatvian = UUID.fromString("173784fb-62ce-4741-b214-53cb5d297411");
    private static final UUID uuidLezghian = UUID.fromString("4783aea1-46c0-40b3-ad02-c13157c7817d");
    private static final UUID uuidLimburgan_Limburger_Limburgish = UUID.fromString("682508f8-414a-4958-a89d-3abea3da1852");
    private static final UUID uuidLingala = UUID.fromString("a00fc918-4d32-4974-94ed-076b1fec9798");
    private static final UUID uuidLithuanian = UUID.fromString("7c080971-7a7e-460a-a772-66790e91ac35");
    private static final UUID uuidMongo = UUID.fromString("ded2f739-586d-42ea-a1b1-5ed3e877d1be");
    private static final UUID uuidLozi = UUID.fromString("4f0d316e-df44-4f6b-b318-cc8539db9895");
    private static final UUID uuidLuxembourgish_Letzeburgesch = UUID.fromString("4dd4d7bf-2e9e-4b2e-8165-5d160d32980b");
    private static final UUID uuidLubaLulua = UUID.fromString("4b826c59-54ec-42a2-8789-67dcffd5c8fb");
    private static final UUID uuidLubaKatanga = UUID.fromString("2a2da196-a57b-480c-a6c9-0f97f66d1ee1");
    private static final UUID uuidGanda = UUID.fromString("7eed96ab-e0ca-48c3-801f-023fe79097a3");
    private static final UUID uuidLuiseno = UUID.fromString("922703d9-b4c3-4643-9503-42e6b7f0fc62");
    private static final UUID uuidLunda = UUID.fromString("cdba1ceb-868f-4672-9afe-0f63f30b60e8");
    private static final UUID uuidLuo = UUID.fromString("7d6068b3-5980-4e3e-82b0-58be20ea01b7");
    private static final UUID uuidLushai = UUID.fromString("8f78143c-854a-40b3-85e7-51d9f202c7b7");
    private static final UUID uuidMacedonian = UUID.fromString("c23f0629-a4f6-45c7-b8c8-4916209af0dd");
    private static final UUID uuidMadurese = UUID.fromString("fa73c1f6-c1b1-4f8f-98a1-8bbd0aaf2f33");
    private static final UUID uuidMagahi = UUID.fromString("7ea5af43-3438-4d44-abd0-e9fa298e6049");
    private static final UUID uuidMarshallese = UUID.fromString("960cbb52-0a61-4273-ae61-09d982c48f40");
    private static final UUID uuidMaithili = UUID.fromString("01ae8ef1-6918-4d3b-9453-b51ce003002a");
    private static final UUID uuidMakasar = UUID.fromString("46328a2f-4042-4897-9340-009c3635adc7");
    private static final UUID uuidMalayalam = UUID.fromString("015a6c3b-5d22-40de-bccd-2d737978b1c2");
    private static final UUID uuidMandingo = UUID.fromString("3af29601-772e-4e15-8f7e-65f31e4b5427");
    private static final UUID uuidMaori = UUID.fromString("867fbe26-e8be-49db-b751-6e8da681a37a");
    private static final UUID uuidAustronesian_Other = UUID.fromString("c52c53b4-26b1-4bfe-8da1-eb958561dc11");
    private static final UUID uuidMarathi = UUID.fromString("53264953-1c1f-4314-a600-c8d399505dbb");
    private static final UUID uuidMasai = UUID.fromString("0fb6df7f-a2c0-426f-baf6-9fe4fc6d622b");
    private static final UUID uuidMalay = UUID.fromString("638c12eb-3954-4bf4-a008-6177a2f880e1");
    private static final UUID uuidMoksha = UUID.fromString("87f46e66-852a-442e-b6b1-e3cec6a398e6");
    private static final UUID uuidMandar = UUID.fromString("f87a2743-0344-4515-a5aa-145966f7ed6f");
    private static final UUID uuidMende = UUID.fromString("d311117f-45aa-4dd6-8ecb-12a9fc5654d4");
    private static final UUID uuidIrishMiddle = UUID.fromString("852da1de-8083-4047-a056-2dc1c9beb815");
    private static final UUID uuidMikmaq_Micmac = UUID.fromString("f529fd4c-0d3f-481b-a35c-1bf26fff80da");
    private static final UUID uuidMinangkabau = UUID.fromString("62528d73-3100-4814-8579-84b1825ae320");
    private static final UUID uuidUncodeds = UUID.fromString("92f1ab72-f760-4a86-b7d6-0859c7961159");
    private static final UUID uuidMonKhmer_Other = UUID.fromString("ca1321fb-4b3d-4e55-ade9-78a26ed71930");
    private static final UUID uuidMalagasy = UUID.fromString("0057bec5-a596-4cbd-9876-d23cf71fee60");
    private static final UUID uuidMaltese = UUID.fromString("c212eff2-5f02-4127-9e39-0e1daa82c075");
    private static final UUID uuidManchu = UUID.fromString("32f7628f-6142-46b2-bc6b-d1381de48eec");
    private static final UUID uuidManipuri = UUID.fromString("cc5c315b-096c-4fc3-a81f-7d404378576c");
    private static final UUID uuidManobos = UUID.fromString("7131c3ae-33e2-4983-b7d9-1d7bfb481f6b");
    private static final UUID uuidMohawk = UUID.fromString("da124f07-db02-47b9-b699-df08142a3d3c");
    private static final UUID uuidMoldavian = UUID.fromString("fe22c65f-3bd7-491c-a067-c2ab111adf29");
    private static final UUID uuidMongolian = UUID.fromString("93261622-1e2a-42a3-b10d-eb3f19262ba3");
    private static final UUID uuidMossi = UUID.fromString("555a247e-05d3-4bb1-b497-d4ecf3dea075");
    private static final UUID uuidMultiples = UUID.fromString("052b0c83-456b-4592-b0c7-58318dbdb34a");
    private static final UUID uuidMundas = UUID.fromString("2feb1694-8fd2-4746-b1d0-8f8c6233be58");
    private static final UUID uuidCreek = UUID.fromString("2ee4a0f9-2a46-429c-aec7-fc10c2567da1");
    private static final UUID uuidMirandese = UUID.fromString("2d69695f-a114-4415-b6e8-5248bba8853c");
    private static final UUID uuidMarwari = UUID.fromString("4824b8e7-c283-43c2-ac98-d986e0e12893");
    private static final UUID uuidMayans = UUID.fromString("17c38fb4-2acc-4cfc-a0d1-83772410c161");
    private static final UUID uuidErzya = UUID.fromString("a78f236f-8fbf-4788-86ed-80fecc3e0fe6");
    private static final UUID uuidNahuatls = UUID.fromString("de64fb4d-9bb7-494f-973d-90a91338e3e2");
    private static final UUID uuidNorthAmericanIndian = UUID.fromString("dad6f73d-69fb-4006-8626-51710ab3a6b6");
    private static final UUID uuidNeapolitan = UUID.fromString("3f53b130-6da7-45a2-8bb1-cb7d53f59d1d");
    private static final UUID uuidNauru = UUID.fromString("3eb63458-aa52-41d6-8abc-173ad217d7e2");
    private static final UUID uuidNavajoNavaho = UUID.fromString("1a05e1fe-49d4-44bb-a143-70df32606124");
    private static final UUID uuidNdebeleSouth = UUID.fromString("ab1ad6e6-3ad8-4aa0-9fa5-e817240ce215");
    private static final UUID uuidNdebeleNorth = UUID.fromString("6d50329c-f53e-48c7-808c-562ddd5998bb");
    private static final UUID uuidNdonga = UUID.fromString("2cf61318-c2ef-4aaf-b9bc-5317c291d8fa");
    private static final UUID uuidLowGerman_LowSaxon = UUID.fromString("5c74671f-2e73-469d-9818-d1f098b200d4");
    private static final UUID uuidNepali = UUID.fromString("b8d0e46b-fc62-471c-aaee-e2580e60439b");
    private static final UUID uuidNepalBhasa_Newari = UUID.fromString("3f362e1d-eb8f-40dd-afb5-3884224bbd48");
    private static final UUID uuidNias = UUID.fromString("5d81b61c-7891-4423-8f8f-5b1d89b28e42");
    private static final UUID uuidNigerKordofanian_Other = UUID.fromString("a3161d26-b1bb-4efe-943c-96d1eb1d1984");
    private static final UUID uuidNiuean = UUID.fromString("c8a8a134-32bc-48c0-829f-1dd811866888");
    private static final UUID uuidNorwegianNynorsk = UUID.fromString("abc32d6e-f8cd-4218-8921-8713160cb0a4");
    private static final UUID uuidNorwegianBokmol = UUID.fromString("efae63c3-f627-47a4-9e2d-8d5924662d1f");
    private static final UUID uuidNogai = UUID.fromString("9da86f92-895b-4c9a-bec3-9591d002b278");
    private static final UUID uuidNorseOld = UUID.fromString("005f5fa5-ed4b-445f-9b8a-16662485462d");
    private static final UUID uuidNorwegian = UUID.fromString("1ea2f3c9-dde4-4e1e-a9fc-675dc378091c");
    private static final UUID uuidNKo = UUID.fromString("f8adeec5-80e3-4945-a1c8-2e7f0ca6ffd7");
    private static final UUID uuidPedi_Sepedi_NorthernSotho = UUID.fromString("dc5a1a59-7b4e-41b8-9aa6-182eca2d8778");
    private static final UUID uuidNubians = UUID.fromString("29cc1f21-8945-47e6-8d23-bfa1878d3bf0");
    private static final UUID uuidClassicalNewari_OldNewari_ClassicalNepal_Bhasa = UUID.fromString("aaae2afe-0a27-4aac-a53c-c0efdb9b00f9");
    private static final UUID uuidChichewa_Chewa_Nyanja = UUID.fromString("17c29acf-96d3-484c-8285-2861fdbdb2f3");
    private static final UUID uuidNyamwezi = UUID.fromString("bd5b4edb-271d-4b3c-b22c-9d53cbc5a829");
    private static final UUID uuidNyankole = UUID.fromString("df2a640f-461a-4152-927e-9e55aa8fad27");
    private static final UUID uuidNyoro = UUID.fromString("71950910-41a0-40a9-8c48-3ac16773e352");
    private static final UUID uuidNzima = UUID.fromString("dc20e27e-c0ed-4dcd-8bb7-ebf7aa67fb16");
    private static final UUID uuidOccitan_Provenal = UUID.fromString("4aad4259-7541-40e3-9954-a36ec49497ce");
    private static final UUID uuidOjibwa = UUID.fromString("0daa9618-b0d8-4363-8790-ece21928e665");
    private static final UUID uuidOriya = UUID.fromString("5b34bd0a-270c-4348-8ab0-a53c938bf90b");
    private static final UUID uuidOromo = UUID.fromString("3b54c94b-e6c5-4072-91c8-bfe383279a9e");
    private static final UUID uuidOsage = UUID.fromString("47071c5a-9f49-4aa1-9a35-49e320b875a2");
    private static final UUID uuidOssetian_Ossetic = UUID.fromString("9b315a1e-f772-4f3f-839c-3fe1f4cb1d67");
    private static final UUID uuidTurkish_Ottoman = UUID.fromString("adf29c75-6b22-4fe0-a0ce-d1c2c8259ead");
    private static final UUID uuidOtomians = UUID.fromString("e4326d01-381b-4024-a748-995e26bb0362");
    private static final UUID uuidPapuan_Other = UUID.fromString("e19c2fc4-75d1-4071-92bb-cde6a8b7383e");
    private static final UUID uuidPangasinan = UUID.fromString("b56ddf7a-975e-4d18-91c4-5952c0f5465e");
    private static final UUID uuidPahlavi = UUID.fromString("32c5f396-e6dc-49c3-bba5-79faeacac765");
    private static final UUID uuidPampanga_Kapampangan = UUID.fromString("f805e450-16ce-4abc-8e79-c9599238b836");
    private static final UUID uuidPanjabi_Punjabi = UUID.fromString("1753ee27-b780-4844-bcb3-bb0d834721aa");
    private static final UUID uuidPapiamento = UUID.fromString("706ce687-ceae-4664-b977-acbcf6a68241");
    private static final UUID uuidPalauan = UUID.fromString("e446caca-0588-41f1-8b28-637ee9c8aad2");
    private static final UUID uuidPersian_Old = UUID.fromString("8b061029-05a6-4fb4-b630-8a04d69cb54f");
    private static final UUID uuidPersian = UUID.fromString("e17273b1-3c4e-4816-91df-c0117cfc2b85");
    private static final UUID uuidPhilippine_Other = UUID.fromString("5591a708-17c7-40c8-9d2c-d3ad20150e45");
    private static final UUID uuidPhoenician = UUID.fromString("b5dc41c7-250d-47d3-aa85-7a7c6b880cb8");
    private static final UUID uuidPali = UUID.fromString("72859b96-61b9-4400-8489-392ceb529b89");
    private static final UUID uuidPolish = UUID.fromString("3fdca387-f1b0-4ec1-808f-1bc3dc482194");
    private static final UUID uuidPohnpeian = UUID.fromString("9a463924-a311-44f3-82e1-88bd4a9e664d");
    private static final UUID uuidPortuguese = UUID.fromString("c2c08339-2405-4d7d-bd25-cbe01fb7ce09");
    private static final UUID uuidPrakrits = UUID.fromString("53fc1b5e-64c4-41ac-8a97-60507a1983fc");
    private static final UUID uuidProvenalOld = UUID.fromString("4e826395-2eae-4a2e-8d7d-ba9cbd16e2a3");
    private static final UUID uuidPushto = UUID.fromString("cc6f53cb-5b8b-47a3-9faf-ba6dbde58941");
    private static final UUID uuidQuechua = UUID.fromString("aa6d4890-5725-4ac6-bf42-3eb827b24fc9");
    private static final UUID uuidRajasthani = UUID.fromString("16aac6c9-cefc-49d6-a8b9-c35f27b68c0d");
    private static final UUID uuidRapanui = UUID.fromString("24d342fe-8800-4284-af6d-7431be07112c");
    private static final UUID uuidRarotongan_CookIslands_Maori = UUID.fromString("8b0a01ec-7c13-4ab8-8fe8-49046a6b1595");
    private static final UUID uuidRomance_Other = UUID.fromString("89189a82-18ff-4385-aa92-200b12692760");
    private static final UUID uuidRomansh = UUID.fromString("04b151e3-c3db-467b-a9ec-f175ab3a52c4");
    private static final UUID uuidRomany = UUID.fromString("225123e7-aa55-41b4-bf14-ab9ef9fb7795");
    private static final UUID uuidRomanian = UUID.fromString("7b4ab3d4-0575-4038-840d-e15caeeedd66");
    private static final UUID uuidRundi = UUID.fromString("2af255f1-52bb-41ae-986f-9a6b71e0ddc4");
    private static final UUID uuidAromanian_Arumanian_MacedoRomanian = UUID.fromString("97b1d73a-2f45-4955-b6ee-2047326a8453");
    private static final UUID uuidRussian = UUID.fromString("64ea9354-cbf8-40de-9f6e-387d24896f50");
    private static final UUID uuidSandawe = UUID.fromString("ed2ea195-a502-4835-948a-8fa00ef6930d");
    private static final UUID uuidSango = UUID.fromString("a9641a51-0502-4798-acc5-2daeb81cc3a2");
    private static final UUID uuidYakut = UUID.fromString("591bb9ab-2e86-4583-b363-cbc8ed70be20");
    private static final UUID uuidSouthAmericanIndian_Other = UUID.fromString("b40cc17a-b05e-4f7b-a3a1-cbf396b79c5e");
    private static final UUID uuidSalishans = UUID.fromString("5017f3a8-3035-4731-8249-e371ee583142");
    private static final UUID uuidSamaritanAramaic = UUID.fromString("86dff8de-2732-4b40-82a4-814897ea2714");
    private static final UUID uuidSanskrit = UUID.fromString("e2141d42-40ea-4eb8-87c7-54faa88526d5");
    private static final UUID uuidSasak = UUID.fromString("7e575cba-a1dd-4773-b712-b4dc031554ab");
    private static final UUID uuidSantali = UUID.fromString("7527cedc-8a0b-4199-9b0f-da1c98b5884e");
    private static final UUID uuidSerbian = UUID.fromString("531e06fd-798c-4700-afe6-2d8a01c5ec46");
    private static final UUID uuidSicilian = UUID.fromString("47f02bfd-d79c-41a4-be9b-43aa9e278f66");
    private static final UUID uuidScots = UUID.fromString("5ff32317-1ddb-43f3-b5e2-42a63efb90c7");
    private static final UUID uuidCroatian = UUID.fromString("db9d12b2-ef5a-4756-88f9-b79cd9b64a01");
    private static final UUID uuidSelkup = UUID.fromString("94130a17-38e2-4c60-993a-355fdd9bd8a1");
    private static final UUID uuidSemitic_Other = UUID.fromString("8331f629-647d-4646-a413-e57c5f7e21a4");
    private static final UUID uuidIrishOld = UUID.fromString("a70f310b-1922-4d48-991f-b3aa63f30832");
    private static final UUID uuidSignLanguages = UUID.fromString("4bcaa01a-1290-4186-a40e-65978af53031");
    private static final UUID uuidShan = UUID.fromString("ebcccf8d-5516-470f-a80c-f74440c000a0");
    private static final UUID uuidSidamo = UUID.fromString("bd7ce28f-6e88-4075-b1b5-2420a94233e6");
    private static final UUID uuidSinhala_Sinhalese = UUID.fromString("69e1325e-5afb-4ffb-b446-1c9e20bb5ea1");
    private static final UUID uuidSiouans = UUID.fromString("c5d539de-0c8b-44cd-86e3-965a85a58892");
    private static final UUID uuidSinoTibetan_Other = UUID.fromString("3e1f5c2e-0103-4bfe-8aa6-e6d01c965539");
    private static final UUID uuidSlavic_Other = UUID.fromString("038ea17e-064a-42f2-9062-fbc679746023");
    private static final UUID uuidSlovak = UUID.fromString("b8a83111-4a2f-4ca4-a27c-fc0d75585963");
    private static final UUID uuidSlovenian = UUID.fromString("1e50c9f4-4261-465c-a7c7-191ec49596ff");
    private static final UUID uuidSouthernSami = UUID.fromString("26f8dcaa-50c5-47b1-b6c3-f2490dd80c78");
    private static final UUID uuidNorthernSami = UUID.fromString("6b8f25c2-ddb7-4b65-8722-ffbe992cf2ce");
    private static final UUID uuidSamis_Other = UUID.fromString("d8968f2e-e383-43d7-913b-d6f35ff9a587");
    private static final UUID uuidLuleSami = UUID.fromString("78e9cc09-7f4f-4d3f-91d5-a2062ce91e8b");
    private static final UUID uuidInariSami = UUID.fromString("032dba38-a378-4ab4-a00b-a94fb1020f53");
    private static final UUID uuidSamoan = UUID.fromString("c8f0c497-0328-4a68-b4b5-296da91875a6");
    private static final UUID uuidSkoltSami = UUID.fromString("12c36e0d-39d1-441c-8b39-87ad034fdf2f");
    private static final UUID uuidShona = UUID.fromString("d42c76ea-e92b-45f0-9e54-430d3519c037");
    private static final UUID uuidSindhi = UUID.fromString("1ea8fb85-19c9-48db-9ac6-5f21df1fd4c3");
    private static final UUID uuidSoninke = UUID.fromString("70c2402e-390b-4b4d-bfd4-582ed08e4af7");
    private static final UUID uuidSogdian = UUID.fromString("59729403-2378-4697-b390-ac41ecb72380");
    private static final UUID uuidSomali = UUID.fromString("35985d42-51bb-41b7-83a9-58845d499f8b");
    private static final UUID uuidSonghais = UUID.fromString("9ad544c7-f8d7-4bea-9476-26d982917ec8");
    private static final UUID uuidSothoSouthern = UUID.fromString("fb7e9746-1bc6-4384-802d-7784b8b301fa");
    private static final UUID uuidSpanish_Castilian = UUID.fromString("511d8125-f5e6-445d-aee2-6327375238be");
    private static final UUID uuidSardinian = UUID.fromString("f67971e9-49bb-4d85-9bc8-e6a1ea0d950e");
    private static final UUID uuidSrananTongo = UUID.fromString("52cc1adb-b715-4a2a-8aaf-8d32fddd3958");
    private static final UUID uuidSerer = UUID.fromString("3234653f-e10d-48a5-ade7-cd628e75fbe8");
    private static final UUID uuidNiloSaharan_Other = UUID.fromString("97e85881-0009-4b4b-a8dc-3c83eddb4440");
    private static final UUID uuidSwati = UUID.fromString("b8abe85d-b486-4951-a523-5278bda2aec1");
    private static final UUID uuidSukuma = UUID.fromString("63c28016-658e-47dd-88b5-2cd3f4a040bc");
    private static final UUID uuidSundanese = UUID.fromString("45c36762-b9e2-4696-8924-500f9e50b198");
    private static final UUID uuidSusu = UUID.fromString("71923e7c-ee28-4ca9-91ac-5c1a066d3293");
    private static final UUID uuidSumerian = UUID.fromString("ac588be1-1ec3-42ce-b8b5-c71d7519ece7");
    private static final UUID uuidSwahili = UUID.fromString("369e7731-be7e-40ef-96e9-3721813441fa");
    private static final UUID uuidSwedish = UUID.fromString("d4541a74-becc-4ba6-874c-510ee0d0a29f");
    private static final UUID uuidClassicalSyriac = UUID.fromString("e7d9f5c7-5fe0-424e-9935-ba61a0edf7c1");
    private static final UUID uuidSyriac = UUID.fromString("9d967232-e64f-44ee-8d18-f254b798d02d");
    private static final UUID uuidTahitian = UUID.fromString("8aa2fcb2-84fc-4418-87b2-23bf7f0ae783");
    private static final UUID uuidTai_Other = UUID.fromString("fb9c601e-26ea-4944-8925-5dea6feb1a34");
    private static final UUID uuidTamil = UUID.fromString("87f48d86-aa28-4080-b9da-186167eb3959");
    private static final UUID uuidTatar = UUID.fromString("4e184daa-9ee2-4c0e-842d-038f1c0b1280");
    private static final UUID uuidTelugu = UUID.fromString("07dfe9e0-8cbf-44fb-8722-397355b7c247");
    private static final UUID uuidTimne = UUID.fromString("1f763527-0257-4991-b669-214c4c2a972d");
    private static final UUID uuidTereno = UUID.fromString("10b3b191-20bd-4f6b-818f-068c70fc6d8b");
    private static final UUID uuidTetum = UUID.fromString("fe362c53-8790-4bdf-9372-eb0e90e5b4aa");
    private static final UUID uuidTajik = UUID.fromString("f75a6211-3022-4600-8053-278c85676f34");
    private static final UUID uuidTagalog = UUID.fromString("ca1e1cc3-ffe7-449e-8077-564b746d8526");
    private static final UUID uuidThai = UUID.fromString("1dd1794d-5868-4224-80c7-d17e0078eca8");
    private static final UUID uuidTibetan = UUID.fromString("0f6f42da-defe-4c6d-88d5-878339ba80f5");
    private static final UUID uuidTigre = UUID.fromString("f7c2ac83-079f-46c8-a2ca-e914e915d5a4");
    private static final UUID uuidTigrinya = UUID.fromString("4c7b08e5-1637-4b59-ad47-9c6a22e21eb1");
    private static final UUID uuidTiv = UUID.fromString("99267f5f-a2cc-4e4a-a0ae-e98c76034781");
    private static final UUID uuidTokelau = UUID.fromString("f763355e-8b02-4df2-a5f1-9120071c77cd");
    private static final UUID uuidKlingon_TlhInganHol = UUID.fromString("813810e4-96c9-43a5-9114-383506a33b46");
    private static final UUID uuidTlingit = UUID.fromString("02b6e82c-b223-4af3-9ced-eee857094245");
    private static final UUID uuidTamashek = UUID.fromString("ce15d514-c62d-4adb-b6f5-0096f9934d58");
    private static final UUID uuidTongaNyasa = UUID.fromString("fd8b1e12-cdd0-487c-9f47-9e97711ee37f");
    private static final UUID uuidTongaTongaIslands = UUID.fromString("fb7421ea-d446-410f-8ef2-3cd125256463");
    private static final UUID uuidTokPisin = UUID.fromString("ec2a7b15-ce28-4465-b1b3-5e045db869d3");
    private static final UUID uuidTsimshian = UUID.fromString("e8661eba-132c-4876-ac37-226db379ce4e");
    private static final UUID uuidTswana = UUID.fromString("8b589844-cd06-47f6-88cf-b4f8afa4ace7");
    private static final UUID uuidTsonga = UUID.fromString("a49330bb-a505-4778-9d81-251e2d5ee627");
    private static final UUID uuidTurkmen = UUID.fromString("d494cb91-b5df-4f7a-98fd-7257cec13213");
    private static final UUID uuidTumbuka = UUID.fromString("63dc5012-8072-4cb8-81dd-05558ba6679a");
    private static final UUID uuidTupis = UUID.fromString("73d9c324-fbc2-47b7-a4be-8f0f43485a82");
    private static final UUID uuidTurkish = UUID.fromString("ecf81f98-177d-49a6-ad0f-f6d89944c76b");
    private static final UUID uuidAltaic_Other = UUID.fromString("668645f4-f35f-4d1a-8343-cec4bc9d54b6");
    private static final UUID uuidTuvalu = UUID.fromString("28b0d5f1-d82b-4dc1-a778-f58331f48b3b");
    private static final UUID uuidTwi = UUID.fromString("4fddcfa6-7b46-45ca-a20f-eb406f89eaae");
    private static final UUID uuidTuvinian = UUID.fromString("c3017b5f-b1e0-4c8e-ad92-a92227f0230f");
    private static final UUID uuidUdmurt = UUID.fromString("e8e3a155-5306-4f71-898f-07eda08ed975");
    private static final UUID uuidUgaritic = UUID.fromString("05ce99f2-2c2f-4ee4-b471-32f3afda2afc");
    private static final UUID uuidUighur_Uyghur = UUID.fromString("55f1b4e0-fd9d-4849-a11c-5262b2e91a87");
    private static final UUID uuidUkrainian = UUID.fromString("09c912c2-06e0-45d5-911c-302447e24baa");
    private static final UUID uuidUmbundu = UUID.fromString("5b96e532-8fc7-418e-a051-887b857e93c0");
    private static final UUID uuidUndetermined = UUID.fromString("643df0b5-5c66-4c27-b5bb-a4f4b170c8c9");
    private static final UUID uuidUrdu = UUID.fromString("4441b4e1-382c-4a1e-86a4-aba93d20f6f3");
    private static final UUID uuidUzbek = UUID.fromString("0cb43888-e619-4e80-9beb-190884d50ea7");
    private static final UUID uuidVai = UUID.fromString("b2dd63be-9247-44ad-bf12-61c1f7066c46");
    private static final UUID uuidVenda = UUID.fromString("246fff28-776c-4ef3-a163-f43eb28cc348");
    private static final UUID uuidVietnamese = UUID.fromString("98468d65-43d7-41fc-b4b0-e1d1aacff950");
    private static final UUID uuidVolapek = UUID.fromString("b7b756db-fec1-4fea-8b5d-75cadb3e5590");
    private static final UUID uuidVotic = UUID.fromString("ba70c6aa-0d9f-4bae-b2a2-afca2c33bcb1");
    private static final UUID uuidWakashans = UUID.fromString("0107ae7d-d244-4352-ba4d-d2969c55d17e");
    private static final UUID uuidWalamo = UUID.fromString("ddaa93d0-3ad1-47f2-920d-c07120c96950");
    private static final UUID uuidWaray = UUID.fromString("40af687c-7706-459b-aa86-d4a5d7d27a14");
    private static final UUID uuidWasho = UUID.fromString("28a0ebd6-d0d4-41d7-a1e8-fd006141076f");
    private static final UUID uuidWelsh = UUID.fromString("c30ccf66-224c-4d65-8cde-77ea91df433c");
    private static final UUID uuidSorbians = UUID.fromString("a5c36bf0-26b6-4474-9bd7-4867c38ab047");
    private static final UUID uuidWalloon = UUID.fromString("0d21e928-75d8-4eef-a5dd-8ef53b93a1aa");
    private static final UUID uuidWolof = UUID.fromString("f0e386ec-b0c5-4d57-bff9-e9b6c72b3122");
    private static final UUID uuidKalmyk_Oirat = UUID.fromString("9ed0f57a-5af3-433a-bf38-808e2cf4103b");
    private static final UUID uuidXhosa = UUID.fromString("26fb67ac-42d8-4aa7-a8c6-47aaf827d7d7");
    private static final UUID uuidYao = UUID.fromString("86521056-865a-4cc4-9eeb-fbce391a20b9");
    private static final UUID uuidYapese = UUID.fromString("82624381-8969-4d4b-a0d3-802d84dbc5b0");
    private static final UUID uuidYiddish = UUID.fromString("04d495dc-449d-46b8-9cca-b108756773c0");
    private static final UUID uuidYoruba = UUID.fromString("e7a347b4-a38c-4c67-87aa-89db702d9b1b");
    private static final UUID uuidYupiks = UUID.fromString("2bf7a818-8b18-4693-91d0-6aa87808de79");
    private static final UUID uuidZapotec = UUID.fromString("b1d7880d-b45d-436b-8459-805089763b14");
    private static final UUID uuidBlissymbols_Blissymbolics_Bliss = UUID.fromString("fe08a3f9-256c-4eb8-849f-867682d243d1");
    private static final UUID uuidZenaga = UUID.fromString("595607e2-b8fc-4edc-8185-0e7b23491473");
    private static final UUID uuidZhuang_Chuang = UUID.fromString("efc8cc48-268c-4083-93cd-c8cf282c83b6");
    private static final UUID uuidZandes = UUID.fromString("0ba49b92-7d15-48de-a1de-74e3170b2da7");
    private static final UUID uuidZulu = UUID.fromString("1f8d4fcb-e888-4ac6-8113-63fe7ea77180");
    private static final UUID uuidZuni = UUID.fromString("d8ce3c47-243b-4a09-835b-01918829a2e7");
    private static final UUID uuidNoLinguisticContent = UUID.fromString("fb85de23-0e3c-4042-b56b-71ec3c7566cf");
    public static final UUID uuidZaza_Dimili_Dimli_Kirdki_Kirmanjki_Zazaki = UUID.fromString("e4bf2ec8-4c1a-4ece-9df1-4890a7f18457");

    public static final UUID uuidUnknownLanguage = UUID.fromString("3d05ab93-2d92-400d-a3dd-fc8dcc5f8203");
    public static final UUID uuidOriginalLanguage = UUID.fromString("42b893f3-16dd-417d-aa92-96cf6c695ca1");


    protected static Map<UUID, Language> termMap = null;

// *************************** Factory MEthods ********************************/

    public static Language NewInstance(){
        return new Language();
    }

    public static Language NewInstance(UUID uuid){
        return new Language(uuid);
    }

    /**
     * Creates a new language instance with a description (in the {@link Language#DEFAULT() default language}),
     * a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new language to be created
     * @param	label  		 the string identifying the new language to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new language to be created
     * @see 				 #readCsvLine(List, Language)
     * @see 				 #NewInstance()
     */
    public static Language NewInstance(String term, String label, String labelAbbrev){
        return new Language(term, label, labelAbbrev);
    }

    public static Language NewInstance(UUID uuid, String label, String iso639_2){
        Language result = Language.NewInstance(label, label, iso639_2);
        result.setUuid(uuid);
//        result.setIso639_2(iso639_2);
        return result;
    }

//**************** Attributes *************************************/

    @XmlAttribute(name = "iso639_1")
    //TODO create userDefinedType ?
    @Column(length=2)
    private String iso639_1;

//    @XmlAttribute(name = "iso639_2")
//    //TODO create userDefinedType ?
//    @Column(length=3)
//    private String iso639_2;

//***** CONSTRUCTOR ***************************************/

    //for hibernate use only
    @Deprecated
    protected Language() {
   		super(TermType.Language);
    };

    public Language(UUID uuid) {
        super(TermType.Language);
    	this.setUuid(uuid);
    }
    public Language(String iso639_1, String iso639_x2, String englishLabel, String frenchLabel) throws Exception {
        super(TermType.Language);
        if(iso639_1 != null && iso639_1.length() != 2){
            logger.warn("iso639_1 is not of size 2: "+iso639_1.toString());
        }
        if(iso639_x2 != null && iso639_x2.length() != 3){
            logger.warn("iso639_2 is not of size 3: "+iso639_x2.toString());
        }
        this.iso639_1=iso639_1;
//        this.iso639_2=iso639_2;
        String textEnglish = englishLabel;
        String textFrench = englishLabel;
        String label = iso639_x2;
        String labelAbbrev = null;
        this.addRepresentation(new Representation(textEnglish, label, labelAbbrev, Language.ENGLISH()));
        this.addRepresentation(new Representation(textFrench, label, labelAbbrev, Language.FRENCH()));
    }
    public Language(String text, String label, String labelAbbrev, Language lang) {
        super(TermType.Language);
    	this.addRepresentation(new Representation(text,label,labelAbbrev, lang));
    }
    public Language(String label, String text, String labelAbbrev) {
        this(label,text,labelAbbrev, DEFAULT());
    }

//********************************** METHODS *********************************************************/

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
     */
    @Override
    public void resetTerms(){
        termMap = null;
        defaultLanguage = null;
        csvLanguage = null;
    }


    protected static Language getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(Language.class, uuid);
        } else {
            return termMap.get(uuid);
        }
    }




    public static final Language ENGLISH(){/*@*/ return getTermByUuid(uuidEnglish);/*@*/}
    public static final Language AFAR(){/*@*/ return getTermByUuid(uuidAfar);/*@*/}
    public static final Language ABKHAZIAN(){/*@*/ return getTermByUuid(uuidAbkhazian);/*@*/}
    public static final Language ACHINESE(){/*@*/ return getTermByUuid(uuidAchinese);/*@*/}
    public static final Language ACOLI(){/*@*/ return getTermByUuid(uuidAcoli);/*@*/}
    public static final Language ADANGME(){/*@*/ return getTermByUuid(uuidAdangme);/*@*/}
    public static final Language ADYGHE_ADYGEI(){/*@*/ return getTermByUuid(uuidAdyghe_Adygei);/*@*/}
    public static final Language AFRO_ASIATIC_OTHER(){/*@*/ return getTermByUuid(uuidAfroAsiatic_Other);/*@*/}
    public static final Language AFRIHILI(){/*@*/ return getTermByUuid(uuidAfrihili);/*@*/}
    public static final Language AFRIKAANS(){/*@*/ return getTermByUuid(uuidAfrikaans);/*@*/}
    public static final Language AINU(){/*@*/ return getTermByUuid(uuidAinu);/*@*/}
    public static final Language AKAN(){/*@*/ return getTermByUuid(uuidAkan);/*@*/}
    public static final Language AKKADIAN(){/*@*/ return getTermByUuid(uuidAkkadian);/*@*/}
    public static final Language ALBANIAN(){/*@*/ return getTermByUuid(uuidAlbanian);/*@*/}
    public static final Language ALEUT(){/*@*/ return getTermByUuid(uuidAleut);/*@*/}
    public static final Language ALGONQUIANS(){/*@*/ return getTermByUuid(uuidAlgonquians);/*@*/}
    public static final Language SOUTHERN_ALTAI(){/*@*/ return getTermByUuid(uuidSouthernAltai);/*@*/}
    public static final Language AMHARIC(){/*@*/ return getTermByUuid(uuidAmharic);/*@*/}
    public static final Language ENGLISH_OLD(){/*@*/ return getTermByUuid(uuidEnglishOld);/*@*/}
    public static final Language ANGIKA(){/*@*/ return getTermByUuid(uuidAngika);/*@*/}
    public static final Language APACHES(){/*@*/ return getTermByUuid(uuidApaches);/*@*/}
    public static final Language ARABIC(){/*@*/ return getTermByUuid(uuidArabic);/*@*/}
    public static final Language OFFICIAL_ARAMAIC_IMPERIAL_ARAMAIC(){/*@*/ return getTermByUuid(uuidOfficialAramaic_ImperialAramaic);/*@*/}
    public static final Language ARAGONESE(){/*@*/ return getTermByUuid(uuidAragonese);/*@*/}
    public static final Language ARMENIAN(){/*@*/ return getTermByUuid(uuidArmenian);/*@*/}
    public static final Language MAPUDUNGUN_MAPUCHE(){/*@*/ return getTermByUuid(uuidMapudungun_Mapuche);/*@*/}
    public static final Language ARAPAHO(){/*@*/ return getTermByUuid(uuidArapaho);/*@*/}
    public static final Language ARTIFICIAL_OTHER(){/*@*/ return getTermByUuid(uuidArtificial_Other);/*@*/}
    public static final Language ARAWAK(){/*@*/ return getTermByUuid(uuidArawak);/*@*/}
    public static final Language ASSAMESE(){/*@*/ return getTermByUuid(uuidAssamese);/*@*/}
    public static final Language ASTURIAN_BABLE_LEONESE_ASTURLEONESE(){/*@*/ return getTermByUuid(uuidAsturian_Bable_Leonese_Asturleonese);/*@*/}
    public static final Language ATHAPASCANS(){/*@*/ return getTermByUuid(uuidAthapascans);/*@*/}
    public static final Language AUSTRALIANS(){/*@*/ return getTermByUuid(uuidAustralians);/*@*/}
    public static final Language AVARIC(){/*@*/ return getTermByUuid(uuidAvaric);/*@*/}
    public static final Language AVESTAN(){/*@*/ return getTermByUuid(uuidAvestan);/*@*/}
    public static final Language AWADHI(){/*@*/ return getTermByUuid(uuidAwadhi);/*@*/}
    public static final Language AYMARA(){/*@*/ return getTermByUuid(uuidAymara);/*@*/}
    public static final Language AZERBAIJANI(){/*@*/ return getTermByUuid(uuidAzerbaijani);/*@*/}
    public static final Language BANDAS(){/*@*/ return getTermByUuid(uuidBandas);/*@*/}
    public static final Language BAMILEKES(){/*@*/ return getTermByUuid(uuidBamilekes);/*@*/}
    public static final Language BASHKIR(){/*@*/ return getTermByUuid(uuidBashkir);/*@*/}
    public static final Language BALUCHI(){/*@*/ return getTermByUuid(uuidBaluchi);/*@*/}
    public static final Language BAMBARA(){/*@*/ return getTermByUuid(uuidBambara);/*@*/}
    public static final Language BALINESE(){/*@*/ return getTermByUuid(uuidBalinese);/*@*/}
    public static final Language BASQUE(){/*@*/ return getTermByUuid(uuidBasque);/*@*/}
    public static final Language BASA(){/*@*/ return getTermByUuid(uuidBasa);/*@*/}
    public static final Language BALTIC_OTHER(){/*@*/ return getTermByUuid(uuidBaltic_Other);/*@*/}
    public static final Language BEJA_BEDAWIYET(){/*@*/ return getTermByUuid(uuidBeja_Bedawiyet);/*@*/}
    public static final Language BELORUSSIAN(){/*@*/ return getTermByUuid(uuidBelorussian);/*@*/}
    public static final Language BEMBA(){/*@*/ return getTermByUuid(uuidBemba);/*@*/}
    public static final Language BENGALI(){/*@*/ return getTermByUuid(uuidBengali);/*@*/}
    public static final Language BERBER_OTHER(){/*@*/ return getTermByUuid(uuidBerber_Other);/*@*/}
    public static final Language BHOJPURI(){/*@*/ return getTermByUuid(uuidBhojpuri);/*@*/}
    public static final Language BIHARI(){/*@*/ return getTermByUuid(uuidBihari);/*@*/}
    public static final Language BIKOL(){/*@*/ return getTermByUuid(uuidBikol);/*@*/}
    public static final Language BINI_EDO(){/*@*/ return getTermByUuid(uuidBini_Edo);/*@*/}
    public static final Language BISLAMA(){/*@*/ return getTermByUuid(uuidBislama);/*@*/}
    public static final Language SIKSIKA(){/*@*/ return getTermByUuid(uuidSiksika);/*@*/}
    public static final Language BANTU_OTHER(){/*@*/ return getTermByUuid(uuidBantu_Other);/*@*/}
    public static final Language BOSNIAN(){/*@*/ return getTermByUuid(uuidBosnian);/*@*/}
    public static final Language BRAJ(){/*@*/ return getTermByUuid(uuidBraj);/*@*/}
    public static final Language BRETON(){/*@*/ return getTermByUuid(uuidBreton);/*@*/}
    public static final Language BATAKS(){/*@*/ return getTermByUuid(uuidBataks);/*@*/}
    public static final Language BURIAT(){/*@*/ return getTermByUuid(uuidBuriat);/*@*/}
    public static final Language BUGINESE(){/*@*/ return getTermByUuid(uuidBuginese);/*@*/}
    public static final Language BULGARIAN(){/*@*/ return getTermByUuid(uuidBulgarian);/*@*/}
    public static final Language BURMESE(){/*@*/ return getTermByUuid(uuidBurmese);/*@*/}
    public static final Language BLIN_BILIN(){/*@*/ return getTermByUuid(uuidBlin_Bilin);/*@*/}
    public static final Language CADDO(){/*@*/ return getTermByUuid(uuidCaddo);/*@*/}
    public static final Language CENTRAL_AMERICAN_INDIAN_OTHER(){/*@*/ return getTermByUuid(uuidCentralAmericanIndian_Other);/*@*/}
    public static final Language GALIBI_CARIB(){/*@*/ return getTermByUuid(uuidGalibi_Carib);/*@*/}
    public static final Language CATALAN_VALENCIAN(){/*@*/ return getTermByUuid(uuidCatalan_Valencian);/*@*/}
    public static final Language CAUCASIAN_OTHER(){/*@*/ return getTermByUuid(uuidCaucasian_Other);/*@*/}
    public static final Language CEBUANO(){/*@*/ return getTermByUuid(uuidCebuano);/*@*/}
    public static final Language CELTIC_OTHER(){/*@*/ return getTermByUuid(uuidCeltic_Other);/*@*/}
    public static final Language CHAMORRO(){/*@*/ return getTermByUuid(uuidChamorro);/*@*/}
    public static final Language CHIBCHA(){/*@*/ return getTermByUuid(uuidChibcha);/*@*/}
    public static final Language CHECHEN(){/*@*/ return getTermByUuid(uuidChechen);/*@*/}
    public static final Language CHAGATAI(){/*@*/ return getTermByUuid(uuidChagatai);/*@*/}
    public static final Language CHINESE(){/*@*/ return getTermByUuid(uuidChinese);/*@*/}
    public static final Language CHUUKESE(){/*@*/ return getTermByUuid(uuidChuukese);/*@*/}
    public static final Language MARI(){/*@*/ return getTermByUuid(uuidMari);/*@*/}
    public static final Language CHINOOK_JARGON(){/*@*/ return getTermByUuid(uuidChinook_jargon);/*@*/}
    public static final Language CHOCTAW(){/*@*/ return getTermByUuid(uuidChoctaw);/*@*/}
    public static final Language CHIPEWYAN_DENE_SULINE(){/*@*/ return getTermByUuid(uuidChipewyan_Dene_Suline);/*@*/}
    public static final Language CHEROKEE(){/*@*/ return getTermByUuid(uuidCherokee);/*@*/}
    public static final Language CHURCH_SLAVIC_OLD_SLAVONIC_ETC(){/*@*/ return getTermByUuid(uuidChurchSlavic_OldSlavonic_etc);/*@*/}
    public static final Language CHUVASH(){/*@*/ return getTermByUuid(uuidChuvash);/*@*/}
    public static final Language CHEYENNE(){/*@*/ return getTermByUuid(uuidCheyenne);/*@*/}
    public static final Language CHAMICS(){/*@*/ return getTermByUuid(uuidChamics);/*@*/}
    public static final Language COPTIC(){/*@*/ return getTermByUuid(uuidCoptic);/*@*/}
    public static final Language CORNISH(){/*@*/ return getTermByUuid(uuidCornish);/*@*/}
    public static final Language CORSICAN(){/*@*/ return getTermByUuid(uuidCorsican);/*@*/}
    public static final Language CREOLES_PIDGINS_ENGLISH_BASED_OTHER(){/*@*/ return getTermByUuid(uuidCreolesAndPidginsEnglishBased_Other);/*@*/}
    public static final Language CREOLES_PIDGINS_FRENCH_BASED_OTHER(){/*@*/ return getTermByUuid(uuidCreolesAndPidginsFrenchBased_Other);/*@*/}
    public static final Language CREOLES_PIDGINS_PORTUGUESE_BASED_OTHER(){/*@*/ return getTermByUuid(uuidCreolesAndPidginsPortugueseBased_Other);/*@*/}
    public static final Language CREE(){/*@*/ return getTermByUuid(uuidCree);/*@*/}
    public static final Language CRIMEAN_TATAR_CRIMEAN_TURKISH(){/*@*/ return getTermByUuid(uuidCrimeanTatar_CrimeanTurkish);/*@*/}
    public static final Language CREOLES_PIDGINS_OTHER(){/*@*/ return getTermByUuid(uuidCreolesAndPidgins_Other);/*@*/}
    public static final Language KASHUBIAN(){/*@*/ return getTermByUuid(uuidKashubian);/*@*/}
    public static final Language CUSHITIC_OTHER(){/*@*/ return getTermByUuid(uuidCushitic_Other);/*@*/}
    public static final Language CZECH(){/*@*/ return getTermByUuid(uuidCzech);/*@*/}
    public static final Language DAKOTA(){/*@*/ return getTermByUuid(uuidDakota);/*@*/}
    public static final Language DANISH(){/*@*/ return getTermByUuid(uuidDanish);/*@*/}
    public static final Language DARGWA(){/*@*/ return getTermByUuid(uuidDargwa);/*@*/}
    public static final Language LANDDAYAKS(){/*@*/ return getTermByUuid(uuidLandDayaks);/*@*/}
    public static final Language DELAWARE(){/*@*/ return getTermByUuid(uuidDelaware);/*@*/}
    public static final Language SLAVE_ATHAPASCAN(){/*@*/ return getTermByUuid(uuidSlaveAthapascan);/*@*/}
    public static final Language DOGRIB(){/*@*/ return getTermByUuid(uuidDogrib);/*@*/}
    public static final Language DINKA(){/*@*/ return getTermByUuid(uuidDinka);/*@*/}
    public static final Language DIVEHI_DHIVEHI_MALDIVIAN(){/*@*/ return getTermByUuid(uuidDivehi_Dhivehi_Maldivian);/*@*/}
    public static final Language DOGRI(){/*@*/ return getTermByUuid(uuidDogri);/*@*/}
    public static final Language DRAVIDIAN_OTHER(){/*@*/ return getTermByUuid(uuidDravidian_Other);/*@*/}
    public static final Language LOWERSORBIAN(){/*@*/ return getTermByUuid(uuidLowerSorbian);/*@*/}
    public static final Language DUALA(){/*@*/ return getTermByUuid(uuidDuala);/*@*/}
    public static final Language DUTCH_MIDDLE(){/*@*/ return getTermByUuid(uuidDutchMiddle);/*@*/}
    public static final Language DUTCH_FLEMISH(){/*@*/ return getTermByUuid(uuidDutchFlemish);/*@*/}
    public static final Language DYULA(){/*@*/ return getTermByUuid(uuidDyula);/*@*/}
    public static final Language DZONGKHA(){/*@*/ return getTermByUuid(uuidDzongkha);/*@*/}
    public static final Language EFIK(){/*@*/ return getTermByUuid(uuidEfik);/*@*/}
    public static final Language EGYPTIAN_ANCIENT(){/*@*/ return getTermByUuid(uuidEgyptianAncient);/*@*/}
    public static final Language EKAJUK(){/*@*/ return getTermByUuid(uuidEkajuk);/*@*/}
    public static final Language ELAMITE(){/*@*/ return getTermByUuid(uuidElamite);/*@*/}
    public static final Language ENGLISH_MIDDLE(){/*@*/ return getTermByUuid(uuidEnglishMiddle);/*@*/}
    public static final Language ESPERANTO(){/*@*/ return getTermByUuid(uuidEsperanto);/*@*/}
    public static final Language ESTONIAN(){/*@*/ return getTermByUuid(uuidEstonian);/*@*/}
    public static final Language EWE(){/*@*/ return getTermByUuid(uuidEwe);/*@*/}
    public static final Language EWONDO(){/*@*/ return getTermByUuid(uuidEwondo);/*@*/}
    public static final Language FANG(){/*@*/ return getTermByUuid(uuidFang);/*@*/}
    public static final Language FAROESE(){/*@*/ return getTermByUuid(uuidFaroese);/*@*/}
    public static final Language FANTI(){/*@*/ return getTermByUuid(uuidFanti);/*@*/}
    public static final Language FIJIAN(){/*@*/ return getTermByUuid(uuidFijian);/*@*/}
    public static final Language FILIPINO(){/*@*/ return getTermByUuid(uuidFilipinoPilipino);/*@*/}
    public static final Language FINNISH(){/*@*/ return getTermByUuid(uuidFinnish);/*@*/}
    public static final Language FINNO_UGRIAN_OTHER(){/*@*/ return getTermByUuid(uuidFinnoUgrian_Other);/*@*/}
    public static final Language FON(){/*@*/ return getTermByUuid(uuidFon);/*@*/}
    public static final Language FRENCH(){/*@*/ return getTermByUuid(uuidFrench);/*@*/}
    public static final Language FRENCH_MIDDLE(){/*@*/ return getTermByUuid(uuidFrenchMiddle);/*@*/}
    public static final Language FRENCH_OLD(){/*@*/ return getTermByUuid(uuidFrenchOld);/*@*/}
    public static final Language NORTHERN_FRISIAN(){/*@*/ return getTermByUuid(uuidNorthernFrisian);/*@*/}
    public static final Language EASTERN_FRISIAN(){/*@*/ return getTermByUuid(uuidEasternFrisian);/*@*/}
    public static final Language WESTERN_FRISIAN(){/*@*/ return getTermByUuid(uuidWesternFrisian);/*@*/}
    public static final Language FULAH(){/*@*/ return getTermByUuid(uuidFulah);/*@*/}
    public static final Language FRIULIAN(){/*@*/ return getTermByUuid(uuidFriulian);/*@*/}
    public static final Language GA(){/*@*/ return getTermByUuid(uuidGa);/*@*/}
    public static final Language GAYO(){/*@*/ return getTermByUuid(uuidGayo);/*@*/}
    public static final Language GBAYA(){/*@*/ return getTermByUuid(uuidGbaya);/*@*/}
    public static final Language GERMANIC_OTHER(){/*@*/ return getTermByUuid(uuidGermanic_Other);/*@*/}
    public static final Language GEORGIAN(){/*@*/ return getTermByUuid(uuidGeorgian);/*@*/}
    public static final Language GERMAN(){/*@*/ return getTermByUuid(uuidGerman);/*@*/}
    public static final Language GEEZ(){/*@*/ return getTermByUuid(uuidGeez);/*@*/}
    public static final Language GILBERTESE(){/*@*/ return getTermByUuid(uuidGilbertese);/*@*/}
    public static final Language GAELIC_SCOTTISH_GAELIC(){/*@*/ return getTermByUuid(uuidGaelic_ScottishGaelic);/*@*/}
    public static final Language IRISH(){/*@*/ return getTermByUuid(uuidIrish);/*@*/}
    public static final Language GALICIAN(){/*@*/ return getTermByUuid(uuidGalician);/*@*/}
    public static final Language MANX(){/*@*/ return getTermByUuid(uuidManx);/*@*/}
    public static final Language GERMAN_MIDDLE_HIGH(){/*@*/ return getTermByUuid(uuidGermanMiddleHigh);/*@*/}
    public static final Language GERMAN_OLD_HIGH(){/*@*/ return getTermByUuid(uuidGermanOldHigh);/*@*/}
    public static final Language GONDI(){/*@*/ return getTermByUuid(uuidGondi);/*@*/}
    public static final Language GORONTALO(){/*@*/ return getTermByUuid(uuidGorontalo);/*@*/}
    public static final Language GOTHIC(){/*@*/ return getTermByUuid(uuidGothic);/*@*/}
    public static final Language GREBO(){/*@*/ return getTermByUuid(uuidGrebo);/*@*/}
    public static final Language GREEK_ANCIENT(){/*@*/ return getTermByUuid(uuidGreekAncient);/*@*/}
    public static final Language GREEK_MODERN(){/*@*/ return getTermByUuid(uuidGreekModern);/*@*/}
    public static final Language GUARANI(){/*@*/ return getTermByUuid(uuidGuarani);/*@*/}
    public static final Language SWISS_GERMAN_ALEMANNIC(){/*@*/ return getTermByUuid(uuidSwissGerman_Alemannic);/*@*/}
    public static final Language GUJARATI(){/*@*/ return getTermByUuid(uuidGujarati);/*@*/}
    public static final Language GWICHIN(){/*@*/ return getTermByUuid(uuidGwichin);/*@*/}
    public static final Language HAIDA(){/*@*/ return getTermByUuid(uuidHaida);/*@*/}
    public static final Language HAITIAN_HAITIAN_CREOLE(){/*@*/ return getTermByUuid(uuidHaitian_HaitianCreole);/*@*/}
    public static final Language HAUSA(){/*@*/ return getTermByUuid(uuidHausa);/*@*/}
    public static final Language HAWAIIAN(){/*@*/ return getTermByUuid(uuidHawaiian);/*@*/}
    public static final Language HEBREW(){/*@*/ return getTermByUuid(uuidHebrew);/*@*/}
    public static final Language HERERO(){/*@*/ return getTermByUuid(uuidHerero);/*@*/}
    public static final Language HILIGAYNON(){/*@*/ return getTermByUuid(uuidHiligaynon);/*@*/}
    public static final Language HIMACHALI(){/*@*/ return getTermByUuid(uuidHimachali);/*@*/}
    public static final Language HINDI(){/*@*/ return getTermByUuid(uuidHindi);/*@*/}
    public static final Language HITTITE(){/*@*/ return getTermByUuid(uuidHittite);/*@*/}
    public static final Language HMONG(){/*@*/ return getTermByUuid(uuidHmong);/*@*/}
    public static final Language HIRI_MOTU(){/*@*/ return getTermByUuid(uuidHiriMotu);/*@*/}
    public static final Language UPPER_SORBIAN(){/*@*/ return getTermByUuid(uuidUpperSorbian);/*@*/}
    public static final Language HUNGARIAN(){/*@*/ return getTermByUuid(uuidHungarian);/*@*/}
    public static final Language HUPA(){/*@*/ return getTermByUuid(uuidHupa);/*@*/}
    public static final Language IBAN(){/*@*/ return getTermByUuid(uuidIban);/*@*/}
    public static final Language IGBO(){/*@*/ return getTermByUuid(uuidIgbo);/*@*/}
    public static final Language ICELANDIC(){/*@*/ return getTermByUuid(uuidIcelandic);/*@*/}
    public static final Language IDO(){/*@*/ return getTermByUuid(uuidIdo);/*@*/}
    public static final Language SICHUANYI_NUOSU(){/*@*/ return getTermByUuid(uuidSichuanYi_Nuosu);/*@*/}
    public static final Language IJOS(){/*@*/ return getTermByUuid(uuidIjos);/*@*/}
    public static final Language INUKTITUT(){/*@*/ return getTermByUuid(uuidInuktitut);/*@*/}
    public static final Language INTERLINGUE_OCCIDENTAL(){/*@*/ return getTermByUuid(uuidInterlingue_Occidental);/*@*/}
    public static final Language ILOKO(){/*@*/ return getTermByUuid(uuidIloko);/*@*/}
    public static final Language INTERLINGUA(){/*@*/ return getTermByUuid(uuidInterlingua);/*@*/}
    public static final Language INDIC_OTHER(){/*@*/ return getTermByUuid(uuidIndic_Other);/*@*/}
    public static final Language INDONESIAN(){/*@*/ return getTermByUuid(uuidIndonesian);/*@*/}
    public static final Language INDO_EUROPEAN_OTHER(){/*@*/ return getTermByUuid(uuidIndoEuropean_Other);/*@*/}
    public static final Language INGUSH(){/*@*/ return getTermByUuid(uuidIngush);/*@*/}
    public static final Language INUPIAQ(){/*@*/ return getTermByUuid(uuidInupiaq);/*@*/}
    public static final Language IRANIAN_OTHER(){/*@*/ return getTermByUuid(uuidIranian_Other);/*@*/}
    public static final Language IROQUOIANS(){/*@*/ return getTermByUuid(uuidIroquoians);/*@*/}
    public static final Language ITALIAN(){/*@*/ return getTermByUuid(uuidItalian);/*@*/}
    public static final Language JAVANESE(){/*@*/ return getTermByUuid(uuidJavanese);/*@*/}
    public static final Language LOJBAN(){/*@*/ return getTermByUuid(uuidLojban);/*@*/}
    public static final Language JAPANESE(){/*@*/ return getTermByUuid(uuidJapanese);/*@*/}
    public static final Language JUDEOPERSIAN(){/*@*/ return getTermByUuid(uuidJudeoPersian);/*@*/}
    public static final Language JUDEO_ARABIC(){/*@*/ return getTermByUuid(uuidJudeoArabic);/*@*/}
    public static final Language KARA_KALPAK(){/*@*/ return getTermByUuid(uuidKaraKalpak);/*@*/}
    public static final Language KABYLE(){/*@*/ return getTermByUuid(uuidKabyle);/*@*/}
    public static final Language KACHIN_JINGPHO(){/*@*/ return getTermByUuid(uuidKachin_Jingpho);/*@*/}
    public static final Language KALAALLISUT_GREENLANDIC(){/*@*/ return getTermByUuid(uuidKalaallisut_Greenlandic);/*@*/}
    public static final Language KAMBA(){/*@*/ return getTermByUuid(uuidKamba);/*@*/}
    public static final Language KANNADA(){/*@*/ return getTermByUuid(uuidKannada);/*@*/}
    public static final Language KARENS(){/*@*/ return getTermByUuid(uuidKarens);/*@*/}
    public static final Language KASHMIRI(){/*@*/ return getTermByUuid(uuidKashmiri);/*@*/}
    public static final Language KANURI(){/*@*/ return getTermByUuid(uuidKanuri);/*@*/}
    public static final Language KAWI(){/*@*/ return getTermByUuid(uuidKawi);/*@*/}
    public static final Language KAZAKH(){/*@*/ return getTermByUuid(uuidKazakh);/*@*/}
    public static final Language KABARDIAN(){/*@*/ return getTermByUuid(uuidKabardian);/*@*/}
    public static final Language KHASI(){/*@*/ return getTermByUuid(uuidKhasi);/*@*/}
    public static final Language KHOISAN_OTHER(){/*@*/ return getTermByUuid(uuidKhoisan_Other);/*@*/}
    public static final Language CENTRAL_KHMER(){/*@*/ return getTermByUuid(uuidCentralKhmer);/*@*/}
    public static final Language KHOTANESE(){/*@*/ return getTermByUuid(uuidKhotanese);/*@*/}
    public static final Language KIKUYU_GIKUYU(){/*@*/ return getTermByUuid(uuidKikuyu_Gikuyu);/*@*/}
    public static final Language KINYARWANDA(){/*@*/ return getTermByUuid(uuidKinyarwanda);/*@*/}
    public static final Language KIRGHIZ_KYRGYZ(){/*@*/ return getTermByUuid(uuidKirghiz_Kyrgyz);/*@*/}
    public static final Language KIMBUNDU(){/*@*/ return getTermByUuid(uuidKimbundu);/*@*/}
    public static final Language KONKANI(){/*@*/ return getTermByUuid(uuidKonkani);/*@*/}
    public static final Language KOMI(){/*@*/ return getTermByUuid(uuidKomi);/*@*/}
    public static final Language KONGO(){/*@*/ return getTermByUuid(uuidKongo);/*@*/}
    public static final Language KOREAN(){/*@*/ return getTermByUuid(uuidKorean);/*@*/}
    public static final Language KOSRAEAN(){/*@*/ return getTermByUuid(uuidKosraean);/*@*/}
    public static final Language KPELLE(){/*@*/ return getTermByUuid(uuidKpelle);/*@*/}
    public static final Language KARACHAY_BALKAR(){/*@*/ return getTermByUuid(uuidKarachayBalkar);/*@*/}
    public static final Language KARELIAN(){/*@*/ return getTermByUuid(uuidKarelian);/*@*/}
    public static final Language KRUS(){/*@*/ return getTermByUuid(uuidKrus);/*@*/}
    public static final Language KURUKH(){/*@*/ return getTermByUuid(uuidKurukh);/*@*/}
    public static final Language KUANYAMA_KWANYAMA(){/*@*/ return getTermByUuid(uuidKuanyama_Kwanyama);/*@*/}
    public static final Language KUMYK(){/*@*/ return getTermByUuid(uuidKumyk);/*@*/}
    public static final Language KURDISH(){/*@*/ return getTermByUuid(uuidKurdish);/*@*/}
    public static final Language KUTENAI(){/*@*/ return getTermByUuid(uuidKutenai);/*@*/}
    public static final Language LADINO(){/*@*/ return getTermByUuid(uuidLadino);/*@*/}
    public static final Language LAHNDA(){/*@*/ return getTermByUuid(uuidLahnda);/*@*/}
    public static final Language LAMBA(){/*@*/ return getTermByUuid(uuidLamba);/*@*/}
    public static final Language LAO(){/*@*/ return getTermByUuid(uuidLao);/*@*/}
    public static final Language LATIN(){/*@*/ return getTermByUuid(uuidLatin);/*@*/}
    public static final Language LATVIAN(){/*@*/ return getTermByUuid(uuidLatvian);/*@*/}
    public static final Language LEZGHIAN(){/*@*/ return getTermByUuid(uuidLezghian);/*@*/}
    public static final Language LIMBURGAN(){/*@*/ return getTermByUuid(uuidLimburgan_Limburger_Limburgish);/*@*/}
    public static final Language LINGALA(){/*@*/ return getTermByUuid(uuidLingala);/*@*/}
    public static final Language LITHUANIAN(){/*@*/ return getTermByUuid(uuidLithuanian);/*@*/}
    public static final Language MONGO(){/*@*/ return getTermByUuid(uuidMongo);/*@*/}
    public static final Language LOZI(){/*@*/ return getTermByUuid(uuidLozi);/*@*/}
    public static final Language LUXEMBOURGISH_LETZEBURGESCH(){/*@*/ return getTermByUuid(uuidLuxembourgish_Letzeburgesch);/*@*/}
    public static final Language LUBA_LULUA(){/*@*/ return getTermByUuid(uuidLubaLulua);/*@*/}
    public static final Language LUBA_KATANGA(){/*@*/ return getTermByUuid(uuidLubaKatanga);/*@*/}
    public static final Language GANDA(){/*@*/ return getTermByUuid(uuidGanda);/*@*/}
    public static final Language LUISENO(){/*@*/ return getTermByUuid(uuidLuiseno);/*@*/}
    public static final Language LUNDA(){/*@*/ return getTermByUuid(uuidLunda);/*@*/}
    public static final Language LUO(){/*@*/ return getTermByUuid(uuidLuo);/*@*/}
    public static final Language LUSHAI(){/*@*/ return getTermByUuid(uuidLushai);/*@*/}
    public static final Language MACEDONIAN(){/*@*/ return getTermByUuid(uuidMacedonian);/*@*/}
    public static final Language MADURESE(){/*@*/ return getTermByUuid(uuidMadurese);/*@*/}
    public static final Language MAGAHI(){/*@*/ return getTermByUuid(uuidMagahi);/*@*/}
    public static final Language MARSHALLESE(){/*@*/ return getTermByUuid(uuidMarshallese);/*@*/}
    public static final Language MAITHILI(){/*@*/ return getTermByUuid(uuidMaithili);/*@*/}
    public static final Language MAKASAR(){/*@*/ return getTermByUuid(uuidMakasar);/*@*/}
    public static final Language MALAYALAM(){/*@*/ return getTermByUuid(uuidMalayalam);/*@*/}
    public static final Language MANDINGO(){/*@*/ return getTermByUuid(uuidMandingo);/*@*/}
    public static final Language MAORI(){/*@*/ return getTermByUuid(uuidMaori);/*@*/}
    public static final Language AUSTRONESIAN_OTHER(){/*@*/ return getTermByUuid(uuidAustronesian_Other);/*@*/}
    public static final Language MARATHI(){/*@*/ return getTermByUuid(uuidMarathi);/*@*/}
    public static final Language MASAI(){/*@*/ return getTermByUuid(uuidMasai);/*@*/}
    public static final Language MALAY(){/*@*/ return getTermByUuid(uuidMalay);/*@*/}
    public static final Language MOKSHA(){/*@*/ return getTermByUuid(uuidMoksha);/*@*/}
    public static final Language MANDAR(){/*@*/ return getTermByUuid(uuidMandar);/*@*/}
    public static final Language MENDE(){/*@*/ return getTermByUuid(uuidMende);/*@*/}
    public static final Language IRISH_MIDDLE(){/*@*/ return getTermByUuid(uuidIrishMiddle);/*@*/}
    public static final Language MIKMAQ_MICMAC(){/*@*/ return getTermByUuid(uuidMikmaq_Micmac);/*@*/}
    public static final Language MINANGKABAU(){/*@*/ return getTermByUuid(uuidMinangkabau);/*@*/}
    public static final Language UNCODEDS(){/*@*/ return getTermByUuid(uuidUncodeds);/*@*/}
    public static final Language MONKHMER_OTHER(){/*@*/ return getTermByUuid(uuidMonKhmer_Other);/*@*/}
    public static final Language MALAGASY(){/*@*/ return getTermByUuid(uuidMalagasy);/*@*/}
    public static final Language MALTESE(){/*@*/ return getTermByUuid(uuidMaltese);/*@*/}
    public static final Language MANCHU(){/*@*/ return getTermByUuid(uuidManchu);/*@*/}
    public static final Language MANIPURI(){/*@*/ return getTermByUuid(uuidManipuri);/*@*/}
    public static final Language MANOBOS(){/*@*/ return getTermByUuid(uuidManobos);/*@*/}
    public static final Language MOHAWK(){/*@*/ return getTermByUuid(uuidMohawk);/*@*/}
    public static final Language MOLDAVIAN(){/*@*/ return getTermByUuid(uuidMoldavian);/*@*/}
    public static final Language MONGOLIAN(){/*@*/ return getTermByUuid(uuidMongolian);/*@*/}
    public static final Language MOSSI(){/*@*/ return getTermByUuid(uuidMossi);/*@*/}
    public static final Language MULTIPLES(){/*@*/ return getTermByUuid(uuidMultiples);/*@*/}
    public static final Language MUNDAS(){/*@*/ return getTermByUuid(uuidMundas);/*@*/}
    public static final Language CREEK(){/*@*/ return getTermByUuid(uuidCreek);/*@*/}
    public static final Language MIRANDESE(){/*@*/ return getTermByUuid(uuidMirandese);/*@*/}
    public static final Language MARWARI(){/*@*/ return getTermByUuid(uuidMarwari);/*@*/}
    public static final Language MAYANS(){/*@*/ return getTermByUuid(uuidMayans);/*@*/}
    public static final Language ERZYA(){/*@*/ return getTermByUuid(uuidErzya);/*@*/}
    public static final Language NAHUATLS(){/*@*/ return getTermByUuid(uuidNahuatls);/*@*/}
    public static final Language NORTH_AMERICAN_INDIAN(){/*@*/ return getTermByUuid(uuidNorthAmericanIndian);/*@*/}
    public static final Language NEAPOLITAN(){/*@*/ return getTermByUuid(uuidNeapolitan);/*@*/}
    public static final Language NAURU(){/*@*/ return getTermByUuid(uuidNauru);/*@*/}
    public static final Language NAVAJO_NAVAHO(){/*@*/ return getTermByUuid(uuidNavajoNavaho);/*@*/}
    public static final Language NDEBELE_SOUTH(){/*@*/ return getTermByUuid(uuidNdebeleSouth);/*@*/}
    public static final Language NDEBELE_NORTH(){/*@*/ return getTermByUuid(uuidNdebeleNorth);/*@*/}
    public static final Language NDONGA(){/*@*/ return getTermByUuid(uuidNdonga);/*@*/}
    public static final Language LOWGERMAN_LOWSAXON(){/*@*/ return getTermByUuid(uuidLowGerman_LowSaxon);/*@*/}
    public static final Language NEPALI(){/*@*/ return getTermByUuid(uuidNepali);/*@*/}
    public static final Language NEPALBHASA_NEWARI(){/*@*/ return getTermByUuid(uuidNepalBhasa_Newari);/*@*/}
    public static final Language NIAS(){/*@*/ return getTermByUuid(uuidNias);/*@*/}
    public static final Language NIGER_KORDOFANIAN_OTHER(){/*@*/ return getTermByUuid(uuidNigerKordofanian_Other);/*@*/}
    public static final Language NIUEAN(){/*@*/ return getTermByUuid(uuidNiuean);/*@*/}
    public static final Language NORWEGIAN_NYNORSK(){/*@*/ return getTermByUuid(uuidNorwegianNynorsk);/*@*/}
    public static final Language NORWEGIAN_BOKMOL(){/*@*/ return getTermByUuid(uuidNorwegianBokmol);/*@*/}
    public static final Language NOGAI(){/*@*/ return getTermByUuid(uuidNogai);/*@*/}
    public static final Language NORSE_OLD(){/*@*/ return getTermByUuid(uuidNorseOld);/*@*/}
    public static final Language NORWEGIAN(){/*@*/ return getTermByUuid(uuidNorwegian);/*@*/}
    public static final Language NKO(){/*@*/ return getTermByUuid(uuidNKo);/*@*/}
    public static final Language PEDI_SEPEDI_NORTHERN_SOTHO(){/*@*/ return getTermByUuid(uuidPedi_Sepedi_NorthernSotho);/*@*/}
    public static final Language NUBIANS(){/*@*/ return getTermByUuid(uuidNubians);/*@*/}
    public static final Language CLASSICAL_NEWARI_OLD_NEWARI_CLASSICAL_NEPAL_BHASA(){/*@*/ return getTermByUuid(uuidClassicalNewari_OldNewari_ClassicalNepal_Bhasa);/*@*/}
    public static final Language CHICHEWA_CHEWA_NYANJA(){/*@*/ return getTermByUuid(uuidChichewa_Chewa_Nyanja);/*@*/}
    public static final Language NYAMWEZI(){/*@*/ return getTermByUuid(uuidNyamwezi);/*@*/}
    public static final Language NYANKOLE(){/*@*/ return getTermByUuid(uuidNyankole);/*@*/}
    public static final Language NYORO(){/*@*/ return getTermByUuid(uuidNyoro);/*@*/}
    public static final Language NZIMA(){/*@*/ return getTermByUuid(uuidNzima);/*@*/}
    public static final Language OCCITAN_PROVENAL(){/*@*/ return getTermByUuid(uuidOccitan_Provenal);/*@*/}
    public static final Language OJIBWA(){/*@*/ return getTermByUuid(uuidOjibwa);/*@*/}
    public static final Language ORIYA(){/*@*/ return getTermByUuid(uuidOriya);/*@*/}
    public static final Language OROMO(){/*@*/ return getTermByUuid(uuidOromo);/*@*/}
    public static final Language OSAGE(){/*@*/ return getTermByUuid(uuidOsage);/*@*/}
    public static final Language OSSETIAN_OSSETIC(){/*@*/ return getTermByUuid(uuidOssetian_Ossetic);/*@*/}
    public static final Language TURKISH_OTTOMAN(){/*@*/ return getTermByUuid(uuidTurkish_Ottoman);/*@*/}
    public static final Language OTOMIANS(){/*@*/ return getTermByUuid(uuidOtomians);/*@*/}
    public static final Language PAPUAN_OTHER(){/*@*/ return getTermByUuid(uuidPapuan_Other);/*@*/}
    public static final Language PANGASINAN(){/*@*/ return getTermByUuid(uuidPangasinan);/*@*/}
    public static final Language PAHLAVI(){/*@*/ return getTermByUuid(uuidPahlavi);/*@*/}
    public static final Language PAMPANGA_KAPAMPANGAN(){/*@*/ return getTermByUuid(uuidPampanga_Kapampangan);/*@*/}
    public static final Language PANJABI_PUNJABI(){/*@*/ return getTermByUuid(uuidPanjabi_Punjabi);/*@*/}
    public static final Language PAPIAMENTO(){/*@*/ return getTermByUuid(uuidPapiamento);/*@*/}
    public static final Language PALAUAN(){/*@*/ return getTermByUuid(uuidPalauan);/*@*/}
    public static final Language PERSIAN_OLD(){/*@*/ return getTermByUuid(uuidPersian_Old);/*@*/}
    public static final Language PERSIAN(){/*@*/ return getTermByUuid(uuidPersian);/*@*/}
    public static final Language PHILIPPINE_OTHER(){/*@*/ return getTermByUuid(uuidPhilippine_Other);/*@*/}
    public static final Language PHOENICIAN(){/*@*/ return getTermByUuid(uuidPhoenician);/*@*/}
    public static final Language PALI(){/*@*/ return getTermByUuid(uuidPali);/*@*/}
    public static final Language POLISH(){/*@*/ return getTermByUuid(uuidPolish);/*@*/}
    public static final Language POHNPEIAN(){/*@*/ return getTermByUuid(uuidPohnpeian);/*@*/}
    public static final Language PORTUGUESE(){/*@*/ return getTermByUuid(uuidPortuguese);/*@*/}
    public static final Language PRAKRITS(){/*@*/ return getTermByUuid(uuidPrakrits);/*@*/}
    public static final Language PROVENAL_OLD(){/*@*/ return getTermByUuid(uuidProvenalOld);/*@*/}
    public static final Language PUSHTO(){/*@*/ return getTermByUuid(uuidPushto);/*@*/}
    public static final Language QUECHUA(){/*@*/ return getTermByUuid(uuidQuechua);/*@*/}
    public static final Language RAJASTHANI(){/*@*/ return getTermByUuid(uuidRajasthani);/*@*/}
    public static final Language RAPANUI(){/*@*/ return getTermByUuid(uuidRapanui);/*@*/}
    public static final Language RAROTONGAN_COOK_ISLANDS_MAORI(){/*@*/ return getTermByUuid(uuidRarotongan_CookIslands_Maori);/*@*/}
    public static final Language ROMANCE_OTHER(){/*@*/ return getTermByUuid(uuidRomance_Other);/*@*/}
    public static final Language ROMANSH(){/*@*/ return getTermByUuid(uuidRomansh);/*@*/}
    public static final Language ROMANY(){/*@*/ return getTermByUuid(uuidRomany);/*@*/}
    public static final Language ROMANIAN(){/*@*/ return getTermByUuid(uuidRomanian);/*@*/}
    public static final Language RUNDI(){/*@*/ return getTermByUuid(uuidRundi);/*@*/}
    public static final Language AROMANIAN_ARUMANIAN_MACEDOROMANIAN(){/*@*/ return getTermByUuid(uuidAromanian_Arumanian_MacedoRomanian);/*@*/}
    public static final Language RUSSIAN(){/*@*/ return getTermByUuid(uuidRussian);/*@*/}
    public static final Language SANDAWE(){/*@*/ return getTermByUuid(uuidSandawe);/*@*/}
    public static final Language SANGO(){/*@*/ return getTermByUuid(uuidSango);/*@*/}
    public static final Language YAKUT(){/*@*/ return getTermByUuid(uuidYakut);/*@*/}
    public static final Language SOUTH_AMERICAN_INDIAN_OTHER(){/*@*/ return getTermByUuid(uuidSouthAmericanIndian_Other);/*@*/}
    public static final Language SALISHANS(){/*@*/ return getTermByUuid(uuidSalishans);/*@*/}
    public static final Language SAMARITAN_ARAMAIC(){/*@*/ return getTermByUuid(uuidSamaritanAramaic);/*@*/}
    public static final Language SANSKRIT(){/*@*/ return getTermByUuid(uuidSanskrit);/*@*/}
    public static final Language SASAK(){/*@*/ return getTermByUuid(uuidSasak);/*@*/}
    public static final Language SANTALI(){/*@*/ return getTermByUuid(uuidSantali);/*@*/}
    public static final Language SERBIAN(){/*@*/ return getTermByUuid(uuidSerbian);/*@*/}
    public static final Language SICILIAN(){/*@*/ return getTermByUuid(uuidSicilian);/*@*/}
    public static final Language SCOTS(){/*@*/ return getTermByUuid(uuidScots);/*@*/}
    public static final Language CROATIAN(){/*@*/ return getTermByUuid(uuidCroatian);/*@*/}
    public static final Language SELKUP(){/*@*/ return getTermByUuid(uuidSelkup);/*@*/}
    public static final Language SEMITIC_OTHER(){/*@*/ return getTermByUuid(uuidSemitic_Other);/*@*/}
    public static final Language IRISH_OLD(){/*@*/ return getTermByUuid(uuidIrishOld);/*@*/}
    public static final Language SIGN_LANGUAGES(){/*@*/ return getTermByUuid(uuidSignLanguages);/*@*/}
    public static final Language SHAN(){/*@*/ return getTermByUuid(uuidShan);/*@*/}
    public static final Language SIDAMO(){/*@*/ return getTermByUuid(uuidSidamo);/*@*/}
    public static final Language SINHALA_SINHALESE(){/*@*/ return getTermByUuid(uuidSinhala_Sinhalese);/*@*/}
    public static final Language SIOUANS(){/*@*/ return getTermByUuid(uuidSiouans);/*@*/}
    public static final Language SINO_TIBETAN_OTHER(){/*@*/ return getTermByUuid(uuidSinoTibetan_Other);/*@*/}
    public static final Language SLAVIC_OTHER(){/*@*/ return getTermByUuid(uuidSlavic_Other);/*@*/}
    public static final Language SLOVAK(){/*@*/ return getTermByUuid(uuidSlovak);/*@*/}
    public static final Language SLOVENIAN(){/*@*/ return getTermByUuid(uuidSlovenian);/*@*/}
    public static final Language SOUTHERN_SAMI(){/*@*/ return getTermByUuid(uuidSouthernSami);/*@*/}
    public static final Language NORTHERN_SAMI(){/*@*/ return getTermByUuid(uuidNorthernSami);/*@*/}
    public static final Language SAMIS_OTHER(){/*@*/ return getTermByUuid(uuidSamis_Other);/*@*/}
    public static final Language LULE_SAMI(){/*@*/ return getTermByUuid(uuidLuleSami);/*@*/}
    public static final Language INARI_SAMI(){/*@*/ return getTermByUuid(uuidInariSami);/*@*/}
    public static final Language SAMOAN(){/*@*/ return getTermByUuid(uuidSamoan);/*@*/}
    public static final Language SKOLT_SAMI(){/*@*/ return getTermByUuid(uuidSkoltSami);/*@*/}
    public static final Language SHONA(){/*@*/ return getTermByUuid(uuidShona);/*@*/}
    public static final Language SINDHI(){/*@*/ return getTermByUuid(uuidSindhi);/*@*/}
    public static final Language SONINKE(){/*@*/ return getTermByUuid(uuidSoninke);/*@*/}
    public static final Language SOGDIAN(){/*@*/ return getTermByUuid(uuidSogdian);/*@*/}
    public static final Language SOMALI(){/*@*/ return getTermByUuid(uuidSomali);/*@*/}
    public static final Language SONGHAIS(){/*@*/ return getTermByUuid(uuidSonghais);/*@*/}
    public static final Language SOTHO_SOUTHERN(){/*@*/ return getTermByUuid(uuidSothoSouthern);/*@*/}
    public static final Language SPANISH_CASTILIAN(){/*@*/ return getTermByUuid(uuidSpanish_Castilian);/*@*/}
    public static final Language SARDINIAN(){/*@*/ return getTermByUuid(uuidSardinian);/*@*/}
    public static final Language SRANAN_TONGO(){/*@*/ return getTermByUuid(uuidSrananTongo);/*@*/}
    public static final Language SERER(){/*@*/ return getTermByUuid(uuidSerer);/*@*/}
    public static final Language NILO_SAHARAN_OTHER(){/*@*/ return getTermByUuid(uuidNiloSaharan_Other);/*@*/}
    public static final Language SWATI(){/*@*/ return getTermByUuid(uuidSwati);/*@*/}
    public static final Language SUKUMA(){/*@*/ return getTermByUuid(uuidSukuma);/*@*/}
    public static final Language SUNDANESE(){/*@*/ return getTermByUuid(uuidSundanese);/*@*/}
    public static final Language SUSU(){/*@*/ return getTermByUuid(uuidSusu);/*@*/}
    public static final Language SUMERIAN(){/*@*/ return getTermByUuid(uuidSumerian);/*@*/}
    public static final Language SWAHILI(){/*@*/ return getTermByUuid(uuidSwahili);/*@*/}
    public static final Language SWEDISH(){/*@*/ return getTermByUuid(uuidSwedish);/*@*/}
    public static final Language CLASSICAL_SYRIAC(){/*@*/ return getTermByUuid(uuidClassicalSyriac);/*@*/}
    public static final Language SYRIAC(){/*@*/ return getTermByUuid(uuidSyriac);/*@*/}
    public static final Language TAHITIAN(){/*@*/ return getTermByUuid(uuidTahitian);/*@*/}
    public static final Language TAI_OTHER(){/*@*/ return getTermByUuid(uuidTai_Other);/*@*/}
    public static final Language TAMIL(){/*@*/ return getTermByUuid(uuidTamil);/*@*/}
    public static final Language TATAR(){/*@*/ return getTermByUuid(uuidTatar);/*@*/}
    public static final Language TELUGU(){/*@*/ return getTermByUuid(uuidTelugu);/*@*/}
    public static final Language TIMNE(){/*@*/ return getTermByUuid(uuidTimne);/*@*/}
    public static final Language TERENO(){/*@*/ return getTermByUuid(uuidTereno);/*@*/}
    public static final Language TETUM(){/*@*/ return getTermByUuid(uuidTetum);/*@*/}
    public static final Language TAJIK(){/*@*/ return getTermByUuid(uuidTajik);/*@*/}
    public static final Language TAGALOG(){/*@*/ return getTermByUuid(uuidTagalog);/*@*/}
    public static final Language THAI(){/*@*/ return getTermByUuid(uuidThai);/*@*/}
    public static final Language TIBETAN(){/*@*/ return getTermByUuid(uuidTibetan);/*@*/}
    public static final Language TIGRE(){/*@*/ return getTermByUuid(uuidTigre);/*@*/}
    public static final Language TIGRINYA(){/*@*/ return getTermByUuid(uuidTigrinya);/*@*/}
    public static final Language TIV(){/*@*/ return getTermByUuid(uuidTiv);/*@*/}
    public static final Language TOKELAU(){/*@*/ return getTermByUuid(uuidTokelau);/*@*/}
    public static final Language KLINGON_TLHINGANHOL(){/*@*/ return getTermByUuid(uuidKlingon_TlhInganHol);/*@*/}
    public static final Language TLINGIT(){/*@*/ return getTermByUuid(uuidTlingit);/*@*/}
    public static final Language TAMASHEK(){/*@*/ return getTermByUuid(uuidTamashek);/*@*/}
    public static final Language TONGA_NYASA(){/*@*/ return getTermByUuid(uuidTongaNyasa);/*@*/}
    public static final Language TONGA_TONGA_ISLANDS(){/*@*/ return getTermByUuid(uuidTongaTongaIslands);/*@*/}
    public static final Language TOK_PISIN(){/*@*/ return getTermByUuid(uuidTokPisin);/*@*/}
    public static final Language TSIMSHIAN(){/*@*/ return getTermByUuid(uuidTsimshian);/*@*/}
    public static final Language TSWANA(){/*@*/ return getTermByUuid(uuidTswana);/*@*/}
    public static final Language TSONGA(){/*@*/ return getTermByUuid(uuidTsonga);/*@*/}
    public static final Language TURKMEN(){/*@*/ return getTermByUuid(uuidTurkmen);/*@*/}
    public static final Language TUMBUKA(){/*@*/ return getTermByUuid(uuidTumbuka);/*@*/}
    public static final Language TUPIS(){/*@*/ return getTermByUuid(uuidTupis);/*@*/}
    public static final Language TURKISH(){/*@*/ return getTermByUuid(uuidTurkish);/*@*/}
    public static final Language ALTAIC_OTHER(){/*@*/ return getTermByUuid(uuidAltaic_Other);/*@*/}
    public static final Language TUVALU(){/*@*/ return getTermByUuid(uuidTuvalu);/*@*/}
    public static final Language TWI(){/*@*/ return getTermByUuid(uuidTwi);/*@*/}
    public static final Language TUVINIAN(){/*@*/ return getTermByUuid(uuidTuvinian);/*@*/}
    public static final Language UDMURT(){/*@*/ return getTermByUuid(uuidUdmurt);/*@*/}
    public static final Language UGARITIC(){/*@*/ return getTermByUuid(uuidUgaritic);/*@*/}
    public static final Language UIGHUR_UYGHUR(){/*@*/ return getTermByUuid(uuidUighur_Uyghur);/*@*/}
    public static final Language UKRAINIAN(){/*@*/ return getTermByUuid(uuidUkrainian);/*@*/}
    public static final Language UMBUNDU(){/*@*/ return getTermByUuid(uuidUmbundu);/*@*/}
    public static final Language UNDETERMINED(){/*@*/ return getTermByUuid(uuidUndetermined);/*@*/}
    public static final Language URDU(){/*@*/ return getTermByUuid(uuidUrdu);/*@*/}
    public static final Language UZBEK(){/*@*/ return getTermByUuid(uuidUzbek);/*@*/}
    public static final Language VAI(){/*@*/ return getTermByUuid(uuidVai);/*@*/}
    public static final Language VENDA(){/*@*/ return getTermByUuid(uuidVenda);/*@*/}
    public static final Language VIETNAMESE(){/*@*/ return getTermByUuid(uuidVietnamese);/*@*/}
    public static final Language VOLAPEK(){/*@*/ return getTermByUuid(uuidVolapek);/*@*/}
    public static final Language VOTIC(){/*@*/ return getTermByUuid(uuidVotic);/*@*/}
    public static final Language WAKASHANS(){/*@*/ return getTermByUuid(uuidWakashans);/*@*/}
    public static final Language WALAMO(){/*@*/ return getTermByUuid(uuidWalamo);/*@*/}
    public static final Language WARAY(){/*@*/ return getTermByUuid(uuidWaray);/*@*/}
    public static final Language WASHO(){/*@*/ return getTermByUuid(uuidWasho);/*@*/}
    public static final Language WELSH(){/*@*/ return getTermByUuid(uuidWelsh);/*@*/}
    public static final Language SORBIANS(){/*@*/ return getTermByUuid(uuidSorbians);/*@*/}
    public static final Language WALLOON(){/*@*/ return getTermByUuid(uuidWalloon);/*@*/}
    public static final Language WOLOF(){/*@*/ return getTermByUuid(uuidWolof);/*@*/}
    public static final Language KALMYK_OIRAT(){/*@*/ return getTermByUuid(uuidKalmyk_Oirat);/*@*/}
    public static final Language XHOSA(){/*@*/ return getTermByUuid(uuidXhosa);/*@*/}
    public static final Language YAO(){/*@*/ return getTermByUuid(uuidYao);/*@*/}
    public static final Language YAPESE(){/*@*/ return getTermByUuid(uuidYapese);/*@*/}
    public static final Language YIDDISH(){/*@*/ return getTermByUuid(uuidYiddish);/*@*/}
    public static final Language YORUBA(){/*@*/ return getTermByUuid(uuidYoruba);/*@*/}
    public static final Language YUPIKS(){/*@*/ return getTermByUuid(uuidYupiks);/*@*/}
    public static final Language ZAPOTEC(){/*@*/ return getTermByUuid(uuidZapotec);/*@*/}
    public static final Language BLIS_SYMBOLS(){/*@*/ return getTermByUuid(uuidBlissymbols_Blissymbolics_Bliss);/*@*/}
    public static final Language ZENAGA(){/*@*/ return getTermByUuid(uuidZenaga);/*@*/}
    public static final Language ZHUANG_CHUANG(){/*@*/ return getTermByUuid(uuidZhuang_Chuang);/*@*/}
    public static final Language ZANDES(){/*@*/ return getTermByUuid(uuidZandes);/*@*/}
    public static final Language ZULU(){/*@*/ return getTermByUuid(uuidZulu);/*@*/}
    public static final Language ZUNI(){/*@*/ return getTermByUuid(uuidZuni);/*@*/}
    public static final Language NO_LINGUISTIC_CONTENT(){/*@*/ return getTermByUuid(uuidNoLinguisticContent);/*@*/}
    public static final Language ZAZA_DIMILI_DIMLI_KIRDKI_KIRMANJKI_ZAZAKI(){/*@*/ return getTermByUuid(uuidZaza_Dimili_Dimli_Kirdki_Kirmanjki_Zazaki);/*@*/}
    public static final Language UNKNOWN_LANGUAGE() {/*@*/ return getTermByUuid(uuidUnknownLanguage);/*@*/}
    public static final Language ORIGINAL_LANGUAGE() {/*@*/ return getTermByUuid(uuidOriginalLanguage);/*@*/}

    private static Language defaultLanguage = null;  //is set in setDefaultTerms()
    private static Language csvLanguage = null;  //is set in setDefaultTerms()


    public static final Language DEFAULT(){
        return getDefaultLanguage();
    }

    public static final Language CSV_LANGUAGE(){
        return csvLanguage;
    }


    /**
     * Get the according iso639-1 alpha-2 language code
     * http://www.loc.gov/standards/iso639-2/
     *
     * @return the iso639 alpha-2 language code or null if not available
     */
    public String getIso639_1() {
        return iso639_1;
    }

    public void setIso639_1(String iso639_1) {
        if (iso639_1 != null){
            iso639_1 = iso639_1.trim();
            if(iso639_1.length() > 2){
                logger.warn("Iso639-1: "+iso639_1+" too long");
            }
            this.iso639_1 = iso639_1 == "" ? null : iso639_1;
        }
    }

    /**
     * Get the according iso639-2 alpha-3 language code
     * http://www.loc.gov/standards/iso639-2/
     *
     * @return the iso639 alpha-3 language code or null if not available
     */
    @Transient
    public String getIso639_2() {
        return getIdInVocabulary();
    }

//    public void setIso639_2(String iso639_2) {
//        if (iso639_2 != null){
//            iso639_2 = iso639_2.trim();
//            if(iso639_2.length() > 3 ){
//                logger.warn("Iso639-2: "+iso639_2+" too long");
//            }
//        }
//        this.iso639_2 = iso639_2;
//    }

    @Override
    public Language readCsvLine(Class<Language> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        try {
            Language newInstance =  Language.class.newInstance();
            if ( UUID.fromString(csvLine.get(0).toString()).equals(Language.uuidEnglish)){
                DefinedTermBase.readCsvLine(newInstance, csvLine, newInstance, abbrevAsId);
            }else{
                DefinedTermBase.readCsvLine(newInstance,csvLine,(Language)terms.get(Language.uuidEnglish), abbrevAsId);
            }

//          newInstance.setIso639_2(csvLine.get(4).trim());   //does not exist anymore
//          newInstance.setIdInVocabulary(csvLine.get(4).trim());  //same as abbrev

            newInstance.setIso639_1(csvLine.get(5).trim());
            //TODO could replace with generic validation
            if(iso639_1 != null && iso639_1.length() > 2){
                logger.warn("Iso639-1: "+ newInstance.getIso639_1() +" from "+csvLine.get(3)+" ,"+csvLine.get(2)+" too long");
            }
            if(getIdInVocabulary() != null &&  getIdInVocabulary().length() != 3 ){
                logger.warn("Iso639-2: "+newInstance.getIso639_2()+" from "+csvLine.get(3)+" ,"+csvLine.get(2)+" too long");
            }

            return newInstance;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    public void writeCsvLine(CSVWriter writer, Language language) {
        String [] line = new String[6];
        line[0] = language.getUuid().toString();
        line[1] = language.getUri().toString();
        line[2] = language.getLabel(Language.CSV_LANGUAGE());
        line[3] = language.getDescription(Language.CSV_LANGUAGE());
        line[4] = language.getIso639_2();
        line[5] = language.getIso639_1();
        writer.writeNext(line);
    }

    public static Language getLanguageByDescription(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        for (Language language : termMap.values()){
            if (text.equalsIgnoreCase(language.getDescription())){
                return language;
            }
        }
        return null;
    }

    public static Language getLanguageByLabel(String label){
        if (StringUtils.isBlank(label)){
            return null;
        }
        for (Language language : termMap.values()){
            if (label.equalsIgnoreCase(language.getLabel())){
                return language;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.TermBase#toString()
     */
    @Override
    public String toString() {
        if (this.getLabel() != null){
            return this.getLabel();
        }else{
            return super.toString();
        }
    }

    @Override
    protected void setDefaultTerms(TermVocabulary<Language> termVocabulary) {
        if (termMap == null){  //there are 2 language vocabularies now
            termMap = new HashMap<UUID, Language>();
        }
        for (Language term : termVocabulary.getTerms()){
            termMap.put(term.getUuid(), term);
        }
        defaultLanguage = ENGLISH();
        csvLanguage = ENGLISH();
        addLanguageForVocabularyRepresentation(termVocabulary);
    }

    //FIXME: Following two methods are temporary and should be moved to more
    //       generic methods in the super classes
    /**
     * Gets the default language using the cache
     *
     * @return
     */
    public static Language getDefaultLanguage() {
    	return getLanguageFromUuid(uuidEnglish);
    }

    /**
     *
     *
     * @param uuid
     * @return
     */
    public static Language getLanguageFromUuid(UUID uuid) {
        if(termMap == null || termMap.isEmpty()) {
            return getTermByClassAndUUID(Language.class, uuid);
        } else {
            return termMap.get(uuid);
        }
    }

    /**
     * During vocabulary initialization the default language is not yet set to ENGLISH but is still null.
     * Therefore we need to add the language later (here).
     * @param termVocabulary
     */
    private void addLanguageForVocabularyRepresentation(TermVocabulary<Language> termVocabulary){
        for (Representation repr : termVocabulary.getRepresentations()){
            Language lang = repr.getLanguage();
            if (lang == null){
                repr.setLanguage(Language.CSV_LANGUAGE());
            }
        }
    }

}