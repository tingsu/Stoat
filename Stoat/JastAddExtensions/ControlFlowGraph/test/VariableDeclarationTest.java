package test;

public class VariableDeclarationTest {
	void v(){
		class VD{
			private int a = 1+1;
			public VD(int a, int b){
				this.a = a; 
				this.a = b;
			}
		}
		
		int a;
		int b = 1 + 2;
		
		VD vd1;
		VD vd2 = new VD(2+3, 4*5){
			void v2(){ 
				int a = 0;
			}			
		};
					
		int[][] array1 = new int[a=2][b=3];		
		array1[a=1][b=2] = 5;
		
		int[][] array2 = new int[a=5][];
		array2[0][10] = 6;
				
		String[][] str1 = {{"s"+"1"},{"s"+"2","s"+"3"}}; 		
		String[][] str2 = new String[][]{{"s1"},{"s2",""}};
		
		VD[][] vd3 = new VD[2][b=3];
		vd3[a=0][b=0] = new VD(a=2, b=5);
		
		int x = add(a=8, b=9);	
	}

	int add(int a, int b){
		return a+b;
	}
}
