/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.config.graphql;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestLoggingInstrumentation extends SimplePerformantInstrumentation {

  public static final Logger logger = LoggerFactory.getLogger(
    RequestLoggingInstrumentation.class
  );

  @Autowired
  private HttpServletRequest httpServletRequest;

  @Value("org.entur.lamassu.graphql.instrumentation.extract-header-name")
  private String extractHeaderName;

  @Override
  public @NotNull InstrumentationContext<ExecutionResult> beginExecution(
    InstrumentationExecutionParameters parameters
  ) {
    long startMillis = System.currentTimeMillis();
    var executionId = parameters.getExecutionInput().getExecutionId();

    final String extractHeaderValue = httpServletRequest.getHeader(extractHeaderName);

    logger.debug(
      "[{}] {}: {},  Query: {}",
      executionId,
      extractHeaderName,
      extractHeaderValue,
      parameters.getQuery()
    );
    if (parameters.getVariables() != null && !parameters.getVariables().isEmpty()) {
      logger.info(
        "[{}] {}: {}, variables: {}",
        executionId,
        extractHeaderName,
        extractHeaderValue,
        parameters.getVariables()
      );
    }

    return SimpleInstrumentationContext.whenCompleted(
      (
        (executionResult, throwable) -> {
          long endMillis = System.currentTimeMillis();
          long duration = endMillis - startMillis;
          if (throwable == null) {
            logger.debug(
              "[{}] {}: {}, completed in {}ms",
              executionId,
              extractHeaderName,
              extractHeaderValue,
              duration
            );
          } else {
            logger.warn("Failed in: {} ", duration, throwable);
          }
        }
      )
    );
  }
}
