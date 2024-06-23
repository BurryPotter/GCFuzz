package test;

import codegen.blocks.*;
import codegen.blocks.add.LocalRefBlock;
import codegen.operators.*;
import codegen.operators.AllOperator;
import codegen.operators.addoperators.AddFieldOperator;
import codegen.providers.*;
import codegen.utils.SootHelper;
import com.google.gson.Gson;
import config.FuzzingConfig;
import dtjvms.DTConfiguration;
import dtjvms.JvmInfo;
import dtjvms.ProjectInfo;
import dtjvms.executor.GC.GCExecutor;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.fjvms.config.ConfigJTRegBean;
import dtjvms.loader.DTLoader;
import examples.OptionParse;
import optiongen.bean.OptionBean;
import optiongen.generator.Generator;
import org.junit.Test;
import soot.*;
import soot.JastAddJ.ForStmt;
import soot.JastAddJ.WhileStmt;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.options.Options;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import utils.ClassUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.PhantomReference;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMain {
    private static String dirpath = "sootOutput";

    @Test
    public void test() {
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass("java.util.Random");

        RefType randomRefType = RefType.v("java.util.Random");
        Local random = Jimple.v().newLocal(NameProvider.genVarName() + "_random", randomRefType);
        List<SootMethod> methodList = seedClass.getMethods();
        for (int i = 0; i < methodList.size(); i++) {
            System.out.println(methodList.get(i).getSignature());
        }
//        test.Empty.main(new String[1]);
//        System.out.println(RefType.v("java.lang.String").getClassName());
    }

    @Test
    public void jimple2class() {
        ClassUtils.initSootEnv();
        Options.v().set_src_prec(Options.src_prec_jimple);
        Options.v().set_output_format(Options.output_format_class);
        String className = "test.Empty";
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void java2class() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassUtils.saveClassToPath(seedClass, dirpath);
        for (SootMethod m : seedClass.getMethods()) {
            for (Unit u : m.getActiveBody().getUnits()) {
                System.out.println(u);
            }
        }
    }

    @Test
    public void loadJavaClass() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();
        SootClass sootClass = ClassUtils.loadClass("sun.hotspot.WhiteBox");

//        FuzzingConfig.setRandomSeed(654646564L);
//        FuzzingConfig.setRandomSeed(16757744464987214L);
//        FuzzingConfig.setRandomSeed(1675774447457891L);

        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
//            FuzzingConfig.updateSeed(16757744464987214L);

            System.out.println(sootMethod.getName());
            boolean cond = sootMethod.getName().equals("main");
            if (cond) {
                Unit identity = null;
                Unit specialInvoke = null;
                UnitPatchingChain units = sootMethod.retrieveActiveBody().getUnits();
                Value instance = SootHelper.findInstance(seedClass, sootMethod, false);

                if (sootMethod.isConstructor()) {
                    for (Unit unit : units) {
                        if (unit instanceof InvokeStmt) {
                            if (unit.getUseBoxes().get(0).getValue().equals(instance)
                                    && unit.getUseBoxes().get(1).getValue() instanceof SpecialInvokeExpr) {
                                specialInvoke = unit;
                                break;
                            }
                        }
                    }
                    System.out.println(specialInvoke);
                    if (specialInvoke != null) {
                        units.remove(specialInvoke);
                    }
                }

                for (int i = 0; i < 2; i++) {
//                    Operator operator = AllOperator.getInstance().operators.get(FuzzingConfig.nextChoice(AllOperator.getInstance().operators.size()));
//                    Operator operator = AddOperator.getInstance().addOperators.get(FuzzingConfig.nextChoice(AddOperator.getInstance().addOperators.size()));
//                    Operator operator = ModifyOperator.getInstance().modifyOperators.get(FuzzingConfig.nextChoice(ModifyOperator.getInstance().modifyOperators.size()));
//                    Operator operator = DeleteOperator.getInstance();
                    List<Operator> list = new ArrayList<>(ControlOperator.getInstance().controlOperators.values());
                    Operator operator = list.get(FuzzingConfig.nextChoice(list.size()));

                    List<Stmt> targets;
                    if (operator instanceof ControlOperator) {
                        targets = ElementsProvider.getTargets(info, sootMethod.getSignature(), 1);
                    } else {
                        targets = ElementsProvider.getTargets(info, sootMethod.getSignature(), 2);
                    }

                    BasicBlock block = operator.nextBlock(info, sootMethod.getSignature(), targets);

                    if (block != null) {
                        System.out.println(block.getClass() + "\t" + block.insertBlock(info, sootMethod) + "\t" + block);
                    } else {
                        System.out.println("null " + operator.getClass());
                    }
                }
                if (sootMethod.isConstructor()) {
                    System.out.println(identity);
                    for (Unit unit : units) {
                        if (unit instanceof IdentityStmt) {
                            if (unit.getDefBoxes().get(0).getValue().equals(instance)
                                    && unit.getUseBoxes().get(0).getValue() instanceof ThisRef) {
                                identity = unit;
                                break;
                            }
                        }
                    }
                    if (identity != null && specialInvoke != null) {
                        units.insertAfter(specialInvoke, identity);
                    }
                }


            }
//            Local local = Jimple.v().newLocal("newLocal", ArrayType.v(IntType.v(), 1));
//            Local localLen = Jimple.v().newLocal("length", IntType.v());
//
//            sootMethod.retrieveActiveBody().getLocals().add(local);
//            sootMethod.retrieveActiveBody().getLocals().add(localLen);
//
//            Stmt s = Jimple.v().newAssignStmt(localLen, Jimple.v().newLengthExpr(local));
//            sootMethod.retrieveActiveBody().getUnits().insertBefore(s, sootMethod.retrieveActiveBody().getUnits().getLast());
        }
//        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
//            for (int i = 0; i < 3; i++) {
//
//                BasicBlock block = ModifyRefOperator.getInstance().nextBlock(info, sootMethod.getSignature(), ElementsProvider.getTargets(info, sootMethod.getSignature()));
//                block.insertBlock(info, sootMethod);
//            }
//        }
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void testLoop() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();

        LoopBlock block = new LoopBlock();
        block.setGotoTarget(Jimple.v().newNopStmt());
        Local loopIndex = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        block.addLocalVar(loopIndex);
        JAssignStmt initStmt = (JAssignStmt) Jimple.v().newAssignStmt(loopIndex, IntConstant.v(0));
        block.setInitStmt(initStmt);
        GeExpr cond = Jimple.v().newGeExpr(loopIndex, IntConstant.v(100));
        //03 head stmt
        // 等待重定向到循环结束
        // 设置跳转的目标为seq后新插入的nop语句
        IfStmt headStmt = Jimple.v().newIfStmt(cond, block.getGotoTarget());
        block.setHeadStmt(headStmt);

        GotoStmt backJumpStmt = Jimple.v().newGotoStmt(headStmt);
        block.setBackJumpStmt(backJumpStmt);
        //05 set loop step
        AddExpr newValue = Jimple.v().newAddExpr(loopIndex, IntConstant.v(1));
        AssignStmt stepStmt = Jimple.v().newAssignStmt(loopIndex, newValue);
        block.setStepStmt(stepStmt);


        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
            List<Stmt> list = new ArrayList<>();
            list.add((Stmt) sootMethod.retrieveActiveBody().getUnits().getLast());
            block.setContents(list);
            block.insertBlock(info, sootMethod);
        }
        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
            List<Stmt> list = new ArrayList<>();
            list.add(backJumpStmt);
            LoopBlock block1 = LoopOperator.getInstance().nextBlock(info, sootMethod.getSignature(), list);
            block1.insertBlock(info, sootMethod);
        }

        ClassUtils.saveClassToPath(seedClass, dirpath);

    }

    @Test
    public void testNull() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();

        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
            for (int i = 0; i < 3; i++) {
//                RefType type = GCTypeProvider.gcRefType();
////            RefType type = RefType.v("java.lang.String");
//                System.out.println(type);
//                LocalRefBlock block = new LocalRefBlock(type);
//                block.setInserationTarget((Stmt) sootMethod.retrieveActiveBody().getUnits().getLast());
//                block.insertBlock(new ClassInfo(seedClass), sootMethod);
//                System.out.println(sootMethod);
//                DuplicatedStringBlock block = new DuplicatedStringBlock(GCValueProvider.nextString());
//                block.setInserationTarget((Stmt) sootMethod.retrieveActiveBody().getUnits().getLast());

            }
            Local local = Jimple.v().newLocal("newLocal", ArrayType.v(IntType.v(), 1));

            sootMethod.retrieveActiveBody().getLocals().add(local);

            Stmt s = Jimple.v().newAssignStmt(local, NullConstant.v());
            sootMethod.retrieveActiveBody().getUnits().insertBefore(s, sootMethod.retrieveActiveBody().getUnits().getLast());
        }
