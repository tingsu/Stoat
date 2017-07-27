package test;

public class LogicalExprTest {
	
	void v(int a, int b, int c, int d){
		boolean r;
		r = a>0 && b>0 && c>0;
		r = a>0 || b>0 || c>0;
		r = !(a>0) && !(b>0) || c>0 && d>0;		
	}

}
