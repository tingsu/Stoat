package com.threedlite.urforms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.DataDao;
import com.threedlite.urforms.data.Entity;


public class SearchResultsActivity extends BaseActivity {


	private TableLayout resultsViewLayoutContents;

	private String mEntityName;
	private String mAttributeName;
	private String mSelectMode = null;
	private Entity mEntity;
	private Entity mSelectedEntity;
	private Map<String, String> mSearchValues;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();

		mEntityName = bundle.getString(EnterDataActivity.ENTITY_NAME);
		mAttributeName = bundle.getString(EnterDataActivity.ATTRIBUTE_NAME);
		mSelectMode = bundle.getString(SELECT_MODE);

		LinearLayout rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.HORIZONTAL);

		populateData(bundle);

		setupResultsView(rootView);

		doSearch();

		setContentView(rootView);

	}

	private void populateData(Bundle bundle) {

		mEntity = new Entity();
		mEntity.setName(mEntityName);
		mEntity.setAttributes(getAttributes(mEntity));

		mSearchValues = new HashMap<String, String>();

		for (Attribute attribute: mEntity.getAttributes()) {

			String searchValue = null;

			if (SearchDataActivity.isSearchable(attribute)) {
				searchValue = bundle.getString(attribute.getAttributeName());
				if (searchValue == null || searchValue.trim().length() == 0) searchValue = null;
				if (searchValue != null) mSearchValues.put(attribute.getAttributeName(), searchValue);
			}

		}
	}


	private void setupResultsView(ViewGroup parent) {

		LinearLayout resultsViewLayout = new LinearLayout(this);
		resultsViewLayout.setOrientation(LinearLayout.VERTICAL);
		resultsViewLayout.setMinimumWidth(500);
		parent.addView(resultsViewLayout);


		resultsViewLayoutContents = new TableLayout(this);
		resultsViewLayout.addView(resultsViewLayoutContents);

	}

	private void doSearch() {

		resultsViewLayoutContents.removeAllViews();

		List<Map<String, String>> results;
		DataDao dataDao = new DataDao(sqlHelper.getWritableDatabase());
		try {
			results = dataDao.search(mEntity, mSearchValues);
		} finally {
			sqlHelper.close();
		}
		populateResultsView(mEntity, results);

	}

	private boolean isListable(Attribute attribute) {
		return attribute.isListable() && 
				(
						attribute.getDataType().equals(Attribute.STRING_TYPE) 
						|| attribute.getDataType().equals(Attribute.DATE_TYPE) 
						);
	}

	private String getSelectText() {
		return (mSelectMode == null) ? "Edit" : "Select";
	}

	private Attribute mSortOn;
	private boolean mSortDesc = false;

	private void populateResultsView(Entity entity, List<Map<String, String>> results) {

		List<Attribute> attributes = getAttributes(entity);
		List<Entity> entities = new ArrayList<Entity>();


		for (Map<String, String> result: results) {

			for (Attribute attribute: attributes) {
				if (isListable(attribute)) {
					if (!result.containsKey(attribute.getAttributeName())) {
						result.put(attribute.getAttributeName(), "");
					}
				} else {
					result.remove(attribute.getAttributeName());
				}
			}

			Entity aentity = new Entity();
			aentity.setName(entity.getName());
			aentity.setAttributes(attributes);
			aentity.setValues(result);
			entities.add(aentity);

		}

		if (mSortOn != null) {
			final boolean isAlphaSort = isAlphaSort(mSortOn);
			Collections.sort(entities, new Comparator<Entity>() {
				public int compare(Entity e1, Entity e2) {
					String v1 = e1.getValues().get(mSortOn.getAttributeName());
					if (v1 == null) v1 = "";
					String v2 = e2.getValues().get(mSortOn.getAttributeName());
					if (v2 == null) v2 = "";
					int retval = 0;
					if (isAlphaSort) {
						retval = v1.compareTo(v2);
					} else {
						try {
							retval = new BigDecimal(v1).compareTo(new BigDecimal(v2));
						} catch (Exception e) {
							retval = v1.compareTo(v2);
						}
					}
					if (mSortDesc) retval = -retval;
					return retval;
				}
			});
		}


		// Views
		resultsViewLayoutContents.removeAllViews();
		TableLayout tableLayout = new TableLayout(this);

		// Refresh button
		TableRow tr = new TableRow(this);
		tableLayout.addView(tr);
		Button bv = new Button(this);
		bv.setText("Refresh");
		bv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doSearch();
			}
		});
		tr.addView(bv);

		// Sort buttons
		for (Attribute attribute:attributes) {
			if (isListable(attribute)) {
				final Attribute sort = attribute;
				bv = new Button(this);
				bv.setText(attribute.getAttributeDesc());
				bv.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (mSortOn == null) {
							mSortDesc = false;
						} else {
							if (mSortOn.getAttributeName().equals(sort.getAttributeName())) mSortDesc = !mSortDesc;
						}
						mSortOn = sort;
						doSearch();
					}
				});
				tr.addView(bv);
			}
		}

		// Data
		for (Entity aentity: entities) {
			tr = new TableRow(this);
			bv = new Button(this);
			bv.setText(getSelectText() + " >");
			final Entity fentity = aentity;
			View.OnClickListener ocl;
			if (this.mSelectMode == null) {
				ocl = new View.OnClickListener() {
					public void onClick(View v) {
						startEdit(fentity);
					}
				};
			} else {
				ocl = new View.OnClickListener() {
					public void onClick(View v) {
						mSelectedEntity = fentity;
						finish();
					}
				};
			}
			bv.setOnClickListener(ocl);
			tr.addView(bv);

			for (Attribute attribute:aentity.getAttributes()) {
				if (isListable(attribute)) {
					String value = aentity.getValues().get(attribute.getAttributeName());
					TextView tv = new TextView(this);
					tv.setText(value);
					tv.setPadding(5, 5, 5, 5);
					tr.addView(tv);
				}
			}
			tableLayout.addView(tr);
		}

		resultsViewLayoutContents.addView(tableLayout);

	}

	private boolean isAlphaSort(Attribute attribute) {
		String v = attribute.getValidationRegex();
		if (v == null || v.trim().length() == 0) return true;
		for (int i = 0; i < v.length(); i++) {
			char c = v.charAt(i);
			if (Character.isLetter(c)) return true;
		}
		return false;
	}
	
	
	private boolean hadFocus = false;
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && hadFocus) {
			doSearch();
		}
		hadFocus = true;
	}

	@Override
	public void finish() {
		Intent intent = new Intent();
		Bundle bundle = getEntitySelectedBundle(mSelectedEntity);
		bundle.putString(EnterDataActivity.ENTITY_NAME, mEntityName); // this entity type
		bundle.putString(EnterDataActivity.ATTRIBUTE_NAME, mAttributeName); //  fk attribute on caller, not this entity
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.finish();
	} 

}
