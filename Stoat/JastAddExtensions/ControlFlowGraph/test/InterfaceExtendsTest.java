public class InterfaceExtendsTest {
	public static void main(String[] args) {
		test1();
		test2();		
		test3();		
		test4();		
		test5();		
		test6();		
		test7();		
		test8();		
		test9();		
		test10();		
		test11();		
		test12();		
		test13();				
	}
	// Possible targets: A.m, B.m, C.m, D.m
	// Correct target: A.m
	private static void test1() {
		I i = new A();
		i.m();
	}
	// Possible targets: A.m, B.m, C.m, D.m
	// Correct target: B.m
	private static void test2() {
		I i = new B();
		i.m();
	}
	// Possible targets: A.m, B.m, C.m, D.m
	// Correct target: C.m
	private static void test3() {
		I i = new C();
		i.m();
	}
	// Possible targets: A.m, B.m, C.m, D.m
	// Correct target: D.m
	private static void test4() {
		I i = new D();
		i.m();
	}
	// Possible targets: B.m, C.m
	// Correct target: B.m
	private static void test5() {
		J j = new B();
		j.m();
	}
	// Possible targets: B.n, C.n
	// Correct target: B.n
	private static void test6() {
		J j = new B();
		j.n();
	}
	// Possible targets: B.m, C.m
	// Correct target: C.m
	private static void test7() {
		J j = new C();
		j.m();
	}
	// Possible targets: B.n, C.n
	// Correct target: B.n
	private static void test8() {
		J j = new C();
		j.n();
	}
	// Possible targets: C.m
	// Correct target: C.m
	private static void test9() {
		K k = new C();
		k.m();
	}
	// Possible targets: C.n
	// Correct target: C.n
	private static void test10() {
		K k = new C();
		k.n();
	}
	// Possible targets: C.o
	// Correct target: C.o
	private static void test11() {
		K k = new C();
		k.o();
	}
	// Possible targets: D.m
	// Correct target: D.m
	private static void test12() {
		L l = new D();
		l.m();
	}
	// Possible targets: D.n
	// Correct target: D.n
	private static void test13() {
		L l = new D();
		l.n();
	}
}

interface I {
	void m();
}
interface J extends I {
	void n();
}
interface K extends J {
	void o();
}
interface L extends I {
	void n();
}
class A implements I {
	public void m() { }
}
class B implements J {
	public void m() { }
	public void n() { }
}
class C implements K {
	public void m() { }
	public void n() { }
	public void o() { }
}
class D implements L {
	public void m() { }
	public void n() { }
}

