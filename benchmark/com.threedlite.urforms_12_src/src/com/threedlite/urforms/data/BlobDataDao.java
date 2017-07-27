package com.threedlite.urforms.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BlobDataDao {


	private SQLiteDatabase database;

	public BlobDataDao(SQLiteDatabase database) {
		this.database = database;
	}

	public static final String TABLE_CREATE = 
			"create table blob_data (" +
					"_id integer primary key autoincrement" +
					", guid text not null" +
					", file_name text not null" +
					", mime_type text not null" +
					", size int not null" +
					", blob_data blob" +  
					");";
	public static final String INDEX_GUID = 
			"create index i1_blob_data on blob_data (" +
					"guid" +
					");";

	public static final String TABLE_NAME = "blob_data";

	public static final String[] ALL_FIELDS = new String[] {
		"_id",
		"guid",
		"file_name",
		"mime_type",
		"size",
		"blob_data"
	};

	public BlobData mapObject(Cursor cursor) {
		BlobData data = new BlobData();
		data.setId(cursor.getInt(0));
		data.setGuid(cursor.getString(1));
		data.setFileName(cursor.getString(2));
		data.setMimeType(cursor.getString(3));
		data.setSize(cursor.getLong(4));
		data.setBlobData(cursor.getBlob(5));
		return data;
	}

	public BlobData getById(long id) {
		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "_id = " + id, null, null, null, null);
		cursor.moveToFirst();
		BlobData data = mapObject(cursor);
		cursor.close();
		return data;
	}

	public BlobData getByGuid(String guid) {
		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "guid = ?" , new String[]{guid}, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				return mapObject(cursor);
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}

	public List<BlobData> list(String entityName, long entityId) {
		List<BlobData> list = new ArrayList<BlobData>();

		Cursor cursor = database.query(TABLE_NAME, ALL_FIELDS, "entity_name = ? and entity_id = ?", 
				new String[]{entityName, ""+entityId}, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			BlobData data = mapObject(cursor);
			list.add(data);
			cursor.moveToNext();
		}

		cursor.close();
		return list;
	}

	public BlobData save(BlobData data) {
		ContentValues values = new ContentValues();

		values.put("guid", data.getGuid());
		values.put("file_name", data.getFileName());
		values.put("mime_type", data.getMimeType());
		values.put("size", data.getSize());
		values.put("blob_data", data.getBlobData());

		if (data.getId() == 0) {
			long insertId = database.insert(TABLE_NAME, null,
					values);
			return getById(insertId);
		} else {
			int rows = database.update(TABLE_NAME,
					values, "_id = "+data.getId(),  null);
			if (rows == 0) throw new RuntimeException("Invalid id "+data.getId());
			return data; 
		}
	}

	public void delete(BlobData data) {
		long id = data.getId();
		database.delete(TABLE_NAME, "_id = " + id, null);
	}

}
