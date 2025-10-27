package co.edu.uco.client_app.controller;

import co.edu.uco.client_app.models.Message;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

<<<<<<< HEAD

@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:5174"},
	    allowedHeaders = "*",
	    allowCredentials = "true"
	)

=======
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:5174"},
    allowedHeaders = "*",
    allowCredentials = "true"
)
>>>>>>> 2b4728000c0f3e29403d60f7167e804b095367b1
@RestController
public class AppController {

    @GetMapping("/list")
    public List<Message> list(){
        return Collections.singletonList(new Message("Lista de varios elementos"));
    }

    @PostMapping("/create")
    public Message create(@RequestBody Message message){
        System.out.println("mensaje guardado "+ message);
        return message;
    }

    @GetMapping("/authorized")
    public Map<String, String> authorized(@RequestParam String code){
        return Collections.singletonMap("code", code);
    }
}
