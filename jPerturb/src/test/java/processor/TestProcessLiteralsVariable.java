package processor;

import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;
import util.Util;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by spirals on 07/03/16.
 */
public class TestProcessLiteralsVariable {

    @Test
    public void testIntroductionOfPerturbation() {
        Launcher launcher = Util.createSpoonWithPerturbationProcessors();

        launcher.addInputResource("src/test/resources/SimpleRes.java");

        launcher.run();

        CtClass simpleResWithPerturbation = (CtClass) launcher.getFactory().Package().getRootPackage().getElements(new NamedElementFilter<>(CtClass.class, "SimpleRes")).get(0);

        CtClass perturbator = (CtClass) launcher.getFactory().Package().getRootPackage().getElements(new NamedElementFilter<>(CtClass.class, "PerturbationEngine")).get(0);

        Set<CtMethod> methods = simpleResWithPerturbation.getAllMethods();

        for (CtMethod m : methods) {
            List<CtLiteral> elems = m.getElements(new TypeFilter(CtLiteral.class));
            for (CtLiteral elem : elems) {
                if (elem.getParent() instanceof CtConstructorCall && ((CtConstructorCall) elem.getParent()).getExecutable().getType().getSimpleName().equals("PerturbationLocationImpl"))
                    continue;// we skip lit introduce by the perturbation
                if (elem.getParent() instanceof CtAnnotation) continue;
                //parent is invokation
                assertTrue(elem.getParent() instanceof CtInvocation);
                //this invokation come from perturbator
                Assert.assertEquals(perturbator.getReference(), ((CtInvocationImpl) elem.getParent()).getExecutable().getDeclaringType());
            }
            List<CtReturn> returns = m.getElements(new TypeFilter<>(CtReturn.class));
            for (CtReturn ret : returns) {
                assertTrue(ret.getReturnedExpression() instanceof CtInvocation);
                assertTrue(((CtInvocationImpl) ret.getReturnedExpression()).getExecutable().getDeclaringType().equals(perturbator.getReference()));
            }
        }
    }
}
