LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := native-lib.cpp
LOCAL_LDFLAGS   += -Wl,-z,max-page-size=16384

include $(BUILD_SHARED_LIBRARY)
