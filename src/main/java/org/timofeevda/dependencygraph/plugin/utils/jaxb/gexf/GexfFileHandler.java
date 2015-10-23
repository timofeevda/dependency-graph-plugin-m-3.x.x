package org.timofeevda.dependencygraph.plugin.utils.jaxb.gexf;

import net.gexf._1.GexfContent;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 *
 * @author timofeevda
 */
public class GexfFileHandler {

    protected static final String JAXB_CONTEXT_PATH = "net.gexf._1";

    /**
     * Read GEXF file
     *
     * @throws JAXBException, FileNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static GexfContent read(File file) throws JAXBException, FileNotFoundException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
        Unmarshaller u = jc.createUnmarshaller();
        return ((JAXBElement<GexfContent>) u.unmarshal(new FileInputStream(file))).getValue();
    }

    /**
     * Write GEXF file
     *
     * @throws JAXBException, FileNotFoundException
     */
    public static void write(GexfContent gexf, File file) throws JAXBException, FileNotFoundException {
        gexf.setVersion("1.2");
        JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        m.marshal(GexfElementFactory.OBJECT_FACTORY.createGexf(gexf), new FileOutputStream(file));
    }

}
