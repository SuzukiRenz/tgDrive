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
        String normalizedCustomUrl = StringUtil.normalizePublicBaseUrl(customUrl);
        if (StringUtil.hasText(normalizedCustomUrl)) {
            return normalizedCustomUrl;
        }

        String normalizedPublicBaseUrl = StringUtil.normalizePublicBaseUrl(publicBaseUrl);
        if (StringUtil.hasText(normalizedPublicBaseUrl)) {
            return normalizedPublicBaseUrl;
        }

        return StringUtil.normalizePublicBaseUrl(StringUtil.getPrefix(request));
    }
}
