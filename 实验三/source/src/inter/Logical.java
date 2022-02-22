package inter;

import symbols.Type;
import lexer.Token;

public class Logical extends Expr{
	public Expr expr1, expr2;
	Logical(Token tok, Expr x1, Expr x2){
		super(tok,null);
		expr1 = x1; expr2 = x2;
		type = check(expr1.type, expr2.type);// 进行类型检查
		if(type==null) error("type error");
	}
	
	public Type check(Type p1, Type p2){// 逻辑运算表达式操作数需要为bool型，进行类型检查
		if(p1==Type.Bool&&p2==Type.Bool) return Type.Bool;
		else return null;
	}
	
	public Expr gen(){
		int f = newlabel(); int a = newlabel();
		Temp temp = new Temp(type);
		this.jumping(0,f);
		emit(temp.toString()+" = true");
		emit("goto L"+a);
		emitlabel(f);emit(temp.toString()+" = false");
		emitlabel(a);
		return temp;
	}
}
