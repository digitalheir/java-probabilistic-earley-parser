package org.leibnizcenter.cfg.earleyparser.parse.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Callbacks for parsing
 * <p>
 * Created by maarten on 16/01/17.
 */
public class ParseCallbacks {
    public final ScanProbability scanProbability;
    public final OnPostPredict onPostPredict;
    public final OnPostScan onPostScan;
    public final OnPostComplete onPostComplete;

    public ParseCallbacks(ScanProbability scanProbability, OnPostPredict onPostPredict, OnPostScan onPostScan, OnPostComplete onPostComplete) {
        this.scanProbability = scanProbability;
        this.onPostPredict = onPostPredict;
        this.onPostScan = onPostScan;
        this.onPostComplete = onPostComplete;
    }

    public <T> void onPredict(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostPredict != null) onPostPredict.onPredict(i, token, chart);
    }

    public <T> void onScan(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostScan != null) onPostScan.onScan(i, token, chart);
    }

    public <T> void onComplete(int i, TokenWithCategories<T> token, Chart<T> chart) {
        if (this.onPostComplete != null) onPostComplete.onComplete(i, token, chart);
    }

    public static class Builder {
        private ScanProbability scanProbability;
        private OnPostPredict onPostPredict;
        private OnPostScan onPostScan;
        private OnPostComplete onPostComplete;

        public Builder withScanProbability(ScanProbability scanProbability) {
            this.scanProbability = scanProbability;
            return this;
        }

        public Builder withOnPostPredict(OnPostPredict onPostPredict) {
            this.onPostPredict = onPostPredict;
            return this;
        }

        public Builder withOnPostScan(OnPostScan onPostScan) {
            this.onPostScan = onPostScan;
            return this;
        }

        public Builder withOnPostComplete(OnPostComplete onPostComplete) {
            this.onPostComplete = onPostComplete;
            return this;
        }


        public ParseCallbacks build() {
            return new ParseCallbacks(scanProbability, onPostPredict, onPostScan, onPostComplete);
        }
    }
}
