package org.finsible.backend.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.zalando.logbook.*;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LogbookConfiguration {
    @Bean
   public Logbook logbook() {
        return Logbook.builder()
                .condition(Conditions.exclude(Conditions.requestTo("/actuator/**")))
                .sink(new DefaultSink(new CustomJsonHttpLogFormatter(), new DefaultHttpLogWriter()))
                .build();
   }

    private static class CustomJsonHttpLogFormatter implements HttpLogFormatter {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Map<String,String> requestMap = new HashMap<>();

        @Override
        public String format(@NonNull Precorrelation precorrelation, @NonNull HttpRequest request) {
            try {
                String requestDetails=objectMapper.writeValueAsString(Map.of(
                        "method", request.getMethod(),
                        "url", request.getPath()
                ));
                requestMap.put(precorrelation.getId(), requestDetails);

                return "";
            } catch (Exception e) {
                return "Error formatting request log: " + e.getMessage();
            }
        }

        @Override
        public String format(@NonNull Correlation correlation, @NonNull HttpResponse response) {
            try {
                Map<String,String> requestDetails=objectMapper.readValue(requestMap.getOrDefault(correlation.getId(),"{}"), Map.class);
                return objectMapper.writeValueAsString(Map.of(
                        "method", requestDetails.get("method"),
                        "url",requestDetails.get("url"),
                        "status", response.getStatus(),
                        "responseTime", correlation.getDuration().toMillis() + "ms"
                ));
            } catch (Exception e) {
                return "Error formatting response log: " + e.getMessage();
            }
        }
    }
}
