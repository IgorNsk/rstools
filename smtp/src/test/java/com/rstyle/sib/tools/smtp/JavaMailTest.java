package com.rstyle.sib.tools.smtp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JavaMailTest {

    private JavaMail m;

    @BeforeEach
    void setUp() {
        m = new JavaMail("qqqqq");

        m.setFrom("bank@bank.ru");
        m.setTo("user@domain.com");
        m.setCc("users@gmail.com");
        m.setFrom("from@mail.ru");
        m.setMailhost("smtp.mail.ru");
        m.setUser("from@mail.ru");
        m.setPassword("pswd");
        m.setSubject("Тест отправки сообщения через JavaMail");
        m.setText("Однажды, в студённую зимнюю пору\nя из лесу вышел");
        //m.setFile("c:\\temp\\file.txt");
        m.setPort(587);
        m.setSsl(true);

        m.setDebug(true);
        m.setAuth(true);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sendEMailTest() throws IOException {


        if (m.sendSimpleMail() == 0) {
            System.out.println("Сообщение отправлено успешно");
        } else {
            System.out.println("Сообщение не отправлено");
        }
    }

    @Test
    void sendEMailCorruptedCCAddressTest(){

        m.setCc("@users@gmail.com");

        if (m.sendSimpleMail() == 0) {
            System.out.println("Сообщение отправлено успешно");
        } else {
            System.out.println("Сообщение не отправлено");
        }

    }
}