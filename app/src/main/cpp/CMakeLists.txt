cmake_minimum_required(VERSION 3.22.1)

project("hardware")

aux_source_directory(. SOURCE_DIR)
add_library(hardware SHARED ${SOURCE_DIR})

find_library(log-lib log)

target_link_libraries(hardware

        EGL
        GLESv3
        android
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})