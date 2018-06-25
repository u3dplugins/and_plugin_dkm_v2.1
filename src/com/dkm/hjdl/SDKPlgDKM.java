package com.dkm.hjdl;

import org.json.JSONObject;

import cc.jyslproxy.framework.bean.JyslPayParam;
import cc.jyslproxy.framework.bean.JyslRoleParam;
import cc.jyslproxy.framework.callback.JyslResultCallback;
import cc.jyslproxy.openapi.JyslSDK;

import com.sdkplugin.bridge.U3DBridge;
import com.sdkplugin.extend.PluginBasic;
import com.sdkplugin.tools.Tools;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * 类名 : 哆可梦 渠道的SDK <br/>
 * 作者 : canyon / 龚阳辉 <br/>
 * 时间 : 2018-01-02 17：01 <br/>
 * 功能 : 实现登录,切换帐号,注销,支付等,也是实现了消息监听
 */
public class SDKPlgDKM extends PluginBasic implements JyslResultCallback {
	static final String CMD_DKM_Init = "/dkm/init";
	static final String CMD_DKM_ChangeUser = "/dkm/changeUser";

	static final String CMD_DKM_InitRinfo = "/dkm/initRInfo";
	static final String CMD_DKM_ReRname = "/dkm/reRname";
	static final String CMD_DKM_Login = "/dkm/login";
	static final String CMD_DKM_Logout = "/dkm/logout";
	static final String CMD_DKM_Pay = "/dkm/pay";
	static final String CMD_DKM_ExitGame = "/dkm/exitGame";
	static final String CMD_DKM_CancelExitGame = "/dkm/cancelExitGame";

	static final String CMD_DKM_RCreate = "/dkm/rCreate";
	static final String CMD_DKM_REntryGame = "/dkm/rEntryGame";
	static final String CMD_DKM_RUpLv = "/dkm/rUpLv";

	// 取得登录后的用户信息
	static final String CMD_DKM_GetUser = "/dkm/getUser";

	// 数据统计
	static final String CMD_DKM_Statistics = "/dkm/statistics";
	
	// bugly
	static final String CMD_DKM_Bugly = "/dkm/bugly";

	boolean isInitSuccess = false;

	String gameId = "", partnerId = "", sdk = "", gamePkg = "";

	String userid = "", account = "", token = "";

	// 角色相关信息
	String rid = "", rname = "", svid = "", svname = "",
			createtime = "1456397360";

	int rlv = 0;

	enum RinfoState {
		Create, EntryGame, UpLv
	}

	public SDKPlgDKM() {
		super();
		this.logLevel = LEV_LOG_NORMAL;
		this.logHead = "dkm";
	}

	@Override
	public void receiveFromUnity(String json) throws Exception {
		if (json == null || json.length() <= 0) {
			logMust("json is null or empty");
			return;
		}
		logInfo(json);

		JSONObject obj = new JSONObject(json);
		final String cmd = obj.getString("cmd");
		handlerMsg(cmd, obj);
	}

	void UserInfo(String code) throws Exception {
		JSONObject data = new JSONObject();
		data.put("gameId", gameId);
		data.put("partnerId", partnerId);
		data.put("gamePkg", gamePkg);
		data.put("sdk", sdk);
		data.put("token", token);
		data.put("userid", userid);

		data.put("imei", getIMEI());
		data.put("device", 1);
		data.put("ifda", "");

		if (code != null && !"".equals(code)) {
			data.put("cmd", code);
			Tools.msg2U3D(CODE_SUCCESS, "", data);

			data.remove("cmd");
		}

		data.put("cmd", CMD_DKM_GetUser);
		Tools.msg2U3D(CODE_SUCCESS, "", data);
	}

