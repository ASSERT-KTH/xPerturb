package perturbation;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.NamedElementFilter;
import util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by spirals on 22/03/16.
 */
public class TestPerturbationLiteralsVariable {

    @Test
    public void testPerturbation() throws Exception {

        Launcher launcher = Util.createSpoonWithPerturbationProcessors();

        launcher.addInputResource("src/test/resources/SimpleRes.java");

        launcher.run();

        CtClass simpleResWithPerturbation = launcher.getFactory().Package().getRootPackage().getElements(new NamedElementFilter<>(CtClass.class, "SimpleRes")).get(0);

        //The pertubation works?
        URLClassLoader classLoaderWithoutOldFile = new URLClassLoader(new URL[]{launcher.getModelBuilder().getBinaryOutputDirectory().toURL()}, Util.class.getClassLoader() );

        Class<?> classPerturbationLocation = classLoaderWithoutOldFile.loadClass("perturbation.location.PerturbationLocation");
        Method setEnactor = classPerturbationLocation.getMethod("setEnactor", classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor"));

        Class<?> classAlwaysEnactorImpl = classLoaderWithoutOldFile.loadClass("perturbation.enactor.AlwaysEnactorImpl");
        Class<?> classNeverEnactorImpl = classLoaderWithoutOldFile.loadClass("perturbation.enactor.NeverEnactorImpl");

        Class<?> classUnderTest = classLoaderWithoutOldFile.loadClass(simpleResWithPerturbation.getQualifiedName());
        Object objectUnderTest = classUnderTest.newInstance();

        Map<Method, Object> returnWithoutPerturbation = new HashMap<>();
        Method[] methods = classUnderTest.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("_p"))
                returnWithoutPerturbation.put(methods[i], methods[i].invoke(objectUnderTest));
        }

        boolean perturbation;

        Field[] fields = classUnderTest.getFields();//Getting all field Location in the class under Test

        for (int f = 0; f < fields.length; f++) {
            if (fields[f].getName().startsWith("__L")) {
                Object instanceField = fields[f].get(objectUnderTest);
                setEnactor.invoke(instanceField, classAlwaysEnactorImpl.newInstance());//Activated the right location
                perturbation = false;
                for (int m = 0; m < methods.length; m++) {
                    if (methods[m].getName().startsWith("_p")) {
                        if (!methods[m].invoke(objectUnderTest).equals(returnWithoutPerturbation.get(methods[m]))) {
                            if (perturbation)
                                assertTrue(false);//One and only one perturbation is activated
                            else
                                perturbation = true;
                        } else
                            assertEquals(returnWithoutPerturbation.get(methods[m]), methods[m].invoke(objectUnderTest));//Others are the same
                    }
                }
                assertTrue(perturbation);//One perturbation is activated
                setEnactor.invoke(instanceField, classNeverEnactorImpl.newInstance());//clean location of perturbation
            }
        }
    }

}
