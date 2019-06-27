package com.company.api.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {

	/**
	 * Gets the result array.
	 *
	 * @param response
	 *            the response
	 * @param key
	 *            the key
	 * @return the result array
	 */
	public static JSONArray getResultArray(String response, String key) {
		JSONObject jsonObj = new JSONObject(response);
		return jsonObj.getJSONArray(key);
	}

}
