package eu.etaxonomy.cdm.io.pesi.merging;

public class FaunaEuErmsMerging {
	
	private String idInFaunaEu;
	private String idInErms;
	
	private String nameCacheInFaunaEu;
	private String nameCacheInErms;
	
	private boolean statInFaunaEu;
	private boolean statInErms;
	
	private String authorInFaunaEu;
	private String authorInErms;
	
	private String rankInFaunaEu;
	private String rankInErms;
	
	private String phylumInFaunaEu;
	private String phylumInErms;
	
	private String parentStringInFaunaEu;
	private String parentStringInErms;
	
	private String parentRankStringInFaunaEu;
	private String parentRankStringInErms;
	
	
	public String getParentRankStringInFaunaEu() {
		return parentRankStringInFaunaEu;
	}

	public void setParentRankStringInFaunaEu(String parentRankStringInFaunaEu) {
		this.parentRankStringInFaunaEu = parentRankStringInFaunaEu;
	}

	public String getParentRankStringInErms() {
		return parentRankStringInErms;
	}

	public void setParentRankStringInErms(String parentRankStringInErms) {
		this.parentRankStringInErms = parentRankStringInErms;
	}

	public String getParentStringInFaunaEu() {
		return parentStringInFaunaEu;
	}

	public void setParentStringInFaunaEu(String parentStringInFaunaEu) {
		this.parentStringInFaunaEu = parentStringInFaunaEu;
	}

	public String getParentStringInErms() {
		return parentStringInErms;
	}

	public void setParentStringInErms(String parentStringInErms) {
		this.parentStringInErms = parentStringInErms;
	}

	public String getRankInFaunaEu() {
		return rankInFaunaEu;
	}

	public void setRankInFaunaEu(String rankInFaunaEu) {
		this.rankInFaunaEu = rankInFaunaEu;
	}

	public String getPhylumInFaunaEu() {
		return phylumInFaunaEu;
	}

	public void setPhylumInFaunaEu(String phylumInFaunaEu) {
		this.phylumInFaunaEu = phylumInFaunaEu;
	}

	public String getPhylumInErms() {
		return phylumInErms;
	}

	public void setPhylumInErms(String phylumInErms) {
		this.phylumInErms = phylumInErms;
	}

	public String getRankInErms() {
		return rankInErms;
	}

	public void setRankInErms(String rankInErms) {
		this.rankInErms = rankInErms;
	}
	
	
	public static FaunaEuErmsMerging newInstance(){
		return new FaunaEuErmsMerging();
		
	}
	
	public boolean isStatInFaunaEu() {
		return statInFaunaEu;
	}
	public void setStatInFaunaEu(boolean statInFaunaEu) {
		this.statInFaunaEu = statInFaunaEu;
	}
	public boolean isStatInErms() {
		return statInErms;
	}
	public void setStatInErms(boolean statInErms) {
		this.statInErms = statInErms;
	}
	public String getAuthorInFaunaEu() {
		return authorInFaunaEu;
	}
	public void setAuthorInFaunaEu(String authorInFaunaEu) {
		this.authorInFaunaEu = authorInFaunaEu;
	}
	public String getAuthorInErms() {
		return authorInErms;
	}
	public void setAuthorInErms(String authorInErms) {
		this.authorInErms = authorInErms;
	}
	public String getIdInFaunaEu() {
		return idInFaunaEu;
	}
	public void setIdInFaunaEu(String idInFaunaEu) {
		this.idInFaunaEu = idInFaunaEu;
	}
	public String getIdInErms() {
		return idInErms;
	}
	public void setIdInErms(String idInErms) {
		this.idInErms = idInErms;
	}
	public String getNameCacheInFaunaEu() {
		return nameCacheInFaunaEu;
	}
	public void setNameCacheInFaunaEu(String nameCacheInFaunaEu) {
		this.nameCacheInFaunaEu = nameCacheInFaunaEu;
	}
	public String getNameCacheInErms() {
		return nameCacheInErms;
	}
	public void setNameCacheInErms(String nameCacheInErms) {
		this.nameCacheInErms = nameCacheInErms;
	}

	
	
}
