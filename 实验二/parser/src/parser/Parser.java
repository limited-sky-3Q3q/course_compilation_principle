package parser;

import inter.Access;
import inter.And;
import inter.Arith;
import inter.Break;
import inter.Constant;
import inter.Do;
import inter.Else;
import inter.Expr;
import inter.Id;
import inter.If;
import inter.Not;
import inter.Or;
import inter.Rel;
import inter.Seq;
import inter.Set;
import inter.SetElem;
import inter.Stmt;
import inter.Unary;
import inter.While;
import inter.For;

import java.io.IOException;

import symbols.Array;
import symbols.Env;
import symbols.Type;
import lexer.Lexer;
import lexer.Tag;
import lexer.Token;
import lexer.Word;
import lexer.Num;

public class Parser {

	   private Lexer lex;    // lexical analyzer for this parser
	   private Token look;   // lookahead tagen
	   Env top = null;       // current or top symbol table
	   int used = 0;         // storage used for declarations

	   public Parser(Lexer l) throws IOException { lex = l; move(); }

	   void move() throws IOException { look = lex.scan(); }

	   void error(String s) { throw new Error("near line "+lex.line+": "+s); }

	   void match(int t) throws IOException {
	      if( look.tag == t ) move();
	      else error("syntax error");
	   }

	   public void program() throws IOException {  // program -> block
		  // build the syntax tree
	      Stmt s = block();
	      // display the syntax tree
	      // only display the stmts, without expr
	      s.display();
	   }

	   Stmt block() throws IOException {  // block -> { decls stmts }
	      match('{');  Env savedEnv = top;  top = new Env(top);
	      decls(); Stmt s = stmts();
	      match('}');  top = savedEnv;
	      return s;
	   }

	   void decls() throws IOException {// decls -> decls decl | ε

	      while( look.tag == Tag.BASIC ) {// decl -> type id ;
	         Type p = type(); Token tok = look; match(Tag.ID); match(';');
	         Id id = new Id((Word)tok, p, used);
	         top.put( tok, id );
	         used = used + p.width;
	      }
	   }

	   Type type() throws IOException {// type -> type [num] | basic
	      Type p = (Type)look;            // expect look.tag == Tag.BASIC 
	      match(Tag.BASIC);               // 语法检查
	      if( look.tag != '[' ) return p; // type -> basic
	      else return dims(p);            // return array type. type -> type [num]
	   }

	   Type dims(Type p) throws IOException {// 递归下降，判断所有 [num] token
	      match('[');  Token tok = look;  match(Tag.NUM);  match(']');
	      if( look.tag == '[' )
	      	p = dims(p);
	      return new Array(((Num)tok).value, p);
	   }

	   Stmt stmts() throws IOException {// stmts -> stmts stmt | ε
	      if ( look.tag == '}' ) return Stmt.Null;// 直到 } 结束
	      else return new Seq(stmt(), stmts());// 递归处理stmt
	   }

	   Stmt stmt() throws IOException {
			/*
				stmt -> loc = bool;
					| if(bool)stmt
					| if(bool)stmt else stmt
					| while(bool)stmt
					| do stmt while (bool);
					| break;
					| block
			*/
	      Expr x;  Stmt s, s1, s2;
	      Stmt savedStmt;         // save enclosing loop for breaks

	      switch( look.tag ) {// 判断下一个状态转移对象

	      case ';':
	         move();
	         return Stmt.Null;

		  case Tag.FOR:// stmt -> for (assign;bool;assign) stmt
		  //{int a; int b; int c; while(true){if(true)for(a=b; a<b; a=a+1)a=b;}}
			 For fornode = new For();
	         savedStmt = Stmt.Enclosing; Stmt.Enclosing = fornode;
			 match(Tag.FOR); match('('); 
			 Stmt a = assign_v2(); match(';');// 匹配第一个assign
			 Expr b = bool(); match(';');// 匹配bool
			 Stmt c = assign_v2(); match(')');// 匹配第二个assign
			 s1 = stmt();
			 fornode.init(a, b, c, s1);//初始化for节点
	         Stmt.Enclosing = savedStmt;  // 更新闭包
	         return fornode;

	      case Tag.IF:
	         match(Tag.IF); match('('); x = bool(); match(')');
	         s1 = stmt();
	         if( look.tag != Tag.ELSE ) return new If(x, s1);// stmt -> if(bool)stmt
	         match(Tag.ELSE);
	         s2 = stmt();
	         return new Else(x, s1, s2);// stmt -> if(bool)stmt else stmt

	      case Tag.WHILE:// stmt -> while(bool)stmt
	         While whilenode = new While();
	         savedStmt = Stmt.Enclosing; Stmt.Enclosing = whilenode;
	         match(Tag.WHILE); match('('); x = bool(); match(')');
	         s1 = stmt();
	         whilenode.init(x, s1);
	         Stmt.Enclosing = savedStmt;  // reset Stmt.Enclosing
	         return whilenode;

	      case Tag.DO:// stmt -> do stmt while (bool);
	         Do donode = new Do();
	         savedStmt = Stmt.Enclosing; Stmt.Enclosing = donode;
	         match(Tag.DO);
	         s1 = stmt();
	         match(Tag.WHILE); match('('); x = bool(); match(')'); match(';');
	         donode.init(s1, x);
	         Stmt.Enclosing = savedStmt;  // reset Stmt.Enclosing
	         return donode;

	      case Tag.BREAK:// stmt -> break;
	         match(Tag.BREAK); match(';');
	         return new Break();

	      case '{':// stmt -> block
	         return block();

	      default:// stmt -> loc = bool;
	         return assign();
	      }
	   }

