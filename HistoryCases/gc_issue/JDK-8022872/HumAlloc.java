public class HumAlloc {
public static byte[] humObj;

public static void main(String[] args) {
while (true) {
humObj = new byte[2*1024*1024];
}
}
}