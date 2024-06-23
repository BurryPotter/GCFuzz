package main;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operators.AllOperator;
import codegen.operators.ControlOperator;
import codegen.operators.Operator;
import codegen.providers.ElementsProvider;
import codegen.providers.StaticMethodProvider;
import codegen.providers.TypeProvider;
import codegen.utils.SootHelper;
import config.FuzzingConfig;
import config.GCConfiguration;
import core.SeedInfo;
import dtjvms.*;
import dtjvms.executor.GC.*;
import dtjvms.executor.GC.tool.GCLogStats;
import dtjvms.fjvms.config.ConfigBean;
import dtjvms.fjvms.config.ConfigJTRegBean;
import dtjvms.loader.DTLoader;
import optiongen.bean.GCOption;
import optiongen.generator.Generator;
import optiongen.generator.VMType;
import optiongen.generator.WeightedRandom;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import utils.ClassUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestMain {

    public static List<SeedInfo> seeds;
    //        TODO coverage: RERUN = false. seed="HotspotTests"
//    public static final boolean RERUN = false;
    public static final String PROJECT_NAME = "sunflow-0.07.2";
    //        Status.projectName = "HotspotTests-Java"; Openj9Test-Test GCTestCases  eclipse-dacapo fop-dacapo
    public static final String GCOBJ_FILE_PATH = "./GCObj.class";

    private static int classIterate = 0;
    private static int optionIterate = 0;
    private static ArrayList<JvmInfo> jvmCmds;
    private static ProjectInfo targetProject;

    private static final int UNIQUE_TIMEOUT = 60 * 60 * 24; // hours
    private static boolean COVERAGE = false;
    private static final boolean NO_FEEDBACK = true;
    private static boolean NO_OPTION_FEEDBACK = false; // random = true ; edge = false
    private static final boolean NO_OPTION = true;
    private static final boolean NO_MUTANT = false;
    private static final GCOption<GCType> HOTSPOT_GC = new GCOption<>(HotspotGCType.G1);
    private static final GCOption<GCType> OPENJ9_GC = new GCOption<>(OpenJ9GCType.BALANCED);

    private static boolean COV_INCREASE_PUTBACK = true; // random = false ; edge = true
    private static boolean GC_RANDOM = true; // random = true ; edge = true

    private static JvmInfo covJVM = null;
    private static boolean EMPTY_TEMPLATE = false;

    public static void main(String[] args) throws IOException {
//        TODO coverage: fixed random seed
        Status.initialSeed = (int) System.currentTimeMillis();
        Status.projectName = PROJECT_NAME;
        DTGlobal.useVMOptions = Status.useVMOptions;
        Status.argsParser(args);
        init();

        DTGlobal.setDiffLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "diff");
        DTGlobal.setDataLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "status");
        DTGlobal.setRecordLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "record");
