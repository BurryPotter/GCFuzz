//open module DTJVM {
//    requires com.microsoft.gctoolkit.api;
//    requires org.apache.commons.lang3;
//    requires java.management;
//    requires org.json;
//    requires com.google.gson;
//    requires junit;
//    requires org.junit.jupiter.api;
//    requires java.xml;
//    requires java.logging;
//
//    exports dtjvms.executor.GC.tool;
//    exports dtjvms.executor.GC.tool.analyzer;
//    exports dtjvms.executor.GC.tool.aggregation;
//    exports dtjvms;
//    exports dtjvms.executor.GC;
//    exports dtjvms.fjvms;
//    exports dtjvms.fjvms.config;
//    exports dtjvms.loader;
//    exports dtjvms.executor;
//    exports dtjvms.executor.JIT;
//    provides com.microsoft.gctoolkit.aggregator.Aggregation with
//            dtjvms.executor.GC.tool.aggregation.GCEventCollector;
//}