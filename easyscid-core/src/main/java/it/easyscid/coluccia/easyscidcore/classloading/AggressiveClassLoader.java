package it.easyscid.coluccia.easyscidcore.classloading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Load all classes it can, leave the rest to the Parent ClassLoader
 */
public abstract class AggressiveClassLoader extends ClassLoader {

	Set<String> loadedClasses = new HashSet<>();
	Set<String> unavaiClasses = new HashSet<>();
	Map<String,Class<?>> loadedClassesReferences = new HashMap<>();
    private ClassLoader parent = AggressiveClassLoader.class.getClassLoader();

    @Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
    	System.out.println("Trying to load class: "+name);
		if (loadedClasses.contains(name) || unavaiClasses.contains(name)) {
			if(loadedClasses.contains(name)){
				System.out.println("Class already loaded: "+name);
				return loadedClassesReferences.get(name);
			}
			return super.loadClass(name); // Use default CL cache
		}

		byte[] newClassData = loadNewClass(name);
		if (newClassData != null) {
			loadedClasses.add(name);
			return loadClass(newClassData, name);
		} else {
			unavaiClasses.add(name);
			return parent.loadClass(name);
		}
	}

//    public AggressiveClassLoader setParent(ClassLoader parent) {
//        this.parent = parent;
//        return this;
//    }
	
	/**
	 * Handle exception
	 * @param name
	 * @return
	 */
	public Class<?> load(String name) {
		try {
			return loadClass(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract byte[] loadNewClass(String name);

	public Class<?> loadClass(byte[] classData, String name) {
		Class<?> clazz = defineClass(name, classData, 0, classData.length);
		if (clazz != null) {
			if (clazz.getPackage() == null) {
				definePackage(name.replaceAll("\\.\\w+$", ""), null, null, null, null, null, null, null);
			}
			resolveClass(clazz);
		}
		return clazz;
	}

	public static String toFilePath(String name) {
		return name.replaceAll("\\.", "/") + ".class";
	}

	public Set<String> getLoadedClasses() {
		return loadedClasses;
	}

	public void setLoadedClasses(Set<String> loadedClasses) {
		this.loadedClasses = loadedClasses;
	}

	public Map<String, Class<?>> getLoadedClassesReferences() {
		return loadedClassesReferences;
	}

	public void setLoadedClassesReferences(Map<String, Class<?>> loadedClassesReferences) {
		this.loadedClassesReferences = loadedClassesReferences;
	}
	
	
}
