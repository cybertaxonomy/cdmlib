/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_31;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v31_33.TermUpdater_31_33;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_314_315 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_314_315.class);
	
	public static final String startTermVersion = "3.0.1.4.201105100000";
	private static final String endTermVersion = "3.0.1.5.201109280000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_314_315 NewInstance(){
		return new TermUpdater_314_315(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_314_315(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

//		String identical = "\u2261";  //identical
//		String identical = "\u2258";  //corresponds to
//		String identical = "\u2263";  //strictly equivalent to
		String identical = "\u2245"; //APPROXIMATELY EQUAL TO
		String included = "\u2282";
		String includes = "\u2283";
		String overlaps = "\u2295";
		String excludes = "!";
		String misapplied = "\u2013";
		String contradiction = "\u2205";
		
		UUID uuidTerm;
		
		String description = null;
		String label = null;
		String abbrev;
		UUID uuidLang = Language.uuidEnglish;

		
		String stepName;
		
		stepName = "Update concept 'Taxonomically Included in'";
		uuidTerm = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
		abbrev = included;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = includes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Misapplied Name'";
		uuidTerm = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
		abbrev = misapplied;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = misapplied;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Invalid Designation for'";
		uuidTerm = UUID.fromString("605b1d01-f2b1-4544-b2e0-6f08def3d6ed");
		abbrev = misapplied;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = misapplied;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Contradiction'";
		uuidTerm = UUID.fromString("a8f03491-2ad6-4fae-a04c-2a4c117a2e9b");
		abbrev = contradiction;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = contradiction;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to'";
		uuidTerm = UUID.fromString("60974c98-64ab-4574-bb5c-c110f6db634d");
		abbrev = identical;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Includes'";
		uuidTerm = UUID.fromString("0501c385-cab1-4fbe-b945-fc747419bb13");
		abbrev = includes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = included;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to or Includes'";
		uuidTerm = UUID.fromString("b55cb3a2-6e20-4ca3-95bc-12b59d3235b0");
		abbrev = identical + includes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical + included;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Included in or Includes'";
		uuidTerm = UUID.fromString("c3ed5089-6779-4051-bb24-f5ea0eca80d5");
		abbrev = included + includes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = included + includes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));
		
		stepName = "Update concept 'Congruent to or Included in or Includes'";
		uuidTerm = UUID.fromString("0170cd83-93ad-43c2-9ad1-7ac879300e2f");
		abbrev = identical + included + includes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical + included + includes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Overlaps'";
		uuidTerm = UUID.fromString("2046a0fd-4fd6-45a1-b707-2b91547f3ec7");
		abbrev = overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to or Overlaps'";
		uuidTerm = UUID.fromString("78355cfa-5200-432f-8e00-82b97afad0ed");
		abbrev = identical + overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Includes or Overlaps'";
		uuidTerm = UUID.fromString("f1ec567b-3c73-436b-8625-b4fd53588abb");
		abbrev = includes + overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = included + overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to or Includes or Overlaps'";
		uuidTerm = UUID.fromString("2d923b1a-6c0f-414c-ac9b-bbc502e18078");
		abbrev = identical + includes + overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical + included + overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Included in or Includes or Overlaps'";
		uuidTerm = UUID.fromString("43466aa9-e431-4f37-8bca-febfd9f63716");
		abbrev = included + includes + overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = included + includes + overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Does Not Exclude'";
		uuidTerm = UUID.fromString("0e5099bb-87c0-400e-abdc-bcfed5b5eece");
		abbrev = identical + included + includes + overlaps;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = identical + included + includes + overlaps;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'excludes'";
		uuidTerm = UUID.fromString("4535a63c-4a3f-4d69-9350-7bf02e2c23be");
		abbrev = excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to or Excludes'";
		uuidTerm = UUID.fromString("758e6cf3-05a0-49ed-9496-d8c4a9fd02ae");
		abbrev = identical + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = identical + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Includes or Excludes'";
		uuidTerm = UUID.fromString("6ee440bc-fd3d-4da2-ad85-906d35a94731");
		abbrev = includes + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = included + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));


		stepName = "Update concept 'Congruent to or Includes or Excludes'";
		uuidTerm = UUID.fromString("d5c6953d-aa53-46f8-aafc-ebc6428ad5d0");
		abbrev = identical + includes + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical + included + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Included in or Includes or Excludes'";
		uuidTerm = UUID.fromString("43d8492c-8bd5-4f38-a633-f1ad910a34dd");
		abbrev = included + includes + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = included + includes + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Does Not Overlap'";
		uuidTerm = UUID.fromString("ecd2382b-3d94-4169-9dd2-2c4ea1d24605");
		abbrev = identical + included + includes + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = identical + included + includes + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Overlaps or Excludes'";
		uuidTerm = UUID.fromString("623ecdeb-ff1f-471d-a8dc-0d75b2fe8d94");
		abbrev = overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Congruent to or Overlaps or Excludes'";
		uuidTerm = UUID.fromString("6fabef72-5264-44f1-bfc0-8e2e141375f2");
		abbrev = identical + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = identical + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Includes or Overlaps or Excludes'";
		uuidTerm = UUID.fromString("b7153c89-cc6c-4f8c-bf74-216f10feac46");
		abbrev = includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = included + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Not Included in'";
		uuidTerm = UUID.fromString("89dffa4e-e004-4d42-b0d1-ae1827529e43");
		abbrev = identical + includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
		abbrev = identical + included + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'Not Congruent to'";
		uuidTerm = UUID.fromString("6c16c33b-cfc5-4a00-92bd-a9f9e448f389");
		abbrev = included + includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = included + includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		stepName = "Update concept 'All Relationships'";
		uuidTerm = UUID.fromString("831fcd88-e5c9-49e0-b06e-bbb67d1c05c9");
		abbrev = identical + included + includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, uuidLang));
//		abbrev = identical + included + includes + overlaps + excludes;
		list.add( TermRepresentationUpdater.NewReverseInstance(stepName + " reverse", uuidTerm, description, label, abbrev, uuidLang));

		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_31_33.NewInstance();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_313_314.NewInstance();
	}

}
