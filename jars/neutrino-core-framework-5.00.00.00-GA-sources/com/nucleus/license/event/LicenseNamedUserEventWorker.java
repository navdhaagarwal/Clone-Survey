package com.nucleus.license.event;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;

public class LicenseNamedUserEventWorker extends NeutrinoEventWorker {

	private String eventType;
	
	public LicenseNamedUserEventWorker(String name) {
		super(name);
	}
	 public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
		 LicenseNamedUserEvent event = new LicenseNamedUserEvent(publisher,eventType, this);
		 return event;
         }


		public String getEventType() {
			return eventType;
		}
		public void setEventType(String eventType) {
			this.eventType = eventType;
		}
}
