package codegen.providers;

import soot.jimple.NegExpr;
import soot.*;
import soot.jimple.Expr;
import soot.jimple.Jimple;
import soot.jimple.internal.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class OperatorProvider {

    public static Value placeholder1 = Jimple.v().newLocal("placeholder1", IntType.v());
    public static Value placeholder2 = Jimple.v().newLocal("placeholder2", IntType.v());
    public static Type placeholder3 = IntType.v();

    //算术运算符
    public static JAddExpr addExpr = (JAddExpr) Jimple.v().newAddExpr(placeholder1, placeholder2); // +
    public static JSubExpr subExpr = (JSubExpr) Jimple.v().newSubExpr(placeholder1, placeholder2); // -
    public static JMulExpr mulExpr = (JMulExpr) Jimple.v().newMulExpr(placeholder1, placeholder2); // *
    public static JDivExpr divExpr = (JDivExpr) Jimple.v().newDivExpr(placeholder1, placeholder2); // /

    public static JRemExpr remExpr = (JRemExpr) Jimple.v().newRemExpr(placeholder1, placeholder2); // %

    public static JCastExpr castExpr = (JCastExpr) Jimple.v().newCastExpr(placeholder1, placeholder3); //checkcast

    //逻辑运算符
    public static JOrExpr orExpr = (JOrExpr) Jimple.v().newOrExpr(placeholder1, placeholder2);
    public static JAndExpr andExpr = (JAndExpr) Jimple.v().newAndExpr(placeholder1, placeholder2);
    public static JXorExpr xorExpr = (JXorExpr) Jimple.v().newXorExpr(placeholder1, placeholder2);
    public static JNegExpr negExpr = (JNegExpr) Jimple.v().newNegExpr(placeholder1);
    public static JShlExpr shlExpr = (JShlExpr) Jimple.v().newShlExpr(placeholder1, placeholder2);
    public static JShrExpr shrExpr = (JShrExpr) Jimple.v().newShrExpr(placeholder1, placeholder2);
    public static JUshrExpr ushrExpr = (JUshrExpr) Jimple.v().newUshrExpr(placeholder1, placeholder2);

    //关系运算符
    public static JEqExpr eqExpr = (JEqExpr) Jimple.v().newEqExpr(placeholder1, placeholder2);
    public static JGtExpr gtExpr = (JGtExpr) Jimple.v().newGtExpr(placeholder1, placeholder2);
    public static JGeExpr geExpr = (JGeExpr) Jimple.v().newGeExpr(placeholder1, placeholder2);
    public static JLtExpr ltExpr = (JLtExpr) Jimple.v().newLtExpr(placeholder1, placeholder2);
    public static JLeExpr leExpr = (JLeExpr) Jimple.v().newLeExpr(placeholder1, placeholder2);
    public static JNeExpr neExpr = (JNeExpr) Jimple.v().newNeExpr(placeholder1, placeholder2);


    /**
     * 给定操作符以及操作数，创建对应的赋值操作
     * @param operator
     * @param var1
     * @param var2
     * @return
     */
    public static Object createOperatorFormulaStmt(Expr operator, Value var1, Value var2) {

        try {
            Class<? extends Expr> operatorClass = operator.getClass();
            if (operator instanceof NegExpr) {
                Constructor method = operatorClass.getConstructor(Value.class);
                Object stmt = method.newInstance(var2);
                return stmt;
            } else {
                Constructor method = operatorClass.getConstructor(Value.class, Value.class);
                Object stmt = method.newInstance(var1, var2);
                return stmt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object createOperatorFormulaStmt(Expr operator, Value var1) {

        try {
            Class<? extends Expr> operatorClass = operator.getClass();
            Constructor method = operatorClass.getConstructor(Value.class);
            Object stmt = method.newInstance(var1);
            return stmt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
