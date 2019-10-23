package de.osx2000.finmath.api;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.Arrays;
import java.util.List;

public interface OpenApiService {

    default OpenAPI getOpenApis() throws Exception {

        return getOpenApis(null);
    }

    OpenAPI getOpenApis(List<String> basePaths) throws Exception;

}