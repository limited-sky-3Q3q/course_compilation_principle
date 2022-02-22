package main;

import java.io.IOException;
import java.util.Hashtable;

import lexer.Lexer;
import lexer.Token;
import lexer.Tag;

public class Main {
	public static void main(String[] args) throws IOException {
		Lexer lexer = new Lexer();
		char c;
		do {
			Token token=lexer.scan();
			switch (token.tag) {
			case Tag.NUM:
			case Tag.REAL:
				System.out.println("(NUM , "+token.toString()+")");
				break;
			case Tag.ID:
				System.out.println("(ID , "+token.toString()+")");
				break;
			case Tag.IF:
			case Tag.ELSE:
			case Tag.WHILE:
			case Tag.DO:
			case Tag.BREAK:
			case Tag.VOID:
			case Tag.INT:
			case Tag.DOUBLE:
			case Tag.BOOL:
			case Tag.STRING:
			case Tag.CLASS:
			case Tag.NULL:
			case Tag.THIS:
			case Tag.EXTENDS:
			case Tag.RETURN:
			case Tag.NEW:
			case Tag.NEWARRAY:
			case Tag.PRINT:
			case Tag.READINTEGER:
			case Tag.RADLINE:
			case Tag.TRUE:
			case Tag.FALSE:
				System.out.println("(KEY , "+token.toString()+")");
				break;
			case Tag.STR:
				System.out.println("(STR , "+token.toString()+")");
				break;
			case Tag.COMMENT://注释识别
			// 	System.out.println("(Comment , "+token.toString()+")");
				break;
			default:
				System.out.println("(SYM , "+token.toString()+")");
				break;
			}
			
		} while (lexer.getPeek()!='\n');
	}
}
