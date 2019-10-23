//package de.osx2000.finmath.service;
//
//
//import de.osx2000.finmath.api.OpenApiService;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.servers.Server;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
//import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
//import org.osgi.service.jaxrs.runtime.dto.RuntimeDTO;
//import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
//import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
//import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
//import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
//
//import javax.ws.rs.*;
//import javax.ws.rs.core.*;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Component(service = OpenApiResourceXXX.class)
//@JaxrsName("OpenApiResourceXXX")
//@JaxrsResource
//@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + FinmathRoot.APPLICATION_NAME + ")")
//public class OpenApiResourceXXX {
//
//    public static final String BASEPATH = "/doc";
//
//    @Reference
//    private JaxrsServiceRuntime jaxrsServiceRuntime;
//
//    @Reference
//    private OpenApiService openApiService;
//
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/application")
//    public ApplicationBaseDTO[] getApplication() {
//
//        final List<ApplicationBaseDTO> applications = new ArrayList<>();
//
//        final RuntimeDTO runtimeDTO = jaxrsServiceRuntime.getRuntimeDTO();
//
//        if (runtimeDTO.applicationDTOs != null) {
//            for (final ApplicationDTO applicationDTO : runtimeDTO.applicationDTOs) {
//                ApplicationBaseDTO abDTO = new ApplicationBaseDTO();
//                abDTO.base = applicationDTO.base;
//                abDTO.name = applicationDTO.name;
//                applications.add(abDTO);
//            }
//        }
//
//        return applications.toArray(new ApplicationBaseDTO[] {});
//    }
//
//    @GET
//    @Produces({ MediaType.APPLICATION_JSON, "application/yaml", MediaType.APPLICATION_XML })
//    @Path("/application/{application}/{type:json|yaml|xml}")
//    public Response getOpenApi(@Context final HttpHeaders headers, @Context final UriInfo uriInfo,
//                               @PathParam("type") final String type, @PathParam("application") final String application,
//                               ) {
//
//
//        OpenAPI openAPI;
//        try {
//            openAPI = getOpenApi(uriInfo, Arrays.asList(application));
//            if (openAPI == null) {
//                return Response.status(404).build();
//            }
//        } catch (Exception e) {
//            return Response.status(500).build();
//        }
//
//        String responseType;
//        if ("yaml".equalsIgnoreCase(type)) {
//            responseType = "application/yaml";
//        } else if ("xml".equalsIgnoreCase(type)) {
//            responseType = MediaType.APPLICATION_XML;
//        } else {
//            responseType = MediaType.APPLICATION_JSON;
//        }
//
//        return Response.status(Response.Status.OK).entity(openAPI).type(responseType).build();
//    }
//
//    @GET
//    @Produces({ MediaType.APPLICATION_JSON, "application/yaml", MediaType.APPLICATION_XML })
//    @Path("/group/")
//    public Response getOpenApi(@Context final HttpHeaders headers, @Context final UriInfo uriInfo,
//                               @QueryParam("application") final List<String> applications) {
//
//        OpenAPI openAPI;
//        try {
//            openAPI = getOpenApi(uriInfo, applications);
//            if (openAPI == null) {
//                return Response.status(404).build();
//            }
//        } catch (Exception e) {
//            return Response.status(500).build();
//        }
//
//        return Response.status(Response.Status.OK).entity(openAPI).type(MediaType.APPLICATION_JSON).build();
//    }
//
//    private OpenAPI getOpenApi(final UriInfo uriInfo, final List<String> applications)
//            throws Exception {
//        OpenAPI openAPI;
//
//        openAPI = openApiService.getOpenApis(applications);
//
//        if (openAPI == null) {
//            return null;
//
//        }
//
//        List<Server> servers = openAPI.getServers();
//        if (servers == null) {
//            servers = new ArrayList<>();
//        }
//        final Server server = new Server();
//        final URI baseurl = uriInfo.getBaseUri();
//        server.setUrl(baseurl.getScheme() + "://" + baseurl.getAuthority());
//        servers.add(server);
//        openAPI.setServers(servers);
//        return openAPI;
//    }
//
//}
