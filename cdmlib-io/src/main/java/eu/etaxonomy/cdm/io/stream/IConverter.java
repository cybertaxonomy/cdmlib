/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Interface for all converters from some input to some output.
 * Supports mapping of input to output and identifier identification.
 *
 * @author a.mueller
 * @since 23.11.2011
 *
 */
public interface IConverter<IN extends IConverterInput, OUT extends IConverterOutput, SOURCE_ID extends Object> {

	public IReader<MappedCdmBase<? extends CdmBase>> map(IN item);

	/**
	 * Returns the identifier (if any) of the input
	 * @param item
	 * @return
	 */
	public SOURCE_ID getSourceId(IN item);
}
