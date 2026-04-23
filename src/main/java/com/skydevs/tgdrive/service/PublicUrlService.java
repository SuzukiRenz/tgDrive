package com.skydevs.tgdrive.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 对外公开链接基址解析服务
 */
public interface PublicUrlService {

    /**
     * 解析公开访问基址。
     * 优先级：customUrl > app.public-base-url > request推导
     *
     * @param request 当前请求
     * @param customUrl 自定义公开URL，可为空
     * @return 规范化后的公开基址，例如 https://files.example.com
     */
    String resolveBaseUrl(HttpServletRequest request, String customUrl);
}
