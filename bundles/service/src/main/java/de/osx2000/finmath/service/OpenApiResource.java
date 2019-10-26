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
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ObjectClassDefinition(name = "Service Root Configuration")
@interface OpenApiJaxrsConfiguration {
    String serverAddress() default "/rest";

    String swaggerBasePath() default "/services/rest";

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

    private OpenAPIConfiguration openAPIConfiguration;

    @Activate
    private void activate(OpenApiJaxrsConfiguration openApiJaxrsConfiguration) {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info()
                .title(openApiJaxrsConfiguration.swaggerTitle())
                .description(openApiJaxrsConfiguration.swaggerDescription())
                .contact(new Contact()
                        .email(openApiJaxrsConfiguration.swaggerContact()))
                ;

        openAPI.info(info);

        openAPIConfiguration = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true);

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

    protected Response getOpenApi(HttpHeaders headers, ServletConfig config, Application app, UriInfo uriInfo, String type) throws Exception {

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
        Map<String, List<String>> output = new HashMap();
        if (params != null) {
            Iterator var2 = params.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                List<String> values = (List)params.get(key);
                output.put(key, values);
            }
        }

        return output;
    }

    private static Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap();
        if (headers != null) {
            Iterator var2 = headers.getCookies().keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                Cookie cookie = (Cookie)headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }

        return output;
    }

    private static Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap();
        if (headers != null) {
            Iterator var2 = headers.getRequestHeaders().keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                List<String> values = (List)headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }

        return output;
    }
}
