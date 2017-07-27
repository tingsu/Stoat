package com.threedlite.urforms.data;


public class Data {

	private long id;
	private String entityName;
	private String attributeName;
	private long entityId;
	private String fieldValue;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}



	private static final String DELIM = "|";

	public String toString() {
		return id + "/" + entityName + DELIM + attributeName + DELIM + entityId 
				+ DELIM + fieldValue;
	}

}
