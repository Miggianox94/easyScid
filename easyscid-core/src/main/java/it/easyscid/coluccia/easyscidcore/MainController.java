package it.easyscid.coluccia.easyscidcore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	private static final String template = "Hello, %s!";
	private static CodeInterface codeFactory = null;

	
	@GetMapping("/hello-world-normal")
    public String sayHello(@RequestParam(name="name", required=false, defaultValue="Stranger") String name) {
		return String.format(template, name);
    }
	
	@GetMapping("/hello-world-redis")
	//@EasyScidMethod(classFolder="./target/classes/", sourceFolder="./src/main/java/", interfaceName = "it.easyscid.coluccia.easyscidcore.CodeInterface", setterMethod = "setCodeFactory")
	@EasyScidMethod(classFolder="./", sourceFolder="./", interfaceName = "it.easyscid.coluccia.easyscidcore.CodeInterface", setterMethod = "setCodeFactory")
	public String sayHelloRedis() {
		return codeFactory.sayHelloRedis();
    }

	public static CodeInterface getCodeFactory() {
		return codeFactory;
	}

	public static void setCodeFactory(CodeInterface codeFactory) {
		MainController.codeFactory = codeFactory;
	}
	
	

}
