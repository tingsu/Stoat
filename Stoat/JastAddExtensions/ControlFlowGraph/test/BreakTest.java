package test;

public class BreakTest {
	void v(int a, boolean b){
		while(b){
			break;
		}
		
		outer:
		do{
			inner:
			for(;;) break outer;
				
		}while(b);
		
		switch(a){
		case 1:
			break;
		case 2:
			a = 3;
		}
		
		a = 1;
		
		do{
			try{
				break;
			}
			finally{
				a = 1;
			}
		}while(b);
		
		
		try{
			while(b){
				break;
			}
			a++;
			
		}
		finally{
			a--;
		}
		a++;
	
				
	}

}
