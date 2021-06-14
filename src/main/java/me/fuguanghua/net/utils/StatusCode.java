package me.fuguanghua.net.utils;

/**
 * 全局状态码
 */
public interface StatusCode {

    /**
     * 服务器异常
     */
    short SERVER_ERROR = -1;

    /**
     * 成功
     */
    short SUCCESS = 0;

    /**
     * 无返回结果
     */
    short NO_RESULTS = 1;

    /**
     * 数据值内容错误
     */
    short DATA_VALUE_ERROR = 2;

    /**
     * 没有权限
     */
    short NO_RIGHT = 3;

    /**
     * 服务器停机维护中
     */
    short MAINTAIN_SERVER = 7;
}