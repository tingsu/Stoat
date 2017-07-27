public class DeadMethodTest {
	public static void main(String[] args) {
		test1();	
	}
	private void test1() {
		A a = new A();
		a.m();
	}
}

class A {
	void m() {
	}
	void n() {
	}
}

class B {
	void m() {
	}
}
	

