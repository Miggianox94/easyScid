package it.easyscid.coluccia.easyscidcore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)  
public @interface EasyScidMethod {

	String classFolder() default "./";
	String sourceFolder() default "./";
	String interfaceName();
	String setterMethod() default "setCodeFactory";
	
}
