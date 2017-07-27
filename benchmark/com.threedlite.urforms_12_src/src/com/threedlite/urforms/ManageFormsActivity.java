package com.threedlite.urforms;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.AttributeDao;
import com.threedlite.urforms.data.Entity;
import com.threedlite.urforms.data.EntityDao;
import com.threedlite.urforms.data.SampleDataPopulator;

public class ManageFormsActivity extends BaseActivity {
	

	private int currentEntity = -1;
	private int currentAttribute = -1;
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Attribute> attributes = new ArrayList<Attribute>();
	private ArrayAdapter<Entity> entityAdapter = null; 
	private ArrayAdapter<Attribute> attributeAdapter = null;
	
	private EditText etFieldName = null;
	private EditText etFieldDescription = null; 
	private Spinner spDatatype  = null;
	private EditText etRefType = null;
	private CheckBox cbKey = null;
	private CheckBox cbRequired = null;
	private CheckBox cbSearchable = null;
	private CheckBox cbListable = null;
	private CheckBox cbEntityDescription = null;
	private EditText etChoices = null;
	private EditText etValidationRegex = null;
	private EditText etValidationExample = null;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TableLayout rootView = new TableLayout(this);
		TableRow tr = new TableRow(this);
		
		rootView.addView(tr);
	
		setupEntityList(tr);
		setupAttributeList(tr);
		setupDetailList(tr);

		populateInitialData();

