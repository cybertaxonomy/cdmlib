/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.Map;
import java.util.Set;

/**
 * Interface for converter that allow partitioned converting.
 * @author a.mueller
 *
 */
public interface IPartitionableConverter<IN extends IConverterInput<CsvStreamItem>, OUT extends IConverterOutput, OBJ extends Object> extends IConverter<IN, OUT, OBJ> {
	
	
	/**
	 * Returns those foreign keys included in the input streams partition separated by namespaces.
	 * @return
	 */
	//TODO make instream a more generic type of stream
	public Map<String, Set<String>> getPartitionForeignKeys(IReader<CsvStreamItem> instream);
	
	/**
	 * Returns a list of namespaces, which are required for related objects loading.
	 * @return
	 */
	public Set<String> requiredSourceNamespaces();
	
	
}
