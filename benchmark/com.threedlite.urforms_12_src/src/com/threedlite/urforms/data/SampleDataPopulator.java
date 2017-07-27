package com.threedlite.urforms.data;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;

public class SampleDataPopulator {
	
	public void addSampleData(UrSqlHelper sqlHelper) {
		SQLiteDatabase database = sqlHelper.getWritableDatabase();
		try {
			EntityDao entityDao = new EntityDao(database);
			AttributeDao attributeDao = new AttributeDao(database);
			DataDao dataDao = new DataDao(database);
			addSampleData(entityDao, attributeDao, dataDao);
		} finally {
			sqlHelper.close();
		}
	}
	
	
	private void addSampleData(EntityDao entityDao, AttributeDao attributeDao, DataDao dataDao) {
		
		Entity entity;
		Attribute attribute;

		entity = new Entity();
		entity.setName("task");
		entityDao.save(entity);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("name");
		attribute.setAttributeDesc("Name");
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("completed");
		attribute.setAttributeDesc("Completed");
		attribute.setDataType(Attribute.CHECKBOX_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("budget");
		attribute.setAttributeDesc("Budget");
		attribute.setDataType(Attribute.STRING_TYPE);
		attribute.setValidationRegex("[0-9]*\\.[0-9][0-9]");
		attribute.setValidationExample("999.99");
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("priority");
		attribute.setAttributeDesc("Priority");
		attribute.setDataType(Attribute.CHOICES_TYPE);
		attribute.setChoices("high|High,medium|Medium,low|Low");
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("comment");
		attribute.setAttributeDesc("Comment");
		attribute.setDataType(Attribute.STRING_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("meeting");
		attribute.setAttributeDesc("Meeting");
		attribute.setDataType(Attribute.REF_TYPE);
		attribute.setRefEntityName("meeting");
		attribute.setListable(false);
		attribute.setSearchable(false);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("ts");
		attribute.setAttributeDesc("Edit Timestamp");
		attribute.setDataType(Attribute.EDIT_TIMESTAMP_TYPE);
		attributeDao.save(attribute);

		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "Restock");
		map.put("completed", ""+true);
		map.put("budget", "123.45");
		map.put("comment", "The quick brown fox jumps over the lazy dog.");
		map = dataDao.saveEntityData("task", map);

		map = dataDao.getEntityDataById("task", Long.valueOf(map.get(DataDao.ID)));
		if (map.size() == 0) throw new RuntimeException("Unable to retrieve data 1");

		map = new HashMap<String, String>();
		map.put("name", "Test");
		map.put("completed", ""+false);
		map.put("budget", "-1234.45");
		map.put("comment", "A man, a plan, a canal, Panama.");
		map = dataDao.saveEntityData("task", map);

		map = dataDao.getEntityDataById("task", Long.valueOf(map.get(DataDao.ID)));
		if (map.size() == 0) throw new RuntimeException("Unable to retrieve data 2");
		
		map = new HashMap<String, String>();
		map.put("name", "Test2");
		map.put("completed", ""+false);
		map.put("budget", "0");
		map.put("comment", "plan");
		map = dataDao.saveEntityData("task", map);
		

		entity = new Entity();
		entity.setName("meeting");
		entityDao.save(entity);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("description");
		attribute.setAttributeDesc("Description");
		attribute.setEntityDescription(true);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("location");
		attribute.setAttributeDesc("Location");
		attribute.setEntityDescription(true);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("type");
		attribute.setAttributeDesc("Type");
		attribute.setDataType(Attribute.CHOICES_TYPE);
		attribute.setChoices("personal,work");
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("date");
		attribute.setAttributeDesc("Date");
		attribute.setEntityDescription(true);
		attribute.setDataType(Attribute.DATE_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("time");
		attribute.setAttributeDesc("Time");
		attribute.setEntityDescription(true);
		attribute.setDataType(Attribute.TIME_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("persons");
		attribute.setAttributeDesc("People List");
		attribute.setDataType(Attribute.REF_BY_TYPE);
		attribute.setRefEntityName("person_x_meeting meeting");
		attribute.setListable(false);
		attributeDao.save(attribute);


		
		
		map = new HashMap<String, String>();
		map.put("description", "Start meeting");
		map.put("location", "123B");
		map.put("date", "2012-09-01");
		map.put("time", "11:30");
		map = dataDao.saveEntityData("meeting", map);

		map = new HashMap<String, String>();
		map.put("description", "Review meeting");
		map.put("location", "456C");
		map.put("date", "2012-10-01");
		map.put("time", "14:40");
		map = dataDao.saveEntityData("meeting", map);


		
		
		entity = new Entity();
		entity.setName("person");
		entityDao.save(entity);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("firstName");
		attribute.setAttributeDesc("First Name");
		attribute.setEntityDescription(true);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("lastName");
		attribute.setAttributeDesc("Last Name");
		attribute.setEntityDescription(true);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("file");
		attribute.setAttributeDesc("File");
		attribute.setDataType(Attribute.FILE_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("picture");
		attribute.setAttributeDesc("Picture");
		attribute.setDataType(Attribute.IMAGE_TYPE);
		attributeDao.save(attribute);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("meetings");
		attribute.setAttributeDesc("Meeting List");
		attribute.setDataType(Attribute.REF_BY_TYPE);
		attribute.setRefEntityName("person_x_meeting person");
		attribute.setListable(false);
		attributeDao.save(attribute);


		map = new HashMap<String, String>();
		map.put("firstName", "Joe");
		map.put("lastName", "Smith");
		map = dataDao.saveEntityData("person", map);

		map = new HashMap<String, String>();
		map.put("firstName", "Mary");
		map.put("lastName", "Jones");
		map = dataDao.saveEntityData("person", map);


		
		entity = new Entity();
		entity.setName("person_x_meeting");
		entityDao.save(entity);

		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("person");
		attribute.setAttributeDesc("Person");
		attribute.setEntityDescription(true);
		attribute.setDataType(Attribute.REF_TYPE);
		attribute.setRefEntityName("person");
		attribute.setPrimaryKeyPart(true);
		attributeDao.save(attribute);
		
		attribute = new Attribute();
		attribute.setEntityName(entity.getName());
		attribute.setAttributeName("meeting");
		attribute.setAttributeDesc("Meeting");
		attribute.setEntityDescription(true);
		attribute.setDataType(Attribute.REF_TYPE);
		attribute.setRefEntityName("meeting");
		attribute.setPrimaryKeyPart(true);
		attributeDao.save(attribute);
		
		map = new HashMap<String, String>();
		map.put("person", "1");
		map.put("meeting", "1");
		map = dataDao.saveEntityData("person_x_meeting", map);

		map = new HashMap<String, String>();
		map.put("person", "2");
		map.put("meeting", "1");
		map = dataDao.saveEntityData("person_x_meeting", map);

		
		
	}

}
