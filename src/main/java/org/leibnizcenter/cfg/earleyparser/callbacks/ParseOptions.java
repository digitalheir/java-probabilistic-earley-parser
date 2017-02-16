package org.leibnizcenter.cfg.earleyparser.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Callbacks for parsing
 * <p>
 * Created by maarten on 16/01/17.
 */
@SuppressWarnings("WeakerAccess")
public class ParseOptions<T> {
    public final ScanProbability<T> scanProbability;

    public final ParseCallback<T> onPrePredict;
    public final ParseCallback<T> onPostPredict;

    public final ParseCallback<T> onPreScan;
    //public final ParseCallback<T> onScanFailNoTokenFound;
    public final ParseCallback<T> onPostScan;

    public final ParseCallback<T> onPreComplete;
    public final ParseCallback<T> onPostComplete;
    public final ScanMode scanMode;

//    public final boolean parallelizePredict;
//    public final boolean parallelizeScan;
//    public final boolean parallelizeComplete;

    public ParseOptions(
            ScanProbability<T> scanProbability,

            ParseCallback<T> onPostPredict,
            ParseCallback<T> onPostScan,
            ParseCallback<T> onPostComplete,

            ParseCallback<T> onPrePredict,
            ParseCallback<T> onPreScan,
            ParseCallback<T> onPreComplete,

            ScanMode scanMode/*,

            boolean parallelizePredict,
            boolean parallelizeScan,
            boolean parallelizeComplete*/
    ) {
        this.scanProbability = scanProbability;

        this.onPostPredict = onPostPredict;
        this.onPostScan = onPostScan;
        this.onPostComplete = onPostComplete;

        this.onPrePredict = onPrePredict;
        this.onPreScan = onPreScan;
        this.onPreComplete = onPreComplete;

        this.scanMode = scanMode;

//        this.parallelizePredict = parallelizePredict;
//        this.parallelizeScan = parallelizeScan;
//        this.parallelizeComplete = parallelizeComplete;
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

        private ScanMode scanMode;
//        private boolean parallelizePredict = false;
//        private boolean parallelizeScan = false;
//        private boolean parallelizeComplete = false;

        public Builder<T> withScanProbability(ScanProbability<T> scanProbability) {
            this.scanProbability = scanProbability;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPostPredict(ParseCallback<T> onPostPredict) {
            this.onPostPredict = onPostPredict;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPostScan(ParseCallback<T> onPostScan) {
            this.onPostScan = onPostScan;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPostComplete(ParseCallback<T> onPostComplete) {
            this.onPostComplete = onPostComplete;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPrePredict(ParseCallback<T> onPrePredict) {
            this.onPrePredict = onPrePredict;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPreScan(ParseCallback<T> onPreScan) {
            this.onPreScan = onPreScan;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T> onPreComplete(ParseCallback<T> onPreComplete) {
            this.onPreComplete = onPreComplete;
            return this;
        }

        public Builder<T> withScanMode(ScanMode scanMode) {
            this.scanMode = scanMode;
            return this;
        }

        public ParseOptions<T> build() {
            return new ParseOptions<>(
                    scanProbability,
                    onPostPredict,
                    onPostScan,
                    onPostComplete,
                    onPrePredict,
                    onPreScan,
                    onPreComplete,
                    scanMode
            );
        }

//        public Builder<T> parallelizeScan() {
//            this.parallelizeScan = true;
//            return this;
//        }
//
//        public Builder<T> parallelizePredict() {
//            this.parallelizePredict = true;
//            return this;
//        }
//
//        public Builder<T> parallelizeComplete() {
//            this.parallelizeComplete = true;
//            return this;
//        }
    }
}
