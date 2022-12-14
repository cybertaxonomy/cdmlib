/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ListReader wraps a list to implement IReader.
 *
 * @author a.mueller
 * @since 23.11.2011
 */
public class ListReader<TYPE extends Object> implements IReader<TYPE> {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private final List<TYPE> list;
	private int index = 0;

	public ListReader(List<TYPE> list) {
		this.list = new ArrayList<>();
		this.list.addAll(list);
	}

	@Override
	public TYPE read() {
		if (hasNext()){
			return list.get(index++);
		}else{
			return null;
		}
	}

	@Override
	public boolean hasNext(){
		return (index < list.size());
	}
}