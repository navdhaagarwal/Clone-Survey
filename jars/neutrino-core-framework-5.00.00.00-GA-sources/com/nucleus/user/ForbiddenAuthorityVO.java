package com.nucleus.user;

import java.util.List;

import com.nucleus.authority.Authority;

public class ForbiddenAuthorityVO {
	
	private int degreeOfAcess;
	
	private List<Authority> forbiddenAuthorities;

	public ForbiddenAuthorityVO()
	{
		degreeOfAcess=-1;
	}
	public int getDegreeOfAcess() {
		return degreeOfAcess;
	}

	public void setDegreeOfAcess(int degreeOfAcess) {
		this.degreeOfAcess = degreeOfAcess;
	}

	public List<Authority> getForbiddenAuthorities() {
		return forbiddenAuthorities;
	}

	public void setForbiddenAuthorities(List<Authority> forbiddenAuthorities) {
		this.forbiddenAuthorities = forbiddenAuthorities;
	} 
	
	
	

}
