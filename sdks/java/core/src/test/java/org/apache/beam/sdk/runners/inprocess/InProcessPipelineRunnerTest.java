/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.runners.inprocess;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.runners.inprocess.InProcessPipelineRunner.InProcessPipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;

/**
 * Tests for basic {@link InProcessPipelineRunner} functionality.
 */
@RunWith(JUnit4.class)
public class InProcessPipelineRunnerTest implements Serializable {
  @Test
  public void wordCountShouldSucceed() throws Throwable {
    Pipeline p = getPipeline();

    PCollection<KV<String, Long>> counts =
        p.apply(Create.of("foo", "bar", "foo", "baz", "bar", "foo"))
            .apply(MapElements.via(new SimpleFunction<String, String>() {
              @Override
              public String apply(String input) {
                return input;
              }
            }))
            .apply(Count.<String>perElement());
    PCollection<String> countStrs =
        counts.apply(MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
          @Override
          public String apply(KV<String, Long> input) {
            String str = String.format("%s: %s", input.getKey(), input.getValue());
            return str;
          }
        }));

    PAssert.that(countStrs).containsInAnyOrder("baz: 1", "bar: 2", "foo: 3");

    InProcessPipelineResult result = ((InProcessPipelineResult) p.run());
    result.awaitCompletion();
  }

  private Pipeline getPipeline() {
    PipelineOptions opts = PipelineOptionsFactory.create();
    opts.setRunner(InProcessPipelineRunner.class);

    Pipeline p = Pipeline.create(opts);
    return p;
  }
}
