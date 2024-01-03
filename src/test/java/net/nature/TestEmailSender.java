package net.nature;

import net.nature.blog.utils.EmailSender;

import javax.mail.MessagingException;

public class TestEmailSender {
    public static void main(String[] args) throws MessagingException {
        EmailSender.subject("测试邮件发送")
                .from("天然博客系统")
                .text("验证码：wesd23")
                .to("3493855051@qq.com")
                .send();
    }
}
