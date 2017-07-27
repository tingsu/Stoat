package com.threedlite.urforms;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.Entity;
import com.threedlite.urforms.data.EntityDao;


public class SearchDataActivity extends BaseActivity {

	private boolean isNewInstall() {

		SQLiteDatabase database = sqlHelper.getWritableDatabase();
		try {
			EntityDao entityDao = new EntityDao(database);
			entities = entityDao.list();
			if (entities.size() == 0) {
				startActivity(new Intent(this, ManageFormsActivity.class));
				return true;
			}
		} finally {
			sqlHelper.close();
		}
		return false;

	}


	private int currentEntity = -1;
	private List<Entity> entities = new ArrayList<Entity>();
	private ArrayAdapter<Entity> entityAdapter = null; 
	private LinearLayout searchViewLayoutContents;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isNewInstall()) return;

		TableLayout rootView = new TableLayout(this);
		TableRow tr = new TableRow(this);
		rootView.addView(tr);

		setupEntityList(tr);
		setupSearchView(tr);
		
		setContentView(rootView);
	}

	private void setupEntityList(ViewGroup parent) {

		LinearLayout entityLayout = new LinearLayout(this);
		entityLayout.setOrientation(LinearLayout.VERTICAL);
		entityLayout.setMinimumWidth(COL_MIN_WIDTH);
		parent.addView(entityLayout);

		ListView entitylist = new ListView(this);

		entityAdapter =  new ArrayAdapter<Entity>(this,
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1,
				entities);
		entitylist.setAdapter(entityAdapter);
		entitylist.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currentEntity = position;
				Entity entity = entities.get(position);
				populateSearchView(entity);
			}
		});
		entityLayout.addView(entitylist);

	}

	private void setupSearchView(ViewGroup parent) {

		LinearLayout searchViewLayout = new LinearLayout(this);
		searchViewLayout.setOrientation(LinearLayout.VERTICAL);
		searchViewLayout.setMinimumWidth(COL_MIN_WIDTH);
		parent.addView(searchViewLayout);
		
		searchViewLayoutContents = new LinearLayout(this);
		searchViewLayoutContents.setOrientation(LinearLayout.VERTICAL);
		searchViewLayout.addView(searchViewLayoutContents);

	}

	private void doSearch(Entity entity) {
		
		Intent intent = new Intent(this, SearchResultsActivity.class);
		Bundle bundle = new Bundle();
		
		for (Attribute attribute: getAttributes(entity)) {
			if (isSearchable(attribute)) {
				String tag = "S_"+attribute.getAttributeName();
				EditText ev = (EditText)searchViewLayoutContents.findViewWithTag(tag);
				bundle.putString(attribute.getAttributeName(), ev.getText().toString());
			}
		}
		bundle.putString(EnterDataActivity.ENTITY_NAME, entity.getName());
		
		intent.putExtras(bundle);
		startActivity(intent);
		
	}
	
	public static boolean isSearchable(Attribute attribute) {
		return attribute.isSearchable() && 
				(
				attribute.getDataType().equals(Attribute.STRING_TYPE) 
				|| attribute.getDataType().equals(Attribute.DATE_TYPE)
				);
	}

	private void populateSearchView(Entity entity) {
		searchViewLayoutContents.removeAllViews();
		
		if (currentEntity == -1) return;
		
		final Entity newEntity = new Entity();
		newEntity.setName(entities.get(currentEntity).getName());
		Button btnAddNew = new Button(this);
		btnAddNew.setText("Add new " + newEntity.getName());
		View.OnClickListener ocl = new View.OnClickListener() {
			public void onClick(View v) {
				startEdit(newEntity);
			}
		};
		btnAddNew.setOnClickListener(ocl);
		searchViewLayoutContents.addView(btnAddNew);

		Button btnSearch = new Button(this);
		btnSearch.setText("Search for " + newEntity.getName());
		btnSearch.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				if (currentEntity != -1) doSearch(entities.get(currentEntity));
			}
		});
		searchViewLayoutContents.addView(btnSearch);

		
		List<Attribute> attributes = getAttributes(entity);
		for (Attribute attribute: attributes) {
			if (isSearchable(attribute)) {

				TextView tvDesc = new TextView(this);
				tvDesc.setText(attribute.getAttributeDesc());
				searchViewLayoutContents.addView(tvDesc);
				EditText etValue = new EditText(this);
				etValue.setTag("S_"+attribute.getAttributeName());
				searchViewLayoutContents.addView(etValue);

			}
		}
	}

}
