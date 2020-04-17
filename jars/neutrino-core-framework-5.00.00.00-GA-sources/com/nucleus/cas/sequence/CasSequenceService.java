/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cas.sequence;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.service.BaseService;

/**
 * @author Nucleus Software Exports Limited
 */
public interface CasSequenceService extends BaseService {

    public String generateNextApplicationNumber();

    public String generateNextProposalNumber();

    public String generateNextCollateralNumber();

    public String generateNextCustomerNumber();

    public String generateNextShowroomNumber();

    public String generateNextConsumerDurableNumber();
    
    public String generateNextInstaCardGroupNumber();

    String generateNextGenericWfNumber();
     /*
     * Returns the start application number and the end application numbers.
     */
    public String[] generateNextApplicationNumbersRange(int incrementBy);

    public String generateNextRequestCode();
	public String generateNextApplicationNumberConfig(String applicationConfig,String productType);

	//OverLoaded Method
    public String[] generateNextApplicationNumbersRange(String applicationConfig,String productType,int incrementBy);

    public boolean isNeoDefaultFormatConfig(ConfigurationVO appNumberFormatConfig);

    public String generateNextFasSimulationNumber();

}