	   Stmt assign() throws IOException {// loc -> loc[bool] | id
	      Stmt stmt;  Token t = look;
	      match(Tag.ID);
	      Id id = top.get(t);
	      if( id == null ) error(t.toString() + " undeclared");

	      if( look.tag == '=' ) {       // S -> id = E ;
	         move();  stmt = new Set(id, bool());
	      }
	      else {                        // S -> L = E ;
	         Access x = offset(id);
	         match('=');  stmt = new SetElem(x, bool());
	      }
	      match(';');
	      return stmt;
	   }

	   Stmt assign_v2() throws IOException {// loc -> loc[bool] | id
		Stmt stmt;  Token t = look;
		match(Tag.ID);
		Id id = top.get(t);
		if( id == null ) error(t.toString() + " undeclared");

		if( look.tag == '=' ) {       // S -> id = E ;
		   move();  stmt = new Set(id, bool());
		}
		else {                        // S -> L = E ;
		   Access x = offset(id);
		   match('=');  stmt = new SetElem(x, bool());
		}
		return stmt;
	 }
	   Expr bool() throws IOException {// bool -> bool || join | join
	      Expr x = join();
	      while( look.tag == Tag.OR ) {
	         Token tok = look;  move();  x = new Or(tok, x, join());
	      }
	      return x;
	   }

	   Expr join() throws IOException {// join -> join && equality | equality
	      Expr x = equality();
	      while( look.tag == Tag.AND ) {
	         Token tok = look;  move();  x = new And(tok, x, equality());
	      }
	      return x;
	   }

	   Expr equality() throws IOException {// equality -> equality == rel | equality != rel | rel
	      Expr x = rel();
	      while( look.tag == Tag.EQ || look.tag == Tag.NE ) {
	         Token tok = look;  move();  x = new Rel(tok, x, rel());
	      }
	      return x;
	   }

	   Expr rel() throws IOException {// rel -> expr < expr | expr <= expr | expr>=expr | expr > expr | expr
	      Expr x = expr();
	      switch( look.tag ) {
	      case '<': case Tag.LE: case Tag.GE: case '>':
	         Token tok = look;  move();  return new Rel(tok, x, expr());
	      default:
	         return x;
	      }
	   }

	   Expr expr() throws IOException {// expr -> expr + term | expr - term | term
	      Expr x = term();
	      while( look.tag == '+' || look.tag == '-' ) {
	         Token tok = look;  move();  x = new Arith(tok, x, term());
	      }
	      return x;
	   }

	   Expr term() throws IOException {// term -> term * unary | term/unary | unary
	      Expr x = unary();
	      while(look.tag == '*' || look.tag == '/' ) {
	         Token tok = look;  move();   x = new Arith(tok, x, unary());
	      }
	      return x;
	   }

	   Expr unary() throws IOException {// unary -> ! unary | - unary | factor
	      if( look.tag == '-' ) {
	         move();  return new Unary(Word.minus, unary());
	      }
	      else if( look.tag == '!' ) {
	         Token tok = look;  move();  return new Not(tok, unary());
	      }
	      else return factor();
	   }

	   Expr factor() throws IOException {// factor -> (bool) | loc | num | real | true | false
	      Expr x = null;
	      switch( look.tag ) {
	      case '(':
	         move(); x = bool(); match(')');
	         return x;
	      case Tag.NUM:
	         x = new Constant(look, Type.Int);    move(); return x;
	      case Tag.REAL:
	         x = new Constant(look, Type.Float);  move(); return x;
	      case Tag.TRUE:
	         x = Constant.True;                   move(); return x;
	      case Tag.FALSE:
	         x = Constant.False;                  move(); return x;
	      default:
	         error("syntax error");
	         return x;
	      case Tag.ID:
	         String s = look.toString();
	         Id id = top.get(look);
	         if( id == null ) error(look.toString() + " undeclared");
	         move();
	         if( look.tag != '[' ) return id;
	         else return offset(id);
	      }
	   }

	   Access offset(Id a) throws IOException {   // I -> [E] | [E] I
	      Expr i; Expr w; Expr t1, t2; Expr loc;  // inherit id

	      Type type = a.type;
	      match('['); i = bool(); match(']');     // first index, I -> [ E ]
	      type = ((Array)type).of;
	      w = new Constant(type.width);
	      t1 = new Arith(new Token('*'), i, w);
	      loc = t1;
	      while( look.tag == '[' ) {      // multi-dimensional I -> [ E ] I
	         match('['); i = bool(); match(']');
	         type = ((Array)type).of;
	         w = new Constant(type.width);
	         t1 = new Arith(new Token('*'), i, w);
	         t2 = new Arith(new Token('+'), loc, t1);
	         loc = t2;
	      }

	      return new Access(a, loc, type);
	   }
	}
