package org.treblereel.gwt.crysknife.generator.api;

import javax.inject.Provider;
import javax.lang.model.element.TypeElement;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.treblereel.gwt.crysknife.util.Utils;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 3/3/19
 */
public class InstanceConstructorBuilder extends Builder {

    public InstanceConstructorBuilder(ClassBuilder classBuilder) {
        super(classBuilder);
    }

    @Override
    public void build() {
        MethodDeclaration methodDeclaration = classBuilder.getClassDeclaration().addMethod("create", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        methodDeclaration.setType(classBuilder.beanDefinition.getClassFactoryName());
        ObjectCreationExpr newInstance = new ObjectCreationExpr();
        newInstance.setType(new ClassOrInterfaceType().setName(classBuilder.beanDefinition.getClassFactoryName()));
        methodDeclaration.getBody().get().getStatements().add(new ReturnStmt(newInstance));

        if (!classBuilder.beanDefinition.getDependsOn().isEmpty()) {
            for (TypeElement argument : classBuilder.beanDefinition.getDependsOn()) {
                String varName = Utils.toVariableName(argument.getQualifiedName().toString());

                ClassOrInterfaceType type = new ClassOrInterfaceType();
                type.setName(Provider.class.getSimpleName());
                type.setTypeArguments(new ClassOrInterfaceType().setName(argument.getQualifiedName().toString()));

                Parameter param = new Parameter();
                param.setName(varName);
                param.setType(type);

                methodDeclaration.addAndGetParameter(param);
                newInstance.addArgument(varName);
            }
        }
    }
}
