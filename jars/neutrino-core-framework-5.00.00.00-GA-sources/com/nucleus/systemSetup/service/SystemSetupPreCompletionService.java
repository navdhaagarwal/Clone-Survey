package com.nucleus.systemSetup.service;

import com.nucleus.core.FrameworkServiceLocator;

/**
 * The Interface SystemSetupPreCompletionService for providing methods for execution. The implementation of this
 * interface would be located by {@link FrameworkServiceLocator} at runtime.
 */
public interface SystemSetupPreCompletionService {

    void execute();

}
