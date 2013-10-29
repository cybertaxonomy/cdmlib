// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.eflora;

import java.net.URI;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 09.06.2010
 *
 */
public class EfloraSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EfloraSources.class);

	//Ericaceae
	public static URI ericacea_local() {
		return URI.create("file:C:/localCopy/Data/eflora/africa/Ericaceae/ericaceae_v2.xml");
	}
	
	public static URI ericacea_specimen_local() {
		return URI.create("file:/C:/localCopy/Data/eflora/africa/Specimen/Ericaceae/Ericaceae_CDM_specimen.xls");
	}
	
//******************* MALESIANA ************************************************************/
	
	//Sapindaceae
	public static URI fm_sapindaceae_local(){
		return URI.create("file:C:/localCopy/Data/eflora/floraMalesiana/sapindaceae-01v25.xml");
	}
	
	//Sapindaceae2
	public static URI fm_sapindaceae2_local(){
		return URI.create("file:C:/localCopy/Data/eflora/floraMalesiana/sapindaceae-02final2.xml");
	}
	
	//Flora Malesiana Vol 13-1
	public static URI fm_13_1_local(){
		return URI.create("file:C:/localCopy/Data/eflora/floraMalesiana/fm13_1_v8_final.xml");
	}

	//Flora Malesiana Vol 13-2
	public static URI fm_13_2_local(){
		return URI.create("file:C:/localCopy/Data/eflora/floraMalesiana/fm13_2_v8_final.xml");
	}

  //***** FM NEW */	
	
	//Flora Malesiana Vol 12_1
	public static URI fm_12_1(){
		return URI.create("file://PESIIMPORT3/malesiana/vol12_1_final.xml");
	}
	
	//Flora Malesiana Vol 12_2
	public static URI fm_12_2(){
		return URI.create("file://PESIIMPORT3/malesiana/vol12_2_final.xml");
	}
	
	public static URI fm_13(){
		return URI.create("file://PESIIMPORT3/malesiana/vol13_final.xml");
	}

//	//Flora Malesiana Vol 13 - large families
//	public static URI fm_13_large_families(){
//		return URI.create("file://PESIIMPORT3/malesiana/vol_13/xmlv9_large_families_vol_13.xml");
//	}

	//Flora Malesiana Vol 14
	public static URI fm_14(){
		return URI.create("file://PESIIMPORT3/malesiana/vol14_final.xml");
	}
	
	//Flora Malesiana Vol 15
	public static URI fm_15(){
		return URI.create("file://PESIIMPORT3/malesiana/vol15_final.xml");
	}

	//Flora Malesiana Vol 16
	public static URI fm_16(){
		return URI.create("file://PESIIMPORT3/malesiana/vol16_final.xml");
	}

	//Flora Malesiana Vol 17, part1
	public static URI fm_17_1(){
		return URI.create("file://PESIIMPORT3/malesiana/vol17_part1_final.xml");
	}
	
	//Flora Malesiana Vol 17, part2
	public static URI fm_17_2(){
		return URI.create("file://PESIIMPORT3/malesiana/vol17_part2_final.xml");
	}
	
	//Flora Malesiana Vol 18
	public static URI fm_18(){
		return URI.create("file://PESIIMPORT3/malesiana/vol18_final.xml");
	}
	//Flora Malesiana Vol 19
	public static URI fm_19(){
		return URI.create("file://PESIIMPORT3/malesiana/vol19_final.xml");
	}
	//Flora Malesiana Vol 20
	public static URI fm_20(){
		return URI.create("file://PESIIMPORT3/malesiana/vol20_final.xml");
	}
	
	//Flora Malesiana Series 2 - Vol 2
	public static URI fm_ser2_2(){
		return URI.create("file://PESIIMPORT3/malesiana/IIvol2_final.xml");
	}

	//Flora Malesiana Series 2 - Vol 3
	public static URI fm_ser2_3(){
		return URI.create("file://PESIIMPORT3/malesiana/IIvol3_final.xml");
	}

//************************* GABON ************************************************/
	
	//Flore du Gabon sample 
	public static URI fdg_sample(){
		return URI.create("file:/E:/opt/data/floreGabon/sample.xml");
	}

	//Flore du Gabon vol 1
	public static URI fdg_1(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol1_9.xml");
	}

	//Flore du Gabon vol 2
	public static URI fdg_2(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol2_9.xml");
	}
	
	//Flore du Gabon vol 3
	public static URI fdg_3(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol3_9.xml");
	}
	
	//Flore du Gabon vol 4
	public static URI fdg_4(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol4_9.xml");
	}
	
	//Flore du Gabon vol 5
	public static URI fdg_5(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol5_final.xml");
	}

	
	//Flore du Gabon vol 5
	public static URI fdg_5bis(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/5bis_final.xml");
	}
	
	//Flore du Gabon vol 6
	public static URI fdg_6(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol6_final.xml");
	}
	
	//Flore du Gabon vol 7
	public static URI fdg_7(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol7_final.xml");
	}
	
	//Flore du Gabon vol 8
	public static URI fdg_8(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol8_final.xml");
	}
	
	//Flore du Gabon vol 9
	public static URI fdg_9(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol9_final.xml");
	}
	
	//Flore du Gabon vol 10
	public static URI fdg_10(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol10_final.xml");
	}
	
	//Flore du Gabon vol 11
	public static URI fdg_11(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol11_final.xml");
	}

	//Flore du Gabon vol 12 and 17  (same family)
	public static URI fdg_12_17(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol12and17_final.xml");
	}


	//Flore du Gabon vol 13
	public static URI fdg_13(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol13_final.xml");
	}

	//Flore du Gabon vol 14
	public static URI fdg_14(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol14_final.xml");
	}

	//Flore du Gabon vol 15
	public static URI fdg_15(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol15_final.xml");
	}

	//Flore du Gabon vol 16
	public static URI fdg_16(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol16_final.xml");
	}

	//Flore du Gabon vol 17
	public static URI fdg_17(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol17_final.xml");
	}

	//Flore du Gabon vol 18
	public static URI fdg_18(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol18_final.xml");
	}

	//Flore du Gabon vol 19
	public static URI fdg_19(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol19_final.xml");
	}

	//Flore du Gabon vol 20
	public static URI fdg_20(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol20_final.xml");
	}
	
	//Flore du Gabon vol 21
	public static URI fdg_21(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol21_final.xml");
	}

	//Flore du Gabon vol 22
	public static URI fdg_22(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol22_final.xml");
	}

	//Flore du Gabon vol 27
	public static URI fdg_27(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol27_final.xml");
	}

	//Flore du Gabon vol 28
	public static URI fdg_28(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol28_final.xml");
	}

	//Flore du Gabon vol 30
	public static URI fdg_30(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol30_final.xml");
	}

	//Flore du Gabon vol 34
	public static URI fdg_34(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol34_final.xml");
	}
	
	//Flore du Gabon vol 35
	public static URI fdg_35(){
		return URI.create("file://PESIIMPORT3/gabon/markupData/fdgvol35_final.xml");
	}
	
//************************* GUIANAS **********************************************/	
	
	//Flora of the Guianas Sample
	public static URI fgu_1(){
		return URI.create("file://PESIIMPORT3/guianas/markupData/79THEOPHRASTACEAE.xml");
	}
	
}
