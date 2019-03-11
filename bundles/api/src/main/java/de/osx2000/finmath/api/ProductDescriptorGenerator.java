package de.osx2000.finmath.api;

import java.io.Serializable;
import java.util.Map;

public interface ProductDescriptorGenerator {
    Map<String, Serializable> getProductDescriptor(
            String productType,
            long id,
            long formatVersion
    );
}