//        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
//            for (int i = 0; i < 3; i++) {
//
//                BasicBlock block = ModifyRefOperator.getInstance().nextBlock(info, sootMethod.getSignature(), ElementsProvider.getTargets(info, sootMethod.getSignature()));
//                block.insertBlock(info, sootMethod);
//            }
//        }
        System.out.println(sootMethods);
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void testSystemGC() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();

        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {

            SootClass sootClass = ClassUtils.loadClass("java.lang.System");
            assert sootClass != null;
            for (SootMethod sootMethod1 : sootClass.getMethods()) {
                System.out.println(sootMethod1.getSignature());
            }
            SootMethod method = sootClass.getMethod("gc", new ArrayList<>(), VoidType.v());
            Stmt s = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef()));
            sootMethod.retrieveActiveBody().getUnits().insertBefore(s, sootMethod.retrieveActiveBody().getUnits().getLast());
        }
        System.out.println(sootMethods);
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void testWhiteBox() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();
        SootClass sootClass = ClassUtils.loadClass("sun.hotspot.WhiteBox");

        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {

            assert sootClass != null;
            for (SootMethod sootMethod1 : sootClass.getMethods()) {
                System.out.println(sootMethod1.getSignature());
            }
            SootMethod method = sootClass.getMethod("getWhiteBox", new ArrayList<>(), RefType.v(sootClass));
//            Stmt s = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef()));
            Local whiteBoxInstance = Jimple.v().newLocal("whitebox", RefType.v(sootClass));
            sootMethod.retrieveActiveBody().getLocals().add(whiteBoxInstance);
            Stmt assign = Jimple.v().newAssignStmt(whiteBoxInstance, Jimple.v().newStaticInvokeExpr(method.makeRef()));
            SootMethod youngGC = sootClass.getMethod("youngGC", new ArrayList<>(), VoidType.v());
            Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(whiteBoxInstance, youngGC.makeRef()));
            SootMethod fullGC = sootClass.getMethod("fullGC", new ArrayList<>(), VoidType.v());
            Stmt invoke2 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(whiteBoxInstance, fullGC.makeRef()));

            sootMethod.retrieveActiveBody().getUnits().insertBefore(assign, sootMethod.retrieveActiveBody().getUnits().getLast());
            sootMethod.retrieveActiveBody().getUnits().insertBefore(invoke, sootMethod.retrieveActiveBody().getUnits().getLast());
            sootMethod.retrieveActiveBody().getUnits().insertBefore(invoke2, sootMethod.retrieveActiveBody().getUnits().getLast());

        }
        System.out.println(sootMethods);
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void testPrint() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();
        for (SootMethod sootMethod : new ArrayList<>(sootMethods)) {
            Body sootMethodBody = sootMethod.retrieveActiveBody();
            RefType refType = RefType.v("java.io.PrintStream");
            Local printer = Jimple.v().newLocal(NameProvider.genVarName() + "_printer", refType);
            SootClass system = ClassUtils.loadClass("java.lang.System");
            Stmt assignStmt = Jimple.v().newAssignStmt(printer, Jimple.v().newStaticFieldRef(system.getField("err", refType).makeRef()));
            Local msg = Jimple.v().newLocal(NameProvider.genVarName() + "_msg", RefType.v("java.lang.String"));
            Stmt msgAssign = Jimple.v().newAssignStmt(msg, StringConstant.v("GCFuzzing: Check Error"));
            SootMethod printMethod = refType.getSootClass().getMethod("void println(java.lang.String)");
            Stmt printStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(printer, printMethod.makeRef(), msg));

            sootMethodBody.getLocals().add(printer);
            sootMethodBody.getLocals().add(msg);
            sootMethodBody.getUnits().insertBefore(assignStmt, sootMethodBody.getUnits().getLast());
            sootMethodBody.getUnits().insertBefore(msgAssign, sootMethodBody.getUnits().getLast());
            sootMethodBody.getUnits().insertBefore(printStmt, sootMethodBody.getUnits().getLast());

//            Local msg = Jimple.v().newLocal(NameProvider.genVarName() + "_msg", RefType.v("java.lang.String"));
//            Stmt msgAssign = Jimple.v().newAssignStmt(msg, StringConstant.v("GCFuzzing: Check Error"));
//            AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(refType));
//            SootMethod method = refType.getSootClass().getMethod("", new ArrayList<>(),refType);
        }
        ClassUtils.saveClassToPath(seedClass, dirpath);
    }

    @Test
    public void t() {
        String cp = "D:\\projects\\GCGenerator\\JITFuzzing\\03results\\1677102543\\classHistory\\arguments.DetermineMaxHeapForCompressedOops";
        String className = "arguments.DetermineMaxHeapForCompressedOops_0101@1677105428061";
        soot.Main.main(new String[]{"-pp", "-src-prec", "J", "-f", "c", "-cp", cp, className});
//        System.out.println(DTConfiguration.TARGET_JVMS.contains("openj9"));
    }

    @Test
    public void configBean() {
//        Generator generator = new Generator();
//        Map<String, String> map = generator.generateOption();
//        String hotspotOption = map.get("hotspot");
//        ConfigBean bean = new ConfigBean();
    }

    @Test
    public void loadClass() {
        String className = "arguments.DetermineMaxHeapForCompressedOops";
        String path = "D:\\projects\\GCGenerator\\JITFuzzing\\03results\\1677102543\\classHistory\\arguments.DetermineMaxHeapForCompressedOops";
        ClassUtils.initSootEnvWithClassPath(path);
        Options.v().set_src_prec(Options.src_prec_jimple);
        Options.v().set_output_format(Options.output_format_class);
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        ClassUtils.saveClassToPath(seedClass, "D:\\Download");
        System.out.println(info);

    }

    @Test
    public void testDeathVarAnalysis() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();
        for (SootMethod method : sootMethods) {
            System.out.println(method.getName());

            UnitPatchingChain unitPatchingChain = method.retrieveActiveBody().getUnits();
            for (Unit unit : unitPatchingChain) {
                ArrayList<Local> locals = new ArrayList<>(GCElementsProvider.getDeathVars(info, method.getSignature(), unit).toList());
                System.out.println(locals);

            }
//            if (method.getName().equals("main")){
//                Local local = Jimple.v().newLocal("name",RefType.v("java.lang.Object"));
//                JAssignStmt initStmt = (JAssignStmt) Jimple.v().newAssignStmt(local, NullConstant.v());
//                method.retrieveActiveBody().getLocals().add(local);
//                method.retrieveActiveBody().getUnits().add(initStmt);
//            }
        }
        ClassUtils.saveClassToPath(seedClass, dirpath);

    }

    @Test
    public void testIfBlock() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(seedClass);
        List<SootMethod> sootMethods = seedClass.getMethods();
        for (SootMethod sootMethod : sootMethods) {
            if (sootMethod.getName().equals("main")) {
                for (int i = 0; i < 10; i++) {

                    Operator operator = ModifyOperator.getInstance().modifyOperators.get(FuzzingConfig.nextChoice(ModifyOperator.getInstance().modifyOperators.size()));
                    List<Stmt> targets;
                    if (operator instanceof ControlOperator) {
                        targets = ElementsProvider.getTargets(info, sootMethod.getSignature(), 1);
                    } else {
                        targets = ElementsProvider.getTargets(info, sootMethod.getSignature(), 2);
                    }
                    BasicBlock block = operator.nextBlock(info, sootMethod.getSignature(), targets);
                    if (block != null) {
                        boolean res = block.insertBlock(info, sootMethod);
                        System.out.println(block.getClass() + "\t" + block + "\t" + res);

                    }
                }


//                IfBlock ifBlock = new IfBlock();
//                ifBlock.setGotoTarget(Jimple.v().newNopStmt());
//
//                Local conVar = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
//                JAssignStmt initStmt = (JAssignStmt) Jimple.v().newAssignStmt(conVar, IntConstant.v(10));
//
//                Expr cond = Jimple.v().newLeExpr(conVar, IntConstant.v(0));
//
//                IfStmt headStmt = Jimple.v().newIfStmt((Value) cond, ifBlock.getGotoTarget());
//                Local local = Jimple.v().newLocal(NameProvider.genVarName()+"local",IntType.v());
//                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(local, IntConstant.v(123));
//                Unit insertation = new ArrayList<>(method.retrieveActiveBody().getUnits()).get(8);
//                method.retrieveActiveBody().getUnits().insertBefore(assignStmt,insertation);
//                ArrayList<Stmt> contents = new ArrayList<>();
//                contents.add(assignStmt);
//                assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(local, IntConstant.v(321));
//                method.retrieveActiveBody().getUnits().insertBefore(assignStmt,insertation);
//                contents.add(assignStmt);
//
//                ifBlock.setContents(contents);
//                ifBlock.setHeadStmt(headStmt);
//                ArrayList<Stmt> list = new ArrayList<>();
//                list.add(initStmt);
//                ifBlock.setInitStmts(list);
//                ifBlock.setInserationTarget((Stmt) method.retrieveActiveBody().getUnits().getLast());
//
//                ifBlock.addLocalVar(conVar);
//                ifBlock.addLocalVar(local);
//                ifBlock.insertBlock(info,method);
            }
        }
        ClassUtils.saveClassToPath(seedClass, dirpath);

    }

    @Test
    public void testCC() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass sc = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(sc);
        List<SootMethod> sootMethods = sc.getMethods();
        for (SootMethod method : sootMethods) {
            // Build the control flow graph for the method
            JimpleBody body = (JimpleBody) method.retrieveActiveBody();
            UnitGraph cfg = new ExceptionalUnitGraph(body);

            // Compute the Cyclomatic Complexity for the method
            int complexity = 1; // Start with a complexity of 1 for the single entry point
            for (Unit u : cfg) {
                Stmt s = (Stmt) u;
//                if (s.containsInvokeExpr()) {
//                    complexity++;
//                }
                if (s instanceof IfStmt || s instanceof SwitchStmt || s instanceof WhileStmt || s instanceof ForStmt) {
                    complexity++;
                }
            }
            System.out.println("Cyclomatic Complexity for method " + method.getName() + ": " + complexity);
        }

    }

    @Test
    public void testAlia() {
        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass sc = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(sc);
        List<SootMethod> sootMethods = sc.getMethods();
        for (SootMethod method : sootMethods) {
            if (!method.getName().equals("main")) {
                continue;
            }
            Body body = method.retrieveActiveBody();
            LocalMustAliasAnalysis aliasAnalysis = new LocalMustAliasAnalysis(new CompleteUnitGraph(body));
            List<Local> locals = new ArrayList<>(body.getLocals());
            Stmt stmt = (Stmt) body.getUnits().getFirst();
            Local local1 = locals.get(3);
            Local local2 = locals.get(4);
            System.out.println(local1 + "=" + local2 + "?");
            System.out.println(aliasAnalysis.mustAlias(local1, stmt, local2, stmt));
        }
        ClassUtils.saveClassToPath(sc, dirpath);

    }

    @Test
    public void testExtend() {
        ClassUtils.initSootEnv();

        SootClass child = ClassUtils.loadClass("codegen.blocks.add.AddFieldBlock");
        SootClass parent = ClassUtils.loadClass("codegen.blocks.Generic");
        boolean bool = SootHelper.isExtends(child, parent);
        System.out.println(bool);
    }

    @Test
    public void testThrow() {
        ClassUtils.initSootEnv();

        String className = "test.Empty";
        ClassUtils.initSootEnv();
        SootClass sc = ClassUtils.loadClass(className);
        ClassInfo info = new ClassInfo(sc);
        List<SootMethod> sootMethods = sc.getMethods();
        for (SootMethod sootMethod : sootMethods) {
            if (sootMethod.isMain()) {
                Body body = sootMethod.retrieveActiveBody();
                RefType refType = RefType.v("java.lang.RuntimeException");
                Local newLocal = Jimple.v().newLocal(NameProvider.genVarName() + "_newRef", refType);
                Local msg = Jimple.v().newLocal(NameProvider.genVarName() + "_msg", RefType.v("java.lang.String"));
                Stmt msgAssign = Jimple.v().newAssignStmt(msg, StringConstant.v("My message"));
                SootMethod init = refType.getSootClass().getMethod("void <init>(java.lang.String)");
                AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(refType));
                Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newLocal, init.makeRef(), Collections.singletonList(msg)));
                Stmt throwStmt = Jimple.v().newThrowStmt(newLocal);
                Value cond = null;
                cond = Jimple.v().newNeExpr(body.getLocals().getFirst(), NullConstant.v());

                Stmt nop = Jimple.v().newNopStmt();
                GotoStmt gotoStmt = Jimple.v().newGotoStmt(nop);
                IfStmt headStmt = Jimple.v().newIfStmt(cond, nop);

                body.getLocals().add(newLocal);
                body.getLocals().add(msg);
                body.getUnits().insertBeforeNoRedirect(nop, body.getUnits().getLast());

