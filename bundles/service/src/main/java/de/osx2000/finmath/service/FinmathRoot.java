package de.osx2000.finmath.service;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationBase;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;

@Component(service = Application.class)

@JaxrsName(FinmathRoot.APPLICATION_NAME)
@JaxrsApplicationBase(FinmathRoot.APPLICATION_BASE)
public class FinmathRoot extends Application {
    public static final String APPLICATION_NAME = "FinmathRoot";
    public static final String APPLICATION_BASE = "/finmath";
}
