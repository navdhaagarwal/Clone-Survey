package com.nucleus.core.email;

/**
 * 
 * @author gajendra.jatav
 *
 */
public enum EmailOperation {

	DEL_FROM_INBOX(1L),DEL_FROM_OUTBOX(2L),DEL_FROM_TRASH(3L);

	private Long value;

	public Long getValue() {
		return value;
	}

	private EmailOperation(Long value) {
		this.value = value;
	}

	public static EmailOperation fromValue(Long value) throws IllegalArgumentException {
		for (EmailOperation emailOperation : values()) {
			if (emailOperation.value == value) {
				return emailOperation;
			}
		}
		throw new IllegalArgumentException("Unknown enum value :" + value);
	}
	
}
