/**
 * ***************************************************************************** Copyright (c) 2013
 * IBM Corp.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.netifi.acmeair.jmeter.functions;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateDateFunction extends AbstractFunction {

  private static final Logger log = LoggerFactory.getLogger(GenerateDateFunction.class);

  private static final List<String> DESC = Arrays.asList("date");
  private static final String KEY = "__generateDate";

  private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT
      = ThreadLocal.withInitial(() -> new SimpleDateFormat("EEE MMM dd 00:00:00 z yyyy"));
  
  private List<CompoundVariable> parameters = Collections.emptyList();

  @Override
  public String execute(SampleResult arg0, Sampler arg1) {
    log.debug("Generate Date for input {} {}", arg0, arg1);
    if (parameters.get(0).execute().equalsIgnoreCase("from")) {
      Calendar aDay = Calendar.getInstance();
      aDay.add(Calendar.DATE, ThreadLocalRandom.current().nextInt(6));
      return DATE_FORMAT.get().format(aDay.getTime());
    } else if (parameters.get(0).execute().equalsIgnoreCase("return")) {
      Calendar aDay = Calendar.getInstance();
      aDay.add(Calendar.DATE, ThreadLocalRandom.current().nextInt(7) + 6);
      return DATE_FORMAT.get().format(aDay.getTime());
    }
    return "";
  }

  @Override
  public String getReferenceKey() {
    return KEY;
  }

  @Override
  public void setParameters(Collection<CompoundVariable> arg0) throws InvalidVariableException {
    parameters = new ArrayList<CompoundVariable>(arg0);
  }

  public List<String> getArgumentDesc() {
    return DESC;
  }
}
