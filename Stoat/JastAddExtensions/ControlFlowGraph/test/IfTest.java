package test;

public class IfTest {
	void v(int a){
		if(a>0)
			a = 1;
		else{
			a = 2;
		}
		
		if(a<0){
			a = 3;
		}
		a = 4;			
	}

}
