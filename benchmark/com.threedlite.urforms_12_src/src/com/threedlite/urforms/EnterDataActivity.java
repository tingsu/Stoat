package com.threedlite.urforms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.threedlite.urforms.data.Attribute;
import com.threedlite.urforms.data.AttributeDao;
import com.threedlite.urforms.data.BlobData;
import com.threedlite.urforms.data.BlobDataDao;
import com.threedlite.urforms.data.DataDao;
import com.threedlite.urforms.data.Entity;
import com.threedlite.urforms.data.Validator;


public class EnterDataActivity extends BaseActivity {


	private String mEntityName;
	private long mEntityId;

	private Entity mEntity;
	private List<Attribute> mAttributes;
	private Map<String, String> mValues;

	private Map<String, Object> mEdits;
	private Map<String, View> mDisplays;



	private void loadData(Bundle bundle) {
		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();
			mEntity = new Entity();
			mEntity.setName(mEntityName);
			AttributeDao attributeDao = new AttributeDao(database);
			mAttributes = attributeDao.list(mEntity);
			mEntity.setAttributes(mAttributes);
			DataDao dataDao = new DataDao(database);
			mValues = dataDao.getEntityDataById(mEntityName, mEntityId);
			// Optional override/default values
			for (Attribute attribute: mAttributes) {
				String setValue = bundle.getString(attribute.getAttributeName());
				if (setValue != null && setValue.trim().length() > 0) {
					mValues.put(attribute.getAttributeName(), setValue);
				}
			}
		} finally {
			sqlHelper.close();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();

		mEntityName = bundle.getString(ENTITY_NAME);
		mEntityId = bundle.getLong(ENTITY_ID);

		loadData(bundle);

		LinearLayout rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.VERTICAL);


		TextView tvHeader = new TextView(this);
		String sid = mValues.get("_id");
		tvHeader.setText(mEntity.getName() + " ( " +(sid == null ? "New" : ("Id " + sid)) + " )");
		tvHeader.setTextSize(30);
		rootView.addView(tvHeader);


