import org.junit.jupiter.api.Test;
import org.projects.Controller.IndexController;

public class IndexControllerTest {

    @Test
    public void testIndex(){
        IndexController indexController = new IndexController();
        assert "Hello! This is an event processor.".equals(indexController.index());
    }

}
