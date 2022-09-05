package it.peps.cartracking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import org.hibernate.criterion.Order;


@ToString()
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TrackingData {

    public String id;
    public String state;

    public ReturnValue returnvalue;


    @Data
    public static class ReturnValue {

        public Result result;

    }

    @Data
    public static class Result {

        public Order order;

    }

    @Data
    public static class Order {

        public String vmacsStatusDesc;
        public String vmacsStatusDate;
        public String vmacs2CharCode;
        public String vmacs3CharCode;
        public String type;
        public String number;
        public String gobStatusDesc;
        public String gobStatusDate;
        public String gobStatusCode;
        public String contractSource;


    }

}
