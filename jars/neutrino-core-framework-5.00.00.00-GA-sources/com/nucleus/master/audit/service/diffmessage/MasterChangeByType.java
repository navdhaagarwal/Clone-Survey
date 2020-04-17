package com.nucleus.master.audit.service.diffmessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.SetChange;
import org.javers.core.diff.changetype.map.MapChange;

public class MasterChangeByType implements Serializable{

	List<NewObject> newObject;
	
	List<ObjectRemoved> removeObject;
	
	List<ValueChange> valueChanges;
	
	List<ReferenceChange> refChange;
	
	List<ListChange> listChange;
	
	List<SetChange> setChanges;

	List<MapChange> mapChanges;

	public List<NewObject> getNewObject() {
		return newObject;
	}

	public void addNewObject(NewObject newObject) {
		if(this.newObject == null){
			this.newObject = new ArrayList<>();
		};
		this.newObject.add(newObject);
	}

	public List<ObjectRemoved> getRemoveObject() {
		return removeObject;
	}

	public void addRemoveObject(ObjectRemoved removeObject) {
		if( this.removeObject == null){
			this.removeObject = new ArrayList<>();
		} 
		this.removeObject.add(removeObject);
	}

	public List<ValueChange> getValueChanges() {
		return valueChanges;
	}

	public void addValueChanges(ValueChange valueChange) {
		if( this.valueChanges == null){
			this.valueChanges = new ArrayList<>();
		} 
		this.valueChanges.add(valueChange);
	}

	public List<ReferenceChange> getRefChange() {
		return refChange;
	}

	public void addRefChange(ReferenceChange refChange) {
		if( this.refChange ==null){
			this.refChange = new ArrayList<>();
		}
		this.refChange.add(refChange);
	}

	public List<ListChange> getListChange() {
		return listChange;
	}

	public void setListChange(List<ListChange> listChange) {
		this.listChange = listChange;
	}
	
	public void addListChange(ListChange listChange) {
		if(this.listChange == null){
			this.listChange = new ArrayList<>();
		}
		this.listChange.add(listChange);
	}

	public List<SetChange> getSetChanges() {
		return setChanges;
	}

	public void addSetChanges(SetChange setChanges) {
		if(this.setChanges == null){
			this.setChanges = new ArrayList<>();
		}
		this.setChanges.add(setChanges);
	}

	public List<MapChange> getMapChanges() {
		return mapChanges;
	}

	public void addMapChanges(MapChange mapChanges) {
		if(this.mapChanges == null){
			this.mapChanges = new ArrayList<>();
		}
		this.mapChanges.add(mapChanges);
	}
}
