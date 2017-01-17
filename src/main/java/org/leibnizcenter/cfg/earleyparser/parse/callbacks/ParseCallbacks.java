package org.leibnizcenter.cfg.earleyparser.parse.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Callbacks for parsing
 * <p>
 * Created by maarten on 16/01/17.
 */
@SuppressWarnings("WeakerAccess")
public class ParseCallbacks {
    public final ScanProbability scanProbability;

    public final ParseCallback onPostPredict;
    public final ParseCallback onPostScan;
    public final ParseCallback onPostComplete;

    public final ParseCallback onPrePredict;
    public final ParseCallback onPreScan;
    public final ParseCallback onPreComplete;

    public ParseCallbacks(
            ScanProbability scanProbability,

            ParseCallback onPostPredict,
            ParseCallback onPostScan,
            ParseCallback onPostComplete,

            ParseCallback onPrePredict,
            ParseCallback onPreScan,
            ParseCallback onPreComplete

    ) {
        this.scanProbability = scanProbability;

        this.onPostPredict = onPostPredict;
        this.onPostScan = onPostScan;
        this.onPostComplete = onPostComplete;

        this.onPrePredict = onPrePredict;
        this.onPreScan = onPreScan;
        this.onPreComplete = onPreComplete;
    }

    public <T> void onPredict(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostPredict != null) onPostPredict.on(i, token, chart);
    }

    public <T> void onScan(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostScan != null) onPostScan.on(i, token, chart);
    }

    public <T> void onComplete(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostComplete != null) onPostComplete.on(i, token, chart);
    }

    public <T> void beforePredict(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPrePredict != null) onPostComplete.on(i, token, chart);
    }

    public <T> void beforeScan(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPreScan != null) onPostComplete.on(i, token, chart);
    }

    public <T> void beforeComplete(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPreComplete != null) onPostComplete.on(i, token, chart);
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private ScanProbability scanProbability;

        private ParseCallback onPostPredict;
        private ParseCallback onPostScan;
        private ParseCallback onPostComplete;

        private ParseCallback onPrePredict;
        private ParseCallback onPreScan;
        private ParseCallback onPreComplete;

        public Builder withScanProbability(ScanProbability scanProbability) {
            this.scanProbability = scanProbability;
            return this;
        }

        public Builder withOnPostPredict(ParseCallback onPostPredict) {
            this.onPostPredict = onPostPredict;
            return this;
        }

        public Builder withOnPostScan(ParseCallback onPostScan) {
            this.onPostScan = onPostScan;
            return this;
        }

        public Builder withOnPostComplete(ParseCallback onPostComplete) {
            this.onPostComplete = onPostComplete;
            return this;
        }

        public Builder withOnPrePredict(ParseCallback onPrePredict) {
            this.onPrePredict = onPrePredict;
            return this;
        }

        public Builder withOnPreScan(ParseCallback onPreScan) {
            this.onPreScan = onPreScan;
            return this;
        }

        public Builder withOnPreComplete(ParseCallback onPreComplete) {
            this.onPreComplete = onPreComplete;
            return this;
        }

        public ParseCallbacks build() {
            return new ParseCallbacks(
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
