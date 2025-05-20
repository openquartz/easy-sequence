package com.openquartz.sequence.core.expr.persist.model;

import com.openquartz.sequence.core.expr.cmd.AssignExtParam;
import lombok.Getter;

/**
 * FetchSequenceRequestBuilder
 *
 * @author svnee
 **/
public class FetchSequenceRequestBuilder {

    private FetchSequenceRequest fetchSequenceRequest;

    private FetchSequenceRequestBuilder() {
    }

    public static FetchSequenceRequestBuilder builder(String registerCode) {
        FetchSequenceRequestBuilder builder = new FetchSequenceRequestBuilder();
        builder.fetchSequenceRequest = new FetchSequenceRequest(registerCode);
        return builder;
    }

    public FetchSequenceRequestBuilder param(AssignExtParam param) {
        this.fetchSequenceRequest.setParam(param);
        return this;
    }

    public FetchSequenceRequestBuilder step(long step) {
        this.fetchSequenceRequest.setStep(step);
        return this;
    }

    public FetchSequenceRequestBuilder fetchCount(int fetchCount) {
        this.fetchSequenceRequest.setFetchCount(fetchCount);
        return this;
    }

    public FetchSequenceRequestBuilder wait(boolean wait) {
        this.fetchSequenceRequest.setWait(wait);
        return this;
    }

    public FetchSequenceRequest build() {
        return fetchSequenceRequest;
    }

    /**
     * fetch sequence request
     *
     * @author svnee
     */
    @Getter
    public static class FetchSequenceRequest {

        /**
         * seq code
         */
        private final String seqCode;
        /**
         * param
         */
        private AssignExtParam param = AssignExtParam.EMPTY_PARAM;
        /**
         * next value step
         */
        private long step = 1;
        /**
         * fetch count
         */
        private int fetchCount = 1;
        /**
         * wait fetch success
         */
        private boolean wait = true;

        private FetchSequenceRequest(String seqCode) {
            this.seqCode = seqCode;
        }

        private void setParam(AssignExtParam param) {
            this.param = param;
        }

        private void setStep(long step) {
            this.step = step;
        }

        private void setFetchCount(int fetchCount) {
            this.fetchCount = fetchCount;
        }

        private void setWait(boolean wait) {
            this.wait = wait;
        }
    }
}
