/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream.mapping;


/**
 * @author a.mueller
 * @since 19.03.2012
 *
 */
public class MappingEntry<SOURCE_NS extends Object, SOURCE_KEY extends Object, 
			DEST_NS extends Object, DEST_KEY extends Object> {
	
	SOURCE_NS namespace;
	SOURCE_KEY sourceKey;
	DEST_NS destinationNamespace;
	DEST_KEY destinationId;
	
	public MappingEntry(SOURCE_NS namespace, SOURCE_KEY sourceKey,
			DEST_NS destinationNamespace, DEST_KEY destinationKey) {
		super();
		this.namespace = namespace;
		this.sourceKey = sourceKey;
		this.destinationNamespace = destinationNamespace;
		this.destinationId = destinationKey;
	}
	
	public DEST_NS getDestinationNamespace() {
		return destinationNamespace;
	}


	public DEST_KEY getDestinationId() {
		return destinationId;
	}
	
	public SOURCE_NS getNamespace() {
		return namespace;
	}

	public SOURCE_KEY getSourceKey() {
		return sourceKey;
	}

	@Override
	public String toString(){
		return "[" + String.valueOf(namespace) + "." + String.valueOf(sourceKey) + "->" +  
					String.valueOf(destinationNamespace) + "." + String.valueOf(destinationId) + "]";
	}
		
	
}
