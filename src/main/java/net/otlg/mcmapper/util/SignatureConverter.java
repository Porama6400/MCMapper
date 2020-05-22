package net.otlg.mcmapper.util;

public class SignatureConverter {
    public static String toASM(String signature) {
        if (signature == null || signature.equals("")) {
            return "";
        } else if (!signature.contains(",")) {
            return convert(signature);
        }

        StringBuilder builder = new StringBuilder();
        String[] split = signature.split(",");
        for (String s : split) {
            builder.append(convert(s));
        }

        return builder.toString();
    }

    /**
     * Convert to ASM's signature thingy
     * @param type
     * @return
     */
    public static String convert(String type) {
        int bracket = 0;
        while (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2);
            bracket++;
        }
        type = type.replace('.', '/');

        StringBuilder out = new StringBuilder();
        switch (type) {
            case "byte":
                out.append("B");
                break;
            case "char":
                out.append("C");
                break;
            case "double":
                out.append("D");
                break;
            case "float":
                out.append("F");
                break;
            case "int":
                out.append("I");
                break;
            case "long":
                out.append("J");
                break;
            case "short":
                out.append("S");
                break;
            case "boolean":
                out.append("Z");
                break;
            default:
                out.append("L").append(type).append(";");
                break;
        }
        for (int i = 0; i < bracket; i++) out.insert(0, '[');
        return out.toString();
    }
}
