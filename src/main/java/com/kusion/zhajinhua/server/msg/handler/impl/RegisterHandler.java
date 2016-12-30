package com.kusion.zhajinhua.server.msg.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

import com.kusion.zhajinhua.manager.AppManager;
import com.kusion.zhajinhua.manager.AppRouterManager;
import com.kusion.zhajinhua.manager.LogTAGManager;
import com.kusion.zhajinhua.protobuf.CdeerMsg.LoginInfo;
import com.kusion.zhajinhua.protobuf.CdeerMsg.Message;
import com.kusion.zhajinhua.redis.RedisDBProvider;
import com.kusion.zhajinhua.server.AppAttrKeys;
import com.kusion.zhajinhua.server.msg.handler.AppMsgHandler;

/**
 * 登录消息处理器
 */
public class RegisterHandler implements AppMsgHandler {

	/**
	 * 创建日志对象
	 */
	private final Logger Log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Channel channel, Message msg) {

		try {
			LoginInfo loginInfo = msg.getLoginInfo();
			long userId = loginInfo.getUserId();
			String token = loginInfo.getToken();
			String platform = loginInfo.getPlatform().toLowerCase();
			String appVersion = loginInfo.getAppVersion();

			Log.info(LogTAGManager.CLIENT_LOGIN_INFO + "userId:" + userId
					+ LogTAGManager.LOG_SEPARATE_PARAMS + "token:" + token
					+ LogTAGManager.LOG_SEPARATE_PARAMS + "platform:"
					+ platform + LogTAGManager.LOG_SEPARATE_PARAMS
					+ "appversion:" + appVersion);

			// token验证
			String user_token = RedisDBProvider.getUserInfo(userId,
					"user_token");
			if ((user_token == null || "".equals(user_token) || (!user_token
					.equals(token)))) {
				int code = 1;
				String info = "登录验证失败";
				int expose = 1;
				AppRouterManager
						.routeError(channel, code, info, expose, userId);
				channel.close();
				return;
			}

			// 判断是否有旧连接
			Channel oldChannel = AppManager.getConn(userId);
			if (oldChannel != null) {
				// 发送错误码，关闭旧连接
				AppManager.removeConn(userId);
				oldChannel.attr(AppAttrKeys.USER_ID).set(0L);

				int code = 2;
				String info = "您已经在另外一台设备上登录";
				int expose = 1;
				AppRouterManager.routeError(oldChannel, code, info, expose,
						userId);
				oldChannel.close();
			}

			// 添加连接
			AppManager.addConn(userId, channel);
			channel.attr(AppAttrKeys.USER_ID).set(userId);

			// 返回登录成功
			AppRouterManager.routeLoginSuccess(channel);

			// 存储用户信息
			RedisDBProvider.setUserInfo(userId, "platform",
					platform.toLowerCase());
			RedisDBProvider.setUserInfo(userId, "app_version", appVersion);

		} catch (Exception e) {
			Log.error(e.getMessage(), e);
			Log.error(LogTAGManager.CLIENT_ERROR + "msg:" + msg.toString());
		}

	}

}
