// To stress large non-array object dirty-card scanning.
//
// H=64g ; T=16 ; java -Xms${H} -Xmx${H} -XX:+UseParallelGC -XX:ParallelGCThreads=$T -Xlog:gc=trace card_scan_big_instances 24
// H=128g; T=16 ; java -Xms${H} -Xmx${H} -XX:+UseParallelGC -XX:ParallelGCThreads=$T -Xlog:gc=trace card_scan_big_instances 48
//

class card_scan_big_instances {
    static BigInstance[] bigInstances;

    public static final int  K = 1024;
    public static final int  M = 1024*K;
    public static final long G = 1024*M;
    public static final int LONG_SIZE_BYTES = 8;
    public static final int BIG_INSTANCE_SIZE_BYTES = 4096 * LONG_SIZE_BYTES;

    static byte[] garbage;

    static final int loop_count = 20;

    public static void main(String[] args) {
        int bigInstancesTotalSizeInGigabytes = Integer.parseInt(args[0]);
        long bigInstancesTotalSizeInBytes = bigInstancesTotalSizeInGigabytes * G;
        int bigInstancesCount = (int)(bigInstancesTotalSizeInBytes / BIG_INSTANCE_SIZE_BYTES);
        System.out.println("BIG_INSTANCE_SIZE_BYTES:" + BIG_INSTANCE_SIZE_BYTES + " (" + BIG_INSTANCE_SIZE_BYTES/K + "K)");
        System.out.println("bigInstancesCount:" + bigInstancesCount);
        bigInstances = new BigInstance[bigInstancesCount];
        for (int k = 0; k < bigInstancesCount; k++) {
            bigInstances[k] = new BigInstance();
        }
        System.out.println("### System.gc"); // get everything promoted
        System.gc();
        System.out.println("### System.gc done");
        for (int i = 0; i < loop_count; i++) {
            Object tmp = new Object();
            for (int j = 0; j < bigInstances.length; j++) {
                bigInstances[j].objOnOtherStripe = tmp;
            }
            int garbage_loop = bigInstancesCount * 4;
            for (int j = 0; j < garbage_loop; ++j) {
                garbage = new byte[1*K];
            }
        }
    }

