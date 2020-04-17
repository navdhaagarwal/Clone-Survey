package com.nucleus.master.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.javers.core.diff.Diff;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MasterChangeDiffHolder implements Serializable {

	private List<String> deltaInString;
	
	@JsonIgnore
	private Diff delta; 
	
	private Map<String, MasterChangeDisJointEntiyDiffHolder> disJointEntityDiff;
	
	public Diff getDelta() {
		return delta;
	}

	public void setDelta(Diff delta) {
		this.delta = delta;
	}

	public List<String> getDeltaInString() {
		return deltaInString;
	}

	public void setDeltaInString(List<String> deltaInString) {
		this.deltaInString = deltaInString;
	}
	
	public void addDeltaInString(String deltaInString) {
		if(this.deltaInString == null){
			this.deltaInString = new ArrayList<>();
		};
		this.deltaInString.add(deltaInString);
	}
	
	
	public void addAllDeltaInString(List<String> deltaInString) {
		if(this.deltaInString == null){
			this.deltaInString = new ArrayList<>();
		};
		this.deltaInString.addAll(deltaInString);
	}

	public Map<String, MasterChangeDisJointEntiyDiffHolder> getDisJointEntityDiff() {
		return disJointEntityDiff;
	}

	public void setDisJointEntityDiff(Map<String, MasterChangeDisJointEntiyDiffHolder> disJointEntityDiff) {
		this.disJointEntityDiff = disJointEntityDiff;
	}
	
	public void addDisJointEntityDiff(String key, MasterChangeDisJointEntiyDiffHolder disJointEntityDiff) {
		this.disJointEntityDiff = Optional.ofNullable(this.disJointEntityDiff).orElse(new HashMap<>());
		this.disJointEntityDiff.put(key, disJointEntityDiff);
	}

	public MasterChangeDiffHolder(String deltaInString) {
		super();
		addDeltaInString(deltaInString);
	}

	public MasterChangeDiffHolder(Diff delta) {
		super();
		this.delta = delta;
	}
	
	public MasterChangeDiffHolder() {
		super();
	}
	
}
