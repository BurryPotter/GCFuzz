  
Disassembly for compiled method [private static void processPendingReferences() @0x0d3e9b78 ] @0x02cf2588
Method
private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290
Compiled Code
[Entry Point] [Verified Entry Point] 0x02cf2780:    mov %eax,-0x9000(%esp)

0x02cf2787:    push %ebp

0x02cf2788:    sub $0x38,%esp


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 0, line = 174


0x02cf278b:    call 0x02cf2e42


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 0, line = 174
            locals ([0], illegal) ([1], illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 



0x02cf2790:    mov $0x517e818,%esi

0x02cf2795:    lea 0x2c(%esp),%edi

0x02cf2799:    mov %esi,0x4(%edi)

0x02cf279c:    mov (%esi),%eax

0x02cf279e:    mov %eax,%ebx

0x02cf27a0:    and $0x7,%ebx

0x02cf27a3:    cmp $0x5,%ebx

0x02cf27a6:    jne 0x02cf282e

0x02cf27ac:    mov %eax,(%edi)

0x02cf27ae:    mov 0x4(%esi),%ebx

0x02cf27b1:    mov 0x70(%ebx),%ebx

0x02cf27b4:    xor %eax,%ebx

0x02cf27b6:    mov %fs:0x0(,%eiz,1),%eax

0x02cf27be:    mov -0xc(%eax),%eax

0x02cf27c1:    xor %ebx,%eax

0x02cf27c3:    and $0xffffff87,%eax

0x02cf27c6:    je 0x02cf284f

0x02cf27cc:    test $0x7,%eax

0x02cf27d1:    jne 0x02cf2822

0x02cf27d3:    test $0x180,%eax

0x02cf27d8:    jne 0x02cf27fe

0x02cf27da:    mov (%edi),%eax

0x02cf27dc:    and $0x1ff,%eax

0x02cf27e2:    mov %fs:0x0(,%eiz,1),%ebx

0x02cf27ea:    mov -0xc(%ebx),%ebx

0x02cf27ed:    or %eax,%ebx

0x02cf27ef:    lock cmpxchg %ebx,(%esi)

0x02cf27f3:    jne 0x02cf2cda

0x02cf27f9:    jmp 0x02cf284f

0x02cf27fe:    mov 0x4(%esi),%ebx

0x02cf2801:    mov 0x70(%ebx),%ebx

0x02cf2804:    mov %fs:0x0(,%eiz,1),%eax

0x02cf280c:    mov -0xc(%eax),%eax

0x02cf280f:    or %eax,%ebx

0x02cf2811:    mov (%edi),%eax

0x02cf2813:    lock cmpxchg %ebx,(%esi)

0x02cf2817:    jne 0x02cf2cda

0x02cf281d:    jmp 0x02cf284f

0x02cf2822:    mov (%edi),%eax

0x02cf2824:    mov 0x4(%esi),%ebx

0x02cf2827:    mov 0x70(%ebx),%ebx

0x02cf282a:    lock cmpxchg %ebx,(%esi)

0x02cf282e:    mov (%esi),%eax

0x02cf2830:    or $0x1,%eax

0x02cf2833:    mov %eax,(%edi)

0x02cf2835:    lock cmpxchg %edi,(%esi)

0x02cf2839:    je 0x02cf284f

0x02cf283f:    sub %esp,%eax

0x02cf2841:    and $0xfffff003,%eax

0x02cf2847:    mov %eax,(%edi)

0x02cf2849:    jne 0x02cf2cda


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 8, line = 176


0x02cf284f:    call 0x02cf2e4e


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 9, line = 177
            locals ([0], illegal) ([1], oop 0x0517e818) ([2], illegal) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:[48]  

0x02cf2854:    mov %eax,%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 9, line = 177


0x02cf2856:    mov $0x51aa530,%eax

0x02cf285b:    movb $0x1,0x64(%eax)


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 14, line = 178


0x02cf285f:    lea 0x2c(%esp),%eax

0x02cf2863:    mov 0x4(%eax),%edi

0x02cf2866:    mov (%edi),%esi

0x02cf2868:    and $0x7,%esi

0x02cf286b:    cmp $0x5,%esi

0x02cf286e:    je 0x02cf2888

0x02cf2874:    mov (%eax),%esi

0x02cf2876:    test %esi,%esi

0x02cf2878:    je 0x02cf2888

0x02cf287e:    lock cmpxchg %esi,(%edi)

0x02cf2882:    jne 0x02cf2ceb


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 18, line = 179


0x02cf2888:    cmp $0x0,%ecx

0x02cf288b:    je 0x02cf2aae


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 28, line = 180


0x02cf2891:    mov 0x14(%ecx),%esi


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 34, line = 182


0x02cf2894:    mov %fs:0x0(,%eiz,1),%edi

0x02cf289c:    mov -0xc(%edi),%edi

0x02cf289f:    movsbl 0x324(%edi),%edi

0x02cf28a6:    cmp $0x0,%edi

0x02cf28a9:    jne 0x02cf2d12

0x02cf28af:    movl $0x0,0x14(%ecx)


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 40, line = 183


0x02cf28b6:    cmp $0x0,%ecx

0x02cf28b9:    je 0x02cf28d1

0x02cf28bf:    cmpl $0xd4cfaa0,0x4(%ecx)

0x02cf28c6:    jne 0x02cf28d1

0x02cf28cc:    jmp 0x02cf28d5

0x02cf28d1:    xor %edi,%edi

0x02cf28d3:    jmp 0x02cf28da

0x02cf28d5:    mov $0x1,%edi


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 44, line = 185


0x02cf28da:    cmp $0x0,%edi

0x02cf28dd:    mov %esi,0x24(%esp)

0x02cf28e1:    je 0x02cf2935


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 47, line = 185


0x02cf28e7:    cmp $0x0,%ecx

0x02cf28ea:    je 0x02cf2902

0x02cf28f0:    cmpl $0xd4cfaa0,0x4(%ecx)

0x02cf28f7:    jne 0x02cf2d2b

0x02cf28fd:    jmp 0x02cf2902

0x02cf2902:    mov %ecx,%edi


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 51, line = 186


0x02cf2904:    mov %edi,0x20(%esp)

0x02cf2908:    cmp (%edi),%eax


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186


0x02cf290a:    mov %edi,%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 1, line = 140


0x02cf290c:    nop

0x02cf290d:    nop

0x02cf290e:    nop

0x02cf290f:    call 0x02be6ee0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 1, line = 140
                locals ('this', stack[32], oop) ([1], illegal) 



OopMap: 

Oops:[36]  [32]  

0x02cf2914:    cmp $0x0,%eax

0x02cf2917:    je 0x02cf29b0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 4, line = 140


0x02cf291d:    mov 0x20(%esp),%edi

0x02cf2921:    mov 0x20(%edi),%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 9, line = 143


0x02cf2924:    nop

0x02cf2925:    nop

0x02cf2926:    mov $0xffffffff,%eax

0x02cf292b:    call 0x02be6c60


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 12, line = 143
                locals ('this', stack[32], oop) ([1], illegal) 



OopMap: 

Oops:[36]  [32]  

0x02cf2930:    jmp 0x02cf29b0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 17, line = 153


0x02cf2935:    mov 0xc(%ecx),%esi


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 83, line = 194


0x02cf2938:    mov $0x51b80c0,%edx

0x02cf293d:    mov 0x60(%edx),%edx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 88, line = 195


0x02cf2940:    cmp %edx,%esi

0x02cf2942:    je 0x02cf2a9d


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 91, line = 195


0x02cf2948:    mov %ecx,%edx

0x02cf294a:    mov %esi,%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 96, line = 195


0x02cf294c:    nop

0x02cf294d:    nop

0x02cf294e:    mov $0xffffffff,%eax

0x02cf2953:    call 0x02be6c60


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 96, line = 195
            locals ('pendingList', stack[36], oop) ('ref', illegal) ('q', illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:[36]  

0x02cf2958:    jmp 0x02cf2a9d


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 100, line = 197


0x02cf295d:    mov 0x20(%esp),%edi

0x02cf2961:    mov %fs:0x0(,%eiz,1),%edx

0x02cf2969:    mov -0xc(%edx),%edx

0x02cf296c:    mov 0x1f4(%edx),%eax

0x02cf2972:    movl $0x0,0x1f4(%edx)

0x02cf297c:    movl $0x0,0x1f8(%edx)

0x02cf2986:    mov %eax,%ecx

0x02cf2988:    jmp 0x02cf2d64

0x02cf298d:    jmp 0x02cf2d6e


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 21, line = 145


0x02cf2992:    mov %edi,%edx

0x02cf2994:    mov %ecx,(%esp)

0x02cf2997:    mov %eax,%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 27, line = 145


0x02cf2999:    mov %eax,0x28(%esp)

0x02cf299d:    nop

0x02cf299e:    nop

0x02cf299f:    call 0x02be69e0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 27, line = 145
                locals ('this', illegal) ('x', illegal) 
                expressions ([0], stack[40], oop) 



OopMap: 

Oops:[36]  [40]  

0x02cf29a4:    mov 0x28(%esp),%ecx


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 30, line = 145


0x02cf29a8:    nop

0x02cf29a9:    nop

0x02cf29aa:    nop

0x02cf29ab:    call 0x02be6ee0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 30, line = 145
                locals ('this', illegal) ('x', illegal) 



OopMap: 

Oops:[36]  

0x02cf29b0:    mov $0x517e818,%ecx

0x02cf29b5:    lea 0x2c(%esp),%esi

0x02cf29b9:    mov %ecx,0x4(%esi)

0x02cf29bc:    mov (%ecx),%eax

0x02cf29be:    mov %eax,%edi

0x02cf29c0:    and $0x7,%edi

0x02cf29c3:    cmp $0x5,%edi

0x02cf29c6:    jne 0x02cf2a4e

0x02cf29cc:    mov %eax,(%esi)

0x02cf29ce:    mov 0x4(%ecx),%edi

0x02cf29d1:    mov 0x70(%edi),%edi

0x02cf29d4:    xor %eax,%edi

0x02cf29d6:    mov %fs:0x0(,%eiz,1),%eax

0x02cf29de:    mov -0xc(%eax),%eax

0x02cf29e1:    xor %edi,%eax

0x02cf29e3:    and $0xffffff87,%eax

0x02cf29e6:    je 0x02cf2a6f

0x02cf29ec:    test $0x7,%eax

0x02cf29f1:    jne 0x02cf2a42

0x02cf29f3:    test $0x180,%eax

0x02cf29f8:    jne 0x02cf2a1e

0x02cf29fa:    mov (%esi),%eax

0x02cf29fc:    and $0x1ff,%eax

0x02cf2a02:    mov %fs:0x0(,%eiz,1),%edi

0x02cf2a0a:    mov -0xc(%edi),%edi

0x02cf2a0d:    or %eax,%edi

0x02cf2a0f:    lock cmpxchg %edi,(%ecx)

0x02cf2a13:    jne 0x02cf2d7a

0x02cf2a19:    jmp 0x02cf2a6f

0x02cf2a1e:    mov 0x4(%ecx),%edi

0x02cf2a21:    mov 0x70(%edi),%edi

0x02cf2a24:    mov %fs:0x0(,%eiz,1),%eax

0x02cf2a2c:    mov -0xc(%eax),%eax

0x02cf2a2f:    or %eax,%edi

0x02cf2a31:    mov (%esi),%eax

0x02cf2a33:    lock cmpxchg %edi,(%ecx)

0x02cf2a37:    jne 0x02cf2d7a

0x02cf2a3d:    jmp 0x02cf2a6f

0x02cf2a42:    mov (%esi),%eax

0x02cf2a44:    mov 0x4(%ecx),%edi

0x02cf2a47:    mov 0x70(%edi),%edi

0x02cf2a4a:    lock cmpxchg %edi,(%ecx)

0x02cf2a4e:    mov (%ecx),%eax

0x02cf2a50:    or $0x1,%eax

0x02cf2a53:    mov %eax,(%esi)

0x02cf2a55:    lock cmpxchg %esi,(%ecx)

0x02cf2a59:    je 0x02cf2a6f

0x02cf2a5f:    sub %esp,%eax

0x02cf2a61:    and $0xfffff003,%eax

0x02cf2a67:    mov %eax,(%esi)

0x02cf2a69:    jne 0x02cf2d7a


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 62, line = 190


0x02cf2a6f:    call 0x02be69e0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 66, line = 191
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], oop 0x0517e818) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:[36]  [48]  

