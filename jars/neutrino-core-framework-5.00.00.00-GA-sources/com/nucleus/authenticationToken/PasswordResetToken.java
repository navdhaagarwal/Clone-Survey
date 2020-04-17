package com.nucleus.authenticationToken;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@Entity
@DynamicUpdate
@DynamicInsert
public class PasswordResetToken extends AuthenticationToken {
	
	private static final long serialVersionUID = 1196901653588786010L;
	
	

}
