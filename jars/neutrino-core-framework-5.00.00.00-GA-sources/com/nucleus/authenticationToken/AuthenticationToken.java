package com.nucleus.authenticationToken;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public abstract class AuthenticationToken extends BaseEntity {

    private static final long serialVersionUID = 1196733183588786010L;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          tokenValidity;

    private String            tokenId;
    
    private Boolean isUsed;

    /**
     * @return the tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * @param tokenId the tokenId to set
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * @return the tokenValidity
     */
    public DateTime getTokenValidity() {
        return tokenValidity;
    }

    /**
     * @param tokenValidity the tokenValidity to set
     */
    public void setTokenValidity(DateTime tokenValidity) {
        this.tokenValidity = tokenValidity;
    }
    
    public Boolean getUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

}
