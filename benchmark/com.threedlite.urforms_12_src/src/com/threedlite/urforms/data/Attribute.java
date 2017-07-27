package com.threedlite.urforms.data;

public class Attribute {
	
	public static final String STRING_TYPE = "string";
	public static final String CHECKBOX_TYPE = "checkbox";
	public static final String CHOICES_TYPE = "choices";
	public static final String DATE_TYPE = "date";
	public static final String TIME_TYPE = "time";
	public static final String REF_TYPE = "entityRef";
	public static final String REF_BY_TYPE = "entityRefBy";
	public static final String EDIT_TIMESTAMP_TYPE = "editTimestamp";
	public static final String FILE_TYPE = "file";
	public static final String IMAGE_TYPE = "image";

	public static final String[] DATA_TYPES = new String[]
			{STRING_TYPE, CHECKBOX_TYPE, DATE_TYPE, TIME_TYPE, CHOICES_TYPE
		 , REF_TYPE, REF_BY_TYPE, EDIT_TIMESTAMP_TYPE
		 , FILE_TYPE, IMAGE_TYPE};
	
	public static final String[] DATA_DESCS = new String[]
			{"String", "Checkbox", "Date", "Time", "Choices"
		 , "Reference", "ReferencedBy", "Edit Timestamp"
		 , "File", "Image"};


	private long id;
	private String entityName;
	private String attributeName;
	private String attributeDesc = "";
	private String dataType  = STRING_TYPE; 
	private String refEntityName = null;
	private boolean isPrimaryKeyPart = false;
	private boolean isRequired = false;
	private boolean isSearchable = true;
	private boolean isListable = true;
	private boolean isEntityDescription = false;
	private int displayOrder = 0;
	private String choices = null;
	private String validationRegex = null;
	private String validationExample = null;
	

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

	public String getAttributeDesc() {
		return attributeDesc;
	}

	public void setAttributeDesc(String attributeDesc) {
		this.attributeDesc = attributeDesc;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String type) {
		this.dataType = type;
	}

	public String getRefEntityName() {
		return refEntityName;
	}

	public void setRefEntityName(String refEntityName) {
		this.refEntityName = refEntityName;
	}

	public boolean isPrimaryKeyPart() {
		return isPrimaryKeyPart;
	}

	public void setPrimaryKeyPart(boolean isPrimaryKeyPart) {
		this.isPrimaryKeyPart = isPrimaryKeyPart;
	}
	
	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	
	public boolean isSearchable() {
		return isSearchable;
	}

	public void setSearchable(boolean isSearchable) {
		this.isSearchable = isSearchable;
	}

	public boolean isListable() {
		return isListable;
	}

	public void setListable(boolean isListable) {
		this.isListable = isListable;
	}

	public boolean isEntityDescription() {
		return isEntityDescription;
	}

	public void setEntityDescription(boolean isEntityDescription) {
		this.isEntityDescription = isEntityDescription;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	
	public String getChoices() {
		return choices;
	}

	public void setChoices(String choices) {
		this.choices = choices;
	}

	public String getValidationRegex() {
		return validationRegex;
	}

	public void setValidationRegex(String validationRegex) {
		this.validationRegex = validationRegex;
	}
	
	public String getValidationExample() {
		return validationExample;
	}

	public void setValidationExample(String validationExample) {
		this.validationExample = validationExample;
	}

	public String toString() {
		return attributeName;
	}


}
