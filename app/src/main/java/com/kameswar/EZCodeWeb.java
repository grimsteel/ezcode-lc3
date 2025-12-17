package com.kameswar;

import org.teavm.jso.dom.html.*;

import java.util.*;

public class EZCodeWeb
{	
  private static HTMLDocument document = HTMLDocument.current();
  private static HTMLFormElement form = (HTMLFormElement) document.getElementById("ezcode-form");
  private static HTMLTextAreaElement input = (HTMLTextAreaElement) document.getElementById("ezcode-source");
  private static HTMLElement lexerLog = document.getElementById("lexer-log");
  private static HTMLElement parserLog = document.getElementById("parser-log");
  private static HTMLElement codegenLog = document.getElementById("codegen-log");

  private static void println(HTMLElement log, String s) {
    log.setTextContent(log.getTextContent() + s + '\n');
  }
  private static void print(HTMLElement log, String s) {
    log.setTextContent(log.getTextContent() + s);
  }
  private static void clear() {
    lexerLog.setTextContent("");
    parserLog.setTextContent("");
    codegenLog.setTextContent("");
  }

  private static void compile() {

		clear();
		
    println(lexerLog, "Lexing source...");
		Lexer lexer = new Lexer(input.getValue());
    println(lexerLog, String.format("|  %5s  %5s  %-15s  %s", "Line", "Col", "Terminal", "Value"));
		println(lexerLog, String.format("|  %5s  %5s  %-15s  %s", "----", "---", "--------------", "-----"));
    for (Token t : lexer.getTokens()) {
      println(lexerLog, String.format("|  %s", t));
    }
		println(lexerLog,  String.format("|- Successfully lexed %d tokens.\n", lexer.getTokens().size()));
		
		DataBlock db = new DataBlock();
		
		println(parserLog, "Parsing tokens...");
    Parser p = new Parser(lexer.getTokens(), db);
    ArrayList<Statement> statements = p.parseSequence(/* root */ true);
    for (Statement t : statements) {
      println(parserLog, String.format("|  %s", t));
    }
    
    println(parserLog, String.format("|- Successfully parsed %d statements.\n\n", statements.size()));
    
    println(codegenLog, "Generating code...");
    Codegen cg = new Codegen(statements, (short) 0x3000, db);
    ArrayList<Instruction> instrs = cg.generate();
    println(codegenLog, String.format("|- Successfully generated %d instructions (including builtins).\n", instrs.size()));
    URL
    //System.out.printf("Writing assembly bytecode to %s\n", outputFile);
    try{ObjGen.getObjFile(instrs);} catch (Exception e) {}
  }
  
	public static void main(String[] args) throws Exception {
    form.onEvent("submit", evt -> {
      evt.preventDefault();
      compile();
    });
	}

}
