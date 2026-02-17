package org;

import org.dao.MessageDao;
import org.entities.Message;

import java.time.LocalDateTime;

public class DaoTest {
    public static void main(String[] args) {
        MessageDao dao = new MessageDao();

        Message m = new Message(LocalDateTime.now(), "hello", 1, 1);


        dao.create(m);

        System.out.println("Saved. Total: " + dao.findAll().size());
    }
}
