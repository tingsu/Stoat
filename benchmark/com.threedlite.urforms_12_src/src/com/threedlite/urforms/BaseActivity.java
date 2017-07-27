package com.threedlite.urforms;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.AttributeDao;
import com.threedlite.urforms.data.DataDao;
import com.threedlite.urforms.data.Entity;
import com.threedlite.urforms.data.EntityDao;
import com.threedlite.urforms.data.UrSqlHelper;

public class BaseActivity extends Activity {
	
	public final static String TAG = "urforms";
	
	public static String ENTITY_NAME = "_entityName";
	public static String ATTRIBUTE_NAME = "_attributeName";
	public static String ENTITY_ID = "_entityId";
    public static final String SELECT_MODE = "selectMode";
    public static final String SELECT_MODE_SELECT = "select";
	public static String FILE_NAME = "_fileName";



	public static final String MANAGE_FORMS = "Manage Forms";
	public static final String SEARCH_DATA = "Search / Enter Data";
	public static final String BACKUP_RESTORE = "Backup / Restore";

	protected int COL_MIN_WIDTH = 300;

	protected UrSqlHelper sqlHelper;

	// Add horiz and vert scrolls
	@Override
	public void setContentView(View view) {
		ScrollView sv = new ScrollView(this);
		sv.setFillViewport(true);
		sv.addView(view);
		HorizontalScrollView hsv = new HorizontalScrollView(this);
		hsv.setFillViewport(true);
		hsv.addView(sv);
		super.setContentView(hsv);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sqlHelper = new UrSqlHelper(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.add(MANAGE_FORMS);
		menu.add(SEARCH_DATA);
		menu.add(BACKUP_RESTORE);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		String title = item.getTitle().toString();
		if (title.equals(MANAGE_FORMS)) {
			startActivity(new Intent(this, ManageFormsActivity.class));
			return true;
		} else if (title.equals(SEARCH_DATA)) {
			startActivity(new Intent(this, SearchDataActivity.class));
			return true;
		} else if (title.equals(BACKUP_RESTORE)) {
			startActivity(new Intent(this, BackupRestoreActivity.class));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	public List<Entity> getEntities() {
		SQLiteDatabase database = sqlHelper.getWritableDatabase();
		try {
			EntityDao entityDao = new EntityDao(database);
			return entityDao.list();
		} finally {
			sqlHelper.close();
		}
	}

	public List<Attribute> getAttributes(Entity entity) {
		SQLiteDatabase database = sqlHelper.getWritableDatabase();
		try {
			AttributeDao attributeDao = new AttributeDao(database);
			return attributeDao.list(entity);
		} finally {
			sqlHelper.close();
		}
	}

	public void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	protected Bundle getEntitySelectedBundle(Entity entity) {
		Bundle bundle = new Bundle();
		if (entity != null) {
			bundle.putString(EnterDataActivity.ENTITY_NAME, entity.getName());
			Long id = entity.getValues() == null ? 0 : Long.valueOf(entity.getValues().get("_id"));
			bundle.putLong(EnterDataActivity.ENTITY_ID, id);
			if (entity.getValues() != null && entity.getAttributes() != null) {
				for (Attribute attribute: entity.getAttributes()) {
					bundle.putString(attribute.getAttributeName(), entity.getValues().get(attribute.getAttributeName()));
				}
			}
		}
		return bundle;
	}

	protected void startEdit(Entity entity) {
		Intent intent = new Intent(this, EnterDataActivity.class);
		Bundle bundle = getEntitySelectedBundle(entity);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	


	public static String[][] parseChoices(Attribute attribute) {
		String[] choices = attribute.getChoices().split(",");
		String[][] result = new String[choices.length][2];
		for (int i = 0; i < choices.length; i++) {
			String choice = choices[i];
			if (choice.contains("|")) {
				String[] cv = choice.split("\\|");
				result[i][0] = cv[0];
				result[i][1] = cv[1];
			} else {
				result[i][0] = choice;
				result[i][1] = choice;			
			}
		}
		return result;
	}

	public String getTitle(String refEntityName, Long entityId, String suppressAttribute) {

		if (entityId == null || entityId.equals(0)) return "";

		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();

			DataDao dataDao = new DataDao(database);
			Map<String, String> values = dataDao.getEntityDataById(refEntityName, entityId);

			AttributeDao attributeDao = new AttributeDao(database);
			Entity fkentity = new Entity();
			fkentity.setName(refEntityName);
			List<Attribute> attributes = attributeDao.list(fkentity);
			StringBuilder sb = new StringBuilder();
			for (Attribute attribute: attributes) {
				if (attribute.isEntityDescription() && !attribute.getAttributeName().equals(suppressAttribute)) {
					String v = values.get(attribute.getAttributeName());
					if (v != null) {
						if (attribute.getDataType().equals(Attribute.REF_TYPE)) {
							v = getTitle(attribute.getRefEntityName(), Long.valueOf(v), null);
						}
						sb.append(v + " ");
					}
				}
			}

			String s = sb.toString();
			if (s.length() == 0) s = ""+entityId;
			return s;

		} finally {
			sqlHelper.close();
		}
	}


}
