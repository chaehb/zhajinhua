package com.kusion.zhajinhua.server;

import com.kusion.zhajinhua.utils.ThreeDES;
import io.netty.util.AttributeKey;

/**
 * 键管理器
 */
public interface AppAttrKeys {

    public AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");
    public AttributeKey<String> USER_TOKEN = AttributeKey.valueOf("userToken");
    public AttributeKey<String> PLATFORM = AttributeKey.valueOf("platform");
    public AttributeKey<String> APP_VERSION = AttributeKey.valueOf("appversion");
    public AttributeKey<ThreeDES> ENCRYPT = AttributeKey.valueOf("encrypt");

}