0x02cf2a74:    lea 0x2c(%esp),%eax

0x02cf2a78:    mov 0x4(%eax),%esi

0x02cf2a7b:    mov (%esi),%ecx

0x02cf2a7d:    and $0x7,%ecx

0x02cf2a80:    cmp $0x5,%ecx

0x02cf2a83:    je 0x02cf2a9d

0x02cf2a89:    mov (%eax),%ecx

0x02cf2a8b:    test %ecx,%ecx

0x02cf2a8d:    je 0x02cf2a9d

0x02cf2a93:    lock cmpxchg %ecx,(%esi)

0x02cf2a97:    jne 0x02cf2d8b


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 70, line = 192


0x02cf2a9d:    mov 0x24(%esp),%esi


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 100, line = 197
            locals ('pendingList', esi, oop) ([1], illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:esi  

0x02cf2aa1:    test %eax,0xf30000

0x02cf2aa7:    mov %esi,%ecx

0x02cf2aa9:    jmp 0x02cf2888


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 100, line = 197


0x02cf2aae:    mov $0x517e818,%ecx

0x02cf2ab3:    lea 0x2c(%esp),%esi

0x02cf2ab7:    mov %ecx,0x4(%esi)

0x02cf2aba:    mov (%ecx),%eax

0x02cf2abc:    mov %eax,%edi

0x02cf2abe:    and $0x7,%edi

0x02cf2ac1:    cmp $0x5,%edi

0x02cf2ac4:    jne 0x02cf2b4c

0x02cf2aca:    mov %eax,(%esi)

0x02cf2acc:    mov 0x4(%ecx),%edi

0x02cf2acf:    mov 0x70(%edi),%edi

0x02cf2ad2:    xor %eax,%edi

0x02cf2ad4:    mov %fs:0x0(,%eiz,1),%eax

0x02cf2adc:    mov -0xc(%eax),%eax

0x02cf2adf:    xor %edi,%eax

0x02cf2ae1:    and $0xffffff87,%eax

0x02cf2ae4:    je 0x02cf2b6d

0x02cf2aea:    test $0x7,%eax

0x02cf2aef:    jne 0x02cf2b40

0x02cf2af1:    test $0x180,%eax

0x02cf2af6:    jne 0x02cf2b1c

0x02cf2af8:    mov (%esi),%eax

0x02cf2afa:    and $0x1ff,%eax

0x02cf2b00:    mov %fs:0x0(,%eiz,1),%edi

0x02cf2b08:    mov -0xc(%edi),%edi

0x02cf2b0b:    or %eax,%edi

0x02cf2b0d:    lock cmpxchg %edi,(%ecx)

0x02cf2b11:    jne 0x02cf2d9c

0x02cf2b17:    jmp 0x02cf2b6d

0x02cf2b1c:    mov 0x4(%ecx),%edi

0x02cf2b1f:    mov 0x70(%edi),%edi

0x02cf2b22:    mov %fs:0x0(,%eiz,1),%eax

0x02cf2b2a:    mov -0xc(%eax),%eax

0x02cf2b2d:    or %eax,%edi

0x02cf2b2f:    mov (%esi),%eax

0x02cf2b31:    lock cmpxchg %edi,(%ecx)

0x02cf2b35:    jne 0x02cf2d9c

0x02cf2b3b:    jmp 0x02cf2b6d

0x02cf2b40:    mov (%esi),%eax

0x02cf2b42:    mov 0x4(%ecx),%edi

0x02cf2b45:    mov 0x70(%edi),%edi

0x02cf2b48:    lock cmpxchg %edi,(%ecx)

0x02cf2b4c:    mov (%ecx),%eax

0x02cf2b4e:    or $0x1,%eax

0x02cf2b51:    mov %eax,(%esi)

0x02cf2b53:    lock cmpxchg %esi,(%ecx)

0x02cf2b57:    je 0x02cf2b6d

0x02cf2b5d:    sub %esp,%eax

0x02cf2b5f:    and $0xfffff003,%eax

0x02cf2b65:    mov %eax,(%esi)

0x02cf2b67:    jne 0x02cf2d9c


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 108, line = 199


0x02cf2b6d:    mov $0x51aa530,%esi

0x02cf2b72:    movb $0x0,0x64(%esi)


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 110, line = 200


0x02cf2b76:    nop

0x02cf2b77:    call 0x02cf2ea2


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 116, line = 201
            locals ('pendingList', illegal) ([1], oop 0x0517e818) ([2], illegal) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:[48]  

0x02cf2b7c:    lea 0x2c(%esp),%eax

0x02cf2b80:    mov 0x4(%eax),%edi

0x02cf2b83:    mov (%edi),%esi

0x02cf2b85:    and $0x7,%esi

0x02cf2b88:    cmp $0x5,%esi

0x02cf2b8b:    je 0x02cf2ba5

0x02cf2b91:    mov (%eax),%esi

0x02cf2b93:    test %esi,%esi

0x02cf2b95:    je 0x02cf2ba5

0x02cf2b9b:    lock cmpxchg %esi,(%edi)

0x02cf2b9f:    jne 0x02cf2dad


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 120, line = 202


0x02cf2ba5:    add $0x38,%esp

0x02cf2ba8:    pop %ebp

0x02cf2ba9:    test %eax,0xf30000

0x02cf2baf:    ret


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 131, line = 203


0x02cf2bb0:    mov %fs:0x0(,%eiz,1),%esi

0x02cf2bb8:    mov -0xc(%esi),%esi

0x02cf2bbb:    mov 0x1f4(%esi),%eax

0x02cf2bc1:    movl $0x0,0x1f4(%esi)

0x02cf2bcb:    movl $0x0,0x1f8(%esi)

0x02cf2bd5:    mov %eax,%esi

0x02cf2bd7:    mov $0x517e818,%eax

0x02cf2bdc:    lea 0x2c(%esp),%eax

0x02cf2be0:    mov 0x4(%eax),%ebx

0x02cf2be3:    mov (%ebx),%edi

0x02cf2be5:    and $0x7,%edi

0x02cf2be8:    cmp $0x5,%edi

0x02cf2beb:    je 0x02cf2c05

0x02cf2bf1:    mov (%eax),%edi

0x02cf2bf3:    test %edi,%edi

0x02cf2bf5:    je 0x02cf2c05

0x02cf2bfb:    lock cmpxchg %edi,(%ebx)

0x02cf2bff:    jne 0x02cf2dbe


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 127, line = 202


0x02cf2c05:    mov %esi,%eax

0x02cf2c07:    jmp 0x02cf2e18


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 130, line = 202


0x02cf2c0c:    mov %fs:0x0(,%eiz,1),%esi

0x02cf2c14:    mov -0xc(%esi),%esi

0x02cf2c17:    mov 0x1f4(%esi),%eax

0x02cf2c1d:    movl $0x0,0x1f4(%esi)

0x02cf2c27:    movl $0x0,0x1f8(%esi)

0x02cf2c31:    mov %eax,%esi

0x02cf2c33:    mov $0x517e818,%eax

0x02cf2c38:    lea 0x2c(%esp),%eax

0x02cf2c3c:    mov 0x4(%eax),%ebx

0x02cf2c3f:    mov (%ebx),%edi

0x02cf2c41:    and $0x7,%edi

0x02cf2c44:    cmp $0x5,%edi

0x02cf2c47:    je 0x02cf2c61

0x02cf2c4d:    mov (%eax),%edi

0x02cf2c4f:    test %edi,%edi

0x02cf2c51:    je 0x02cf2c61

0x02cf2c57:    lock cmpxchg %edi,(%ebx)

0x02cf2c5b:    jne 0x02cf2dcf


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 76, line = 192


0x02cf2c61:    mov %esi,%eax

0x02cf2c63:    jmp 0x02cf2e18


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 78, line = 192


0x02cf2c68:    mov %fs:0x0(,%eiz,1),%esi

0x02cf2c70:    mov -0xc(%esi),%esi

0x02cf2c73:    mov 0x1f4(%esi),%eax

0x02cf2c79:    movl $0x0,0x1f4(%esi)

0x02cf2c83:    movl $0x0,0x1f8(%esi)

0x02cf2c8d:    mov %eax,%esi

0x02cf2c8f:    mov $0x517e818,%eax

0x02cf2c94:    lea 0x2c(%esp),%eax

0x02cf2c98:    mov 0x4(%eax),%ebx

0x02cf2c9b:    mov (%ebx),%edi

0x02cf2c9d:    and $0x7,%edi

0x02cf2ca0:    cmp $0x5,%edi

0x02cf2ca3:    je 0x02cf2cbd

0x02cf2ca9:    mov (%eax),%edi

0x02cf2cab:    test %edi,%edi

0x02cf2cad:    je 0x02cf2cbd

0x02cf2cb3:    lock cmpxchg %edi,(%ebx)

0x02cf2cb7:    jne 0x02cf2de0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 24, line = 179


0x02cf2cbd:    mov %esi,%eax

0x02cf2cbf:    jmp 0x02cf2e18

0x02cf2cc4:    call 0x02ca0700


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 8, line = 176
            locals ([0], illegal) ([1], illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:esi  

0x02cf2cc9:    push $0x712f5d0c

0x02cf2cce:    call 0x02cf2cd3

0x02cf2cd3:    pusha

0x02cf2cd4:    call 0x71031ff0

0x02cf2cd9:    hlt

0x02cf2cda:    mov %esi,0x4(%esp)

0x02cf2cde:    mov %edi,(%esp)

0x02cf2ce1:    call 0x02ca2780


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 8, line = 176
            locals ([0], illegal) ([1], oop 0x0517e818) ([2], illegal) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:esi  [48]  

0x02cf2ce6:    jmp 0x02cf284f

0x02cf2ceb:    lea 0x2c(%esp),%eax

0x02cf2cef:    mov %eax,(%esp)

0x02cf2cf2:    call 0x02ca2b80

0x02cf2cf7:    jmp 0x02cf2888

0x02cf2cfc:    call 0x02ca0700


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 34, line = 182
            locals ('pendingList', illegal) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:ecx  

0x02cf2d01:    push $0x712f5d0c

0x02cf2d06:    call 0x02cf2d0b

0x02cf2d0b:    pusha

0x02cf2d0c:    call 0x71031ff0

0x02cf2d11:    hlt

0x02cf2d12:    mov 0x14(%ecx),%edi

0x02cf2d15:    cmp $0x0,%edi

0x02cf2d18:    je 0x02cf28af

0x02cf2d1e:    mov %edi,(%esp)

0x02cf2d21:    call 0x02ca3b60

0x02cf2d26:    jmp 0x02cf28af

0x02cf2d2b:    mov %ecx,(%esp)

0x02cf2d2e:    call 0x02ca1f80


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 51, line = 186
            locals ('pendingList', illegal) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:[36]  

0x02cf2d33:    push $0x712f5d0c

0x02cf2d38:    call 0x02cf2d3d

0x02cf2d3d:    pusha

0x02cf2d3e:    call 0x71031ff0

0x02cf2d43:    hlt

0x02cf2d44:    call 0x02ca0700


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', illegal) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 



OopMap: 

Oops:[36]  edi  [32]  

0x02cf2d49:    push $0x712f5d0c

0x02cf2d4e:    call 0x02cf2d53

0x02cf2d53:    pusha

0x02cf2d54:    call 0x71031ff0

0x02cf2d59:    hlt

0x02cf2d5a:    mov $0x0,%edx

0x02cf2d5f:    mov $0x5050a00,%eax

0x02cf2d64:    call 0x02ca32a0


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 21, line = 145
                locals ('this', edi, oop) ('x', ecx, oop) 



OopMap: 

Oops:[36]  edi  [32]  ecx  

0x02cf2d69:    jmp 0x02cf2988

0x02cf2d6e:    mov %edx,%edx

0x02cf2d70:    call 0x02ca0b00


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 54, line = 186
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], illegal) ([3], illegal) ([4], illegal) 
                public void clean() @0x0d4cf9d0 of public class jdk.internal.ref.Cleaner [signature Ljava/lang/ref/PhantomReference<Ljava/lang/Object;>;] @0x0d4cfaa0 @ bci = 21, line = 145
                locals ('this', edi, oop) ('x', ecx, oop) 