    static class BigInstance {
        long l0,l1,l2,l3,l4,l5,l6,l7,l8,l9,l10,l11,l12,l13,l14,l15,l16,l17,l18,l19,l20,l21,l22,l23,l24,l25,l26,l27,l28,l29,l30,l31,l32,l33,l34,l35,l36,l37,l38,l39,l40,l41,l42,l43,l44,l45,l46,l47,l48,l49,l50,l51,l52,l53,l54,l55,l56,l57,l58,l59,l60,l61,l62,l63,l64,l65,l66,l67,l68,l69,l70,l71,l72,l73,l74,l75,l76,l77,l78,l79,l80,l81,l82,l83,l84,l85,l86,l87,l88,l89,l90,l91,l92,l93,l94,l95,l96,l97,l98,l99;
        long l100,l101,l102,l103,l104,l105,l106,l107,l108,l109,l110,l111,l112,l113,l114,l115,l116,l117,l118,l119,l120,l121,l122,l123,l124,l125,l126,l127,l128,l129,l130,l131,l132,l133,l134,l135,l136,l137,l138,l139,l140,l141,l142,l143,l144,l145,l146,l147,l148,l149,l150,l151,l152,l153,l154,l155,l156,l157,l158,l159,l160,l161,l162,l163,l164,l165,l166,l167,l168,l169,l170,l171,l172,l173,l174,l175,l176,l177,l178,l179,l180,l181,l182,l183,l184,l185,l186,l187,l188,l189,l190,l191,l192,l193,l194,l195,l196,l197,l198,l199;
        long l200,l201,l202,l203,l204,l205,l206,l207,l208,l209,l210,l211,l212,l213,l214,l215,l216,l217,l218,l219,l220,l221,l222,l223,l224,l225,l226,l227,l228,l229,l230,l231,l232,l233,l234,l235,l236,l237,l238,l239,l240,l241,l242,l243,l244,l245,l246,l247,l248,l249,l250,l251,l252,l253,l254,l255,l256,l257,l258,l259,l260,l261,l262,l263,l264,l265,l266,l267,l268,l269,l270,l271,l272,l273,l274,l275,l276,l277,l278,l279,l280,l281,l282,l283,l284,l285,l286,l287,l288,l289,l290,l291,l292,l293,l294,l295,l296,l297,l298,l299;
        long l300,l301,l302,l303,l304,l305,l306,l307,l308,l309,l310,l311,l312,l313,l314,l315,l316,l317,l318,l319,l320,l321,l322,l323,l324,l325,l326,l327,l328,l329,l330,l331,l332,l333,l334,l335,l336,l337,l338,l339,l340,l341,l342,l343,l344,l345,l346,l347,l348,l349,l350,l351,l352,l353,l354,l355,l356,l357,l358,l359,l360,l361,l362,l363,l364,l365,l366,l367,l368,l369,l370,l371,l372,l373,l374,l375,l376,l377,l378,l379,l380,l381,l382,l383,l384,l385,l386,l387,l388,l389,l390,l391,l392,l393,l394,l395,l396,l397,l398,l399;
        long l400,l401,l402,l403,l404,l405,l406,l407,l408,l409,l410,l411,l412,l413,l414,l415,l416,l417,l418,l419,l420,l421,l422,l423,l424,l425,l426,l427,l428,l429,l430,l431,l432,l433,l434,l435,l436,l437,l438,l439,l440,l441,l442,l443,l444,l445,l446,l447,l448,l449,l450,l451,l452,l453,l454,l455,l456,l457,l458,l459,l460,l461,l462,l463,l464,l465,l466,l467,l468,l469,l470,l471,l472,l473,l474,l475,l476,l477,l478,l479,l480,l481,l482,l483,l484,l485,l486,l487,l488,l489,l490,l491,l492,l493,l494,l495,l496,l497,l498,l499;
        long l500,l501,l502,l503,l504,l505,l506,l507,l508,l509,l510,l511,l512,l513,l514,l515,l516,l517,l518,l519,l520,l521,l522,l523,l524,l525,l526,l527,l528,l529,l530,l531,l532,l533,l534,l535,l536,l537,l538,l539,l540,l541,l542,l543,l544,l545,l546,l547,l548,l549,l550,l551,l552,l553,l554,l555,l556,l557,l558,l559,l560,l561,l562,l563,l564,l565,l566,l567,l568,l569,l570,l571,l572,l573,l574,l575,l576,l577,l578,l579,l580,l581,l582,l583,l584,l585,l586,l587,l588,l589,l590,l591,l592,l593,l594,l595,l596,l597,l598,l599;
        long l600,l601,l602,l603,l604,l605,l606,l607,l608,l609,l610,l611,l612,l613,l614,l615,l616,l617,l618,l619,l620,l621,l622,l623,l624,l625,l626,l627,l628,l629,l630,l631,l632,l633,l634,l635,l636,l637,l638,l639,l640,l641,l642,l643,l644,l645,l646,l647,l648,l649,l650,l651,l652,l653,l654,l655,l656,l657,l658,l659,l660,l661,l662,l663,l664,l665,l666,l667,l668,l669,l670,l671,l672,l673,l674,l675,l676,l677,l678,l679,l680,l681,l682,l683,l684,l685,l686,l687,l688,l689,l690,l691,l692,l693,l694,l695,l696,l697,l698,l699;
        long l700,l701,l702,l703,l704,l705,l706,l707,l708,l709,l710,l711,l712,l713,l714,l715,l716,l717,l718,l719,l720,l721,l722,l723,l724,l725,l726,l727,l728,l729,l730,l731,l732,l733,l734,l735,l736,l737,l738,l739,l740,l741,l742,l743,l744,l745,l746,l747,l748,l749,l750,l751,l752,l753,l754,l755,l756,l757,l758,l759,l760,l761,l762,l763,l764,l765,l766,l767,l768,l769,l770,l771,l772,l773,l774,l775,l776,l777,l778,l779,l780,l781,l782,l783,l784,l785,l786,l787,l788,l789,l790,l791,l792,l793,l794,l795,l796,l797,l798,l799;
        long l800,l801,l802,l803,l804,l805,l806,l807,l808,l809,l810,l811,l812,l813,l814,l815,l816,l817,l818,l819,l820,l821,l822,l823,l824,l825,l826,l827,l828,l829,l830,l831,l832,l833,l834,l835,l836,l837,l838,l839,l840,l841,l842,l843,l844,l845,l846,l847,l848,l849,l850,l851,l852,l853,l854,l855,l856,l857,l858,l859,l860,l861,l862,l863,l864,l865,l866,l867,l868,l869,l870,l871,l872,l873,l874,l875,l876,l877,l878,l879,l880,l881,l882,l883,l884,l885,l886,l887,l888,l889,l890,l891,l892,l893,l894,l895,l896,l897,l898,l899;
        long l900,l901,l902,l903,l904,l905,l906,l907,l908,l909,l910,l911,l912,l913,l914,l915,l916,l917,l918,l919,l920,l921,l922,l923,l924,l925,l926,l927,l928,l929,l930,l931,l932,l933,l934,l935,l936,l937,l938,l939,l940,l941,l942,l943,l944,l945,l946,l947,l948,l949,l950,l951,l952,l953,l954,l955,l956,l957,l958,l959,l960,l961,l962,l963,l964,l965,l966,l967,l968,l969,l970,l971,l972,l973,l974,l975,l976,l977,l978,l979,l980,l981,l982,l983,l984,l985,l986,l987,l988,l989,l990,l991,l992,l993,l994,l995,l996,l997,l998,l999;
        long l1000,l1001,l1002,l1003,l1004,l1005,l1006,l1007,l1008,l1009,l1010,l1011,l1012,l1013,l1014,l1015,l1016,l1017,l1018,l1019,l1020,l1021,l1022,l1023,l1024,l1025,l1026,l1027,l1028,l1029,l1030,l1031,l1032,l1033,l1034,l1035,l1036,l1037,l1038,l1039,l1040,l1041,l1042,l1043,l1044,l1045,l1046,l1047,l1048,l1049,l1050,l1051,l1052,l1053,l1054,l1055,l1056,l1057,l1058,l1059,l1060,l1061,l1062,l1063,l1064,l1065,l1066,l1067,l1068,l1069,l1070,l1071,l1072,l1073,l1074,l1075,l1076,l1077,l1078,l1079,l1080,l1081,l1082,l1083,l1084,l1085,l1086,l1087,l1088,l1089,l1090,l1091,l1092,l1093,l1094,l1095,l1096,l1097,l1098,l1099;
        long l1100,l1101,l1102,l1103,l1104,l1105,l1106,l1107,l1108,l1109,l1110,l1111,l1112,l1113,l1114,l1115,l1116,l1117,l1118,l1119,l1120,l1121,l1122,l1123,l1124,l1125,l1126,l1127,l1128,l1129,l1130,l1131,l1132,l1133,l1134,l1135,l1136,l1137,l1138,l1139,l1140,l1141,l1142,l1143,l1144,l1145,l1146,l1147,l1148,l1149,l1150,l1151,l1152,l1153,l1154,l1155,l1156,l1157,l1158,l1159,l1160,l1161,l1162,l1163,l1164,l1165,l1166,l1167,l1168,l1169,l1170,l1171,l1172,l1173,l1174,l1175,l1176,l1177,l1178,l1179,l1180,l1181,l1182,l1183,l1184,l1185,l1186,l1187,l1188,l1189,l1190,l1191,l1192,l1193,l1194,l1195,l1196,l1197,l1198,l1199;
        long l1200,l1201,l1202,l1203,l1204,l1205,l1206,l1207,l1208,l1209,l1210,l1211,l1212,l1213,l1214,l1215,l1216,l1217,l1218,l1219,l1220,l1221,l1222,l1223,l1224,l1225,l1226,l1227,l1228,l1229,l1230,l1231,l1232,l1233,l1234,l1235,l1236,l1237,l1238,l1239,l1240,l1241,l1242,l1243,l1244,l1245,l1246,l1247,l1248,l1249,l1250,l1251,l1252,l1253,l1254,l1255,l1256,l1257,l1258,l1259,l1260,l1261,l1262,l1263,l1264,l1265,l1266,l1267,l1268,l1269,l1270,l1271,l1272,l1273,l1274,l1275,l1276,l1277,l1278,l1279,l1280,l1281,l1282,l1283,l1284,l1285,l1286,l1287,l1288,l1289,l1290,l1291,l1292,l1293,l1294,l1295,l1296,l1297,l1298,l1299;
        long l1300,l1301,l1302,l1303,l1304,l1305,l1306,l1307,l1308,l1309,l1310,l1311,l1312,l1313,l1314,l1315,l1316,l1317,l1318,l1319,l1320,l1321,l1322,l1323,l1324,l1325,l1326,l1327,l1328,l1329,l1330,l1331,l1332,l1333,l1334,l1335,l1336,l1337,l1338,l1339,l1340,l1341,l1342,l1343,l1344,l1345,l1346,l1347,l1348,l1349,l1350,l1351,l1352,l1353,l1354,l1355,l1356,l1357,l1358,l1359,l1360,l1361,l1362,l1363,l1364,l1365,l1366,l1367,l1368,l1369,l1370,l1371,l1372,l1373,l1374,l1375,l1376,l1377,l1378,l1379,l1380,l1381,l1382,l1383,l1384,l1385,l1386,l1387,l1388,l1389,l1390,l1391,l1392,l1393,l1394,l1395,l1396,l1397,l1398,l1399;
        long l1400,l1401,l1402,l1403,l1404,l1405,l1406,l1407,l1408,l1409,l1410,l1411,l1412,l1413,l1414,l1415,l1416,l1417,l1418,l1419,l1420,l1421,l1422,l1423,l1424,l1425,l1426,l1427,l1428,l1429,l1430,l1431,l1432,l1433,l1434,l1435,l1436,l1437,l1438,l1439,l1440,l1441,l1442,l1443,l1444,l1445,l1446,l1447,l1448,l1449,l1450,l1451,l1452,l1453,l1454,l1455,l1456,l1457,l1458,l1459,l1460,l1461,l1462,l1463,l1464,l1465,l1466,l1467,l1468,l1469,l1470,l1471,l1472,l1473,l1474,l1475,l1476,l1477,l1478,l1479,l1480,l1481,l1482,l1483,l1484,l1485,l1486,l1487,l1488,l1489,l1490,l1491,l1492,l1493,l1494,l1495,l1496,l1497,l1498,l1499;
        long l1500,l1501,l1502,l1503,l1504,l1505,l1506,l1507,l1508,l1509,l1510,l1511,l1512,l1513,l1514,l1515,l1516,l1517,l1518,l1519,l1520,l1521,l1522,l1523,l1524,l1525,l1526,l1527,l1528,l1529,l1530,l1531,l1532,l1533,l1534,l1535,l1536,l1537,l1538,l1539,l1540,l1541,l1542,l1543,l1544,l1545,l1546,l1547,l1548,l1549,l1550,l1551,l1552,l1553,l1554,l1555,l1556,l1557,l1558,l1559,l1560,l1561,l1562,l1563,l1564,l1565,l1566,l1567,l1568,l1569,l1570,l1571,l1572,l1573,l1574,l1575,l1576,l1577,l1578,l1579,l1580,l1581,l1582,l1583,l1584,l1585,l1586,l1587,l1588,l1589,l1590,l1591,l1592,l1593,l1594,l1595,l1596,l1597,l1598,l1599;
        long l1600,l1601,l1602,l1603,l1604,l1605,l1606,l1607,l1608,l1609,l1610,l1611,l1612,l1613,l1614,l1615,l1616,l1617,l1618,l1619,l1620,l1621,l1622,l1623,l1624,l1625,l1626,l1627,l1628,l1629,l1630,l1631,l1632,l1633,l1634,l1635,l1636,l1637,l1638,l1639,l1640,l1641,l1642,l1643,l1644,l1645,l1646,l1647,l1648,l1649,l1650,l1651,l1652,l1653,l1654,l1655,l1656,l1657,l1658,l1659,l1660,l1661,l1662,l1663,l1664,l1665,l1666,l1667,l1668,l1669,l1670,l1671,l1672,l1673,l1674,l1675,l1676,l1677,l1678,l1679,l1680,l1681,l1682,l1683,l1684,l1685,l1686,l1687,l1688,l1689,l1690,l1691,l1692,l1693,l1694,l1695,l1696,l1697,l1698,l1699;
        long l1700,l1701,l1702,l1703,l1704,l1705,l1706,l1707,l1708,l1709,l1710,l1711,l1712,l1713,l1714,l1715,l1716,l1717,l1718,l1719,l1720,l1721,l1722,l1723,l1724,l1725,l1726,l1727,l1728,l1729,l1730,l1731,l1732,l1733,l1734,l1735,l1736,l1737,l1738,l1739,l1740,l1741,l1742,l1743,l1744,l1745,l1746,l1747,l1748,l1749,l1750,l1751,l1752,l1753,l1754,l1755,l1756,l1757,l1758,l1759,l1760,l1761,l1762,l1763,l1764,l1765,l1766,l1767,l1768,l1769,l1770,l1771,l1772,l1773,l1774,l1775,l1776,l1777,l1778,l1779,l1780,l1781,l1782,l1783,l1784,l1785,l1786,l1787,l1788,l1789,l1790,l1791,l1792,l1793,l1794,l1795,l1796,l1797,l1798,l1799;
        long l1800,l1801,l1802,l1803,l1804,l1805,l1806,l1807,l1808,l1809,l1810,l1811,l1812,l1813,l1814,l1815,l1816,l1817,l1818,l1819,l1820,l1821,l1822,l1823,l1824,l1825,l1826,l1827,l1828,l1829,l1830,l1831,l1832,l1833,l1834,l1835,l1836,l1837,l1838,l1839,l1840,l1841,l1842,l1843,l1844,l1845,l1846,l1847,l1848,l1849,l1850,l1851,l1852,l1853,l1854,l1855,l1856,l1857,l1858,l1859,l1860,l1861,l1862,l1863,l1864,l1865,l1866,l1867,l1868,l1869,l1870,l1871,l1872,l1873,l1874,l1875,l1876,l1877,l1878,l1879,l1880,l1881,l1882,l1883,l1884,l1885,l1886,l1887,l1888,l1889,l1890,l1891,l1892,l1893,l1894,l1895,l1896,l1897,l1898,l1899;
        long l1900,l1901,l1902,l1903,l1904,l1905,l1906,l1907,l1908,l1909,l1910,l1911,l1912,l1913,l1914,l1915,l1916,l1917,l1918,l1919,l1920,l1921,l1922,l1923,l1924,l1925,l1926,l1927,l1928,l1929,l1930,l1931,l1932,l1933,l1934,l1935,l1936,l1937,l1938,l1939,l1940,l1941,l1942,l1943,l1944,l1945,l1946,l1947,l1948,l1949,l1950,l1951,l1952,l1953,l1954,l1955,l1956,l1957,l1958,l1959,l1960,l1961,l1962,l1963,l1964,l1965,l1966,l1967,l1968,l1969,l1970,l1971,l1972,l1973,l1974,l1975,l1976,l1977,l1978,l1979,l1980,l1981,l1982,l1983,l1984,l1985,l1986,l1987,l1988,l1989,l1990,l1991,l1992,l1993,l1994,l1995,l1996,l1997,l1998,l1999;
        long l2000,l2001,l2002,l2003,l2004,l2005,l2006,l2007,l2008,l2009,l2010,l2011,l2012,l2013,l2014,l2015,l2016,l2017,l2018,l2019,l2020,l2021,l2022,l2023,l2024,l2025,l2026,l2027,l2028,l2029,l2030,l2031,l2032,l2033,l2034,l2035,l2036,l2037,l2038,l2039,l2040,l2041,l2042,l2043,l2044,l2045,l2046,l2047,l2048,l2049,l2050,l2051,l2052,l2053,l2054,l2055,l2056,l2057,l2058,l2059,l2060,l2061,l2062,l2063,l2064,l2065,l2066,l2067,l2068,l2069,l2070,l2071,l2072,l2073,l2074,l2075,l2076,l2077,l2078,l2079,l2080,l2081,l2082,l2083,l2084,l2085,l2086,l2087,l2088,l2089,l2090,l2091,l2092,l2093,l2094,l2095,l2096,l2097,l2098,l2099;
        long l2100,l2101,l2102,l2103,l2104,l2105,l2106,l2107,l2108,l2109,l2110,l2111,l2112,l2113,l2114,l2115,l2116,l2117,l2118,l2119,l2120,l2121,l2122,l2123,l2124,l2125,l2126,l2127,l2128,l2129,l2130,l2131,l2132,l2133,l2134,l2135,l2136,l2137,l2138,l2139,l2140,l2141,l2142,l2143,l2144,l2145,l2146,l2147,l2148,l2149,l2150,l2151,l2152,l2153,l2154,l2155,l2156,l2157,l2158,l2159,l2160,l2161,l2162,l2163,l2164,l2165,l2166,l2167,l2168,l2169,l2170,l2171,l2172,l2173,l2174,l2175,l2176,l2177,l2178,l2179,l2180,l2181,l2182,l2183,l2184,l2185,l2186,l2187,l2188,l2189,l2190,l2191,l2192,l2193,l2194,l2195,l2196,l2197,l2198,l2199;
        long l2200,l2201,l2202,l2203,l2204,l2205,l2206,l2207,l2208,l2209,l2210,l2211,l2212,l2213,l2214,l2215,l2216,l2217,l2218,l2219,l2220,l2221,l2222,l2223,l2224,l2225,l2226,l2227,l2228,l2229,l2230,l2231,l2232,l2233,l2234,l2235,l2236,l2237,l2238,l2239,l2240,l2241,l2242,l2243,l2244,l2245,l2246,l2247,l2248,l2249,l2250,l2251,l2252,l2253,l2254,l2255,l2256,l2257,l2258,l2259,l2260,l2261,l2262,l2263,l2264,l2265,l2266,l2267,l2268,l2269,l2270,l2271,l2272,l2273,l2274,l2275,l2276,l2277,l2278,l2279,l2280,l2281,l2282,l2283,l2284,l2285,l2286,l2287,l2288,l2289,l2290,l2291,l2292,l2293,l2294,l2295,l2296,l2297,l2298,l2299;
        long l2300,l2301,l2302,l2303,l2304,l2305,l2306,l2307,l2308,l2309,l2310,l2311,l2312,l2313,l2314,l2315,l2316,l2317,l2318,l2319,l2320,l2321,l2322,l2323,l2324,l2325,l2326,l2327,l2328,l2329,l2330,l2331,l2332,l2333,l2334,l2335,l2336,l2337,l2338,l2339,l2340,l2341,l2342,l2343,l2344,l2345,l2346,l2347,l2348,l2349,l2350,l2351,l2352,l2353,l2354,l2355,l2356,l2357,l2358,l2359,l2360,l2361,l2362,l2363,l2364,l2365,l2366,l2367,l2368,l2369,l2370,l2371,l2372,l2373,l2374,l2375,l2376,l2377,l2378,l2379,l2380,l2381,l2382,l2383,l2384,l2385,l2386,l2387,l2388,l2389,l2390,l2391,l2392,l2393,l2394,l2395,l2396,l2397,l2398,l2399;
        long l2400,l2401,l2402,l2403,l2404,l2405,l2406,l2407,l2408,l2409,l2410,l2411,l2412,l2413,l2414,l2415,l2416,l2417,l2418,l2419,l2420,l2421,l2422,l2423,l2424,l2425,l2426,l2427,l2428,l2429,l2430,l2431,l2432,l2433,l2434,l2435,l2436,l2437,l2438,l2439,l2440,l2441,l2442,l2443,l2444,l2445,l2446,l2447,l2448,l2449,l2450,l2451,l2452,l2453,l2454,l2455,l2456,l2457,l2458,l2459,l2460,l2461,l2462,l2463,l2464,l2465,l2466,l2467,l2468,l2469,l2470,l2471,l2472,l2473,l2474,l2475,l2476,l2477,l2478,l2479,l2480,l2481,l2482,l2483,l2484,l2485,l2486,l2487,l2488,l2489,l2490,l2491,l2492,l2493,l2494,l2495,l2496,l2497,l2498,l2499;
        long l2500,l2501,l2502,l2503,l2504,l2505,l2506,l2507,l2508,l2509,l2510,l2511,l2512,l2513,l2514,l2515,l2516,l2517,l2518,l2519,l2520,l2521,l2522,l2523,l2524,l2525,l2526,l2527,l2528,l2529,l2530,l2531,l2532,l2533,l2534,l2535,l2536,l2537,l2538,l2539,l2540,l2541,l2542,l2543,l2544,l2545,l2546,l2547,l2548,l2549,l2550,l2551,l2552,l2553,l2554,l2555,l2556,l2557,l2558,l2559,l2560,l2561,l2562,l2563,l2564,l2565,l2566,l2567,l2568,l2569,l2570,l2571,l2572,l2573,l2574,l2575,l2576,l2577,l2578,l2579,l2580,l2581,l2582,l2583,l2584,l2585,l2586,l2587,l2588,l2589,l2590,l2591,l2592,l2593,l2594,l2595,l2596,l2597,l2598,l2599;
        long l2600,l2601,l2602,l2603,l2604,l2605,l2606,l2607,l2608,l2609,l2610,l2611,l2612,l2613,l2614,l2615,l2616,l2617,l2618,l2619,l2620,l2621,l2622,l2623,l2624,l2625,l2626,l2627,l2628,l2629,l2630,l2631,l2632,l2633,l2634,l2635,l2636,l2637,l2638,l2639,l2640,l2641,l2642,l2643,l2644,l2645,l2646,l2647,l2648,l2649,l2650,l2651,l2652,l2653,l2654,l2655,l2656,l2657,l2658,l2659,l2660,l2661,l2662,l2663,l2664,l2665,l2666,l2667,l2668,l2669,l2670,l2671,l2672,l2673,l2674,l2675,l2676,l2677,l2678,l2679,l2680,l2681,l2682,l2683,l2684,l2685,l2686,l2687,l2688,l2689,l2690,l2691,l2692,l2693,l2694,l2695,l2696,l2697,l2698,l2699;
        long l2700,l2701,l2702,l2703,l2704,l2705,l2706,l2707,l2708,l2709,l2710,l2711,l2712,l2713,l2714,l2715,l2716,l2717,l2718,l2719,l2720,l2721,l2722,l2723,l2724,l2725,l2726,l2727,l2728,l2729,l2730,l2731,l2732,l2733,l2734,l2735,l2736,l2737,l2738,l2739,l2740,l2741,l2742,l2743,l2744,l2745,l2746,l2747,l2748,l2749,l2750,l2751,l2752,l2753,l2754,l2755,l2756,l2757,l2758,l2759,l2760,l2761,l2762,l2763,l2764,l2765,l2766,l2767,l2768,l2769,l2770,l2771,l2772,l2773,l2774,l2775,l2776,l2777,l2778,l2779,l2780,l2781,l2782,l2783,l2784,l2785,l2786,l2787,l2788,l2789,l2790,l2791,l2792,l2793,l2794,l2795,l2796,l2797,l2798,l2799;
        long l2800,l2801,l2802,l2803,l2804,l2805,l2806,l2807,l2808,l2809,l2810,l2811,l2812,l2813,l2814,l2815,l2816,l2817,l2818,l2819,l2820,l2821,l2822,l2823,l2824,l2825,l2826,l2827,l2828,l2829,l2830,l2831,l2832,l2833,l2834,l2835,l2836,l2837,l2838,l2839,l2840,l2841,l2842,l2843,l2844,l2845,l2846,l2847,l2848,l2849,l2850,l2851,l2852,l2853,l2854,l2855,l2856,l2857,l2858,l2859,l2860,l2861,l2862,l2863,l2864,l2865,l2866,l2867,l2868,l2869,l2870,l2871,l2872,l2873,l2874,l2875,l2876,l2877,l2878,l2879,l2880,l2881,l2882,l2883,l2884,l2885,l2886,l2887,l2888,l2889,l2890,l2891,l2892,l2893,l2894,l2895,l2896,l2897,l2898,l2899;
        long l2900,l2901,l2902,l2903,l2904,l2905,l2906,l2907,l2908,l2909,l2910,l2911,l2912,l2913,l2914,l2915,l2916,l2917,l2918,l2919,l2920,l2921,l2922,l2923,l2924,l2925,l2926,l2927,l2928,l2929,l2930,l2931,l2932,l2933,l2934,l2935,l2936,l2937,l2938,l2939,l2940,l2941,l2942,l2943,l2944,l2945,l2946,l2947,l2948,l2949,l2950,l2951,l2952,l2953,l2954,l2955,l2956,l2957,l2958,l2959,l2960,l2961,l2962,l2963,l2964,l2965,l2966,l2967,l2968,l2969,l2970,l2971,l2972,l2973,l2974,l2975,l2976,l2977,l2978,l2979,l2980,l2981,l2982,l2983,l2984,l2985,l2986,l2987,l2988,l2989,l2990,l2991,l2992,l2993,l2994,l2995,l2996,l2997,l2998,l2999;
        long l3000,l3001,l3002,l3003,l3004,l3005,l3006,l3007,l3008,l3009,l3010,l3011,l3012,l3013,l3014,l3015,l3016,l3017,l3018,l3019,l3020,l3021,l3022,l3023,l3024,l3025,l3026,l3027,l3028,l3029,l3030,l3031,l3032,l3033,l3034,l3035,l3036,l3037,l3038,l3039,l3040,l3041,l3042,l3043,l3044,l3045,l3046,l3047,l3048,l3049,l3050,l3051,l3052,l3053,l3054,l3055,l3056,l3057,l3058,l3059,l3060,l3061,l3062,l3063,l3064,l3065,l3066,l3067,l3068,l3069,l3070,l3071,l3072,l3073,l3074,l3075,l3076,l3077,l3078,l3079,l3080,l3081,l3082,l3083,l3084,l3085,l3086,l3087,l3088,l3089,l3090,l3091,l3092,l3093,l3094,l3095,l3096,l3097,l3098,l3099;
        long l3100,l3101,l3102,l3103,l3104,l3105,l3106,l3107,l3108,l3109,l3110,l3111,l3112,l3113,l3114,l3115,l3116,l3117,l3118,l3119,l3120,l3121,l3122,l3123,l3124,l3125,l3126,l3127,l3128,l3129,l3130,l3131,l3132,l3133,l3134,l3135,l3136,l3137,l3138,l3139,l3140,l3141,l3142,l3143,l3144,l3145,l3146,l3147,l3148,l3149,l3150,l3151,l3152,l3153,l3154,l3155,l3156,l3157,l3158,l3159,l3160,l3161,l3162,l3163,l3164,l3165,l3166,l3167,l3168,l3169,l3170,l3171,l3172,l3173,l3174,l3175,l3176,l3177,l3178,l3179,l3180,l3181,l3182,l3183,l3184,l3185,l3186,l3187,l3188,l3189,l3190,l3191,l3192,l3193,l3194,l3195,l3196,l3197,l3198,l3199;
        long l3200,l3201,l3202,l3203,l3204,l3205,l3206,l3207,l3208,l3209,l3210,l3211,l3212,l3213,l3214,l3215,l3216,l3217,l3218,l3219,l3220,l3221,l3222,l3223,l3224,l3225,l3226,l3227,l3228,l3229,l3230,l3231,l3232,l3233,l3234,l3235,l3236,l3237,l3238,l3239,l3240,l3241,l3242,l3243,l3244,l3245,l3246,l3247,l3248,l3249,l3250,l3251,l3252,l3253,l3254,l3255,l3256,l3257,l3258,l3259,l3260,l3261,l3262,l3263,l3264,l3265,l3266,l3267,l3268,l3269,l3270,l3271,l3272,l3273,l3274,l3275,l3276,l3277,l3278,l3279,l3280,l3281,l3282,l3283,l3284,l3285,l3286,l3287,l3288,l3289,l3290,l3291,l3292,l3293,l3294,l3295,l3296,l3297,l3298,l3299;
        long l3300,l3301,l3302,l3303,l3304,l3305,l3306,l3307,l3308,l3309,l3310,l3311,l3312,l3313,l3314,l3315,l3316,l3317,l3318,l3319,l3320,l3321,l3322,l3323,l3324,l3325,l3326,l3327,l3328,l3329,l3330,l3331,l3332,l3333,l3334,l3335,l3336,l3337,l3338,l3339,l3340,l3341,l3342,l3343,l3344,l3345,l3346,l3347,l3348,l3349,l3350,l3351,l3352,l3353,l3354,l3355,l3356,l3357,l3358,l3359,l3360,l3361,l3362,l3363,l3364,l3365,l3366,l3367,l3368,l3369,l3370,l3371,l3372,l3373,l3374,l3375,l3376,l3377,l3378,l3379,l3380,l3381,l3382,l3383,l3384,l3385,l3386,l3387,l3388,l3389,l3390,l3391,l3392,l3393,l3394,l3395,l3396,l3397,l3398,l3399;
        long l3400,l3401,l3402,l3403,l3404,l3405,l3406,l3407,l3408,l3409,l3410,l3411,l3412,l3413,l3414,l3415,l3416,l3417,l3418,l3419,l3420,l3421,l3422,l3423,l3424,l3425,l3426,l3427,l3428,l3429,l3430,l3431,l3432,l3433,l3434,l3435,l3436,l3437,l3438,l3439,l3440,l3441,l3442,l3443,l3444,l3445,l3446,l3447,l3448,l3449,l3450,l3451,l3452,l3453,l3454,l3455,l3456,l3457,l3458,l3459,l3460,l3461,l3462,l3463,l3464,l3465,l3466,l3467,l3468,l3469,l3470,l3471,l3472,l3473,l3474,l3475,l3476,l3477,l3478,l3479,l3480,l3481,l3482,l3483,l3484,l3485,l3486,l3487,l3488,l3489,l3490,l3491,l3492,l3493,l3494,l3495,l3496,l3497,l3498,l3499;
        long l3500,l3501,l3502,l3503,l3504,l3505,l3506,l3507,l3508,l3509,l3510,l3511,l3512,l3513,l3514,l3515,l3516,l3517,l3518,l3519,l3520,l3521,l3522,l3523,l3524,l3525,l3526,l3527,l3528,l3529,l3530,l3531,l3532,l3533,l3534,l3535,l3536,l3537,l3538,l3539,l3540,l3541,l3542,l3543,l3544,l3545,l3546,l3547,l3548,l3549,l3550,l3551,l3552,l3553,l3554,l3555,l3556,l3557,l3558,l3559,l3560,l3561,l3562,l3563,l3564,l3565,l3566,l3567,l3568,l3569,l3570,l3571,l3572,l3573,l3574,l3575,l3576,l3577,l3578,l3579,l3580,l3581,l3582,l3583,l3584,l3585,l3586,l3587,l3588,l3589,l3590,l3591,l3592,l3593,l3594,l3595,l3596,l3597,l3598,l3599;
        long l3600,l3601,l3602,l3603,l3604,l3605,l3606,l3607,l3608,l3609,l3610,l3611,l3612,l3613,l3614,l3615,l3616,l3617,l3618,l3619,l3620,l3621,l3622,l3623,l3624,l3625,l3626,l3627,l3628,l3629,l3630,l3631,l3632,l3633,l3634,l3635,l3636,l3637,l3638,l3639,l3640,l3641,l3642,l3643,l3644,l3645,l3646,l3647,l3648,l3649,l3650,l3651,l3652,l3653,l3654,l3655,l3656,l3657,l3658,l3659,l3660,l3661,l3662,l3663,l3664,l3665,l3666,l3667,l3668,l3669,l3670,l3671,l3672,l3673,l3674,l3675,l3676,l3677,l3678,l3679,l3680,l3681,l3682,l3683,l3684,l3685,l3686,l3687,l3688,l3689,l3690,l3691,l3692,l3693,l3694,l3695,l3696,l3697,l3698,l3699;
        long l3700,l3701,l3702,l3703,l3704,l3705,l3706,l3707,l3708,l3709,l3710,l3711,l3712,l3713,l3714,l3715,l3716,l3717,l3718,l3719,l3720,l3721,l3722,l3723,l3724,l3725,l3726,l3727,l3728,l3729,l3730,l3731,l3732,l3733,l3734,l3735,l3736,l3737,l3738,l3739,l3740,l3741,l3742,l3743,l3744,l3745,l3746,l3747,l3748,l3749,l3750,l3751,l3752,l3753,l3754,l3755,l3756,l3757,l3758,l3759,l3760,l3761,l3762,l3763,l3764,l3765,l3766,l3767,l3768,l3769,l3770,l3771,l3772,l3773,l3774,l3775,l3776,l3777,l3778,l3779,l3780,l3781,l3782,l3783,l3784,l3785,l3786,l3787,l3788,l3789,l3790,l3791,l3792,l3793,l3794,l3795,l3796,l3797,l3798,l3799;
        long l3800,l3801,l3802,l3803,l3804,l3805,l3806,l3807,l3808,l3809,l3810,l3811,l3812,l3813,l3814,l3815,l3816,l3817,l3818,l3819,l3820,l3821,l3822,l3823,l3824,l3825,l3826,l3827,l3828,l3829,l3830,l3831,l3832,l3833,l3834,l3835,l3836,l3837,l3838,l3839,l3840,l3841,l3842,l3843,l3844,l3845,l3846,l3847,l3848,l3849,l3850,l3851,l3852,l3853,l3854,l3855,l3856,l3857,l3858,l3859,l3860,l3861,l3862,l3863,l3864,l3865,l3866,l3867,l3868,l3869,l3870,l3871,l3872,l3873,l3874,l3875,l3876,l3877,l3878,l3879,l3880,l3881,l3882,l3883,l3884,l3885,l3886,l3887,l3888,l3889,l3890,l3891,l3892,l3893,l3894,l3895,l3896,l3897,l3898,l3899;
        long l3900,l3901,l3902,l3903,l3904,l3905,l3906,l3907,l3908,l3909,l3910,l3911,l3912,l3913,l3914,l3915,l3916,l3917,l3918,l3919,l3920,l3921,l3922,l3923,l3924,l3925,l3926,l3927,l3928,l3929,l3930,l3931,l3932,l3933,l3934,l3935,l3936,l3937,l3938,l3939,l3940,l3941,l3942,l3943,l3944,l3945,l3946,l3947,l3948,l3949,l3950,l3951,l3952,l3953,l3954,l3955,l3956,l3957,l3958,l3959,l3960,l3961,l3962,l3963,l3964,l3965,l3966,l3967,l3968,l3969,l3970,l3971,l3972,l3973,l3974,l3975,l3976,l3977,l3978,l3979,l3980,l3981,l3982,l3983,l3984,l3985,l3986,l3987,l3988,l3989,l3990,l3991,l3992,l3993,l3994,l3995,l3996,l3997,l3998,l3999;
        long l4000,l4001,l4002,l4003,l4004,l4005,l4006,l4007,l4008,l4009,l4010,l4011,l4012,l4013,l4014,l4015,l4016,l4017,l4018,l4019,l4020,l4021,l4022,l4023,l4024,l4025,l4026,l4027,l4028,l4029,l4030,l4031,l4032,l4033,l4034,l4035,l4036,l4037,l4038,l4039,l4040,l4041,l4042,l4043,l4044,l4045,l4046,l4047,l4048,l4049,l4050,l4051,l4052,l4053,l4054,l4055,l4056,l4057,l4058,l4059,l4060,l4061,l4062,l4063,l4064,l4065,l4066,l4067,l4068,l4069,l4070,l4071,l4072,l4073,l4074,l4075,l4076,l4077,l4078,l4079,l4080,l4081,l4082,l4083,l4084,l4085,l4086,l4087,l4088,l4089,l4090,l4091,l4092,l4093,l4094,l4095;
        Object objOnOtherStripe;
    }
}
