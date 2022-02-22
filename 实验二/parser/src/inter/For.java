package inter;

import symbols.Type;

public class For extends Stmt {

   Stmt stmt1, stmt2, stmt3;
   Expr expr;//for节点各部分组件

   public For() { stmt1 = null; stmt2 = null; stmt3 = null; expr = null; }//置空各组件

   public void init(Stmt s1, Expr ex, Stmt s2, Stmt s3) {//初始化各组件
      stmt1 = s1;
      expr = ex;
      stmt2 = s2;  
      stmt3 = s3;
      if( expr.type != Type.Bool ) expr.error("boolean required in for");
   }
   public void gen(int b, int a) {}
   
   public void display() {
	   emit("stmt : for begin");
       stmt1.display();
       stmt2.display();
	   stmt3.display();
	   emit("stmt : for end");
   }
}
