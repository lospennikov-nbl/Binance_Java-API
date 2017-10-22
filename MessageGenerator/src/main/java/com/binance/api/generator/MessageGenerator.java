package com.binance.api.generator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.binance.api.generator.BinanceXMLAttributes.*;

class MessageGenerator {
  private static String outPath;

  private static void parseFile(Path path) throws IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      ErrorHandler handler = new MessageXMLParseErrorHandler();
      builder.setErrorHandler(handler);
      Document document = builder.parse(path.toFile());
      document.getDocumentElement().normalize();

      Element head = document.getDocumentElement();
      String packageName = head.getAttribute(PACKAGE);
      if (packageName.isEmpty()) {
        throw new MessageParseException("Package is not specified");
      }
      NodeList children = head.getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if (head.getTagName().equals(MESSAGES_XML) && node.getNodeName().equals(MESSAGE)) {
            handleMessage(node, packageName);
          } else if (head.getTagName().equals(ENUMS_XML) && node.getNodeName().equals(ENUM)) {
            handleEnum();
          } else {
            throw new MessageParseException("Message xml can contain only messages and enum xml can contain only enums");
          }
        }
      }

    } catch (ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }
  }

  private static void handleMessage(Node node, String packageName) throws IOException {
    Element element = (Element) node;
    Map<String, String> mandatory = new HashMap<>();
    Map<String, String> optional = new HashMap<>();
    String name = element.getAttribute(NAME);
    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    String address = element.getAttribute(ADDRESS);

    checkPresence(name, "Missing name of message");
    checkPresence(address, "Missing Binance api address");

    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node innerNode = children.item(i);
      if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
        if (innerNode.getNodeName().equals(PARAMETER)) {
          insertMessage(innerNode, mandatory);
        } else if (innerNode.getNodeName().equals(OPTIONAL) && optional.isEmpty()) {
          handleOptional(innerNode, optional);
        } else {
          throw new MessageParseException("Message can contain only parameters and only one nested optional element");
        }
      }
    }

    List<String> fieldsList = new ArrayList<>();
    for (Map.Entry<String, String> fieldEntry : mandatory.entrySet()) {
      fieldsList.add(String.format("private final %s %s;", fieldEntry.getValue(), fieldEntry.getKey()));
      fieldsList.add("");
    }
    for (Map.Entry<String, String> fieldEntry : optional.entrySet()) {
      fieldsList.add(String.format("private %s %s = null;", fieldEntry.getValue(), fieldEntry.getKey()));
      fieldsList.add("");
    }

    List<String> mandatoryConstructor = new ArrayList<>();
    StringBuilder constructorBuilder = new StringBuilder("public ").append(name).append('(');
    StringBuilder paramsBuilder = new StringBuilder();

    for (Map.Entry<String, String> fieldEntry : mandatory.entrySet()) {
      constructorBuilder.append(fieldEntry.getValue()).append(' ').append(fieldEntry.getKey()).append(", ");
      paramsBuilder.append(fieldEntry.getKey()).append(", ");
    }
    StringBuilder constructorBuilderOptional = new StringBuilder(constructorBuilder);
    if (!mandatory.isEmpty()) {
      constructorBuilder.delete(constructorBuilder.length() - ", ".length(), constructorBuilder.length());
      paramsBuilder.delete(paramsBuilder.length() - ", ".length(), paramsBuilder.length());
    }

    constructorBuilder.append(") {");
    mandatoryConstructor.add(constructorBuilder.toString());
    for (String fieldName : mandatory.keySet()) {
      mandatoryConstructor.add(String.format("this.%s = %s;", fieldName, fieldName));
    }
    mandatoryConstructor.add("}");

    List<String> optionalConstructor = new ArrayList<>();

    if (optional.keySet().size() == 1) {
      optionalConstructor.add("");
      for (Map.Entry<String, String> fieldEntry : optional.entrySet()) {
        constructorBuilderOptional.append(fieldEntry.getValue()).append(' ').append(fieldEntry.getKey()).append(") {");
        optionalConstructor.add(constructorBuilderOptional.toString());
        optionalConstructor.add(String.format("this(%s);", paramsBuilder.toString()));
        optionalConstructor.add(String.format("this.%s = %s;", fieldEntry.getKey(), fieldEntry.getKey()));
      }
      optionalConstructor.add("}");
    } else if (!optional.keySet().isEmpty()) {
      optionalConstructor.add("");
      constructorBuilderOptional.append("Map<String, Object> optionalParams) {");
      optionalConstructor.add(constructorBuilderOptional.toString());
      optionalConstructor.add(String.format("this(%s);", paramsBuilder.toString()));
      for (Map.Entry<String, String> fieldEntry : optional.entrySet()) {
        optionalConstructor.add(String.format("if (optionalParams.containsKey(\"%s\")) {", fieldEntry.getKey()));
        optionalConstructor.add(String.format("this.%s = (%s) optionalParams.get(\"%s\");", fieldEntry.getKey(),
            fieldEntry.getValue(), fieldEntry.getKey()));
        optionalConstructor.add("}");
      }
      optionalConstructor.add("}");
    }

    List<String> getters = new ArrayList<>();
    List<String> setters = new ArrayList<>();

    for (Map.Entry<String, String> fieldEntry : mandatory.entrySet()) {
      String methodName = Character.toUpperCase(fieldEntry.getKey().charAt(0)) + fieldEntry.getKey().substring(1);

      getters.add("");
      getters.add(String.format("public %s get%s() {", fieldEntry.getValue(), methodName));
      getters.add("return " + fieldEntry.getKey() + ";");
      getters.add("}");
    }

    for (Map.Entry<String, String> fieldEntry : optional.entrySet()) {
      String methodName = Character.toUpperCase(fieldEntry.getKey().charAt(0)) + fieldEntry.getKey().substring(1);

      getters.add("");
      getters.add(String.format("public %s get%s() {", fieldEntry.getValue(), methodName));
      getters.add("return this." + fieldEntry.getKey() + ";");
      getters.add("}");

      setters.add("");
      setters.add(String.format("public void set%s(%s %s) {", methodName, fieldEntry.getValue(), fieldEntry.getKey()));
      setters.add(String.format("this.%s = %s;", fieldEntry.getKey(), fieldEntry.getKey()));
      setters.add("}");
    }

    try (PrintWriter out = new PrintWriter(createFile(String.format("%s.java", name), outPath, packageName))) {
      CodeWriter writer = new CodeWriter(out)
          .write("package " + packageName + ";",
              "",
              "import java.util.Map;",
              "",
              "public class " + name + " {",
              "")
          .write(fieldsList)
          .write(mandatoryConstructor)
          .write(optionalConstructor);
      if (!getters.isEmpty()) {
        writer.write(getters).write(setters);
      }
      writer.write("}");
    }
  }

  private static void handleOptional(Node innerNode, Map<String, String> map) throws MessageParseException {
    NodeList optionalParams = innerNode.getChildNodes();
    for (int j = 0; j < optionalParams.getLength(); j++) {
      Node optionalNode = optionalParams.item(j);
      if (optionalNode.getNodeType() == Node.ELEMENT_NODE) {
        if (optionalNode.getNodeName().equals(PARAMETER)) {
          insertMessage(optionalNode, map);
        } else {
          throw new MessageParseException("Optional element can contain only parameters");
        }
      }
    }
  }

  private static void insertMessage(Node innerNode, Map<String, String> map) throws MessageParseException {
    Element parameter = (Element) innerNode;
    String fieldName = parameter.getAttribute(NAME);
    String fieldType = parameter.getAttribute(TYPE);
    checkPresence(fieldName, "Missing name of parameter");
    checkPresence(fieldType, "Missing parameter type");
    map.put(fieldName, fieldType);
  }

  private static void checkPresence(String param, String message) throws MessageParseException {
    if (param.isEmpty()) {
      throw new MessageParseException(message);
    }
  }


  private static void handleEnum() {

  }

  private static File createFile(String name, String path, String packageName) throws IOException {
    StringBuilder destination = new StringBuilder(path).append(packageName.replaceAll("\\.", File.separator))
        .append(File.separator).append(name);
    File outFile = Paths.get(destination.toString()).toFile();
    Files.createDirectories(Paths.get(outFile.getParent()));

    if (!outFile.createNewFile()) {
      outFile.delete();
      outFile.createNewFile();
    }
    return outFile;
  }

  public static void main(String[] args) throws IOException {
    String inPath = args[0].replaceAll("/", File.separator);
    outPath = args[1];
    try (Stream<Path> stream = Files.walk(Paths.get(inPath))) {
      Optional<IOException> optional = stream
          .filter(Files::isRegularFile)
          .flatMap(path -> {
            try {
              parseFile(path);
              return null;
            } catch (IOException e) {
              return Stream.of(e);
            }
          })
          .reduce((ex1, ex2) -> {
            ex1.addSuppressed(ex2);
            return ex1;
          });
      if (optional.isPresent()) {
        throw optional.get();
      }
    }
    Function<Integer, Integer> a = b -> b;
  }

}
