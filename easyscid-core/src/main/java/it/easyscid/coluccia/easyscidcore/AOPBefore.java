package it.easyscid.coluccia.easyscidcore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.Jedis;

@Aspect
@Configuration
public class AOPBefore {

	public final static String DYNAMIC_CLASS_NAME = "HelloFuckingWorld";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	//private Class<?> dynamicClass;

	//@Before("execution(* it.easyscid.coluccia.easyscidcore.MainController.sayHelloRedis(..))")
	@Before("execution(@EasyScidMethod * *(..))")
	public void before(JoinPoint joinPoint)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method thisMethod = signature.getMethod();
		EasyScidMethod annotation = thisMethod.getAnnotation(EasyScidMethod.class);
		
		/*if (MainController.getCodeFactory() != null) {
			logger.info("Code Factory già inizializato");
			return;
		}*/
		
		CodeInterface implementation = getImplementation(annotation.classFolder(),annotation.sourceFolder(),annotation.interfaceName());
		MainController.setCodeFactory(implementation);
		logger.info("Code Factory adesso inizializzato");


	}

	public void createIt(String packageName, String interfaceName) {
		try {

			Jedis jedis = new Jedis("localhost");
			String value = jedis.get("helloworldmethod");
			logger.info("VALUE: " + value);
			jedis.close();

			FileWriter aWriter = new FileWriter(
					"./src/main/java/it/easyscid/coluccia/easyscidcore/" + DYNAMIC_CLASS_NAME + ".java", false);
			aWriter.write("package "+packageName+";");
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

	public boolean compileIt(String classFolder, String sourceFolder, String packagePath) {
		String[] source = { "-d", classFolder, "-sourcepath", sourceFolder,
				new String(sourceFolder+packagePath + File.separatorChar + DYNAMIC_CLASS_NAME + ".java") };
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
	
	private CodeInterface getImplementation(String classFolder, String sourceFolder, String interfaceName) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		String packageName = interfaceName.substring(0, interfaceName.lastIndexOf("."));
		String interfaceClassName = interfaceName.substring(interfaceName.lastIndexOf(".")+1);
		createIt(packageName,interfaceClassName);
		if (compileIt(classFolder,sourceFolder,packageName.replace(".", "/"))) {
			logger.info("Compiled correctly");
			//return (CodeInterface)Class.forName(packageName+"." + DYNAMIC_CLASS_NAME).newInstance();
			try {	
				return (CodeInterface)reloadImplementation(classFolder,packageName+"."+DYNAMIC_CLASS_NAME);
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
	
	private Object reloadImplementation(String classFolder,String fullPackageClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, IllegalArgumentException, SecurityException{
		DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(classFolder);
		dynamicClassLoader.getLoadedClasses().add(CodeInterface.class.getName());
		Class<?> reloadedClass = dynamicClassLoader.load(fullPackageClass);
	    if(reloadedClass.isInterface()){
	    	return null;
	    }
	    Object toReturn = reloadedClass.newInstance();
	    try {
			Object returnedValue = reloadedClass.getMethods()[0].invoke(reloadedClass.newInstance());
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
	    return toReturn;
	}

	public static void main(String args[]) {
		AOPBefore mtc = new AOPBefore();
		//System.out.println(mtc.runIt());
		/*mtc.createIt();
		if (mtc.compileIt()) {
			System.out.println("Running " + DYNAMIC_CLASS_NAME + ":\n\n");
			System.out.println(mtc.runIt());
		} else
			System.out.println(DYNAMIC_CLASS_NAME + ".java" + " is bad.");*/
	}

}
