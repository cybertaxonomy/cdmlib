/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v525_527;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v523_525.SchemaUpdater_5250_5251;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 22.04.2021
 */
public class SchemaUpdater_5251_5270 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5251_5270.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_25_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_27_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5251_5270 NewInstance() {
		return new SchemaUpdater_5251_5270();
	}

	protected SchemaUpdater_5251_5270() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5250_5251.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//cultivarEpi
		stepName = "Change cultivar name column name";
		tableName = "TaxonName";
		String oldColumnName = "cultivarName";
		newColumnName = "cultivarEpithet";
		ColumnNameChanger.NewVarCharInstance(stepList, stepName, tableName, oldColumnName, newColumnName, 255, INCLUDE_AUDIT);

		//add cultivarGroup
		//#9761
		stepName = "Add cultivarGroup";
		tableName = "TaxonName";
		newColumnName = "cultivarGroupEpithet";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

		//TODO update where rank = CultivarGroup

		//#9755 Add Gp abbreviation to cultivar group rank
		stepName = "Add abbrev to cultivar group rank";
		UUID uuidTerm = UUID.fromString("d763e7d3-e7de-4bb1-9d75-225ca6948659");
		UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, "Gp", uuidLanguage, true);

        //update country representations
        updateCountryRepresentations(stepList);

        return stepList;
    }

    private void updateCountryRepresentations(List<ISchemaUpdaterStep> stepList) {
        updateCountryRepresentation(stepList, "974ce01a-5bce-4be8-b728-a46869354960", "Afghanistan", "Islamic Emirate of Afghanistan", "AF");
        updateCountryRepresentation(stepList, "1a8118d8-6741-41a4-9278-30561005191a", "\u00c5land Islands", "\u00c5land Islands", "AX");
        updateCountryRepresentation(stepList, "238a6a93-8857-4fd6-af9e-6437c90817ac", "Albania", "Republic of Albania", "AL");
        updateCountryRepresentation(stepList, "a14b38ac-e963-4c1a-85c2-de1f17f8c72a", "Algeria", "People's Democratic Republic of Algeria", "DZ");
        updateCountryRepresentation(stepList, "4a071803-88aa-4367-9707-bb1f24ad4386", "American Samoa", "American Samoa", "AS");
        updateCountryRepresentation(stepList, "7efd738f-33a1-4969-9d49-552571ffe935", "Andorra", "Principality of Andorra", "AD");
        updateCountryRepresentation(stepList, "c48ca5e4-154a-46d6-af29-f722486bedba", "Angola", "Republic of Angola", "AO");
        updateCountryRepresentation(stepList, "4a3b7f0d-0ff5-4691-a232-a2dc43ad4c56", "Anguilla", "Anguilla", "AI");
        updateCountryRepresentation(stepList, "36aea55c-5d4c-4015-bb70-f15d9280c805", "Antarctica", "Antarctica (the territory South of 60 deg S)", "AQ");
        updateCountryRepresentation(stepList, "fe425b94-f0e2-4e20-9e08-f28d53016347", "Antigua and Barbuda", "Antigua and Barbuda", "AG");
        updateCountryRepresentation(stepList, "ee0a4820-914d-424c-8133-57efb3028741", "Argentina", "Argentine Republic", "AR");
        updateCountryRepresentation(stepList, "7c685229-ce21-4dfd-a2c7-0932003f14ef", "Armenia", "Republic of Armenia", "AM");
        updateCountryRepresentation(stepList, "f5a9fc99-52d5-4a54-9859-edede22cb39d", "Aruba", "Country of Aruba", "AW");
        updateCountryRepresentation(stepList, "c22658e2-b1a9-4f4c-9ccd-affe0255efc8", "Australia", "Commonwealth of Australia", "AU");
        updateCountryRepresentation(stepList, "dfeb9102-7101-41cb-9449-bf5eae83cb5b", "Austria", "Republic of Austria", "AT");
        updateCountryRepresentation(stepList, "5189a180-f4ef-4a8a-9e90-36977c351960", "Azerbaijan", "Republic of Azerbaijan", "AZ");
        updateCountryRepresentation(stepList, "8b6851bf-b82e-4114-a99f-9b40ce0f3b2c", "Bahamas", "Commonwealth of The Bahamas", "BS");
        updateCountryRepresentation(stepList, "7f7e8c06-a804-4efa-b02f-7679f929a760", "Bahrain", "Kingdom of Bahrain", "BH");
        updateCountryRepresentation(stepList, "89752d76-d03a-46e1-9763-cc089f8a8e53", "Bangladesh", "People's Republic of Bangladesh", "BD");
        updateCountryRepresentation(stepList, "c870ad88-4393-4e76-a37d-39656c5d7ff2", "Barbados", "Barbados", "BB");
        updateCountryRepresentation(stepList, "66872923-5ae7-48be-b669-d9a2b7e4663c", "Belarus", "Republic of Belarus", "BY");
        updateCountryRepresentation(stepList, "fa27fe27-4966-4381-a341-3535f2b4309e", "Belgium", "Kingdom of Belgium", "BE");
        updateCountryRepresentation(stepList, "6c3eeed7-00eb-4aa3-8e3c-2d8bc25f3338", "Belize", "Belize", "BZ");
        updateCountryRepresentation(stepList, "e6875306-892c-43d0-9aaa-9ac26e5d6551", "Benin", "Republic of Benin", "BJ");
        updateCountryRepresentation(stepList, "88f4017e-27dc-4828-a2d7-0cf0637f1a7b", "Bermuda", "Bermuda", "BM");
        updateCountryRepresentation(stepList, "35d9b61f-15d6-453d-8b01-8c786da241b3", "Bhutan", "Kingdom of Bhutan", "BT");
        updateCountryRepresentation(stepList, "8a18a774-0072-4678-8746-43de9ee066c4", "Bolivia", "Plurinational State of Bolivia", "BO");
        updateCountryRepresentation(stepList, "a452f067-e31c-46c5-8855-08a5fc3082ff", "Bonaire, Sint Eustatius and Saba", "Bonaire, Sint Eustatius and Saba", "BQ");
        updateCountryRepresentation(stepList, "368be113-c0f2-444c-939c-65b544d19702", "Bosnia and Herzegovina", "Bosnia and Herzegovina", "BA");
        updateCountryRepresentation(stepList, "e00464af-d38e-4cd5-b5fe-50b27eace4ee", "Botswana", "Republic of Botswana", "BW");
        updateCountryRepresentation(stepList, "65fa17a7-efa7-4be5-9d51-8b261c5217b7", "Bouvet Island", "Bouvet Island", "BV");
        updateCountryRepresentation(stepList, "dccbe7f8-d5e3-48e5-bcbb-96886eb7108a", "Brazil", "Federative Republic of Brazil", "BR");
        updateCountryRepresentation(stepList, "996f912c-971f-40cb-88a4-1575226415b9", "British Indian Ocean Territory", "British Indian Ocean Territory", "IO");
        updateCountryRepresentation(stepList, "5b71a5a2-0551-4563-b0aa-8aa259b90979", "British Virgin Islands", "Virgin Islands", "VG");
        updateCountryRepresentation(stepList, "7e6247b5-4145-454b-ad51-b60809a8a939", "Brunei", "Nation of Brunei, the Abode of Peace", "BN");
        updateCountryRepresentation(stepList, "51ddedf0-4646-46ba-9840-ab5513eec455", "Bulgaria", "Republic of Bulgaria", "BG");
        updateCountryRepresentation(stepList, "c4b22384-e26f-4a44-b641-64208f72ea25", "Burkina Faso", "Burkina Faso", "BF");
        updateCountryRepresentation(stepList, "0d584b61-15b9-41fa-8cec-242f1f094417", "Burundi", "Republic of Burundi", "BI");
        updateCountryRepresentation(stepList, "485a4988-a3dd-43b8-9c18-e0351618056a", "Cambodia", "Kingdom of Cambodia", "KH");
        updateCountryRepresentation(stepList, "30ba95a0-a951-46a0-aa67-e539475d4386", "Cameroon", "Republic of Cameroon", "CM");
        updateCountryRepresentation(stepList, "5dc3dc6f-3816-44b3-b661-a4cf1528bae7", "Canada", "Canada", "CA");
        updateCountryRepresentation(stepList, "083ff0fc-9eea-4f1b-80c0-f203bd2890b8", "Cape Verde", "Republic of Cabo Verde", "CV");
        updateCountryRepresentation(stepList, "23264b59-fcc9-47a0-9f69-30a98757c121", "Cayman Islands", "Cayman Islands", "KY");
        updateCountryRepresentation(stepList, "40d7ffa6-11cc-417c-adf7-f4acc03cca20", "Central African Republic", "Central African Republic", "CF");
        updateCountryRepresentation(stepList, "d1ea5922-6bd2-4c63-b49a-259207c584a4", "Chad", "Republic of Chad", "TD");
        updateCountryRepresentation(stepList, "9c41644f-4946-4586-b2a4-c8ec33dbe68b", "Chile", "Republic of Chile", "CL");
        updateCountryRepresentation(stepList, "e0ed33bb-4afe-4994-81f3-b5f91655ff62", "China", "People's Republic of China", "CN");
        updateCountryRepresentation(stepList, "e785a72e-2b51-42b9-bea0-888924906b3e", "Christmas Island", "Territory of Christmas Island", "CX");
        updateCountryRepresentation(stepList, "0994e57b-a0fa-4597-9098-8815235e9053", "Cocos (Keeling) Islands", "Territory of Cocos (Keeling) Islands", "CC");
        updateCountryRepresentation(stepList, "cd334393-328c-4fb7-9600-bdca44c224d6", "Colombia", "Republic of Colombia", "CO");
        updateCountryRepresentation(stepList, "3b52601e-e85f-415c-bc36-acc45717107f", "Comoros", "Union of the Comoros", "KM");
        updateCountryRepresentation(stepList, "5a70a5b8-7264-48f1-b552-6fde52ae43f7", "Congo (DRC)", "Democratic Republic of the Congo", "CD");
        updateCountryRepresentation(stepList, "5c0a6d1d-f5c1-4c92-b3cd-9a1c0cd0d9dc", "Congo Republic", "Republic of the Congo", "CG");
        updateCountryRepresentation(stepList, "72f5df8d-ff1c-44af-9444-e368d770f36f", "Cook Islands", "Cook Islands", "CK");
        updateCountryRepresentation(stepList, "aca508c7-2d49-4760-83cb-93b6ccce6751", "Costa Rica", "Republic of Costa Rica", "CR");
        updateCountryRepresentation(stepList, "5a6673d7-1580-4470-974c-b36c4584247f", "Ivory Coast", "Republic of C\u00f4te d'Ivoire", "CI");
        updateCountryRepresentation(stepList, "229f0575-9035-4738-8741-f131cad59107", "Cuba", "Republic of Cuba", "CU");
        updateCountryRepresentation(stepList, "41d3dd57-e1e0-4d88-94be-ef163fb9dd82", "Cura\u00e7ao", "Country of Cura\u00e7ao", "CW");
        updateCountryRepresentation(stepList, "4b13d6b8-7eca-4d42-8172-f2018051ca19", "Cyprus", "Republic of Cyprus", "CY");
        updateCountryRepresentation(stepList, "56ee8c08-506d-4c27-9c31-db5344356ea3", "Czech Republic", "Czech Republic", "CZ");
        updateCountryRepresentation(stepList, "dbf70b64-a47e-4339-ae07-828f9ff2b7d8", "Denmark", "Kingdom of Denmark", "DK");
        updateCountryRepresentation(stepList, "8c80ca2b-e6e6-46bc-9f35-978a1a078a55", "Djibouti", "Republic of Djibouti", "DJ");
        updateCountryRepresentation(stepList, "c8ef3805-69dd-4e84-ab69-c813252910dd", "Dominica", "Republic of Dominica", "DM");
        updateCountryRepresentation(stepList, "1c2084e4-38cc-41d1-9d33-0360fed7c55d", "Dominican Republic", "Dominican Republic", "DO");
        updateCountryRepresentation(stepList, "e396160a-3554-4da8-ad40-cd7137c021d7", "Ecuador", "Republic of Ecuador", "EC");
        updateCountryRepresentation(stepList, "3c4a2a5a-d3d7-4c82-a28f-2feaa7050c04", "Egypt", "Arab Republic of Egypt", "EG");
        updateCountryRepresentation(stepList, "2706e84c-a57d-40ab-aee4-dce25fe89211", "El Salvador", "Republic of El Salvador", "SV");
        updateCountryRepresentation(stepList, "7d0cee2b-086a-465e-afc3-0216bff7fd19", "Equatorial Guinea", "Republic of Equatorial Guinea", "GQ");
        updateCountryRepresentation(stepList, "8394a73d-a0c6-481c-8e86-e05705891fac", "Eritrea", "State of Eritrea", "ER");
        updateCountryRepresentation(stepList, "b442614f-5bfa-4583-b87b-7c7c856015f1", "Estonia", "Republic of Estonia", "EE");
        updateCountryRepresentation(stepList, "8866fa09-8ee2-4957-ad86-4e622085ef40", "Ethiopia", "Federal Democratic Republic of Ethiopia", "ET");
        updateCountryRepresentation(stepList, "0b2933ea-cee6-4611-b52b-09d6fcdbcf9d", "Faeroe Islands", "Faeroe Islands", "FO");
        updateCountryRepresentation(stepList, "8c667c52-70b6-447a-b4f2-dfa2d759d5f6", "Falkland Islands", "Falkland Islands", "FK");
        updateCountryRepresentation(stepList, "8a83a1e5-b648-4cea-86cd-7affaea817a7", "Fiji", "Republic of Fiji", "FJ");
        updateCountryRepresentation(stepList, "47bbb4b3-6f18-46f9-9eb6-6ec92c41fe84", "Finland", "Republic of Finland", "FI");
        updateCountryRepresentation(stepList, "4c49d9d3-6bc3-481a-93c6-c8156cba25fe", "France", "French Republic", "FR");
        updateCountryRepresentation(stepList, "38ba5ec2-913b-4894-a5bf-d55f3bd9d7a0", "French Guiana", "French Guiana", "GF");
        updateCountryRepresentation(stepList, "7dadc5d4-d4e8-4ad6-bfa4-e8498a706778", "French Polynesia", "French Polynesia", "PF");
        updateCountryRepresentation(stepList, "590663d7-1b7e-4088-9407-2a589eb73fd4", "French Southern and Antarctic Lands", "French Southern and Antarctic Lands", "TF");
        updateCountryRepresentation(stepList, "d285a9f8-4349-4428-a848-c9aa45c4c8ab", "Gabon", "Gabonese Republic", "GA");
        updateCountryRepresentation(stepList, "3dcc7fea-7785-4254-9947-f724e27a76fc", "The Gambia", "Republic of The Gambia", "GM");
        updateCountryRepresentation(stepList, "af3f8bd9-1f5e-42cf-a0cc-f9199ab1bb89", "Georgia", "Georgia", "GE");
        updateCountryRepresentation(stepList, "cbe7ce69-2952-4309-85dd-0d7d4a4830a1", "Germany", "Federal Republic of Germany", "DE");
        updateCountryRepresentation(stepList, "d4cf6c57-98ee-43b8-8d92-b510371dd151", "Ghana", "Republic of Ghana", "GH");
        updateCountryRepresentation(stepList, "46764ae0-2d8d-461e-89d0-a1953edef02f", "Gibraltar", "Gibraltar", "GI");
        updateCountryRepresentation(stepList, "5b7c78d1-f068-4c4d-b2c9-9ac075b7169a", "Greece", "Hellenic Republic", "GR");
        updateCountryRepresentation(stepList, "34bbe398-e0da-40bd-b16b-34a2e9fd3cc2", "Greenland", "Greenland", "GL");
        updateCountryRepresentation(stepList, "dda637e3-7742-4faf-bc05-e5d2c2d86a52", "Grenada", "Grenada", "GD");
        updateCountryRepresentation(stepList, "2559330d-f79b-4273-b6db-e47abce1de6c", "Guadeloupe", "Guadeloupe", "GP");
        updateCountryRepresentation(stepList, "264c71d7-91ef-4a5e-9ae6-49aac2a6ba3a", "Guam", "Guam", "GU");
        updateCountryRepresentation(stepList, "54040dec-6f42-48cc-93d8-8b283b23e530", "Guatemala", "Republic of Guatemala", "GT");
        updateCountryRepresentation(stepList, "28d26acf-55d3-4419-aa63-f5867d247872", "Guernsey", "Bailiwick of Guernsey", "GG");
        updateCountryRepresentation(stepList, "1b3cf756-b0c2-4e14-88af-d260b937d01f", "Guinea", "Republic of Guinea", "GN");
        updateCountryRepresentation(stepList, "2dbf1dc1-7428-4284-9090-8785a30f4e71", "Guinea-Bissau", "Republic of Guinea-Bissau", "GW");
        updateCountryRepresentation(stepList, "9cbe3428-0cfe-420e-a88e-eac196a16a37", "Guyana", "Co-operative Republic of Guyana", "GY");
        updateCountryRepresentation(stepList, "f1071b42-0247-4c4d-92a5-8bdf18099c50", "Haiti", "Republic of Haiti", "HT");
        updateCountryRepresentation(stepList, "646a16d4-4a1f-47a0-a475-a19c605e04e0", "Heard Island and McDonald Islands", "Territory of Heard Island and McDonald Islands", "HM");
        updateCountryRepresentation(stepList, "afebd310-0c8d-4601-b025-a06a1d195035", "Vatican City", "Vatican City State", "VA");
        updateCountryRepresentation(stepList, "c6684b89-3ea6-4922-9148-d74ff3ee33fd", "Honduras", "Republic of Honduras", "HN");
        updateCountryRepresentation(stepList, "5aa1c98c-9efd-443f-9c10-708f175d5cea", "Hong Kong", "Hong Kong Special Administrative Region of the People's Republic of China", "HK");
        updateCountryRepresentation(stepList, "a3acb45e-39ec-476b-bff2-7ff7e0383f7e", "Croatia", "Republic of Croatia", "HR");
        updateCountryRepresentation(stepList, "4d8b56d0-ab74-437f-98e0-3b88ebaa8c89", "Hungary", "Hungary", "HU");
        updateCountryRepresentation(stepList, "c7bf91f8-024c-4c04-9c0b-856a27b2d0ca", "Iceland", "Iceland", "IS");
        updateCountryRepresentation(stepList, "a0b872f9-fc04-440d-ace3-edce8ea75e0b", "India", "Republic of India", "IN");
        updateCountryRepresentation(stepList, "96eb663a-61b1-4a44-9017-0c4b1ea024d6", "Indonesia", "Republic of Indonesia", "ID");
        updateCountryRepresentation(stepList, "14f148e0-a9cf-428d-a244-a9917aae974d", "Iran", "Islamic Republic of Iran", "IR");
        updateCountryRepresentation(stepList, "daf3de07-b1b8-47fa-8207-7e237ea30b7f", "Iraq", "Republic of Iraq", "IQ");
        updateCountryRepresentation(stepList, "376f61f8-6234-4e61-bc5e-d0d76393cfa0", "Ireland", "Republic of Ireland", "IE");
        updateCountryRepresentation(stepList, "4c61dc3f-978d-4df9-9bd9-65089ee01dae", "Israel", "State of Israel", "IL");
        updateCountryRepresentation(stepList, "c726cf14-b9d2-4d72-8767-862b33be9e49", "Isle of Man", "Isle of Man", "IM");
        updateCountryRepresentation(stepList, "9404a588-503b-4033-acf5-ee4a47337ed0", "Italy", "Italian Republic", "IT");
        updateCountryRepresentation(stepList, "528bede6-26db-47e6-b6cb-32f77ab5fef7", "Jamaica", "Jamaica", "JM");
        updateCountryRepresentation(stepList, "a8be059a-6f1a-45aa-8019-f6bc3b81c691", "Japan", "Japan", "JP");
        updateCountryRepresentation(stepList, "ccfd99b1-aabc-42ef-b0f6-36c83eb9eb05", "Jersey", "Bailiwick of Jersey", "JE");
        updateCountryRepresentation(stepList, "533b9709-1f97-43e6-8e12-68e116675c64", "Jordan", "Hashemite Kingdom of Jordan", "JO");
        updateCountryRepresentation(stepList, "3047567d-997d-491a-b0bc-d4b287f76fab", "Kazakhstan", "Republic of Kazakhstan", "KZ");
        updateCountryRepresentation(stepList, "9410b793-43fa-4205-bd24-5f92d392667f", "Kenya", "Republic of Kenya", "KE");
        updateCountryRepresentation(stepList, "d46f42ec-a520-49d8-ac87-cc8bccc91516", "Kiribati", "Republic of Kiribati", "KI");
        updateCountryRepresentation(stepList, "0f2068a7-e284-417d-87ec-691c1e64c13c", "North Korea", "Democratic People's Republic of Korea", "KP");
        updateCountryRepresentation(stepList, "f81e0bbb-8984-431e-9962-de590a989fd3", "South Korea", "Republic of Korea", "KR");
        updateCountryRepresentation(stepList, "00451db7-4f5a-4e5d-a6fe-955a8af306a0", "Kuwait", "State of Kuwait", "KW");
        updateCountryRepresentation(stepList, "fc3cb838-98f0-46b4-a5fe-5efafc121e95", "Kyrgyzstan", "Kyrgyz Republic", "KG");
        updateCountryRepresentation(stepList, "83b736b4-5049-4301-b370-ba19e7aa0403", "Laos", "Lao People's Democratic Republic", "LA");
        updateCountryRepresentation(stepList, "c24a316c-cec1-47c2-a777-296ce67ce11a", "Latvia", "Republic of Latvia", "LV");
        updateCountryRepresentation(stepList, "425b9cd2-0056-484a-9f77-5449215c65ba", "Lebanon", "Lebanese Republic", "LB");
        updateCountryRepresentation(stepList, "fbbbc46c-ed8f-45f5-87bc-062a7ee7ffdf", "Lesotho", "Kingdom of Lesotho", "LS");
        updateCountryRepresentation(stepList, "f40126ab-4cbe-409e-8f61-8911280e0857", "Liberia", "Republic of Liberia", "LR");
        updateCountryRepresentation(stepList, "b9115908-2937-45e3-8fb3-009136b013af", "Libya", "State of Libya", "LY");
        updateCountryRepresentation(stepList, "1bb6cf13-1286-40c8-bff8-1a18ef65e213", "Liechtenstein", "Principality of Liechtenstein", "LI");
        updateCountryRepresentation(stepList, "3a2a0f69-92b1-45ab-baa8-47cf48e7272b", "Lithuania", "Republic of Lithuania", "LT");
        updateCountryRepresentation(stepList, "5c481573-3d28-4c2c-87e1-acee4ccc64f1", "Luxembourg", "Grand Duchy of Luxembourg", "LU");
        updateCountryRepresentation(stepList, "927f5ae3-8d26-4794-9e5d-95cf9e0dfd03", "Macau", "Macao Special Administrative Region of the People's Republic of China", "MO");
        updateCountryRepresentation(stepList, "1cf135bb-cac7-4ba9-82dc-319ee41984c5", "North Macedonia", "Republic of North Macedonia", "MK");
        updateCountryRepresentation(stepList, "116be5e1-861e-4283-8689-f527a923b9d3", "Madagascar", "Republic of Madagascar", "MG");
        updateCountryRepresentation(stepList, "61b41230-6365-433f-9454-5fea029f0e02", "Malawi", "Republic of Malawi", "MW");
        updateCountryRepresentation(stepList, "5650de95-a90c-45c1-92bf-85d9b91911dd", "Malaysia", "Malaysia", "MY");
        updateCountryRepresentation(stepList, "5b932d64-3ca6-4691-881f-8b48bd2f3f15", "Maldives", "Republic of Maldives", "MV");
        updateCountryRepresentation(stepList, "2e201266-8535-4437-8870-a1d63745ec3d", "Mali", "Republic of Mali", "ML");
        updateCountryRepresentation(stepList, "0ee9727a-36cb-40cb-9e65-cd4646c09d63", "Malta", "Republic of Malta", "MT");
        updateCountryRepresentation(stepList, "2c507bb4-de73-4e3f-98ce-26bd2b0c016a", "Marshall Islands", "Republic of the Marshall Islands", "MH");
        updateCountryRepresentation(stepList, "93ec114a-0486-4325-bef8-d1b5dea89419", "Martinique", "Martinique", "MQ");
        updateCountryRepresentation(stepList, "dfd0aaf0-4a73-4d41-a6d7-9cdfd01f4c40", "Mauritania", "Islamic Republic of Mauritania", "MR");
        updateCountryRepresentation(stepList, "719daa07-1dce-4473-8c40-b0efd644028c", "Mauritius", "Republic of Mauritius", "MU");
        updateCountryRepresentation(stepList, "48116e69-19a9-4169-9952-4ca46c586fa2", "Mayotte", "Department of Mayotte", "YT");
        updateCountryRepresentation(stepList, "4ba4809b-3fa8-496d-a74d-80843a4740c8", "Mexico", "United Mexican States", "MX");
        updateCountryRepresentation(stepList, "70a91b6f-f196-4051-afdb-4e9aeaca490d", "Micronesia", "Federated States of Micronesia", "FM");
        updateCountryRepresentation(stepList, "500f43b9-47c4-4c2a-af58-80adbc40c5f3", "Moldova", "Republic of Moldova", "MD");
        updateCountryRepresentation(stepList, "4ef4c6cb-e02c-41a3-8d5f-74e8ae09ca71", "Monaco", "Principality of Monaco", "MC");
        updateCountryRepresentation(stepList, "8b7ebb83-9998-4efd-b97c-f1b7d3a7151f", "Mongolia", "Mongolia", "MN");
        updateCountryRepresentation(stepList, "ac503b8d-ec58-433c-a64a-e76c9d1a7310", "Montenegro", "Montenegro", "ME");
        updateCountryRepresentation(stepList, "cd64d76f-6f2b-4e44-8d31-a2765100257b", "Montserrat", "Montserrat", "MS");
        updateCountryRepresentation(stepList, "d9c048d5-3220-439d-8af4-2a8ec3036e5b", "Morocco", "Kingdom of Morocco", "MA");
        updateCountryRepresentation(stepList, "9f2b714e-6159-401b-9108-5d0b9413f6c8", "Mozambique", "Republic of Mozambique", "MZ");
        updateCountryRepresentation(stepList, "fd07e660-b3d6-46e7-bf7d-ec984e573c60", "Myanmar", "Republic of the Union of Myanmar", "MM");
        updateCountryRepresentation(stepList, "2c361180-c71c-4de0-8a98-0ff5a71bccaa", "Namibia", "Republic of Namibia", "NA");
        updateCountryRepresentation(stepList, "35d8c1ce-a2e9-43d6-9afe-582278a53d34", "Nauru", "Republic of Nauru", "NR");
        updateCountryRepresentation(stepList, "fa46cd94-68f9-4d0d-98a2-27dc6589658f", "Nepal", "Federal Democratic Republic of Nepal", "NP");
        updateCountryRepresentation(stepList, "5880f989-f10d-4a9c-aae8-4e6c7b212dd8", "Netherlands", "Netherlands", "NL");
        updateCountryRepresentation(stepList, "587f11ed-27de-4751-9d04-b04f13f3f67c", "New Caledonia", "New Caledonia", "NC");
        updateCountryRepresentation(stepList, "322c12c9-7b5a-4343-9861-23c93bbe62b4", "New Zealand", "New Zealand", "NZ");
        updateCountryRepresentation(stepList, "290da724-674d-4c99-8630-cb237162ae0a", "Nicaragua", "Republic of Nicaragua", "NI");
        updateCountryRepresentation(stepList, "1804792f-cccd-4f14-9e63-5c241bfd8429", "Niger", "Republic of the Niger", "NE");
        updateCountryRepresentation(stepList, "6dae052c-7477-485a-9d2c-63760991f9d8", "Nigeria", "Federal Republic of Nigeria", "NG");
        updateCountryRepresentation(stepList, "e804fe1d-8246-481b-a293-d3c0b71d6abd", "Niue", "Niue", "NU");
        updateCountryRepresentation(stepList, "3d5afd71-90d7-459f-ade1-c8b65cbc7fe1", "Norfolk Island", "Norfolk Island", "NF");
        updateCountryRepresentation(stepList, "43471298-1133-473e-b9b3-9152c5955177", "Northern Mariana Islands", "Commonwealth of the Northern Mariana Islands", "MP");
        updateCountryRepresentation(stepList, "e136efdf-82bb-4528-be5c-881acd8315cb", "Norway", "Kingdom of Norway", "NO");
        updateCountryRepresentation(stepList, "36f43aca-3302-4abd-a7e3-f65ff050a087", "Oman", "Sultanate of Oman", "OM");
        updateCountryRepresentation(stepList, "d42712ec-45aa-4811-9029-d38e5a607345", "Pakistan", "Islamic Republic of Pakistan", "PK");
        updateCountryRepresentation(stepList, "02f4bc12-bc36-447b-b08c-e74e8fe25678", "Palau", "Republic of Palau", "PW");
        updateCountryRepresentation(stepList, "41f45c19-6910-470e-86fb-a3f426b8ca9c", "Palestine", "State of Palestine", "PS");
        updateCountryRepresentation(stepList, "fd2ac965-bdb4-484a-9e4a-250f26aad030", "Panama", "Republic of Panama", "PA");
        updateCountryRepresentation(stepList, "3bc710b1-8b46-48e3-bdcd-54f64ca018cc", "Papua New Guinea", "Independent State of Papua New Guinea", "PG");
        updateCountryRepresentation(stepList, "e99f321f-664a-4a4b-90a9-1bdc98ea35f6", "Paraguay", "Republic of Paraguay", "PY");
        updateCountryRepresentation(stepList, "e4d92c3e-0f91-41d8-b10e-58c78b4c55ea", "Peru", "Republic of Peru", "PE");
        updateCountryRepresentation(stepList, "8547697c-d80f-4531-b092-4c9fde373d7b", "Philippines", "Republic of the Philippines", "PH");
        updateCountryRepresentation(stepList, "c3abd7ab-c953-4c0c-8bc1-e32f4a49775a", "Pitcairn Islands", "Pitcairn, Henderson, Ducie and Oeno Islands", "PN");
        updateCountryRepresentation(stepList, "579f8a7a-7fa5-4783-a8ec-cdc527781411", "Poland", "Republic of Poland", "PL");
        updateCountryRepresentation(stepList, "f47bd6f5-c82b-4932-81ce-40345748536b", "Portugal", "Portuguese Republic", "PT");
        updateCountryRepresentation(stepList, "6471bdcc-b4cc-4a07-b946-dd15be7eec41", "Puerto Rico", "Commonwealth of Puerto Rico", "PR");
        updateCountryRepresentation(stepList, "710d68a7-4a02-4d70-bbc8-22b904893429", "Qatar", "State of Qatar", "QA");
        updateCountryRepresentation(stepList, "d85d98f6-3f09-44b0-a39b-0e2b6bf4746c", "R\u00e9union", "R\u00e9union", "RE");
        updateCountryRepresentation(stepList, "7d7c8221-4123-4ba2-88ef-25e7f10aafbc", "Romania", "Romania", "RO");
        updateCountryRepresentation(stepList, "504292b5-053a-4c6a-a690-db031ac02fc0", "Russian Federation", "Russian Federation", "RU");
        updateCountryRepresentation(stepList, "27c2cc85-7c54-4356-b713-836c15f2da4e", "Rwanda", "Republic of Rwanda", "RW");
        updateCountryRepresentation(stepList, "2d89ed31-f695-4f7f-8d4d-989ec6509e50", "St. Barth\u00e9lemy", "Saint Barth\u00e9lemy", "BL");
        updateCountryRepresentation(stepList, "626ec513-fddb-41f3-ab36-2ae2190a1bc1", "St. Helena, Ascension and Tristan da Cunha", "Saint Helena, Ascension and Tristan da Cunha", "SH");
        updateCountryRepresentation(stepList, "777d19e2-d5e8-48e2-9a0f-cd95097e4e75", "St. Kitts and Nevis", "Federation of Saint Christopher and Nevis", "KN");
        updateCountryRepresentation(stepList, "a3a55f1c-ea50-43df-b141-e8543eb20ebb", "St. Lucia", "Saint Lucia", "LC");
        updateCountryRepresentation(stepList, "8e7dbca3-eda4-4cf8-9227-15abf4a75efc", "St. Martin", "Collectivity of Saint Martin", "MF");
        updateCountryRepresentation(stepList, "34f97908-18c5-4f67-b411-1b905161a330", "St. Pierre and Miquelon", "Territorial Collectivity of Saint-Pierre and Miquelon", "PM");
        updateCountryRepresentation(stepList, "dfe67a34-6a3a-4a56-8f90-3c007360f105", "St. Vincent and the Grenadines", "Saint Vincent and the Grenadines", "VC");
        updateCountryRepresentation(stepList, "7ad3f6bd-5e8a-467b-a481-1a523066b0e7", "Samoa", "Independent State of Samoa", "WS");
        updateCountryRepresentation(stepList, "e0c3ad69-a078-424f-a7d4-81025d190c91", "San Marino", "Republic of San Marino", "SM");
        updateCountryRepresentation(stepList, "a5369890-7a96-46bf-b91c-2c47d86660dd", "S\u00e3o Tom\u00e9 and Pr\u00edncipe", "Democratic Republic of S\u00e3o Tom\u00e9 and Pr\u00edncipe", "ST");
        updateCountryRepresentation(stepList, "62fe4794-7fb0-4520-9493-b9150436393e", "Saudi Arabia", "Kingdom of Saudi Arabia", "SA");
        updateCountryRepresentation(stepList, "e106a448-1205-4515-96f4-758e98176342", "Senegal", "Republic of Senegal", "SN");
        updateCountryRepresentation(stepList, "61619261-d853-42da-b662-0da0cd144e81", "Serbia", "Republic of Serbia", "RS");
        updateCountryRepresentation(stepList, "3bb44fb7-0976-4e3d-94b9-439763b53711", "Seychelles", "Republic of Seychelles", "SC");
        updateCountryRepresentation(stepList, "88e731a7-5c80-4f29-8cf0-54acf70d6277", "Sierra Leone", "Republic of Sierra Leone", "SL");
        updateCountryRepresentation(stepList, "e063b480-c834-4e39-b7a9-74fc578c637b", "Singapore", "Republic of Singapore", "SG");
        updateCountryRepresentation(stepList, "0e45eaf9-7109-4fc3-831f-7839cddf6dba", "Sint Maarten", "Sint Maarten", "SX");
        updateCountryRepresentation(stepList, "0349b9b5-865d-46ea-9750-ab71962d5106", "Slovakia", "Slovak Republic", "SK");
        updateCountryRepresentation(stepList, "526b3fb4-08fc-4238-aa8b-e3217fae7214", "Slovenia", "Republic of Slovenia", "SI");
        updateCountryRepresentation(stepList, "fc915f15-b2cf-40a7-8268-7c1f2744295a", "Solomon Islands", "Solomon Islands", "SB");
        updateCountryRepresentation(stepList, "e8591331-3b75-4569-90a6-4aca1d1d9a53", "Somalia", "Federal Republic of Somalia", "SO");
        updateCountryRepresentation(stepList, "508c9fcb-1b6c-4225-8e31-262a4df64a85", "South Africa", "Republic of South Africa", "ZA");
        updateCountryRepresentation(stepList, "bf34dad1-63d1-4859-8818-da369616c470", "South Georgia and the South Sandwich Islands", "South Georgia and the South Sandwich Islands", "GS");
        updateCountryRepresentation(stepList, "6abf6496-407e-4e73-8032-42748c74f88a", "South Sudan", "Republic of South Sudan", "SS");
        updateCountryRepresentation(stepList, "e4d6474b-d903-4850-b51e-389f546b7601", "Spain", "Kingdom of Spain", "ES");
        updateCountryRepresentation(stepList, "c7e74d0e-5c0d-4e3f-a19b-e072abbf0b92", "Sri Lanka", "Democratic Socialist Republic of Sri Lanka", "LK");
        updateCountryRepresentation(stepList, "a47a922b-fa61-4164-8f6d-7cf2ba33ca8c", "Sudan", "Republic of the Sudan", "SD");
        updateCountryRepresentation(stepList, "6268a5c7-df0e-4230-8681-966798383dc4", "Suriname", "Republic of Suriname", "SR");
        updateCountryRepresentation(stepList, "e47f9fe5-54c7-4c61-8c74-abc514749e41", "Svalbard & Jan Mayen", "Svalbard and Jan Mayen", "SJ");
        updateCountryRepresentation(stepList, "bb006073-0088-4adf-9482-01e598bc3fd3", "Eswatini", "Kingdom of Eswatini", "SZ");
        updateCountryRepresentation(stepList, "8272e206-cb6f-499c-a1d9-7c581f5947c5", "Sweden", "Kingdom of Sweden", "SE");
        updateCountryRepresentation(stepList, "dd79f943-8237-4710-bc5f-acc1ea1a2dd8", "Switzerland", "Swiss Confederation", "CH");
        updateCountryRepresentation(stepList, "f92c3ca4-3468-40b6-b387-d4677fca86d9", "Syria", "Syrian Arab Republic", "SY");
        updateCountryRepresentation(stepList, "0fffb0e5-81b9-40be-be69-9aff204f51c4", "Taiwan", "Republic of China", "TW");
        updateCountryRepresentation(stepList, "b78e4b96-6095-4316-bc4c-6bdec5593622", "Tajikistan", "Republic of Tajikistan", "TJ");
        updateCountryRepresentation(stepList, "8a519200-784a-495a-b0da-b3277913b880", "Tanzania", "United Republic of Tanzania", "TZ");
        updateCountryRepresentation(stepList, "6c35d8b5-a75b-4f17-8869-04cad4535bd8", "Thailand", "Kingdom of Thailand", "TH");
        updateCountryRepresentation(stepList, "77f9e6b5-a363-454c-996b-34aec2f10f99", "Timor-Leste", "Democratic Republic of Timor-Leste", "TL");
        updateCountryRepresentation(stepList, "75f15dd5-9998-4937-9a2c-b440798a6695", "Togo", "Togolese Republic", "TG");
        updateCountryRepresentation(stepList, "b301d428-6936-4538-b5d3-778534b779e6", "Tokelau", "Tokelau Islands", "TK");
        updateCountryRepresentation(stepList, "0abdcd01-09ff-42a8-b8ba-10458dca5ba9", "Tonga", "Kingdom of Tonga", "TO");
        updateCountryRepresentation(stepList, "20ed7f03-1263-47fd-a4df-26fab6daae75", "Trinidad and Tobago", "Republic of Trinidad and Tobago", "TT");
        updateCountryRepresentation(stepList, "e121e4d7-e1aa-4f2e-9b9e-33f5109460d7", "Tunisia", "Tunisian Republic", "TN");
        updateCountryRepresentation(stepList, "f7c15c55-d0b3-4eda-8961-582d5071df78", "Turkey", "Republic of Turkey", "TR");
        updateCountryRepresentation(stepList, "442c0439-cf39-4c5a-96de-a99fe1a476cf", "Turkmenistan", "Turkmenistan", "TM");
        updateCountryRepresentation(stepList, "d6c83f2f-5130-477a-994e-daa08b70352f", "Turks and Caicos Islands", "Turks and Caicos Islands", "TC");
        updateCountryRepresentation(stepList, "30745e37-22c6-4b92-b955-85cb23f0526f", "Tuvalu", "Tuvalu", "TV");
        updateCountryRepresentation(stepList, "b5f9a299-41ea-414b-83d5-91518f64a481", "US Virgin Islands", "Virgin Islands of the United States", "VI");
        updateCountryRepresentation(stepList, "e74c11af-3a4e-4d13-9c2a-2e57d2954111", "Uganda", "Republic of Uganda", "UG");
        updateCountryRepresentation(stepList, "c44e49c7-a447-466d-ae4f-d290ab03ff18", "Ukraine", "Ukraine", "UA");
        updateCountryRepresentation(stepList, "a5b5e8ce-66c8-4ca0-a31b-473c90876108", "United Arab Emirates", "United Arab Emirates", "AE");
        updateCountryRepresentation(stepList, "5364e352-926f-4e07-9abb-2deea19346ec", "United Kingdom", "United Kingdom of Great Britain and Northern Ireland", "GB");
        updateCountryRepresentation(stepList, "4e88114b-e278-4816-ba7d-7bc17098c407", "United States Minor Outlying Islands", "United States Minor Outlying Islands", "UM");
        updateCountryRepresentation(stepList, "d9dacd9e-dd04-4641-957a-589bdb9fe5fb", "United States of America", "United States of America", "US");
        updateCountryRepresentation(stepList, "baf46f00-7b05-4d88-b1cf-ce922f3ba262", "Uruguay", "Oriental Republic of Uruguay", "UY");
        updateCountryRepresentation(stepList, "86ebc56d-8b06-4bb1-a0f9-b15626c02fbd", "Uzbekistan", "Republic of Uzbekistan", "UZ");
        updateCountryRepresentation(stepList, "b4e16ad0-3cb7-4809-a5ae-9a143595c2a4", "Vanuatu", "Republic of Vanuatu", "VU");
        updateCountryRepresentation(stepList, "e8099497-0e51-41ca-85d7-d23b730d9c1a", "Venezuela", "Bolivarian Republic of Venezuela", "VE");
        updateCountryRepresentation(stepList, "f9295319-572e-4c3d-9962-176a7802750b", "Vietnam", "Socialist Republic of Vietnam", "VN");
        updateCountryRepresentation(stepList, "b4844963-f140-41b3-935d-58fd14df5878", "Wallis and Futuna", "Territory of the Wallis and Futuna Islands", "WF");
        updateCountryRepresentation(stepList, "fa9e1eb4-ee4c-4b13-82dd-ec42a8b7e627", "Western Sahara", "Western Sahara", "EH");
        updateCountryRepresentation(stepList, "713e1840-ff18-4a96-bc32-3da2b048c77d", "Yemen", "Republic of Yemen", "YE");
        updateCountryRepresentation(stepList, "90318040-d346-4c8f-be69-fa8ade0b12d9", "Zambia", "Republic of Zambia", "ZM");
        updateCountryRepresentation(stepList, "aa96ca19-46ab-40ad-a494-e4842f13eb4c", "Zimbabwe", "Republic of Zimbabwe", "ZW");
    }

    private void updateCountryRepresentation(List<ISchemaUpdaterStep> stepList, String uuidStr, String label,
            String description, String abbrev) {

        String stepName = "Update representation for country " + label;
        UUID uuidTerm = UUID.fromString(uuidStr);
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        abbrev = null; //not needed as 2letter code is neither used in abbreviation nor in idInVoc
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm, description, label, abbrev, uuidLanguage);
    }

}