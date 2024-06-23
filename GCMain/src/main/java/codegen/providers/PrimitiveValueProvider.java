package codegen.providers;

import config.FuzzingConfig;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.typing.fast.Integer127Type;
import soot.jimple.toolkits.typing.fast.Integer1Type;
import soot.jimple.toolkits.typing.fast.Integer32767Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimitiveValueProvider {

    public static List<Integer> intPredefined = new ArrayList<>(Arrays.asList(-100, -3, -2, -1, 0, 1, 2, 3, 100));
    public static List<Float> floatPredefined = new ArrayList<>(Arrays.asList(-100f, -3.0f, -2.0f, -1.0f, -0.1f, 0.0f, 0.1f, 1.0f, 2.0f, 3.0f, 100f));
    public static List<Double> doublePredefined = new ArrayList<>(Arrays.asList(-100d, -3.0d, -2.0d, -1.0d, -0.1d, 0.0d, 0.1d, 1.0d, 2.0d, 3.0d, 100d));
    public static List<Character> charPredefined = new ArrayList<Character>(Arrays.asList(
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '~','`','!','@','#','$','%','^','&','*','(',')','-','_','+','=','{','}','[',']',':',';','\'','<','>',
            ',','.','?','/','|',' '));// all chars found on a US keyboard, except for '\' and '"' which causes trouble in Strings

    public static boolean isPrimitive(Type type){

        if (type instanceof IntType){
            return true;
        } else if (type instanceof BooleanType){
            return true;
        } else if (type instanceof ByteType){
            return true;
        } else if (type instanceof CharType){
            return true;
        } else if (type instanceof DoubleType){
            return true;
        } else if (type instanceof FloatType){
            return true;
        } else if (type instanceof IntType){
            return true;
        } else if (type instanceof Integer127Type){
            return true;
        } else if (type instanceof Integer1Type){
            return true;
        } else if (type instanceof Integer32767Type){
            return true;
        } else if (type instanceof LongType){
            return true;
        } else if (type instanceof ShortType){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPrimitiveOrString(Type type){

        if (type instanceof PrimType){
            return true;
        } else if (type instanceof RefType){
            if ( ((RefType)type).getClassName().equals("java.lang.String") ){
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public static Value next(String type){

        switch (type){

            case "boolean":
                if (nextBoolean()){
                    return IntConstant.v(0);
                } else {
                    return IntConstant.v(1);
                }
            case "int":
                return IntConstant.v(nextInt());
            case "String":
                return StringConstant.v(nextString());
            default:
                return IntConstant.v(nextInt());
        }
    }

    public static Value nextPositiveInt(){

        int value = nextInt();
        if (value > 0){
            return IntConstant.v(value);
        } else {
            return IntConstant.v(value * -1);
        }
    }


    public static Value next(Type type){

        if (type instanceof BooleanType){

            if (nextBoolean()){
                return IntConstant.v(0);
            } else {
                return IntConstant.v(1);
            }
        } else if (type instanceof ByteType){
            return IntConstant.v(nextByte());
        } else if (type instanceof CharType){
            return IntConstant.v(nextChar());
        } else if (type instanceof DoubleType){
            return DoubleConstant.v(nextDouble());
        } else if (type instanceof FloatType){
            return FloatConstant.v(nextFloat());
        } else if (type instanceof IntType){
            return IntConstant.v(nextInt());
        } else if (type instanceof Integer127Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof Integer1Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof Integer32767Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof LongType){
            return LongConstant.v(nextInt());
        } else if (type instanceof ShortType){
            //TODO
            return IntConstant.v(nextInt());
        } else if (type instanceof RefType){

            if ( ((RefType)type).getClassName().equals("java.lang.String") ){
                return StringConstant.v(nextString());
            } else {
                throw new RuntimeException("Unknown type:" + type);
            }
        } else if (type instanceof  NullType) {
          return NullConstant.v();
        } else {
            throw new RuntimeException("Unknown type:" + type);
        }
    }

    public static Value nextNonZero(Type type){

        if (type instanceof BooleanType){

            if (nextBoolean()){
                return IntConstant.v(0);
            } else {
                return IntConstant.v(1);
            }
        } else if (type instanceof ByteType){
            int val = nextByte();
            while (val == 0) val = nextByte();
            return IntConstant.v(val);
        } else if (type instanceof CharType){
            return IntConstant.v(nextChar());
        } else if (type instanceof DoubleType){
            double val = nextDouble();
            while (Math.abs(val - 0)  <= 1e-8) val = nextDouble();
            return DoubleConstant.v(val);
        } else if (type instanceof FloatType){
            float val = nextFloat();
            while (Math.abs(val - 0)  <= 1e-8) val = nextFloat();
            return FloatConstant.v(val);
        } else if (type instanceof IntType){
            int val = nextInt();
            while (val == 0) val = nextInt();
            return IntConstant.v(val);
        } else if (type instanceof Integer127Type){
            int val = nextInt();
            while (val == 0) val = nextInt();
            return IntConstant.v(val);
            //TODO
        } else if (type instanceof Integer1Type){
            int val = nextInt();
            while (val == 0) val = nextInt();
            return IntConstant.v(val);
            //TODO
        } else if (type instanceof Integer32767Type){
            int val = nextInt();
            while (val == 0) val = nextInt();
            return IntConstant.v(val);
            //TODO
        } else if (type instanceof LongType){
            int val = nextInt();
            while (val == 0) val = nextInt();
            return LongConstant.v(val);
        } else if (type instanceof ShortType){
            //TODO
            int val = nextInt();
            while (val == 0) val = nextInt();
            return IntConstant.v(val);
        } else if (type instanceof RefType){

            if ( ((RefType)type).getClassName().equals("java.lang.String") ){
                return StringConstant.v(nextString());
            } else {
                throw new RuntimeException("Unknown type:" + type);
            }
        } else {
            throw new RuntimeException("Unknown type:" + type);
        }

    }

    public static int nextInt(){
        return intPredefined.get(FuzzingConfig.getDefaultRandom().nextInt(intPredefined.size()));
    }

    public static char nextChar(){
        return charPredefined.get(FuzzingConfig.getDefaultRandom().nextInt(charPredefined.size()));
    }

    public static String nextString(){
        String str = "";
        while (FuzzingConfig.getDefaultRandom().nextBoolean()){
            str += nextChar();
        }
        return str;
    }

    public static boolean nextBoolean(){
        return FuzzingConfig.getDefaultRandom().nextBoolean();
    }

    public static float nextFloat(){
        return floatPredefined.get(FuzzingConfig.getDefaultRandom().nextInt(floatPredefined.size()));
    }

    public static double nextDouble(){
        return doublePredefined.get(FuzzingConfig.getDefaultRandom().nextInt(doublePredefined.size()));
    }

    public static byte nextByte(){
        return intPredefined.get(FuzzingConfig.getDefaultRandom().nextInt(intPredefined.size())).byteValue();
    }
}
