package test;

public class ForTest {
	void v(int a, int b){
		for(int i=0, j=1;i<10;i++,j--){
			a = 8;
		}
		
		for(a=1,b=2;;a++){
			a = 7;
		}
		
	}
	
	void v2(int a){
		for(;a<5;){
			a = 2;
		}
		
		for(;;)
			;
	}

}
