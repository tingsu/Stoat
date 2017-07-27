package test;

public class ContinueTest {
	void v(int a){
		while(a>0){
			continue;
		}
		
		do{
			continue;
		}while(a>0);
		
		outer:
		while(a>0){
			inner:
			while(a>0){
				continue outer;
			}					
		}
		
		while(a>0){
			try{
				if(a>0) continue;
				a--;
			}
			finally{
				a++;
			}
			a--;
		}
		
		try{
			while(a>0){
				continue;
			}			
		}
		finally{
			a++;
		}
		
		
		for(a=0;a<5;a++) 
			continue;
		
		for(a=0;a<5;)
			continue;
		
		for(;;){
			a++;
			continue;
		}
					
	}

}
