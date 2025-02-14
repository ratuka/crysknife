/*
 * Copyright 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.crysknife.exception;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

/**
 * Used to indicate that some part of a multi-step process failed. Typically, operation can continue
 * after this exception is caught.
 *
 * <p>
 * Before throwing an object of this type, the thrower
 *
 * <ul>
 * <li>must log a detailed user-facing message describing the failure,
 * <li>must report any caught exception using the logger that contributed to the failure, and
 * <li>must not include the cause of the failure in the thrown exception because (1) it will already
 * have been associated with the detailed log entry above and (2) doing so would create a
 * misunderstanding of how to find the causes of low-level errors in that sometimes there is an
 * underlying an exception, sometimes not, but there can <em>always</em> be a preceding log entry.
 * </ul>
 *
 * <p>
 * After catching an object of this type, the catcher
 *
 * <ul>
 * <li>can be assured that the thrower has already logged a message about the lower-level problem
 * <li>can optionally itself log a higher-level description of the process that was interrupted and
 * the implications of the failure, and if so,
 * <li>should report this caught exception via the logger as well.
 * </ul>
 *
 * <pre>
 * void lowLevel(Logger logger)throws UnableToCompleteException{try{doSomethingThatMightFail();catch(SomeException e){
 * // Log low-level detail and the caught exception.
 * //
 * logger.log("detailed problem explanation for user eyes...",e);
 *
 * // Do not include the caught exception.
 * //
 * throw new UnableToCompleteException();}}
 *
 * void highLevel(Logger logger){try{
 * // Multiple calls are shown to indicate that the process can
 * // include any number of steps.
 * //
 * lowLevel(logger);lowLevel(logger);lowLevel(logger);}catch(UnableToCompleteException e){logger.log("high-level thing failed",e);}}
 * </pre>
 */
public class UnableToCompleteException extends Exception {

  public Set<UnableToCompleteException> errors;

  public UnableToCompleteException(String msg, Exception e) {
    super(msg, e);
  }

  public UnableToCompleteException(Exception e) {
    super(e);
  }

  public UnableToCompleteException(String e) {
    super(e);
  }

  public UnableToCompleteException(Set<UnableToCompleteException> e) {
    this.errors = e;
  }

  public UnableToCompleteException() {}

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(getMessage()).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (!(o instanceof UnableToCompleteException))
      return false;

    UnableToCompleteException that = (UnableToCompleteException) o;

    return new EqualsBuilder().append(getMessage(), that.getMessage()).isEquals();
  }
}
