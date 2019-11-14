package com.dkm.hjdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * 类名 : 数据统计 打点 <br/>
 * 作者 : canyon / 龚阳辉 <br/>
 * 时间 : 2018-01-10 14：37 <br/>
 * 功能 :
 */
@SuppressWarnings("deprecation")
public class HJDLStatistics {
	static final public String URL = "http://z.info.cd9130.com:8122";
	static final String gid = "22";
	
	// 添加 线程池
	static ScheduledExecutorService m_excutor = Executors.newScheduledThreadPool(2);

	static public String client_ver = "1.0";
	static String uuid = "";
	static String os = "1";
	public String is_first = "0";
	static String tracertid = "";
	static int thr_index = 0;

	public String appid = "";
	public String eventtype = "";
	public String model = "";
	public String mac = "";
	public String simoperator = "";
	public String network = "";
	public String ip = "";
	public String imei = "";
	public String step = "";
	public String time_c = "";
	public String ext = "";

	private boolean isInit = false;
	protected boolean isUUIDRnd = false;

	protected boolean isLogger = true;
	protected String logHead = "HJDLStatistics";

	protected void logInfo(String msg) {
		if (!isLogger || msg == null || msg.length() <= 0)
			return;

		System.out.println(String.format("== %s ==,msg = [%s]", logHead, msg));
	}

	public HJDLStatistics Init(String eventtype, String step, String extJson) {
		getUUID();
		client_ver = SDKPlgDKM.getVersionName();
		this.appid = SDKPlgDKM.getPkgName();
		this.model = SDKPlgDKM.getBModel();
		this.mac = SDKPlgDKM.getMacAddress();
		this.simoperator = SDKPlgDKM.getSimOperator();
		this.network = SDKPlgDKM.getNetWorkStatus();
		this.ip = SDKPlgDKM.getIPAddress();
		this.imei = SDKPlgDKM.getIMEI();
		long sec = System.currentTimeMillis() / 1000;
		this.time_c = String.valueOf(sec);

		this.step = step;
		this.eventtype = eventtype;
		this.ext = extJson;

		this.isInit = true;
		// logInfo(this.toString());
		return this;
	}

	public HJDLStatistics Init(int ntype, int nstep) {
		return Init(String.valueOf(ntype), String.valueOf(nstep), "");
	}

