package com.dotastory.roshan.controller;

import com.dotastory.roshan.utils.SSEUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/dotastory")
public class StoryController {

    @GetMapping("/connect")
    public SseEmitter connect(HttpServletRequest request) {
        String id =request.getSession().getId();
        return SSEUtils.connect(id);
    }

    @PostMapping("/send/story")
    public void sendMessage(HttpServletRequest request,String story,String userId) {
        if(StringUtils.isNotBlank(userId)) {
            SSEUtils.sendMessageByGiveNoticeAll(userId, story);
        }else {
          if(request.getSession()!=null&&StringUtils.isNotBlank(request.getSession().getId())){
              SSEUtils.sendMessageByGiveNoticeAll(request.getSession().getId(), story);
          }
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // ??????????????????????????????IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // ?????????????????????????????????????????????IP??????????????????IP,??????IP??????','??????
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "" ;
        }
        return ipAddress;

    }
}
