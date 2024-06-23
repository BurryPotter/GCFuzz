package dtjvms.executor.GC;

import dtjvms.DTPlatform;
import dtjvms.JvmInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GCHelper {
    public static List<GCType> supportGC(JvmInfo jvmInfo) {

        return supportGC(jvmInfo.getVersion(), jvmInfo.getJvmName());
    }

    public static List<GCType> supportGC(String jdkVersion, String jvmName) {
        // jdkVersion like openjdkxx e.g. openjdk11, openjdk8
        int version = 0;
        int index = jdkVersion.length() - 1;
        for (; index >= 0; index--) {
            char c = jdkVersion.charAt(index);
            if (!Character.isDigit(c)) {
                break;
            }
        }
        version = Integer.parseInt(jdkVersion.substring(index + 1));
        return supportGC(version, jvmName);
    }

    public static List<GCType> supportGC(int version, String jvmName) {
        List<GCType> list = new ArrayList<>();
        if (jvmName.equalsIgnoreCase("hotspot")||jvmName.equalsIgnoreCase("bisheng")){
            list.add(HotspotGCType.Parallel);
            list.add(HotspotGCType.G1);
            list.add(HotspotGCType.SERIAL);
            if (6 <= version && version < 14) {
                list.add(HotspotGCType.CMS);
            }
            int ZGCVersion = DTPlatform.isWin() ? 14 : 11;
            if (version >= ZGCVersion) {
                list.add(HotspotGCType.Z);
            }
        }else if(jvmName.equalsIgnoreCase("openj9")){
            list = Arrays.asList(OpenJ9GCType.values());
        }

        return list;
    }

    public static String gc2option(GCType type) {
        if (type instanceof HotspotGCType) {
            switch ((HotspotGCType) type) {
                case Parallel:
                    return "-XX:+UseParallelGC";
                case G1:
                    return "-XX:+UseG1GC";
                case CMS:
                    return "-XX:+UseConcMarkSweepGC";
                case Z:
                    return "-XX:+UseZGC";
                case SERIAL:
                    return "-XX:+UseSerialGC";
            }
        }else if (type instanceof OpenJ9GCType){
            switch ((OpenJ9GCType) type) {
                case GENCON:
                    return "-Xgcpolicy:gencon";
                case BALANCED:
                    return "-Xgcpolicy:balanced";
                case OPT_AVG_PAUSE:
                    return "-Xgcpolicy:optavgpause";
                case OPT_THRUPUT:
                    return "-Xgcpolicy:optthruput";
            }
        }

        throw new RuntimeException("[Should not reach here] Not support GCType: " + type);
    }
}