	String getUUID() {
		this.is_first = "0";
		if (uuid == null || "".equals(uuid)) {
			String _tmp = "";
			String _fdir = "";
			long lnow = System.currentTimeMillis();
			
			try {
				_fdir = SDKPlgDKM.getDiskFileDir();
				_tmp = String.format("%s%s", _fdir, "hjdl_uuid");
				logInfo("绝对地址:" + _tmp);
				File file = new File(_tmp);
				if (file.exists()) {
					FileInputStream inStream = new FileInputStream(file);
					int size = inStream.available();
					byte[] buf = new byte[size];
					inStream.read(buf);
					inStream.close();
					uuid = new String(buf, "UTF-8");
				} else {
					if (isUUIDRnd)
						_tmp = UUID.randomUUID().toString();
					else
						_tmp = SDKPlgDKM.getUUID();

					_tmp = String.format("%s_%s", _tmp, lnow);
					uuid = com.bowlong.security.MD5.MD5Encode(_tmp);

					this.is_first = "1";
					FileOutputStream outStrem = new FileOutputStream(file);
					outStrem.write(uuid.getBytes("UTF-8"));
					outStrem.flush();
					outStrem.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (uuid != null && !"".equals(uuid)) {
					_tmp = String.format("%s_%s", uuid, lnow);
					tracertid = com.bowlong.security.MD5.MD5Encode(_tmp);
				}
			}
		}
		return uuid;
	}

	private List<BasicNameValuePair> getList() {
		List<BasicNameValuePair> ret = new ArrayList<BasicNameValuePair>();
		ret.add(new BasicNameValuePair("tracertid", tracertid));
		ret.add(new BasicNameValuePair("eventtype", eventtype));
		ret.add(new BasicNameValuePair("model", model));
		ret.add(new BasicNameValuePair("mac", mac));
		ret.add(new BasicNameValuePair("os", os));
		ret.add(new BasicNameValuePair("simoperator", simoperator));
		ret.add(new BasicNameValuePair("network", network));
		ret.add(new BasicNameValuePair("ip", ip));
		ret.add(new BasicNameValuePair("imei", imei));
		ret.add(new BasicNameValuePair("uuid", uuid));
		ret.add(new BasicNameValuePair("is_first", is_first));
		ret.add(new BasicNameValuePair("gid", gid));
		ret.add(new BasicNameValuePair("appid", appid));
		ret.add(new BasicNameValuePair("client_ver", client_ver));
		ret.add(new BasicNameValuePair("step", step));
		ret.add(new BasicNameValuePair("time_c", time_c));
		ret.add(new BasicNameValuePair("ext", ext));
		return ret;
	}

	public void SendKVEntity() {
		try {
			SendEntity(new UrlEncodedFormEntity(getList(), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getJson() {
		try {
			JSONObject ret = new JSONObject();
			ret.put("tracertid", tracertid);
			ret.put("eventtype", eventtype);
			ret.put("model", model);
			ret.put("mac", mac);
			ret.put("os", os);
			ret.put("simoperator", simoperator);
			ret.put("network", network);
			ret.put("ip", ip);
			ret.put("imei", imei);
			ret.put("uuid", uuid);
			ret.put("is_first", is_first);
			ret.put("gid", gid);
			ret.put("appid", appid);
			ret.put("client_ver", client_ver);
			ret.put("step", step);
			ret.put("time_c", time_c);
			ret.put("ext", ext);
			return ret.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	public void SendJson() {
		try {
			SendEntity(new StringEntity(getJson(), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// conTimeOut 请求超时 - ms, soTimeOut 读取超时 - ms
	DefaultHttpClient getHttpClient(int conTimeOutMs,int soTimeOutMs){  
        BasicHttpParams httpParams = new BasicHttpParams();  
        HttpConnectionParams.setConnectionTimeout(httpParams, conTimeOutMs);  
        HttpConnectionParams.setSoTimeout(httpParams, soTimeOutMs);
        return new DefaultHttpClient(httpParams);
    }

	protected void SendEntity(HttpEntity entity) {
		try {
			DefaultHttpClient m_client = getHttpClient(15000, 5000);
			HttpPost m_httpPost = new HttpPost(URL);
			m_httpPost.setEntity(entity);
			HttpResponse httpResponse = m_client.execute(m_httpPost);

			String strResult = EntityUtils.toString(httpResponse.getEntity());
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				logInfo("Fails," + strResult);
			} else {
				logInfo("Successes," + strResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void DoStatistices() {
		if (!this.isInit) {
			logInfo("还没初始化!");
			return;
		}

		if (URL == null || "".equals(URL)) {
			logInfo("URL 为空!");
			return;
		}

		if (tracertid == null || "".equals(tracertid)) {
			logInfo("tracertid 为空!");
			return;
		}
		
		m_excutor.execute(new Runnable() {
			@Override
			public void run() {
				// SendJson();
				SendKVEntity();
			}
		});
	}

	@Override
	public String toString() {
		return "HJDLStatistics [is_first=" + is_first + ", isInit=" + isInit
				+ ", appid=" + appid + ", eventtype=" + eventtype + ", model="
				+ model + ", mac=" + mac + ", simoperator=" + simoperator
				+ ", network=" + network + ", ip=" + ip + ", imei=" + imei
				+ ", step=" + step + ", time_c=" + time_c + ", ext=" + ext
				+ ", uuid=" + uuid + ", client_ver=" + client_ver
				+ ", tracertid=" + tracertid + "]";
	}

	public static void main(String[] args) {
		long lnow = System.currentTimeMillis();
		String _tmp, _md5;
		_tmp = String.format("%s_%s", UUID.randomUUID().toString(), lnow);
		_md5 = com.bowlong.security.MD5.MD5Encode(_tmp);
		System.out.println(_md5);

		_tmp = String.format("%s_%s", _md5, lnow);
		_md5 = com.bowlong.security.MD5.MD5Encode(_tmp);
		System.out.println(_md5);

		HJDLStatistics ret = new HJDLStatistics();
		ret.isUUIDRnd = true;
		System.out.println(ret.getJson());
		System.out.println(ret.getUUID());
	}
}
