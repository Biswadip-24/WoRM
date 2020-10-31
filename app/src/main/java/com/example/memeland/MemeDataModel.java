package com.example.memeland;
import org.json.JSONException;
import org.json.JSONObject;

public class MemeDataModel {

    private String[] urls;
    public static MemeDataModel fromJSON(JSONObject jsonObject)
    {

        MemeDataModel memeData=new MemeDataModel();
        try {
                int length = jsonObject.getJSONObject("data").getInt("dist");
                memeData.urls = new String[length];
                int start = startIndex(length);

                for(int k = 0;k < length ; k++){
                    if(start == length)
                        start = 0;
                    String s = jsonObject.getJSONObject("data").getJSONArray("children").getJSONObject(start).getJSONObject("data").getString("url");
                    if (checkValidUrl(s))
                        memeData.urls[k] = s;

                    start++;
                }
            }

        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }

        return memeData;
    }

    public static boolean checkValidUrl(String s)
    {
        return ((s.contains(".jpg") || s.contains(".png") || s.contains(".gif")) && !s.contains(".gifv"));
    }
    public static int startIndex(int a)
    {
        return (int)(Math.random()*(a));
    }

    public String[] getUrls() {
        return urls;
    }
}
