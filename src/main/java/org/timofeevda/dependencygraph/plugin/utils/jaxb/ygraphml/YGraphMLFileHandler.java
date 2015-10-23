package org.timofeevda.dependencygraph.plugin.utils.jaxb.ygraphml;


import com.yworks.xml.graphml.ShapeNodeType;
import org.graphdrawing.graphml.xmlns.GraphmlType;
import org.graphdrawing.graphml.xmlns.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author dtimofeev
 * @version 06.10.2015
 */
public class YGraphMLFileHandler {

    public static void write(GraphmlType graphmlType, File file) throws JAXBException, FileNotFoundException {
        JAXBContext jc = JAXBContext.newInstance(GraphmlType.class, ShapeNodeType.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        m.marshal((new ObjectFactory()).createGraphml(graphmlType), new FileOutputStream(file));
    }

}