//                body.getUnits().insertBeforeNoRedirect(gotoStmt, nop);
                body.getUnits().insertBeforeNoRedirect(headStmt, nop);

                body.getUnits().insertBeforeNoRedirect(msgAssign, nop);
                body.getUnits().insertBeforeNoRedirect(assign, nop);
                body.getUnits().insertBeforeNoRedirect(invoke, nop);
                body.getUnits().insertBeforeNoRedirect(throwStmt, nop);


            }
        }
        ClassUtils.saveClassToPath(sc, dirpath);

    }

    @Test
    public void testCoverage() {
        String randomCov = "G1:G1Young (e-s t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t+h+) G1Young (e-s+t+h+) G1Young (e-s+t+h+) , G1:G1YoungInitialMark (e-s+t+h+) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t-h-) G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t-h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1Young (e-s+t+h ) , G1:G1Young (e-s-t+h ) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1Young (e-s-t+h+) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1SystemGC (e-s t h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1YoungInitialMark (e-s t-h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s t+h ) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s+t+h-) G1Young (e-s-t+h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1Young (e-s t+h+) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h ) G1Young (e-s t+h-) , G1:G1Young (e-s-t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) , G1:G1ConcurrentMark G1Young (e s t h ) G1FullGCNES (e s t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t-h-) G1Young (e-s-t+h-) , G1:G1Young (e-s-t h-) G1Young (e-s t h-) G1Young (e-s+t h-) , G1:G1ConcurrentRebuildRememberedSets G1Young (e-s t+h ) G1FullGCNES (e s-t+h ) , G1:G1YoungInitialMark (e-s+t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s-t+h-) G1Young (e-s+t+h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h+) G1YoungInitialMark (e-s t+h+) , G1:G1Young (e-s+t h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h ) G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets , G1:G1Young (e-s+t+h-) G1Young (e-s+t h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t+h+) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h ) G1Young (e-s t h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1Young (e-s-t+h ) G1Young (e-s-t+h ) G1Young (e-s t-h-) , G1:G1ConcurrentRebuildRememberedSets G1Young (e-s t+h+) G1Young (e-s+t+h+) , G1:G1ConcurrentRebuildRememberedSets G1Cleanup (e s t h ) ConcurrentCleanupForNextMark , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s-t+h-) G1Young (e-s+t+h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) PSYoungGen (y-t+h-) , G1:G1YoungInitialMark (e-s t h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t h-) G1Young (e-s-t+h-) G1Young (e-s t h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t h-) G1Young (e-s t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h+) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h ) G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h ) G1Young (e-s+t+h ) , G1:G1Young (e-s t+h+) G1Young (e-s+t+h+) G1Young (e-s t+h+) , G1:G1ConcurrentMark G1Young (e-s t+h ) G1Remark (e s t h ) , G1:ConcurrentCleanupForNextMark G1Young (e-s+t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1YoungInitialMark (e-s t-h-) , G1:G1Young (e-s-t+h+) G1Young (e-s-t+h+) G1Young (e-s t+h+) , SERIAL:FullGC (y-t-h-) FullGC (y-t-h-) FullGC (y-t-h-) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s t h-) G1Young (e-s t+h ) G1Young (e-s t+h ) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , G1:G1ConcurrentMark G1Young (e-s t+h+) G1Young (e-s-t+h+) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s-t+h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s t-h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t h-) G1Young (e-s-t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t+h+) G1YoungInitialMark (e-s t+h+) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t+h-) G1Young (e-s t+h+) G1Young (e-s t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s+t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h+) G1Young (e-s-t+h+) G1Young (e-s-t+h+) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s+t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1ConcurrentRebuildRememberedSets G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t h-) G1Young (e-s+t-h-) G1Young (e-s-t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1YoungInitialMark (e-s-t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s t-h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s+t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t h-) G1Young (e-s t-h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t h-) G1Young (e-s-t h-) , G1:G1Young (e-s+t+h ) G1Cleanup (e s t h ) ConcurrentCleanupForNextMark , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h ) PSYoungGen (y-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s t h-) , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Young (e-s+t+h ) , G1:G1Young (e-s t+h ) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t h-) G1Young (e-s+t h-) G1Young (e-s-t h-) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) , G1:G1Young (e-s t+h+) G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1ConcurrentRebuildRememberedSets G1Young (e-s t-h-) G1Cleanup (e s t h ) , G1:G1Young (e-s t+h+) G1Young (e-s t+h+) G1YoungInitialMark (e-s t+h+) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t+h-) G1Young (e-s+t-h-) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s+t h ) ConcurrentClearClaimedMarks , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s t-h-) G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h-) G1Young (e-s+t+h ) G1YoungInitialMark (e-s+t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s+t+h-) G1Young (e-s-t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s-t h-) G1Young (e-s+t+h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s t+h ) , G1:G1YoungInitialMark (e-s-t+h ) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Young (e-s+t+h ) , G1:G1FullGCNES (e s t h ) G1Cleanup (e s t h ) G1ConcurrentRebuildRememberedSets , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h-) PSYoungGen (y-t h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h ) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t-h-) G1Young (e-s-t h-) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h ) G1Young (e-s t+h ) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1FullGCNES (e s t-h-) G1YoungInitialMark (e-s t+h ) ConcurrentClearClaimedMarks , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s+t-h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h ) G1Young (e-s+t+h ) , G1:G1YoungInitialMark (e-s t+h+) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h-) G1Young (e-s-t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s-t+h ) G1Young (e-s t-h-) G1Young (e-s-t+h ) , G1:G1Young (e s t h ) G1FullGCNES (e s t h ) G1FullGCNES (e s t h ) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h+) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t h-) G1Young (e-s-t-h-) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s-t+h-) G1Young (e-s t+h-) G1Young (e-s+t-h-) , G1:G1Young (e-s-t+h-) G1Young (e-s t h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h ) G1Young (e-s+t+h ) , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Cleanup (e s t h ) , G1:G1Young (e-s-t+h-) G1Young (e-s+t h-) G1Young (e-s t h-) , G1:G1Young (e-s-t h-) G1Young (e-s+t h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s+t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h+) G1Remark (e s t h ) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t+h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1Young (e-s+t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h-) G1Young (e-s+t+h ) , G1:G1YoungInitialMark (e-s+t+h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s+t-h-) G1Young (e-s-t h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t+h ) , G1:G1Young (e-s t h-) G1Young (e-s t-h-) G1Young (e-s t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t h-) G1Young (e-s t-h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t-h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h ) G1YoungInitialMark (e-s+t+h ) , G1:G1Young (e-s-t h-) G1Young (e-s+t+h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t+h+) G1Young (e-s+t+h+) G1Young (e-s+t+h+) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t h-) PSYoungGen (y-t h ) , G1:G1Young (e-s t+h ) G1Young (e-s t-h-) G1Young (e-s t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t h-) G1Young (e-s-t+h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSFullGC (y-t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h ) G1Young (e-s+t+h ) , G1:G1Young (e-s t+h+) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1FullGCNES (e s t h ) G1FullGCNES (e s t h ) ConcurrentCleanupForNextMark , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t+h ) G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s-t h-) G1Young (e-s+t h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , G1:G1Young (e-s-t+h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1Young (e-s t+h+) G1Young (e-s+t+h+) G1YoungInitialMark (e-s t+h+) , G1:G1Young (e-s t+h+) G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h ) , G1:G1Young (e-s t h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t h-) G1Young (e-s+t+h-) , G1:G1Young (e-s-t+h ) G1Young (e-s+t+h-) G1Young (e-s+t+h ) , G1:G1Young (e-s t+h-) G1FullGCNES (e s t h ) G1FullGCNES (e s t h ) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Young (e s t h ) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s-t-h-) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s+t-h-) ConcurrentClearClaimedMarks , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s t h-) , G1:G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1SystemGC (e s t h ) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , G1:G1ConcurrentMark G1Young (e-s+t+h ) G1Remark (e s t h ) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s+t h ) , G1:G1Young (e-s+t+h ) G1Young (e-s-t+h-) G1Young (e-s+t+h ) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h ) G1Young (e-s+t+h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t+h-) G1FullGCNES (e s t h ) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s+t+h ) , G1:G1Young (e s t h ) G1Young (e-s t+h ) G1Young (e s t h ) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s+t+h-) , G1:G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t h-) , G1:G1Young (e-s+t+h+) G1Young (e-s t+h ) G1Young (e-s t+h+) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1YoungInitialMark (e-s t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s-t h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t-h-) G1Young (e-s+t+h-) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t h-) G1Young (e-s+t h-) G1Young (e-s t h-) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s t-h-) , G1:G1YoungInitialMark (e-s-t+h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t h-) G1Young (e-s-t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1YoungInitialMark (e-s t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h+) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s+t h-) G1Young (e-s+t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h ) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1Young (e s t h ) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1Young (e-s+t-h-) G1Young (e-s+t h-) G1Young (e-s t+h-) , G1:G1Young (e-s t h-) G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t+h-) G1Young (e-s t+h+) G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s t+h ) G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t-h-) G1Young (e-s-t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s+t-h-) G1YoungInitialMark (e-s+t-h-) ConcurrentClearClaimedMarks , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s+t h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:G1ConcurrentMark G1Young (e-s t+h+) G1Remark (e s t h ) , G1:G1Young (e-s t+h-) G1Young (e-s-t-h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t h-) G1Young (e-s+t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t+h+) G1Young (e-s+t+h+) G1YoungInitialMark (e-s+t+h+) , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s t h-) , G1:G1Young (e-s+t h-) G1Young (e-s+t-h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h ) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t+h ) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t+h+) , G1:G1Young (e-s+t+h+) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t+h-) ConcurrentClearClaimedMarks , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s+t h-) G1Young (e-s t+h-) G1Young (e-s t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e s t h ) G1Young (e s t h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1YoungInitialMark (e-s t-h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h-) G1Young (e-s t+h+) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t h-) G1Young (e-s t+h-) , G1:G1ConcurrentMark G1Young (e s t h ) G1FullGCNES (e s t h ) , G1:G1Young (e-s+t h-) G1Young (e-s t+h-) G1Young (e-s-t-h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h ) G1Young (e-s t+h ) , G1:G1ConcurrentMark G1Young (e-s t+h+) G1Young (e-s t+h+) , G1:G1YoungInitialMark (e-s-t h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h ) G1Young (e-s+t+h ) G1Young (e-s+t+h-) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) G1SystemGC (e-s t+h-) , G1:G1Young (e-s-t-h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) PSYoungGen (y-t h ) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h ) G1Young (e-s-t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h-) G1Young (e-s t h-) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t h-) ConcurrentClearClaimedMarks , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Young (e-s t+h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h ) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s+t+h-) , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Young (e-s t-h-) , G1:G1Young (e-s t h-) G1Young (e-s-t h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:ConcurrentClearClaimedMarks ConcurrentScanRootRegion G1ConcurrentMark , G1:G1Young (e-s t-h-) G1YoungInitialMark (e-s-t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h ) , G1:G1Young (e-s t-h-) G1Young (e-s t+h ) G1Young (e-s t-h-) , G1:G1Young (e-s+t h-) G1Young (e-s-t+h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h+) G1YoungInitialMark (e-s t+h+) ConcurrentClearClaimedMarks , G1:G1Young (e-s-t+h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h-) G1Young (e-s t+h+) , G1:G1Young (e-s-t+h-) G1Young (e-s+t h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h+) G1YoungInitialMark (e-s+t+h+) ConcurrentClearClaimedMarks , G1:G1Young (e-s-t h-) G1Young (e-s+t h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t+h-) ConcurrentModeFailure (y t+h ) PSFullGC (y-t-h-) , G1:G1Young (e-s t+h ) G1Cleanup (e s t h ) ConcurrentCleanupForNextMark , G1:G1Young (e-s+t h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h-) G1Young (e-s+t+h ) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Cleanup (e s t h ) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t+h ) ConcurrentClearClaimedMarks , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Young (e-s t+h+) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) ConcurrentModeFailure (y t h ) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h ) G1Young (e-s+t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1Young (e-s t+h ) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h-) G1Young (e-s t+h+) , G1:G1Young (e s t h ) G1FullGCNES (e s t-h-) G1YoungInitialMark (e-s t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s+t+h ) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1Young (e s t+h+) , G1:G1Young (e-s-t h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h-) G1Young (e-s t+h+) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t+h-) G1Young (e-s-t h-) , G1:G1Young (e-s+t h-) G1Young (e-s t h-) G1Young (e-s-t-h-) , G1:G1Young (e-s+t h-) G1Young (e-s-t h-) G1Young (e-s+t h-) , G1:G1Young (e-s-t+h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s+t-h-) G1Young (e-s t-h-) , PARALLEL:ConcurrentModeFailure (y t+h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t+h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h ) G1Young (e-s t+h-) , PARALLEL:ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s+t h-) G1Young (e-s+t h-) G1Young (e-s+t h-) , G1:G1Young (e-s-t h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1YoungInitialMark (e-s t+h ) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s+t+h ) G1YoungInitialMark (e-s+t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t-h-) G1Cleanup (e s t h ) ConcurrentCleanupForNextMark , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t+h ) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , G1:G1Young (e-s t-h-) G1YoungInitialMark (e-s t h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h-) G1Young (e-s t+h+) G1Young (e-s t+h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) ConcurrentModeFailure (y t+h ) , G1:G1Young (e-s t+h ) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t+h ) G1Young (e-s+t+h-) , G1:G1Young (e-s-t h-) G1Young (e-s+t h-) G1Young (e-s-t h-) , G1:G1FullGCNES (e s t h ) G1FullGCNES (e s t h ) G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t+h-) G1Young (e-s-t h-) , G1:G1YoungInitialMark (e-s+t h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s t h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s-t+h-) G1Young (e-s t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s t+h+) , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Young (e-s t+h+) , PARALLEL:PSYoungGen (y-t h-) ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s-t+h-) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e s t h ) , G1:G1Young (e-s-t+h ) G1Young (e-s+t+h ) G1Young (e-s+t+h ) , PARALLEL:PSYoungGen (y-t+h-) ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h+) G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t h-) PSYoungGen (y-t+h ) , G1:G1Young (e-s t+h ) G1FullGCNES (e s-t+h ) G1FullGCNES (e s t h ) , G1:G1Young (e-s-t+h-) G1Young (e-s+t h-) G1Young (e-s+t h-) , G1:G1Young (e-s t+h ) G1YoungInitialMark (e-s+t+h ) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t h-) G1Young (e-s+t h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) ConcurrentModeFailure (y t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s+t h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s t+h-) G1Young (e-s t+h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Young (e-s t+h ) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t+h ) , G1:G1Young (e-s+t h-) G1Young (e-s-t h-) G1Young (e-s t+h-) , G1:G1Young (e-s-t-h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Remark (e s t h ) , G1:G1FullGCNES (e s-t+h ) G1FullGCNES (e s t h ) G1Cleanup (e s t h ) , G1:G1Young (e-s t-h-) G1Young (e-s t h-) G1Young (e-s t+h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s-t-h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s t+h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t-h-) , G1:G1Young (e-s t+h ) G1Young (e-s t h-) G1Young (e-s t+h ) , G1:G1Young (e-s+t h-) G1Young (e-s+t h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s+t-h-) G1Young (e-s+t-h-) , G1:G1ConcurrentRebuildRememberedSets G1Young (e-s+t+h ) G1Cleanup (e s t h ) , G1:G1Young (e-s t+h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s-t+h-) G1Young (e-s+t h-) G1Young (e-s-t h-) , G1:ConcurrentCleanupForNextMark G1Young (e-s+t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s t-h-) G1Young (e-s-t+h ) G1Young (e-s t+h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t-h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s-t-h-) G1YoungInitialMark (e-s+t-h-) ConcurrentClearClaimedMarks , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s+t-h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h ) G1Young (e-s t+h-) , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Young (e-s t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s-t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1YoungInitialMark (e-s+t h ) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s-t+h-) G1Young (e-s+t-h-) G1Young (e-s-t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t-h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h ) G1Young (e-s+t+h ) , G1:ConcurrentCleanupForNextMark G1Young (e-s t-h-) G1YoungInitialMark (e-s t h-) , G1:G1YoungInitialMark (e-s+t+h ) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t+h+) G1Young (e-s t+h+) G1Young (e-s+t+h+) , G1:G1Young (e-s t-h-) G1Young (e s t h ) G1Young (e s t h ) , G1:G1FullGCNES (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t+h-) , G1:G1Young (e-s-t-h-) G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s+t h-) G1Young (e-s+t+h-) G1Young (e-s-t+h-) , G1:G1Young (e-s+t+h ) G1Young (e-s+t+h-) G1Young (e-s+t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h ) G1Young (e-s+t+h ) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s+t-h-) , G1:G1ConcurrentMark G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets , G1:G1Young (e-s+t-h-) G1Young (e-s t+h ) G1Young (e-s t-h-) , G1:G1Young (e-s t+h+) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1YoungInitialMark (e-s+t-h-) , G1:G1Young (e-s t h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e s t h ) G1Young (e s t h ) G1Young (e s t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t h-) G1Young (e-s t+h-) , G1:G1FullGCNES (e s t h ) G1YoungInitialMark (e-s t+h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t h-) , G1:G1Young (e-s t h-) G1Young (e-s t-h-) G1Young (e-s t+h ) , G1:G1Young (e-s t+h ) G1YoungInitialMark (e-s t+h ) ConcurrentClearClaimedMarks";
        Set<String> eventSet = Arrays.stream(randomCov.split(",")).map(ngram -> ngram.trim().replaceAll("\\(.*?\\)", "")).collect(Collectors.toSet());
        System.out.println(eventSet.size());
        System.out.println(eventSet);

        String randomCov2 = "CMS:ConcurrentMark ParNew (y-t h-) ConcurrentMark , G1:G1SystemGC (e-s t-h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , SERIAL:FullGC (y-t h-) FullGC (y t h ) FullGC (y-t h-) , CMS:ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , G1:G1Young (e-s t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , SERIAL:FullGC (y-t-h-) FullGC (y t h ) FullGC (y-t+h-) , G1:G1SystemGC (e-s t+h ) G1SystemGC (e-s t+h ) G1SystemGC (e s t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s+t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s+t+h-) , CMS:ConcurrentSweep ConcurrentSweep ParNew (y-t h-) , CMS:ParNew (y-t+h+) ParNew (y-t+h+) InitialMark (y t h ) , G1:G1SystemGC (e-s t h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , PARALLEL:PSFullGC (y t h ) PSYoungGen (y t h ) PSFullGC (y t h ) , G1:G1SystemGC (e-s-t-h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ParNew (y-t+h+) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s-t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , Z:ZGCCycle (l a-g+) ZGCCycle (l a-g+) ZGCCycle (l a-g ) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) , CMS:ParNew (y-t+h ) ParNew (y-t+h ) ParNew (y-t h ) , G1:G1Young (e-s-t-h-) G1Young (e-s+t+h-) G1Young (e-s t-h-) , SERIAL:FullGC (y-t+h-) FullGC (y t h ) FullGC (y-t+h-) , CMS:ParNew (y-t h ) ParNew (y-t h ) ParNew (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSFullGC (y-t+h-) , Z:ZGCCycle (l a g ) ZGCCycle (l a-g ) ZGCCycle (l a-g+) , CMS:ParNew (y-t+h ) ParNew (y-t h-) ParNew (y-t h-) , SERIAL:FullGC (y t h ) FullGC (y t h ) FullGC (y-t-h-) , PARALLEL:PSFullGC (y t-h ) PSYoungGen (y-t+h-) PSFullGC (y t-h ) , CMS:FullGC (y-t h-) FullGC (y-t h-) FullGC (y-t h-) , CMS:AbortablePreClean ParNew (y-t h-) ParNew (y-t h-) , SERIAL:FullGC (y-t+h-) FullGC (y t h ) FullGC (y t-h ) , CMS:ConcurrentSweep ParNew (y-t h-) ParNew (y-t h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h+) ParNew (y-t+h+) , CMS:ParNew (y-t h ) ParNew (y-t h ) InitialMark (y t h ) , CMS:ParNew (y+t h ) ConcurrentReset ParNew (y+t h ) , PARALLEL:PSYoungGen (y t h ) PSFullGC (y-t-h-) PSYoungGen (y-t h-) , G1:G1SystemGC (e-s t h-) G1SystemGC (e-s t h-) G1SystemGC (e s t h ) , G1:G1YoungInitialMark (e-s+t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , PARALLEL:PSYoungGen (y-t h-) PSFullGC (y-t h ) PSYoungGen (y t h ) , SERIAL:FullGC (y t h ) FullGC (y-t h-) FullGC (y-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , G1:G1ConcurrentRebuildRememberedSets G1Cleanup (e s t h ) ConcurrentCleanupForNextMark , CMS:ConcurrentReset ConcurrentReset ParNew (y-t h ) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t h-) PSYoungGen (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s-t-h-) G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks , CMS:ConcurrentModeFailure (y-t+h-) ConcurrentModeFailure (y-t+h-) ParNew (y-t h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t-h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , CMS:ConcurrentPreClean ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , CMS:ConcurrentMark ConcurrentMark ConcurrentPreClean , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y+t h ) , CMS:ConcurrentSweep ConcurrentSweep ConcurrentModeFailure (y-t+h-) , CMS:ParNew (y-t h-) ConcurrentPreClean ConcurrentPreClean , CMS:ConcurrentReset ParNew (y t h ) ParNew (y t h ) , SERIAL:FullGC (y-t h-) FullGC (y-t+h-) FullGC (y t h ) , SERIAL:FullGC (y-t-h-) FullGC (y-t-h-) FullGC (y-t-h-) , SERIAL:FullGC (y-t h-) FullGC (y t h ) FullGC (y t h ) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , PARALLEL:PSYoungGen (y+t+h ) PSFullGC (y-t-h-) PSYoungGen (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , CMS:FullGC (y-t h-) FullGC (y t h ) FullGC (y t h ) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s t-h-) , CMS:ParNew (y-t+h-) CMSRemark (y t h ) CMSRemark (y t h ) , CMS:ParNew (y-t+h+) InitialMark (y t h ) InitialMark (y t h ) , CMS:ParNew (y-t h ) InitialMark (y t h ) InitialMark (y t h ) , CMS:ConcurrentSweep ParNew (y-t+h-) ParNew (y-t+h-) , CMS:ConcurrentModeFailure (y-t h-) ParNew (y-t h-) ParNew (y-t h-) , CMS:ConcurrentPreClean ConcurrentPreClean AbortablePreClean , CMS:ParNew (y-t h-) ConcurrentReset ConcurrentReset , SERIAL:FullGC (y t h ) FullGC (y-t h-) FullGC (y t h ) , G1:ConcurrentCleanupForNextMark G1Young (e-s+t-h-) G1YoungInitialMark (e-s-t-h-) , SERIAL:FullGC (y t h ) FullGC (y t-h ) FullGC (y-t+h-) , PARALLEL:PSFullGC (y t h ) PSYoungGen (y-t+h-) PSFullGC (y t-h ) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t h-) G1Young (e-s t-h-) G1Young (e-s t+h-) , CMS:ConcurrentReset ParNew (y-t h ) InitialMark (y t h ) , CMS:FullGC (y-t h-) FullGC (y-t+h-) FullGC (y-t+h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s t h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ConcurrentReset , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , CMS:InitialMark (y t h ) ConcurrentMark ConcurrentPreClean , CMS:ParNew (y-t+h-) InitialMark (y t h ) InitialMark (y t h ) , CMS:FullGC (y t h ) FullGC (y-t-h-) FullGC (y-t-h-) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s-t-h-) , PARALLEL:PSFullGC (y-t-h-) ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) , SERIAL:FullGC (y-t+h-) FullGC (y-t-h-) FullGC (y-t-h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y+t h ) PSFullGC (y-t-h-) , PARALLEL:ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) ConcurrentModeFailure (y t h ) , CMS:ConcurrentMark ConcurrentPreClean AbortablePreClean , CMS:ParNew (y-t h-) CMSRemark (y t h ) CMSRemark (y t h ) , CMS:ParNew (y t h ) InitialMark (y t h ) InitialMark (y t h ) , CMS:ConcurrentReset ParNew (y+t h ) ParNew (y+t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h-) PSYoungGen (y-t+h-) , SERIAL:FullGC (y t-h ) FullGC (y-t+h-) FullGC (y t h ) , PARALLEL:PSYoungGen (y-t+h+) PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y+t h ) PSFullGC (y-t-h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , G1:G1SystemGC (e s t h ) G1SystemGC (e s t h ) G1SystemGC (e-s t h-) , PARALLEL:PSFullGC (y t h ) PSYoungGen (y-t h-) PSFullGC (y-t h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h-) PSYoungGen (y-t h-) , CMS:ParNew (y-t h-) ParNew (y-t h-) ConcurrentReset , CMS:ParNew (y-t+h ) InitialMark (y t h ) InitialMark (y t h ) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y+t h ) , G1:G1Young (e-s+t h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , CMS:ConcurrentReset ParNew (y-t h ) ParNew (y-t h ) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t-h-) , CMS:ParNew (y-t h-) AbortablePreClean AbortablePreClean , CMS:ConcurrentMark ConcurrentMark ParNew (y-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s-t-h-) , PARALLEL:PSYoungGen (y+t h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , CMS:ConcurrentReset ParNew (y+t h ) InitialMark (y t h ) , SERIAL:FullGC (y t h ) FullGC (y t h ) FullGC (y-t h-) , PARALLEL:PSYoungGen (y t+h ) PSFullGC (y-t-h-) PSYoungGen (y-t h-) , CMS:ParNew (y-t h-) ParNew (y-t h-) ParNew (y-t h-) , CMS:ParNew (y-t+h-) ConcurrentPreClean ConcurrentPreClean , G1:G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets G1Cleanup (e s t h ) , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s+t h-) , CMS:ParNew (y-t h-) ParNew (y-t h ) ParNew (y-t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) G1SystemGC (e s t h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t h ) PSFullGC (y-t-h-) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t+h-) , CMS:ConcurrentReset ParNew (y-t+h-) ParNew (y-t+h-) , PARALLEL:PSFullGC (y-t h ) PSYoungGen (y-t h-) PSFullGC (y-t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h ) PSYoungGen (y-t+h+) , CMS:ConcurrentSweep ConcurrentModeFailure (y-t+h-) ConcurrentModeFailure (y-t+h-) , CMS:ConcurrentMark ParNew (y-t+h-) ParNew (y-t+h-) , PARALLEL:PSYoungGen (y-t h ) PSFullGC (y-t-h-) PSYoungGen (y-t h-) , G1:G1Young (e-s+t-h-) G1Young (e-s-t h-) G1Young (e-s t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t+h ) , Z:ZGCCycle (l a-g ) ZGCCycle (l a-g ) ZGCCycle (l a-g+) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y-t+h ) PSYoungGen (y t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSFullGC (y-t-h-) , CMS:ConcurrentMark ParNew (y-t h-) ConcurrentPreClean , G1:G1Young (e-s t+h-) G1Young (e-s t h-) G1Young (e-s t-h-) , CMS:ParNew (y-t+h-) ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , SERIAL:FullGC (y t h ) FullGC (y t h ) FullGC (y-t+h-) , G1:G1Young (e-s t h-) G1Young (e-s t h-) G1Young (e-s t-h-) , Z:ZGCCycle (l a-g+) ZGCCycle (l a-g ) ZGCCycle (l a-g+) , G1:G1Young (e-s+t+h-) G1Young (e-s t h-) G1Young (e-s+t h-) , PARALLEL:PSYoungGen (y t h ) PSFullGC (y t h ) PSYoungGen (y-t+h-) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t h-) PSYoungGen (y+t h ) , CMS:ConcurrentPreClean ConcurrentPreClean ParNew (y-t h-) , CMS:ConcurrentReset ConcurrentReset ParNew (y-t+h-) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSFullGC (y-t-h-) , SERIAL:FullGC (y t-h ) FullGC (y t h ) FullGC (y-t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , CMS:ConcurrentReset ConcurrentReset ConcurrentModeFailure (y-t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y+t+h ) , CMS:ConcurrentReset ParNew (y+t h ) ConcurrentReset , CMS:ParNew (y+t h ) InitialMark (y t h ) InitialMark (y t h ) , CMS:ParNew (y-t+h-) ParNew (y-t h-) ParNew (y-t h-) , CMS:ParNew (y-t+h ) ParNew (y-t+h ) ParNew (y-t+h-) , CMS:ConcurrentPreClean AbortablePreClean ParNew (y-t+h-) , SERIAL:FullGC (y-t h-) FullGC (y-t h-) FullGC (y t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t h-) G1SystemGC (e-s t h-) , CMS:FullGC (y-t h-) FullGC (y-t h-) FullGC (y t h ) , CMS:ParNew (y-t+h ) ParNew (y-t+h ) ParNew (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) PSYoungGen (y-t h-) , PARALLEL:PSYoungGen (y-t h-) PSFullGC (y-t h-) PSYoungGen (y t h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y t h ) PSFullGC (y-t-h-) , CMS:ParNew (y-t h-) ParNew (y-t+h-) InitialMark (y t h ) , CMS:ConcurrentReset ConcurrentReset ParNew (y+t h ) , Z:ZGCCycle (l a-g ) ZGCCycle (l a-g+) ZGCCycle (l a-g+) , CMS:ParNew (y-t h-) ParNew (y-t h-) CMSRemark (y t h ) , CMS:ConcurrentReset ParNew (y+t h+) ParNew (y+t h+) , PARALLEL:PSFullGC (y t-h ) PSYoungGen (y t h ) PSFullGC (y t h ) , CMS:ConcurrentSweep ConcurrentSweep ConcurrentModeFailure (y-t-h-) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y+t+h ) , SERIAL:FullGC (y-t+h-) FullGC (y t h ) FullGC (y-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t h-) PSYoungGen (y-t h ) , CMS:ConcurrentModeFailure (y-t-h-) ParNew (y-t h-) ParNew (y-t h-) , SERIAL:FullGC (y-t-h-) FullGC (y t h ) FullGC (y t h ) , PARALLEL:PSYoungGen (y t h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , PARALLEL:PSFullGC (y-t-h ) PSYoungGen (y-t h-) PSFullGC (y-t h ) , SERIAL:FullGC (y t h ) FullGC (y-t+h-) FullGC (y t-h ) , G1:G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h ) PSYoungGen (y-t+h+) , G1:G1SystemGC (e s t h ) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , SERIAL:FullGC (y t h ) FullGC (y-t+h-) FullGC (y-t+h-) , CMS:AbortablePreClean AbortablePreClean ParNew (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y t-h ) PSYoungGen (y t h ) , CMS:CMSRemark (y t h ) ConcurrentSweep ConcurrentSweep , SERIAL:FullGC (y t h ) FullGC (y-t h-) FullGC (y-t-h-) , G1:G1SystemGC (e-s t+h ) G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) , CMS:ParNew (y-t h-) ParNew (y-t+h-) ParNew (y-t h-) , CMS:ParNew (y-t h-) ConcurrentMark ParNew (y-t h-) , CMS:ParNew (y-t+h-) AbortablePreClean ParNew (y-t+h-) , CMS:ParNew (y-t h-) ParNew (y-t h-) ParNew (y-t+h-) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y t h ) PSFullGC (y-t-h-) , CMS:ParNew (y-t h-) ParNew (y-t h-) ParNew (y-t h ) , CMS:ParNew (y-t+h ) ParNew (y-t+h-) ParNew (y-t+h-) , CMS:ConcurrentPreClean ParNew (y-t h-) ParNew (y-t h-) , CMS:ParNew (y-t h ) ParNew (y-t+h ) ParNew (y-t+h ) , G1:G1Young (e-s+t h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , G1:G1Young (e-s+t-h-) G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSFullGC (y-t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y-t-h-) PSFullGC (y-t-h-) , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s-t-h-) G1YoungInitialMark (e-s-t-h-) , CMS:ConcurrentModeFailure (y-t h-) ConcurrentModeFailure (y-t h-) ParNew (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t h-) PSYoungGen (y-t+h-) , G1:G1SystemGC (e-s t h-) G1SystemGC (e s t h ) G1SystemGC (e-s t h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ConcurrentModeFailure (y-t-h-) , CMS:ConcurrentSweep ConcurrentReset ConcurrentMark , SERIAL:FullGC (y-t-h-) FullGC (y t h ) FullGC (y-t h-) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t h-) G1SystemGC (e s t h ) , G1:G1Young (e-s-t-h-) G1Young (e-s+t-h-) G1Young (e-s t-h-) , G1:G1SystemGC (e-s t-h-) G1SystemGC (e s t h ) G1SystemGC (e-s t h-) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , SERIAL:FullGC (y-t h-) FullGC (y t h ) FullGC (y-t+h-) , CMS:ParNew (y-t+h-) InitialMark (y t h ) ConcurrentMark , CMS:ConcurrentReset ConcurrentReset ConcurrentModeFailure (y-t+h-) , Z:ZGCCycle (l a-g+) ZGCCycle (l a-g ) ZGCCycle (l a-g ) , PARALLEL:PSYoungGen (y-t+h ) PSYoungGen (y-t+h+) PSYoungGen (y-t+h ) , PARALLEL:PSYoungGen (y-t h-) PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) , PARALLEL:PSYoungGen (y-t h-) PSFullGC (y-t-h ) PSYoungGen (y-t h-) , G1:G1Young (e-s t+h-) G1Young (e-s-t-h-) G1Young (e-s+t+h-) , G1:G1Young (e-s+t-h-) G1Young (e-s t h-) G1Young (e-s t h-) , SERIAL:FullGC (y t h ) FullGC (y-t h-) FullGC (y-t+h-) , CMS:CMSRemark (y t h ) CMSRemark (y t h ) ConcurrentSweep , CMS:FullGC (y t h ) FullGC (y t h ) FullGC (y-t h-) , CMS:FullGC (y-t-h-) FullGC (y-t h-) FullGC (y-t h-) , CMS:ConcurrentPreClean AbortablePreClean CMSRemark (y t h ) , CMS:ConcurrentReset ConcurrentReset ParNew (y t h ) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) , CMS:ParNew (y-t+h ) ParNew (y-t h ) ParNew (y-t h ) , G1:ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t-h-) ConcurrentClearClaimedMarks , G1:G1Young (e-s t-h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , CMS:FullGC (y-t+h-) FullGC (y t h ) FullGC (y t h ) , CMS:ConcurrentModeFailure (y-t-h-) InitialMark (y t h ) InitialMark (y t h ) , G1:G1Young (e-s-t-h-) G1Young (e-s t-h-) G1Young (e-s+t-h-) , PARALLEL:PSYoungGen (y t h ) PSFullGC (y t h ) PSYoungGen (y t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) G1SystemGC (e-s t+h-) , CMS:ParNew (y+t h ) ParNew (y+t h ) InitialMark (y t h ) , CMS:FullGC (y t h ) FullGC (y t h ) FullGC (y t h ) , CMS:ConcurrentMark ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s+t+h-) G1Young (e-s+t+h-) , CMS:ParNew (y+t h+) InitialMark (y t h ) InitialMark (y t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t h-) PSFullGC (y-t-h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ParNew (y-t h-) , CMS:ConcurrentPreClean CMSRemark (y t h ) CMSRemark (y t h ) , CMS:ConcurrentSweep ConcurrentReset ParNew (y+t h ) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s+t+h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h ) ParNew (y-t+h ) , G1:ConcurrentClearClaimedMarks ConcurrentScanRootRegion G1ConcurrentMark , CMS:CMSRemark (y t h ) ConcurrentSweep ConcurrentReset , CMS:ParNew (y-t+h-) ParNew (y-t h-) ParNew (y-t+h-) , CMS:ConcurrentPreClean ConcurrentPreClean CMSRemark (y t h ) , CMS:AbortablePreClean AbortablePreClean ConcurrentModeFailure (y-t-h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) CMSRemark (y t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) , CMS:ConcurrentReset ParNew (y-t h ) ConcurrentReset , CMS:ParNew (y-t h ) ParNew (y-t h ) ParNew (y-t+h ) , PARALLEL:PSYoungGen (y-t+h-) ConcurrentModeFailure (y t+h ) PSFullGC (y-t-h-) , Z:ZGCCycle (l a-g ) ZGCCycle (l a-g ) ZGCCycle (l a-g ) , Z:ZGCCycle (l a-g+) ZGCCycle (l a g ) ZGCCycle (l a-g ) , CMS:ConcurrentSweep ConcurrentSweep ConcurrentReset , CMS:ParNew (y-t+h ) ParNew (y-t+h ) ParNew (y-t+h ) , SERIAL:FullGC (y-t+h-) FullGC (y t-h ) FullGC (y t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , CMS:ParNew (y-t+h ) ParNew (y-t+h ) InitialMark (y t h ) , PARALLEL:PSFullGC (y t h ) PSYoungGen (y-t h-) PSFullGC (y-t-h ) , G1:G1Young (e-s-t+h-) G1Young (e-s t+h-) G1Young (e-s t h-) , G1:G1Young (e-s-t-h-) G1Young (e-s t-h-) G1Young (e-s-t-h-) , SERIAL:FullGC (y-t h-) FullGC (y-t-h-) FullGC (y t h ) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y+t+h ) PSFullGC (y-t-h-) , PARALLEL:PSFullGC (y-t+h ) PSYoungGen (y t h ) PSFullGC (y t h ) , PARALLEL:PSFullGC (y-t h ) PSYoungGen (y t h ) PSFullGC (y t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s+t-h-) G1Young (e-s t-h-) , CMS:AbortablePreClean ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , CMS:ParNew (y-t h-) ParNew (y-t h-) ParNew (y-t+h ) , PARALLEL:PSFullGC (y-t-h-) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , CMS:InitialMark (y t h ) ParNew (y-t+h-) InitialMark (y t h ) , CMS:ParNew (y+t h+) ParNew (y+t h+) InitialMark (y t h ) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ParNew (y-t+h ) , CMS:FullGC (y-t-h-) FullGC (y-t-h-) FullGC (y-t h-) , G1:G1Young (e-s+t h-) G1Young (e-s+t-h-) G1Young (e-s-t-h-) , CMS:AbortablePreClean AbortablePreClean ParNew (y-t+h-) , CMS:ParNew (y-t h ) ParNew (y-t h-) ParNew (y-t h-) , CMS:ParNew (y-t h-) ParNew (y-t+h-) ParNew (y-t+h-) , G1:G1Young (e-s-t+h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s-t h-) G1Young (e-s+t h-) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ConcurrentPreClean , CMS:ConcurrentSweep ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , PARALLEL:ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , G1:G1Young (e-s-t-h-) G1Young (e-s-t-h-) G1Young (e-s+t-h-) , SERIAL:FullGC (y t h ) FullGC (y-t+h-) FullGC (y-t-h-) , CMS:ConcurrentSweep ConcurrentReset ConcurrentReset , CMS:ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) ParNew (y-t h-) , G1:G1Young (e-s t-h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , G1:G1Young (e-s t+h ) G1Young (e-s t+h ) G1Young (e-s t+h ) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , PARALLEL:PSYoungGen (y+t h ) PSFullGC (y-t-h-) PSYoungGen (y-t h-) , CMS:ConcurrentModeFailure (y-t+h-) ParNew (y-t h-) ParNew (y-t h-) , CMS:ConcurrentReset ConcurrentReset ParNew (y+t h+) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e-s t+h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y-t+h-) PSYoungGen (y t h ) , CMS:ConcurrentMark ConcurrentMark ParNew (y-t+h-) , CMS:ConcurrentReset ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) , CMS:AbortablePreClean AbortablePreClean CMSRemark (y t h ) , G1:G1YoungInitialMark (e-s+t h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , G1:G1Young (e-s t-h-) G1Young (e-s-t-h-) G1Young (e-s+t+h-) , CMS:ParNew (y-t+h-) InitialMark (y t h ) ParNew (y-t+h-) , CMS:ParNew (y-t h-) ParNew (y-t+h ) ParNew (y-t+h ) , CMS:ParNew (y-t h ) ConcurrentReset ParNew (y-t h ) , G1:G1Young (e-s+t-h-) G1Young (e-s t+h-) G1Young (e-s-t-h-) , CMS:ConcurrentMark ConcurrentPreClean ConcurrentPreClean , G1:G1Young (e-s+t+h-) G1Young (e-s-t+h-) G1Young (e-s t+h-) , CMS:ConcurrentMark ParNew (y-t h-) ParNew (y-t h-) , PARALLEL:PSYoungGen (y+t+h ) PSFullGC (y-t-h-) ConcurrentModeFailure (y t h ) , CMS:ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) ParNew (y-t+h-) , CMS:ConcurrentSweep ConcurrentReset ParNew (y-t h ) , CMS:ParNew (y-t h-) ParNew (y-t h-) AbortablePreClean , CMS:ConcurrentModeFailure (y-t-h-) ConcurrentModeFailure (y-t-h-) InitialMark (y t h ) , G1:G1SystemGC (e s t h ) G1SystemGC (e-s t+h ) G1SystemGC (e-s t+h ) , PARALLEL:PSYoungGen (y t h ) PSFullGC (y t h ) PSYoungGen (y-t h-) , PARALLEL:PSYoungGen (y-t+h-) ConcurrentModeFailure (y t h ) PSFullGC (y-t-h-) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e s t h ) , CMS:AbortablePreClean ConcurrentModeFailure (y-t h-) ConcurrentModeFailure (y-t h-) , CMS:FullGC (y t h ) FullGC (y-t h-) FullGC (y-t h-) , CMS:AbortablePreClean AbortablePreClean ConcurrentModeFailure (y-t h-) , CMS:ConcurrentMark ConcurrentMark ConcurrentModeFailure (y-t-h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y t h ) , CMS:AbortablePreClean CMSRemark (y t h ) CMSRemark (y t h ) , CMS:ConcurrentPreClean ConcurrentPreClean ConcurrentModeFailure (y-t-h-) , CMS:ConcurrentReset ConcurrentReset ParNew (y-t h-) , CMS:ConcurrentReset ParNew (y-t h-) ParNew (y-t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h-) G1Young (e-s t+h-) , PARALLEL:PSYoungGen (y-t h ) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y-t+h-) PSYoungGen (y-t h-) , SERIAL:FullGC (y-t+h-) FullGC (y t h ) FullGC (y t h ) , Z:ZGCCycle (l a-g+) ZGCCycle (l a-g+) ZGCCycle (l a-g+) , G1:G1Young (e-s-t-h-) G1Young (e-s+t+h-) G1Young (e-s t+h-) , Z:ZGCCycle (l a-g ) ZGCCycle (l a-g+) ZGCCycle (l a-g ) , G1:ConcurrentScanRootRegion G1ConcurrentMark G1Remark (e s t h ) , CMS:ParNew (y-t h-) ParNew (y-t h-) ConcurrentPreClean , CMS:FullGC (y t h ) FullGC (y t h ) FullGC (y-t-h-) , CMS:ConcurrentModeFailure (y-t-h-) ParNew (y-t+h-) ParNew (y-t+h-) , SERIAL:FullGC (y-t+h-) FullGC (y t h ) FullGC (y-t-h-) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1YoungInitialMark (e-s t-h-) , CMS:InitialMark (y t h ) InitialMark (y t h ) ConcurrentMark , G1:G1Young (e-s t+h-) G1Young (e-s t h-) G1Young (e-s t h-) , G1:G1Young (e-s+t+h-) G1Young (e-s t+h ) G1Young (e-s t+h ) , PARALLEL:PSYoungGen (y-t h-) PSFullGC (y-t-h-) PSYoungGen (y-t+h-) , SERIAL:FullGC (y t h ) FullGC (y-t+h-) FullGC (y t h ) , CMS:InitialMark (y t h ) ConcurrentMark ParNew (y-t h-) , G1:G1YoungInitialMark (e-s-t-h-) ConcurrentClearClaimedMarks ConcurrentScanRootRegion , CMS:ParNew (y-t h-) ParNew (y-t h-) InitialMark (y t h ) , G1:G1SystemGC (e-s t h-) G1SystemGC (e s t h ) G1SystemGC (e s t h ) , G1:G1Cleanup (e s t h ) ConcurrentCleanupForNextMark G1Young (e-s+t-h-) , G1:G1SystemGC (e-s t+h ) G1SystemGC (e s t h ) G1SystemGC (e s t h ) , CMS:AbortablePreClean ParNew (y-t+h-) ParNew (y-t+h-) , CMS:ConcurrentSweep ParNew (y-t+h-) ParNew (y-t h-) , PARALLEL:PSFullGC (y t h ) PSYoungGen (y-t h-) PSFullGC (y-t h-) , PARALLEL:PSFullGC (y-t+h-) PSYoungGen (y t h ) PSFullGC (y t h ) , G1:G1Young (e-s+t h ) G1SystemGC (e-s-t-h-) G1SystemGC (e s t h ) , CMS:InitialMark (y t h ) ConcurrentMark ConcurrentMark , G1:G1Young (e-s t-h-) G1Young (e-s t+h ) G1Young (e-s t+h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h ) G1Young (e-s t+h-) , CMS:FullGC (y-t+h-) FullGC (y-t+h-) FullGC (y t h ) , CMS:ParNew (y-t h ) ParNew (y-t h ) ParNew (y-t h ) , CMS:ParNew (y-t+h-) ParNew (y-t h ) ParNew (y-t h ) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ParNew (y-t+h-) , PARALLEL:PSFullGC (y-t-h-) PSYoungGen (y-t h-) PSYoungGen (y t h ) , G1:G1Young (e-s t-h-) G1Young (e-s t+h-) G1Young (e-s-t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t+h-) G1Young (e-s-t-h-) , PARALLEL:PSYoungGen (y-t h-) PSFullGC (y-t h ) PSYoungGen (y-t h-) , SERIAL:FullGC (y t h ) FullGC (y-t-h-) FullGC (y t h ) , CMS:FullGC (y-t h-) FullGC (y-t h-) FullGC (y-t+h-) , PARALLEL:PSYoungGen (y-t+h-) PSYoungGen (y t+h ) PSFullGC (y-t-h-) , SERIAL:FullGC (y-t+h-) FullGC (y-t+h-) FullGC (y t h ) , CMS:ParNew (y-t+h-) ParNew (y-t+h-) ParNew (y-t h ) , PARALLEL:PSFullGC (y-t h-) PSYoungGen (y t h ) PSFullGC (y t h ) , G1:G1ConcurrentMark G1Remark (e s t h ) G1ConcurrentRebuildRememberedSets , CMS:ParNew (y t h ) ParNew (y t h ) InitialMark (y t h ) , CMS:ParNew (y-t h-) InitialMark (y t h ) InitialMark (y t h ) , CMS:ConcurrentSweep ConcurrentSweep ParNew (y-t+h-) , CMS:ParNew (y-t+h-) ConcurrentReset ConcurrentReset , CMS:ConcurrentPreClean AbortablePreClean AbortablePreClean , SERIAL:FullGC (y-t+h-) FullGC (y-t-h-) FullGC (y t h ) , CMS:ConcurrentReset ConcurrentMark ConcurrentPreClean , G1:G1Young (e-s t-h-) G1Young (e-s t-h-) G1Young (e-s t-h-) , G1:G1Young (e-s t+h-) G1Young (e-s t-h-) G1Young (e-s t+h ) , G1:G1Young (e-s t+h ) G1Young (e-s t+h-) G1Young (e-s t+h-) , CMS:AbortablePreClean CMSRemark (y t h ) ConcurrentSweep , CMS:ParNew (y-t+h-) ParNew (y-t+h-) InitialMark (y t h ) , CMS:ParNew (y-t+h-) ParNew (y-t h-) ConcurrentReset , CMS:ParNew (y-t+h-) ParNew (y-t+h-) AbortablePreClean , G1:G1Young (e-s t-h-) G1Young (e-s t+h-) G1Young (e-s t h-) , PARALLEL:PSYoungGen (y-t+h-) PSFullGC (y t-h ) PSYoungGen (y-t+h-) , G1:G1SystemGC (e-s t+h-) G1SystemGC (e s t h ) G1SystemGC (e-s t h-) , CMS:ConcurrentReset ConcurrentModeFailure (y-t+h-) ConcurrentModeFailure (y-t+h-)";
        Set<String> eventSet2 = Arrays.stream(randomCov2.split(",")).map(ngram -> ngram.trim().replaceAll("\\(.*?\\)", "")).collect(Collectors.toSet());
        System.out.println(eventSet2.size());
        System.out.println(eventSet2);
    }

    @Test
    public void testOptions() throws IOException {
        Generator generator = new Generator("D:\\projects\\GCGenerator\\GCFuzzing\\vmoptions\\hotspot_option.json", "D:\\projects\\GCGenerator\\GCFuzzing\\vmoptions\\openj9_option.json", "D:\\projects\\GCGenerator\\GCFuzzing\\vmoptions\\common_option.json");
        List<OptionBean> hotspotOptions = generator.getOPENJ9_OPTIONS();
        List<String> optionNames = hotspotOptions.stream().map(o -> o.getName().split("/")).flatMap(Arrays::stream).collect(Collectors.toList());

        String srcPath = "D:\\projects\\GCGenerator\\GCFuzzing\\02Benchmarks\\Openj9Test-Test\\src";
        File root = new File(srcPath);
        HashMap<String, Integer> option2Count = new HashMap<>();

        OptionParse.filterFilesInFolder(root, ".java", option2Count);
        for (Map.Entry<String, Integer> stringIntegerEntry : option2Count.entrySet()) {
            System.out.println(String.format("%s\t%d", stringIntegerEntry.getKey(), stringIntegerEntry.getValue()));
        }
//        List<Map.Entry<String, Integer>>
        Map<String, Integer> option2Frequency = option2Count.entrySet()
                .stream()
                .filter(stringIntegerEntry -> optionNames.contains(stringIntegerEntry.getKey()))
                .filter(o -> o.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry<String, Integer>::getKey, Map.Entry<String, Integer>::getValue));
        System.out.println(option2Frequency);

        List<OptionBean> newHotspotOptions = hotspotOptions.stream()
                .peek(optionBean -> {
                    String[] enums = optionBean.getName().split("/");
                    int priority = 1;
                    for (String anEnum : enums) {
                        if (option2Frequency.containsKey(anEnum)) {
                            priority += option2Frequency.get(anEnum);
                        }
                    }
                    optionBean.setPriority(priority);

                })
                .collect(Collectors.toList());
        System.out.println(newHotspotOptions);
        Gson gson = new Gson();
        gson.toJson(newHotspotOptions, new FileWriter("D:\\projects\\GCGenerator\\GCFuzzing\\vmoptions\\openj9_option_priority.json"));
    }

    @Test
    public void extractCrashFileName() {
        String message = " VerifyDuringGC:(full)[Verifying Roots HeapRegions RemSet StrDedup #\n" +
                "# A fatal error has been detected by the Java Runtime Environment:\n" +
                "#\n" +
                "#  Internal Error (g1StringDedupQueue.cpp:164), pid=222, tid=0x00007fe68f31b700\n" +
                "#  guarantee(!obj->is_forwarded()) failed: Object must not be forwarded\n" +
                "#\n" +
                "# JRE version: Java(TM) SE Runtime Environment (8.0_371) (build 1.8.0_371-b11)\n" +
                "# Java VM: Java HotSpot(TM) 64-Bit Server VM (25.371-b11 mixed mode linux-amd64 compressed oops)\n" +
                "# Core dump written. Default location: /home/zhengkai/jdk/boot/core or core.222\n" +
                "#\n" +
                "# An error report file with more information is saved as:\n" +
                "# /home/zhengkai/jdk/boot/hs_err_pid222.log\n" +
                "#\n" +
                "# If you would like to submit a bug report, please visit:\n" +
                "#   http://bugreport.java.com/bugreport/crash.jsp\n" +
                "#\n" +
                "Aborted (core dumped)";
        Pattern pattern = Pattern.compile("hs_err_pid(\\d+)\\.log");
        Matcher matcher = pattern.matcher(message);

        // 如果找到匹配项，则提取文件名和数字后缀
        if (matcher.find()) {
            String fileName = matcher.group(0);
            String pidSuffix = matcher.group(1);
            System.out.println("文件名: " + fileName);
            System.out.println("PID后缀数字: " + pidSuffix);
        } else {
            System.out.println("未找到匹配项");
        }
    }

    @Test
    public void extractCrashFileName2() {
        System.out.println(Math.log(1 + Math.exp(1 - 35)));
//        String inputText = "JVMDUMP039I Processing dump event \"systhrow\", detail \"java/lang/OutOfMemoryError\" at 2023/07/29 08:22:48 - please wait.\n" +
//                "JVMDUMP032I JVM requested System dump using '/home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/core.20230729.082248.21106.0001.dmp' in response to an event\n" +
//                "JVMPORT030W /proc/sys/kernel/core_pattern setting \"|/usr/share/apport/apport -p%p -s%s -c%c -d%d -P%P -u%u -g%g -- %E\" specifies that the core dump is to be piped to an external program.  Attempting to rename either core or core.21377.\n" +
//                "\n" +
//                "JVMDUMP012E Error in System dump: The core file created by child process with pid = 21377 was not found. Expected to find core file with name \"/home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/core\"\n" +
//                "JVMDUMP032I JVM requested Heap dump using '/home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/heapdump.20230729.082248.21106.0002.phd' in response to an event\n" +
//                "JVMDUMP010I Heap dump written to /home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/heapdump.20230729.082248.21106.0002.phd\n" +
//                "JVMDUMP032I JVM requested Java dump using '/home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/javacore.20230729.082248.21106.0003.txt' in response to an event\n" +
//                "JVMDUMP010I Java dump written to /home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/javacore.20230729.082248.21106.0003.txt\n" +
//                "JVMDUMP032I JVM requested Snap dump using '/home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/Snap.20230729.082248.21106.0004.trc' in response to an event\n" +
//                "JVMDUMP010I Snap dump written to /home/zhengkai/fuzzer/sootOutput/Openj9Test-Test/out/production/Openj9Test-Test/Snap.20230729.082248.21106.0004.trc\n" +
//                "JVMDUMP013I Processed dump event \"systhrow\", detail \"java/lang/OutOfMemoryError\".";
//        // 使用正则表达式匹配 javacore 文件名及其中间的数字和小数点
//        Pattern pattern = Pattern.compile("javacore\\.(\\d+\\.)+txt");
//        Matcher matcher = pattern.matcher(inputText);
//
//        // 如果找到匹配项，则提取文件名及其中间的数字和小数点
//        if (matcher.find()) {
//            String fileName =  matcher.group(0);
//            System.out.println("提取的文件名: " + fileName);
//        } else {
//            System.out.println("未找到匹配项");
//        }
    }
}
