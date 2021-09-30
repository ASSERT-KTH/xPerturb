package perturbation.perturbator;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.NamedElementFilter;
import util.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by spirals on 25/03/16.
 */
public class TestPerturbator {

    private static Launcher launcher = null;

    private static URLClassLoader classLoaderWithoutOldFile;
    private static CtClass simpleResWithPerturbation;

    private static Method setPerturbator;

    private static Class<?> classPerturbationLocation;
    private static Object objectPerturbationLocation0;
    private static Object objectPerturbationLocation10;

    private static Class<?> classUnderTest;
    private static Object objectUnderTest;
    private static Method booleanMethodOfClassUnderTest;
    private static Method intMethodOfClassUnderTest;


    private static void initialisation() throws Exception {
        launcher = Util.createSpoonWithPerturbationProcessors();

        launcher.addInputResource("src/test/resources/SimpleRes.java");

        launcher.run();

        simpleResWithPerturbation = launcher.getFactory().Package().getRootPackage().getElements(new NamedElementFilter<>(CtClass.class, "SimpleRes")).get(0);

        classLoaderWithoutOldFile = new URLClassLoader(new URL[]{launcher.getModelBuilder().getBinaryOutputDirectory().toURL()}, Util.class.getClassLoader() );

        classPerturbationLocation = classLoaderWithoutOldFile.loadClass("perturbation.location.PerturbationLocation");
        setPerturbator = classPerturbationLocation.getMethod("setPerturbator", classLoaderWithoutOldFile.loadClass("perturbation.perturbator.Perturbator"));

        classUnderTest = classLoaderWithoutOldFile.loadClass(simpleResWithPerturbation.getQualifiedName());
        objectUnderTest = classUnderTest.newInstance();
        booleanMethodOfClassUnderTest = classUnderTest.getMethod("_pBoolean");
        intMethodOfClassUnderTest = classUnderTest.getMethod("_pInt");

        Field field = classUnderTest.getFields()[12];

        objectPerturbationLocation0 = field.get(null);
        objectPerturbationLocation10 = classUnderTest.getFields()[10].get(null);

        field.get(null).getClass().getMethod("setEnactor", classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor")).invoke(
                field.get(null), classLoaderWithoutOldFile.loadClass("perturbation.enactor.AlwaysEnactorImpl").newInstance()
        );

        classUnderTest.getFields()[10].get(null).getClass().getMethod("setEnactor", classLoaderWithoutOldFile.loadClass("perturbation.enactor.Enactor")).invoke(
                classUnderTest.getFields()[10].get(null), classLoaderWithoutOldFile.loadClass("perturbation.enactor.AlwaysEnactorImpl").newInstance()
        );

    }

    /**
     * This method allows test to set a Perturbator which it extends PerturbatorDecorator with a specific decorated
     * Perturbator
     *
     * @param perturbator
     * @param decoratedPerturbator
     * @throws Exception
     */
    private static void setPerturbatorWithDecoratedPerturbator(String perturbator, String decoratedPerturbator) throws Exception {
        Constructor constructorOfDecoratorPerturbator = classLoaderWithoutOldFile.loadClass(perturbator).getConstructor(
                classLoaderWithoutOldFile.loadClass("perturbation.perturbator.Perturbator")
        );

        Object perturbatorObject = constructorOfDecoratorPerturbator.newInstance(
                classLoaderWithoutOldFile.loadClass(decoratedPerturbator).newInstance()
        );

        setPerturbator.invoke(objectPerturbationLocation0, perturbatorObject);
        setPerturbator.invoke(objectPerturbationLocation10, perturbatorObject);
    }

    @Test
    public void testInvPerturbator() throws Exception {

        //test the default behavior of Perturbator : InvPerturbator

        if (launcher == null)
            initialisation();

        setPerturbator.invoke(objectPerturbationLocation0, classLoaderWithoutOldFile.loadClass("perturbation.perturbator.InvPerturbatorImpl").newInstance());
        setPerturbator.invoke(objectPerturbationLocation10, classLoaderWithoutOldFile.loadClass("perturbation.perturbator.InvPerturbatorImpl").newInstance());

        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(-1, intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testAddOnePerturbator() throws Exception {

        //test the perturbator +1 with all other booleans perturbator

        if (launcher == null)
            initialisation();

        setPerturbatorWithDecoratedPerturbator("perturbation.perturbator.AddOnePerturbatorImpl", "perturbation.perturbator.InvPerturbatorImpl");

        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(2, intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testMinusOnePerturbator() throws Exception {

        //test the perturbator -1 with all other booleans perturbator

        if (launcher == null)
            initialisation();

        setPerturbatorWithDecoratedPerturbator("perturbation.perturbator.MinusOnePerturbatorImpl", "perturbation.perturbator.InvPerturbatorImpl");

        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(0, intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testZeroPerturbator() throws Exception {

        //test the perturbator 0

        if (launcher == null)
            initialisation();

        setPerturbatorWithDecoratedPerturbator("perturbation.perturbator.ZeroPerturbatorImpl", "perturbation.perturbator.InvPerturbatorImpl");

        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertEquals(0, intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testRndPerturbator() throws Exception {

        //test the rndPerturbator

        if (launcher == null)
            initialisation();

        setPerturbator.invoke(objectPerturbationLocation10, classLoaderWithoutOldFile.loadClass("perturbation.perturbator.RndPerturbatorImpl").newInstance());


//        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertNotEquals(1, intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testRndPosPerturbator() throws Exception {

        //test the rndPosPerturbator

        if (launcher == null)
            initialisation();

        setPerturbator.invoke(objectPerturbationLocation10, classLoaderWithoutOldFile.loadClass("perturbation.perturbator.RndPosPerturbatorImpl").newInstance());

//        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertTrue(0 < (Integer) intMethodOfClassUnderTest.invoke(objectUnderTest));

    }

    @Test
    public void testRndNegPerturbator() throws Exception {

        //test the rndNegPerturbator

        if (launcher == null)
            initialisation();

        setPerturbator.invoke(objectPerturbationLocation10, classLoaderWithoutOldFile.loadClass("perturbation.perturbator.RndNegPerturbatorImpl").newInstance());

//        assertEquals(false, booleanMethodOfClassUnderTest.invoke(objectUnderTest));
        assertTrue(0 > (Integer) intMethodOfClassUnderTest.invoke(objectUnderTest));

    }


}
