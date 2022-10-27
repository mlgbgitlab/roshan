package com.dotastory.roshan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class SSEUtils {
    private static Logger logger = LoggerFactory.getLogger(SSEUtils.class);
    private static AtomicInteger ssecount = new AtomicInteger(0);
    private static Map<String, SseEmitter> sseEmitterMap = Maps.newConcurrentMap();

    public static SseEmitter connect(String userId) {
        //设置超时时间，0表示不过期，默认是30秒，超过时间未完成会抛出异常
        if(sseEmitterMap.containsKey(userId)){
            return sseEmitterMap.get(userId);
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        try {
            sseEmitter.onCompletion(completionCallBack(userId));
            sseEmitter.onTimeout(timeOutCallBack(userId));
            sseEmitter.send(userId);
            sseEmitterMap.put(userId, sseEmitter);
            ssecount.getAndIncrement();
        } catch (Exception e) {
            logger.info("USERID：{}——SSE ERROR:{}", userId, e);
            removeUser(userId);
        }

        logger.info("创建链接:{}", userId);
        return sseEmitter;
    }

    public static List<String> getIds() {
        return Lists.newArrayList(sseEmitterMap.keySet());
    }

    public static int getUserCount() {
        return ssecount.intValue();
    }

    private static Runnable completionCallBack(String userId) {
        return () -> {
            logger.info("结束连接,{}", userId);
            removeUser(userId);
        };
    }

    private static Runnable timeOutCallBack(String userId) {
        return () -> {
            logger.info("连接超时,{}", userId);
            removeUser(userId);
        };
    }

    public static void removeUser(String userId) {
        sseEmitterMap.remove(userId);
        //数量-1
        ssecount.getAndDecrement();
        logger.info("remove user id:{}", userId);
    }

    public static boolean sendMessage(String userId, String message) {
        if (sseEmitterMap.containsKey(userId)) {
            try {
                sseEmitterMap.get(userId).send(message);
            } catch (IOException e) {
                logger.error("发给用户:{}, 消息发送失败:{}", userId, e.getMessage());
                return false;
            }
        } else {
            logger.error("用户{}关闭链接，进度同步终止", userId);
            return false;
        }
        return true;
    }

    public static boolean sendMessageByGiveNoticeAll(String userId, String message) {
        if (sseEmitterMap.containsKey(userId)) {
               for(String key: sseEmitterMap.keySet()){
                   try {
                       sseEmitterMap.get(key).send(message);
                   } catch (IOException e) {
                       logger.error("用户:{}, 通知消息发送失败:{}", userId, e.getMessage());
                   }
               }
        } else {
            logger.error("用户{}关闭链接，进度同步终止", userId);
            return false;
        }
        return true;
    }
}
