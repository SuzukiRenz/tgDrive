package com.skydevs.tgdrive.utils;

import com.pengrad.telegrambot.model.Message;
import jakarta.servlet.http.HttpServletRequest;

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
        String protocol = headerOrDefault(request, "X-Forwarded-Proto", request.getScheme()); // 先从代理请求头中获取协议
        String host = extractHost(request); // 优先从 Host / X-Forwarded-Host 中取值，避免反代场景下拿到容器内主机名
        int port = extractPort(request, protocol); // 优先从代理请求头中获取端口号
        // 如果是默认端口，则省略端口号
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
