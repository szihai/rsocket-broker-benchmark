package com.netifi.acmeair.jmeter;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.jmeter.samplers.SampleResult;

public class ProtobufSampleResult extends SampleResult {

    private GeneratedMessageV3 message = Empty.getDefaultInstance();

    public GeneratedMessageV3 getMessage() {
        return message;
    }

    public void setMessage(GeneratedMessageV3 message) {
        this.message = message;
    }

    @Override
    public long getBodySizeAsLong() {
        return message.getSerializedSize();
    }

    @Override
    public byte[] getResponseData() {
        return message.toByteArray();
    }
}
