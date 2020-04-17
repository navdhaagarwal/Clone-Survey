package com.nucleus.master.audit;

import java.io.Serializable;

import org.javers.core.diff.Diff;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MasterChangeDisJointEntiyDiffHolder implements Serializable{

	@JsonIgnore
	private Diff delta;

	public Diff getDelta() {
		return delta;
	}

	public void setDelta(Diff delta) {
		this.delta = delta;
	}

	public MasterChangeDisJointEntiyDiffHolder(Diff delta) {
		super();
		this.delta = delta;
	}

	public MasterChangeDisJointEntiyDiffHolder() {
		super();
	}
	
	
	
}
