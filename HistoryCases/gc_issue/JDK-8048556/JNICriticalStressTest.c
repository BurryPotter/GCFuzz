#include <stdio.h>
#include <stdlib.h>
#include "JNICriticalStressTest.h"

JNIEXPORT void JNICALL
Java_JNICriticalStressTest_transformArrays(JNIEnv *env, jclass the_class,
                                           jintArray prevObj,
                                           jintArray currObj) {
  jint* prev = NULL;
  jboolean prevIsCopy;
  jint* curr = NULL;
  jboolean currIsCopy;

  if (prevObj == NULL) {
    printf("warning: prevObj == NULL\n");
    return;
  }

  if (currObj == NULL) {
    printf("warning: currObj == NULL\n");
    return;
  }

  jsize prevLen = (*env)->GetArrayLength(env, prevObj);
  jsize currLen = (*env)->GetArrayLength(env, currObj);

  if (prevLen != currLen) {
    printf("warning: prevLen %d != currLen %d\n", prevLen, currLen);
    return;
  }

  prev = (*env)->GetPrimitiveArrayCritical(env, prevObj, &prevIsCopy);
  if (prev == NULL) {
    printf("warning: prev == NULL\n");
    return;
  }

  curr = (*env)->GetPrimitiveArrayCritical(env, currObj, &currIsCopy);
  if (curr == NULL) {
    printf("warning: curr == NULL\n");
    (*env)->ReleasePrimitiveArrayCritical(env, prevObj, prev, JNI_ABORT);
    return;
  }

  jint* prevEnd = prev + prevLen;
  jint* currEnd = curr + currLen;
  jint* p = NULL;
  jint* c = NULL;

  for (p = prev, c = curr;
       p < prevEnd && c < currEnd;
       p += 1, c += 1) {
    *c += *p;
  }

  for (p = prev, c = curr;
       p < prevEnd && c < currEnd;
       p += 1, c += 1) {
    *p += *c;
  }

  (*env)->ReleasePrimitiveArrayCritical(env, prevObj, prev, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, currObj, curr, 0);
}
