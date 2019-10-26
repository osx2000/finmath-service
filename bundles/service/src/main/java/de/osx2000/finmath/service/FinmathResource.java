package de.osx2000.finmath.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.osx2000.finmath.api.ProductDescriptorGenerator;
import de.osx2000.finmath.api.SettlementValuation;
import de.osx2000.finmath.api.SettlementValuationOracleFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import net.finmath.smartcontract.contract.SmartDerivativeContractSchedule;
import net.finmath.smartcontract.contract.SmartDerivativeContractScheduleGenerator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component(service = FinmathResource.class)
@OpenAPIDefinition()
@JaxrsName("FinmathResource")
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + FinmathRoot.APPLICATION_NAME + ")")
public class FinmathResource {

    @Reference
    private SettlementValuationOracleFactory settlementValuationOracleFactory;

    @Reference
    private ProductDescriptorGenerator productDescriptorGenerator;

    private final static Gson gson = new GsonBuilder().create();

    private static LocalDateTime LDTfromString(String yyyyMMdd) {
        return LocalDateTime.parse(yyyyMMdd, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    private static LocalTime LTfromString(String yyyyMMdd) {
        return LocalTime.parse(yyyyMMdd, DateTimeFormatter.ISO_LOCAL_TIME);
    }
    private static LocalDate LDfromString(String yyyyMMdd) {
        return LocalDate.parse(yyyyMMdd, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @GET
    @Path("/settlementvaluation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response settlementvaluation(
            @QueryParam(value = "contractUID")
                    String contractUID,
            @QueryParam(value = "periodStart") String periodStart,
            @QueryParam(value = "periodEnd") String periodEnd
    ) {
        /*
         * Create oracle on the fly and get margin. This is efficient since we do cache the valuation oracle.
         */

        LocalDateTime start = LDTfromString(periodStart);
        LocalDateTime end = LDTfromString(periodEnd);

        Double value = settlementValuationOracleFactory.getSettlementValuationOracle(contractUID).getMargin(start, end);

        return Response.ok(gson.toJson(new SettlementValuation(contractUID, start, end, value))).build();
    }

    @GET
    @Path("/productdescriptor")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductDescriptor(
            @QueryParam(value = "productType")
                    String productType,
            @QueryParam(value = "id") long id,
            @QueryParam(value = "formatVersion") long formatVersion
    ) {
        return Response.ok(gson.toJson(productDescriptorGenerator.getProductDescriptor(productType, id, formatVersion))).build();
    }

    @GET
    @Path("/smartderivativecontractschedule")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSmartDerivativeContractSchedule(
            @QueryParam(value = "startDate")
                    String startDate,
            @QueryParam(value = "maturityDate")
                    String maturityDate,
            @QueryParam(value = "settlementTime")
                    String settlementTime,
            @QueryParam(value = "accountAccessAllowedSeconds") long accountAccessAllowedSeconds
    ) {
        Duration accountAccessAllowedDuration = Duration.ofSeconds(accountAccessAllowedSeconds);

        SmartDerivativeContractSchedule schedule = SmartDerivativeContractScheduleGenerator.getScheduleForBusinessDays("target2", LDfromString(startDate), LDfromString(maturityDate), LTfromString(settlementTime), accountAccessAllowedDuration);
        return Response.ok(gson.toJson(schedule.getEventTimes())).build();
    }

}
