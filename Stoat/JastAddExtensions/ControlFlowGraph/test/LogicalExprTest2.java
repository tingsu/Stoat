package test;

public class LogicalExprTest2 {
	void v(int a, boolean b, int c) {
		b = !(a>0 && c<0);
		b = a>0 || c<0;
		
		//if((a>0 && c<0) instanceof Boolean) ;
		//b = (Boolean)(a>0 && c<0);
		b = b && (a>0 || c<0);
		
		f(a>0&&c<0, a>0||c<0);
		
		boolean[][] array = {{a>0&&c<0, a>0||c<0},{a>5&&c<6}};
		
		b = a>0&&c<0 ? a>1||c<1 : a>2&&c<2;
		
		if(a>0&&c<0) ;
		
		if(a>0||c<0)
			a++;
		else
			a--;
		
		while(a>0&&c<0) ;
		
		do{
			
		}while(a>0&&c<0);
		
		for(;a>0&&c<0;) ;
		
		
		
		
		
	}
	
	void f(boolean x, boolean y) {
		
	}

}
