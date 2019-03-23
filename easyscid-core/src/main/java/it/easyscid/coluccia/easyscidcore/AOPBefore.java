package it.easyscid.coluccia.easyscidcore;

import java.io.FileWriter;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import redis.clients.jedis.Jedis;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Aspect
@Configuration
public class AOPBefore {

	public final static String DYNAMIC_CLASS_NAME = "HelloFuckingWorld";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	//private Class<?> dynamicClass;

	@Before("execution(* it.easyscid.coluccia.easyscidcore.MainController.sayHelloRedis(..))")
	public void before(JoinPoint joinPoint)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		if (MainController.getCodeFactory() != null) {
			logger.info("Code Factory già inizializato");
			return;
		}
		
		CodeInterface implementation = getImplementation();
		MainController.setCodeFactory(implementation);
		logger.info("Code Factory adesso inizializzato");

		/*dynamicClass = Class.forName("it.easyscid.coluccia.easyscidcore." + DYNAMIC_CLASS_NAME);

		Object iClass = dynamicClass.newInstance();

		Class<? extends MainController> subController = new ByteBuddy().subclass(MainController.class)
				.method(named("sayHelloRedis")).intercept(MethodDelegation.to(iClass)).make()
				.load(getClass().getClassLoader()).getLoaded();*/
		/*
		 * try { if (MainController.code != null) {
		 * logger.info("MainController già inizializzato!"); return; } // get
		 * the class pool ClassPool pool = ClassPool.getDefault(); // access the
		 * class without loading it just yet CtClass clazz =
		 * pool.get("it.easyscid.coluccia.easyscidcore.CodeClass"); //
		 * clazz.defrost(); // CtMethod[] methods = clazz.getMethods(); CtMethod
		 * method = clazz.getMethod("sayHelloRedis",
		 * "(Ljava/lang/String;)Ljava/lang/String;");
		 * method.setBody(value.replace("\\", "")); clazz.writeFile(); Class
		 * codeclass = clazz.toClass(); // clazz.detach(); // CodeClass
		 * codeupdated = (CodeClass)codeclass.newInstance(); CodeClass
		 * codeupdated = new CodeClass(); MainController.code = codeupdated;
		 * 
		 * 
		 * ClassPool cp = ClassPool.getDefault(); CtClass controller =
		 * cp.get("it.easyscid.coluccia.easyscidcore.CodeClass"); CtClass redis
		 * = cp.get("Singleton"); CtClass actual = cp.get("Client");
		 * CodeConverter conv = new CodeConverter(); conv.replaceNew(actual,
		 * redis, "sayHelloRedis"); controller.instrument(conv);
		 * 
		 * } catch (Exception e) { logger.error(e.getMessage(), e); }
		 */

	}

	public void createIt() {
		try {

			Jedis jedis = new Jedis("localhost");
			String value = jedis.get("helloworldmethod");
			logger.info("VALUE: " + value);
			jedis.close();

			FileWriter aWriter = new FileWriter(
					"./src/main/java/it/easyscid/coluccia/easyscidcore/" + DYNAMIC_CLASS_NAME + ".java", false);
			aWriter.write("package it.easyscid.coluccia.easyscidcore;");
			aWriter.write("public class " + DYNAMIC_CLASS_NAME + " implements CodeInterface{");
			aWriter.write(" public String sayHelloRedis() {");
			// aWriter.write(" return \"Hello Fucking World!\";");
			aWriter.write(value.replace("\\", ""));
			aWriter.write(" }}\n");
			aWriter.flush();
			aWriter.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean compileIt() {
		String[] source = { "-d", "./target/classes/", "-sourcepath", "./src/main/java/",
				new String("./src/main/java/it/easyscid/coluccia/easyscidcore/" + DYNAMIC_CLASS_NAME + ".java") };
		return (com.sun.tools.javac.Main.compile(source) == 0);
	}

	public Object runIt() {
		try {
			Class params[] = {};
			Object paramsObj[] = {};
			Class thisClass = Class.forName("it.easyscid.coluccia.easyscidcore." + DYNAMIC_CLASS_NAME);
			Object iClass = thisClass.newInstance();
			Method thisMethod = thisClass.getDeclaredMethod("sayHello", params);
			return thisMethod.invoke(iClass, paramsObj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private CodeInterface getImplementation() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		createIt();
		if (compileIt()) {
			logger.info("Compiled correctly");
			return (CodeInterface)Class.forName("it.easyscid.coluccia.easyscidcore." + DYNAMIC_CLASS_NAME).newInstance();
		}
		else{
			logger.error("Compilation failed");
			return null;
		}
	}

	public static void main(String args[]) {
		AOPBefore mtc = new AOPBefore();
		//System.out.println(mtc.runIt());
		mtc.createIt();
		if (mtc.compileIt()) {
			System.out.println("Running " + DYNAMIC_CLASS_NAME + ":\n\n");
			System.out.println(mtc.runIt());
		} else
			System.out.println(DYNAMIC_CLASS_NAME + ".java" + " is bad.");
	}

}
