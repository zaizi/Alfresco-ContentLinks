/**
 * This file is part of Alfresco RedLink Module.
 *
 * Alfresco RedLink Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco RedLink Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Alfresco RedLink Module.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zaizi.alfresco.redlink.domain;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Entity bean
 * 
 * @author efoncubierta
 * 
 */
public class Entity implements Serializable {
	private static final long serialVersionUID = -2340892940785351584L;

	private String label;
	private String type;
//	private String provider;
//	private String reference;
//	private String raw;
//	private String rawFormat;
	private String data;

	/**
	 * Get label property
	 * 
	 * @return Label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set label property
	 * 
	 * @param label
	 *            Label property
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get classes property
	 * 
	 * @return Classes property
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set classes property
	 * 
	 * @param classes
	 *            Classes property
	 */
	public void setType(String type) {
		this.type = type;
	}

//	/**
//	 * Get provider property
//	 * 
//	 * @return Provider property
//	 */
//	public String getProvider() {
//		return provider;
//	}
//
//	/**
//	 * Set provider property
//	 * 
//	 * @param provider
//	 *            Provider property
//	 */
//	public void setProvider(String provider) {
//		this.provider = provider;
//	}
//
//	/**
//	 * Get reference property
//	 * 
//	 * @return Reference property
//	 */
//	public String getReference() {
//		return reference;
//	}
//
//	/**
//	 * Set reference property
//	 * 
//	 * @param reference
//	 *            Reference property
//	 */
//	public void setReference(String reference) {
//		this.reference = reference;
//	}
//
	/**
	 * Get data property
	 * 
	 * @return Data property
	 */
	public String getData() {
		return data;
	}

	/**
	 * Set details property
	 * 
	 * @param data
	 *            Details property
	 */
	public void setData(String data) {
		this.data = data;
	}
//
//	/**
//	 * Get raw content
//	 * 
//	 * @return Raw content
//	 */
//	public String getRaw() {
//		return raw;
//	}
//
//	/**
//	 * Set raw content
//	 * 
//	 * @param raw
//	 *            Raw content
//	 */
//	public void setRaw(String raw) {
//		this.raw = raw;
//	}
//
//	/**
//	 * Get the raw format property
//	 * 
//	 * @return Raw format
//	 */
//	public String getRawFormat() {
//		return rawFormat;
//	}
//
//	/**
//	 * Set the raw format property
//	 * 
//	 * @param rawFormat
//	 *            Raw format
//	 */
//	public void setRawFormat(String rawFormat) {
//		this.rawFormat = rawFormat;
//	}
//
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Entity) {
			return this.label.equals(((Entity) o).getLabel());
		}
		return false;
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();

		try {
			json.put("label", label);
			json.put("type", type);
			json.put("data", new JSONArray(data));
		} catch (JSONException e) {
		}

		return json;
	}
}