OopMap: 

Oops:[36]  edi  [32]  ecx  

0x02cf2d75:    jmp 0x02cf2992

0x02cf2d7a:    mov %ecx,0x4(%esp)

0x02cf2d7e:    mov %esi,(%esp)

0x02cf2d81:    call 0x02ca2780


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 62, line = 190
            locals ('pendingList', stack[36], oop) ('ref', illegal) ([2], oop 0x0517e818) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:[36]  ecx  [48]  

0x02cf2d86:    jmp 0x02cf2a6f

0x02cf2d8b:    lea 0x2c(%esp),%eax

0x02cf2d8f:    mov %eax,(%esp)

0x02cf2d92:    call 0x02ca2b80

0x02cf2d97:    jmp 0x02cf2a9d

0x02cf2d9c:    mov %ecx,0x4(%esp)

0x02cf2da0:    mov %esi,(%esp)

0x02cf2da3:    call 0x02ca2780


            private static void processPendingReferences() @0x0d3e9b78 of public abstract class java.lang.ref.Reference [signature <T:Ljava/lang/Object;>Ljava/lang/Object;] @0x0d3ea290 @ bci = 108, line = 199
            locals ('pendingList', illegal) ([1], oop 0x0517e818) ([2], illegal) ([3], illegal) ([4], illegal) 
            monitors (owner = stack[48], oop, lock = stack[44], normal) 



