package org.safehaus.subutai.shared.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * Created by daralbaev on 7/20/14.
 */
public class SettingConfig {
	@SerializedName ("file")
	public String file;

	@SerializedName ("type")
	public String type;

	@SerializedName ("fieldName")
	public String fieldName;

	@SerializedName ("fieldPath")
	public String fieldPath;

	@SerializedName ("label")
	public String label;

	@SerializedName ("required")
	public boolean required;

	@SerializedName ("tooltip")
	public String tooltip;

	@SerializedName ("uiType")
	public String uiType;

	@SerializedName ("value")
	public String value;
}
