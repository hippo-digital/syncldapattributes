package com.southdevon.trust.dto;

public class UCSLdapUser 
{

	private String userDN;
	private String esrEmployeeNumber;
	public String getUserDN() {
		return userDN;
	}
	public void setUserDN(String userDN) {
		this.userDN = userDN;
	}
	public String getEsrEmployeeNumber() {
		return esrEmployeeNumber;
	}
	public void setEsrEmployeeNumber(String esrEmployeeNumber) {
		this.esrEmployeeNumber = esrEmployeeNumber;
	}
	@Override
	public String toString() {
		return "UCSLdapUser [userDN=" + userDN + ", esrEmployeeNumber=" + esrEmployeeNumber + "]";
	}
	
	
}
