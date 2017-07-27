package test;

public class SwitchTest {
	void v(int a) {
		switch(a = 1) {
		case 1: { a = 2; }
		case 1+1:
			a = 3;
			a = 4;
		default:
			a = 5;			
		}

		
	    switch (a){
	    case 6: a = 7;
	    }
	    
		a = 8;
		
	}
	


}
