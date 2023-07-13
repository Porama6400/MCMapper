package dev.porama.mcmapper.record;

import dev.porama.mcmapper.util.SignatureConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChildRecord {
    private static final Pattern signaturePattern = Pattern.compile("\\(([^)]*)\\)");
    private final String returnType;
    private final String originalName;
    private final String obfuscatedName;

    public ChildRecord(String returnType, String originalName, String obfuscatedName) {
        this.returnType = returnType;
        this.originalName = originalName;
        this.obfuscatedName = obfuscatedName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        for (char c : originalName.toCharArray()) {
            if (c == '(') break;
            nameBuilder.append(c);
        }
        return nameBuilder.toString();
    }

    public String getSignature() {
        Matcher matcher = signaturePattern.matcher(originalName);
        if (matcher.find()) {
            return SignatureConverter.toASM(matcher.group(1));
        }
        return "";
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    @Override
    public String toString() {
        return "ChildRecord{" +
                "returnType='" + returnType + '\'' +
                ", originalName='" + originalName + '\'' +
                ", obfuscatedName='" + obfuscatedName + '\'' +
                '}';
    }
}