OopMap: 

Oops:ecx  [48]  

0x02cf2da8:    jmp 0x02cf2b6d

0x02cf2dad:    lea 0x2c(%esp),%eax

0x02cf2db1:    mov %eax,(%esp)

0x02cf2db4:    call 0x02ca2b80

0x02cf2db9:    jmp 0x02cf2ba5

0x02cf2dbe:    lea 0x2c(%esp),%eax

0x02cf2dc2:    mov %eax,(%esp)

0x02cf2dc5:    call 0x02ca2b80

0x02cf2dca:    jmp 0x02cf2c05

0x02cf2dcf:    lea 0x2c(%esp),%eax

0x02cf2dd3:    mov %eax,(%esp)

0x02cf2dd6:    call 0x02ca2b80

0x02cf2ddb:    jmp 0x02cf2c61

0x02cf2de0:    lea 0x2c(%esp),%eax

0x02cf2de4:    mov %eax,(%esp)

0x02cf2de7:    call 0x02ca2b80

0x02cf2dec:    jmp 0x02cf2cbd

0x02cf2df1:    nop

0x02cf2df2:    nop

0x02cf2df3:    mov %fs:0x0(,%eiz,1),%esi

0x02cf2dfb:    mov -0xc(%esi),%esi

0x02cf2dfe:    mov 0x1f4(%esi),%eax

