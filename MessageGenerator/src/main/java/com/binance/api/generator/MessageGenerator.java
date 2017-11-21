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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.binance.api.generator.BinanceXMLAttributes.ADDRESS;
import static com.binance.api.generator.BinanceXMLAttributes.BEAN;
import static com.binance.api.generator.BinanceXMLAttributes.BEANS_XML;
import static com.binance.api.generator.BinanceXMLAttributes.ENUM;
import static com.binance.api.generator.BinanceXMLAttributes.ENUMS_XML;
import static com.binance.api.generator.BinanceXMLAttributes.FIELD;
import static com.binance.api.generator.BinanceXMLAttributes.MESSAGE;
import static com.binance.api.generator.BinanceXMLAttributes.MESSAGES_XML;
import static com.binance.api.generator.BinanceXMLAttributes.NAME;
import static com.binance.api.generator.BinanceXMLAttributes.OPTIONAL;
import static com.binance.api.generator.BinanceXMLAttributes.PACKAGE;
import static com.binance.api.generator.BinanceXMLAttributes.PARAMETER;
import static com.binance.api.generator.BinanceXMLAttributes.PROPERTY;
import static com.binance.api.generator.BinanceXMLAttributes.SIGNED;
import static com.binance.api.generator.BinanceXMLAttributes.TYPE;
import static com.binance.api.generator.BinanceXMLAttributes.VALUE;

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
      boolean isSigned = Boolean.parseBoolean(head.getAttribute(SIGNED));
      NodeList children = head.getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if (head.getTagName().equals(MESSAGES_XML) && node.getNodeName().equals(MESSAGE)) {
            handleMessage(node, packageName, isSigned);
          } else if (head.getTagName().equals(ENUMS_XML) && node.getNodeName().equals(ENUM)) {
            handleEnum(node, packageName);
          } else if (head.getTagName().equals(BEANS_XML) && node.getNodeName().equals(BEAN)) {
            handleBean(node, packageName);
          } else {
            throw new MessageParseException("Message xml can contain only messages and enum xml can contain only enums");
          }
        }
      }

    } catch (ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }
  }

  private static void handleMessage(Node node, String packageName, boolean isSigned) throws IOException {
    Element element = (Element) node;
    Map<String, String> mandatory = new HashMap<>();
    Map<String, String> optional = new HashMap<>();
    String baseName = element.getAttribute(NAME);
    String name = Character.toUpperCase(baseName.charAt(0)) + baseName.substring(1);
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

    boolean isStatic = mandatory.isEmpty() && optional.isEmpty();

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
    StringBuilder constructorBuilder = new StringBuilder(isStatic ? "private " : "public ").append(name).append('(');
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
      addGetter(getters, fieldEntry.getKey(), fieldEntry.getValue());
    }

    for (Map.Entry<String, String> fieldEntry : optional.entrySet()) {
      addGetter(getters, fieldEntry.getKey(), fieldEntry.getValue());
      addSetter(setters, fieldEntry.getKey(), fieldEntry.getValue());
    }

    ArrayList<String> queryList = new ArrayList<>();
    ArrayList<String> builderList = new ArrayList<>();

    if (!mandatory.isEmpty() || !optional.isEmpty()) {
      int i = 0;
      for (String str : mandatory.keySet()) {
        addToBuilderList(builderList, str, i++);
        addToQueryList(queryList, str);
      }
      for (String str : optional.keySet()) {
        queryList.add(String.format("if (%s != null) {", str));
        builderList.add(String.format("if (%s != null) {", str));
        addToBuilderList(builderList, str, i++);
        addToQueryList(queryList, str);
        queryList.add("}");
        builderList.add("}");
      }
    }

    try (PrintWriter out = new PrintWriter(createFile(String.format("%s.java", name), outPath, packageName))) {
      String queryHeader = isSigned ? "getQuery(String key)" : "getQuery()";
      String returnQuery = isSigned ? "return MessageUtil.generateSigQuery(ADDRESS, key" : "return MessageUtil.generateQuery(ADDRESS";

      CodeWriter writer = new CodeWriter(out)
          .write("package " + packageName + ";",
              "",
              "import java.util.Map;",
              "import java.util.List;",
              "import java.util.ArrayList;",
              "",
              String.format("import %s.MessageUtil;", packageName),
              "",
              "public class " + name + " {",
              "",
              String.format("public static final String %s = \"%s\";", ADDRESS.toUpperCase(), address),
              "")
          .write(fieldsList)
          .write(mandatoryConstructor)
          .write(optionalConstructor);
      if (!getters.isEmpty()) {
        writer.write(getters).write(setters);
      }

      writer.write(
          "",
          String.format("public %sString %s {", isStatic ? "static " : "", queryHeader)
      );
      if (!queryList.isEmpty()) {
        writer.write("List<String> list = new ArrayList<>();")
            .write(queryList)
            .write(String.format("%s, list.toArray(new String[list.size()]));", returnQuery));

      } else {
        writer.write(String.format("%s);", returnQuery));
      }
      writer.write("}");

      writeToStringMethod(writer, builderList);
      writer.write(
          "}"
      );
    }
  }

  private static void addToQueryList(List<String> queryList, String str) {
    queryList.add(String.format("list.add(\"%s\");", str));
    queryList.add(String.format("list.add(%s.toString());", str));
  }

  private static void addToBuilderList(List<String> builderList, String str, int pos) {
    builderList.add(String.format("builder%s.append(\"%s: \").append(%s.toString());",
        pos == 0 ? "" : ".append(\", \")",
        str, str));
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


  private static void addGetter(List<String> getters, String fieldName, String fieldType, String... annotations) {
    getters.add("");
    getters.addAll(Arrays.asList(annotations));
    getters.add(String.format("public %s get%s() {", fieldType, getMethodName(fieldName)));
    getters.add("return " + fieldName + ";");
    getters.add("}");
  }

  private static void addSetter(List<String> setters, String fieldName, String fieldType) {
    setters.add("");
    setters.add(String.format("public void set%s(%s %s) {", getMethodName(fieldName), fieldType, fieldName));
    setters.add(String.format("this.%s = %s;", fieldName, fieldName));
    setters.add("}");
  }

  private static String getMethodName(String fieldName) {
    return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
  }

  private static void writeToStringMethod(CodeWriter writer, List<String> builderList) {
    writer.write("",
        "@Override",
        "public String toString() {"
    );
    if (!builderList.isEmpty()) {
      writer
          .write("StringBuilder builder = new StringBuilder(\"{\");")
          .write(builderList)
          .write(
              "builder.append('}');",
              "return builder.toString();"
          );
    } else {
      writer.write("return \"{}\";");
    }
    writer.write("}");
  }

  private static String insertMessage(Node innerNode, Map<String, String> map) throws MessageParseException {
    Element parameter = (Element) innerNode;
    String fieldName = parameter.getAttribute(NAME);
    String fieldType = parameter.getAttribute(TYPE);
    checkPresence(fieldName, "Missing name of parameter");
    checkPresence(fieldType, "Missing parameter type");
    map.put(fieldName, fieldType);
    return fieldName;
  }

  private static void checkPresence(String param, String message) throws MessageParseException {
    if (param.isEmpty()) {
      throw new MessageParseException(message);
    }
  }

  private static void handleBean(Node node, String packageName) throws IOException {
    Element element = (Element) node;
    String name = element.getAttribute(NAME);
    checkPresence(name, "Missing name of bean");
    Map<String, String> fieldTypes = new HashMap<>();
    Map<String, String> fieldProperties = new HashMap<>();
    List<String> fieldNames = new ArrayList<>();

    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node innerNode = children.item(i);
      if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
        if (innerNode.getNodeName().equals(FIELD)) {
          String fieldName = insertMessage(innerNode, fieldTypes);
          fieldNames.add(fieldName);
          String property = ((Element) innerNode).getAttribute(PROPERTY);
          if (!property.isEmpty()) {
            fieldProperties.put(fieldName, property);
          }
        } else {
          throw new MessageParseException("Bean can contain only fields");
        }
      }
    }

    List<String> constructor = new ArrayList<>();
    StringBuilder constructorBuilder = new StringBuilder("public ").append(name).append('(');

    for (Map.Entry<String, String> fieldEntry : fieldTypes.entrySet()) {
      constructorBuilder.append(fieldEntry.getValue()).append(' ').append(fieldEntry.getKey()).append(", ");
    }
    if (!fieldTypes.isEmpty()) {
      constructorBuilder.delete(constructorBuilder.length() - ", ".length(), constructorBuilder.length());
      constructor.add(String.format("public %s () {", name));
      constructor.add("}");
      constructor.add("");
    }
    constructorBuilder.append(") {");
    constructor.add(constructorBuilder.toString());
    for (String fieldName : fieldTypes.keySet()) {
      constructor.add(String.format("this.%s = %s;", fieldName, fieldName));
    }
    constructor.add("}");


    List<String> fieldsList = new ArrayList<>();
    List<String> getters = new ArrayList<>();
    List<String> setters = new ArrayList<>();
    List<String> fromList = new ArrayList<>();
    generateLists(name, fieldTypes, fieldProperties, fieldNames, fieldsList, getters, setters, fromList);
    if (!fieldsList.isEmpty()) {
      fieldsList.remove(fieldsList.size() - 1);
    }

    ArrayList<String> builderList = new ArrayList<>();

    if (!fieldTypes.isEmpty()) {
      int i = 0;
      for (String str : fieldTypes.keySet()) {
        addToBuilderList(builderList, str, i++);
      }
    }

    try (PrintWriter out = new PrintWriter(createFile(String.format("%s.java", name), outPath, packageName))) {
      CodeWriter writer = new CodeWriter(out)
          .write(
              "package " + packageName + ";",
              "")
          .write(fieldProperties.isEmpty() ?
              List.of("import java.util.List;") :
              List.of("import java.util.List;",
                  "import com.fasterxml.jackson.annotation.JsonProperty;"))
          .write("",
              "public class " + name + " {")
          .write(fieldsList)
          .write("")
          .write(constructor)
          .write(getters)
          .write(setters)
          .write("")
          .write(fromList);

      writeToStringMethod(writer, builderList);
      writer.write(
          "}"
      );
    }
  }

  private static void generateLists(String name, Map<String, String> fieldTypes, Map<String, String> fieldProperties, List<String> fieldNames,
                                    List<String> fieldsList, List<String> getters, List<String> setters, List<String> fromList) {
    fromList.add(String.format("public static %s fromList(Object ... objects) throws BinanceJsonException {", name));
    fromList.add(String.format("%s result = new %s();", name, name));
    fromList.add(String.format("if (objects.length != %d) {", fieldTypes.size()));
    System.out.println(name + " " + fieldTypes.size() + " " + fieldTypes);
    fromList.add(String.format("throw new BinanceJsonException(\"Received \" + objects.length + \" objects, expected %s\");",
        fieldTypes.size()));
    fromList.add("}");
    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = fieldNames.get(i);
      String fieldType = fieldTypes.get(fieldName);
      fieldsList.add(String.format("private %s %s;", fieldType, fieldName));
      fieldsList.add("");
      String desiredType = fieldType.replaceAll("<.*>", "");
      if (desiredType.equals("Double")) {
        desiredType = "String";
      }
      fromList.add(String.format("if (objects[%d] instanceof %s) {", i, desiredType));
      if (!fieldType.equals("Double")) {
        fromList.add(String.format("result.set%s((%s) objects[%d]);", getMethodName(fieldName), fieldType, i));
      } else {
        fromList.add(String.format("result.set%s(Double.parseDouble((String) objects[%d]));", getMethodName(fieldName), i));

      }
      fromList.add("} else {");
      fromList.add(String.format(
          "throw new BinanceJsonException(\"Invalid type for %s field (position %d), expected is %s, got value \" + objects[%d]);",
          fieldName, i, fieldType, i));
      fromList.add("}");

      if (!fieldProperties.containsKey(fieldName)) {
        addGetter(getters, fieldName, fieldType);
      } else {
        addGetter(getters, fieldName, fieldType,
            String.format("@JsonProperty(\"%s\")", fieldProperties.get(fieldName)));
      }
      addSetter(setters, fieldName, fieldType);
    }
    fromList.add("return result;");
    fromList.add("}");
  }

  private static void handleEnum(Node node, String packageName) throws IOException {
    Element element = (Element) node;
    String name = element.getAttribute(NAME);
    checkPresence(name, "Missing name of enum");
    List<String> valueNames = new ArrayList<>();
    Map<String, String> valueValues = new HashMap<>();

    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node innerNode = children.item(i);
      if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
        if (innerNode.getNodeName().equals(VALUE)) {
          Element parameter = (Element) innerNode;
          String valueName = parameter.getAttribute(NAME);
          checkPresence(valueName, "Missing name of value");
          valueNames.add(valueName);
          String valueValue = parameter.getAttribute(VALUE);
          if (!valueValue.isEmpty()) {
            valueValues.put(valueName, valueValue);
          }
        } else {
          throw new MessageParseException("Enum can contain only it's values");
        }
      }
    }

    List<String> enumValues = new ArrayList<>();
    if (valueValues.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < valueNames.size(); i++) {
        builder.append(valueNames.get(i)).append(i == valueNames.size() - 1 ? ";" : ", ");
      }
      enumValues.add(builder.toString());
    } else {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < valueNames.size(); i++) {
        String current = valueNames.get(i);
        builder.append(current).append('(').append('"').append(valueValues.get(current)).append('"').append(')')
            .append(i == valueNames.size() - 1 ? ";" : ", ");
      }
      enumValues.add(builder.toString());
      enumValues.add("");
      enumValues.add("private String value;");
      enumValues.add("");
      enumValues.add(String.format("%s(String value) {", name));
      enumValues.add("this.value = value;");
      enumValues.add("}");
      enumValues.add("");
      enumValues.add("@Override");
      enumValues.add("public String toString() {");
      enumValues.add("return value;");
      enumValues.add("}");
    }

    try (PrintWriter out = new PrintWriter(createFile(String.format("%s.java", name), outPath, packageName))) {
      new CodeWriter(out)
          .write(
              "package " + packageName + ";",
              "",
              "public enum " + name + " {")
          .write(enumValues)
          .write(
              "}"
          );
    }
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
  }
}
