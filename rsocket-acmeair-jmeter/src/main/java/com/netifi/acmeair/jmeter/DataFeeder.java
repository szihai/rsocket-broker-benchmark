package com.netifi.acmeair.jmeter;

import com.google.protobuf.GeneratedMessageV3;

public interface DataFeeder<T extends GeneratedMessageV3> {

    T next();
}
