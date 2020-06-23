/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @since 06.08.2009
 */
public class ParsedBaseMatchStrategy extends DefaultMatchStrategy implements IParsedMatchStrategy {

    private static final long serialVersionUID = 1253144282030211050L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParsedBaseMatchStrategy.class);

	final static UUID uuid = UUID.fromString("4e2efeca-96a3-4894-80f4-f1015295f059");

	public static ParsedBaseMatchStrategy NewInstance(Class<? extends IMatchable> matchClazz){
		return new ParsedBaseMatchStrategy(matchClazz);
	}

    protected ParsedBaseMatchStrategy(Class<? extends IMatchable> matchClazz) {
		super(matchClazz);
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    protected void preInitMapping() {
	    defaultMatchMode = DEFAULT_PARSED_MATCH_MODE;
        defaultCollectionMatchMode = DEFAULT_PARSED_COLLECTION_MATCH_MODE;
        defaultMatchMatchMode = DEFAULT_PARSED_MATCH_MATCH_MODE;
	}

//
//	@Override
//    public Matching getMatching() {
//		return matching;
//	}
//
//
//	/**
//	 * @param class1
//	 */
//	private void initializeSubclass(Class<? extends IMatchable> instanceClass) {
//		Map<String, Field> subClassFields = CdmUtils.getAllFields(instanceClass, matchClass, false, false, true, false);
//		for (Field field: subClassFields.values()){
//			initField(field, true);
//		}
//	}

}
