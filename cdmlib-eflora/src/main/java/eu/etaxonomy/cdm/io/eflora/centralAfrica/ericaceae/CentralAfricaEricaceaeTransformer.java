// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.EfloraTransformer;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class CentralAfricaEricaceaeTransformer extends EfloraTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CentralAfricaEricaceaeTransformer.class);
	
	//Languages
	private static final UUID uuidKinyarwanda = UUID.fromString("95d170f5-7654-42bf-8293-c3584191a45a");
	private static final UUID uuidKibemba = UUID.fromString("6069b231-101a-4a7b-84c5-4116b92db99c");
	private static final UUID uuidMashi = UUID.fromString("fca40807-6b89-49c9-9a4a-9e0b7a928309");
	private static final UUID uuidKihavu = UUID.fromString("050821ee-fac0-4c82-837d-f0a3d2206eb3");
	private static final UUID uuidKinande = UUID.fromString("9ba45e39-86af-4200-b578-c45f8425acad");
	private static final UUID uuidKihunde = UUID.fromString("8077644c-6deb-48de-a9d0-a649201184ed");
	private static final UUID uuidKiluba = UUID.fromString("a90ac2df-3391-4cf7-91c5-b32c2f32a068");
	private static final UUID uuidKitabwa = UUID.fromString("a8008e51-01a7-4432-8bf7-4b18fa60a1c7");
	private static final UUID uuidKibatwa = UUID.fromString("b0130472-3ab1-4bb9-9605-0bf7b3d5c798");
	private static final UUID uuidKinyanga = UUID.fromString("706661cb-0086-4fd7-a421-7476850b34f9");
	private static final UUID uuidKirundi = UUID.fromString("089cab72-d673-42de-83e8-d20ff6937986");
	private static final UUID uuidKinyindu = UUID.fromString("e4e405fe-4ff0-46b9-bd1e-bf09d1a6f3a9");
	private static final UUID uuidKifulero = UUID.fromString("6cadd25c-b2f3-4d5b-a44e-cb88d0f184fe");
	private static final UUID uuidKitembo = UUID.fromString("09a7da83-0e1f-42ae-886b-88675800d245");
	private static final UUID uuidKinyabongo = UUID.fromString("cae69a27-77f9-46db-b7ea-646c0c037cfe");

	
	@Override
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		
		}else if (key.equalsIgnoreCase("Kinyarwanda")){return uuidKinyarwanda;
		}else if (key.equalsIgnoreCase("Kibemba")){return uuidKibemba;
		}else if (key.equalsIgnoreCase("Mashi")){return uuidMashi;
		}else if (key.equalsIgnoreCase("Kihavu")){return uuidKihavu;
		}else if (key.equalsIgnoreCase("Kinande")){return uuidKinande;
		}else if (key.equalsIgnoreCase("Kihunde")){return uuidKihunde;
		}else if (key.equalsIgnoreCase("Kiluba")){return uuidKiluba;
		}else if (key.equalsIgnoreCase("Kitabwa")){return uuidKitabwa;
		}else if (key.equalsIgnoreCase("Viele ")){return uuidKibatwa;
		}else if (key.equalsIgnoreCase("Kinyanga")){return uuidKinyanga;
		}else if (key.equalsIgnoreCase("Kirundi")){return uuidKirundi;
		}else if (key.equalsIgnoreCase("Kinyindu")){return uuidKinyindu;
		}else if (key.equalsIgnoreCase("Kifulero")){return uuidKifulero;
		}else if (key.equalsIgnoreCase("Kitembo")){return uuidKitembo;
		}else if (key.equalsIgnoreCase("Kinyabongo")){return uuidKinyabongo;
		}else{
			return null;
		}
	}
	
}
