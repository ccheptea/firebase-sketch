package com.cheptea.cc.firebasesketch.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sketch Document specs.
 * Created by constantin.cheptea on 03/10/16.
 */

public class Document implements Parcelable {
	public static final Creator<Document> CREATOR = new Creator<Document>() {
		@Override
		public Document createFromParcel(Parcel in) {
			return new Document(in);
		}

		@Override
		public Document[] newArray(int size) {
			return new Document[size];
		}
	};
	private String key;
	private String title;
	private int width; // inch
	private int height; // inch
	private long date;

	public Document() {

	}

	protected Document(Parcel in) {
		key = in.readString();
		title = in.readString();
		width = in.readInt();
		height = in.readInt();
		date = in.readLong();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Document{" +
				"key='" + key + '\'' +
				", title='" + title + '\'' +
				", width=" + width +
				", height=" + height +
				", date=" + date +
				'}';
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(key);
		dest.writeString(title);
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeLong(date);
	}
}
