package kr.co.cjdashboard.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtil {
    private static MessageSource messageSource;

    @Autowired
    public MessageUtil(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    public static String exceptionMessage(String code) {
        return messageSource.getMessage(code, null, Locale.getDefault());
    }
}