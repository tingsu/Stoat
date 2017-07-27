package com.threedlite.urforms.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Validator {
	
	private UrSqlHelper sqlHelper;
	private Entity entity;
	
	public Validator(UrSqlHelper sqlHelper, Entity entity) {
		this.sqlHelper = sqlHelper;
		this.entity = entity;
	}
	
	public String validate(List<Attribute> attributes, Map<String, String> values) {

		List<Attribute> keys = new ArrayList<Attribute>();
		for (Attribute attribute: attributes) {

			String value = values.get(attribute.getAttributeName());
			if (value == null || value.trim().length() == 0) value = null;

			if (attribute.isRequired() && value == null) return attribute.getAttributeName() + " is required.";

			String regex = attribute.getValidationRegex();
			if (regex == null || regex.trim().length() == 0) regex = null;

			if (value != null && regex != null) {
				boolean matches = Pattern.matches(regex, value);
				if (!matches) {
					return attribute.getAttributeName() + " value " + value + " is invalid.  Example: "+attribute.getValidationExample();
				}
			}

			if (attribute.isPrimaryKeyPart()) {
				keys.add(attribute);
			}

		}

		// Check unique constraint
		if (keys.size() > 0) {
			Map<String, String> checkValues = new HashMap<String, String>();
			for (Attribute key: keys) {
				checkValues.put(key.getAttributeName(), values.get(key.getAttributeName()));
			}
			List<Map<String, String>> results;
			DataDao dataDao = new DataDao(sqlHelper.getWritableDatabase());
			try {
				results = dataDao.search(entity, checkValues);
			} finally {
				sqlHelper.close();
			}
			if (results.size() > 0) {
				String message = "A record already exists for ";
				for (Attribute key: keys) {
					message += key.getAttributeName() + " = " + values.get(key.getAttributeName()) + "  ";
				}
				return message;
			}
		}

		return null;
	}

	
}