	@Override
	public void onResult(int code, JSONObject data) {
		try {
			switch (code) {
			case JyslResultCallback.CODE_INIT_SUCCESS:
				gameId = data.getString("game_id");
				// partner_id 对应映射平台关系请参考对接文档中平台号对应表格
				partnerId = data.getString("partner_id");
				gamePkg = JyslSDK.getInstance().getGamePkg();
				sdk = JyslSDK.getInstance().getSdkPartnerid();

				logInfo("初始化成功");
				isInitSuccess = true;
				Tools.msg2U3D(CODE_SUCCESS, "", Tools.ToData(CMD_DKM_Init, ""));
				new HJDLStatistics().Init(2, 1).DoStatistices();
				break;
			case JyslResultCallback.CODE_INIT_FAILURE:
				logMust("初始化失败,需再次调用初始化方法");
				Tools.msg2U3D(CODE_FAILS, "初始化失败,会再次调用初始化方法的",
						Tools.ToData(CMD_DKM_Init, ""));
				JyslSDK.getInstance().init(getCurActivity());
				break;
			case JyslResultCallback.CODE_LOGIN_SUCCESS:
				logInfo("登录成功");
				userid = data.getString("userid");
				account = data.getString("account");
				token = data.getString("token");

				UserInfo(CMD_DKM_Login);
				break;
			case JyslResultCallback.CODE_LOGIN_FAILURE:
				logMust("登录失败");
				Tools.msg2U3D(CODE_FAILS, "登录失败",
						Tools.ToData(CMD_DKM_Login, ""));
				break;
			case JyslResultCallback.CODE_SWITCH_ACCOUNT_SUCCESS:
				logInfo("切换帐号成功");
				userid = data.getString("userid");
				account = data.getString("account");
				token = data.getString("token");

				UserInfo(CMD_DKM_ChangeUser);
				break;
			case JyslResultCallback.CODE_SWITCH_ACCOUNT_FAILURE:
				logMust("切换帐号失败");
				Tools.msg2U3D(CODE_FAILS, "切换帐号失败",
						Tools.ToData(CMD_DKM_ChangeUser, ""));
				break;
			case JyslResultCallback.CODE_LOGOUT_SUCCESS:
				logInfo("注销成功");
				userid = "";
				account = "";
				token = "";
				Tools.msg2U3D(CODE_SUCCESS, "",
						Tools.ToData(CMD_DKM_Logout, ""));
				break;
			case JyslResultCallback.CODE_LOGOUT_FAILURE:
				logInfo("注销失败");
				Tools.msg2U3D(CODE_FAILS, "", Tools.ToData(CMD_DKM_Logout, ""));
				break;
			case JyslResultCallback.CODE_PAY_SUCCESS:
				// 支付成功 ， 这个成功并不一定是成功，有的平台是异步的，只是订单流程走通
				logInfo("支付流程成功，请等待服务器发送资源!");
				Tools.msg2U3D(CODE_SUCCESS, "支付流程成功，请等待服务器发资源!",
						Tools.ToData(CMD_DKM_Pay, ""));
				break;
			case JyslResultCallback.CODE_PAY_WAIT:
				logInfo("已支付，等待确认");
				Tools.msg2U3D(CODE_WAIT, "已支付，等待确认",
						Tools.ToData(CMD_DKM_Pay, ""));
				break;
			case JyslResultCallback.CODE_PAY_FAILURE:
				logMust("支付失败");
				Tools.msg2U3D(CODE_FAILS, "支付失败", Tools.ToData(CMD_DKM_Pay, ""));
				break;
			case JyslResultCallback.CODE_PAY_CANCEL:
				logMust("支付取消");
				Tools.msg2U3D(CODE_FAILS, "支付取消", Tools.ToData(CMD_DKM_Pay, ""));
				break;
			case JyslResultCallback.EXITGAME:
				logMust("处理游戏关闭逻辑");
				// 第三方渠道有自己退出界面的时候，当点击第三方渠道的确定退出按钮时候的回调,此时在处理自身的逻辑
				MainActivity.sendMsg(0);
				break;
			case JyslResultCallback.CANCELEXITGAME:
				logInfo("第三方取消了退出");
				Tools.msg2U3D(CODE_SUCCESS, "第三方取消了退出!",
						Tools.ToData(CMD_DKM_CancelExitGame, ""));
				break;
			default:
				logInfo(String.format("cmd=[%s],msg=[%s]", code, jsonData));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isInitSuccessed(String extDesc) {
		if (!isInitSuccess)
			System.out.println(String.format("初始化未完成，请稍后再次尝试登陆(%s)", extDesc));

		return isInitSuccess;
	}

	void login() {
		if (!isInitSuccessed("login")) {
			return;
		}
		JyslSDK.getInstance().login();
	}

	void logout() {
		if (!isInitSuccessed("logout")) {
			return;
		}
		JyslSDK.getInstance().logout();
	}

	void updateRoleInfo(int lv, RinfoState emState) {
		this.rlv = lv;

		if (!isInitSuccessed("updateRoleInfo")) {
			return;
		}

		JyslRoleParam roleParam = new JyslRoleParam();
		roleParam.setRoleId(rid);// 角色ID，字符串类型
		roleParam.setRoleName(rname);// 角色名字，字符串类型
		roleParam.setRoleLevel(rlv);// 角色等级，int类型
		roleParam.setServerId(svid);// 服务器ID，字符串类型
		roleParam.setServerName(svname); // 服务器名字，字符串类型
		// 获取服务器存储的角色创建时间,时间戳，单位秒，长度10，不可用本地手机时间
		// ，同一角色创建时间不可变，上线UC联运必需接入，（字符串类型，sdk内如会有转换）
		roleParam.setRoleCreateTime(createtime);
		switch (emState) {
		case Create:
			JyslSDK.getInstance().createRole(roleParam);
			break;
		case UpLv:
			JyslSDK.getInstance().roleUpLevel(roleParam);
			break;
		default:
			JyslSDK.getInstance().enterGame(roleParam);
			break;
		}
	}

	void InitRinfo(String rid, String rname, String svid, String svname,
			String createtime) {
		this.rid = rid;
		ReRname(rname);
		this.svid = svid;
		this.svname = svname;
		this.createtime = createtime;
	}

	void ReRname(String rname) {
		this.rname = rname;
	}

	// 支付 price 单位为分
	void pay(String cpOrderId, String pdId, String pdName, String pdDesc,
			float price, String ext) {

		if (!isInitSuccessed("pay")) {
			return;
		}

		JyslPayParam payParam = new JyslPayParam();
		payParam.setCpBill(cpOrderId); // cp（游戏方）订单，字符串类型
		payParam.setProductId(pdId); // 商品标识 ，字符串类型
		payParam.setProductName(pdName); // 商品名称，字符串类型
		payParam.setProductDesc(pdDesc); // 商品说明，字符串类型
		payParam.setServerId(svid); // 服务器编号，字符串类型
		payParam.setServerName(svname);// 服务器名称
		payParam.setRoleId(rid); // 角色id，字符串类型
		payParam.setRoleName(rname); // 角色名，字符串类型
		payParam.setRoleLevel(rlv); // 角色等级（int类型）
		payParam.setPrice(price / 100); // 价格(单位元)(float 类型)
		payParam.setExtension(ext);// 会原样返回给游戏，字符串类型
		JyslSDK.getInstance().pay(payParam);
	}

	public void handlerMsg(final String code, JSONObject data) throws Exception {
		String val1 = "", val2 = "", val3 = "";
		switch (code) {
		case CMD_DKM_Init:
			if (isInitSuccess) {
				Tools.msg2U3D(CODE_SUCCESS, "", Tools.ToData(CMD_DKM_Init, ""));
			}
			break;
		case CMD_DKM_InitRinfo:
			val1 = data.getString("rid");
			val2 = data.getString("rname");
			val3 = data.getString("svid");
			InitRinfo(val1, val2, val3, data.getString("svname"),
					data.getString("createtime"));
			break;
		case CMD_DKM_ReRname:
			ReRname(data.getString("rname"));
			break;
		case CMD_DKM_Login:
			login();
			break;
		case CMD_DKM_Logout:
			logout();
			break;
		case CMD_DKM_Pay:
			val1 = data.getString("orid");
			val2 = data.getString("pdid");
			val3 = data.getString("pdname");
			pay(val1, val2, val3, data.getString("pddesc"),
					data.getInt("price"), data.getString("ext"));
			break;
		case CMD_DKM_ExitGame:
			MainActivity.sendMsg(0);
			break;
		case CMD_DKM_RCreate:
			// 创建角色
			updateRoleInfo(data.getInt("lv"), RinfoState.Create);
			break;
		case CMD_DKM_REntryGame:
			// 进入游戏
			updateRoleInfo(data.getInt("lv"), RinfoState.EntryGame);
			break;
		case CMD_DKM_RUpLv:
			// 角色升级
			updateRoleInfo(data.getInt("lv"), RinfoState.UpLv);
			break;
		case CMD_DKM_GetUser:
			UserInfo("");
			break;
		case CMD_DKM_Statistics:
			HJDLStatistics oneStatistics = new HJDLStatistics();
			val1 = data.getString("ntype");
			val2 = data.getString("nstep");
			if (data.has("ext")) {
				val3 = data.getString("ext");
			}
			oneStatistics.Init(val1, val2, val3);
			oneStatistics.DoStatistices();
			break;
		case CMD_DKM_Bugly:
			if (data.has("ntype")) {
				val1 = data.getString("ntype");
			}
			if("1".equals(val1))
				CrashReport.testANRCrash();
			else if("2".equals(val1))
				CrashReport.testNativeCrash();
			else
				CrashReport.testJavaCrash();
			break;
		default:
			handlerJson(data);
			break;
		}
	}

	static SDKPlgDKM _instance;

	static public SDKPlgDKM getInstance() {
		if (_instance == null) {
			_instance = new SDKPlgDKM();

			U3DBridge.setListener(_instance);
		}
		return _instance;
	}
}