		Button btnSaveEntity = new Button(this);
		btnSaveEntity.setText("Save");
		btnSaveEntity.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				saveEntity();
			}
		});
		rootView.addView(btnSaveEntity);


		mEdits = new HashMap<String, Object>();
		mDisplays = new HashMap<String, View>();

		TableLayout tableView = new TableLayout(this);
		rootView.addView(tableView);

		EditViewFactory evf = new EditViewFactory(this, sqlHelper, mEdits, mDisplays, mValues);

		TableRow tr = null;
		for (int i = 0; i < mAttributes.size(); i++) {

			Attribute attribute = mAttributes.get(i);
			if (i % 3 == 0 
					|| attribute.getDataType().equals(Attribute.REF_BY_TYPE)
					|| ( i > 0 && mAttributes.get(i-1).getDataType().equals(Attribute.REF_BY_TYPE) )
					) {
				tr = new TableRow(this);
				tableView.addView(tr);
			}

			LinearLayout cell = new LinearLayout(this);
			cell.setPadding(5, 5, 5, 5);
			cell.setOrientation(LinearLayout.VERTICAL);
			tr.addView(cell);

			TextView tvDesc = new TextView(this);
			tvDesc.setText(attribute.getAttributeDesc());
			cell.addView(tvDesc);

			String value = mValues.get(attribute.getAttributeName());
			cell.addView(evf.getEdit(attribute, value));

		}

		Button btnDeleteEntity = new Button(this);
		btnDeleteEntity.setText("Delete");
		btnDeleteEntity.setBackgroundColor(Color.parseColor("#A00000"));
		final Context context = this;
		btnDeleteEntity.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Are you sure you want to delete this item?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						deleteEntity();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
		rootView.addView(btnDeleteEntity);


		setContentView(rootView);

	}



	private void saveEntity() {

		// Read all (non-blob) edited values
		for (Attribute attribute: mAttributes) {

			String value = "";

			Object edit = mEdits.get(attribute.getAttributeName());
			if (edit != null) {
				if (edit instanceof EditText) {
					value = ((EditText)edit).getText().toString();
				} else if (edit instanceof Spinner) {
					String[][] choices = parseChoices(attribute);
					int position = ((Spinner)edit).getSelectedItemPosition();
					if (position == -1) position = 0;
					value = choices[position][0]; // value
				} else if (edit instanceof CheckBox) {
					value = ""+((CheckBox)edit).isChecked();
				} else if (edit instanceof TextView) {
					value = ((TextView)edit).getText().toString();
				} 

			}

			if (attribute.getDataType().equals(Attribute.EDIT_TIMESTAMP_TYPE)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				value = sdf.format(new java.util.Date());
			}

			mValues.put(attribute.getAttributeName(), value);

		}

		// Validation
		String validationMessage = new Validator(sqlHelper, mEntity).validate(mAttributes, mValues);
		if (validationMessage != null) {
			makeToast(validationMessage);
			return;
		}

		// Blobs should be saved after validation in case of validation error.
		for (Attribute attribute: mAttributes) {

			Object edit = mEdits.get(attribute.getAttributeName());
			if (edit != null) {
				if (edit instanceof BlobData) {
					BlobData blobData = (BlobData)edit;
					String guid = blobData.getGuid();
					if (blobData.isDirty()) {
						if (guid != null && guid.trim().length() > 0) {
							deleteBlobByGuid(guid);
						} 
						guid = UUID.randomUUID().toString();
						blobData.setGuid(guid);
						blobData.setId(0);
						blobData = saveBlob(blobData);
						edit = blobData;
					}
					mValues.put(attribute.getAttributeName(), guid);
				}
			}
		}

		// Save values to db
		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();
			DataDao dataDao = new DataDao(database);
			mValues = dataDao.saveEntityData(mEntityName, mValues);
			makeToast("Saved.");
		} finally {
			sqlHelper.close();
		}

	}

	private BlobData saveBlob(BlobData blobData) {
		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();
			BlobDataDao dataDao = new BlobDataDao(database);
			return dataDao.save(blobData);
		} finally {
			sqlHelper.close();
		}
	}

	private void deleteBlobByGuid(String guid) {
		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();
			BlobDataDao dataDao = new BlobDataDao(database);
			BlobData blobData = dataDao.getByGuid(guid);
			if (blobData != null) {
				dataDao.delete(blobData);
			}
		} finally {
			sqlHelper.close();
		}
	}

	private byte[] getFileContents(String filePath) {
		try {
			File file = new File(filePath);
			FileInputStream in = new FileInputStream(file);
			try {
				byte[] b = new byte[in.available()];
				in.read(b);
				return b;
			} finally {
				in.close();
			}
		} catch (Exception e) {
			makeToast("Unable to read file "+filePath+" "+e.getMessage());
			return null;
		}
	}

	private void deleteEntity() {

		try {
			SQLiteDatabase database = sqlHelper.getWritableDatabase();
			DataDao dataDao = new DataDao(database);
			String id = mValues.get("_id");
			if (id == null || id.equals("0")) return;
			dataDao.deleteEntityData(mEntityName, Long.parseLong(id));
			makeToast("Deleted.");
		} finally {
			sqlHelper.close();
		}

	}


	public final static int SELECT_REQUEST_CODE = 1;
	public final static int CHOOSE_FILE_REQUEST_CODE = 2;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Reference type
		if (resultCode == RESULT_OK && requestCode == SELECT_REQUEST_CODE) {

			if (data.hasExtra(EnterDataActivity.ENTITY_ID)) {

				Long selectedId = data.getExtras().getLong(EnterDataActivity.ENTITY_ID);
				if (selectedId == null || selectedId.equals("0")) return;
				String refentityName = data.getExtras().getString(EnterDataActivity.ENTITY_NAME);
				if (refentityName == null || refentityName.equals("")) return;

				String attributeName = data.getExtras().getString(EnterDataActivity.ATTRIBUTE_NAME);
				if (attributeName == null || attributeName.equals("")) return;

				TextView tv = (TextView)mEdits.get(attributeName);
				if (tv != null) tv.setText(""+selectedId);
				TextView tvDesc = (TextView)mDisplays.get(attributeName);
				if (tvDesc != null) tvDesc.setText(getTitle(refentityName, selectedId, null));


			}

			// File and image types
		} else if (resultCode == RESULT_OK && requestCode == CHOOSE_FILE_REQUEST_CODE) {

			if (data.hasExtra(EnterDataActivity.FILE_NAME)) {

				String filePath = data.getExtras().getString(EnterDataActivity.FILE_NAME);
				if (filePath == null || filePath.equals("")) return;
				String attributeName = data.getExtras().getString(EnterDataActivity.ATTRIBUTE_NAME);
				if (attributeName == null || attributeName.equals("")) return;

				File file = new File(filePath);
				if (file.length() > 5000000) {
					makeToast("File size too large");
					return;
				}

				String fileName = file.getName();
				String mimeType = FileUtils.getMimeType(this, file);
				byte[] contents = getFileContents(filePath);
				if (contents == null) return;

				View display = mDisplays.get(attributeName);
				if (display instanceof ImageView) {
					fileName = "thumb_"+fileName;
					contents = getThumbnail(contents);
					mimeType = "image/jpeg";
				} 

				BlobData blobData = (BlobData)mEdits.get(attributeName);
				if (blobData == null) return;

				blobData.setFileName(fileName);
				blobData.setBlobData(contents);
				blobData.setMimeType(mimeType);
				blobData.setSize(contents.length);
				blobData.setDirty(true);

				try {
					if (display instanceof ImageView) {
						Bitmap bm = BitmapFactory.decodeByteArray(contents, 0, contents.length);
						((ImageView)display).setImageBitmap(bm);
					} else if (display instanceof TextView) {
						((TextView)display).setText(fileName);
					}
				} catch (Exception e) {
					Log.e(TAG, "Cannot update display "+e.getMessage());
				}

			}

		}

	} 

	private byte[] getThumbnail(byte[] contents) {

		try {

			final int THUMBNAIL_SIZE = 128;

			InputStream fis = new ByteArrayInputStream(contents);
			Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

			imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			return baos.toByteArray();

		} catch(Exception ex) {
			makeToast("Cannot create image thumbnail "+ex.getMessage());
			return null;
		}

	}

	
	private boolean hadFocus = false;
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && hadFocus) {
			refreshReferenceBys();
		}
		hadFocus = true;
	}
	
	private void refreshReferenceBys() {
		EditViewFactory evf = new EditViewFactory(this, sqlHelper, mEdits, mDisplays, mValues);
		for (Attribute attribute: mAttributes) {
			if (attribute.getDataType().equals(Attribute.REF_BY_TYPE)) {
				Object oedit = mEdits.get(attribute.getAttributeName());
				if (oedit instanceof LinearLayout) {
					LinearLayout edit = (LinearLayout)oedit;
					LinearLayout newEdit = (LinearLayout)evf.getEdit(attribute, null);
					View newContent = newEdit.getChildAt(0);
					newEdit.removeView(newContent);
					edit.removeAllViews();
					edit.addView(newContent);
					mEdits.put(attribute.getAttributeName(), edit);
					
					edit.setWillNotDraw(false);
					edit.invalidate();
				}
			}
		}
			
	}
	
}
