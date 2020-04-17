package com.nucleus.xml.parser.stats;

import com.nucleus.xml.document.PoolableDocumentBuilderFactoryImpl;
import com.nucleus.xml.parser.PoolableSAXParserFactoryImpl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by gajendra.jatav on 5/7/2019.
 */
@Controller
@RequestMapping(value = "/XmlParserPool")
public class XmlParserPoolStats {


    @ResponseBody
    @RequestMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public String stats() {
        return PoolableDocumentBuilderFactoryImpl.getObjectPool().getStats()+
                "  "+ PoolableSAXParserFactoryImpl.getObjectPool().getStats();
    }


}
