package test;

import java.io.*;

public class TryTest {
/*
	void v(int a) throws IOException {
		try {
			if(a>0)
				throw new IOException();
			if(a<6) 
				throw new NullPointerException ();
			a++;
			//return;
		}
		catch (IOException e) {
			if(a>1)
				throw new ArrayIndexOutOfBoundsException();
			a = 1;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			a = 2;
		}
		finally{			
			if(a<8)
				throw new IOException();

			a = 3;
		}
		
		if(a>1)
			throw new IOException();
		
		a--;
		
	}
	*/
/*
	void v2(int a, boolean b){
		while(b){
			
			try{
				try {
					if(a>0)
						throw new IOException();
					if(a<6) 
						throw new NullPointerException ();
					if(a==8)
						break;
					a++;
					//return;
				}
				catch (IOException e) {
					if(a>1)
						throw new ArrayIndexOutOfBoundsException();
					a = 1;
				}
				catch (ArrayIndexOutOfBoundsException e) {
					a = 2;
				}
				finally{			
					if(a<8)
						throw new IOException();

					a = 3;
				}
				
				if(a>1)
					throw new IOException();
				
				a--;
				
			}
			catch (IOException e) {
				//if(a>1)
					throw new ArrayIndexOutOfBoundsException();
				//a = 1;
			}
			catch (ArrayIndexOutOfBoundsException e) {
				a = 2;
			}
			finally{
				//throw new ArrayIndexOutOfBoundsException();
				a++;
			}	
			a--;
		}
		a++;

	}
*/	
	
	void v3(int a, boolean b){
		try{
			while(b){
				try{
					if(b) break;
				}
				finally{
					continue;
				}
			}
			a++;
			if(b) return;
		}
		finally{
			a--;
		}
		a++;
		
	}
	


}