//        DTGlobal.setPerformLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "perf");
        DTGlobal.setClassPriorityLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "class_weight");
        DTGlobal.setOptionPriorityLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "option_weight");
        DTGlobal.setRandomLogger(Status.timeStamp + DTPlatform.FILE_SEPARATOR + Status.projectName, "random");

        DTGlobal.setGcLogPath(System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "03results" +
                DTPlatform.FILE_SEPARATOR + Status.timeStamp +
                DTPlatform.FILE_SEPARATOR + "gcLogs");

        DTGlobal.setShowmapTmpPath(System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "03results" +
                DTPlatform.FILE_SEPARATOR + Status.timeStamp +
                DTPlatform.FILE_SEPARATOR + "tmp.txt");

        Status.mutationHistoryPath = "." + DTPlatform.FILE_SEPARATOR + "03results" +
                DTPlatform.FILE_SEPARATOR + Status.timeStamp +
                DTPlatform.FILE_SEPARATOR + "classHistory";
        Status.diffClassPath = "." + DTPlatform.FILE_SEPARATOR + "03results" +
                DTPlatform.FILE_SEPARATOR + Status.timeStamp +
                DTPlatform.FILE_SEPARATOR + "diffClasses";
        DTConfiguration.setJvmDepensRoot(Status.jvmRootPath);
        DTConfiguration.setProjectDepensRoot(Status.projRootPath);
        Status.globalStatus();


        /**
         * Testing platform
         */
        System.out.println(DTPlatform.getInstance());
        // load JVMs
        jvmCmds = DTLoader.getInstance().loadJvms();
        // load projects
        ProjectInfo originProject = DTLoader.getInstance().loadTargetProjectWithGivenPath("02Benchmarks", Status.projectName, null, Status.usePredefinedClass);
        targetProject = DTLoader.getInstance().loadTargetProjectWithGivenPath("sootOutput", Status.projectName, null, Status.usePredefinedClass);
        List<String> seedClasses = originProject.getApplicationClasses();
        MainHelper.restoreBadClasses(seedClasses, originProject, targetProject);
        targetProject.setApplicationClasses(originProject.getApplicationClasses());
        seeds = MainHelper.initialSeedsWithType(originProject.getApplicationClasses(), targetProject.getSrcClassPath(), false, Status.mutationHistoryPath);
        seeds = seeds.stream().sorted(Comparator.comparing(SeedInfo::getClassName)).collect(Collectors.toList());

        for (SeedInfo seed : seeds) {
            System.out.println(seed.getClassName());
        }
//        if (ONLY_ONE_SEED) {
//            SeedInfo seedInfo = seeds.get(FuzzingConfig.nextChoice(seeds.size()));
//            seeds.clear();
//            seeds.add(seedInfo);
//        }
        /**
         * JVM
         */
        for (JvmInfo jvmCmd : jvmCmds) {
            System.out.println(jvmCmd);
        }
        /**
         * Project
         */
        System.out.println(originProject);
        System.out.println(targetProject);

        ClassUtils.initSootEnvWithClassPath(targetProject.getpClassPath());
        ClassUtils.set_output_path(targetProject.getSrcClassPath());

        FuzzingConfig.setRandomSeed(Status.initialSeed);


        TypeProvider.loadRefTypes();
        StaticMethodProvider.loadStaticMethods();
        Generator vmOptionGenerator = new Generator();


        MainHelper.copyToFile(GCOBJ_FILE_PATH, targetProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + GCOBJ_FILE_PATH);

        List<VMType> supportVMType = new ArrayList<>();
        if (DTConfiguration.TARGET_JVMS.contains("hotspot") || DTConfiguration.TARGET_JVMS.contains("bisheng")) {
            supportVMType.add(VMType.HOTSPOT);
        }
        if (DTConfiguration.TARGET_JVMS.contains("openj9")) {
            supportVMType.add(VMType.J9);
        }

