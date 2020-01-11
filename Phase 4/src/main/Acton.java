package main;

import main.ast.node.Program;
import main.compileError.CompileErrorException;
import main.visitor.TypeChecker.TypeAnalyser;
import main.visitor.codeGenerator.CodeGenerator;
import main.visitor.nameAnalyser.NameAnalyser;
import org.antlr.v4.runtime.*;
import main.parsers.*;
import main.parsers.actonParser;

import java.io.IOException;
import java.util.*;

// Visit https://stackoverflow.com/questions/26451636/how-do-i-use-antlr-generated-parser-and-lexer
public class Acton {
    public static void main(String[] args) throws IOException {
        CharStream reader = CharStreams.fromFileName(args[1]);
        actonLexer lexer = new actonLexer(reader);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        actonParser parser = new actonParser(tokens);
        try {
            Program program = parser.program().p; // program is starting production rule
            NameAnalyser nameAnalyser = new NameAnalyser();
            nameAnalyser.visit(program);
            if (nameAnalyser.numOfErrors() > 0)
                throw new CompileErrorException();

            TypeAnalyser typeAnalyser = new TypeAnalyser();
            typeAnalyser.visit(program);

            CodeGenerator codeGenerator = new CodeGenerator();
            codeGenerator.visit(program);

        } catch (CompileErrorException compileError) {
        }
    }
}