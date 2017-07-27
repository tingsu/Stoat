package com.threedlite.urforms.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EntityDao {

	private SQLiteDatabase database;

	public EntityDao(SQLiteDatabase database) {
		this.database = database;
	}
	
	public static final String TABLE_CREATE = 
			"create table entity (" +
			"_id integer primary key autoincrement" +
			", entity_name text not null" +
			");";


	public static final String TABLE_NAME = "entity";

	public static final String[] ALL_FIELDS = new String[] {
		"_id",
		"entity_name"
	};

	public Entity mapObject(Cursor cursor) {
		Entity entity = new Entity();
		entity.setId(cursor.getInt(0));
		entity.setName(cursor.getString(1));
		return entity;
	}

	public Entity getById(long id) {
		Cursor cursor = database.query(TABLE_NAME,
				ALL_FIELDS, "_id = " + id, null,
				null, null, null);
		cursor.moveToFirst();
		Entity entity = mapObject(cursor);
		cursor.close();
		return entity;
	}

	public List<Entity> list() {
		List<Entity> list = new ArrayList<Entity>();

		Cursor cursor = database.query(TABLE_NAME,
				ALL_FIELDS, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Entity entity = mapObject(cursor);
			list.add(entity);
			cursor.moveToNext();
		}

		cursor.close();
		return list;
	}

	public Entity save(Entity entity) {
		ContentValues values = new ContentValues();
		
		values.put("entity_name", entity.getName());
		
		if (entity.getId() == 0) {
			long insertId = database.insert(TABLE_NAME, null,
					values);
			return getById(insertId);
		} else {
			int rows = database.update(TABLE_NAME,
					values, "_id = "+entity.getId(),  null);
			if (rows == 0) throw new RuntimeException("Invalid id "+entity.getId());
			return entity; // if there were triggers, would need to re-read row here.
		}
	}

	public void delete(Entity entity) {
		long id = entity.getId();
		System.out.println("Entity deleted with id: " + id);
		database.delete(TABLE_NAME, 
				"_id = " + id, null);
	}

}