//        for (VMType vmType : supportVMType) {
//            List<GCOption<GCType>> list = vmOptionGenerator.getSupportGCTypes(vmType);
//            for (SeedInfo seed : seeds) {
//                HashMap<GCType, Double> gcEventCov = new HashMap<>();
//                for (GCOption<GCType> gcTypeGCOption : list) {
//                    gcEventCov.put(gcTypeGCOption.getGcType(), 0d);
//                }
//                seed.setGcEventCov(gcEventCov);
//            }
//        }

        List<Double> seedWeight = new ArrayList<>(seeds.size());
        for (SeedInfo seedInfo : seeds) {
            seedWeight.add(seedInfo.calcPriority());
        }
        boolean regenOption = true;
        long startTime = System.currentTimeMillis();
        Set<String> gcCovTotal = new HashSet<>();
        Set<String> logEventCovTotal = new HashSet<>();
        Set<String> logEdgeCovTotal = new HashSet<>();

        int oldTotalCov = 0;
        boolean eventCovIncrease = true;
        long lastIncreaseTime = -1;
        int position;
        while (true) {

            long currentTime = System.currentTimeMillis();
            int time = (int) (currentTime - startTime) / 1000;
            if ((DTConfiguration.FIND_UNIQUE_BUG || COVERAGE) && time > UNIQUE_TIMEOUT) {
                System.out.println("timeout");
                break;
            }

            if (seeds.isEmpty()) {
                System.out.println("GCMain: Seed is empty");
                break;
            }
            try {

                if (NO_FEEDBACK) {
                    position = FuzzingConfig.nextChoice(seeds.size(), FuzzingConfig.RandomType.SEED_POOL);
                } else {
                    position = WeightedRandom.pick(seedWeight);
                }

                SeedInfo seed = seeds.get(position);
                String classFileFolder = Status.mutationHistoryPath + DTPlatform.FILE_SEPARATOR + seed.getOriginClassName();
                ClassInfo clazz = null;

                if (seed.isOriginClass() && !seed.hasCovered()) {
                    clazz = ClassUtils.loadClassInfo(seed.getClassName());
                } else {
                    seed.storeToCoverOriginClass();
                    clazz = ClassUtils.loadClassInfo(seed.getOriginClassName());
                }

                if (clazz == null || seed.getMutationTimes() > GCConfiguration.MAX_MUTATE_TIME || seed.getMutationOrder() > GCConfiguration.MAX_MUTATE_ORDER) {
                    seeds.remove(position);
                    if (!NO_FEEDBACK) {
                        seedWeight.remove(position);
                    }
                    MainHelper.logSeedInfo("<rmv>", seed.getPriority(), seed);
                    continue;
                }

//                Scene.v().loadClass("compiler.types.TestPhiElimination$A", SootClass.BODIES);
                List<SootMethod> sootMethods = clazz.getSootClass().getMethods();

                try {
                    for (SootMethod sootMethod : sootMethods) {
                        if (!sootMethod.isAbstract()) {
                            sootMethod.retrieveActiveBody();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("GCMain Exception Occurred when retrieveActiveBody");
                    e.printStackTrace(System.err);
                    SeedInfo seedInfo = seeds.get(position);
                    seeds.remove(position);
                    if (!NO_FEEDBACK) {
                        seedWeight.remove(position);
                    }
                    MainHelper.logSeedInfo("<soot error rmv>", seedInfo.getPriority(), seedInfo);
                    continue;
                }

                List<SootMethod> candidates;
                if (Status.projectName.equals("Templates")) {
                    candidates = sootMethods.stream().filter(m -> m.getName().contains("opt")).collect(Collectors.toList());
                } else {
                    candidates = sootMethods.stream().filter(m -> !m.isStaticInitializer() && !m.isConstructor() && !m.isAbstract()).collect(Collectors.toList());
                }
                if (candidates.size() == 0) {
                    SeedInfo seedInfo = seeds.get(position);
                    seeds.remove(position);
                    if (!NO_FEEDBACK) {
                        seedWeight.remove(position);
                    }
                    MainHelper.logSeedInfo("<no method rmv>", seedInfo.getPriority(), seedInfo);
                    continue;
                }
                // update seed after check seed.
                Status.currentSeed = FuzzingConfig.nextControlRandom();
                FuzzingConfig.updateSeed(Status.currentSeed);
//                TODO coverage: remove fuzzing
                if (!NO_MUTANT) {
                    try {
                        for (SootMethod method : candidates) {
                            if (method.isMain()) {
                                fuzzing(clazz, method);
                                candidates.remove(method);
                                break;
                            }
                        }
                        if (candidates.size() > 0) {
                            SootMethod randomMethod = candidates.get(FuzzingConfig.nextChoice(candidates.size(), FuzzingConfig.RandomType.SEED_POOL));
                            fuzzing(clazz, randomMethod);
                        }
                    } catch (Exception e) {
                        System.err.println("GCMain Exception Occurred when fuzzing");
                        e.printStackTrace(System.err);
                    }
                }


                if (ClassUtils.saveClass(clazz.getSootClass(), Options.output_format_class)) {

                    seed.mutationTimesIncrease();
                    SeedInfo newSeed = new SeedInfo(seed.getOriginClassName(),
                            seed.getOriginClassPath(),
                            seed.generateMutateClassFilename(),
                            classFileFolder,
                            seed.isJunit(),
                            seed.getMutationOrder() + 1,
                            0,
                            0,
                            0,
//                            seed.calcScore() * 0.5,
                            seed.getGcEventCov());

                    //save to history
                    newSeed.saveSootClassToFile(clazz.getSootClass());

                    String className = "";
                    String targetClassName = clazz.getClassName();
                    if (targetClassName.contains(".")) {
                        className = targetClassName.substring(targetClassName.lastIndexOf(".") + 1);
                    } else {
                        className = targetClassName;
                    }

                    classIterate++;
                    seed.setLastSelect(classIterate);
                    boolean disCard = true;
                    oldTotalCov = gcCovTotal.size();
                    DiffResult noOptionResult = null;
                    for (VMType vmType : supportVMType) {
                        GCType gcType = vmOptionGenerator.generateGCOption(vmType, GC_RANDOM).getGcType();
                        int optionNumber = 0;
                        optionNumber = GCConfiguration.OPTION_NUMBER;
                        optionIterate++;
                        ConfigJTRegBean config = generateConfig(vmOptionGenerator, vmType, optionNumber, targetClassName, className);
                        String option = execute(targetClassName, config);
                        if (covJVM != null) {
                            GCExecutor.getInstance().codeCoverage(covJVM, config, targetProject);
                        }
                        DTGlobal.getClassPriorityLogger().info(String.format("<CR:% 5d,OR:% 5d>", classIterate, optionIterate));
                        // update new seed program Cov
                        HashMap<String, GCLogStats> seedGCLogStatsHashMap = GCExecutor.getInstance().getGcLogStatsHashMap();
                        newSeed.updateGCEventCov(gcType, seedGCLogStatsHashMap, gcCovTotal); // j9 gc cov = 0. forever.
                        for (GCLogStats value : seedGCLogStatsHashMap.values()) {
                            if (DTConfiguration.EDGE_COV) {
                                if (value != null && value.getEdgeCov() != null) {
                                    gcCovTotal.addAll(value.getEdgeCov());
                                }
                            } else {
                                if (value != null && value.getGcEventNGram() != null) {
                                    gcCovTotal.addAll(value.getGcEventNGram());
                                }
                            }
                        }
                        for (GCLogStats value : seedGCLogStatsHashMap.values()) {
                            if (value != null && value.getEdgeCov() != null) {
                                logEdgeCovTotal.addAll(value.getEdgeCov());
                            }
                            if (value != null && value.getGcEventNGram() != null) {
                                logEventCovTotal.addAll(value.getGcEventNGram());
                            }
                        }
                        MainHelper.logResult(newSeed, seed, clazz, option, gcType, gcCovTotal.size(), classIterate, optionIterate);
                        MainHelper.logResult(newSeed, seed, clazz, option, gcType, logEventCovTotal.size(), logEdgeCovTotal.size(), classIterate, optionIterate);

                        if (!NO_FEEDBACK) {
                            MainHelper.classFeedBack(seed);
                            MainHelper.classFeedBack(newSeed);
                        }
                        if (!NO_OPTION_FEEDBACK) {
                            MainHelper.optionFeedBack(GC_RANDOM, vmOptionGenerator, vmType, noOptionResult, seedGCLogStatsHashMap, optionIterate);
                        }

                        double oldPrio = seed.getPriority();
                        double newPrio = seed.calcPriority();
                        if (!NO_FEEDBACK) {
                            seedWeight.set(position, newPrio);
                        }
//                    DTGlobal.getClassPriorityLogger().info(String.format("<updt>%5.2f->%-5.2f C:%s", oldPrio, newPrio, seed.getClassName()));
                        MainHelper.logSeedInfo("<updt>", oldPrio, seed);
                        noOptionResult = new DiffResult(GCExecutor.getInstance());
                        disCard = disCard && GCExecutor.getInstance().isDisCard();
                    }

                    // putback new seed
                    if (!disCard) {
                        boolean covIncrease = true;
                        if (COV_INCREASE_PUTBACK) {
                            covIncrease = gcCovTotal.size() > oldTotalCov;
                        }
                        boolean emptyProjectPutBack = false;
                        if (classIterate < 100 && EMPTY_TEMPLATE) {
                            emptyProjectPutBack = true;
                        }
                        // add seed, if cov increases
                        if (!NO_MUTANT && covIncrease || emptyProjectPutBack) {
                            seeds.add(newSeed);
                            if (!NO_FEEDBACK) {
                                seedWeight.add(newSeed.calcPriority());
                            }
                            double oldPrio = newSeed.getPriority();
                            //                        DTGlobal.getClassPriorityLogger().info(String.format("<add>%5.2f->%-5.2f C:%s", oldPrio, newPrio, newSeed.getClassName()));
                            MainHelper.logSeedInfo("<add>", oldPrio, newSeed);
                        }
                    }
//                    }
                } else {
                    Status.updateStatus(seed.getClassName(), "GenerationFailed", "GenerationFailed");
                }
                for (SootMethod method : clazz.getSootClass().getMethods()) {
                    method.releaseActiveBody();
                }
                Scene.v().removeClass(clazz.getSootClass());

            } catch (Throwable e) {
                System.err.println("GCMain Exception Occurred");
                e.printStackTrace(System.err);
                if (e instanceof OutOfMemoryError) {
                    throw e;
                }

            }
        }
        System.out.println("Total Event Cov:");
        System.out.println(gcCovTotal);

    }

    public static ConfigJTRegBean generateConfig(Generator vmOptionGenerator, VMType vmType, int optionNumber, String targetClassName, String className) {
        String option = "-XX:+UseG1GC -XX:+UseStringDeduplication -Xms512m -Xmx4096m";
        //TODO without option coverage
//        option = "";
        GCType gcType = HotspotGCType.G1;
        String parameters = "";
        if (Status.projectName.contains("sunflow-0.07.2")) {
            parameters = "-bench 2 256";

        } else if (Status.projectName.contains("pmd-4.2.5")) {
            parameters = "Hello.java text unusedcode";

        } else if (Status.projectName.contains("jython-dacapo")) {
            parameters = "-S hello.py";

        } else if (Status.projectName.contains("fop-dacapo")) {
            parameters = "-xml name.xml -xsl name2fo.xsl -pdf name.pdf";

        } else if (Status.projectName.contains("eclipse-dacapo")) {
            parameters = "-debug";

        } else if (Status.projectName.contains("avrora-cvs-20091224")) {
            parameters = "-action=cfg example.asm";
        }
        ConfigJTRegBean configBean = new ConfigJTRegBean(targetClassName, option, className, parameters, gcType);

        return configBean;
    }

    public static String execute(String targetClassName, ConfigJTRegBean config) {
        ArrayList<ConfigBean> arrayList = new ArrayList<>();
        arrayList.add(config.getBean());
        HashMap<String, ArrayList<ConfigBean>> configBeans = new HashMap<>();
        configBeans.put(targetClassName, arrayList);
        //successful saved
        DTGlobal.getOptionPriorityLogger().info(String.format("<CR:% 5d,OR:% 5d>", classIterate, optionIterate));
        GCExecutor.getInstance().dtSingleClassInProj(jvmCmds, targetProject, targetClassName, configBeans, config.getGcType(), classIterate, optionIterate);
        return config.getBean().getOptions();
    }


    public static void fuzzing(ClassInfo clazz, SootMethod sootMethod) {
        // remove special invoke before fuzzing
        boolean isInit = sootMethod.isConstructor();
        Value instance = null;
        Unit identity = null;
        Unit specialInvoke = null;
        UnitPatchingChain units = sootMethod.retrieveActiveBody().getUnits();
        if (isInit) {
            instance = SootHelper.findInstance(clazz.getSootClass(), sootMethod, false);
            for (Unit unit : units) {
                if (unit instanceof InvokeStmt) {
                    if (unit.getUseBoxes().get(0).getValue().equals(instance)
                            && unit.getUseBoxes().get(1).getValue() instanceof SpecialInvokeExpr) {
                        specialInvoke = unit;
                        break;
                    }
                }
            }
            if (specialInvoke != null) {
                units.remove(specialInvoke);
            }
        }

        // fuzzing
        for (int i = 0; i < Status.fuzzingRound; ) {

            Operator operator = AllOperator.getInstance().operators.get(FuzzingConfig.nextChoice(AllOperator.getInstance().operators.size(), FuzzingConfig.RandomType.OPERATOR_POOL));
            List<Stmt> targets;
            if (operator instanceof ControlOperator) {
                targets = ElementsProvider.getTargets(clazz, sootMethod.getSignature(), 1);
            } else {
                targets = ElementsProvider.getTargets(clazz, sootMethod.getSignature(), 2);
            }
            BasicBlock block = operator.nextBlock(clazz, sootMethod.getSignature(), targets);
            if (blockInsertion(clazz, sootMethod, block, true)) {
                i++;
            }
        }
        // restore special invoke after fuzzing
        if (isInit) {
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

    public static boolean blockInsertion(ClassInfo clazz, SootMethod sootMethod, BasicBlock block, boolean nested) {

        if (block == null) return false;
        boolean success = block.insertBlock(clazz, sootMethod);
//        System.out.println("Block: " + block.getClass().getName() + " " + success);
        return success;
    }

    private static void init() {
        if (Status.methodMode.equals("random")) {
            NO_OPTION_FEEDBACK = true;
            COV_INCREASE_PUTBACK = false;
            GC_RANDOM = true;
        } else if (Status.methodMode.equals("edge")) {
            NO_OPTION_FEEDBACK = false;
            COV_INCREASE_PUTBACK = true;
            GC_RANDOM = true;
            if (!DTConfiguration.EDGE_COV) {
                throw new RuntimeException("To enable Edge_COV must set afl-showmap path.\nusing -afl option.");
            }
        } else {
            NO_OPTION_FEEDBACK = false;
            COV_INCREASE_PUTBACK = true;
            GC_RANDOM = false;
        }

        if ((DTConfiguration.FIND_UNIQUE_BUG || COVERAGE)) {
            System.out.println("run time limit=" + UNIQUE_TIMEOUT / 60 / 60 + " hours");
        }

        if (DTConfiguration.FIND_UNIQUE_BUG) {
            System.out.println("Enable finding unique bug");
            System.out.println("unique jdk version=" + DTConfiguration.UNIQUE_JDK_VERSION);
        }
        if (NO_FEEDBACK) {
            System.out.println("Enable no weighted seed selection.");
        }
        if (NO_OPTION_FEEDBACK) {
            System.out.println("Enable no weighted option selection.");
        }
        if (NO_MUTANT) {
            System.out.println("Not Enable mutant operators");
        }
        if (NO_OPTION) {
            System.out.println("Not Enable option explore");
            System.out.println("Hotspot GC: " + HOTSPOT_GC.getGcType());
            System.out.println("OpenJ9 GC: " + OPENJ9_GC.getGcType());
        }
        if (COV_INCREASE_PUTBACK) {
            System.out.println("event-coverage increase putback");
        } else {
            System.out.println("normal execute putback");
        }
        if (GC_RANDOM) {
            System.out.println("Random Option Explore");
        } else {
            System.out.println("Guide Option Explore");
        }
        if (DTConfiguration.EDGE_COV) {
            System.out.println("Use edge coverage");
        }
        if (Status.covJDK != null) {
            covJVM = new JvmInfo(Status.covJDK);
            COVERAGE = true;
        }
        if (COVERAGE) {
            System.out.println("collect jvm coverage");
        }
        if (Status.projectName.contains("sunflow-0.07.2") || Status.projectName.contains("pmd-4.2.5") || Status.projectName.contains("jython-dacapo") ||
                Status.projectName.contains("fop-dacapo") || Status.projectName.contains("eclipse-dacapo") || Status.projectName.contains("avrora-cvs-20091224")) {
            EMPTY_TEMPLATE = true; // these projects only have one seed. Add seeds at first 100 round
        }
        if (EMPTY_TEMPLATE) {
            System.out.println("use empty project");
        }
    }
}
