package org.lytsiware.clash.dto;

import java.io.Serializable;

public class NewPlayersUpdateDto implements Serializable {

	private static final long serialVersionUID = 1L;

	String tag;
	Boolean deleteChest;
	Boolean deleteCard;

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Boolean shouldDeleteChest() {
		return deleteChest;
	}
	public void setDeleteChest(Boolean deleteChest) {
		this.deleteChest = deleteChest;
	}
	public Boolean shouldDeleteCard() {
		return deleteCard;
	}
	public void setDeleteCard(Boolean deleteCard) {
		this.deleteCard = deleteCard;
	}
	

}
