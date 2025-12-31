package com.xhs.clothingpatternbackend.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP地理位置工具类
 * 用于从IP地址解析省份信息
 */
@Slf4j
public class IpLocationUtils {

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多次反向代理后会有多个IP值，第一个为真实IP
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 根据IP地址获取省份信息
     * 使用太平洋IP查询接口（免费）
     *
     * @param ip IP地址
     * @return 省份名称，解析失败返回null
     */
    public static String getProvinceByIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return null;
        }
        // 本地IP地址不做解析
        if (isLocalIp(ip)) {
            log.info("本地IP地址不做解析: {}", ip);
            return null;
        }
        try {
            // 使用太平洋IP查询接口
            String url = "https://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String result = HttpUtil.get(url, 3000);
            if (StrUtil.isBlank(result)) {
                log.warn("IP解析接口返回为空, ip: {}", ip);
                return null;
            }
            JSONObject jsonObject = JSONUtil.parseObj(result);
            String province = jsonObject.getStr("pro");
            if (StrUtil.isNotBlank(province)) {
                log.info("IP: {} 解析省份: {}", ip, province);
                return province;
            }
            // 备用方案：使用ip-api.com（国际接口）
            return getProvinceByIpApi(ip);
        } catch (Exception e) {
            log.error("解析IP省份失败, ip: {}, error: {}", ip, e.getMessage());
            // 备用方案
            return getProvinceByIpApi(ip);
        }
    }

    /**
     * 备用方案：使用ip-api.com接口
     *
     * @param ip IP地址
     * @return 省份名称
     */
    private static String getProvinceByIpApi(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";
            String result = HttpUtil.get(url, 3000);
            if (StrUtil.isBlank(result)) {
                return null;
            }
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if ("success".equals(jsonObject.getStr("status"))) {
                String regionName = jsonObject.getStr("regionName");
                log.info("IP: {} 通过备用接口解析省份: {}", ip, regionName);
                return regionName;
            }
        } catch (Exception e) {
            log.error("备用IP解析接口失败, ip: {}, error: {}", ip, e.getMessage());
        }
        return null;
    }

    /**
     * 判断是否为本地IP地址
     *
     * @param ip IP地址
     * @return 是否为本地IP
     */
    private static boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip) 
                || "0:0:0:0:0:0:0:1".equals(ip) 
                || "::1".equals(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || (ip.startsWith("172.") && isPrivate172(ip));
    }

    /**
     * 判断172.x.x.x是否为私有IP
     */
    private static boolean isPrivate172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从request获取省份信息
     *
     * @param request HttpServletRequest
     * @return 省份名称
     */
    public static String getProvinceFromRequest(HttpServletRequest request) {
        String ip = getClientIp(request);
        return getProvinceByIp(ip);
    }
}
