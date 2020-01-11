package main.ast.type.primitiveType;

import main.ast.type.Type;

public class BooleanType extends Type {
    @Override
    public String toString() {
        return "boolean";
    }

    @Override
    public String getCode() {
        return "I";
    }
}
