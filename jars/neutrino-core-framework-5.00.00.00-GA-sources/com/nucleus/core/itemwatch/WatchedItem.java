/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.itemwatch;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * This entity keeps a record of any entity being 'watched' and its watchers.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="watched_Item_index_entityUri",columnList="entityUri")})
public class WatchedItem extends BaseEntity {

    private static final long serialVersionUID = 6883446842959551675L;
    
    private String            entityUri;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "watched_item_watchers")
    private Set<String>       userUris;

    /**
     * Gets the entity uri.
     *
     * @return the entityUri
     */
    public String getEntityUri() {
        return entityUri;
    }

    /**
     * Sets the entity uri.
     *
     * @param entityUri the entityUri to set
     */
    public void setEntityUri(String entityUri) {
        this.entityUri = entityUri;
    }

    /**
     * Gets the user uris.
     *
     * @return the userUri
     */
    public Set<String> getUserUris() {
        return userUris;
    }

    /**
     * Sets the user uris.
     *
     * @param userUris the new user uris
     */
    public void setUserUris(Set<String> userUris) {
        this.userUris = userUris;
    }

    /**
     * Adds the user uri.
     *
     * @param userUri the user uri
     */
    public void addUserUri(String userUri) {
        if (isEmpty(userUris)) {
            userUris = new HashSet<String>();
        }
        userUris.add(userUri);
    }

    /**
     * Delete user uri.
     *
     * @param userUri the user uri
     */
    public void deleteUserUri(String userUri) {
        if (!isEmpty(userUris)) {
            userUris.remove(userUri);
        }
    }

}
