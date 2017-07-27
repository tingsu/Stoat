package com.threedlite.urforms.data;

import java.util.List;
import java.util.Map;

public class Entity {

	private long id;
	private String name;
	
	private List<Attribute> attributes; // not auto-populated
	private Map<String, String> values; // not auto-populated
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public String toString() {
		return name;
	}
	
	

}
