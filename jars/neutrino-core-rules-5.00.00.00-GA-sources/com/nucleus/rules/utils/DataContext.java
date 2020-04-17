package com.nucleus.rules.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataContext extends HashMap<Object,Object>{

	private static final long serialVersionUID = -8112584500801344293L;

	private Map<String, DataEntryPopulator> dataPopulators = new HashMap<>();

	private Set<Object> internal = new HashSet<>();
	private Set<Object> external = new HashSet<>();

	private boolean executionStarted = false;
	private boolean internalCall = false;

	public DataContext(){}

	//private constructor for cloning
	private DataContext(DataContext source){
		super(source);
		this.dataPopulators = new HashMap<>(source.dataPopulators);
		this.internal = new HashSet<>(source.internal);
		this.external = new HashSet<>(source.external);
		this.executionStarted = source.executionStarted;
		this.internalCall = source.internalCall;
	}

	public void setExecutionStarted(boolean executionStarted) {
		this.executionStarted = executionStarted;
	}

	public void addDataPopulator(DataEntryPopulator dataPopulator, String... variables){
		if(variables != null){
			Arrays.stream(variables).forEach(v -> {
				if(dataPopulators.containsKey(v) && dataPopulators.get(v)!=dataPopulator){
					internal.remove(v);
					external.remove(v);
				}
				dataPopulators.put(v, dataPopulator);
			});
		}
	}

	@Override
	public Object get(Object key) {
		if(!internal.contains(key) && dataPopulators.containsKey(key)) {
			internalCall = true;
			try{
				dataPopulators.get(key).populate();
			}finally{
				internalCall = false;
			}
			return super.get(key);
		}
		return super.get(key);
	}

	@Override
	public Object put(Object key, Object value) {
		if(!executionStarted && !internalCall){
			return super.put(key, value);
		}
		if(!executionStarted && internalCall){
			internal.add(key);
			return super.put(key, value);
		}
		if(executionStarted && internalCall){
			if(external.contains(key)){
				return false;
			}else{
				internal.add(key);
				return super.put(key, value);
			}
		}
		if(executionStarted && !internalCall){
			external.add(key);
			return super.put(key, value);
		}
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key) || dataPopulators.containsKey(key);
	}

	public static void addToContext(DataEntryPopulator entryPopulator, Map<?,?> context, String... keys){
		if(context instanceof DataContext){
			((DataContext)context).addDataPopulator(entryPopulator, keys);
		}else{
			entryPopulator.populate();
		}
	}

	@Override
	public Object clone() {
		return new DataContext(this);
	}

}
