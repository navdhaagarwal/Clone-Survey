package com.nucleus.security.core.session;

import org.springframework.security.core.session.SessionInformation;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class ModuleConcurrencyUserData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*private Long activeSessionCount;*/

	private CopyOnWriteArraySet<NeutrinoSessionInformation> lowPrioritySessions;

	private CopyOnWriteArraySet<NeutrinoSessionInformation> highPrioritySessions;

	/**
	 * Getter for property 'lowPrioritySessions'.
	 *
	 * @return Value for property 'lowPrioritySessions'.
	 */
	public CopyOnWriteArraySet<NeutrinoSessionInformation> getLowPrioritySessions() {
		return lowPrioritySessions;
	}

	/**
	 * Setter for property 'lowPrioritySessions'.
	 *
	 * @param lowPrioritySessions Value to set for property 'lowPrioritySessions'.
	 */
	public void setLowPrioritySessions(CopyOnWriteArraySet<NeutrinoSessionInformation> lowPrioritySessions) {
		this.lowPrioritySessions = lowPrioritySessions;
	}

	/**
	 * Getter for property 'highPrioritySessions'.
	 *
	 * @return Value for property 'highPrioritySessions'.
	 */
	public CopyOnWriteArraySet<NeutrinoSessionInformation> getHighPrioritySessions() {
		return highPrioritySessions;
	}

	/**
	 * Setter for property 'highPrioritySessions'.
	 *
	 * @param highPrioritySessions Value to set for property 'highPrioritySessions'.
	 */
	public void setHighPrioritySessions(CopyOnWriteArraySet<NeutrinoSessionInformation> highPrioritySessions) {
		this.highPrioritySessions = highPrioritySessions;
	}
}
