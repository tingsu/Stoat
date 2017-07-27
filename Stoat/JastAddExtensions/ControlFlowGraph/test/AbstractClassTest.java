public class AbstractClassTest {
	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
	}
	private static void test1() {
		A a = new C();
		a.m();
	}
	private static void test2() {
		A a = new C();
		a.n();
	}
	private static void test3() {
		B a = new C();
		a.n();
	}
	private static void test4() {
		B a = new C();
		a.m();
	}
}

abstract class A {
	abstract void m();
	void n() {
	}
}

abstract class B extends A {
}

class C extends B {
	void m() {
	}
}

