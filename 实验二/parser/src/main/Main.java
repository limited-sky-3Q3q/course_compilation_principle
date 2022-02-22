package main;

import java.io.IOException;

import parser.Parser;
import lexer.Lexer;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Lexer lex = new Lexer();
		Parser parser = new Parser(lex);
		System.out.println("----------\nMain start\n----------");
		parser.program();//进行语法分析
		System.out.print("\n");
		System.out.println("----------\nMain quit\n----------");
	}

}
