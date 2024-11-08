#include <jni.h>

#ifndef _Included_GetArrayCritical
#define _Included_GetArrayCritical
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     GetArrayCritical
 * Method:    get_critical_array
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_GetArrayCritical_get_1critical_1array
  (JNIEnv *env, jclass cls) {
  jarray arr1 = (*env)->NewByteArray(env, 8);
  jbyte* buffer = (*env)->GetPrimitiveArrayCritical(env, arr1, 0); 
}

#ifdef __cplusplus
}
#endif
#endif
