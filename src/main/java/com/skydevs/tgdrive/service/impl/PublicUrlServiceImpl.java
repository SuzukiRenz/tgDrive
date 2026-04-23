package com.skydevs.tgdrive.service.impl;

import com.skydevs.tgdrive.service.PublicUrlService;
import com.skydevs.tgdrive.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PublicUrlServiceImpl implements PublicUrlService {

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Override
    public String resolveBaseUrl(HttpServletRequest request, String customUrl) {
        if (StringUtil.hasText(customUrl)) {
            return StringUtil.normalizePublicBaseUrl(customUrl);
        }
        if (StringUtil.hasText(publicBaseUrl)) {
            return StringUtil.normalizePublicBaseUrl(publicBaseUrl);
        }
        return StringUtil.normalizePublicBaseUrl(StringUtil.getPrefix(request));
    }
}
