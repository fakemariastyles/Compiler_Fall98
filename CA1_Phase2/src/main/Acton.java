package main;

import main.ast.node.Program;
import main.visitor.AstVisitorImpl;
import main.visitor.FirstNameAnalyzerVisitor;
import main.visitor.SecondNameAnalyzerVisitor;
import org.antlr.v4.runtime.*;
import main.parsers.actonLexer;
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
        Program program = parser.program().p; /* assuming that the name of the Program ast node
                                                 that the program rule returns is p */


        FirstNameAnalyzerVisitor firstNameAnalyzerVisitor = new FirstNameAnalyzerVisitor();
        firstNameAnalyzerVisitor.visit(program);

        SecondNameAnalyzerVisitor secondNameAnalyzerVisitor = new SecondNameAnalyzerVisitor();

        secondNameAnalyzerVisitor.setErrors(firstNameAnalyzerVisitor.getErrors());
        Map<String , Integer> x = firstNameAnalyzerVisitor.getLineErrors();

        secondNameAnalyzerVisitor.setLineErrors(x);

        secondNameAnalyzerVisitor.visit(program);

        Map<String , Integer> errors = secondNameAnalyzerVisitor.getLineErrors();

        Object[] a = errors.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o1).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o2).getValue());
            }
        });

        for (Object e : a) {
            System.out.println(((Map.Entry<String, Integer>) e).getKey());
        }

        if (secondNameAnalyzerVisitor.getErrors() == 0) {
            AstVisitorImpl astVisitor = new AstVisitorImpl();
            astVisitor.visit(program);
        }
    }
}