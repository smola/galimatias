/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestURL {

    public String rawURL;
    public String rawBaseURL;
    public URL parsedURL;
    public URL parsedBaseURL;

    @Override
    public String toString() {
        return String.format("TestURL(rawURL=%s, rawBaseURL=%s, parsedURL=%s, parsedBaseURL=%s)",
                rawURL, rawBaseURL, parsedURL, parsedBaseURL);
    }

    public abstract class DATASETS {
        public static final String WHATWG = "/data/urltestdata_whatwg.txt";
        public static final String HOST_WHATWG = "/data/urltestdata_host_whatwg.txt";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @ParametersSuppliedBy(TestURLSupplier.class)
    public static @interface TestURLs {
        String dataset() default DATASETS.WHATWG;
    }

    public static class TestURLSupplier extends ParameterSupplier {

        private static final Map<String,List<TestURL>> datasetMap = new HashMap<String,List<TestURL>>();

        @Override
        public List<PotentialAssignment> getValueSources(final ParameterSignature sig) {
            final TestURLs ref = sig.getAnnotation(TestURLs.class);
            final String dataset = ref.dataset();
            if (!datasetMap.containsKey(dataset)) {
                datasetMap.put(dataset, TestURLLoader.loadTestURLs(dataset));
            }
            final List<PotentialAssignment> values = new ArrayList<PotentialAssignment>();
            for (final TestURL testURL : datasetMap.get(dataset)) {
                values.add(PotentialAssignment.forValue(testURL.toString(), testURL));
            }
            return values;
        }
    }

}
