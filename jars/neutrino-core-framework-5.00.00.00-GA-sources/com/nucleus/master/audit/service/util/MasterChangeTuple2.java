package com.nucleus.master.audit.service.util;

import java.io.Serializable;

public class MasterChangeTuple2<K,V> implements Serializable {

	private K _1;
	
	private V _2;

	public K get_1() {
		return _1;
	}

	public void set_1(K _1) {
		this._1 = _1;
	}

	public V get_2() {
		return _2;
	}

	public void set_2(V _2) {
		this._2 = _2;
	}

	public MasterChangeTuple2(K _1, V _2) {
		super();
		this._1 = _1;
		this._2 = _2;
	}
	
	
}
