/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clearspring.analytics.stream.frequency;

/**
 * A version of the TinyLFU sketch based on a regular conservative update sketch. The difference is
 * that any time the sum of counters reach a predefined values we divide all counters by 2 in what
 * is called a reset operation.
 * <p>
 * For more details see <a href="http://www.cs.technion.ac.il/~gilga/TinyLFU_PDP2014.pdf">TinyLFU:
 * A Highly Efficient Cache Admission Policy</a>.
 * <p>
 * The CountMinSketch parameters are described in
 * <a href="https://github.com/twitter/algebird/blob/develop/algebird-core/src/main/scala/com/twitter/algebird/CountMinSketch.scala">
 * Twitter's implementation</a>.
 *
 * @author gilga1983@gmail.com (Gilga Einziger)
 */
public class CountMinTinyLfu extends ConservativeAddSketch {
  final int sampleSize;
  int nrItems;

  public CountMinTinyLfu(int depth, int width, int seed, int samplesize) {
    super(depth, width, seed);
    sampleSize = samplesize;
    nrItems = 0;
  }

  public CountMinTinyLfu(double epsOfTotalCount, double confidence, int seed, int samplesize) {
    super(epsOfTotalCount, confidence, seed);
    sampleSize = samplesize;
    nrItems = 0;
  }

  @Override
  public void add(long item, long count) {
    if (estimateCount(item) < 10) {
      super.add(item, count);
    }
    nrItems += count;
    resetIfNeeded();
  }

  @Override
  public void add(String item, long count) {
    if (estimateCount(item) < 10) {
      super.add(item, count);
    }
    nrItems += count;
    resetIfNeeded();
  }

  private void resetIfNeeded() {
    if (nrItems > sampleSize) {
      nrItems /= 2;
      for (int i = 0; i < depth; i++) {
        for (int j = 0; j < width; j++) {
          nrItems -= table[i][j] & 1;
          table[i][j] >>>= 1;
        }
      }
    }
  }
}
