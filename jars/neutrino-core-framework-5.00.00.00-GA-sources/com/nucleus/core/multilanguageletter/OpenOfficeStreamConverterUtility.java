package com.nucleus.core.multilanguageletter;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import ooo.connector.BootstrapSocketConnector;


@Named("openOfficeConverterUtility")
public class OpenOfficeStreamConverterUtility {
	
	@Value("${openoffice.executable.directory.location}")
    private String openOfficeExecutableDirectoryLocation;
	
	
    public byte[] convert(OpenOfficeInputStream input, OpenOfficeOutputStream output, String filterName) throws BootstrapException,com.sun.star.uno.Exception  {
    	XComponentContext xComponentContext = BootstrapSocketConnector.bootstrap(openOfficeExecutableDirectoryLocation);
                
        XMultiComponentFactory xMultiComponentFactory = xComponentContext.getServiceManager();
        Object desktopService = xMultiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xComponentContext);
        XComponentLoader xComponentLoader = UnoRuntime.queryInterface(XComponentLoader.class, desktopService);
        
        PropertyValue[] conversionProperties = new PropertyValue[2];
        conversionProperties[0] = new PropertyValue();
        conversionProperties[1] = new PropertyValue();

        conversionProperties[0].Name = "InputStream";
        conversionProperties[0].Value = input;
        conversionProperties[1].Name = "Hidden";
        conversionProperties[1].Value = Boolean.TRUE;

        XComponent document = xComponentLoader.loadComponentFromURL("private:stream", "_blank", 0, conversionProperties);
        conversionProperties[0] = new PropertyValue();
        conversionProperties[1] = new PropertyValue();
        
        conversionProperties[0].Name = "OutputStream";
        conversionProperties[0].Value = output;
        conversionProperties[1].Name = "FilterName";
        conversionProperties[1].Value = filterName;

        XStorable xstorable = UnoRuntime.queryInterface(XStorable.class,document);
        xstorable.storeToURL("private:stream", conversionProperties);
        
        return output.toByteArray();
    }

    
}

