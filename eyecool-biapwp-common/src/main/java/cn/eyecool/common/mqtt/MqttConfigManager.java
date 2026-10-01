package cn.eyecool.common.mqtt;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MqttConfigManager {
    private static Document document = null;
    private static MqttConfigManager mqttConfigManager = null;

    private MqttConfigManager() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            String filePath = System.getProperty("user.dir") + "/config/mqttConfig.xml";
            File outFile = new File(filePath);
            if (outFile.isFile() && outFile.exists()) {
                document = db.parse(new FileInputStream(filePath));
            } else {
                document = db.parse(MqttConfigManager.class.getResourceAsStream("/mqttConfig.xml"));
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static MqttConfigManager getMqttConfigManagerInstance() {
        if (mqttConfigManager == null) {
            mqttConfigManager = new MqttConfigManager();
        }

        return mqttConfigManager;
    }

    public static String getItemValue(String textlabel) {
        getMqttConfigManagerInstance();
        NodeList list = document.getElementsByTagName(textlabel);
        String result = "";
        if (list != null && list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); ++i) {
                Element element = (Element)list.item(i);
                result = result + element.getTextContent() + ",";
            }

            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}