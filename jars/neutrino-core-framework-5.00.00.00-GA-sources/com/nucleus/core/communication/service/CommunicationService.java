/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.communication.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.nucleus.core.communication.Communication;
import com.nucleus.document.core.entity.Document;

/**
 * The Interface CommunicationService.
 *
 * @author Nucleus Software Exports Limited
 *
 */
public interface CommunicationService {

    /**
     * This method saves the communication.
     *
     * @param communication the communication
     */
    void saveCommunication(Communication communication);

    /**
     * This method gets all the communication types.
     *
     * @return the communication types
     */
    List<Map<Integer, String>> getCommunicationTypes();

    /**
     * This method gets the all the communications that are related to the mentioned owner Entity Uri.
     *
     * @param ownerEntityUri the owner entity uri
     * @return the all communication by uri
     */
    List<Communication> getAllCommunicationByUri(String ownerEntityUri);
    
    
    List<Communication> getAllCommunicationByUriList(List<String> ownerEntityUri);
    
   
    /**
     * Persist email communication.- Used when the document is read from resource folder and communication needs to be persist
     * Called from Notification Master Service
     *
     * @param documentToPersist the document to persist
     * @param uploadedFileName the uploaded file name
     * @param ownerEntityUri the owner entity uri
     * @param contextMap the context map
     * @return the document
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Document persistEmailCommunication(byte[] documentToPersist, String uploadedFileName,String ownerEntityUri ,Map contextMap  )throws IOException;

    Document persistEmailCommunication(byte[] documentToPersist, String uploadedFileName,String ownerEntityUri ,Map contextMap ,String suffix )throws IOException;

    Document persistEmailCommunication(byte[] documentToPersist, String uploadedFileName,String ownerEntityUri ,Map contextMap ,String suffix,String tempName )throws IOException;
    

    /**
     * This method gets the all the communications that are related to the mentioned owner Entity Uri in the descending order of last updated time stamp.
     * If last updated time stamp in null then creation time stamp is checked.
     *
     * @param ownerEntityUri the owner entity uri
     * @return the all communication by uri
     */
    List<Communication> getOtherThanAppointmentCommunicationOrderByCreationTimeStamp(String ownerEntityUri);
    List<Communication> getAppointmentFromCommunicationHistory(String ownerEntityUri);

	void persistDynamicGeneratedEmailAttachment(byte[] documentToPersist,File attachFile, String ownerEntityUri, Map contextMap,String suffix,String extension) throws IOException;

}
