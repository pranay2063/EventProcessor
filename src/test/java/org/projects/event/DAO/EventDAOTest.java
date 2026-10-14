package org.projects.event.DAO;

import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class EventDAOTest {
    @Test void storesAndQueriesEventsBySender() {
        EventDAO dao = new EventDAO();
        dao.init();
        Event event = new Event(); event.setSenderEmail("a@example.com");
        dao.addEvent(event);
        assertEquals(1, dao.queryEvent("a@example.com").size());
        assertTrue(dao.queryEvent("missing@example.com").isEmpty());
    }

    @Test void supportsConcurrentWrites() throws Exception {
        EventDAO dao = new EventDAO(); dao.init();
        var pool = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 1000; i++) pool.submit(() -> { Event e = new Event(); e.setSenderEmail("a@example.com"); dao.addEvent(e); });
        } finally { pool.shutdown(); assertTrue(pool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)); }
        assertEquals(1000, dao.queryEvent("a@example.com").size());
    }
}
