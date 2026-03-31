#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_movie_1project_util_ApiKeyProvider_getApiKey(JNIEnv *env, jobject /* this */) {
    std::string api_key = "1dd5fc6831acffaa5cb5999a57c389c7";
    return env->NewStringUTF(api_key.c_str());
}
