package org.leibnizcenter.cfg.earleyparser.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Callbacks for parsing
 * <p>
 * Created by maarten on 16/01/17.
 */
@SuppressWarnings("WeakerAccess")
public class ParseCallbacks<T> {
    public final ScanProbability<T> scanProbability;

    public final ParseCallback<T> onPostPredict;
    public final ParseCallback<T> onPostScan;
    public final ParseCallback<T> onPostComplete;

    public final ParseCallback<T> onPrePredict;
    public final ParseCallback<T> onPreScan;
    public final ParseCallback<T> onPreComplete;

    public ParseCallbacks(
            ScanProbability<T> scanProbability,

            ParseCallback<T> onPostPredict,
            ParseCallback<T> onPostScan,
            ParseCallback<T> onPostComplete,

            ParseCallback<T> onPrePredict,
            ParseCallback<T> onPreScan,
            ParseCallback<T> onPreComplete

    ) {
        this.scanProbability = scanProbability;

        this.onPostPredict = onPostPredict;
        this.onPostScan = onPostScan;
        this.onPostComplete = onPostComplete;

        this.onPrePredict = onPrePredict;
        this.onPreScan = onPreScan;
        this.onPreComplete = onPreComplete;
    }

    public void onPredict(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostPredict != null) onPostPredict.on(i, token, chart);
    }

    public void onScan(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostScan != null) onPostScan.on(i, token, chart);
    }

    public void onComplete(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostComplete != null) onPostComplete.on(i, token, chart);
    }

    public void beforePredict(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPrePredict != null) onPostComplete.on(i, token, chart);
    }

    public void beforeScan(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPreScan != null) onPostComplete.on(i, token, chart);
    }

    public void beforeComplete(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPreComplete != null) onPostComplete.on(i, token, chart);
    }

    public static class Builder<T> {
        private ScanProbability<T> scanProbability;

        private ParseCallback<T> onPostPredict;
        private ParseCallback<T> onPostScan;
        private ParseCallback<T> onPostComplete;

        private ParseCallback<T> onPrePredict;
        private ParseCallback<T> onPreScan;
        private ParseCallback<T> onPreComplete;

        public Builder<T> withScanProbability(ScanProbability<T> scanProbability) {
            this.scanProbability = scanProbability;
            return this;
        }

        public Builder<T> withOnPostPredict(ParseCallback<T> onPostPredict) {
            this.onPostPredict = onPostPredict;
            return this;
        }

        public Builder<T> withOnPostScan(ParseCallback<T> onPostScan) {
            this.onPostScan = onPostScan;
            return this;
        }

        public Builder<T> withOnPostComplete(ParseCallback<T> onPostComplete) {
            this.onPostComplete = onPostComplete;
            return this;
        }

        public Builder<T> withOnPrePredict(ParseCallback<T> onPrePredict) {
            this.onPrePredict = onPrePredict;
            return this;
        }

        public Builder<T> withOnPreScan(ParseCallback<T> onPreScan) {
            this.onPreScan = onPreScan;
            return this;
        }

        public Builder<T> withOnPreComplete(ParseCallback<T> onPreComplete) {
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
}
