package de.osx2000.finmath.service;

import io.swagger.v3.core.filter.OpenAPISpecFilter;
import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.ServletConfigContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ObjectClassDefinition(name = "Service Root Configuration")
@interface OpenApiJaxrsConfiguration {
  
    String swaggerDescription() default "Service REST API";

    String swaggerTitle() default "My Service";

    String swaggerContact() default "oschweitzer@me.com";
}
@Designate(ocd = OpenApiJaxrsConfiguration.class)
@Path("/openapi.{type:json|yaml}")
@Component(service = OpenApiResource.class)
@JaxrsName("OpenApiResource")
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + FinmathRoot.APPLICATION_NAME + ")")
public class OpenApiResource {
    @Context
    ServletConfig config;

    @Context
    Application app;

    private OpenApiJaxrsConfiguration openApiJaxrsConfiguration;

    @Activate
    private void activate(OpenApiJaxrsConfiguration openApiJaxrsConfiguration) {
        this.openApiJaxrsConfiguration = openApiJaxrsConfiguration;
    }

    @GET
    @Produces({"application/json", "application/yaml"})
    @Operation(
            hidden = true
    )
    public Response getOpenApi(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("type") String type) throws Exception {
        return getOpenApi(headers, this.config, this.app, uriInfo, type);
    }

    private static Logger LOGGER = LoggerFactory.getLogger(OpenApiResource.class);

    private Response getOpenApi(HttpHeaders headers, ServletConfig config, Application app, UriInfo uriInfo, String type) throws Exception {

        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info()
                .title(openApiJaxrsConfiguration.swaggerTitle())
                .description(openApiJaxrsConfiguration.swaggerDescription())
                .contact(new Contact()
                        .email(openApiJaxrsConfiguration.swaggerContact()))
                );
        openAPI.addServersItem(new Server().url(uriInfo.getBaseUri().toString()));

        OpenAPIConfiguration openAPIConfiguration = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true);

        String ctxId = ServletConfigContextUtils.getContextIdFromServletConfig(config);

        OpenApiContext ctx = (new JaxrsOpenApiContextBuilder())
                .servletConfig(config)
                .application(app)
                .openApiConfiguration(openAPIConfiguration)
                .ctxId(ctxId)
                .buildContext(true);

        OpenAPI oas = ctx.read();
        boolean pretty = false;
        if (ctx.getOpenApiConfiguration() != null && Boolean.TRUE.equals(ctx.getOpenApiConfiguration().isPrettyPrint())) {
            pretty = true;
        }

        if (oas != null && ctx.getOpenApiConfiguration() != null && ctx.getOpenApiConfiguration().getFilterClass() != null) {
            try {
                OpenAPISpecFilter filterImpl = (OpenAPISpecFilter)Class.forName(ctx.getOpenApiConfiguration().getFilterClass()).newInstance();
                SpecFilter f = new SpecFilter();
                oas = f.filter(oas, filterImpl, getQueryParams(uriInfo.getQueryParameters()), getCookies(headers), getHeaders(headers));
            } catch (Exception var12) {
                LOGGER.error("failed to load filter", var12);
            }
        }

        if (oas == null) {
            return Response.status(404).build();
        } else {
            return StringUtils.isNotBlank(type) && type.trim().equalsIgnoreCase("yaml") ? Response.status(Response.Status.OK).entity(pretty ? Yaml.pretty(oas) : Yaml.mapper().writeValueAsString(oas)).type("application/yaml").build() : Response.status(Response.Status.OK).entity(pretty ? Json.pretty(oas) : Json.mapper().writeValueAsString(oas)).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    private static Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<>();
        if (params != null) {
            params.keySet().forEach(key -> {
                List<String> values = params.get(key);
                output.put(key, values);
            });
        }

        return output;
    }

    private static Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<>();
        if (headers != null) {
            headers.getCookies().keySet().forEach(key -> {
                Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            });
        }

        return output;
    }

    private static Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<>();
        if (headers != null) {
            headers.getRequestHeaders().keySet().forEach(key -> {
                List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            });
        }
        return output;
    }
}
