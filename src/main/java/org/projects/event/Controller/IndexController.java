package org.projects.event.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class IndexController {

    @RequestMapping("/")
    public void index(HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect("/swagger-ui/index.html");
    }

}
