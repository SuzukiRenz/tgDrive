package com.skydevs.tgdrive.utils;

import com.pengrad.telegrambot.model.Message;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtil {
    /**
     * 获取前缀
     * @param request HTTP请求
     * @return 前缀
     */
    public static String getPrefix(HttpServletRequest request) {
        String protocol = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme());
        String host = extractHost(request);
        int port = extractPort(request, protocol);

        // 对外公开链接中，标准端口不应显式出现在URL里；
        // 对于反向代理 / CDN / Tunnel 场景，https + 80、http + 443 通常是源站回源端口污染，统一纠正。
        if ("https".equalsIgnoreCase(protocol) && port == 80) {
            port = 443;
        } else if ("http".equalsIgnoreCase(protocol) && port == 443) {
            port = 80;
        }

        if ((protocol.equalsIgnoreCase("http") && port == 80) || (protocol.equalsIgnoreCase("https") && port == 443)) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port;
    }

    private static String headerOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
    }

    private static String extractHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String hostHeader = (forwardedHost != null && !forwardedHost.trim().isEmpty()) ? forwardedHost : request.getHeader("Host");
        if (hostHeader != null && !hostHeader.trim().isEmpty()) {
            String firstHost = hostHeader.split(",")[0].trim();
            if (firstHost.startsWith("[")) {
                int end = firstHost.indexOf(']');
                if (end > 0) {
                    return firstHost.substring(0, end + 1);
                }
            }
            int colonIndex = firstHost.lastIndexOf(':');
            if (colonIndex > 0 && firstHost.indexOf(':') == colonIndex) {
                return firstHost.substring(0, colonIndex);
            }
            return firstHost;
        }
        return request.getServerName();
    }

    private static int extractPort(HttpServletRequest request, String protocol) {
        String forwardedPort = request.getHeader("X-Forwarded-Port");
        if (forwardedPort != null && !forwardedPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(forwardedPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String hostHeader = (forwardedHost != null && !forwardedHost.trim().isEmpty()) ? forwardedHost : request.getHeader("Host");
        Integer portFromHost = extractPortFromHost(hostHeader);
        if (portFromHost != null) {
            return portFromHost;
        }

        int serverPort = request.getServerPort();
        if (serverPort > 0) {
            return serverPort;
        }
        return protocol.equalsIgnoreCase("https") ? 443 : 80;
    }

    private static Integer extractPortFromHost(String hostHeader) {
        if (hostHeader == null || hostHeader.trim().isEmpty()) {
            return null;
        }
        String firstHost = hostHeader.split(",")[0].trim();
        if (firstHost.startsWith("[")) {
            int end = firstHost.indexOf(']');
            if (end > 0 && firstHost.length() > end + 2 && firstHost.charAt(end + 1) == ':') {
                try {
                    return Integer.parseInt(firstHost.substring(end + 2));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }
        int colonIndex = firstHost.lastIndexOf(':');
        if (colonIndex > 0 && firstHost.indexOf(':') == colonIndex) {
            try {
                return Integer.parseInt(firstHost.substring(colonIndex + 1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String normalizePublicBaseUrl(String rawUrl) {
        if (!hasText(rawUrl)) {
            return null;
        }

        String value = rawUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();

            // 只接受绝对 http/https URL，避免把 /admin、admin 之类相对路径当成公开基址。
            if (!hasText(scheme) || !hasText(host)
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return null;
            }

            boolean omitPort = port == -1
                    || ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);

            StringBuilder normalized = new StringBuilder();
            normalized.append(scheme).append("://").append(host);
            if (!omitPort) {
                normalized.append(":").append(port);
            }
            if (hasText(path) && !"/".equals(path)) {
                normalized.append(path);
            }
            return normalized.toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * 获取相对路径
     * @param path 路径
     * @return 相对路径
     */
    public static String getPath(String path) {
        return path.substring("/webdav".length());
    }

    /**
     * 获取纯文件名
     * @param path 相对路径
     * @param dir 是否为文件夹
     * @return 文件名
     */
    public static String getDisplayName(String path, boolean dir) {
        if (dir) {
            path = path.substring(0, path.lastIndexOf('/'));
            path = path.substring(path.lastIndexOf('/') + 1);
            return path;
        } else {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    /**
     * 获取路径中的文件夹名字
     * @param path 路径
     * @return 文件夹名字数组
     */
    public static List<String> getDirsPathFromPath(String path) {
        String[] paths = path.split("/");
        // 去掉文件名
        if (paths.length > 0 && paths[paths.length - 1].contains(".")) {
            paths = Arrays.copyOf(paths, paths.length - 1);
        }

        List<String> dirPaths = new ArrayList<>(); // 用于存储每个文件夹的路径

        StringBuilder currentPath = new StringBuilder(); // 拼接路径

        for (String p : paths) {
            if (p.isEmpty()) {
                continue;
            }
            if (p.contains(".")) {
                break;
            }
            currentPath.append("/" + p);
            dirPaths.add(currentPath + "/");
        }
        return dirPaths;
    }

    /**
     * 从消息中提取文件ID
     * @param message Telegram消息
     * @return 文件ID
     */
    public static String extractFileId(Message message) {
        if (message == null) {
            return null;
        }

        // 按优先级检查可能的文件类型
        if (message.document() != null) {
            return message.document().fileId();
        } else if (message.sticker() != null) {
            return message.sticker().fileId();
        } else if (message.video() != null) {
            return message.video().fileId();
        } else if (message.photo() != null && message.photo().length > 0) {
            return message.photo()[message.photo().length - 1].fileId(); // 取最后一张（通常是最高分辨率）
        } else if (message.audio() != null) {
            return message.audio().fileId();
        } else if (message.animation() != null) {
            return message.animation().fileId();
        } else if (message.voice() != null) {
            return message.voice().fileId();
        } else if (message.videoNote() != null) {
            return message.videoNote().fileId();
        }

        return null; // 没有找到 fileId
    }

}