0x02cf2e04:    movl $0x0,0x1f4(%esi)

0x02cf2e0e:    movl $0x0,0x1f8(%esi)

0x02cf2e18:    add $0x38,%esp

0x02cf2e1b:    pop %ebp

0x02cf2e1c:    jmp 0x02c9fc80

0x02cf2e21:    hlt

0x02cf2e22:    hlt

0x02cf2e23:    hlt

0x02cf2e24:    hlt

0x02cf2e25:    hlt

0x02cf2e26:    hlt

0x02cf2e27:    hlt

0x02cf2e28:    hlt

0x02cf2e29:    hlt

0x02cf2e2a:    hlt

0x02cf2e2b:    hlt

0x02cf2e2c:    hlt

0x02cf2e2d:    hlt

0x02cf2e2e:    hlt

0x02cf2e2f:    hlt

0x02cf2e30:    hlt

0x02cf2e31:    hlt

0x02cf2e32:    hlt

0x02cf2e33:    hlt

0x02cf2e34:    hlt

0x02cf2e35:    hlt

0x02cf2e36:    hlt

0x02cf2e37:    hlt

0x02cf2e38:    hlt

0x02cf2e39:    hlt

0x02cf2e3a:    hlt

0x02cf2e3b:    hlt

0x02cf2e3c:    hlt

