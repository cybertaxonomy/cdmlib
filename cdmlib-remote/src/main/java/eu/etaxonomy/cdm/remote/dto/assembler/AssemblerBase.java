package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.UUID;

public abstract class AssemblerBase {
	
	public String getRandomUUID(){
		return UUID.randomUUID().toString();
	}
	public UUID getUUID(String uuid){
		return UUID.fromString(uuid);
	}
}
