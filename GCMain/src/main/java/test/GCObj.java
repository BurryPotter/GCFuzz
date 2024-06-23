//package test;
//
//import java.lang.ref.PhantomReference;
//import java.lang.ref.ReferenceQueue;
//import java.lang.ref.SoftReference;
//import java.lang.ref.WeakReference;
//
//public class GCObj {
//    public GCObj strongReference = null;
//    public SoftReference<GCObj> softReference = null;
//    public WeakReference<GCObj> weakReference = null;
//    public PhantomReference<GCObj> phantomReference = null;
//    public byte[] space = null;
//
//    public GCObj(GCObj strongReference, GCObj softReference, GCObj weakReference, GCObj phantomReference, int size) {
//        this.strongReference = strongReference;
//        this.softReference = new SoftReference<>(softReference);
//        this.weakReference = new WeakReference<>(weakReference);
//        ReferenceQueue<GCObj> referenceQueue = new ReferenceQueue<>();
//        this.phantomReference = new PhantomReference<>(phantomReference, referenceQueue);
//        this.space = new byte[size];
//    }
//}
