package br.com.cobranca.banks.caixa;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Utilitários de escaping/parsing de XML usados pela integração com a Caixa
 * (sem libs externas).
 */
final class CaixaXmlUtil {

    private static final String PERMITIDOS = "ABCDEFGHIJKLMNOPQRSTUVXYZW0123456789,/()*&\"=-+!:?<>.;_ ";

    private CaixaXmlUtil() {
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }

        // 1) remove acentos/diacríticos via decomposição Unicode (á -> a, ç -> c, ã -> a...)
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        // 2) minúsculas -> maiúsculas
        String maiusculo = semAcento.toUpperCase(Locale.ROOT);

        // 3) qualquer caractere fora do conjunto admitido vira espaço
        StringBuilder resultado = new StringBuilder(maiusculo.length());
        for (int i = 0; i < maiusculo.length(); i++) {
            char c = maiusculo.charAt(i);
            resultado.append(PERMITIDOS.indexOf(c) >= 0 ? c : ' ');
        }
        return resultado.toString().trim();
    }

    /**
     * Retorna o texto do primeiro filho direto {@code childLocalName} do
     * primeiro elemento {@code parentLocalName} encontrado no documento.
     * <p>
     * Diferente de {@link #text}, que pega a PRIMEIRA ocorrência da tag em
     * QUALQUER nível do XML, este método é usado quando a mesma tag existe em
     * mais de um lugar do documento com significados diferentes — caso clássico
     * do &lt;COD_RETORNO&gt; da Caixa, que aparece tanto no nível SIBAR
     * (transporte/barramento) quanto dentro de
     * &lt;DADOS&gt;&lt;CONTROLE_NEGOCIAL&gt; (negócio/SIGCB). Usar
     * {@code text()} para o segundo caso sempre devolvia o valor do SIBAR,
     * mascarando rejeições de negócio mesmo quando o transporte deu certo.
     */
    public static String textUnder(Document doc, String parentLocalName, String childLocalName) {
        NodeList candidates = doc.getElementsByTagName(parentLocalName);
        Node parent = candidates.getLength() > 0 ? candidates.item(0) : null;
        if (parent == null) {
            // tenta localizar o pai por sufixo (ex.: prefixo de namespace)
            NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                Node n = all.item(i);
                if (n.getNodeName().equals(parentLocalName) || n.getNodeName().endsWith(":" + parentLocalName)) {
                    parent = n;
                    break;
                }
            }
        }
        if (parent == null) {
            return null;
        }

        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(childLocalName) || child.getNodeName().endsWith(":" + childLocalName)) {
                return child.getTextContent();
            }
        }
        return null;
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            // proteção básica contra XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("XML de resposta da Caixa inválido: " + e.getMessage(), e);
        }
    }

    /*
     * Retorna o texto do primeiro elemento com o nome local informado (ignora
     * namespaces/prefixos).
     */
    static String text(Document doc, String localName) {
        NodeList list = doc.getElementsByTagName(localName);
        if (list.getLength() == 0) {
            // tenta localizar por sufixo (ex.: sibar_base:HEADER vs HEADER)
            list = doc.getElementsByTagName("*");
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeName().equals(localName) || n.getNodeName().endsWith(":" + localName)) {
                    return n.getTextContent();
                }
            }
            return null;
        }
        return list.item(0).getTextContent();
    }

    public static String xpathText(Document doc, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toXmlString(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar XML da Caixa: " + e.getMessage(), e);
        }
    }
}
