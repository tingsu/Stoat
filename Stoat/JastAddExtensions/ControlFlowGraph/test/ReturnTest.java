package test;

public class ReturnTest {
	void v(int a){
		if(a>0)
			return;
		a++;
	}
	
	int v2(int a){
		if(a>0)
			return a+2;
		return a;
		
	}

}
