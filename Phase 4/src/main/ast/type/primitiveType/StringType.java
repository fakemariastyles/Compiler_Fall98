package main.ast.type.primitiveType;

import main.ast.type.Type;

public class StringType extends Type {
    @Override
    public String toString() {
        return "string";
    }

    @Override
    public String getCode() {
        return "Ljava/lang/String";
    }
}
