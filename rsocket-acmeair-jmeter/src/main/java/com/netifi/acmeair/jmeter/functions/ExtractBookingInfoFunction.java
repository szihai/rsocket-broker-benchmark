package com.netifi.acmeair.jmeter.functions;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import com.netifi.acmeair.jmeter.BookingContext;

import java.util.*;

public class ExtractBookingInfoFunction extends AbstractFunction {
  private static final String BOOKING_CONTEXT_VAR = "booking.context";
  private static final String KEY = "__extractBookingInfo";
  private static final List<String> DESC = Arrays.asList("extract_booking_info");
  private List<CompoundVariable> parameters = Collections.emptyList();

  @Override
  public String execute(SampleResult previousResult, Sampler currentSampler)
      throws InvalidVariableException {
    JMeterVariables variables = currentSampler.getThreadContext().getVariables();
    String value = parameters.get(0).execute();
    String retVal = "";
    BookingContext context = (BookingContext) variables.getObject(BOOKING_CONTEXT_VAR);
  
    if ("UNSET".equalsIgnoreCase(value)) {
      variables.remove(BOOKING_CONTEXT_VAR);
    }
    
    if (context != null) {
      if ("BOOKING_IN".equalsIgnoreCase(value)) {
        retVal = context.getBookingIds()[context.getCounter()];
      }
  
      if ("NUMBER_OF_BOOKINGS".equalsIgnoreCase(value)) {
        retVal = String.valueOf(context.getNumberOfBookings());
      }
  
      if ("NUMBER_TO_CANCEL".equalsIgnoreCase(value)) {
        retVal =  String.valueOf(context.getNumberToCancel());
      }
  
    }
    return retVal;
  }

  @Override
  public String getReferenceKey() {
    return KEY;
  }

  @Override
  public void setParameters(Collection<CompoundVariable> arg0) throws InvalidVariableException {
    parameters = new ArrayList<>(arg0);
  }

  public List<String> getArgumentDesc() {
    return DESC;
  }
}
