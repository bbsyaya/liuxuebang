package com.origwood.liuxue.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.origwood.liuxue.bean.Result;
import com.origwood.liuxue.bean.User;
import com.origwood.liuxue.ui.PerfectInfo;
import com.origwood.liuxue.util.ImageTools;
import com.origwood.liuxue.util.Json2Bean;

/**
 * 所有业务方法
 * 
 * @author wanggang
 * @date： 2013-4-14 email: 315331371@qq.com
 */
@Singleton
public class AppService {
	private Context context;
	private static final String DUG_TAG = "AppService";

	public AppService() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AppService(Context context) {
		super();
		this.context = context;
	}

	static AsyncHttpClient client = new AsyncHttpClient();

	public static void stop(Context context, boolean b) {
		client.cancelRequests(context, b);
	}

	public void modifyPwd(String newPwd, String oldPwd,
			final AppServiceOnFinished a, final Context context) {
		RequestParams params = new RequestParams();
		params.put("newPwd", newPwd);
		params.put("oldPwd", oldPwd);
		Log.d(DUG_TAG, URLs.MODIFYPWD);
		client.get(URLs.MODIFYPWD, params, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Result result = new Result();
				result.setMsg("连接错误");
				a.onFailed(result);
				Log.e(DUG_TAG, arg0 + arg1);
			}

			@Override
			public void onSuccess(String response) {
				Result result = Json2Bean.getResult(response);
				if (result.getSubResultType() == 0) {
					a.onFailed(result);
				} else {
					PersistentCookieStore myCookieStore = new PersistentCookieStore(
							context);
					client.setCookieStore(null);
					client.setCookieStore(myCookieStore);
					a.onSuccess(result);
				}
			}

		});
	}

	public static Object getUserById(String id, final AppServiceOnFinished a) {

		client.get("http://42.96.136.159/app/subUserLogin",
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						Result result = Json2Bean.getResult(response);
						if (result.getSubResultType() == 0) {
							a.onFailed(result);
						} else {
							String str = "wanggang";
							a.onSuccess(str);
						}
					}

				});

		return null;
	}

	public void login(String username, String password,
			final AppServiceOnFinished a, final Context context) {
		RequestParams params = new RequestParams();
		params.put("loginName", username);
		params.put("password", password);

		client.get(URLs.LOGIN, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {

				Result result = Json2Bean.getResult(response);
				if (result.getSubResultType() == 0) {
					a.onFailed(result);

				} else {
					PersistentCookieStore myCookieStore = new PersistentCookieStore(
							context);
					client.setCookieStore(myCookieStore);
					a.onSuccess(result);
				}
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {

				Result result = new Result();
				result.setMsg("连接出错");
				a.onFailed(result);

				super.onFailure(arg0, arg1);
			}

		});
	}

	public void getUserInfo(final AppServiceOnFinished onFinished) {

		client.get(URLs.USERINFO, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Result result = Json2Bean.getResult(response);
				if (result.getSubResultType() == 0) {
					onFinished.onFailed(result);
				} else {
					User user = Json2Bean.getBeanFromHasResult(response,
							User.class);
					onFinished.onSuccess(user);
				}
			}

		});
	}

	public void checkIsRegister(String username,
			final AppServiceOnFinished onFinished) {

		RequestParams params = new RequestParams();
		params.put("loginName", username);
		client.get(URLs.CHECKISREGISTER, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						Result result = Json2Bean.getResult(response);
						if (result.getSubResultType() == 0) {
							onFinished.onFailed(result);
						} else {
							onFinished.onSuccess(null);
						}
					}
				});
	}

	public void subInfoSetting(Drawable usericon, String sex, String nickname,
			String stage, String phone, final AppServiceOnFinished onFinished,
			final Context context) {
		Log.i(DUG_TAG, sex+stage);
		RequestParams params = new RequestParams();
		byte[] icon=ImageTools.getInstance().Drawable2Bytes(usericon);
		params.put("headImgFile", new ByteArrayInputStream(icon),"icon.png");
		params.put("sex", sex);
		params.put("nickName", nickname);
		params.put("stage", stage);
		params.put("mobilePhone", phone);
		client.post(URLs.INFOSETTING, params,new AsyncHttpResponseHandler(){

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Log.e(DUG_TAG, arg0+arg1);
				onFinished.onFailed(null);
			}

			@Override
			public void onSuccess(String response) {
				Log.i(DUG_TAG, response);
				Result result = Json2Bean.getResult(response);
				if (result.getSubResultType() == 0) {
					onFinished.onFailed(result);
				} else {
					onFinished.onSuccess(null);
				}
			}
			
		});
	}
	public void getUserAllStage(final Context context,final AbsAppServiceOnFinished onFinished) {
		final Result result=new Result();
		client.get(URLs.GETUSERALLSTAGE, new AsyncHttpResponseHandler(){
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Log.e(DUG_TAG, "网络异常");
				result.setMsg("网络异常");
				onFinished.onFailed(result);
			}

			@Override
			public void onSuccess(String content) {
				Log.i(DUG_TAG, content);
				try {
					JSONObject jsonObject = new JSONObject(content);
					int subResultType=jsonObject.optInt("subResultType");
					if(subResultType==1){
						LinkedHashMap<String, String> values=new LinkedHashMap<String, String>();
						values.clear();
						JSONArray jsonArray=jsonObject.getJSONArray("items");
						for(int i=0;i<jsonArray.length();i++){
							JSONObject obj=jsonArray.getJSONObject(i);
							String name=obj.getString("name");
							String value=obj.getString("value");
							values.put(name, value);
							Log.i(DUG_TAG, name+"==="+ value);
						}
						onFinished.onSuccess(values);
						return;
					}
					result.setMsg("服务器异常");
					onFinished.onFailed(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