0x02cf2e3d:    hlt

0x02cf2e3e:    hlt

0x02cf2e3f:    hlt

[Stub Code] 0x02cf2e40:    nop

0x02cf2e41:    nop

0x02cf2e42:    mov $0xd3e9a00,%ebx

0x02cf2e47:    jmp 0x02bf4a89

0x02cf2e4c:    nop

0x02cf2e4d:    nop

0x02cf2e4e:    mov $0xd3e9920,%ebx

0x02cf2e53:    jmp 0x02bf4e89

0x02cf2e58:    nop

0x02cf2e59:    nop

0x02cf2e5a:    mov $0x0,%ebx

0x02cf2e5f:    jmp 0x02cf2e5f

0x02cf2e64:    nop

0x02cf2e65:    nop

0x02cf2e66:    mov $0x0,%ebx

0x02cf2e6b:    jmp 0x02cf2e6b

0x02cf2e70:    nop

0x02cf2e71:    nop

0x02cf2e72:    mov $0x0,%ebx

0x02cf2e77:    jmp 0x02cf2e77

0x02cf2e7c:    nop

0x02cf2e7d:    nop

0x02cf2e7e:    mov $0x0,%ebx

0x02cf2e83:    jmp 0x02cf2e83

0x02cf2e88:    nop

0x02cf2e89:    nop

0x02cf2e8a:    mov $0x0,%ebx

0x02cf2e8f:    jmp 0x02cf2e8f

0x02cf2e94:    nop

0x02cf2e95:    nop

0x02cf2e96:    mov $0x0,%ebx

0x02cf2e9b:    jmp 0x02cf2e9b

0x02cf2ea0:    nop

0x02cf2ea1:    nop

0x02cf2ea2:    mov $0xd3c1090,%ebx

0x02cf2ea7:    jmp 0x02be7c0c

[Exception Handler] 0x02cf2eac:    mov $0xdead,%ebx

0x02cf2eb1:    mov $0xdead,%ecx

0x02cf2eb6:    mov $0xdead,%esi

0x02cf2ebb:    mov $0xdead,%edi

0x02cf2ec0:    call 0x02ca1b80

0x02cf2ec5:    push $0x712f5d0c

0x02cf2eca:    call 0x02cf2ecf

0x02cf2ecf:    pusha

0x02cf2ed0:    call 0x71031ff0

0x02cf2ed5:    hlt

0x02cf2ed6:    push $0x2cf2ed6

0x02cf2edb:    jmp 0x02be5fa0

