package com.threedlite.urforms.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataDao {
	
	private static final String TAG = "urforms_DataDao";
	
	private SQLiteDatabase database;

	public DataDao(SQLiteDatabase database) {
		this.database = database;
	}
	
	public static final String TABLE_CREATE = 
			"create table entity_data (" +
			"_id integer primary key autoincrement" +
			", entity_name text not null" +
			", attribute_name text not null" +
			", entity_id int not null" +
			", field_value text not null  collate nocase" +
			", field_value_num float not null" +  // can populate as numeric for index range
			");";
	public static final String INDEX_FOR_ONE_ROW_CREATE = 
			"create index i1_entity_data on entity_data (" +
			"entity_id" +
			", entity_name" +
			", attribute_name" +
			", field_value" +
			");";
	public static final String INDEX_FOR_JOIN_CREATE = 
			"create index i2_entity_data on entity_data (" +
			"field_value" +
			", attribute_name" +
			", entity_name" +
			");";
	public static final String INDEX_FOR_NUM_CREATE = 
			"create index i3_entity_data on entity_data (" +
			"field_value_num" +
			", attribute_name" +
			", entity_name" +
			");";
	
	public static final String TABLE_NAME = "entity_data";
	
	public static final String[] ALL_FIELDS = new String[] {
			"_id",
			"entity_name",
			"attribute_name",
			"entity_id",
			"field_value",
			"field_value_num"
	};

	public Data mapObject(Cursor cursor) {
		Data data = new Data();
		data.setId(cursor.getInt(0));
		data.setEntityName(cursor.getString(1));
		data.setAttributeName(cursor.getString(2));
		data.setEntityId(cursor.getLong(3));
		data.setFieldValue(cursor.getString(4));
		return data;
	}

	public Data getById(long id) {
		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "_id = " + id, null, null, null, null);
		cursor.moveToFirst();
		Data data = mapObject(cursor);
		cursor.close();
		return data;
	}

	public List<Data> list(String entityName, long entityId) {
		List<Data> list = new ArrayList<Data>();

		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "entity_name = ? and entity_id = ?", 
				new String[]{entityName, ""+entityId}, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Data data = mapObject(cursor);
			list.add(data);
			cursor.moveToNext();
		}

		cursor.close();
		return list;
	}

	private boolean isNumeric(String s) {
		if (s.startsWith("-")) s = s.substring(1);
		boolean foundpoint = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '.') {
				if (foundpoint) return false;
				foundpoint = true;
			} else {
				if (!Character.isDigit(c)) return false;
			}
		}
		return true;
	}
	
	public Data save(Data data) {
		ContentValues values = new ContentValues();
		
		values.put("entity_name", data.getEntityName());
		values.put("attribute_name", data.getAttributeName());
		values.put("entity_id", data.getEntityId());
		values.put("field_value", data.getFieldValue());
		double num = 0;  
		if (isNumeric(data.getFieldValue())) {
			try {
				num = Double.valueOf(data.getFieldValue());
			} catch (Exception e) {}
		}
		values.put("field_value_num", num);

		if (data.getId() == 0) {
			long insertId = database.insert(TABLE_NAME, null,
					values);
			return getById(insertId);
		} else {
			int rows = database.update(TABLE_NAME,
					values, "_id = "+data.getId(),  null);
			if (rows == 0) throw new RuntimeException("Invalid id "+data.getId());
			return data; // if there were triggers, would need to re-read row here.
		}
	}

	public void delete(Data data) {
		long id = data.getId();
		database.delete(TABLE_NAME, "_id = " + id, null);
	}
	
	public static final String ID = "_id";
	
	public Map<String, String> getEntityDataById(String entityName, long id) {
		List<Data> list = list(entityName, id);
		Map<String, String> record = new HashMap<String, String>();
		for (Data data: list) {
			record.put(data.getAttributeName(), data.getFieldValue());
		}
		return record;
	}
	
	private long getMaxLogicalId(String entityName) {
		Cursor cursor = database.rawQuery("select max(CAST(field_value as integer)) from "+TABLE_NAME+" where entity_name = ? and attribute_name = '_id'", new String[]{entityName});
		cursor.moveToFirst();
		long result = cursor.getLong(0);
		cursor.close();
		return result;
	}
	
	public int deleteEntityData(String entityName, long id) {
		return database.delete(TABLE_NAME, "entity_name = ? and entity_id = ?", new String[]{entityName, ""+id});
	}
	
	public Map<String, String> saveEntityData(String entityName, Map<String, String> fieldValues) {

		Map<String, Data> oldRecord = new HashMap<String, Data>();
		long id;
		String oid = fieldValues.get(ID);
		if (oid == null) {
			// entity "insert"
			Data data = new Data();
			data.setAttributeName(ID);
			data.setFieldValue("0");
			data.setEntityId(0);
			data.setEntityName(entityName);
			id = getMaxLogicalId(entityName)+1; // needs to be max of logical ids in case of import foreign ids
			data.setEntityId(id);  
			data.setFieldValue(""+id);
			data = save(data);
			fieldValues.put(ID, ""+id);
		} else {
			// entity "update"
			id = Long.valueOf(oid);
			List<Data> list = list(entityName, id);
			for (Data data: list) {
				oldRecord.put(data.getAttributeName(), data);
			}
		}
		
		List<Data> adds = new ArrayList<Data>();
		List<Data> deletes = new ArrayList<Data>();
		for (Map.Entry<String, String> me: fieldValues.entrySet()) {
			String newValue = me.getValue();
			Data oldValue = oldRecord.get(me.getKey());
			if (oldValue == null) {
				Data newData = new Data();
				newData.setAttributeName(me.getKey());
				newData.setEntityId(id);
				newData.setEntityName(entityName);
				newData.setFieldValue(""+me.getValue());
				adds.add(newData);
			} else if (newValue != null && !newValue.equals(oldValue.getFieldValue())) {
				oldValue.setFieldValue(newValue);
				adds.add(oldValue);
			}
		}
		for (Map.Entry<String, Data> me: oldRecord.entrySet()) {
			if (fieldValues.get(me.getKey()) == null) {
				deletes.add(me.getValue());
			}
		}
		for (Data data: deletes) delete(data);
		for (Data data: adds) save(data);
		
		return fieldValues;
		
	}
	
	public static String formatDate(java.util.Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	
	// Entity.attributes must be populated.
	public List<Map<String, String>> search(Entity entity, Map<String, String> searchValues) {
		String sql = "";

		int i = 0;

		List<String> args = new ArrayList<String>();
		for (Attribute attribute: entity.getAttributes()) {

			String searchValue = searchValues.get(attribute.getAttributeName());


			if (i == 0) { 
				sql += "select * from  ";
			} else {
				sql += ((searchValue == null) ? " left ":"") + " join ";
			}
			sql += " ("
					+"select entity_id \"i"+i+"\", field_value \"f"+i+"\" "
					+"from entity_data "
					+"where "
					+"entity_name = ? "
					+"and attribute_name = ? " + ((searchValue == null) ? "" : " and field_value like ? collate nocase ")
					+")  \"t"+i+"\" "
					;
			args.add(attribute.getEntityName());
			args.add(attribute.getAttributeName());
			if (searchValue != null) {
				args.add("%"+searchValue+"%");
			}
			if (i > 0) { 
				sql += " on t0.i0 = t"+i+".i"+i + " ";
			}
			i++;
		}
		sql += " order by i0  limit 100 ";

		//Log.d(TAG, sql);
		String debug = ""; for (String s:args) debug += s + " ";
		//Log.d(TAG, debug);

		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Cursor cursor = database.rawQuery(sql, (String[])args.toArray(new String[args.size()]));
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Map<String, String> row = new HashMap<String, String>();
			i = 0;
			for (Attribute attribute: entity.getAttributes()) {
				int id = cursor.getInt(i*2);
				String value = cursor.getString(i*2+1);
				if (i == 0) row.put("_id", ""+id);
				row.put(attribute.getAttributeName(), value);
				i++;
			}
			results.add(row);
			cursor.moveToNext();
		}
		
		//Log.d(TAG, "results="+results.size());
		
		return results;
	}
	
}
