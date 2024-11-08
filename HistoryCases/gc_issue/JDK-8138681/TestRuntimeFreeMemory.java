/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */


package gc.g1.humongousObjects;

import jdk.test.lib.Asserts;
import sun.hotspot.WhiteBox;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;


/**
 * @test TestRuntimeFreeMemory
 * @summary Checks that heap counters work as expected during Humongous allocations
 * @requires vm.gc=="G1" | vm.gc=="null"
 * @library /testlibrary /../../test/lib
 * @modules java.management
 * @build sun.hotspot.WhiteBox
 *        gc.g1.humongousObjects.TestRuntimeFreeMemory
 * @run driver ClassFileInstaller sun.hotspot.WhiteBox
 *                                sun.hotspot.WhiteBox$WhiteBoxPermission
 *
 * @run main/othervm -XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:.
 * -XX:G1HeapRegionSize=1M
 * gc.g1.humongousObjects.TestRuntimeFreeMemory
 *
 */
public class TestRuntimeFreeMemory {
    private static final WhiteBox WHITE_BOX = WhiteBox.getWhiteBox();
    private static final int REGION_SIZE = WHITE_BOX.g1RegionSize();
    public static byte[] storage;

    public static void main(String[] args) {

        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

        long memoryUsedBeforeReportedByMBean =  mbean.getHeapMemoryUsage().getUsed();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Allocating humongous object ie object with size larger that REGION_SIZE / 2
        int allocationSize = REGION_SIZE * 2 / 3;
        System.out.format("Allocating humongous object with size = %d\n", allocationSize);
        storage = new byte[allocationSize];
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsedAfterReportedByMBean =  mbean.getHeapMemoryUsage().getUsed();


        long usedMemoryIncrease = usedMemoryAfter - usedMemoryBefore;

        System.out.println(String.format("Allocation of object with size %d takes a whole region. So used memory "
                        + "should increase on full region size %d %s it increased on %d", allocationSize, REGION_SIZE,
                (usedMemoryIncrease == REGION_SIZE ? "and" : "but"), usedMemoryIncrease));

        System.out.format("MemoryMXBean reported used memory increase %d which is %s\n",
                memoryUsedAfterReportedByMBean - memoryUsedBeforeReportedByMBean,
                ((memoryUsedAfterReportedByMBean - memoryUsedBeforeReportedByMBean) == REGION_SIZE ? "right" : "wrong"));

        Asserts.assertEquals(usedMemoryIncrease, REGION_SIZE, String.format("Used memory increased on %d but"
                + " it should increased on %d", usedMemoryIncrease, REGION_SIZE));

    }
}
