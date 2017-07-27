package com.threedlite.urforms.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AttributeDao {
	
	private SQLiteDatabase database;

	public AttributeDao(SQLiteDatabase database) {
		this.database = database;
	}
	
	public static final String TABLE_CREATE = 
			"create table entity_attribute (" +
			"_id integer primary key autoincrement" +
			", entity_name text not null" +
			", attribute_name text not null" +
			", attribute_desc text not null" +
			", data_type text not null" +
			", ref_entity_name text" +
			", is_primary_key_part text not null" +
			", is_required text not null" +
			", is_searchable text not null" +       
			", is_listable text not null" +       
			", is_entity_description text not null" +
			", display_order int not null" +
			", choices text" +
			", validation_regex text" +
			", validation_example text" +
			");";
	
	public static final String TABLE_NAME = "entity_attribute";
	
	public static final String[] ALL_FIELDS = new String[] {
			"_id",
			"entity_name",
			"attribute_name",
			"attribute_desc",
			"data_type",
			"ref_entity_name",
			"is_primary_key_part",
			"is_required",
			"is_searchable",       
			"is_listable",       
			"is_entity_description",
			"display_order",
			"choices",
			"validation_regex", 
			"validation_example" 
	};

	public Attribute mapObject(Cursor cursor) {
		Attribute attribute = new Attribute();
		attribute.setId(cursor.getInt(0));
		attribute.setEntityName(cursor.getString(1));
		attribute.setAttributeName(cursor.getString(2));
		attribute.setAttributeDesc(cursor.getString(3));
		attribute.setDataType(cursor.getString(4));
		attribute.setRefEntityName(cursor.getString(5));
		attribute.setPrimaryKeyPart("1".equals(cursor.getString(6)));
		attribute.setRequired("1".equals(cursor.getString(7)));
		attribute.setSearchable("1".equals(cursor.getString(8)));
		attribute.setListable("1".equals(cursor.getString(9)));
		attribute.setEntityDescription("1".equals(cursor.getString(10)));
		attribute.setDisplayOrder(cursor.getInt(11));
		attribute.setChoices(cursor.getString(12));
		attribute.setValidationRegex(cursor.getString(13));
		attribute.setValidationExample(cursor.getString(14));
		return attribute;
	}

	public Attribute getById(long id) {
		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "_id = " + id, null, null, null, null);
		cursor.moveToFirst();
		Attribute attribute = mapObject(cursor);
		cursor.close();
		return attribute;
	}

	public List<Attribute> list(Entity entity) {
		List<Attribute> list = new ArrayList<Attribute>();

		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "entity_name = ?", 
				new String[]{entity.getName()}, null, null, "display_order, _id");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Attribute attribute = mapObject(cursor);
			list.add(attribute);
			cursor.moveToNext();
		}

		cursor.close();
		return list;
	}

	public Attribute save(Attribute attribute) {
		ContentValues values = new ContentValues();
		
		values.put("entity_name", attribute.getEntityName());
		values.put("attribute_name", attribute.getAttributeName());
		values.put("attribute_desc", attribute.getAttributeDesc());
		values.put("data_type", attribute.getDataType());
		values.put("ref_entity_name", attribute.getRefEntityName());
		values.put("is_primary_key_part", attribute.isPrimaryKeyPart());
		values.put("is_required", attribute.isRequired());
		values.put("is_searchable", attribute.isSearchable());
		values.put("is_listable", attribute.isListable());
		values.put("is_entity_description", attribute.isEntityDescription());
		values.put("display_order", attribute.getDisplayOrder());
		values.put("choices", attribute.getChoices());
		values.put("validation_regex", attribute.getValidationRegex());
		values.put("validation_example", attribute.getValidationExample());

		if (attribute.getId() == 0) {
			long insertId = database.insert(TABLE_NAME, null,
					values);
			return getById(insertId);
		} else {
			int rows = database.update(TABLE_NAME,
					values, "_id = "+attribute.getId(),  null);
			if (rows == 0) throw new RuntimeException("Invalid id "+attribute.getId());
			return attribute; // if there were triggers, would need to re-read row here.
		}
	}

	public void delete(Attribute attribute) {
		long id = attribute.getId();
		System.out.println("Attribute deleted with id: " + id);
		database.delete(TABLE_NAME, 
				"_id = " + id, null);
	}
}
