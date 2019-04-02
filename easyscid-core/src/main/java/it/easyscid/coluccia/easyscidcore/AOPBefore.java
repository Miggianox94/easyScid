package it.easyscid.coluccia.easyscidcore;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.sun.tools.javac.Main;

import it.easyscid.coluccia.easyscidcore.classloading.DynamicClassLoader;
import redis.clients.jedis.Jedis;

@Aspect
@Configuration
public class AOPBefore {

	public final static String DYNAMIC_CLASS_NAME = "HelloFuckingWorld";
	private final String JAR_NAME = "easyscid-core-0.0.1-SNAPSHOT.jar.original";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//@Before("execution(* it.easyscid.coluccia.easyscidcore.MainController.sayHelloRedis(..))")
	@Before("execution(@EasyScidMethod * *(..))")
	public void before(JoinPoint joinPoint)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		logger.info("before is executing: "+joinPoint);
		System.out.println("before is executing: "+joinPoint);
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method thisMethod = signature.getMethod();
		EasyScidMethod annotation = thisMethod.getAnnotation(EasyScidMethod.class);
		
		/*if (MainController.getCodeFactory() != null) {
			logger.info("Code Factory già inizializato");
			return;
		}*/
		
		Object implementation = getImplementation(annotation.classFolder(),annotation.sourceFolder(),annotation.interfaceName());
		
		try {
			Object caller = joinPoint.getThis();
			Class<?> params[] = { Class.forName(annotation.interfaceName()) };
			Object paramsObj[] = { implementation };
			Method setCodeFactoryMethod = caller.getClass().getMethod(annotation.setterMethod(), params);
			setCodeFactoryMethod.invoke(caller, paramsObj);
			logger.info("Code Factory adesso inizializzato");
		} catch (Exception e) {
			logger.error("Code factory non inizializzato!", e);
		}

	}

	public void createIt(String packageName, String interfaceName, String sourceFolder) throws IOException {

			Jedis jedis = new Jedis("localhost");
			String value = jedis.get("helloworldmethod");
			logger.info("VALUE: " + value);
			jedis.close();

			String packagePath = packageName.replace(".", "/");
			FileWriter aWriter = new FileWriter(
					sourceFolder/*+packagePath + File.separatorChar*/ + DYNAMIC_CLASS_NAME + ".java", false);
			aWriter.write("package "+packageName+";");
			aWriter.write("public class " + DYNAMIC_CLASS_NAME + " implements "+interfaceName+"{");
			aWriter.write(" public String sayHelloRedis() {");
			aWriter.write(value.replace("\\", ""));
			aWriter.write(" }}\n");
			aWriter.flush();
			aWriter.close();

	}

	public boolean compileIt(String classFolder, String sourceFolder, String packagePath) {
		String[] source = { "-cp",".;"+JAR_NAME,"-d", classFolder, "-sourcepath", sourceFolder,
				new String(sourceFolder/*+packagePath + File.separatorChar*/ + DYNAMIC_CLASS_NAME + ".java") };
		return (Main.compile(source) == 0);
	}

	public Object runIt() {
		try {
			Class<?> params[] = {};
			Object paramsObj[] = {};
			Class<?> thisClass = Class.forName("it.easyscid.coluccia.easyscidcore." + DYNAMIC_CLASS_NAME);
			Object iClass = thisClass.newInstance();
			Method thisMethod = thisClass.getDeclaredMethod("sayHello", params);
			return thisMethod.invoke(iClass, paramsObj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private Object getImplementation(String classFolder, String sourceFolder, String interfaceName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		String packageName = interfaceName.substring(0, interfaceName.lastIndexOf("."));
		String interfaceClassName = interfaceName.substring(interfaceName.lastIndexOf(".")+1);
		createIt(packageName,interfaceClassName,sourceFolder);
		if (compileIt(classFolder,sourceFolder,packageName.replace(".", "/"))) {
			logger.info("Compiled correctly");
			try {	
				return /*(CodeInterface)*/reloadImplementation(classFolder,packageName+"."+DYNAMIC_CLASS_NAME,interfaceName);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				return null;
			}
		}
		else{
			logger.error("Compilation failed");
			return null;
		}
	}
	
	private Object reloadImplementation(String classFolder,String fullPackageClass, String packageInterfaceName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, IllegalArgumentException, SecurityException{
		DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(classFolder);
		dynamicClassLoader.getLoadedClasses().add(packageInterfaceName);
		dynamicClassLoader.getLoadedClassesReferences().put(packageInterfaceName, Class.forName(packageInterfaceName));
		Class<?> reloadedClass = dynamicClassLoader.load(fullPackageClass);
	    if(reloadedClass.isInterface()){
	    	return null;
	    }
	    return reloadedClass.newInstance();
	}

	/*public static void main(String args[]) {
		AOPBefore mtc = new AOPBefore();
		//System.out.println(mtc.runIt());
		mtc.createIt();
		if (mtc.compileIt()) {
			System.out.println("Running " + DYNAMIC_CLASS_NAME + ":\n\n");
			System.out.println(mtc.runIt());
		} else
			System.out.println(DYNAMIC_CLASS_NAME + ".java" + " is bad.");
	}*/

}
