package org.leibnizcenter.cfg.earleyparser.callbacks;

public class ParserCallbacksBuilder<T> {
    private ScanProbability<T> scanProbability;

    private ParseCallback<T> onPostPredict;
    private ParseCallback<T> onPostScan;
    private ParseCallback<T> onPostComplete;

    private ParseCallback<T> onPrePredict;
    private ParseCallback<T> onPreScan;
    private ParseCallback<T> onPreComplete;

    public ParserCallbacksBuilder<T> withScanProbability(ScanProbability<T> scanProbability) {
        this.scanProbability = scanProbability;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPostPredict(ParseCallback<T> onPostPredict) {
        this.onPostPredict = onPostPredict;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPostScan(ParseCallback<T> onPostScan) {
        this.onPostScan = onPostScan;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPostComplete(ParseCallback<T> onPostComplete) {
        this.onPostComplete = onPostComplete;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPrePredict(ParseCallback<T> onPrePredict) {
        this.onPrePredict = onPrePredict;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPreScan(ParseCallback<T> onPreScan) {
        this.onPreScan = onPreScan;
        return this;
    }

    public ParserCallbacksBuilder<T> withOnPreComplete(ParseCallback<T> onPreComplete) {
        this.onPreComplete = onPreComplete;
        return this;
    }

    public ParseCallbacks<T> build() {
        return new ParseCallbacks<>(
                scanProbability,
                onPostPredict,
                onPostScan,
                onPostComplete,
                onPrePredict,
                onPreScan,
                onPreComplete
        );
    }
}
