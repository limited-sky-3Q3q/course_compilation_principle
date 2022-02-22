package inter;

import symbols.Type;

public class For extends Stmt {
	Expr expr; Stmt stmt1; Stmt stmt2; Stmt stmt3; 
	
	public For() { expr = null; stmt1 = null; stmt2 = null; stmt3 = null; }
	
	public void init(Stmt s1, Expr x, Stmt s2, Stmt s3) {
		expr = x; stmt1 = s1; stmt2 = s2; stmt3 = s3;
		if( expr.type != Type.Bool ) expr.error("boolean required in do");
	}
	
	public void gen(int b, int a) {
		int label = newlabel();   // label for stmt1
		stmt1.gen(label, b);

		b = label; // 更新b
		// label = newlabel();
		emitlabel(label);
		after = a;                // save label a
		expr.jumping(0, a);

		label = newlabel();   // label for stmt3
		emitlabel(label); stmt3.gen(label, b);

		label = newlabel();   // label for stmt2
		emitlabel(label); stmt2.gen(label, b);
		emit("goto L" + b);
	}

}
