package com.threedlite.urforms.data;


public class BlobData {
	
	private long id;
	private String guid;
	private String fileName;
	private String mimeType;
	private long size;
	private byte[] blobData;
	
	private transient boolean dirty = false; // not saved

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public byte[] getBlobData() {
		return blobData;
	}

	public void setBlobData(byte[] blobData) {
		this.blobData = blobData;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}


}
