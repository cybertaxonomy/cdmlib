package eu.etaxonomy.cdm.remoting.httpinvoker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DtoList implements Serializable {

	private List<Dto> dtoList = new ArrayList<Dto>();

	public List<Dto> getDtoList() {
		return dtoList;
	}

	public void setDtoList(List<Dto> dtoList) {
		this.dtoList = dtoList;
	}
	
}
