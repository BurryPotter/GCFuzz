import java.util.ArrayList; 
import java.util.List; 

public class Test { 

    public static void main(String[] args) { 

        List<Integer> list = new ArrayList<>(); 

        try{ 
            for (int i = 0; i < Integer.MAX_VALUE; i++){ 
                list.add(i); 
            } 
        } catch (Throwable e){ 
            System.out.println("Test catch"); 
        } finally { 
            System.out.println("Test finally"); 
        } 
    } 
} 