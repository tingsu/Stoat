public class ClassExtendsTest {
	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
	}
	// Possible targets: A.m, C.m and D.m
	// Correct target: A.m
	private static void test1() {
		A a = new A();
		a.m();		
	}
	// Possible targets: A.m, C.m, D.m
	// Correct target: A.m
	private static void test2() {
		A a = new B();
		a.m();
	}
	// Possible targets: A.m, C.m, D.m
	// Correct target: A.m
	private static void test3() {
		B b = new B();
		b.m();
	}
	// Possible targets: A.m, C.m, D.m
	// Correct target: C.m
	private static void test4() {
		B b = new C();
		b.m();
	}
	// Possible targets: C.m
	// Correct target: C.m
	private static void test5() {
		C c = new C();
		c.m();
	}
	// Possible targets: D.m, A.m (through cast)
	// Correct target: A.m
	private static void test6() {
		D d = new D();
		((A)d).m();
	}
	
	private static void test7() {
		B b = new C();
		b.n();
	}
}

class A {
	public void m() {
	}
	public void n() {
	}
}

class B extends A {
}

class C extends B {
	public void m() {
	}
}

class D extends B {
	public void m() {
	}
}
