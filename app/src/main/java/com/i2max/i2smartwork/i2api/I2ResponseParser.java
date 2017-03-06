package com.i2max.i2smartwork.i2api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by JHTart on 15. 7. 13..
 * response Data Parse
 */
public class I2ResponseParser {

    public static class Update {

//        public static JSONObject getVersionCheckPhoneApp(JSONObject responseJSON) {
//            JSONObject versionJSON = null;
//
//            try {
//                JSONArray Jarray = responseJSON.getJSONArray("version");
//
//                for (int i = 0; i < Jarray.length(); i++) {
//                    JSONObject object = Jarray.getJSONObject(i);
//                    if(object.get("type").equals("connect_phone_app")) {
//                        versionJSON = object;
//                        break;
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//            return versionJSON;
//        }
    }

    public static boolean checkReponseStatus(JSONObject responseJSON) {
        boolean isResponseOK = false;

        try {
            if(responseJSON == null) isResponseOK = true;
            else if(responseJSON.getInt("statusCode") >= 0) {
                isResponseOK = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return isResponseOK;
    }

    public static boolean checkReponseStatus(Map<String, Object> responseJsonMap) {
        boolean isResponseOK = false;

        if(responseJsonMap == null) isResponseOK = true;

        int statusCode = (int)Float.parseFloat((String) responseJsonMap.get("statusCode"));
        if (statusCode >= 0) {
            isResponseOK = true;
        }

        return isResponseOK;
    }

    public static String getStatusMessage(JSONObject responseJSON) {
        if(responseJSON == null) return "";

        String msg = null;
        try {
            msg = responseJSON.getString("statusMessage");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static JSONObject getStatusInfo(JSONObject responseJSON) {
        return getJsonObject(responseJSON, "statusInfo");
    }

    public static JSONArray getStatusInfoArray(JSONObject responseJSON) {
        return getJsonArray(responseJSON, "statusInfo");
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String key) {
        if(jsonObject == null) return null;

        JSONObject jsonObj = null;
        try {
            jsonObj = jsonObject.getJSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }

    public static JSONArray getJsonArray(JSONObject jsonObject, String key) {
        if(jsonObject == null) return null;

        JSONArray jsonArray = null;
        try {
            if (jsonObject.isNull(key)) return null;
            jsonArray = jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static List<JSONObject> getStatusInfoArrayAsList(JSONObject responseJSON) {
        if(responseJSON == null) return null;

        JSONArray ja = null;
        try {
            ja = responseJSON.getJSONArray("statusInfo");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getListFromJSONArray(ja);
    }

    public static List<JSONObject> getJsonArrayAsList(JSONObject jsonObject, String key) {
        if(jsonObject == null) return null;

        JSONArray ja = null;
        try {
            if (jsonObject.isNull(key)) return null;
            ja = jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getListFromJSONArray(ja);
    }

    public static List<JSONObject> getListFromJSONArray(JSONArray ja) {
        if(ja == null) return null;

        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    public static class Login {

        public static String getOAuthToken(JSONObject responseJSON) {
            String oauthToekn = null;

            try {
                JSONObject oauthJSON = responseJSON.getJSONObject("statusInfo");
                oauthToekn = oauthJSON.getString("access_token");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return oauthToekn;
        }

        public static String getUserID(JSONObject responseJSON) {
            String usrID = null;

            try {
                JSONObject oauthJSON = responseJSON.getJSONObject("statusInfo");
                usrID = oauthJSON.getString("usr_id");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return usrID;
        }
    }
}
