package com.netifi.acmeair.jmeter;

import org.apache.jmeter.samplers.SampleResult;
import reactor.core.publisher.Mono;

public class ReactiveSampleResult extends SampleResult {
    private static final long serialVersionUID = 1L;

    private boolean isValid;
    private Mono<Void> executionResult;

    void setExecutionResult(Mono<Void> executionResult) {
        this.executionResult = executionResult;
    }

    Mono<Void> getExecutionResult() {
        return this.executionResult;
    }


    boolean isValid() {
        return isValid;
    }

    void setValid(boolean valid) {
        isValid = valid;
    }
}
