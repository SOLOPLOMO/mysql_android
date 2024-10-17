cmake_minimum_required(VERSION 3.4.1)

include_directories(node/)
add_library(
             native-lib
             SHARED
             native-lib.cpp )

set_target_properties( # Specifies the target library.
        native-lib
        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION
        # Provides the path to the library you want to import.
        ${CMAKE_SOURCE_DIR}/../libnode/${ANDROID_ABI}/libnode.so )

find_library(log-lib log)
target_link_libraries(native-lib ${log-lib} ${CMAKE_SOURCE_DIR}/../libnode/${ANDROID_ABI}/libnode.so)
