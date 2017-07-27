package tmp;


public class NullPointerClass {

	public static void main(String[] args) {
		String aaa;
		int bbb;
		String foo = null;
		aaa = foo;
		
		String[] array = new String[5];
		array[2] = null;
		
		foo = "";
		
		String bar = new String("Hello");
		String baz = "World";
		if(foo != null)
			System.out.println(foo);
		if(bar != null)
			System.out.println(bar);
		if(baz != null)
			System.out.println(baz);
		

	}

}