		setContentView(rootView);

	}
	
	private void setupEntityList(ViewGroup parent) {
		
		LinearLayout entityLayout = new LinearLayout(this);
		entityLayout.setOrientation(LinearLayout.VERTICAL);
		entityLayout.setMinimumWidth(COL_MIN_WIDTH);
		parent.addView(entityLayout);
		
		Button btnAddEntity = new Button(this);
		btnAddEntity.setText("Add Form");
		btnAddEntity.setFocusableInTouchMode(true);
		btnAddEntity.requestFocus();
		btnAddEntity.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				addEntity();
			}
		});
		entityLayout.addView(btnAddEntity);
	
		
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
				attributes.clear();
				attributes.addAll(getAttributes(entity));
				attributeAdapter.notifyDataSetChanged();
				clearFieldDetails();
				currentAttribute = -1;
			}
		});
		entityLayout.addView(entitylist);
		
	}
	
	private void setupAttributeList(ViewGroup parent) {
		
		LinearLayout attributeLayout = new LinearLayout(this);
		attributeLayout.setOrientation(LinearLayout.VERTICAL);
		attributeLayout.setMinimumWidth(COL_MIN_WIDTH);
		parent.addView(attributeLayout);
		
		Button btnAddAttribute = new Button(this);
		btnAddAttribute.setText("Add Field");
		btnAddAttribute.setFocusableInTouchMode(true);
		btnAddAttribute.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
			   addAttribute();
			}
		});
		attributeLayout.addView(btnAddAttribute);
	

		ListView attributelist = new ListView(this);
		attributeAdapter = new ArrayAdapter<Attribute>(this,
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1,
				attributes);
		attributelist.setAdapter(attributeAdapter);
		attributelist.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currentAttribute = position;
				Attribute attribute = attributes.get(position);
				readFieldDetails(attribute);
			}
		});
		attributeLayout.addView(attributelist);

	}
	
	private void setupDetailList(ViewGroup parent) {
		
		LinearLayout fieldDetails = new LinearLayout(this);
		fieldDetails.setMinimumWidth(COL_MIN_WIDTH*2);
		fieldDetails.setOrientation(LinearLayout.VERTICAL);
		
		Button btnSave = new Button(this);
		btnSave.setText("Save Field Definition");
		btnSave.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				saveFieldDetails();
			}
		});
		fieldDetails.addView(btnSave);
		
		TextView tvFieldName = new TextView(this);
		tvFieldName.setText("Field Name");
		fieldDetails.addView(tvFieldName);
		etFieldName = new EditText(this);
		fieldDetails.addView(etFieldName);

		TextView tvFieldDescription = new TextView(this);
		tvFieldDescription.setText("Field Description");
		fieldDetails.addView(tvFieldDescription);
		etFieldDescription = new EditText(this);
		fieldDetails.addView(etFieldDescription);

		TextView tvDatatype = new TextView(this);
		tvDatatype.setText("Data type");
		fieldDetails.addView(tvDatatype);
		spDatatype = new Spinner(this);
		ArrayAdapter<CharSequence> adapter = 
				new ArrayAdapter<CharSequence>(this,
						android.R.layout.simple_spinner_item,
						Attribute.DATA_DESCS);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spDatatype.setAdapter(adapter);
    	fieldDetails.addView(spDatatype);
    	
		TextView tvRefType = new TextView(this);
		tvRefType.setText("Form name (if reference type), or Form name Field name (if referencedBy type)");
		fieldDetails.addView(tvRefType);
		etRefType = new EditText(this);
		fieldDetails.addView(etRefType);

    	cbKey = new CheckBox(this);
    	cbKey.setText("Key");
    	fieldDetails.addView(cbKey);
		
    	cbRequired = new CheckBox(this);
    	cbRequired.setText("Required");
    	fieldDetails.addView(cbRequired);
		
    	cbSearchable = new CheckBox(this);
    	cbSearchable.setText("Search");
    	fieldDetails.addView(cbSearchable);
		
    	cbListable = new CheckBox(this);
    	cbListable.setText("List");
    	fieldDetails.addView(cbListable);
		
    	cbEntityDescription = new CheckBox(this);
    	cbEntityDescription.setText("Short Desc");
    	fieldDetails.addView(cbEntityDescription);
		
		TextView tvChoices = new TextView(this);
		tvChoices.setText("Choices");
		fieldDetails.addView(tvChoices);
		etChoices = new EditText(this);
		fieldDetails.addView(etChoices);

		TextView tvValidation = new TextView(this);
		tvValidation.setText("Validation regex");
		fieldDetails.addView(tvValidation);
		etValidationRegex = new EditText(this);
		fieldDetails.addView(etValidationRegex);
    	
		TextView tvExample = new TextView(this);
		tvExample.setText("Validation example");
		fieldDetails.addView(tvExample);
		etValidationExample = new EditText(this);
		fieldDetails.addView(etValidationExample);
    	
		parent.addView(fieldDetails);
	}
	
	private void clearFieldDetails() {
		etFieldDescription.setText("");
		etFieldName.setText("");
		int position = 0;
		spDatatype.setSelection(position);
		etRefType.setText("");
		cbKey.setChecked(false);
		cbRequired.setChecked(false);
		cbSearchable.setChecked(false);
		cbListable.setChecked(false);
		cbEntityDescription.setChecked(false);
		etChoices.setText("");
		etValidationRegex.setText("");
		etValidationExample.setText("");
	}
		
	private void readFieldDetails(Attribute attribute) {
		etFieldDescription.setText(attribute.getAttributeDesc());
		etFieldName.setText(attribute.getAttributeName());
		int position = 0;
		for (int i = 0; i < Attribute.DATA_TYPES.length; i++) {
			if (Attribute.DATA_TYPES[i].equals(attribute.getDataType())) {
				position = i;
				break;
			}
		}
		spDatatype.setSelection(position);
		etRefType.setText(attribute.getRefEntityName());
		cbKey.setChecked(attribute.isPrimaryKeyPart());
		cbRequired.setChecked(attribute.isRequired());
		cbSearchable.setChecked(attribute.isSearchable());
		cbListable.setChecked(attribute.isListable());
		cbEntityDescription.setChecked(attribute.isEntityDescription());
		etChoices.setText(attribute.getChoices());
		etValidationRegex.setText(attribute.getValidationRegex());
		etValidationExample.setText(attribute.getValidationExample());
	}
	
	private void saveFieldDetails() {
		if (currentAttribute == -1) return;
		Attribute attribute = attributes.get(currentAttribute);
		attribute.setAttributeName(etFieldName.getText().toString());
		attribute.setAttributeDesc(etFieldDescription.getText().toString());
		int position = spDatatype.getSelectedItemPosition();
		attribute.setDataType(Attribute.DATA_TYPES[position]);
		attribute.setRefEntityName(etRefType.getText().toString());
		attribute.setPrimaryKeyPart(cbKey.isChecked());
		attribute.setRequired(cbRequired.isChecked());
		attribute.setSearchable(cbSearchable.isChecked());
		attribute.setListable(cbListable.isChecked());
		attribute.setEntityDescription(cbEntityDescription.isChecked());
		attribute.setChoices(etChoices.getText().toString());
		attribute.setValidationRegex(etValidationRegex.getText().toString());
		attribute.setValidationExample(etValidationExample.getText().toString());
		
		try {
			attribute = new AttributeDao(sqlHelper.getWritableDatabase()).save(attribute);
			makeToast("Saved.");
		} finally {
			sqlHelper.close();
		}
		attributes.set(currentAttribute, attribute);
	}

	private void populateInitialData() {

		entities.addAll(getEntities());
		if (entities.size() == 0) {
			new SampleDataPopulator().addSampleData(sqlHelper);
			entities.addAll(getEntities());
		}

	}

	
	private void addEntity() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add Form");
		alert.setMessage("Enter new form name");

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String name = input.getText().toString();
			if (name.length() == 0) return;
			for (Entity entity: entities) if (entity.getName().equals(name)) return; // duplicate
		  
		  	Entity entity = new Entity();
		  	entity.setName(name);
		  	try {
		  		entity = new EntityDao(sqlHelper.getWritableDatabase()).save(entity);
		  	} finally {
		  		sqlHelper.close();
		  	}
		  	entities.add(entity);
		  	entityAdapter.notifyDataSetChanged();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    
		  }
		});

		alert.show();
	}

	private void addAttribute() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add Field");
		alert.setMessage("Enter new field name");

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String name = input.getText().toString();
			if (name.length() == 0) return;
			int maxOrder = 0;
			for (Attribute attribute: attributes) {
				if (attribute.getAttributeName().equals(name)) return; // duplicate
				if (attribute.getDisplayOrder() > maxOrder) maxOrder = attribute.getDisplayOrder();
			}
		  
		  	Attribute attribute = new Attribute();
		  	attribute.setAttributeName(name);
		  	attribute.setDisplayOrder(maxOrder+1);
		  	attribute.setEntityName(entities.get(currentEntity).getName());
		  	try {
		  		attribute = new AttributeDao(sqlHelper.getWritableDatabase()).save(attribute);
		  	} finally {
		  		sqlHelper.close();
		  	}
		  	attributes.add(attribute);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    
		  }
		});

		alert.show();
	}


	


}
