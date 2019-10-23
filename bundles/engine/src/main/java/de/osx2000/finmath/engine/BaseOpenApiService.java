//package de.osx2000.finmath.engine;
//
//import de.osx2000.finmath.api.OpenApiService;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
//import org.osgi.service.metatype.annotations.ObjectClassDefinition;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component(service = OpenApiService.class)
//public class BaseOpenApiService implements OpenApiService {
//
//    @ObjectClassDefinition
//    @interface Config {
//
//        public boolean someProperty() default false;
//    }
//
//    private Config config;
//
//    @Activate
//    private void activate(Config config) {
//        this.config = config;
//
//    };
//
//    @Reference
//    private JaxrsServiceRuntime jaxrsServiceRuntime;
//
//    @Override
//    public OpenAPI getOpenApis(List<String> basePaths)  {
//
//
//        final List<OpenAPI> openAPIs = new ArrayList<>();
//
//        for (String basePath : basePaths) {
//
//            final List<OpenAPI> basePathApiList = new ArrayList<>();
//            ;
//            for (OpenApiFragmentsService fragmentsService : apiAppenderServices) {
//
//                basePathApiList.add(fragmentsService.getFragmentOpenApi(basePath));
//            }
//
//            OpenAPI pathOpenAPI = mergerService.merge(basePathApiList);
//
//            openAPIs.add(pathOpenAPI);
//        }
//
//        return mergeOpenApis(openAPIs, filterTagTypes);
//    }
//}
