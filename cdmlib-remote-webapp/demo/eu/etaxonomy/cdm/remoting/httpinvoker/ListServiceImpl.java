package eu.etaxonomy.cdm.remoting.httpinvoker;

import java.util.List;

import org.apache.log4j.Logger;

public class ListServiceImpl implements ListService {

	DtoList dtoList;
	public static final Logger logger = Logger.getLogger(ListServiceImpl.class);
	
	@Override
	public DtoList getList() {
		List<Dto> list = dtoList.getDtoList();
		for (Dto dto: list) {
			logger.info("Server send back to client: " + dto.getText() + " " + dto.getNumber());
		}
		return this.dtoList;
	}

	@Override
	public void setList(DtoList dtoList) {
		this.dtoList=dtoList;
		List<Dto> list = dtoList.getDtoList();
		for (Dto dto: list) {
			logger.info("Server received from client: " + dto.getText() + " " + dto.getNumber());
		}

	}

}
