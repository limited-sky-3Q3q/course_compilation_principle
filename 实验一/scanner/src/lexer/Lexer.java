package lexer;

import java.io.IOException;
import java.util.Hashtable;

public class Lexer {

	public static int line = 1;
	char peek = ' ';
	Hashtable words = new Hashtable();

	void reserve(Word w) {
		words.put(w.lexeme, w);
	}

	public Lexer() {
		/*
			KEY型token
			预留关键字
			void int double bool string class
			true false null this extends for 
			while if else return new NewArray 
			Print ReadInteger ReadLine
		*/
		reserve(new Word("if", Tag.IF));
		reserve(new Word("else", Tag.ELSE));
		reserve(new Word("while", Tag.WHILE));
		reserve(new Word("do", Tag.DO));
		reserve(new Word("break", Tag.BREAK));
		reserve(new Word("void", Tag.VOID));
		reserve(new Word("int", Tag.INT));
		reserve(new Word("double", Tag.DOUBLE));
		reserve(new Word("bool", Tag.BOOL));
		reserve(new Word("string", Tag.STRING));
		reserve(new Word("class", Tag.CLASS));
		reserve(new Word("null", Tag.NULL));
		reserve(new Word("this", Tag.THIS));
		reserve(new Word("extends", Tag.EXTENDS));
		reserve(new Word("return", Tag.RETURN));
		reserve(new Word("new", Tag.NEW));
		reserve(new Word("NewArray", Tag.NEWARRAY));
		reserve(new Word("Print", Tag.PRINT));
		reserve(new Word("ReadInteger", Tag.READINTEGER));
		reserve(new Word("ReadLine", Tag.RADLINE));
		reserve(Word.True);
		reserve(Word.False);
	}

	public void readch() throws IOException {
		peek = (char) System.in.read();		
	}

	boolean readch(char c) throws IOException {//判断下一个字符peek==c
		readch();
		if (peek != c) {//预期字符不符，返回false
			return false;
		}
		peek = ' ';//peek == c，peek置空格，返回true
		return true;
	}

	public Token scan() throws IOException {
		for (;; readch()) {//忽略空格和换行符
			if (peek == ' ' || peek == '\t')
				continue;
			else if (peek == '\n') {
				line += 1;
			} else {
				break;
			}
		}
		
		if (peek == '/'){//注释识别
			readch();
			if (peek == '/'){//单行注释
				readch();
				while(peek != '\n'){
					readch();
				}
				readch();
				return new Comment();
			}
			else if (peek == '*'){//多行注释
				char temp;
				readch();
				temp = peek;
				readch();
				while(!(temp == '*' && peek == '/')){
					temp = peek;
					readch();
				}
				readch();
				if(peek == '\n')
					peek = ' ';//防止中途退出
				return new Comment();
			}
			else{
				return new Token('/');
			}
		}

		switch (peek) {
		/*
			SYM型token
		    操作符和标点
			+ - * / % \ < <= > >= = 
			!= && || == ! ; , . [ ] ( ) { }
		*/
		case '+':
			peek = ' ';
			return new Token('+');
		case '-':
			peek = ' ';
			return new Token('-');
		case '%':
			peek = ' ';
			return new Token('%');
		case '\\':
			peek = ' ';
			return new Token('\\');
		case '&':
			if (readch('&'))
				return Word.and;
			else
				return new Token('&');
		case '|':
			if (readch('|'))
				return Word.or;
			else
				return new Token('|');
		case '=':
			if (readch('='))
				return Word.eq;
			else
				return new Token('=');
		case '!':
			if (readch('='))
				return Word.ne;
			else
				return new Token('!');
		case '<':
			if (readch('='))
				return Word.le;
			else
				return new Token('<');
		case '>':
			if (readch('='))
				return Word.ge;
			else
				return new Token('>');
		}
		if (Character.isDigit(peek)) {// NUM型token
			if(peek == '0'){
				readch();
				if(peek == 'x' || peek == 'X'){//16进制整型
					readch();
					StringBuffer b = new StringBuffer();
					do {
						b.append(peek);
						readch();
					} while (Character.isDigit(peek) 
							|| (peek >= 'a' && peek <= 'f')
							|| (peek >= 'A' && peek <= 'F'));
					String s = b.toString();
					return new Num(Integer.parseInt(s,16));//将十六进制字符串转为十进制整型存储
				}
			}

			int v = 0;
			while (Character.isDigit(peek)){//十进制整型或双精度常量的整数部分
				v = 10 * v + Character.digit(peek, 10);
				readch();
			} 
			if (peek != '.')
				return new Num(v);//十进制整型
			//双精度常量
			float x = v;//整数部分
			float d = 10;//表示十进制
			for (;;) {//小数部分
				readch();
				if (!Character.isDigit(peek))
					break;
				x = x + Character.digit(peek, 10) / d;
				d = d * 10;
			}
			if(peek == 'e' || peek == 'E'){//科学计数法
				int offset = 0;
				boolean flag = true;//false为正数，false为负数，没有符号时默认正数
				readch();
				if(peek == '-'){//指数部分为负数
					flag = false;
				}
				if(!Character.isDigit(peek)){//当前位为符号位，再读取一位
					readch();
				}
				while(Character.isDigit(peek)){//读取指数部分的数值
					offset = 10*offset + Character.digit(peek, 10);
					readch();
				}
				if(flag)//根据指数部分的正负进行计算
					for(int i=0; i<offset; i++)
						x *= 10;
				else
					for(int i=0; i<offset; i++)
						x /= 10;
			}
			return new Real(x);
		}
		if (Character.isLetter(peek)) {// ID型token
			StringBuffer b = new StringBuffer();
			do {
				b.append(peek);
				readch();
			} while (Character.isLetterOrDigit(peek) || peek=='_');//在原代码上加上下划线的判断
			String s = b.toString();
			Word w = (Word) words.get(s);
			if (w != null)
				return w;
			w = new Word(s, Tag.ID);
			words.put(s, w);
			return w;
		}
		if(peek == '"'){// STR型token
			StringBuffer b = new StringBuffer();
			readch();
			while (peek != '"'){//读取到下一个双引号为之
				if(peek == '\n')//在字符串中读取到换行符，输出问题
					System.out.println("wrong string with \"\\n\"");
				else
					b.append(peek);
				readch();
			}
			String s = b.toString();
			readch();
			return new STR(s);
		}
		Token tok = new Token(peek);
		peek = ' ';
		return tok;
	}
	
	public void out() {
		System.out.println(words.size());
		
	}

	public char getPeek() {
		return peek;
	}

	public void setPeek(char peek) {
		this.peek = peek;
	}

}
