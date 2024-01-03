package net.nature.blog.services.impl;

import net.nature.blog.utils.EmailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    @Async
    public void sendVerifyCode(String verifyCode, String emailAddress) throws Exception {
        EmailSender.sendVerifyCode(verifyCode, emailAddress);
    }
}
